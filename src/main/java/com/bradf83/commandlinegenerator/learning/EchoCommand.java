package com.bradf83.commandlinegenerator.learning;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class EchoCommand {
    @ShellMethod("Displays name that is supplied")
    public String echo(@ShellOption({"-N", "--name"}) String name) {
        return String.format("Hello %s!.", name);
    }
}
