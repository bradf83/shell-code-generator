package com.bradf83.commandlinegenerator.generatecode;

import java.util.List;

public interface DatabaseOperations {
    List<String> lookupPrimaryKeys(String table);
    List<TableColumn> lookupTableColumns(String table);
    String determineJavaType(TableColumn tableColumn);
}
