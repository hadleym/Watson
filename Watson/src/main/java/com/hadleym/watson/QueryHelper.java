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
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.wordnet.SynonymMap;
import org.apache.lucene.analysis.tokenattributes.*;

/* This class 
 * 
 * 
 */
public class QueryHelper {

	boolean verbose;
	Preprocessor preprocessor;
	Analyzer analyzer;
	File index;
	File questions;
	int[] ranks = new int[10];
	BufferedReader br;
	int total;
	public QuestionHandler handler;

	public QueryHelper(File questionsFile, File indexDir, Analyzer a, Preprocessor pp, boolean v) {
		this.preprocessor = pp;
		this.verbose = v;
		this.analyzer = a;
		this.index = indexDir;
		this.questions = questionsFile;
		total = 0;
		handler = new QuestionHandler(questions);

	}

	public void printCorrectQuestions() {
		for (Question question : handler.questions) {
			if (question.rank == 0) {
				System.out.println(question);
			}
		}
	}

	public void printRanks() {
		int sum = 0;
		System.out.println("Rank is the position the system retrieved the correct answer for the question");
		System.out.println("Total questions: " + total);
		for (int i = 0; i < ranks.length; i++) {
			int adjustedRank = i + 1;
			System.out.println("Rank " + adjustedRank + ": " + ranks[i]);
			sum += ranks[i];
		}
		System.out.println(
				"Number of questions failed to be answered in the top " + Constants.HITSPERPAGE + ": " + (total - sum));

	}

	public void executeQuestions() {
		int num = 1;
		for (Question question : handler.questions) {
			String query = question.getQuery();
			if (preprocessor != null) {
				query = preprocessor.preprocessLine(query);
			} else {
				// need to remove certain characters to perform a straight
				// query,
				// otherwise it is interpreted incorrectly or as a wildcard
				// for the standard analyzer.
				query = filter(query);
			}
			try {
				question.setResults(getResults(query, question));
				question.setRank(question.calculateRank());
				int rank = question.getRank();
				if (rank >= 0) {
					ranks[question.getRank()]++;
				}
				question.setNumber(num++);
				total++;
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private Result[] getResults(String query, Question question) throws IOException, ParseException {
		Result[] results = new Result[Constants.HITSPERPAGE];
		Query q = new QueryParser(Constants.FIELD_CONTENTS, analyzer).parse(query);
		IndexReader reader = DirectoryReader.open(Constants.getDirectory(index.toPath()));
		IndexSearcher searcher = new IndexSearcher(reader);
		// searcher.setSimilarity(new BM25Similarity());
		searcher.setSimilarity(new ClassicSimilarity());
		TopDocs docs = searcher.search(q, Constants.HITSPERPAGE);
		ScoreDoc[] hits = docs.scoreDocs;
		for (int i = 0; i < docs.scoreDocs.length; i++) {
			String name = searcher.doc(hits[i].doc).get(Constants.FIELD_CATEGORY);
			name = name.substring(2, name.length() - 2);
			results[i] = new Result(name, hits[i].score);
		}
		question.setParsedQuestion(q.toString(Constants.FIELD_CONTENTS));
		return results;
	}

	private static String filter(String line) {
		return line.replaceAll("[()!#&\"\'-]", "");
	}

	public void printResults(ScoreDoc[] hits, IndexSearcher indexSearcher) throws IOException {
		if (Constants.DEBUG) {
			System.out.println("PrintResults:");
		}
		for (int i = 0; i < hits.length; i++) {

			System.out.println(hits[i].score + ", " + hits[i].doc + ", "
					+ indexSearcher.doc(hits[i].doc).get(Constants.FIELD_CATEGORY));
		}
	}

	public void printAllQuestions() {
		for (Question question : handler.questions) {
			question.printQuestion();
		}
	}

}
