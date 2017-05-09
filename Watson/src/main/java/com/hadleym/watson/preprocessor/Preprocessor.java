package com.hadleym.watson.preprocessor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

import com.hadleym.watson.Constants;
import com.hadleym.watson.Helper;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

/*
 * This class utilizes the NLPCore api to 'pre-process' a document.
 * It will strip all of the parts of speech not listed in the Constants class
 * as 'parts of speech' and will remove all stop words associated with the 
 * stop words array.  It then writes these preprocessed files to an
 * output directory to then be parsed by lucene. 
 * 
 * THIS PROCESS TAKES OVER 2 HOURS 
 */
public class Preprocessor {
	public HashSet<String> stopWords;
	public HashSet<String> parts;
	private String tag;
	public Preprocessor(String tag, String[] sw, String[] parts){
		this.tag = tag;
		this.parts = generateHashSet(parts);
		this.stopWords = generateHashSet(sw);
	}
	// generate a hashSet for the parts of speech we wish
	// to keep, and the stop words we do not wish to keep.
	public HashSet<String> generateHashSet(String[] p){
		HashSet<String> set = new HashSet<String>();
		for ( String s: p){
			set.add(s);
		}
		return set;
	}
	// preprocess an entire dirctory.
	public void preprocessDirectory(File srcDir, File destDir) throws IOException{
		String destPath = destDir.getPath();
		String separator = File.separator;
		int total = srcDir.listFiles().length;
		int count = 1;
		for ( File srcFile : srcDir.listFiles()){
			System.out.print(count++ + " of " + total + ":");
			String filename = destPath + separator + srcFile.getName() + ".pp";
			File destFile = new File(filename);
			preprocessFile(srcFile, destFile );
		}
		
	}
	// preprocess a file
	public void preprocessFile(File src, File dest) throws IOException {
		System.out.print("preprocessing file: " + src.getName());
		String line;
		BufferedWriter bw = null;
		bw = new BufferedWriter(new FileWriter(dest));
		BufferedReader br = new BufferedReader(new FileReader(src));
		int i = 0;
		for (line = br.readLine(); line != null; line = br.readLine()){
			i++;
			if (i%500 == 0){
				System.out.print('.');
			}
			bw.write(preprocessLine(line));
		}
		br.close();
		bw.close();
		System.out.println("Finished.");
		
	}
	/*
	 * Process a single line.  Parts of speech are identified, compared
	 * to the HashSet parts, and stop words are removed.
	 */
	public String preprocessLine(String line) {
		StringBuilder sb = new StringBuilder();
		if (!isCategory(line)) {
			// if the line begins with '==' or '#RED' then it can be ignored.
			if (!beginsWith(line, "==") && !beginsWith(line, "#RED")) {
				Document doc = new Document(line);
				for (Sentence sent : doc.sentences()) {
					for (int i = 0; i < sent.posTags().size(); i++) {
						// Parts of speech not contained in parts hashSet and are 
						// in the stopWords hashSet, are discarded.
						if ( parts.contains(sent.posTag(i)) && !stopWords.contains(sent.lemma(i))){ 
							sb.append(sent.lemma(i).toString() + " ");
						}
					}
					sb.append("\n");
				}
			}
		} else {
			// simply add the line as is (not processed) because it is 
			// a category.
			sb.append(line + "\n");
		}
		return sb.toString();
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
		String prefix = Constants.CATEGORY_PREFIX;
		if ( line.length() < prefix.length()){
			return false;
		}
		return line.substring(0, prefix.length()).equals(prefix);
	}
	/*
	 * Performs pre-processing with the NLPCore api. Lemmatizes, parses and
	 * removes specific parts of speech. This process can take over 3 hours on a
	 * laptop for the entire wiki provided.
	 */
	public static void preprocessDir(String iDir, String oDir) {
		File inputDir = new File(iDir);
		File outputDir = Helper.checkDirectoryAndCreate(oDir);
		if ( !inputDir.exists() || !inputDir.isDirectory() || inputDir.listFiles().length == 0){
			System.err.println("Directory [" + inputDir + "] is either empty, doesnt exist, or is not a directory. Please correct and try again.");
			System.err.println("This directory should contain all raw wikipedia files."); 
			System.err.println("Exiting.");
			System.exit(1);
		}
		System.err.println("Starting preprocessing...");
		Preprocessor preprocessor = PreprocessorGenerator.standardPreprocessor();
		try {
			preprocessor.preprocessDirectory(inputDir, outputDir);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error with input or output directory");
		}
		System.err.println("Preprocessing finished.");
	}

}
