package com.hadleym.watson;

import java.io.File;
import java.util.ArrayList;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;

public class Helper {

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
	 * Helper method for creating all 4 of the types of analyzer/scoring combinations
	 * 
	 * Core NLP Branch = 'rawFiles' preprocessed with CoreNLP ( a 3 hour process) and then indexed with Lucene
	 * Whitespace analyzer.
	 * 
	 * StandardAnalyzer Branch = 'rawFiles' indexed with Lucene StandardAnalyzer.
	 * 
	 * Both of the branches calculate MRR and Precision @ 1 with both Classic Analyzer(tf-idf)
	 * and BM25 Analyzer.
	 * 
	 */
	public static ArrayList<QueryHelper> buildAllFour(String q, String nIndex, String sIndex) {
		File questions = new File(q);
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

}
