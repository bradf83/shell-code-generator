package com.bradf83.commandlinegenerator.generatecode;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Setter
@Getter
@NoArgsConstructor
public class GenerationInfo {
    String tableName;
    String modelName;
    List<TableColumn> tableColumns = new ArrayList<>();
    List<String> warnings = new ArrayList<>();
    private boolean queryDsl;

    public GenerationInfo addTableColumn(TableColumn tableColumn){
        this.tableColumns.add(tableColumn);
        return this;
    }

    public GenerationInfo addTableColumns(List<TableColumn> tableColumns){
        this.tableColumns.addAll(tableColumns);
        return this;
    }

    public GenerationInfo addWarning(String warning){
        this.warnings.add(warning);
        return this;
    }

    public GenerationInfo setPrimaryKey(List<String> primaryKeys){
        if(primaryKeys.size() == 1){
            String primaryKeyColumn = primaryKeys.get(0);
            Optional<TableColumn> primaryKey = this.tableColumns.stream().filter(col -> col.getDatabaseColumnName().equalsIgnoreCase(primaryKeyColumn)).findFirst();
            if(primaryKey.isEmpty()){
                warnings.add(String.format("Unable to find the primary key [%s], in the list of columns", primaryKeyColumn));
            } else {
                primaryKey.get().setPrimaryKey(true);
            }
        } else {
            this.addWarning("Composite Primary Key Detected.  Unable to set.");
        }
        return this;
    }

    // Currently sorts by beanFieldName but may want to sort by Primary Key > Fields(beanFieldName) > Relationships
    public GenerationInfo sortTableColumns(){
        this.tableColumns.sort((o1, o2) -> o1.getBeanFieldName().compareToIgnoreCase(o2.getBeanFieldName()));
        return this;
    }

    public GenerationInfo writeModelInfoToConsole(){
        System.out.println("Table [" + this.getTableName() + "] and model name [" + this.getModelName() + "]");
        for (TableColumn field : this.tableColumns) {
            System.out.println("Database Column: [" + field.getDatabaseColumnName() + "] will have field name [" + field.getBeanFieldName() + "]");
        }
        return this;
    }
}
