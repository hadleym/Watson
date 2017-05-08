package com.hadleym.watson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

/*
 * WATSON PROJECT
 * Mark Hadley
 * CS 483 
 * 5/1/2017
 * 
 * This is a full end-to-end system for emulating the 'Watson' Jeopardy 
 * system.  
 * 
 * It utilizes Lucene as well as the NLPCore.
 * 
 * CoreNLP is used to remove parts of speech not wanted, and also performs lemmatization.
 * Lucene is used to index using either the StandardAnalyzer, or WhitespaceAnalyzer.
 * 
 * Documents that are preprocessed with CoreNLP are then used with just the WhitespaceAnalyzer.
 * 
 * Documents that are not preproccessed with CoreNLP are analyzed with the lucene StandardAnalyzer.
 * 
 * Questions are created by parsing the 'questions.txt' file contained given with these files.
 * 
 * 
 * 
 */
public class App {

	public static void main(String[] args) throws IOException, ParseException {
		// this is for the files referred to as 'nlp' preprocessed files.
		if (args.length == 3 && args[0].equals("-p")) {
			try {
				preprocessDir(new File(args[1]), new File(args[2]));
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("please make sure the directories exist and contain files to preprocess");
				System.exit(1);
			}
			// will index the nlp preprocessed files with the lucene
			// whitespace analyzer.
		} else if (args.length == 3 && args[0].equals("-iwht")) {
			index(new File(args[1]), new File(args[2]), new WhitespaceAnalyzer());
		} else if (args.length == 3 && args[0].equals("-istd")) {
			String filesToIndex = args[1];
			File index = new File(args[2]);
			if (index.listFiles().length > 0) {
				System.out.println(index.getName() + " is not empty.  Please empty index directory and retry.");
				System.exit(1);
			}
			index(new File(filesToIndex), index, new StandardAnalyzer());
		} else if (args.length == 3 && args[0].equals("-ewht"))
		// will evaluate the nlp pre-processed files vs. the collection
		// of questions.
		{
			System.out.println("Evaluating against the questions file '" + args[1] + "' and the preprocessed index '"
					+ args[2] + "' with whitespace analyzer...");

			// because this is the NLP files, we must do the preprocessing.
			Preprocessor preprocessor = PreprocessorGenerator.standardPreprocessor();
			String questions = args[1];
			String index = args[2];
			QueryHelper nlpQueryClassic = new QueryHelper(new File(questions), new File(index),
					new WhitespaceAnalyzer(), preprocessor, true, new ClassicSimilarity());
			nlpQueryClassic.executeQuestions();
			nlpQueryClassic.printSummary();
			System.out.println();
			QueryHelper nlpQueryBM25 = new QueryHelper(new File(questions), new File(index), new WhitespaceAnalyzer(),
					preprocessor, true, new BM25Similarity());
			nlpQueryBM25.executeQuestions();
			nlpQueryBM25.printSummary();

			// nlpQuery.printAllQuestions();

		} else if (args.length == 3 && args[0].equals("-estd"))
		// will evaluate the lucene standard analyzer index documents vs
		// the collection of questions.
		{
			System.out.println("Evaluating the questions file [" + args[1] + "] with the index dir " + args[2]
					+ " with standard analyzer...");
			String questions = args[1];
			String index = args[2];
			QueryHelper stdQueryClassic = new QueryHelper(new File(questions), new File(index), new StandardAnalyzer(),
					null, true, new ClassicSimilarity());
			QueryHelper stdQueryBM25 = new QueryHelper(new File(questions), new File(index), new StandardAnalyzer(),
					null, true, new BM25Similarity());
			stdQueryClassic.executeQuestions();
			stdQueryClassic.printSummary();
			System.out.println();
			stdQueryBM25.executeQuestions();
			stdQueryBM25.printSummary();
			System.out.println();
			// stdQueryBM25.printAllQuestions();

		} else if (args.length == 3 && args[0].equals("-explore")) {
			// Handy 'explorer' that can be used to see what individual questions
			// for both the CoreNLP and StandardAnalyzer with BM25.
			Scanner s = new Scanner(System.in);
			System.out.println("Exploring mode");
			int selection = -1;
			while (selection != 1 && selection != 2) {
				System.out.println("Which index do you wish to explore?");
				System.out.println("1) Preprocessed NLP and whitespace index.");
				System.out.println("2) Lucene Standard Analyzer index.");

				selection = s.nextInt();
			}

			System.out.println("Enter directory containing appropriate index:");
			String questions = args[1];
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String index = in.readLine();
			System.out.println("Using directory '" + index + "' as index...");
			if (selection == 1) {
				System.out.println("evaluating vs NLP");
				System.out.println("Please wait while analysis is being generated...");
				Preprocessor preprocessor = PreprocessorGenerator.standardPreprocessor();
				QueryHelper nlpQuery = new QueryHelper(new File(questions), new File(index), new WhitespaceAnalyzer(),
						preprocessor, true, new BM25Similarity());
				exploreQuery(nlpQuery, s);
			} else if (selection == 2) {
				System.out.println("Evaluating vs Lucene Standard Analyzer");
				System.out.println("Please wait while analysis is being generated...");
				QueryHelper stdQuery = new QueryHelper(new File(questions), new File(index), new StandardAnalyzer(),
						null, true, new BM25Similarity());
				exploreQuery(stdQuery, s);
			}
		} else if (args.length == 4 && args[0].equals("-a")) {
			evaluateAllPrecisionAtOne(args[1], args[2], args[3]);

		} else {
			printUsageMessage();
			System.exit(1);
		}

	}
	/*
	 * Evaluate all four model (CoreNLP w/tf-id, CoreNLP w/BM25, StandardAnalyzer w/tf-id, Standard
	 * Analyzer w/BM25
	 * with the Precision at one ranking algorithm.
	 * print to standard output.
	 * 
	 * REQUIRES that the CoreNLP directory has been preprocessed (a 3 hour process) and indexed
	 * 			and that the standardIndex has been indexed with the Lucene Standard Analyzer.
	 */
	public static void evaluateAllPrecisionAtOne(String questions, String nlpIndex, String stdIndex) {
		ArrayList<QueryHelper> queries = new ArrayList<>();
		queries.add(new QueryHelper(new File(questions), new File(stdIndex), new StandardAnalyzer(), null, true,
				new ClassicSimilarity()));
		queries.add(new QueryHelper(new File(questions), new File(stdIndex), new StandardAnalyzer(), null, true,
				new BM25Similarity()));
		queries.add(new QueryHelper(new File(questions), new File(nlpIndex), new WhitespaceAnalyzer(),
				PreprocessorGenerator.standardPreprocessor(), true, new ClassicSimilarity()));
		queries.add(new QueryHelper(new File(questions), new File(nlpIndex), new WhitespaceAnalyzer(),
				PreprocessorGenerator.standardPreprocessor(), true, new BM25Similarity()));
		System.out.println("Whole system Precision @ 1 Rank");
		System.out.println("Evaluating...");
		for (QueryHelper query : queries) {
			query.executeQuestions();
		}
		QuestionHandler[] handler = new QuestionHandler[4];
		for (int i = 0; i < handler.length; i++) {
			handler[i] = queries.get(i).handler;
		}
		int total = queries.get(0).total;
		System.out.println("Rank 0 == not found, Rank 1 == correct answer");
		System.out.println("StdAnalyzer/Classic, StdAnalyzer/BM25, NLPCore/Classic, NLPCore/BM25");
		for (int i = 0; i < total; i++) {
			System.out.print(String.format("Question #%3d: ", (i + 1)));
			for (int h = 0; h < handler.length; h++) {

				if (h < handler.length - 1) {
					System.out.print(String.format("%3d,", handler[h].questions.get(i).getRank() + 1));
				} else {
					System.out.print(String.format("%3d", handler[h].questions.get(i).getRank() + 1));
				}
			}
			System.out.println();
		}
	}

