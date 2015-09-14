package zhaoiwei.removealsearchresults;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Spider {
	
	static class DuplicateInfo{
		String title1,title2;
		public DuplicateInfo(String t1,String t2){
			title1 = t1;
			title2 = t2;
		}
	}
	public static List<DuplicateInfo> duplicateInfo = new ArrayList<DuplicateInfo>();
	public static List<SearchResult> urlList;
	public static LinkedBlockingQueue<WebTextResult> textBlockingQueue = new LinkedBlockingQueue<WebTextResult>();
	final private static String URL= "http://www.baidu.com/s?wd=";  
    public final static int NUMBER_RETRY = 16;
    public final static int GETWEBTEXT_THREADNUMBER = 20;
    public final static double SIMILARITY_THRESHOLD = 0.7;
    public static int searchNum = 10;
	public AtomicInteger removalNumber = new AtomicInteger(0);
	
	public static class DuplicateRemoval implements Runnable{
		List<WebTextResult> list;
    	public DuplicateRemoval(List<WebTextResult> list){
    		this.list = list;
    	}
		
		public void run() {
			WebTextResult text1,text2;
			double similarityRatio;
			for (int i = 0; i < list.size() - 1; ++i){
				if (list.get(i) == null)
					continue;
				text1 = list.get(i);
				for (int j = i + 1; j < list.size(); ++j){
					if (list.get(j) == null)
						continue;
					text2 = list.get(j);
					similarityRatio = ComputeSimilarity.computeSimilarity(text1.getBodyText(), text2.getBodyText());
					if (similarityRatio > SIMILARITY_THRESHOLD){
						duplicateInfo.add(new DuplicateInfo(text1.getSearchResult().getText(),text2.getSearchResult().getText()));
						list.set(j,null);
					}
				}
			}
			return ;
		}
		
	}
	public static List<WebTextResult> removal(){
		List<WebTextResult> textlist = new ArrayList<WebTextResult>(searchNum+1);
		while (!textBlockingQueue.isEmpty()){
			try {
    			textlist.add(textBlockingQueue.take());
    		} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		List<WebTextResult> list1 = textlist.subList(0, (textlist.size() / 2));
		List<WebTextResult> list2 = textlist.subList((textlist.size() / 2), textlist.size());
		DuplicateRemoval d1 = new DuplicateRemoval(list1);
		DuplicateRemoval d2 = new DuplicateRemoval(list2);
		Thread t1 = new Thread(d1);
		Thread t2 = new Thread(d2);
		t1.start();
		t2.start();
		try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		int con = 0;
		WebTextResult text1,text2;
		double similarityRatio;
		for (int i = 0; i < list1.size(); ++i){
			if (list1.get(i) == null){
				continue;
			}
			text1 = list1.get(i);
			for (int j = 0; j < list2.size(); ++j){
				if (list2.get(j) == null){
					continue;
				}
				text2 = list2.get(j);
				similarityRatio = ComputeSimilarity.computeSimilarity(text1.getBodyText(), text2.getBodyText());
				if (similarityRatio > SIMILARITY_THRESHOLD){
					duplicateInfo.add(new DuplicateInfo(text1.getSearchResult().getText(),text2.getSearchResult().getText()));
					list2.set(j,null);
				}
			}
		}
		List<WebTextResult> anslist = new ArrayList<WebTextResult>(searchNum+1);
		for (int i = 0; i < textlist.size(); ++i){
			if (textlist.get(i) == null){
				con++;
				continue;
			}
			anslist.add(textlist.get(i));
		}
		System.out.println("去除重复结果：" + con + "条");
		return anslist;
	}
    public static class GetWebText implements Runnable{
    	List<SearchResult> list;
    	public GetWebText(List<SearchResult> list){
    		this.list = list;
    		//System.out.println(this.list.get(0).getText());
    	}
		public void run() {
			try {
				SearchResult tt;
				String content,webText;
				for (int i = 0; i < list.size(); ++i){
					tt = list.get(i);
					content =  DownloadWebPage.crawlPageContent(tt.getUrl());
					if (content != null)
						webText = TextExtract.parse(content);
					else 
						webText = tt.getText();
					textBlockingQueue.put(new WebTextResult(tt,webText));
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
    	
    }
    
    public static void getWebText(){
    	int size = urlList.size();
    	int chu = size / GETWEBTEXT_THREADNUMBER , yu = size % GETWEBTEXT_THREADNUMBER;
    	int index = 0;
    	int threadnum = (chu == 0 ? yu : GETWEBTEXT_THREADNUMBER);
    	for (int i = 0; i < threadnum; ++i){
    		Thread th;
    		GetWebText t;
    		if (i < yu){
    			t = new GetWebText(urlList.subList(index, index + chu +1));
    			index = index + chu +1; 			
    		}else {
    			t = new GetWebText(urlList.subList(index, index + chu));
    			index = index + chu;
    		}
    		th = new Thread(t);
			th.start();  
    		try {
				th.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
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
	
	public static void spider(String searchStr,int num) throws SearchResultException, URISyntaxException{
		String url = URL + searchStr;
		urlList = new ArrayList<SearchResult>(num+1);
		int numPage = (num + 9) / 10, numResult = 0;
		int numOfNull = 0;
		final String filepath = "/home/zhaoiwei/spider/out";
	    for (int page = 0; page < numPage; ++page){
			String content =DownloadWebPage.crawlPageContent(url);  
			if (page == 0){
				content =DownloadWebPage.crawlPageContent(url);  
			}else {
				URI uri = new URI(url);
				content =DownloadWebPage.crawlPageContent(url,uri);
			}
			
		    writeToFile(content,filepath + page +".html");
		    Document dd = Jsoup.parse(content);
		    //System.out.println(dd.title());
		    Elements links = dd.getElementsByTag("h3");
		    //System.out.println("links : " + links.size());
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
		    	urlList.add(new SearchResult(linkHref,linkText));
		    	numResult++;
		    	if (numResult >= num)
		    		return ;
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
	    return ;
	}
	
	
	public static void main(String[] args) {

		Scanner in = new Scanner(System.in);
		while (true){
			System.out.print("请输入要搜索的关键字(#END结束搜索): ");
		    String keyword = in.nextLine(); //要查找的关键字  
		    if (keyword.trim().equals("#END"))
		    	break;
		    System.out.print("请输入要提取的关键字个数： ");
		    int numResult = in.nextInt();
		    if (numResult > 100)//限制最大搜索结果为100条
		    	numResult = 100;
		    searchNum = numResult;
		    in.nextLine();
		    try {
		    	spider(keyword,numResult);
		    	System.out.println("正在爬取网页");
		    	getWebText();
		    	System.out.println("正在进行相似度比较");
		    	urlList = null;//help GC
		    	List<WebTextResult> removalResult = removal();
		    	System.out.println("去除重复后的搜索结果");
		    	for (int i = 0; i < removalResult.size(); ++i){
		    		System.out.println(i + " : " +removalResult.get(i).getSearchResult().getText());
		    		System.out.println(removalResult.get(i).getSearchResult().getUrl());
		    		System.out.println();
		    	}
		    	System.out.println("重复的搜索结果");
		    	for (DuplicateInfo t : duplicateInfo){
		    		System.out.println(t.title1);
		    		System.out.println(t.title2);
		    		System.out.println();
		    	}
			} catch (SearchResultException e) {
				e.printStackTrace();
			} catch (URISyntaxException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		    break;
		}
		in.close();
	}  
}
