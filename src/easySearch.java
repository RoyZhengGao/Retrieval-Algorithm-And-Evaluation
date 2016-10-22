import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class easySearch {

	public static void main(String[] args) throws ParseException, IOException {
		easySearch search = new easySearch();
		System.out.println("Please check result in /output/easySearchOutput.txt file.");
		System.out.println("Searching your query now...");
		search.getRank("new york");
		System.out.println("Search Complete.");
	}
	
	public List<Document> getRank(String queryString)throws ParseException, IOException{
		
		BufferedWriter writer = new BufferedWriter(new FileWriter("./output/easySearchOutput.txt"));
		
		//Read Index files
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("./res/index")));
		IndexSearcher searcher = new IndexSearcher(reader);
		
		//N Value
		double N = reader.maxDoc();
		writer.append("\nTotal number of documents: "+ N);
		writer.newLine();

		// Get the preprocessed query terms
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer);
		Query query = parser.parse(QueryParser.escape(queryString));
		Set<Term> queryTerms = new LinkedHashSet<Term>();
		searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);
		
		//Maps for storing term freq in documents 
		HashMap<String, HashMap<Integer, Integer>> termCounts = new HashMap<String, HashMap<Integer, Integer>>();
		//Maps for storing IDF 
		HashMap<String, Double> IDF = new HashMap<String, Double>();
		//Maps for Document Length
		HashMap<Integer, Float> length = new HashMap<Integer, Float>();
		
		//System.out.println("Terms in the query: ");
		writer.append("\nTerms in the query:");
		for (Term t : queryTerms) {
			writer.append("\n"+t.text());
			writer.newLine();
			
			double df=reader.docFreq(new Term("TEXT", t.text()));
			
			writer.append("Number of documents containing the term "+ t.text()+ " for field \"TEXT\": "+df);
			writer.newLine();
			
			HashMap<Integer, Integer> count = new HashMap<Integer, Integer>();
			termCounts.put(t.text(), count);
			
			IDF.put(t.text(), Math.log(1 + (N/df)));
		}
		
		ClassicSimilarity dSimi = new ClassicSimilarity();
		List<LeafReaderContext> leafContexts = reader.getContext().reader().leaves();
		for (int i = 0; i < leafContexts.size(); i++) { 
			LeafReaderContext leafContext = leafContexts.get(i);
			int startDocNo = leafContext.docBase;
			int numberOfDoc = leafContext.reader().maxDoc();
			
			//Store document length
			for (int docId = 0; docId < numberOfDoc; docId++) {
				float normDocLeng = dSimi.decodeNormValue(leafContext.reader().getNormValues("TEXT").get(docId));
				float docLeng = 1 / (normDocLeng * normDocLeng);
				length.put((docId + startDocNo), docLeng);
			}
			
			for (Term t : queryTerms) {
				
				PostingsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(),"TEXT", new BytesRef(t.text()));
				
				int doc;
				if (de != null) {
					while ((doc = de.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
						termCounts.get(t.text()).put((de.docID() + startDocNo), de.freq());
					}
				}
			}
		}
		
		writer.newLine();
		writer.append("\n***********************OUTPUT*****************************\n");
		writer.newLine();
		
		ArrayList<Document> docList = new ArrayList<Document>();
		writer.append("Relevance score for document:");
		writer.newLine();
		for(int doc=0;doc<N; doc++){
			double value = 0;
			String docID = searcher.doc(doc).get("DOCNO");
			writer.append(docID + " ");
			for (Term t : queryTerms) {
				if(termCounts.get(t.text()).get(doc)!=null){
					double termValue = 0;
					termValue = IDF.get(t.text()) * termCounts.get(t.text()).get(doc) / length.get(doc);
					//System.out.println("Score for term " + t.text() +" is "+ termValue);
					writer.append(termValue + " ");
					value += termValue;
				}
			}
			
			Document docScore = new Document();
			docScore.setScore(value);
			docScore.setDocID(docID);
			docScore.setDocNo(doc);
			docList.add(docScore);
			
			writer.append(value + " ");
			writer.newLine();
		}
		reader.close();
		writer.close();
		return docList;
	}
}
