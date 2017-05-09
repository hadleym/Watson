package com.hadleym.watson.query;

// A container class that holds name and score from a lucene query.
public class Result {
	public String name;
	public float score;

	public Result(String n, float s) {
		name = n;
		score = s;
	}
}
