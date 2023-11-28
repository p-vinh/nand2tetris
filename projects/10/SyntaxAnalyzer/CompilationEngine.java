package SyntaxAnalyzer;

import java.io.File;
import java.io.FileNotFoundException;

public class CompilationEngine {
    private File output;

    public CompilationEngine() {
        try {
            CompileClass();
        } catch (FileNotFoundException fe) {
            fe.printStackTrace();
        }
    }

    public CompilationEngine(File input) {

        try {

            CompileClass();
        } catch (FileNotFoundException fe) {
            fe.printStackTrace();
        }
    }

    public void CompileClass() {
        
    }

    public void CompileClassVarDec() {
        
    }

    public void CompileSubroutine() {
        
    }

    public void compileParameterList() {
        
    }

    public void compileVarDec() {
        
    }

    public void compileStatements() {
        
    }

    public void compileDo() {

    }

    public void compileLet() {

    }

    public void compileWhile() {

    }

    public void compileReturn() {

    }

    public void compileIf() {

    }

    public void CompileExpression() {

    }

    public void CompileTerm() {

    }

    public void CompileExpressionList() {

    }
}
