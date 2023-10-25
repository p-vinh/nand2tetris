import java.io.File;

public class VMTranslator {
    private File file;
    private Parser parser;
    private CodeWriter codeWriter;

    public VMTranslator(File file) {
        String fileName = file.getName().split(".vm")[0];
        this.file = file;
        this.parser = new Parser(file);
        this.codeWriter = new CodeWriter(fileName);
    }

    public void generateAssemblyFile() {

        while (parser.hasMoreCommands()) {
            parser.advance();

            
        }
    }
    public static void main(String[] args) {
        VMTranslator main = new VMTranslator(new File(args[0]));
        main.generateAssemblyFile();
        
    }
}