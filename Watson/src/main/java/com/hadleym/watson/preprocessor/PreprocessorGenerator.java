package com.hadleym.watson.preprocessor;

import com.hadleym.watson.Constants;

// Simple class that generates a preprocessor
public class PreprocessorGenerator {
	public static Preprocessor standardPreprocessor(){
		return new Preprocessor("pp", Constants.STOP_WORDS, Constants.PARTS_OF_SPEECH);
	}

}
