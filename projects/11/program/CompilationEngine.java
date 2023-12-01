
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CompilationEngine {
    private static enum TokenType {
        KEYWORD, SYMBOL, IDENTIFIER, INT_CONST, STRING_CONST
    }

    private static enum KeyWord {
        CLASS, METHOD, FUNCTION, CONSTRUCTOR, INT, BOOLEAN, CHAR, VOID, VAR, STATIC, FIELD, LET, DO, IF, ELSE, WHILE,
        RETURN, TRUE, FALSE, NULL, THIS
    }

    private File output;
    private File input;
    private JackTokenizer tokenizer;
    private SymbolTable symbolTable;
    private VMWriter vmWriter;
    private int expressionNum;
    private String className;
    private int index = 0;
    private String regex = "^[a-zA-Z_]{1}[a-zA-Z0-9_]*";

    public CompilationEngine(File input) {
        this.input = input;
        this.output = new File(input.getParent() + "\\" + input.getName().split("\\.")[0] + ".vm");
        this.vmWriter = new VMWriter(output);
        this.symbolTable = new SymbolTable();

        try {
            this.tokenizer = new JackTokenizer(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void CompileClass() {
        tokenizer.advance();
        if (!tokenizer.getToken().equals("class"))
            throw new RuntimeException("Expected class declaration");

        tokenizer.advance();
        className = tokenizer.getToken();
        if (!className.matches(regex)) {
            throw new RuntimeException("Expected class name");
        }

        tokenizer.advance();
        if (!tokenizer.getToken().equals("{"))
            throw new RuntimeException("Expected {");

        tokenizer.advance();

        while (!tokenizer.getToken().equals("}")) {
            if (tokenizer.getToken().equals("static") || tokenizer.getToken().equals("field")) {
                CompileClassVarDec();
            } else if (tokenizer.getToken().equals("constructor") || tokenizer.getToken().equals("function")
                    || tokenizer.getToken().equals("method")) {
                CompileSubroutine();
            } else {
                throw new RuntimeException("Expected class var declaration or subroutine declaration");
            }
        }

        tokenizer.advance();

        if (!tokenizer.getToken().equals("}"))
            throw new RuntimeException("Expected }");
    }

    private boolean isExpression(String curToken) {
        if (curToken.matches("^[0-9]+") ||
                (curToken.substring(0, 1).equals("\"") && curToken.substring(0, 1).equals("\"")) ||
                curToken.equals("true") || curToken.equals("false") ||
                curToken.equals("null") || curToken.equals("this") ||
                curToken.matches(regex) ||
                curToken.equals("(") || curToken.equals("-") ||
                curToken.equals("~")) {
            return true;
        } else {
            return false;
        }
    }

    public void CompileClassVarDec() {
        String name = "";
        String type = "";
        String kind = "";

        if (tokenizer.keyWord().equals("static")) {
            kind = "static";
        } else if (tokenizer.keyWord().equals("field")) {
            kind = "field";
        } else {
            throw new RuntimeException("Expected static or field declaration");
        }

        tokenizer.advance();
        if (!tokenizer.getToken().matches(regex)) {
            throw new RuntimeException("Expected type declaration");
        } else {
            type = tokenizer.getToken();
        }

        tokenizer.advance();
        if (!tokenizer.getToken().matches(regex)) {
            throw new RuntimeException("Expected name declaration");
        } else {
            name = tokenizer.getToken();
        }

        symbolTable.define(name, type, SymbolTable.Kind.valueOf(kind.toUpperCase()));

        tokenizer.advance();
        if (tokenizer.getToken().equals(",")) {
            do {
                tokenizer.advance();
                name = tokenizer.getToken();
                symbolTable.define(name, type, SymbolTable.Kind.valueOf(kind.toUpperCase()));
                tokenizer.advance();
            } while (!tokenizer.getToken().equals(";"));
        }
        if (!tokenizer.symbol().equals(";"))
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected token!");

    }

    public void CompileSubroutine() {
        boolean isConstructor = tokenizer.getToken().equals("constructor") ? true : false;
        boolean isMethod = tokenizer.getToken().equals("method") ? true : false;

        // Write function declaration
        vmWriter.getWriter().println("function " + className + ".");

        // Write constructor declaration
        if (isConstructor) {
            vmWriter.writePush("constant", symbolTable.varCount(SymbolTable.Kind.FIELD));
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop("pointer", 0);
        }

        // Write method declaration
        if (isMethod) {
            vmWriter.writePush("argument", 0);
            vmWriter.writePop("pointer", 0);
        }

        // Get the kind | void
        tokenizer.advance();
        String kind = tokenizer.getToken();
        if (!kind.matches(regex)) {
            throw new RuntimeException("Expected kind or void declaration expected");
        }

        // Get name
        tokenizer.advance();
        if (!tokenizer.getToken().matches(regex)) {
            throw new RuntimeException("Expected name declaration expected");
        } else {
            vmWriter.getWriter().write(tokenizer.getToken());
            vmWriter.getWriter().flush();
        }

        // Get (
        tokenizer.advance();
        if (!tokenizer.getToken().equals("(")) {
            throw new RuntimeException("Expected (");
        }

        // Get parameter list
        tokenizer.advance();
        if (!tokenizer.getToken().equals(")")) {
            compileParameterList(); // TODO
        }

        // Get )
        tokenizer.advance();
        if (!tokenizer.getToken().equals(")")) {
            throw new RuntimeException("Expected )");
        }

        // Get {
        tokenizer.advance();
        if (!tokenizer.getToken().equals("{")) {
            throw new RuntimeException("Expected {");
        }

        // Get var dec
        tokenizer.advance();
        while (tokenizer.getToken().equals("var")) {
            compileVarDec(); // TODO
            tokenizer.advance();
        }

        // Get Body
        compileStatements(); // TODO

        // Get }
        tokenizer.advance();
        if (!tokenizer.getToken().equals("}")) {
            throw new RuntimeException("Expected }");
        }
    }

    public void compileParameterList() {

    }

    public void compileVarDec() {

    }

    public void compileStatements() {

    }

    public void compileLet() {

    }

    public void compileDo() {

    }

    public void compileReturn() {
        tokenizer.advance();
        if (tokenizer.getToken().equals(";")) {
            tokenizer.putBack();
            vmWriter.writePush("constant", 0);
        } else if (isExpression(tokenizer.getToken()))
            compileExpression();
        else
            throw new RuntimeException("Expected ; or expression");
        
        tokenizer.advance();
        if (!tokenizer.getToken().equals(";"))
            throw new RuntimeException("Expected ;");
        vmWriter.writeReturn();
    }

    public void compileWhile() {

    }

    public void compileIf() {

    }

    public void compileExpression() {

    }

    public void compileTerm() {

    }

    public void compileExpressionList() {

    }

}
