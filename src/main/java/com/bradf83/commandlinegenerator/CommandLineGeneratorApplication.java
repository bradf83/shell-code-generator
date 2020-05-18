package com.bradf83.commandlinegenerator;

import com.bradf83.commandlinegenerator.utils.InputReader;
import org.jline.reader.LineReader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

@SpringBootApplication
public class CommandLineGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommandLineGeneratorApplication.class, args);
	}

	// Create our input reader, ensure the LineReader is lazy so that we don't have a cyclic dependency.
	@Bean
	public InputReader inputReader(@Lazy LineReader lineReader){
		return new InputReader(lineReader);
	}
}
