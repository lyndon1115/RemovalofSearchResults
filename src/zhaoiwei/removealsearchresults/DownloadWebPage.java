package zhaoiwei.removealsearchresults;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.ByteOrderMarkDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.UnicodeDetector;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

public class DownloadWebPage {

	public final static int MAX_TOTAL_CONNECTIONS = 800;  
    public final static int WAIT_TIMEOUT = 60000;  
    public final static int MAX_ROUTE_CONNECTIONS = 400;  
    public final static int CONNECT_TIMEOUT = 10000;  
    public final static int READ_TIMEOUT = 60000;  
    private static HttpClient httpClient;  
	static{  
	      
	    HttpParams params = new BasicHttpParams();  
	    SchemeRegistry schemeRegistry = new SchemeRegistry();  
	    schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));  
	    schemeRegistry.register(new Scheme("https",443,SSLSocketFactory.getSocketFactory()));  
	    PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);  
	    httpClient = new DefaultHttpClient(cm, params);  
	    new DecompressingHttpClient(httpClient);  
	    cm.setMaxTotal(MAX_TOTAL_CONNECTIONS);  
	    cm.setDefaultMaxPerRoute(MAX_ROUTE_CONNECTIONS);  
	    HttpHost localhost = new HttpHost("locahost", 80);  
	    cm.setMaxPerRoute(new HttpRoute(localhost), 50);  
	    httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECT_TIMEOUT);  
	    httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, READ_TIMEOUT);  
	}  
	  
	/** 
	 * 初始化消息头  
	 * @param httpGet 
	 * @param url 
	 * @throws URISyntaxException 
	 * @throws MalformedURLException 
	 */  
	public static void initHeader(HttpGet httpGet,String strUrl) throws URISyntaxException, MalformedURLException{  
		URL url = new URL(strUrl);
		URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null);
		httpGet.setURI(uri);  
	    httpGet.addHeader("Accept-Language", "en-us");  
	    httpGet.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36");
	//  httpGet.addHeader("Accept-Encoding", "gzip,deflate");  
	}  
	
	public static String crawlPageContent(String url,URI uri){
		HttpGet httpGet = new HttpGet();  
	    InputStream inputStream = null;  
	    try {  
	    	httpGet.setURI(uri);  
		    httpGet.addHeader("Accept-Language", "en-us");  
		    httpGet.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36");
	        HttpResponse response = httpClient.execute(httpGet); 
	        HttpEntity entity = response.getEntity();  
	        String encode = getEncoding(url);  
	        if(encode.equals("windows-1252")){  
	            encode = "GBK";  
	        }  
	        if (entity != null) {  
	            inputStream = entity.getContent();  
	            String content = EntityUtils.toString(entity, encode);  
	            return content;  
	        }  
	        return null;  
	    } catch (ClientProtocolException e) {  
	        e.printStackTrace();  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }  finally {  
	        if (inputStream != null) {  
	            try {  
	                inputStream.close();  
	            } catch (IOException e) {  
	                e.printStackTrace();  
	            }  
	        }  
	    }  
	    return null;  
	}
	
	public static String crawlPageContent(String url){
		return crawlPageContent(httpClient,url);
	}
	
	public static String crawlPageContent(HttpClient httpClient, String url){  
	    HttpGet httpGet = new HttpGet();  
	    InputStream inputStream = null;  
	    try {  
	        initHeader(httpGet,url);  
	        HttpResponse response = httpClient.execute(httpGet); 
	        HttpEntity entity = response.getEntity();  
	        String encode = getEncoding(url);  
	        if(encode.equals("windows-1252")){  
	            encode = "GBK";  
	        }  
	        if (entity != null) {  
	            inputStream = entity.getContent();  
	            String content = EntityUtils.toString(entity, encode);  
	            return content;  
	        }  
	        return null;  
	    } catch (Exception e) {  
	      return null;
	    } finally {  
	        if (inputStream != null) {  
	            try {  
	                inputStream.close();  
	            } catch (IOException e) {  
	                e.printStackTrace();  
	            }  
	        }  
	    }   
	}  
	  
	  
	/** 
	 *分析页面编码 用到包cpdetector.jar,chardet.jar 
	 */  
	private static CodepageDetectorProxy detector;  
	  
	public static String getEncoding(String url) {  
	    java.nio.charset.Charset charset = null;  
	    detector = CodepageDetectorProxy.getInstance();  
	    detector.add(new ByteOrderMarkDetector());  
	    detector.add(ASCIIDetector.getInstance());  
	    detector.add(UnicodeDetector.getInstance());  
	    detector.add(JChardetFacade.getInstance());  
	    try {  
	        charset = detector.detectCodepage(new URL(url));  
	    } catch (MalformedURLException e1) {  
	        e1.printStackTrace();  
	    } catch (IOException e1) {  
	        //e1.printStackTrace();  
	    }  
	    if (charset == null) {  
	        return "utf-8";  
	    }  
	    return charset.toString();  
	}  
}
