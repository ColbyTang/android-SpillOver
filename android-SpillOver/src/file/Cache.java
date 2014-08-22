package file;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * �����쳣�Ҳ�����,����Ҫ�ڻ���֮ǰ��֤ԭ����
 * 
 * ����Ψһ���������Ҫ������̫����ô�����ڲ�ץ�����쳣,���ߵ������ݶ�ʧ
 * @author user
 *
 */
public abstract class Cache {
	
	public Cache(BasicCalculator basicCalculator, File cacheDir) {
		
	}
	
	public abstract void put(String requestKey,Cache.Entry entry) throws IOException ,IndexPoolOverflowException;
	
	public abstract Cache.Entry get(String requestKey) throws IOException ;
	
	public static class Entry{
		
		public String etag;	//�ⲿ���д���,���etagû�о�����Ϊnull
		
		public String iMS; //�ⲿ���д���,���last - modifiedû�о�����Ϊnull
		
		public long ttl;
		
		public long expires;
		
		public Map<String,String> headers;
		
		public byte[] datas;
		
	}
	
	public abstract boolean initialize();
	
	protected static Iterator<Object> iterator(Cache.Entry entry) {
		final List<Object> mlist = new ArrayList<Object>();
		mlist.add(entry.etag);
		mlist.add(entry.iMS);
		mlist.add(entry.ttl);
		mlist.add(entry.expires);
		mlist.add(entry.headers);
		mlist.add(entry.datas);
		
		return new Iterator<Object>(){
			
			private int record = -1;
			
			@Override
			public boolean hasNext() {
				record++;
				return record <= 5 ? true : false;
			}

			@Override
			public Object next() {
				return mlist.get(record);
			}

			@Override
			public void remove() {
				
			}
			
		};
	}
	
}
