

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

    public void writeArithmetic(Command command) {
        switch (command) {
            case ADD:
                write("add");
                break;
            case SUB:
                write("sub");
                break;
            case NEG:
                write("neg");
                break;
            case EQ:
                write("eq");
                break;
            case GT:
                write("gt");
                break;
            case LT:
                write("lt");
                break;
            case AND:
                write("and");
                break;
            case OR:    
                write("or");
                break;
            case NOT:
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
