package com.hadleym.watson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
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
 * 
 * 
 * 
 */
// Usage: "$ java App -p" 						      -- preprocess all files in " + Constants.RAW_FILE_DIR + " to " + Constants.PREPROCESS_DIR);
//        "$ java App -inlp SRC_DIR INDEX_DIR"	      -- index all files in SRC_DIR to INDEX_DIR with the Lucene Whitespace analyzer.");
//        "$ java App -istd SRC_DIR INDEX_DIR         -- index all files in SRC_DIR to INDEX_DIR with the Lucene Standard Analyzer.");
//        "$ java App -enlp QUESTIONS_FILE INDEX_DIR  -- Evaluate the QUESTIONS_FILE vs the INDEX_DIR with the Preprocessor and Whitespace Analyzer.");
//        "$ java App -estd QUESTIONS_FILE INDEX_DIR  -- Evaluate the QUESTIONS_FILE vs the INDEX_DIR with Lucene Standard Analyzer.");
public class App {

	public static void main(String[] args) throws IOException, ParseException {
		// this is for the files referred to as 'nlp' preprocessed files.
		if (args.length == 3 && args[0].equals("-p")) {
			try {
				File srcDir = new File(args[1]);
				File destDir = new File(args[0]);
				preprocessDir(srcDir, destDir);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("please make sure the directories exist and contain files to preprocess");
				System.exit(1);
			}
			// will index the nlp preprocessed files with the lucene
			// whitespace analyzer.
		} else if (args.length == 3 && args[0].equals("-inlp")) {
			String filesToIndex = args[1];
			String index = args[2];
			index(new File(filesToIndex), new File(index), new WhitespaceAnalyzer());
			// will index the files with the lucene standard analyzer
		} else if (args.length == 2 && args[0].equals("-istd")) {
			String filesToIndex = args[1];
			File index = new File(args[2]);
			if (index.listFiles().length > 0) {
				System.out.println(index.getName() + " is not empty.  Please empty index directory and retry.");
				System.exit(1);
			}
			index(new File(filesToIndex), index, new StandardAnalyzer());
			// will evaluate the nlp pre-processed files vs. the collection
			// of questions.
		} else if (args.length == 3 && args[0].equals("-enlp")) {
			System.out.println("Evaluating against the questions file '" + args[1] + "' with the preprocessed index '" + args[2] + "' with whitespace analyzer...");
			Preprocessor preprocessor = PreprocessorGenerator.standardPreprocessor();
			String questions = args[1];
			String index = args[2];
			QueryHelper nlpQuery = new QueryHelper(new File(questions), new File(index), new WhitespaceAnalyzer(),
					preprocessor, true);
			// nlpQuery.analyze();
			// nlpQuery.printRanks();
			nlpQuery.executeQuestions();
			nlpQuery.printRanks();
			// will evaluate the lucene standard analyzer index documents vs
			// the collection of questions.
		} else if (args.length == 3 && args[0].equals("-estd")) {
			System.out.println("Evaluating the questions file [" + args[1] + "] with the index dir " + args[2]
					+ " with standard analyzer...");
			String questions = args[1];
			String index = args[2];
			QueryHelper stdQuery = new QueryHelper(new File(questions), new File(index), new StandardAnalyzer(), null,
					true);
			stdQuery.executeQuestions();
			stdQuery.printRanks();
			// stdQuery.printCorrectQuestions();

		} else {
			printUsageMessage();
			System.exit(1);
		}

	}

	public static void printUsageMessage() {
		System.out.println("Usage: App -p \t preprocess all files in " + Constants.RAW_FILE_DIR + " to "
				+ Constants.PREPROCESS_DIR);
		System.out.println(
				"Usage: App -inlp SRC_DIR INDEX_DIR \t index all files in SRC_DIR to INDEX_DIR with the Lucene Whitespace analyzer.");
		System.out.println(
				"Usage: App -istd SRC_DIR INDEX_DIR \t index all files in SRC_DIR to INDEX_DIR with the Lucene Standard Analyzer.");
		System.out.println(
				"Usage: App -enlp QUESTIONS_FILE INDEX_DIR \t Evaluate the QUESTIONS_FILE vs the INDEX_DIR with the Preprocessor and Whitespace Analyzer.");
		System.out.println(
				"Usage: App -estd QUESTIONS_FILE INDEX_DIR \t Evaluate the QUESTIONS_FILE vs the INDEX_DIR with Lucene Standard Analyzer.");
	}

