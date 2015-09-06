package zhaoiwei.removealsearchresults;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String url1 = "http://yangshangchuan.iteye.com/blog/2195559";
		String url2 = "http://my.oschina.net/apdplat/blog/391023";
		String content1 = DownloadWebPage.crawlPageContent(url1);
		String text1 = TextExtract.parse(content1);
		//Spider.writeToFile(content1, "/home/zhaoiwei/spider/testweb.html");
		//Spider.writeToFile(text1, "/home/zhaoiwei/spider/testwebtext.txt");
		String content2 = DownloadWebPage.crawlPageContent(url2);
		String text2 = TextExtract.parse(content2);
		System.out.println(ComputeSimilarity.computeSimilarity(text1, text2));
	}

}
