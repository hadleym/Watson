package com.hadleym.watson;

public class PreprocessorGenerator {
	public static Preprocessor standardPreprocessor(){
		return new Preprocessor("pp", Constants.STOP_WORDS, Constants.PARTS_OF_SPEECH);
	}

}
