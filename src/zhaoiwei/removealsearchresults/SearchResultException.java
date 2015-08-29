package zhaoiwei.removealsearchresults;

public class SearchResultException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public SearchResultException(){
		super("搜索结果格式不正确或爬取失败！");
	}
	
	public SearchResultException(String msg){
		super(msg);
	}

}
