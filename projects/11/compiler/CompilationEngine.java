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
            compileParameterList();
        }

        // Get )
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
            compileVarDec();
            tokenizer.advance();
        }

        // Get Body
        compileStatements();

        // Get }
        tokenizer.advance();
        if (!tokenizer.getToken().equals("}")) {
            throw new RuntimeException("Expected }");
        }
    }

    public void compileParameterList() {
        String name = "";
        String type = "";

        type = tokenizer.getToken();

        tokenizer.advance();
        if (!tokenizer.getToken().matches(regex)) {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected token!");
        } else {
            name = tokenizer.getToken();
            symbolTable.define(name, type, SymbolTable.Kind.ARG);
        }

        tokenizer.advance();
        while (!tokenizer.getToken().equals(")")) {
            if (!tokenizer.getToken().equals(",")) {
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected token!");
            }

            // Get type
            tokenizer.advance();
            if (!tokenizer.getToken().matches(regex))
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected token!");
            else
                type = tokenizer.getToken();

            // Get identifier
            tokenizer.advance();
            if (!tokenizer.getToken().matches(regex))
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected token!");
            else {
                name = tokenizer.getToken();
                symbolTable.define(name, type, SymbolTable.Kind.ARG);
            }

            tokenizer.advance();
        }

        tokenizer.putBack();
    }

    public void compileVarDec() {
        String name = "";
        String type = "";

        // Get var
        if (!tokenizer.getToken().equals("var")) {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected token!");
        }

        // Get type
        tokenizer.advance();
        if (!tokenizer.getToken().matches(regex)) {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected token!");
        } else {
            type = tokenizer.getToken();
        }

        // Get identifier
        tokenizer.advance();
        if (!tokenizer.getToken().matches(regex)) {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected token!");
        } else {
            name = tokenizer.getToken();
            symbolTable.define(name, type, SymbolTable.Kind.VAR);
        }

        // Get ,
        tokenizer.advance();
        while (!tokenizer.getToken().equals(";")) {
            if (!tokenizer.getToken().equals(",")) {
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected token!");
            }

            // Get identifier
            tokenizer.advance();
            if (!tokenizer.getToken().matches(regex)) {
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected token!");
            } else {
                name = tokenizer.getToken();
                symbolTable.define(name, type, SymbolTable.Kind.VAR);
            }

            tokenizer.advance();
        }
    }

    public void compileStatements() {
        while (!tokenizer.getToken().equals("}")) {
            if (tokenizer.getToken().equals("let")) {
                compileLet();
            } else if (tokenizer.getToken().equals("do")) {
                compileDo();
            } else if (tokenizer.getToken().equals("return")) {
                compileReturn();
            } else if (tokenizer.getToken().equals("while")) {
                compileWhile();
            } else if (tokenizer.getToken().equals("if")) {
                compileIf();
            } else {
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected token!");
            }
        }

        // Put back }
        tokenizer.putBack();
    }

    public String transKind(SymbolTable.Kind kind) {
        switch (kind) {
            case ARG:
                return "argument";
            case VAR:
                return "local";
            case STATIC:
                return "static";
            case FIELD:
                return "this";
            default:
                return "none";
        }
    }

    public void compileLet() {
        String name = "";
        boolean isArray = false;

        // Get identifier
        tokenizer.advance();
        if (!tokenizer.getToken().matches(regex)) {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected token!");
        } else {
            name = tokenizer.getToken();
        }

        // Get = or [
        boolean startBracket = false;
        tokenizer.advance();
        if (!tokenizer.getToken().equals("=") && !tokenizer.getToken().equals("[")) {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected token!");
        } else if (tokenizer.getToken().equals("[")) {
            isArray = true;
            startBracket = true;
        }

        // Get expression
        tokenizer.advance();
        if (!isExpression(tokenizer.getToken())) {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected token!");
        } else {
            compileExpression(); // TODO
        }

        if (isArray) {
            String segmentName = transKind(symbolTable.kindOf(name));
            vmWriter.writePush(segmentName, symbolTable.indexOf(name));
            vmWriter.writeArithmetic(VMWriter.Command.ADD);
        }

        if (startBracket) {
            // Get ]
            tokenizer.advance();
            if (!tokenizer.getToken().equals("]")) {
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected token!");
            }

            tokenizer.advance();
            if (!tokenizer.getToken().equals("=")) {
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected token!");
            }

            tokenizer.advance();
            if (!isExpression(tokenizer.getToken())) {
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected token!");
            } else {
                compileExpression(); // TODO
            }

            // Get ;
            tokenizer.advance();
            if (!tokenizer.getToken().equals(";")) {
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected token!");
            }

            vmWriter.writePop("temp", 0);
        }

        // Get ;
        tokenizer.advance();
        if (!tokenizer.getToken().equals(";")) {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected token!");
        }

        String segmentName = transKind(symbolTable.kindOf(name));

        if (isArray) {
            vmWriter.writePop("pointer", 1);
            vmWriter.writePush("temp", 0);
            vmWriter.writePop("that", 0);
        } else {
            vmWriter.writePop(segmentName, symbolTable.indexOf(name));
        }
    }

    public void compileDo() {
        String callName = null;
        String objectName = null;

        tokenizer.advance();
        if (tokenizer.getToken().matches(regex)) {
            callName = tokenizer.getToken();
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }
        tokenizer.advance();

        boolean isClassCall = true;
        String firstChar = callName.charAt(0) + "";
        if (!firstChar.matches("[A-Z]") && tokenizer.getToken().equals(".")) {
            objectName = callName;
            callName = symbolTable.typeOf(callName);
            isClassCall = false;
            expressionNum++;
        } else if (!firstChar.matches("[A-Z]") && !tokenizer.getToken().equals(".")) {
            vmWriter.writePush("pointer", 0);
            expressionNum++;
            callName = className + "." + callName;

        }

        if (tokenizer.getToken().equals(".")) {
            callName += tokenizer.getToken();

            tokenizer.advance();
            if (tokenizer.getToken().matches(regex))
                callName += tokenizer.getToken();
            else
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");

        } else if (tokenizer.getToken().equals("("))
            tokenizer.putBack();
        else
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");

        tokenizer.advance();

        if (!tokenizer.getToken().equals("("))
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");

        if (!isClassCall) {
            String segmenName = transKind(symbolTable.kindOf(objectName));
            vmWriter.writePush(segmenName, symbolTable.indexOf(objectName));
        }

        if (isExpression(tokenizer.getToken()))
            compileExpressionList();

        tokenizer.advance();
        if (!tokenizer.getToken().equals(")"))
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected, ) expected.");

        tokenizer.advance();
        if (!tokenizer.getToken().equals(";"))
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected, ) expected.");

        vmWriter.writeCall(callName, expressionNum);
        expressionNum = 0; // clear this variable.
        vmWriter.writePop("temp", 0);
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
        int originIndex = this.index;
        this.vmWriter.writeLabel("WHILE_EXP" + index);

        tokenizer.advance();
        if (!tokenizer.getToken().equals("(")) {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }

        tokenizer.advance();
        if (isExpression(tokenizer.getToken()))
            compileExpression();
        else if (tokenizer.getToken().equals(")"))
            tokenizer.putBack();
        else
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");

        tokenizer.advance();
        if (!tokenizer.getToken().equals(")")) {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }

        vmWriter.writeArithmetic(VMWriter.Command.NOT);
        vmWriter.writeIf("WHILE_END" + originIndex);

        tokenizer.advance();
        if (!tokenizer.getToken().equals("{")) {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }

        tokenizer.advance();
        if (tokenizer.getToken().equals("let") || tokenizer.getToken().equals("do") ||
                tokenizer.getToken().equals("if") || tokenizer.getToken().equals("while") ||
                tokenizer.getToken().equals("return") || tokenizer.getToken().equals("else"))
            compileStatements();
        else if (tokenizer.getToken().equals("}"))
            tokenizer.putBack();
        else
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");

        tokenizer.advance();
        if (!tokenizer.getToken().equals("}")) {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }
        this.vmWriter.writeGoto("WHILE_EXP" + originIndex);
        this.vmWriter.writeLabel("WHILE_END" + originIndex);
    }

    public void compileIf() {
        compileTerm();

        tokenizer.advance();
        Boolean endFlag = tokenizer.getToken().equals("]") ||
                tokenizer.getToken().equals(")") ||
                tokenizer.getToken().equals(";") ||
                tokenizer.getToken().equals(",");
        while (!endFlag) {
            String op = null;
            if (tokenizer.getToken().matches("\\+|-|\\*|/|\\&|\\||<|=|>"))
                op = tokenizer.getToken();
            else
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
            tokenizer.advance();
            String curToken = tokenizer.getToken();
            if (isExpression(curToken))
                compileTerm();

            if (op.equals("*"))
                this.vmWriter.writeCall("Math.multiply", 2);
            else if (op.equals("/"))
                this.vmWriter.writeCall("Math.divide", 2);
            else
                this.vmWriter.writeArithmetic(VMWriter.Command.valueOf(op.toUpperCase()));

            tokenizer.advance();
            endFlag = tokenizer.getToken().equals("]") ||
                    tokenizer.getToken().equals(")") ||
                    tokenizer.getToken().equals(";") ||
                    tokenizer.getToken().equals(",");
        }

        tokenizer.putBack();
    }

    public void compileExpression() {
        compileTerm();

        // If there is an operator after the term then compile the term
        tokenizer.advance();
        boolean isOperator = false;

        if (tokenizer.getToken().equals("+") || tokenizer.getToken().equals("-") ||
                tokenizer.getToken().equals("*") || tokenizer.getToken().equals("/") ||
                tokenizer.getToken().equals("&") || tokenizer.getToken().equals("|") ||
                tokenizer.getToken().equals("<") || tokenizer.getToken().equals(">") ||
                tokenizer.getToken().equals("=")) {
            isOperator = true;
        }

        while (isOperator) {
            String operator = "";

            if (tokenizer.getToken().matches(operator))
                operator = tokenizer.getToken();
            else
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected token!");

            tokenizer.advance();
            String curString = tokenizer.getToken();

            if (!isExpression(curString))
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected token!");
            else
                compileTerm();

            if (operator.equals("/"))
                vmWriter.writeCall("Math.divide", 2);
            else if (operator.equals("*"))
                vmWriter.writeCall("Math.multiply", 2);
            else
                vmWriter.writeArithmetic(VMWriter.Command.valueOf(operator.toUpperCase()));

            tokenizer.advance();

            // If token is ], ), ;, or ,
            isOperator = tokenizer.getToken().equals("+") || tokenizer.getToken().equals("-") ||
                    tokenizer.getToken().equals("*") || tokenizer.getToken().equals("/") ||
                    tokenizer.getToken().equals("&") || tokenizer.getToken().equals("|") ||
                    tokenizer.getToken().equals("<") || tokenizer.getToken().equals(">") ||
                    tokenizer.getToken().equals("=");
        }

        tokenizer.putBack();
    }

    public void compileTerm() {
        if (tokenizer.tokenType().equals("integerConstant")) {
            int num = Integer.parseInt(tokenizer.getToken());
            if (num >= 0 && num < 32767)
                this.vmWriter.writePush("constant", num);
            else
                throw new RuntimeException("Integer over than max Integer: " + tokenizer.getToken());

        } else if (tokenizer.tokenType().equals("stringConstant")) { // String test
            String stripString = tokenizer.getToken().substring(1,
                    tokenizer.getToken().length() - 1);
            char[] charString = stripString.toCharArray();

            vmWriter.writePush("constant", charString.length);
            vmWriter.writeCall("String.new", 1);

            for (int i = 0; i < charString.length; i++) {
                vmWriter.writePush("constant", charString[i]);
                vmWriter.writeCall("String.appendChar", 2);
            }

        } else if (tokenizer.getToken().equals("true") ||
                tokenizer.getToken().equals("false") ||
                tokenizer.getToken().equals("null") ||
                tokenizer.getToken().equals("this")) {
            switch (tokenizer.getToken()) {
                case "true":
                    vmWriter.writePush("constant", 0);
                    vmWriter.writeArithmetic(VMWriter.Command.NOT);
                    break;
                case "false":
                    vmWriter.writePush("constant", 0);
                    break;
                case "this":
                    vmWriter.writePush("pointer", 0);
                    break;
                case "null":
                    vmWriter.writePush("constant", 0);
                    break;
                default:
                    break;
            }
        } else if (tokenizer.tokenType().equals("identifier")) {
            // Used to memory the subroutine name.
            String subName = tokenizer.getToken();
            tokenizer.advance();
            String head = tokenizer.getToken();
            tokenizer.putBack();
            if (head.equals("[")) {
                tokenizer.putBack();
                // Get "[".
                tokenizer.advance();
                tokenizer.advance();
                if (isExpression(tokenizer.getToken()))
                    compileExpression();
                else
                    throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected.");

                String segmentName = transKind(symbolTable.kindOf(subName));
                vmWriter.writePush(segmentName, symbolTable.indexOf(subName));
                // append "]"
                tokenizer.advance();
                if (!tokenizer.getToken().equals("]")) {
                    throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected.");
                }

                vmWriter.writeArithmetic(VMWriter.Command.ADD);
                vmWriter.writePop("pointer", 1);
                vmWriter.writePush("that", 0);

            } else if (head.equals("(")) {
                // Get the "(".
                tokenizer.advance();

                if (!tokenizer.getToken().matches(regex))
                    throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected.");
                else if (tokenizer.getToken().matches(regex))
                    compileExpressionList();

                // get next token
                tokenizer.advance();
                if (!tokenizer.getToken().equals(")")) {
                    throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected.");
                }

                vmWriter.writeCall(subName, expressionNum);
                expressionNum = 0; // Clear it.

            } else if (head.equals(".")) {
                boolean isClassCall = true;
                String objectName = null;
                if (subName.matches("[a-z]+")) {
                    isClassCall = false;
                    objectName = subName;
                    subName = symbolTable.typeOf(subName);
                }

                if (!isClassCall) {
                    String segmentName = transKind(symbolTable.kindOf(objectName));
                    vmWriter.writePush(segmentName, symbolTable.indexOf(objectName));
                    expressionNum++;
                }
                tokenizer.advance();
                // Append subroutine name.
                subName += tokenizer.getToken();

                // next token
                tokenizer.advance();
                if (tokenizer.getToken().matches(regex)) {
                    subName += tokenizer.getToken();

                    // next token
                    tokenizer.advance();
                    if (tokenizer.getToken().equals("(")) {
                        if (!isExpression(tokenizer.getToken())) {
                            throw new RuntimeException(
                                    "Syntax error: " + tokenizer.getToken() + " unexpected.");
                        } else if (isExpression(tokenizer.getToken()))
                            compileExpressionList();

                        tokenizer.advance();
                        if (!tokenizer.getToken().equals(")"))
                            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected.");

                    } else
                        throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected.");

                    this.vmWriter.writeCall(subName, this.expressionNum);
                    this.expressionNum = 0; // clear this variable.
                } else {
                    throw new RuntimeException("subroutine call error: " + tokenizer.getToken() + " unexpected.");
                }
                // means the next token is a symbol, we just append the current identifier
            } else if (head.matches("\\+|-|\\*|/|\\&|\\||<|=|>")) {
                if (tokenizer.getToken().matches("[0-9]+")) {
                    this.vmWriter.writePush("constant", Integer.parseInt(tokenizer.getToken()));
                } else {
                    String segmentName = this.transKind(symbolTable.kindOf(tokenizer.getToken()));
                    this.vmWriter.writePush(segmentName, symbolTable.indexOf(tokenizer.getToken()));
                }
            } else if (head.equals(")") || head.equals("]") || head.equals(";") || head.equals(",")) {
                String segmentName = this.transKind(symbolTable.kindOf(tokenizer.getToken()));
                this.vmWriter.writePush(segmentName, symbolTable.indexOf(tokenizer.getToken()));
            } else {
                throw new RuntimeException("Subroutine call error: " + tokenizer.getToken() + " unexpected.");
            }
        } else if (tokenizer.getToken().equals("(")) {
            tokenizer.advance();
            String curToken = tokenizer.getToken();
            if (this.isExpression(curToken))
                compileExpression();
            else
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected.");

            tokenizer.advance();
            if (!tokenizer.getToken().equals(")")) {
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected.");
            }
            // Means this structure is "unaryOp term"
        } else if (tokenizer.getToken().matches("\\-|~")) {
            String op = tokenizer.getToken().equals("-") ? "--" : "~";

            // Means that the next is an expression.
            if (tokenizer.getToken().equals("(")) {
                tokenizer.advance();
                compileExpression();
                // Means that the next is a term.
            } else if (tokenizer.getToken().matches(regex) ||
                    tokenizer.getToken().matches("[0-9]+")) {
                tokenizer.advance();
                compileTerm();
            } else {
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected.");
            }

            this.vmWriter.writeArithmetic(VMWriter.Command.valueOf(op.toUpperCase()));
        } else
            throw new RuntimeException(
                    "Term token compile error: " + tokenizer.getToken() + " unexpected.");

    }

    public void compileExpressionList() {
        tokenizer.advance();
        if (tokenizer.tokenType() == "stringConstant" || isExpression(tokenizer.getToken())) {
            compileExpression();
        }
        expressionNum++;

        // next token maybe "," or ")"
        if (tokenizer.getToken().equals(",")) {
            String curToken = tokenizer.getToken();
            while (!curToken.equals(")")) {
                if (curToken.equals(","))
                    tokenizer.advance();
                else
                    throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected.");

                tokenizer.advance();
                if (isExpression(tokenizer.getToken())) {
                    expressionNum++;
                    compileExpression();
                } else
                    throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected.");
                curToken = tokenizer.getToken();
            }
        } else if (tokenizer.getToken().equals(")")) {
            tokenizer.advance();
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected.");
        }
    }

}
