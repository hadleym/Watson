package com.hadleym.watson;

import java.util.HashSet;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

public class Preprocessor {
	public String[] partsOfSpeechToKeep = { "V", "RB", "JJ", "NN" };
	public HashSet<String> stopWords;
	public Preprocessor(HashSet<String> sw, String[] parts){
		stopWords = sw;
		partsOfSpeechToKeep = parts;
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

}