	public static void index(File inputDir, File outputDir, Analyzer analyzer) {
		System.out.println("indexing directory: " + inputDir.getName() + " to ouput to " + outputDir.getName());
		DocumentIndexer indexer = new DocumentIndexer(inputDir, outputDir, analyzer);
		indexer.indexAllFiles();
	}

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

	public static void preprocessingFile(File inputFile, File outputFile) throws IOException {
		String line;
		BufferedWriter bw = null;
		FileWriter fw = null;
		fw = new FileWriter(outputFile);
		bw = new BufferedWriter(fw);
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		for (line = br.readLine(); line != null; line = br.readLine()) {
			bw.write(preprocessLine(line));
			/*
			 * if (!beginsWith(line, "[[")) { if (!beginsWith(line, "==") &&
			 * !beginsWith(line, "#RED")) { Document doc = new Document(line);
			 * for (Sentence sent : doc.sentences()) { StringBuilder sb = new
			 * StringBuilder(); for (int i = 0; i < sent.posTags().size(); i++)
			 * { if (keepPartOfSpeech(sent.posTag(i))) {
			 * sb.append(sent.lemma(i).toString() + " "); } }
			 * bw.write(sb.toString() + "\n"); } } } else { bw.write(line +
			 * "\n"); }
			 */
		}

		br.close();
		bw.close();
		System.out.println("File " + outputFile.toPath() + " created");
	}

	public static String preprocessLine(String line) {
		StringBuilder sb = new StringBuilder();
		if (!beginsWith(line, "[[")) {
			if (!beginsWith(line, "==") && !beginsWith(line, "#RED")) {
				Document doc = new Document(line);
				for (Sentence sent : doc.sentences()) {
					for (int i = 0; i < sent.posTags().size(); i++) {
						if (keepPartOfSpeech(sent.posTag(i))) {
							sb.append(sent.lemma(i).toString() + " ");
						}
					}
				}
			}
		} else {
			sb.append(line + "\n");
		}
		return sb.toString();
	}

	public static boolean keepPartOfSpeech(String pos) {
		if (pos.length() == 1) {
			if (pos.equals("V")) {
				return true;
			} else {
				return false;
			}
		}
		String firstTwo = pos.substring(0, 2);
		if ((pos.charAt(0) == 'V') || firstTwo.equals("RB") || firstTwo.equals("JJ") || firstTwo.equals("NN")) {
			return true;
		}
		return false;
	}

	// strip the leading 2 characters from a line.
	public static String parseCategory(String s) {
		String returnString = s.substring(2, s.length() - 2);
		return returnString;
	}

	// This returns if the line of text is a section
	// for example:
	// ==Information==
	// or
	// #REDIRECT
	// is a section, and is most likely not relevant.
	public static boolean beginsWith(String line, String prefix) {
		if (line.length() >= prefix.length()) {
			return line.substring(0, prefix.length()).equals(prefix);
		}
		return false;
	}

	// Determines if a string is a category, denoted by
	// a leading '=='. Should be added to the Category field
	// of the index.
	public static boolean isCategory(String line) {
		return (line.length() > 2 && line.charAt(0) == '[' && line.charAt(1) == '[');
	}

	public static void searchIndex(Path directoryPath, String searchString) throws IOException, ParseException {
		Directory directory = FSDirectory.open(directoryPath);
		IndexReader indexReader = DirectoryReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		StandardAnalyzer analyzer = new StandardAnalyzer();
		QueryParser queryParser = new QueryParser(Constants.FIELD_CONTENTS, analyzer);
		Query query = queryParser.parse(searchString);
		if (Constants.DEBUG) {
			System.out.println("SearchString: " + searchString);
			System.out.println("Query: " + query);
		}

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
