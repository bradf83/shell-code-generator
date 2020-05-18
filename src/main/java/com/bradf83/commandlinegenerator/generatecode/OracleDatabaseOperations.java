package com.bradf83.commandlinegenerator.generatecode;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@ConditionalOnExpression("#{'${spring.datasource.url}'.contains('oracle')}")
public class OracleDatabaseOperations implements DatabaseOperations {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<String> lookupPrimaryKeys(String table) {
        List<String> constraints = jdbcTemplate.query("SELECT constraint_name FROM ALL_CONSTRAINTS WHERE table_name = '" + table + "' AND constraint_type = 'P'", (rs, rowNumber) -> rs.getString(1));
        if(constraints.size() != 1){
            return new ArrayList<>();
        }
        return jdbcTemplate.query("SELECT column_name FROM ALL_CONS_COLUMNS WHERE table_name = '" + table + "' AND constraint_name = '" + constraints.get(0) + "'", (rs, rowNumber) -> rs.getString(1));
    }

    @Override
    public List<TableColumn> lookupTableColumns(String table) {
        List<TableColumn> results;
        results = jdbcTemplate.query("SELECT column_name databaseColumnName, data_type type, data_length length, data_precision precision, data_scale scale, nullable nullable FROM all_tab_columns WHERE table_name = '" + table + "'", new BeanPropertyRowMapper<>(TableColumn.class));
        return results;
    }

    @Override
    public String determineJavaType(TableColumn tableColumn) {
        // Likely will need more type mappings defined here.
        switch(tableColumn.getType()){
            case "DATE":
                return "Instant";
            case "NUMBER":
                // TODO: More edge cases here but this will cover most cases.
                //  Integers will be classified longs in some cases here.
                if(tableColumn.getPrecision() <= 12 && tableColumn.getScale() == 0) {
                    return "Long";
                }
                if(tableColumn.getScale() > 0) {
                    return "Double";
                }
                throw new RuntimeException("Unable to determine type for table column [" + tableColumn.getDatabaseColumnName() + "]");
            case "VARCHAR2":
                // TODO: Depending on the size you may want something bigger than a String, but will handle most cases.
                return "String";
            default:
                throw new RuntimeException("Unable to determine type for table column [" + tableColumn.getDatabaseColumnName() + "]");
        }
    }
}
