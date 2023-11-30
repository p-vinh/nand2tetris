package SyntaxAnalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;


public class VMWriter {
    public enum Segment {
        CONST, ARG, LOCAL, STATIC, THIS, THAT, POINTER, TEMP, NONE
    }

    public enum Command {
        ADD, SUB, NEG, EQ, GT, LT, AND, OR, NOT, NONE
    }

    public VMWriter(File file) {

    }

    public void writePush(Segment segment, int index) {
    }

    public void writePop(Segment segment, int index) {
    }

    public void writeArithmetic(Command command) {
    }

    public void writeLabel(String label) {
    }

    public void writeGoto(String label) {
    }

    public void writeIf(String label) {
    }

    public void writeCall(String name, int nArgs) {
    }

    public void writeFunction(String name, int nLocals) {
    }

    public void writeReturn() {
    }

    public void close() {
    }   
}
