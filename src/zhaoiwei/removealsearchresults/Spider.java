package zhaoiwei.removealsearchresults;

import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.ByteOrderMarkDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.UnicodeDetector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Spider {

	final private static String URL= "http://www.baidu.com/s?wd=";  
    public final static int MAX_TOTAL_CONNECTIONS = 800;  
    public final static int WAIT_TIMEOUT = 60000;  
    public final static int MAX_ROUTE_CONNECTIONS = 400;  
    public final static int CONNECT_TIMEOUT = 10000;  
    public final static int READ_TIMEOUT = 60000;  
    public final static int NUMBER_RETRY = 16;
    private static HttpClient httpClient;  
	public static void  initHttpClient(){  
	      
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
	  
	  
	/** 
	 * 爬取网页 上所有内容 
	 * @param httpClient 
	 * @param url 
	 * @return 
	 */  
	
	public static String crawlPageContent(String url){
		return crawlPageContent(httpClient,url);
	}
	
	public static String crawlPageContent(HttpClient httpClient, String url){  
	    HttpGet httpGet = new HttpGet();  
	    InputStream inputStream = null;  
	    try {  
	        initHeader(httpGet,url);  
	        HttpResponse response = httpClient.execute(httpGet); 
	        System.out.println("Content-Encoding:" + response.getHeaders("Content-Encoding"));
	        HttpEntity entity = response.getEntity();  
	        String encode = getEncoding(url);  
	         if(encode.equals("windows-1252")){  
	             encode = "GBK";  
	         }  
	        if (entity != null) {  
	            inputStream = entity.getContent();  
	            String content = EntityUtils.toString(entity,encode);  
	            return content;  
	        }  
	        return null;  
	    } catch (ClientProtocolException e) {  
	        e.printStackTrace();  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    } catch (URISyntaxException e) {  
	        e.printStackTrace();  
	    } finally {  
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
	  
	
	public static void writeToFile(String content,String filename){
		 try{
			    File file = new File(filename);
			    if (!file.exists()){
			    	file.createNewFile();
			    }
			    OutputStream os = new FileOutputStream(file);
			    os.write(content.getBytes());
				os.close();
		    }catch (Exception e){
		    	e.printStackTrace();
		    }
	}
	
	public static void printResultLinkList(List<SearchResult> list){
		SearchResult sr;
    	for (int i = 0; i < list.size(); ++i){
    		sr = list.get(i);
    		System.out.println("================= " + i + " =====================");
    		System.out.println(sr.getUrl());
    		System.out.println(sr.getText());
    	}
	}
	
	public static List<SearchResult> spider(String searchStr,int num) throws SearchResultException{
		String url = URL + searchStr;
	    List<SearchResult> resultList = new ArrayList<SearchResult>(num+1);
		int numPage = (num + 9) / 10, numResult = 0;
		int numOfNull = 0;
		final String filepath = "/home/zhaoiwei/spider/out";
	    for (int page = 0; page < numPage; ++page){
			String content =crawlPageContent(httpClient,url);  
			
		    writeToFile(content,filepath + page +".html");
		    Document dd = Jsoup.parse(content);
		    System.out.println(dd.title());
		    Elements links = dd.getElementsByTag("h3");
		    if (links == null || links.isEmpty()){
		    	numOfNull++;
		    	if (numOfNull > NUMBER_RETRY){
		    		throw new SearchResultException();
		    	}
		    	page--;
		    	System.out.println("链接失败，正在重试！");
		    	continue;
		    }
		    String linkHref,linkText;
		    for (Element link : links) { 
		    	linkHref = link.getElementsByTag("a").attr("href");//百度搜索结果链接
		    	linkText = link.text();		    	//百度搜索结果链接对应的文字
		    	resultList.add(new SearchResult(linkHref,linkText));
		    	numResult++;
		    	if (numResult >= num)
		    		return resultList;
		    }     
		    Element div = dd.getElementById("page");
		    if (div == null)
		    	break;
		    Elements nextPage = div.getElementsByClass("n");
		    
		    for (Element link : nextPage) { 
		    	linkHref = link.attr("href");
		    	url = "http://www.baidu.com" + linkHref;
		    }
	    }
	    return resultList;
	}
	
	public static String getBodyText(String url){
		String content = crawlPageContent(httpClient, url);
		Document dd = Jsoup.parse(content);
		StringBuilder sb = new StringBuilder();
		Elements elements = dd.getElementsByTag("p");
		String str;
	    for(Element e : elements){
	    	str = e.text();
	    	sb.append(str);
	    }
		String bodyText = sb.toString();
		return bodyText;
	}
	
	public static void main(String[] args) {

	    initHttpClient();  
		Scanner in = new Scanner(System.in);
		while (true){
			System.out.print("请输入要搜索的关键字(#结束搜索): ");
		    String keyword = in.nextLine(); //要查找的关键字  
		    if (keyword.trim().equals("#"))
		    	break;
		    System.out.print("请输入要提取的关键字个数： ");
		    int numResult = in.nextInt();
		    in.nextLine();
		    try {
		    	List<SearchResult> list = spider(keyword,numResult);
		    	printResultLinkList(list);
		    	String content = crawlPageContent(httpClient,list.get(0).getUrl());
		    	writeToFile(content, "/home/zhaoiwei/spider/out.html");
//		    	String bodyText = getBodyText(list.get(0).getUrl());
//				writeToFile(bodyText, "/home/zhaoiwei/spider/body1.txt");
				ExtractText extract = new ExtractText();
				String text = extract.parseUrl(Jsoup.parse(content));
				writeToFile(text, "/home/zhaoiwei/spider/body.txt");
		    	
			} catch (SearchResultException e) {
				e.printStackTrace();
			}
		}
		in.close();
	}  
}
