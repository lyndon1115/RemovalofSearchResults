package zhaoiwei.removealsearchresults;

public class WebTextResult {

	final SearchResult searchResult;
	final String bodyText;
	
	public WebTextResult(SearchResult sr, String bodyText){
		this.searchResult = sr;
		this.bodyText = bodyText;
	}
	public SearchResult getSearchResult() {
		return searchResult;
	}
	public String getBodyText() {
		return bodyText;
	}

}
