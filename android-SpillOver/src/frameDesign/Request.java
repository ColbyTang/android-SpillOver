package frameDesign;

import java.util.Map;

/****
 * 1.������post get		Ĭ��post
 * 2.������request���ȼ�		Ĭ����ͬ
 * 3.�������Ƿ���л���		Ĭ�ϻ���
 * 4.����������ͷ			Ĭ��null
 * 5.�����ò���ͷ			Ĭ��null			
 * 6.ǿ�����ûٵ��ӿ�
 * 
 */
public abstract class Request <T> implements Comparable<Request<T>> {
	
	public ResponseListener<?> listener;
	
	private String mUrl;
	
	Request(String url ,ResponseListener<?> listener){
		this.listener = listener;
		this.mUrl = url;
	}
	
	interface ResponseListener<T>{
		public void callBack(T arg0);
	}
	
	public Method method = Method.POST; 
	
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

	protected Map<String,String> headers;	//����ͷ
	
	protected Map<String,String> params;	//�������
	
	public abstract Map<String,String> getHeader();	//�ص�����ͷ
	
	public abstract Map<String,String> getParam(); 	//�ص������б�

	protected abstract T handlerCallBack();
	

	
}
