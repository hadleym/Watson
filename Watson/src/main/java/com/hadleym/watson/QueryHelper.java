package com.hadleym.watson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.synonym.WordnetSynonymParser;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.wordnet.SynonymMap;
import org.apache.lucene.analysis.tokenattributes.*;

public class QueryHelper {
	public static void main(String[] args) throws IOException, ParseException {
		File index = new File(Constants.INDEX_DIR);
		// String initialQuery = "newspaper capital 10 papers u.s. circulation";
		String initialQuery1 = "newspaper The dominant paper in our nation's capital, it's among the top 10 U.S. papers in circulation";
		String initialQuery2 = "The practice of pre-authorizing presidential use of force dates to a 1955 resolution re: this island near mainland China";
		String initialQuery3 = "Daniel Hertzberg & James B. Stewart of this paper shared a 1988 Pulitzer for their stories about insider trading";
		String initialQuery = initialQuery3;
		StopAnalyzer analyzer = new StopAnalyzer();
		App.preprocessLine(initialQuery);
		
	}

	public static List<String> analyze(Analyzer analyzer, List<String> words) throws IOException, ParseException {
		List<String> result = new ArrayList<String>();
		String wordsToAnalyze = words.toString();
		TokenStream tokenStream = analyzer.tokenStream(Constants.FIELD_CONTENTS, new StringReader(wordsToAnalyze));
		OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
		CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
		tokenStream.reset();
		while (tokenStream.incrementToken()) {
			int startOffset = offsetAttribute.startOffset();
			int endOffset = offsetAttribute.endOffset();
			String term = charTermAttribute.toString();
			System.out.println(term);
			result.add(term);
		}
		tokenStream.end();
		tokenStream.close();
		return result;
	}

	public static String parseQuery(List<String> arrayList) {
		String category = Constants.FIELD_CONTENTS + ":";
		String parsed = new String() + category;
		parsed += arrayList.get(0);
		for (int i = 1; i < arrayList.size(); i++) {
			parsed += " AND " + category + arrayList.get(i);
		}
		if (Constants.DEBUG) {
			System.out.println("Parsed query : " + parsed);
		}
		return parsed;
	}

	public static ArrayList<String> createSynonyms(List<String> words) throws IOException {
		ArrayList<String> synonyms = new ArrayList<String>();
		SynonymMap map = new SynonymMap(new FileInputStream("samples/fulltext/wn_s.pl"));
		for (int i = 0; i < words.size(); i++) {
			String[] syn = map.getSynonyms(words.get(i));
			if (syn.length < 20) {
				for (int c = 0; c < syn.length; c++) {
					synonyms.add(syn[c]);
				}

				System.out.println(words.get(i) + " : " + java.util.Arrays.asList(syn).toString());
			} 
			synonyms.add(words.get(i));
			
		}
		return synonyms;

	}

	public static void searchIndex(Path directoryPath, Query query) throws IOException, ParseException {
		Directory directory = FSDirectory.open(directoryPath);
		IndexReader indexReader = DirectoryReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		TopDocs docs = indexSearcher.search(query, Constants.HITSPERPAGE);
		ScoreDoc[] hits = docs.scoreDocs;
		printResults(hits, indexSearcher);
		System.out.println(indexSearcher.doc(0).get(Constants.FIELD_CATEGORY));

	}

	public static void printResults(ScoreDoc[] hits, IndexSearcher indexSearcher) throws IOException {
		if (Constants.DEBUG) {
			System.out.println("PrintResults:");
		}
		for (int i = 0; i < hits.length; i++) {

			System.out.println(hits[i].score + ", " + hits[i].doc + ", "
					+ indexSearcher.doc(hits[i].doc).get(Constants.FIELD_CATEGORY));
		}
	}

}
