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
			QueryHelper nlpQueryClassic = new QueryHelper(new File(questions), new File(index), new WhitespaceAnalyzer(),
					preprocessor, true, new ClassicSimilarity());
			nlpQueryClassic.executeQuestions();
			nlpQueryClassic.printSummary();
			System.out.println();
			QueryHelper nlpQueryBM25 = new QueryHelper(new File(questions), new File(index), new WhitespaceAnalyzer(),
					preprocessor, true, new BM25Similarity());
			nlpQueryBM25.executeQuestions();
			nlpQueryBM25.printSummary();

//			nlpQuery.printAllQuestions();

		} else if (args.length == 3 && args[0].equals("-estd"))
		// will evaluate the lucene standard analyzer index documents vs
		// the collection of questions.
		{
			System.out.println("Evaluating the questions file [" + args[1] + "] with the index dir " + args[2]
					+ " with standard analyzer...");
			String questions = args[1];
			String index = args[2];
			QueryHelper stdQueryClassic = new QueryHelper(new File(questions), new File(index), new StandardAnalyzer(), null,
					true, new ClassicSimilarity());
			QueryHelper stdQueryBM25 = new QueryHelper(new File(questions), new File(index), new StandardAnalyzer(), null,
					true, new BM25Similarity());
			stdQueryClassic.executeQuestions();
			stdQueryClassic.printSummary();
			System.out.println();
			stdQueryBM25.executeQuestions();
			stdQueryBM25.printSummary();
			System.out.println();

		} else if (args.length == 3 && args[0].equals("-explore")) {
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
		} else {
			printUsageMessage();
			System.exit(1);
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

	/*
	 * public static String preprocessLine(String line) { StringBuilder sb = new
	 * StringBuilder(); if (!beginsWith(line, "[[")) { if (!beginsWith(line,
	 * "==") && !beginsWith(line, "#RED")) { Document doc = new Document(line);
	 * for (Sentence sent : doc.sentences()) { for (int i = 0; i <
	 * sent.posTags().size(); i++) { if (keepPartOfSpeech(sent.posTag(i))) {
	 * sb.append(sent.lemma(i).toString() + " "); } } } } } else {
	 * sb.append(line + "\n"); } return sb.toString(); }
	 * 
	 */
	/*
	 * public static boolean keepPartOfSpeech(String pos) { if (pos.length() ==
	 * 1) { if (pos.equals("V")) { return true; } else { return false; } }
	 * String firstTwo = pos.substring(0, 2); if ((pos.charAt(0) == 'V') ||
	 * firstTwo.equals("RB") || firstTwo.equals("JJ") || firstTwo.equals("NN"))
	 * { return true; } return false; }
	 */
	// strip the leading 2 characters from a line.
	/*
	 * public static String parseCategory(String s) { String returnString =
	 * s.substring(2, s.length() - 2); return returnString; }
	 */

	// This returns if the line of text is a section
	// for example:
	// ==Information==
	// or
	// #REDIRECT
	// is a section, and is most likely not relevant.
	/*
	 * public static boolean beginsWith(String line, String prefix) { if
	 * (line.length() >= prefix.length()) { return line.substring(0,
	 * prefix.length()).equals(prefix); } return false; }
	 */

	// Determines if a string is a category, denoted by
	// a leading '=='. Should be added to the Category field
	// of the index.
	/*
	 * public static boolean isCategory(String line) { return (line.length() > 2
	 * && line.charAt(0) == '[' && line.charAt(1) == '['); }
	 */
	/*
	 * public static void searchIndex(Path directoryPath, String searchString)
	 * throws IOException, ParseException { Directory directory =
	 * FSDirectory.open(directoryPath); IndexReader indexReader =
	 * DirectoryReader.open(directory); IndexSearcher indexSearcher = new
	 * IndexSearcher(indexReader); StandardAnalyzer analyzer = new
	 * StandardAnalyzer(); QueryParser queryParser = new
	 * QueryParser(Constants.FIELD_CONTENTS, analyzer); Query query =
	 * queryParser.parse(searchString); if (Constants.DEBUG) {
	 * System.out.println("SearchString: " + searchString);
	 * System.out.println("Query: " + query); }
	 * 
	 * TopDocs docs = indexSearcher.search(query, Constants.HITSPERPAGE);
	 * ScoreDoc[] hits = docs.scoreDocs; printResults(hits, indexSearcher);
	 * System.out.println(indexSearcher.doc(0).get(Constants.FIELD_CATEGORY));
	 * 
	 * }
	 */

	/*
	 * public static void printResults(ScoreDoc[] hits, IndexSearcher
	 * indexSearcher) throws IOException { if (Constants.DEBUG) {
	 * System.out.println("PrintResults:"); } for (int i = 0; i < hits.length;
	 * i++) {
	 * 
	 * System.out.println(hits[i].score + ", " + hits[i].doc + ", " +
	 * indexSearcher.doc(hits[i].doc).get(Constants.FIELD_CATEGORY)); } }
	 */
}
