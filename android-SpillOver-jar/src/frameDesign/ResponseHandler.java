package frameDesign;

public interface ResponseHandler {
	public void callBack(final Request<?> request,final String responseData);
}