	public static void exploreQuery(QueryHelper helper, Scanner s) {
		helper.executeQuestions();
		int total = helper.total;
		System.out.println("Processing finished, " + helper.handler.questions.size() + " questions analyzed.");

		System.out.println("Please enter an integer between 1 - " + (total));
		System.out.println("enter negative number to exit");
		System.out.println("Which question would you like to explore: ");
		int num = 1;
		while (num >= 0) {
			num = s.nextInt();
			if (num < 0) {
				break;
			}
			num--;
			if (num < total) {
				Question question = helper.handler.questions.get(num);
				question.printQuestion();
			}
			System.out.println("\nWhich question would you like to explore: ");
			System.out.println("enter negative number to exit");
		}
		System.out.println("Finished exploring...");
	}

	public static void printUsageMessage() {
		System.out.println(
				"\nUsage: java -jar Watson.jar -a QUESTIONS_FILE PREPROCESS_DIR INDEX_DIR \n\t -- evaluate all 4 models with Precision @ 1");
		System.out.println(
				"\nUsage: java -jar Watson.jar -p SRC_DIR PREPROCESS_DIR \n\t -- preprocess all files in SRC_DIR to PREPROCESS_DIR.");
		System.out.println(
				"\nUsage: java -jar Watson.jar -iwht SRC_DIR INDEX_DIR \n\t -- index all files in SRC_DIR to INDEX_DIR with the Lucene Whitespace analyzer.");
		System.out.println(
				"\nUsage: java -jar Watson.jar -istd SRC_DIR INDEX_DIR \n\t -- index all files in SRC_DIR to INDEX_DIR with the Lucene Standard Analyzer.");
		System.out.println("\nUsage: java -jar Watson.jar -ewht QUESTIONS_FILE INDEX_DIR");
		System.out.println(
				"\t -- Evaluate the QUESTIONS_FILE vs the INDEX_DIR with the Preprocessor and Whitespace Analyzer and will output analysis to STDOUT");
		System.out.println(
				"\nUsage: java -jar Watson.jar -estd QUESTIONS_FILE INDEX_DIR \n\t -- Evaluate the QUESTIONS_FILE vs the INDEX_DIR with Lucene Standard Analyzer and will ouput analysis to STDOUT");
		System.out.println("\nUsage: java -jar Watson.jar -explore QUESTIONS_FILE \n\t -- Explore the QUESTIONS_FILE.");
	}

	/*
	 * Accepts a directory of files and indexes them with the given analyzer,
	 * outputing to the given output directory.
	 */
	public static void index(File inputDir, File outputDir, Analyzer analyzer) {
		System.out.println("indexing directory: " + inputDir.getName() + " to ouput to " + outputDir.getName());
		DocumentIndexer indexer = new DocumentIndexer(inputDir, outputDir, analyzer);
		indexer.indexAllFiles();
	}

	/*
	 * Performs pre-processing with the NLPCore api. Lemmatizes, parses and
	 * removes specific parts of speech. This process can take over 3 hours on a
	 * laptop for the entire wiki provided.
	 */
	public static void preprocessDir(File inputDir, File outputDir) {
		System.out.println("Starting preprocessing...");
		Preprocessor preprocessor = PreprocessorGenerator.standardPreprocessor();
		try {
			preprocessor.preprocessDirectory(inputDir, outputDir);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error with input or output directory");
		}
		System.out.println("Preprocessing finished.");
	}

}
