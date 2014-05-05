// BSD License (http://www.galagosearch.org/license)

package org.galagosearch.core.tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.galagosearch.core.parse.Document;
import org.galagosearch.core.parse.TagTokenizer;
import org.galagosearch.core.parse.Time;
import org.galagosearch.core.parse.TimeTuple;
import org.galagosearch.core.parse.TimeWrap;
import org.galagosearch.core.retrieval.Retrieval;
import org.galagosearch.core.retrieval.ScoredDocument;
import org.galagosearch.core.retrieval.query.Node;
import org.galagosearch.core.retrieval.query.SimpleQuery;
import org.galagosearch.core.retrieval.query.StructuredQuery;
import org.galagosearch.core.store.DocumentStore;
import org.galagosearch.core.store.SnippetGenerator;
import org.galagosearch.tupleflow.Parameters;
import org.tartarus.snowball.ext.englishStemmer;

import rita.RiWordNet;

/**
 *
 * @author trevor
 */
public class Search {
	SnippetGenerator generator;
	DocumentStore store;
	Retrieval retrieval;
	public static Retrieval getIdentifier;

	public Search(Retrieval retrieval, DocumentStore store) {
		this.store = store;
		this.retrieval = retrieval;
		getIdentifier = retrieval;
		generator = new SnippetGenerator();
	}

	public void close() throws IOException {
		store.close();
		retrieval.close();
	}

	public static class SearchResult {
		public Node query;
		public Node transformedQuery;
		public List<SearchResultItem> items;
	}

	public static class SearchResultItem {
		public int rank;
		public String identifier;
		public String displayTitle;
		public String url;
		public Time publication; 
		public Map<String, String> metadata;
		public String summary;
		public TimeTuple timeFrame;
	}

	public String getSummary(Document document, Set<String> query) throws IOException {
		if (document.metadata.containsKey("description")) {
			String description = document.metadata.get("description");

			if (description.length() > 10) {
				return generator.highlight(description, query);
			}
		}

		return generator.getSnippet(document.text, query);
	}

	public static Node parseQuery(String query, Parameters parameters) {
		String queryType = parameters.get("queryType", "complex");

		if (queryType.equals("simple")) {
			return SimpleQuery.parseTree(query);
		}

		return StructuredQuery.parse(query);
	}

	public Document getDocument(String identifier) throws IOException {
		return store.get(identifier);
	}

	public SearchResult runQuery(String query, int startAt, int count, boolean summarize) throws Exception {
		Document doc = new TagTokenizer().tokenize(query);
		TimeTuple.setTimeFrame(doc, true);
		TimeTuple.qTF = doc.timeFrame;
		TimeTuple.Q_abs();
		
		//*
		StringBuffer sb = new StringBuffer("");
		for(String s:doc.terms){
			sb.append(s);
			sb.append(' ');
		}
		/*
		String s[] = new String[doc.terms.size()];
		ArrayList<String> improvedQuery = queryExpand(doc.terms.toArray(s));
		System.out.println(improvedQuery.toString());
		//*/
		/*
		 * @author
		 * Suresh Rangaswamy
		 */


		Time.qFos = new PrintStream(new File("/home/ocean/Desktop/" + query + Time.counter++ + ".txt"));
		Time.qlog = new PrintStream(new File("/home/ocean/Desktop/" + query + Time.counter++ + ".txt"));
		//Node tree = parseQuery(query, new Parameters());
		Node tree = parseQuery(sb.toString(), new Parameters());
		Node transformed = retrieval.transformQuery(tree);
		ScoredDocument[] results = retrieval.runQuery(transformed, startAt + count);

		//addTime(results);
		//Arrays.sort(results);

		SearchResult result = new SearchResult();
		Set<String> queryTerms = StructuredQuery.findQueryTerms(tree);
		result.query = tree;
		result.transformedQuery = transformed;
		result.items = new ArrayList();

		for (int i = startAt; i < Math.min(startAt + count, results.length); i++) {
			String identifier = retrieval.getDocumentName(results[i].document);
			Document document = getDocument(identifier);
			SearchResultItem item = new SearchResultItem();          

			item.rank = i + 1;
			item.identifier = identifier;
			item.displayTitle = identifier;
			TimeWrap w;
			/*
			if((w = Time._perfectTuples.get(identifier)) != null){
				item.publication = w.publication;
				item.timeFrame = w.timeFrame;
			}
			else{
				item.publication = TimeTuple.qTF.tb_u;
			}
			//*/

			if (document.metadata.containsKey("title")) {
				item.displayTitle = document.metadata.get("title");
			}

			if (item.displayTitle != null) {
				item.displayTitle = generator.highlight(item.displayTitle, queryTerms);
			}

			if (document.metadata.containsKey("url")) {
				item.url = document.metadata.get("url");
			}

			if (summarize) {
				item.summary = getSummary(document, queryTerms);
			}

			//System.out.println(results[i].score);

			item.metadata = document.metadata;
			result.items.add(item);
		}
		System.out.println("qTF : " + TimeTuple.qTF.toString() 
				+ "\n" + doc.terms.toString());
		Time.qFos.close();
		Time.qlog.close();
		return result;
	}

	private void addTime(ScoredDocument[] results) throws IOException {
		// TODO Auto-generated method stub
		for(int i=0; i<results.length; i++){
			results[i].score = results[i].score*TimeTuple.overlap(
					retrieval.getDocumentName(results[i].document));
		}
	}

	/**
	 * Expands the user query into a set of meaning full list of words
	 * @param words set of initial words.
	 * @return expanded set of words
	 */
	public ArrayList<String> queryExpand(String[] words)

	{
		RiWordNet wordnet;
		ArrayList<String> expandedQuery = new ArrayList<String>();
		Set<String> similarWords = new HashSet<String> ();
		if(System.getProperty("os.name").equals("unix")){
			wordnet = new RiWordNet("src/main/resources/WordNet-3.0", false, false);
		}
		else wordnet = new RiWordNet("C:\\Program Files (x86)\\WordNet\\2.1", false, false);
		englishStemmer stemmer = new englishStemmer();

		for(String word:words){
			stemmer.setCurrent(word);
			if(stemmer.stem()){
				word = stemmer.getCurrent();
			}
			expandedQuery.add(word);
		}
		for(String word:expandedQuery) {
			// Add original query terms after stemming

			try{
				String[] tmp = {};   
				String pos = wordnet.getBestPos(word);
				int[] ids = wordnet.getSenseIds(word, pos);
				System.out.println("Word: "+word);
				for(int id: ids) {
					String description = wordnet.getDescription(id);
					String[] results = wordnet.getSynset(id);
					System.out.println("Id: "+id);
					System.out.println("Description: "+description);
					for(String result:results){
						if(!expandedQuery.contains(result) && result.split(" ").length==1)
							similarWords.add(result);
					}
					System.out.println("---------------------");
				}
			}catch(Exception e){}
		}
		for(String smword:similarWords)
			expandedQuery.add(smword);
		return expandedQuery;
	}
}
