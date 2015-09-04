package zhaoiwei.removealsearchresults;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ExtractText {

	private static List<Element> divList=new ArrayList<Element>();
    public String parseUrl(Document doc) {   
    	String text="";
        wash(doc);
        washAll(doc);
        Elements bodychildren=doc.body().children();
        for(Element element : bodychildren)
        {
        	if(judgeBlockTag(element))
        	{
        		addList(element);
        	}
        }
        Iterator<Element> it=divList.iterator();
        while(it.hasNext())
        {
        	int linkcount=0;
        	int contentcount=0;
        	double LC=0;
        	Element candidate=it.next();       		
        	contentcount=candidate.text().length();  //统计该候选快的文本字符个数
        	Elements links=candidate.select("a[href]");
        	linkcount=links.size();                  //统计该候选快的链接数      
        	if(contentcount>50)                      //经过处理后清理后，候选快中任然可能有干扰项，最后一次处理掉噪声。
        	{
        		LC=(double)linkcount/(double)contentcount;
        		if(LC<0.03)
        		{
        			 text += candidate.text();
        		}
        	}
        } 
        return text;
    } 
    public void addList(Element element)
    {
    	Elements children=element.children();
    	if(children==null)
    	{
    		divList.add(element);         //该元素没有子元素，则添加该元素到列表
    		return;
    	}
    	else
    	{
    		boolean havediv=false;
    		for(Element child:children)
    		{
    			if(judgeBlockTag(child))  //该元素子元素为div元素，则递归调用函数
        		{
    				havediv=true;
        			addList(child);
        		}
    		}
    		if(!havediv)
        		divList.add(element);     //如果有子元素，且子元素全部为非div标签的话，也添加该元素到列表。
    	} 	
    	return;
    }
    public boolean judgeBlockTag(Element element)//判断元素对应标签是不是分块标签
    {
    	if(element.tagName().equals("div"))
    		return true;
    	else
    		return false;
    }
    public void washAll(Document doc)
    {
    	Elements es=doc.getAllElements();
    	for(Element element : es)
    	{
    		
    		if(judgeBlockTag(element))          //分块元素，包含文本太少则不太可能为正文，删除
    		{
    			if(element.text().length()<50)
    				element.remove();
    		}
    		else if(element.text().equals(""))  //删除所有文本节点为空的元素
    		{
    			element.remove();
    		}
    	}
    }
    public void wash(Document doc)
    {	
    	Elements script=doc.select("script");
    	for(Element element : script)
    	{
    		element.remove();
    	}
    	Elements form=doc.select("form");
    	for(Element element : form)
    	{
    		element.remove();
    	}
    	Elements meta=doc.select("meta");
    	for(Element element : meta)
    	{
    		element.remove();
    	}
    	Elements style=doc.select("style");
    	for(Element element : style)
    	{
    		element.remove();
    	}
    	Elements iframe=doc.select("iframe");
    	for(Element element : iframe)
    	{
    		element.remove();
    	}
    	Elements font=doc.select("font");
    	for(Element element : font)
    	{
    		element.remove();
    	}
    }
    
}
