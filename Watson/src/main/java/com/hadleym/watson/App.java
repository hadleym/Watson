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
		if (args.length == 1 && args[0].equals("-p")) {
			try {
				Preprocessor.preprocessDir(Constants.RAW_FILE_DIR, Constants.NLP_PREPROCESS_DIR);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("please make sure the directories exist and contain files to preprocess");
				System.exit(1);
			}
			// will index the nlp preprocessed files with the lucene
			// whitespace analyzer.
		} else if (args.length == 1 && args[0].equals("-index")) {
			indexBothBranches();
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
			// Handy 'explorer' that can be used to see what individual
			// questions
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
		} else if (args.length == 1 && args[0].equals("-full")) {
			evaluateFull(Helper.buildAllFour());
		} else if (args.length == 4 && args[0].equals("-full")) {
			evaluateFull(Helper.buildAllFour(args[1], args[2], args[3]));
		} else if (args.length == 1 && args[0].equals("-a")) {
			evaluateAllPrecisionAtOne(Helper.buildAllFour());
		} else if (args.length == 4 && args[0].equals("-a")) {
			evaluateAllPrecisionAtOne(Helper.buildAllFour(args[1], args[2], args[3]));

		} else {
			Helper.printUsageMessage();
			System.exit(1);
		}

	}

	// will index the preprocessed Core NLP files (that need to have already
	// been generated using the '-p' flag)
	// into the default directory of 'nlpIndex' and will also index the default
	// directory 'rawFiles' into
	// the directory 'standardIndex' for use with the StandardAnalyzer.
	public static void indexBothBranches() {
		System.err.println("Indexing both branches with default directories.");
		System.err.println("This process takes approximately 8 minutes on a non-SSD hard drive.");
		File nlpSource = new File(Constants.NLP_PREPROCESS_DIR);
		File nlpIndex = Helper.checkDirectoryAndCreate(Constants.NLP_INDEX);
		File stdIndex = Helper.checkDirectoryAndCreate(Constants.STD_INDEX);
		File stdSource = new File(Constants.RAW_FILE_DIR);
		if (!nlpSource.exists() || !nlpSource.isDirectory() || nlpSource.listFiles().length == 0) {
			System.err.println("Directory [" + nlpSource
					+ "] is either empty, doesnt exist, or is not a directory. Please correct and try again.");
			System.err.println(
					"This directory should contain all NLP preprocessed files created with the -p flag. See -usage for usage messages.");
			System.err.println("Exiting.");
			System.exit(1);
		}
		if (!stdSource.exists() || !stdSource.isDirectory() || stdSource.listFiles().length == 0) {
			System.err.println("Directory [" + stdSource
					+ "] is either empty, doesnt exist, or is not a directory. Please correct and try again.");
			System.err.println("This directory should contain all raw wikipedia files.");
			System.err.println("Exiting.");
			System.exit(1);
		}

		if (nlpIndex.listFiles().length != 0) {
			System.err.println("Directory [" + nlpIndex + "] is NOT empty. Please delete before attempting to index.");
			System.err.println("Exiting...");
			System.exit(1);
		}

		if (stdIndex.listFiles().length != 0) {
			System.err.println("Directory [" + stdIndex + "] is NOT empty. Please delete before attempting to index.");
			System.err.println("Exiting...");
			System.exit(1);
		}

		index(nlpSource, nlpIndex, new WhitespaceAnalyzer());
		index(stdSource, stdIndex, new StandardAnalyzer());
	}

	// evaluates both branches ( StandardAnalyzer and Core NLP ) with
	// both scoring functions (tf-idf and BM25) and writes each to
	// one of four text files.
	public static void evaluateFull(ArrayList<QueryHelper> queries) {
		String[] names = Constants.FILENAMES;
		int pos = 0;
		for (QueryHelper query : queries) {
			System.out.println("Evaluating " + query + "...");
			query.executeQuestions();
			try {
				System.out.println("Producing " + names[pos] + "...");
				BufferedWriter bw = new BufferedWriter(new FileWriter(names[pos]));
				bw.write(query.getSummary());
				bw.write('\n');
				for (Question question : query.handler.questions) {
					bw.write(question.getResults());
					bw.write('\n');
				}
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Finished creating " + names[pos]);
			pos++;
		}
		System.out.println("Evaluation finished, all files complete.");
	}

	/*
	 * Evaluate all four model (CoreNLP w/tf-id, CoreNLP w/BM25,
	 * StandardAnalyzer w/tf-id, Standard Analyzer w/BM25 with the Precision at
	 * one ranking algorithm. print to standard output.
	 * 
	 * REQUIRES that the CoreNLP directory has been preprocessed (a 3 hour
	 * process) and indexed and that the standardIndex has been indexed with the
	 * Lucene Standard Analyzer.
	 */
	public static void evaluateAllPrecisionAtOne(ArrayList<QueryHelper> queries) {
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

	/*
	 * Accepts a directory of files and indexes them with the given analyzer,
	 * outputing to the given output directory.
	 */
	public static void index(File inputDir, File outputDir, Analyzer analyzer) {
		System.out.println("indexing directory: " + inputDir.getName() + " to ouput to " + outputDir.getName());
		DocumentIndexer indexer = new DocumentIndexer(inputDir, outputDir, analyzer);
		indexer.indexAllFiles();
	}

}
