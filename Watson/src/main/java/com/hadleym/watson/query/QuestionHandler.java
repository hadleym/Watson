package com.hadleym.watson.query;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

// This class is the interface between the QueryHelper and
// the Question class.
public class QuestionHandler {
	// ignore "POTPOURRI" as a category in jeopardy.
	String[] ignore = { "POTPOURRI" };
	File questionsFile;
	public ArrayList<Question> questions;

	// analyzes the Question file.
	public QuestionHandler(File filename) {
		questionsFile = filename;
		questions = new ArrayList<Question>();
		try {
			createQuestions();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// reads through the file and creates questions
	// to then be used by the QueryHelper class.
	public void createQuestions() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(questionsFile));
		for (String subject = br.readLine(); subject != null; subject = br.readLine()) {

			for (String w : ignore) {
				if (subject.equals(w)) {
					subject = "";
					break;
				}
			}

			questions.add(new Question(subject, br.readLine(), br.readLine()));

			// read blank line
			br.readLine();
		}
		br.close();
	}

}
