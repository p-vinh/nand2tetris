
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;


public class VMWriter {
    private PrintWriter writer;


    public enum Command {
        ADD, SUB, NEG, EQ, GT, LT, AND, OR, NOT, NONE
    }

    public VMWriter(File file) {
        try {
            writer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void write(String command) {
        writer.println(command);
    }

    public void writePush(String segment, int index) {
        write("push " + segment + " " + index);
        writer.flush();
    }

    public void writePop(String segment, int index) {
        write("pop " + segment + " " + index);
        writer.flush();
    }

    public void writeArithmetic(String command) {
        
        switch (command) {
            case "+":
                write("add");
                break;
            case "-":
                write("sub");
                break;
            case "--":
                write("neg");
                break;
            case "=":
                write("eq");
                break;
            case ">":
                write("gt");
                break;
            case "<":
                write("lt");
                break;
            case "&":
                write("and");
                break;
            case "|":    
                write("or");
                break;
            case "~":
                write("not");
                break;
            default:
                break;
        }

        writer.flush();
    }

    public void writeLabel(String label) {
        write("label " + label);
        writer.flush();
    }

    public void writeGoto(String label) {
        write("goto " + label);
        writer.flush();

    }

    public void writeIf(String label) {
        write("if-goto " + label);
        writer.flush();
    }

    public void writeCall(String name, int nArgs) {
        write("call " + name + " " + nArgs);
        writer.flush();
    }

    public void writeFunction(String name, int nLocals) {
        write("function " + name + " " + nLocals);
        writer.flush();
    }

    public void writeReturn() {
        write("return");
        writer.flush();
    }

    public void close() {
        writer.close();
    }   

    public PrintWriter getWriter() {
        return this.writer;
    }
}
