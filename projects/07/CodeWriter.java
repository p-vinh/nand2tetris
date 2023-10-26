import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CodeWriter {
    private PrintWriter writer;
    private int labelCounter = 0;
    private String file;
    public CodeWriter(File file) {
        File outputFile = new File(file.getAbsolutePath().split(".vm")[0] + ".asm");
        try {
            writer = new PrintWriter(new FileWriter(outputFile));
            this.file = file.getName();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setFileName(String fileName) {
        file = fileName;
    }

    public void writeArithmetic(String command) {
        
    }

    public void writePushPop(Parser.CommandType commandType, String segment, int index) {

    }

    // Closes the output file
    public void close() {
        writer.close();
    }
}
