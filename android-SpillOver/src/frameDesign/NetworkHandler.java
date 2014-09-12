package frameDesign;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHttpResponse;

import android.util.Log;

import file.Cache;
import file.Cache.Entry;
import file.IndexPoolOverflowException;

public class NetworkHandler extends Thread{

	private HttpHeap mHttpHeap = null;
	
	private BlockingQueue<Request<?>> mQueue = null;

	private Cache mCache = null;
	
	private ResponseParse mResponseParse = null;

	protected static final int DEFAULT_POOL_SIZE = 4096;
	
	private ResponseHandler mCallBack = null;
	
	private boolean isCancel = false;
	
	public boolean isCancel() {
		return isCancel;
	}

	public void setCancel(boolean isCancel) {
		this.isCancel = isCancel;
	}

	/**
	 * �ⲿ�ӿ�
	 * @param mQueue
	 * @param mCache
	 * @param mHttpHeap
	 * @param parse
	 * @param response
	 */
	public NetworkHandler(BlockingQueue<Request<?>> mQueue, Cache mCache,
			HttpHeap mHttpHeap , ResponseParse parse , ResponseHandler response) {
		this.mQueue = mQueue;
		this.mCache = mCache;
		this.mHttpHeap = mHttpHeap;
		this.mResponseParse = parse;
		this.mCallBack = response;
	}
	
	/**
	 * ͬ���Ľӿ�,ʹ��ͬ��������NetworkHandler�������й����󻺴������;
	 * @param mCache
	 * @param mHttpHeap
	 * @param parse
	 * @param response
	 */
	protected NetworkHandler(Cache mCache,
			HttpHeap mHttpHeap , ResponseParse parse , ResponseHandler response){
		this.mCache = mCache;
		this.mHttpHeap = mHttpHeap;
		this.mResponseParse = parse;
		this.mCallBack = response;
	}
	
	/**
	 * ͬ���ӿ�,����304���,�������ֱ�ӻص�
	 * 
	 * @param request
	 * @throws IOException 
	 */
	protected void noModifiedHandler(Request<?> request,Map<String,String> responseHeaders) throws IOException{
		Cache.Entry entry = mCache.get(request.getUrl());
		callBackResult(request,entry.datas,responseHeaders);
	}

	/**
	 * ͬ���ӿ�,����ttl֮��Ļ������,��Ϊttl���漰�ⲿ�Ĵ���,����ֻ�ܰ����ֳ���
	 * 
	 * @param requestKey
	 * @param entry
	 * @param responseHeaders
	 * @param responseContent
	 * @throws IOException
	 * @throws IndexPoolOverflowException
	 */
    protected void cacheWithoutTTL(String requestKey,Cache.Entry entry, Map<String, String> responseHeaders,
    		byte[] responseContent) throws IOException, IndexPoolOverflowException {
		entry.expires = mResponseParse.parseExpires(responseHeaders.get("Expires"), responseHeaders.get("Date"));
		entry.iMS = responseHeaders.get("Last-Modified");
		entry.etag = responseHeaders.get("ETag");
		entry.headers = responseHeaders;
		entry.datas = responseContent;
		mCache.put(requestKey, entry); 		
	}
	
    protected void callBackResult(Request<?> request , byte[] responseContent , Map<String,String> responseHeaders){
		String callBackdata = mResponseParse.byteToEntity(responseContent,responseHeaders);
    	mCallBack.callBack(request, responseContent,callBackdata);    	
    }
    
	@Override
	public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		while(true){
			Request<?> request = null;
			try {
				request = mQueue.take();
				BasicHttpResponse response = mHttpHeap.handlerRequest(request);
				if(response == null){
					mCallBack.callErrorBack(request);
					continue;
				}
				byte[] responseContent = mResponseParse.entityToBytes(
						response.getEntity(), new ByteArrayPool(DEFAULT_POOL_SIZE));
				Map<String,String> responseHeaders = convertHeaders(response.getAllHeaders());
		        StatusLine statusLine = response.getStatusLine();
		        int statusCode = statusLine.getStatusCode();
		        
				//304����;
		        if(statusCode == HttpStatus.SC_NOT_MODIFIED){
		        	noModifiedHandler(request,responseHeaders);
		        	continue;
				}
		        
		        // ��û��� 
				if(request.shouldCache()){
					Cache.Entry entry = new Cache.Entry();
					long ttl = mResponseParse.parseTtl(responseHeaders.get("Cache-Control"));
					if(ttl == -1){
						callBackResult(request,responseContent,responseHeaders);
						continue;
					} 
					entry.ttl = ttl;
					cacheWithoutTTL(request.getUrl(),entry,responseHeaders,responseContent);
				}
				callBackResult(request,responseContent,responseHeaders);
			} catch (IOException e) {
				if(request != null){
					mCallBack.callErrorBack(request);
				}
			} catch (InterruptedException e) {
				if(isCancel){
					Thread.currentThread().interrupt();
				} 
				this.start();
			} catch (ServerError e) {
				e.printStackTrace();
			} catch (IndexPoolOverflowException e) {
				e.printStackTrace();
			}
		}
	}

	

	private static Map<String, String> convertHeaders(Header[] headers) {
        Map<String, String> result = new HashMap<String, String>();
        for (int i = 0; i < headers.length; i++) {
            result.put(headers[i].getName(), headers[i].getValue());
        }
        return result;
    }
	
}
