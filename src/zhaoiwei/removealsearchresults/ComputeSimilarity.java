package zhaoiwei.removealsearchresults;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

public class ComputeSimilarity {
	
	public static double computeSimilarity(String text1,String text2){
		Map<String,int[]> map = new HashMap<String,int[]>(500);
		String word;
		int[] tempArray;
		
		StringReader reader = new StringReader(text1);
		IKSegmenter ik = new IKSegmenter(reader,true);// 当为true时，分词器进行最大词长切分
		Lexeme lexeme = null;
		try {
		    while((lexeme = ik.next())!=null){
		    	word = lexeme.getLexemeText();
		        //System.out.println(word);
		        if (map.containsKey(word)){
		        	map.get(word)[0]++;
		        }else {
		        	tempArray = new int[2];
		        	tempArray[0] = 1;
		        	tempArray[1] = 0;
		        	map.put(word, tempArray);
		        }
		    }
		} catch (IOException e) {
		    e.printStackTrace();
		} finally{
		    reader.close();
		}
		
		reader = new StringReader(text2);
		ik = new IKSegmenter(reader,true);// 当为true时，分词器进行最大词长切分
		lexeme = null;
		try {
		    while((lexeme = ik.next())!=null){
		    	word = lexeme.getLexemeText();
		        //System.out.println(word);
		        if (map.containsKey(word)){
		        	map.get(word)[1]++;
		        }else {
		        	tempArray = new int[2];
		        	tempArray[1] = 1;
		        	tempArray[0] = 0;
		        	map.put(word, tempArray);
		        }
		    }
		} catch (IOException e) {
		    e.printStackTrace();
		} finally{
		    reader.close();
		}
		
		double vector1Modulo = 0.00;//向量1的模  
        double vector2Modulo = 0.00;//向量2的模  
        double vectorProduct = 0.00; //向量积  
        Iterator<Entry<String, int[]>> iter = map.entrySet().iterator();  
          
        while(iter.hasNext())  
        {  
            Map.Entry entry = (Map.Entry)iter.next();  
            tempArray = (int[])entry.getValue();  
              
            vector1Modulo += tempArray[0]*tempArray[0];  
            vector2Modulo += tempArray[1]*tempArray[1];  
              
            vectorProduct += tempArray[0]*tempArray[1];  
        }  
          
        vector1Modulo = Math.sqrt(vector1Modulo);  
        vector2Modulo = Math.sqrt(vector2Modulo);  
          
        //返回相似度  
       return (vectorProduct/(vector1Modulo*vector2Modulo));  
	}
	
}
