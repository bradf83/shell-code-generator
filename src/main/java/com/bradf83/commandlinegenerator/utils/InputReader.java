package com.bradf83.commandlinegenerator.utils;

import org.jline.reader.LineReader;

public class InputReader {
    private LineReader lineReader;

    public InputReader(LineReader lineReader) {
        this.lineReader = lineReader;
    }

    public String prompt(String prompt){
        String input;
        input = lineReader.readLine(prompt + ": ");
        return input;
    }
}
