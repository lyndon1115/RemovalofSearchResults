package zhaoiwei.removealsearchresults;

public class SearchResult {

	private final String url;
	private final String text;

	
	public SearchResult(String url,String text){
		this.url = url;
		this.text = text;
	}
	public String getUrl() {
		return url;
	}
	public String getText(){
		return text;
	}
	
	
}
