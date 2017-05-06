package com.hadleym.watson;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

public class Question {
	public String category;
	public String question;
	public String answer;
	public int number;


	public String query;
	public int rank;
	public ScoreDoc[] hits;
	public Result[] results;
	public TopDocs topDocs;
	
	private String parsedQuestion;

	public Question(String s, String q, String a) {
		category = s;
		question = q;
		answer = a;
		number = -1;
	}

	public void setParsedQuestion(String p){
		parsedQuestion = p;
	}
	
	public String getParsedQuestion(){
		return parsedQuestion;
	}
	public void setNumber(int n){
		number = n;
	}

	public int getNumber(){
		return number;
	}
	
	public int calculateRank(){
		for (int i = 0; i < results.length; i++){
			if (answer.equals(results[i].name)){
				return i;
			}
		}
		return -1;
	}
	public void setRank(int i ){
		rank = i;
	}
	
	public int getRank(){
		return rank;
	}
	public String getQuery() {
		return question + " " + category;
	}

	public void setTopDocs(TopDocs topDocs) {
		this.topDocs = topDocs;
		setTopHits(topDocs.scoreDocs);
	}

	public void setTopHits(ScoreDoc[] hits){
		this.hits = hits;
	}

	public void printTopHits(){
		for ( int i = 0; i < hits.length; i++){
			System.out.println(hits[i].score);
		}
	}
	@Override
	public String toString() {
		return "QUESTION: " + question + "\nANSWER: " + answer + "\nTOP RESULT: " + results[0].name + "\nRANK: " + getRank() + "\n";
	}

	public void setResults(Result[] results) {
		this.results = results;
	}

	public void printResults(){
		for (int i = 0; i < this.results.length; i++){
			int num = i+1;
			System.out.println(num + ") "+results[i].score + ":\t " + results[i].name );
		}
	}
	
	public void printQuestion(){
		if (getNumber()>=0){
			System.out.println("NUMBER " + getNumber());
		}
		System.out.println("CATEGORY: " + category);
		System.out.println("QUESTION: \"" + question +"\"");
		System.out.println("CORRECT ANSWER: " + answer);
		System.out.println("PARSED QUESTION: " + getParsedQuestion());
		if (rank >= 0 ){
			int foundrank = rank+1;
			System.out.println("WATSON FOUND AT RANK: " + foundrank);
		} else {
			System.out.println("WATSON DID NOT FIND IN TOP " + Constants.HITSPERPAGE);
		}
		printResults();	
		System.out.println("");
	}

}
