package frameDesign;

import java.util.Map;

/****
 * 1.能设置post get		默认post
 * 2.能设置request优先级		默认相同
 * 3.能设置是否进行缓存		默认缓存
 * 4.能设置请求头			默认null
 * 5.能设置参数头			默认null			
 * 6.强制设置毁掉接口
 * 
 */
public abstract class Request <T> implements Comparable<Request<T>> {
	
	public ResponseListener<T> listener;
	
	private String mUrl;
	
	public Request(String url ,ResponseListener<T> listener){
		this.listener = listener;
		this.mUrl = url;
	}
	
	public interface ResponseListener<T>{
		//这里的泛型是个坑。。。
		public void callBack(Object responseData);
		
		public void callErrorBack();
	}
	
	public Method method = Method.GET; 
	
	public static enum Method{
		GET,
		POST
	}
	private enum Priority{
		LOW,
		MEDIUM,
		HIGH
	}
	
	public Priority getPriority(){
		return Priority.MEDIUM; 
	}
	
	@Override
	public int compareTo(Request<T> another) {
		if(this.getPriority() == another.getPriority()){
			return 0;
		}
		return another.getPriority().ordinal() - this.getPriority().ordinal();
	}
	
	
	public boolean shouldCache(){
		return true;
	}
	
	public String getUrl(){
		return mUrl;
	}
	
	public void reWriteUrl(String str){
		this.mUrl = str;
	}
	
	private String Etag = null;
	
	public String getEtag() {
		return Etag;
	}

	public void setEtag(String etag) {
		Etag = etag;
	}
	
	private String iMS = null;
	
	public String getiMS() {
		return iMS;
	}

	public void setiMS(String iMS) {
		this.iMS = iMS;
	}

	protected Map<String,String> headers;	//请求头
	
	protected Map<String,String> params;	//请求参数
	
	public abstract Map<String,String> getHeader();	//回调请求头
	
	public abstract Map<String,String> getParam(); 	//回调参数列表

	protected abstract T handlerCallBack(byte[] responseContent, String callBackdata);
	
}
