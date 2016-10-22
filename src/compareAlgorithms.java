import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopDocsCollector;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.store.FSDirectory;

public class compareAlgorithms {

	public static void main(String[] args) throws ParseException, IOException {
		compareAlgorithms search = new compareAlgorithms();
		search.searchTopics("VSM");
		search.searchTopics("BM25");
		search.searchTopics("LMDirchlet");
		search.searchTopics("LMJelinekMercer");
	}
	
	public void searchTopics(String algorithm) throws ParseException, IOException{
		
		File file  = new File("./res/topics.51-100");
		
		String shortQueryFile = algorithm + "shortQuery.txt";
		String longQueryFile = algorithm + "longQuery.txt";
		BufferedWriter shortWriter = new BufferedWriter(new FileWriter("./output/"+shortQueryFile));
		BufferedWriter longWriter = new BufferedWriter(new FileWriter("./output/"+longQueryFile));
		
		searchTRECtopics searchTopic = new searchTRECtopics(); 
		ArrayList<QueryInput> queries = searchTopic.getQueries(file);
		
		//Code for Short Query
		int queryNo = 0;
		for(QueryInput query : queries){
			System.out.println("Searching for Small Query No " + queryNo);
			IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("./res/index")));
			IndexSearcher searcher = new IndexSearcher(reader);
			
			if(algorithm.equals("BM25")) searcher.setSimilarity(new BM25Similarity());
			else if(algorithm.equals("VSM")) searcher.setSimilarity(new ClassicSimilarity());
			else if(algorithm.equals("LMDirchlet")) searcher.setSimilarity(new LMDirichletSimilarity());
			else if (algorithm.equals("LMJelinekMercer")) searcher.setSimilarity(new LMJelinekMercerSimilarity(0.7f));
			else{
				System.out.println("This algorithm is not available");
				return;
			}
			
			Analyzer analyzer = new StandardAnalyzer();
			QueryParser queryParser = new QueryParser("TEXT",analyzer);
			Query queryinput = queryParser.parse(QueryParser.escape(query.getTopic()));
			TopDocs results = searcher.search(queryinput, 1000);
			ScoreDoc[] docScores = results.scoreDocs;
			outputSearchResults(shortWriter, queryNo++, docScores, searcher, algorithm+"_short");
			reader.close();
		}
		
		//Code for Long Query
		queryNo = 0;
		for(QueryInput query : queries){
			System.out.println("Searching for Small Query No " + queryNo);
			IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("./res/index")));
			IndexSearcher searcher = new IndexSearcher(reader);

			if(algorithm.equals("BM25")) searcher.setSimilarity(new BM25Similarity());
			else if(algorithm.equals("VSM")) searcher.setSimilarity(new ClassicSimilarity());
			else if(algorithm.equals("LMDirchlet")) searcher.setSimilarity(new LMDirichletSimilarity());
			else if (algorithm.equals("LMJelinekMercer")) searcher.setSimilarity(new LMJelinekMercerSimilarity(0.7f));
			else{
				System.out.println("This algorithm is not available");
				return;
			}
			
			Analyzer analyzer = new StandardAnalyzer();
			QueryParser queryParser = new QueryParser("TEXT",analyzer);
			Query queryinput = queryParser.parse(QueryParser.escape(query.getDescription()));
			TopDocs results = searcher.search(queryinput, 1000);
			ScoreDoc[] docScores = results.scoreDocs;
			outputSearchResults(longWriter, queryNo++, docScores, searcher, algorithm+"_long");
			reader.close();
		}
		
		shortWriter.close();
		longWriter.close();
	}
	
	public void outputSearchResults(BufferedWriter writer, int queryNo, ScoreDoc[] docScores, IndexSearcher searcher, String algorithm) throws IOException{
		for(int i=0; i<docScores.length; i++){
			Document doc=searcher.doc(docScores[i].doc);
			writer.append((51+queryNo)+" ");
			writer.append(0+" ");
			writer.append(doc.get("DOCNO")+" ");
			writer.append(i+1+" ");
			writer.append(docScores[i].score+" ");
			writer.append(algorithm);
			writer.newLine();
		}
	}
}
