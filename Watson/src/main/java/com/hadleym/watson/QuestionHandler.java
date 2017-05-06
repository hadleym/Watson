package com.hadleym.watson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class QuestionHandler {
	String[] ignore = { "POTPOURRI" };
	File questionsFile;
	public ArrayList<Question> questions;

	public QuestionHandler(File filename) {
		questionsFile = filename;
		questions = new ArrayList<Question>();
		try {
			createQuestions();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

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
