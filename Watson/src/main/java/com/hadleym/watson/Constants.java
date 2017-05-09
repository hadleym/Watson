package com.hadleym.watson;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

public class Constants {
	public static final Analyzer whitespaceAnalyzer = new WhitespaceAnalyzer();
	public static final Analyzer standardAnalyzer = new StandardAnalyzer();
	public static final boolean DEBUG = false;

//	public static final String STANDARD_ANALYZE_DIR = "standardIndex";
//	public static final String NLP_INDEX = "nlpIndex";
//	public static final String LUCENE_INDEX = "luceneIndex";
//	public static final String INDEX_DIR = "index";
	public static final String RAW_FILE_DIR = "rawFiles";
	public static final String NLP_PREPROCESS_DIR = "preprocessed";
	public static final String NLP_INDEX = "nlpIndex";
	public static final String STD_INDEX = "standardIndex";
	public static final String QUESTIONS_FILE = "questions.txt";
	public static final String FIELD_CONTENTS = "contents";
	public static final String FIELD_CATEGORY = "category";
	public static final boolean recreate = true;
	public static final int HITSPERPAGE = 10;
	public static final String[] PARTS_OF_SPEECH = {"JJ", "JJR", "JJS", "NN", "NNS", "NNP", 
			"NNPS", "RB", "RBR", "RBS", "VB", "VBD", "VBG" ,"VBN", "VBP", "VBZ"};
	public static final String[] STOP_WORDS = {"a", "an", "and", "are", "as", "at", "be", "but", "by",
			"for", "if", "in", "into", "is", "it",
			"no", "not", "of", "on", "or", "such",
			"that", "the", "their", "then", "there", "these",
			"they", "this", "to", "was", "will", "with"};
	
	public static final String CATEGORY_PREFIX = "[[";
	public static final String[] IGNORE_PREFIX = { "==", "#RED" };
	public static final String ENG_INDEX = "engIndex";
	public static Directory getDirectory(Path p) {
		try { 
			return new SimpleFSDirectory(p);
		} catch (IOException io) {
			io.printStackTrace();
		}
		return null;
	}



}
