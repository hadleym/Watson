package com.hadleym.watson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

public class Preprocessor {
	public HashSet<String> stopWords;
	public HashSet<String> parts;
	private String tag;
	public Preprocessor(String tag, String[] sw, String[] parts){
		this.tag = tag;
		this.parts = generateHashSet(parts);
		this.stopWords = generateHashSet(sw);
	}
	public HashSet<String> generateHashSet(String[] p){
		HashSet<String> set = new HashSet<String>();
		for ( String s: p){
			set.add(s);
		}
		return set;
	}
	public void preprocessDirectory(File srcDir, File destDir) throws IOException{
		String destPath = destDir.getPath();
		String separator = File.separator;
		for ( File srcFile : srcDir.listFiles()){
			String filename = destPath + separator + srcFile.getName() + ".pp";
			File destFile = new File(filename);
			preprocessFile(srcFile, destFile );
		}
		
	}
	public void preprocessFile(File src, File dest) throws IOException {
		System.out.println("preprocessing file: " + src.getName());
		String line;
		BufferedWriter bw = null;
		bw = new BufferedWriter(new FileWriter(dest));
		BufferedReader br = new BufferedReader(new FileReader(src));
		for (line = br.readLine(); line != null; line = br.readLine()){
			bw.write(preprocessLine(line));
		}
		
		br.close();
		bw.close();
		System.out.println("Finished preprocessing file: " + src.getName());
		
	}
	public String preprocessLine(String line) {
		StringBuilder sb = new StringBuilder();
		if (!isCategory(line)) {
			if (!beginsWith(line, "==") && !beginsWith(line, "#RED")) {
				Document doc = new Document(line);
				for (Sentence sent : doc.sentences()) {
					for (int i = 0; i < sent.posTags().size(); i++) {
						if ( parts.contains(sent.posTag(i)) && !stopWords.contains(sent.lemma(i))){ 
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

}
