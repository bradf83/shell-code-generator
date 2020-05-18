package com.bradf83.commandlinegenerator.generatecode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TableColumn {
    private String databaseColumnName;
    private String beanFieldName;
    private String type;
    private Integer length;
    private Integer precision;
    private Integer scale;
    private boolean nullable;
    private boolean primaryKey;

    public TableColumn(String databaseColumnName, String type, Integer length, Integer precision, Integer scale, boolean nullable) {
        this.databaseColumnName = databaseColumnName;
        this.type = type;
        this.length = length;
        this.precision = precision;
        this.scale = scale;
        this.nullable = nullable;
    }
}
