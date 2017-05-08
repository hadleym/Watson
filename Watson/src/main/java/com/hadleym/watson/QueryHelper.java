package com.hadleym.watson;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;

/* This class takes a directory that has been indexed by lucene, 
 * a file containing the Jeopardy questions, and queries the
 * index using a given analyzer and generates scores for each
 * question.  It stores each question in its own Question class.
 */
public class QueryHelper {

	boolean verbose;
	Preprocessor preprocessor;
	Analyzer analyzer;
	File index;
	File questions;
	int[] ranks = new int[Constants.HITSPERPAGE];
	BufferedReader br;
	int total;
	public QuestionHandler handler;
	private float MRRScore;
	private int RA1Score;
	Similarity sim;

	public QueryHelper(File questionsFile, File indexDir, Analyzer a, Preprocessor pp, boolean v, Similarity sim) {
		this.preprocessor = pp;
		this.verbose = v;
		this.sim = sim;
		this.analyzer = a;
		this.index = indexDir;
		this.questions = questionsFile;
		total = 0;
		handler = new QuestionHandler(questions);
		RA1Score = -1;
		MRRScore = -1;
		// initialize to -1 to flag if its been calculated yet.
		ranks[0] = -1;
	}

	// prints all of the questions that received a rank of '1' meaning it was
	// the top result for that question.
	public void printCorrectQuestions() {
		for (Question question : handler.questions) {
			if (question.rank == 0) {
				System.out.println(question);
			}
		}
	}

	// print all the ranks that each question received from the analsis.
	// the number of ranks depends on the Constants.HITSPERPAGE vairable.
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

	// performs the analysis on the questions given by questions file.
	// Will iterate through each question, create a Question class,
	// and perform a query with the question vs the index.
	public void executeQuestions() {
		int num = 1;
		// iterate over each question
		for (Question question : handler.questions) {
			String query = question.getQuery();
			// if a preprocessor was used in the indexing,
			// the query must receive the same processing.
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
				// set the results returned from the query.
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

	// This is where the indexer is combined with analyzer and the
	// query is evaluated against the index. An array of results (name and
	// score)
	// are returned, also the Question passed along receives a copy of
	// what the parsed question was, for future analysis.
	// this is where the similarity can be modified.
	private Result[] getResults(String query, Question question) throws IOException, ParseException {
		Result[] results = new Result[Constants.HITSPERPAGE];
		Query q = new QueryParser(Constants.FIELD_CONTENTS, analyzer).parse(query);
		IndexReader reader = DirectoryReader.open(Constants.getDirectory(index.toPath()));
		IndexSearcher searcher = new IndexSearcher(reader);
		searcher.setSimilarity(sim);
		TopDocs docs = searcher.search(q, Constants.HITSPERPAGE);
		ScoreDoc[] hits = docs.scoreDocs;
		// find the document category that matches the answer.
		for (int i = 0; i < docs.scoreDocs.length; i++) {
			String name = searcher.doc(hits[i].doc).get(Constants.FIELD_CATEGORY);
			// remove the leading and trailing '[['
			name = name.substring(2, name.length() - 2);
			results[i] = new Result(name, hits[i].score);
		}
		// assign the parsed question
		question.setParsedQuestion(q.toString(Constants.FIELD_CONTENTS));
		return results;
	}

	public int getRA1Score() {
		if (RA1Score == -1) {
			calculateRA1Score();
		}
		return RA1Score;
	}

	public void calculateRA1Score() {
		if (ranks[0] > 0) {
			RA1Score = ranks[0];
		}
	}

	public void calculateMRRScore() {
		float score = 0;
		for (Question question : handler.questions) {
			// rank start at 0 being the top hit.
			int rank = question.getRank() + 1;
			if (rank > 0) {
				float recip = 1 / (float) rank;
				score = score + recip;
			}
			// If a rank is not within the top 10 (indicated by
			// question.getRank() == -1)
			// then its MRRScore is 0, and not added to the MRRScore.
		}
		MRRScore = score;
	}

	// returns MMRScore. Calculates if the result is -1, which means
	// it hasnt been calculated yet.
	public double getMRRScore() {
		if (MRRScore < 0) {
			calculateMRRScore();
		}
		return MRRScore;
	}

	// filter out certain characters, these characters cause the lucene query
	// parser
	// to throw ParseExceptions
	private static String filter(String line) {
		return line.replaceAll("[()!#&\"\'-]", "");
	}

	// display the results of a search.
	public void printResults(ScoreDoc[] hits, IndexSearcher indexSearcher) throws IOException {
		if (Constants.DEBUG) {
			System.out.println("PrintResults:");
		}
		for (int i = 0; i < hits.length; i++) {

			System.out.println(hits[i].score + ", " + hits[i].doc + ", "
					+ indexSearcher.doc(hits[i].doc).get(Constants.FIELD_CATEGORY));
		}
	}

	// print all of the questions contained for this QueryHelper.
	public void printAllQuestions() {
		for (Question question : handler.questions) {
			question.printQuestion();
		}
	}

	// Prints a summary of the results from the Questions.
	public void printSummary() {
		
		System.out.println("Summary for " + this );
		System.out.println("Total Questions: " + total);
		System.out.println("Correct top 1 questions: " + getRA1Score());
		System.out.println("MRRScore: " + getMRRScore());
		// printRanks();
	}

	@Override
	public String toString(){
		if (analyzer instanceof StandardAnalyzer) {
			return "Lucene Standard Analyzer " + sim;
		} else {
			return "CoreNLP lemmas and Lucene Whitespace Analyzer " + sim;
		}	
	}
	public String getSummary() {
		StringBuilder sb = new StringBuilder();
		String analyzerString = null;
		if (analyzer instanceof StandardAnalyzer) {
			analyzerString = "Lucene Standard Analyzer with " + sim ;
		} else {
			analyzerString = "CoreNLP lemmas and Lucene Whitespace Analyzer";
		}
		sb.append("Summary for " + analyzerString + '\n');
		sb.append("Total Questions: " + total + "\nCorrect top 1 questions: " + getRA1Score() + "\nMRRScore: "
				+ getMRRScore() + '\n');
		return sb.toString();
	}

}
