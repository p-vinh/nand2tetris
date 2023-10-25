import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;



public class CodeWriter {
    private PrintWriter writer;
    public CodeWriter(File file) {
        try {
            writer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public CodeWriter(String fileName) {
        try {
            writer = new PrintWriter(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setFileName(String fileName) {
        
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
