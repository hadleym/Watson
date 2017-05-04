package com.hadleym.watson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

public class App {

	public static void main(String[] args) throws IOException, ParseException {

		Preprocessor preprocessor = new Preprocessor("pp", null, Constants.PARTS_OF_SPEECH);
		System.out.println("Starting preprocessing...");
		File inputDir = new File("files");
		File outputDir = new File("preprocessedFiles");
		preprocessor.preprocessDirectory(inputDir, outputDir);
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
