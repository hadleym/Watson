package com.hadleym.watson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
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

	public static void evaluate(File questionsFile, File indexDir, Analyzer a, Preprocessor pp, boolean verbose)
			throws IOException, ParseException {
		Preprocessor preprocessor = pp;
		Analyzer analyzer = a;
		int[] ranks = new int[10];
		File questions = questionsFile;
		File index = indexDir;
		BufferedReader br = new BufferedReader(new FileReader(questions));
		int total = 0;
		for (String subject = br.readLine(); subject != null; subject = br.readLine()) {
			if (subject.equals("POTPOURRI")) {
				subject = "";
			}
			String question = br.readLine();
			String answer = br.readLine();

			// read blank line
			br.readLine();

			String query = subject + " " + question;
			
			// if the NLP_FLAG is false, then there is no 'preprocessing' to do on this query
			if (preprocessor != null ){
				query = preprocessor.preprocessLine(query);
			}
			if (verbose) {
				System.out.println("QUESTION: " + query);
				System.out.println("ANSWER: " + answer);
			}
			int rank = doQuery(query, index, answer, analyzer, verbose);
			if (rank >= 0) {
				ranks[rank]++;
			}
			if (verbose) {
				System.out.println("");
			}
			total++;
		}
		br.close();
		for (int i = 0; i < ranks.length; i++) {
			System.out.println("Rank " + i + ": " + ranks[i]);
		}
		System.out.println("Total: " + total);
	}
	

	public static int doQuery(String query, File index, String answer, Analyzer analyzer, boolean verbose) throws IOException, ParseException {
		Query q = new QueryParser(Constants.FIELD_CONTENTS, analyzer).parse(query);
		IndexReader reader = DirectoryReader.open(Constants.getDirectory(index.toPath()));
		IndexSearcher searcher = new IndexSearcher(reader);
		TopDocs docs = searcher.search(q, Constants.HITSPERPAGE);
		ScoreDoc[] hits = docs.scoreDocs;
		if (verbose) {
			System.out.println("Results:");
		}
		for (int i = 0; i < hits.length; i++) {
			String result = searcher.doc(hits[i].doc).get(Constants.FIELD_CATEGORY);

			// strip '[[' and ']]'
			result = result.substring(2, result.length() - 2);

			if (result.equals(answer)) {
				System.out.println(i + ": " + result);
				return i;
			}
		}
		return -1;
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
