package com.bradf83.commandlinegenerator.generatecode;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@ConditionalOnExpression("#{'${spring.datasource.url}'.contains('postgresql')}")
public class PostgreSQLDatabaseOperations implements DatabaseOperations {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<String> lookupPrimaryKeys(String table) {
        List<String> constraints = jdbcTemplate.query("SELECT constraint_name FROM information_schema.table_constraints WHERE table_name = '" + table + "' AND constraint_type = 'PRIMARY KEY'", (rs, rowNumber) -> rs.getString(1));
        if(constraints.size() != 1){
            return new ArrayList<>();
        }
        return jdbcTemplate.query("SELECT column_name FROM information_schema.key_column_usage WHERE constraint_name = '" + constraints.get(0) + "'", (rs, rowNumber) -> rs.getString(1));
    }

    @Override
    public List<TableColumn> lookupTableColumns(String table) {
        List<TableColumn> results;
        results = jdbcTemplate.query("SELECT column_name databaseColumnName, data_type as \"type\", character_maximum_length length, numeric_precision as \"precision\", numeric_precision_radix scale, is_nullable nullable" +
                " FROM information_schema.columns c WHERE table_name = '" + table + "'", new BeanPropertyRowMapper<>(TableColumn.class));
        return results;
    }

    @Override
    public String determineJavaType(TableColumn tableColumn) {
        // Likely will need more type mappings defined here.
        switch(tableColumn.getType().toLowerCase()){
            case "bigint":
                return "Long";
            case "character varying":
                return "String";
            case "timestamp without time zone":
                return "Instant";
            default:
                throw new RuntimeException("Unable to determine type for table column [" + tableColumn.getDatabaseColumnName() + "]");
        }
    }
}
