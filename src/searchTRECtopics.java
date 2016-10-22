import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;

public class searchTRECtopics {

	public static void main(String[] args) throws ParseException, IOException {
		searchTRECtopics search = new searchTRECtopics();
		search.searchTopics();
	}
	
	public void searchTopics() throws ParseException, IOException{
		File file  = new File("./res/topics.51-100");
		BufferedWriter shortWriter = new BufferedWriter(new FileWriter("./output/easyShortQuery.txt"));
		BufferedWriter longWriter = new BufferedWriter(new FileWriter("./output/easyLongQuery.txt"));
		
		ArrayList<QueryInput> queries = getQueries(file);
		easySearch search = new easySearch();
		
		int queryNo = 0;
		for(QueryInput query : queries){
			System.out.println("Searching for Small Query No " + queryNo);
			List<Document> docScores = search.getRank(query.getTopic());
			Collections.sort(docScores, Comparator.comparingDouble(Document::getScore).reversed());
			queryNo++;
			//code to write to file
			outputSearchResults(shortWriter, queryNo, docScores, "easyAlgo_short");
		}
		
		queryNo = 0;
		for(QueryInput query : queries){
			System.out.println("Searching for Small Query No " + queryNo);
			List<Document> docScores = search.getRank(query.getDescription());
			Collections.sort(docScores, Comparator.comparingDouble(Document::getScore).reversed());
			queryNo++;
			//code to write to file
			outputSearchResults(longWriter, queryNo, docScores, "easyAlgo_long");
		}
		
		shortWriter.close();
		longWriter.close();
	}
	
	public ArrayList<QueryInput> getQueries(File file) throws IOException{
		ArrayList<QueryInput> queries = new ArrayList<QueryInput>();
		//code to parse
		BufferedReader fileReader = new BufferedReader(new FileReader(file));
	    String lineText = null;
	    int queryNo = 0;
	    QueryInput query = null;
	    while ((lineText = fileReader.readLine()) != null) {
	    	if(lineText.contains("<top>")){
	    		query = new QueryInput();
	    		query.setQueryNo(queryNo++);
	    		continue;
	    	}else if(lineText.contains("</top>")){
	    		
	    		System.out.println("Query No: " + query.getQueryNo());
	    		System.out.println("Query Topic: " + query.getTopic());
	    		System.out.println("Query Desc: " + query.getDescription());
	    		
	    		queries.add(query);
	    		continue;
	    	}
	    	if(lineText.contains("<title>")){
	    		String[] splitText = lineText.split("Topic:");
	    		query.setTopic(splitText[1].trim());
	    		continue;
	    	}
	    	if(lineText.contains("<desc>")){
	    		String desc = "";
	    		String[] splitText = lineText.split("Description:");
	    		if(splitText.length > 1){
	    			desc += splitText[1].trim() + " "; 
	    		}
	    		while(!(lineText = fileReader.readLine()).contains("<smry>")){
	    			desc += lineText.trim() + " ";
	    		}
	    		query.setDescription(desc);
	    		continue;
	    	}
	    }
	    return queries;
	}
	
	public void outputSearchResults(BufferedWriter writer, int queryNo, List<Document> docScores, String algorithm) throws IOException{
		for(int i=0; i<1000; i++){
			writer.append((50+queryNo)+" ");
			writer.append(0+" ");
			writer.append(docScores.get(i).getDocID()+" ");
			writer.append(i+1+" ");
			writer.append(docScores.get(i).getScore()+" ");
			writer.append(algorithm);
			writer.newLine();
		}
	}

}
