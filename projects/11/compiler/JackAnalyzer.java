
import java.io.File;
public class JackAnalyzer {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java JackAnalyzer <file.jack|dir>");
            System.exit(1);
        }

        File file = new File(args[0]);

        if (file.isDirectory())
            for (File f : file.listFiles()) {
                if (f.getName().endsWith(".jack")) {
                    CompilationEngine compilationEngine = new CompilationEngine(f);
                    compilationEngine.CompileClass();
                }
            }
        else {
            CompilationEngine compilationEngine = new CompilationEngine(file);
            compilationEngine.CompileClass();
        }
    }

}
