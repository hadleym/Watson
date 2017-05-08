package com.hadleym.watson;

import java.io.File;
import java.util.ArrayList;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;

public class Helper {

	/*
	 * Helper method for handling file structure.
	 */
	public static File checkDirectoryAndCreate(String filename) {
		File f = new File(filename);
		if (!f.exists()) {
			System.err.println("Directory " + filename + " doesn't exist...creating");
			f.mkdir();
		} else if (!f.isDirectory()) {
			System.err.println("[" + filename + "] already exists and is not a directory. Exiting.");
			System.exit(1);
		}
		return f;
	}

	/*
	 * Overriden to use defaults from Constants class.
	 */
	public static ArrayList<QueryHelper> buildAllFour() {
		return buildAllFour(Constants.QUESTIONS_FILE, Constants.NLP_INDEX, Constants.STD_INDEX);
	}

	/*
	 * Helper method for creating all 4 of the types of analyzer/scoring
	 * combinations
	 * 
	 * Core NLP Branch = 'rawFiles' preprocessed with CoreNLP ( a 3 hour
	 * process) and then indexed with Lucene Whitespace analyzer.
	 * 
	 * StandardAnalyzer Branch = 'rawFiles' indexed with Lucene
	 * StandardAnalyzer.
	 * 
	 * Both of the branches calculate MRR and Precision @ 1 with both Classic
	 * Analyzer(tf-idf) and BM25 Analyzer.
	 * 
	 */
	public static ArrayList<QueryHelper> buildAllFour(String q, String nIndex, String sIndex) {
		File questions = new File(q);
		if ( questions.isDirectory() || !questions.exists()){
			System.err.println("[questions.txt] does not exist. This file was provided with the submission.");
			System.err.println("Please make sure it is in the base directory of the project or the same directory as the .jar file.");
			System.exit(1);
		}
		File nlpIndex = Helper.checkDirectoryAndCreate(nIndex);
		File stdIndex = Helper.checkDirectoryAndCreate(sIndex);
		ArrayList<QueryHelper> queries = new ArrayList<QueryHelper>();
		queries.add(new QueryHelper(questions, stdIndex, new StandardAnalyzer(), null, true, new ClassicSimilarity()));
		queries.add(new QueryHelper(questions, stdIndex, new StandardAnalyzer(), null, true, new BM25Similarity()));
		queries.add(new QueryHelper(questions, nlpIndex, new WhitespaceAnalyzer(),
				PreprocessorGenerator.standardPreprocessor(), true, new ClassicSimilarity()));
		queries.add(new QueryHelper(questions, nlpIndex, new WhitespaceAnalyzer(),
				PreprocessorGenerator.standardPreprocessor(), true, new BM25Similarity()));
		return queries;
	}

	/*
	 * Usage messages for the App.
	 */
	public static void printUsageMessage() {
		System.out.println("USAGE: java -jar Watson.jar -[pre,index,evaluate]");
		System.out.println();
		System.out.println("USAGE: java -jar Watson.jar -pre");
		System.out.println("\t Preprocess all files in SRC_DIR to PREPROCESS_DIR.");
		System.out.println("\t This is MANDATORY for the indexing of the Core NLP branch discussed in the attached paper.");
		System.out.println("\t This is a 3 hour process on a 2.2 GHz machine with a slow HDD.");
		System.out.println( "USAGE: java -jar Watson.jar -index");
		System.out.println("\t Index all preprocessed and rawFiles for both the StandardAnalzyer Branch and the NLP Core branch. ");
		System.out.println("\t Dependant on the -p flag being run on rawFiles ( a 3 hour process ).");
		System.out.println("USAGE: java -jar Watson.jar -evaluate");
		System.out.println("\t Evaluate both branches with both scoring methods. ");
		System.out.println("\t Output to predetermined output files.");
		System.out.println();
		System.out.println("Additional Features: SEE ATTACHED README.txt");
	}

}
