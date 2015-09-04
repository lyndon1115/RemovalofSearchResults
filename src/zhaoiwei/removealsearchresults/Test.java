package zhaoiwei.removealsearchresults;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String url = "http://zhidao.baidu.com/link?url=eneA_t--qShb1HckDXjGnuBOCOR_-afD6eSwNsf7At_OFmhIuva4QnWyhd0Bo6W3s8zEZB_67NwoDF5m5qPrT_";
		Spider.initHttpClient();
		String content = Spider.crawlPageContent(url);
		String text = TextExtract.parse(content);
		Spider.writeToFile(text, "/home/zhaoiwei/spider/testContent.txt");
	}

}
