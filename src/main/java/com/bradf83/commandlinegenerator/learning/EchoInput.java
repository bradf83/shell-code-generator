package com.bradf83.commandlinegenerator.learning;

import com.bradf83.commandlinegenerator.utils.InputReader;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class EchoInput {

    private final InputReader inputReader;

    public EchoInput(InputReader inputReader) {
        this.inputReader = inputReader;
    }

    @ShellMethod("Displays name input from user.")
    public String echoInput() {

        String input;

        do {
            input = this.inputReader.prompt("What is your name?");
        } while(input == null);

        return String.format("Hello %s!.", input);
    }
}
