package com.bradf83.commandlinegenerator.generatecode;

import com.bradf83.commandlinegenerator.utils.InputReader;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@ShellComponent
public class GenerateCode {
    private final InputReader inputReader;

    // User selects the database at runtime
    // TODO: It would be nice for this to be based on configuration and the application just pick the appropriate one
    private final OracleDatabaseOperations oracleDatabaseOperations;
    private final PostgreSQLDatabaseOperations postgreSQLDatabaseOperations;
    private DatabaseOperations selectedDatabaseOperations;

    // TODO: Convert to use configuration properties class and inject it
    @Value("${generator.model.package:''}")
    private String modelPackage;
    @Value("${generator.model.directory:''}")
    private String modelDirectory;
    @Value("${generator.repository.package:''}")
    private String repositoryPackage;
    @Value("${generator.repository.directory:''}")
    private String repositoryDirectory;
    @Value("${generator.controller-tests.package:''}")
    private String controllerTestsPackage;
    @Value("${generator.controller-tests.directory:''}")
    private String controllerTestsDirectory;

    @ShellMethod(value = "Generate Spring Data Rest Code based on a table", key = {"generate-code", "gc"})
    public String generateCode() throws Exception {
        this.validateConfiguration();

        String database = getInput("What database? [Oracle, PostgreSQL]");

        switch(database.toLowerCase()){
            case "oracle":
                this.selectedDatabaseOperations = this.oracleDatabaseOperations;
                break;
            case "postgresql":
                this.selectedDatabaseOperations = this.postgreSQLDatabaseOperations;
                break;
            default:
                return "The database you chose is not supported";
        }

        String table = getInput("What is the name of the table you wish to generate code for?  Remember this could be case sensitive");

        // Look for table information
        List<TableColumn> tableColumns = this.selectedDatabaseOperations.lookupTableColumns(table);

        if(tableColumns.size() == 0){
            return "The table " + table + " was not found.";
        }

        GenerationInfo generationInfo = new GenerationInfo();
        generationInfo.setTableName(table);
        generationInfo.addTableColumns(tableColumns);
        // Determine Primary Key
        generationInfo.setPrimaryKey(this.selectedDatabaseOperations.lookupPrimaryKeys(table));
        // Class Name
        generationInfo.setModelName(this.getInput("What is the name of the model for this table"));
        // Ensure the first letter is capital
        generationInfo.setModelName(generationInfo.getModelName().substring(0,1).toUpperCase() + generationInfo.getModelName().substring(1));

        // Ask any questions for each column
        for (TableColumn tableColumn : generationInfo.getTableColumns()) {
            tableColumn.setBeanFieldName(this.getInput("What is the bean field name for the [" + tableColumn.getDatabaseColumnName() + "] column"));
        }

        generationInfo
                .sortTableColumns()
                .writeModelInfoToConsole();

        this.writeModelFile(generationInfo);

        return "Generation complete.";
    }

    /**
     * Ensure that the model, repository and controller-tests configurations are valid.
     */
    private void validateConfiguration(){
        this.validateDirectoryNotBlank(this.modelDirectory, "model", "generator.model.directory");
        this.validateDirectoryExists(this.modelDirectory, "model");

        this.validateDirectoryNotBlank(this.repositoryDirectory, "repository", "generator.repository.directory");
        this.validateDirectoryExists(this.repositoryDirectory, "repository");

        this.validateDirectoryNotBlank(this.controllerTestsDirectory, "controller-tests", "generator.controller-tests.directory");
        this.validateDirectoryExists(this.controllerTestsDirectory, "controller-tests");
    }

    private void validateDirectoryNotBlank(String value, String name, String key){
        if(Strings.isBlank(value)){
            throw new RuntimeException(String.format("The %s directory is blank.  Please add a configuration for %s to your application.properties file.", name, key));
        }
    }

    private void validateDirectoryExists(String value, String name){
        File directory = new File(value);
        if(!directory.exists()){
            throw new RuntimeException(String.format("The %s directory you supplied does not exist.  Please check your application.properties configuration.", name));
        }
    }

    /**
     * Write the model file
     * @param generationInfo collection of information used to generate the model file.
     * @throws Exception an exception if an unprocessable error occurs (file exists)
     */
    private void writeModelFile(GenerationInfo generationInfo) throws Exception {
        File modelDirectory = new File(this.modelDirectory);

        File modelFile = new File(modelDirectory, generationInfo.modelName + ".java");
        if(modelFile.exists()){
            throw new RuntimeException("The model file you are trying to generate already exists.");
        }

        // package
        String modelPackage = "com.bradf83.model";

        // imports
        // TODO: Additional imports may be needed.  This will cover most cases.
        List<String> imports = new ArrayList<>();
        imports.add("lombok.EqualsAndHashCode");
        imports.add("lombok.Getter");
        imports.add("lombok.ToString");
        imports.add("javax.persistence.*");
        imports.add("java.time.Instant");

        // annotations
        List<String> annotations = new ArrayList<>();
        annotations.add("@Entity");
        annotations.add("@Table(name = \"" + generationInfo.getTableName() +"\")");
        annotations.add("@Getter");
        annotations.add("@EqualsAndHashCode(of = \"id\")");
        annotations.add("@ToString(onlyExplicitlyIncluded = true)");

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(modelFile, true))){
            writer.write("package " + modelPackage + ";");
            writer.newLine();
            writer.newLine();
            for (String anImport : imports) {
                writer.write("import " + anImport + ";");
                writer.newLine();
            }
            writer.newLine();
            for (String annotation : annotations) {
                writer.write(annotation);
                writer.newLine();
            }
            writer.write("public class " + generationInfo.getModelName() + " {");
            writer.newLine();
            writer.newLine();
            for (TableColumn field : generationInfo.getTableColumns()) {
                if(field.isPrimaryKey()){
                    writer.write(("\t@ToString.Include"));
                    writer.newLine();
                    writer.write("\t@Id");
                    writer.newLine();
                }
                writer.write("\t@Column(name = \"" + field.getDatabaseColumnName() + "\")");
                writer.newLine();
                writer.write("\tprivate " + this.selectedDatabaseOperations.determineJavaType(field) + " " + field.getBeanFieldName() + ";");
                writer.newLine();
                writer.newLine();
            }

            // Very custom notes based on the current process I follow.
            writer.write("\t// TODO: Make sure to define any relationships");
            writer.newLine();
            writer.write("\t// TODO: Make sure to add any SecurityConfiguration paths");
            writer.newLine();

            writer.write("}");
        }
    }

    /**
     * A helper to prompt a user for input
     * @param prompt the prompt message.
     * @return the user's input.
     */
    private String getInput(String prompt) {
        String input;

        do{
            input = this.inputReader.prompt(prompt);
        }while(input == null);

        return input;
    }

    private void writeRepositoryFile(GenerationInfo generationInfo) {
        // TODO:
        //  repository package
        //  repository folder
        //  indicator for query dsl
    }

    private void writeControllerTestFile(GenerationInfo generationInfo){
        // TODO:
        //  test controller package
        //  test controller folder
        //  plural name for model
        //  base test class import
    }
}
