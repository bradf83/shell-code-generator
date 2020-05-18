package com.bradf83.commandlinegenerator.learning;

import com.bradf83.commandlinegenerator.utils.InputReader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.Map;

@ShellComponent
public class ReadTable {

    private final InputReader inputReader;
    private final JdbcTemplate jdbcTemplate;

    public ReadTable(InputReader inputReader, JdbcTemplate jdbcTemplate) {
        this.inputReader = inputReader;
        this.jdbcTemplate = jdbcTemplate;
    }

    @ShellMethod("Prompt for a table name and read the data.")
    public String readTable() {

        String tableName;

        do {
            tableName = this.inputReader.prompt("What is the name of the table?");
        } while(tableName == null);

        Map<String, Object> stringObjectMap = this.jdbcTemplate.queryForMap("SELECT * FROM " + tableName);

        System.out.println(stringObjectMap);

        String something = null;

        do {
            something = this.inputReader.prompt("What should it be " + stringObjectMap.get("short_name") + ":" );
        } while (something == null);

        System.out.println(something);

        return "Table read.";
    }
}
