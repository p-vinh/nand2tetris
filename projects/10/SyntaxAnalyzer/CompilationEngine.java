package SyntaxAnalyzer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
    private int expressionNum = 0;
    private Document document;
    private Element root;
    private Element curRoot;
    private JackTokenizer tokenizer;

    public CompilationEngine(File input) {
        this.input = input;
        this.document = getDocument();
        this.root = this.document.createElement("class");
        this.curRoot = this.root;
        this.output = new File(input.getName().replace(".jack", "T.xml"));
        try {
            this.tokenizer = new JackTokenizer(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CompilationEngine(File input, File output) {
        this.input = input;
        this.output = output;
        try {
            this.tokenizer = new JackTokenizer(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isExpression(String curToken) {
        curToken = tokenizer.getToken();
        return curToken.matches("^[0-9]+") ||
                curToken.startsWith("\"") && curToken.endsWith("\"") ||
                curToken.matches("^(true|false|null|this)$") ||
                curToken.matches("^[a-zA-Z_][a-zA-Z0-9_]*$") ||
                curToken.matches("^[(~-]$");
    }

    public void CompileClass() {
        tokenizer.advance();
        validateToken("class", tokenizer.keyWord());
        createAndAppendElement(tokenizer.tokenType(), tokenizer.identifier());

        tokenizer.advance();
        createAndAppendElement(tokenizer.tokenType(), tokenizer.identifier());

        tokenizer.advance();
        validateToken("{", tokenizer.symbol());
        createAndAppendElement(tokenizer.tokenType(), "{");

        tokenizer.advance();
        while (!tokenizer.getToken().equals("}")) {
            switch (tokenizer.getToken()) {
                case "static":
                case "field":
                    CompileClassVarDec();
                    break;
                case "constructor":
                case "function":
                case "method":
                    CompileSubroutine();
                    break;
                default:
                    throw new RuntimeException("Unknown class declaration!");
            }
            tokenizer.advance();
        }

        validateToken("}", tokenizer.symbol());
        appendEndElement("}");
        docSave(this.output, this.root);
    }

    private void validateToken(String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new RuntimeException("Syntax error: expected " + expected + ", got " + actual);
        }
    }

    private void appendEndElement(String symbol) {
        Element endElement = this.document.createElement("symbol");
        endElement.setTextContent(" " + symbol + " ");
        this.root.appendChild(endElement);
    }

    public void CompileClassVarDec() {
        Element varElement = this.document.createElement("classVarDec");
        this.root.appendChild(varElement);
        this.curRoot = varElement;

        validateToken("static|field", tokenizer.getToken());
        createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());

        tokenizer.advance();
        createAndAppendElement(tokenizer.tokenType(), tokenizer.identifier());

        tokenizer.advance();

        if (tokenizer.getToken().equals(",")) {
            while (!tokenizer.getToken().equals(";")) {
                createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
                tokenizer.advance();
                createAndAppendElement(tokenizer.tokenType(), tokenizer.identifier());
                tokenizer.advance();
            }
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        } else {
            validateToken(";", tokenizer.getToken());
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        }

        this.curRoot = (Element) this.curRoot.getParentNode();
    }

    public void CompileSubroutine() {
        Element subElement = this.document.createElement("subroutineDec");
        this.root.appendChild(subElement);
        this.curRoot = subElement;

        // constructor | function | method
        createAndAppendElement(tokenizer.tokenType(), tokenizer.identifier());

        // void | type
        tokenizer.advance();
        if (tokenizer.getToken().equals("void")) {
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        } else if (tokenizer.getToken().equals("keyword")) {
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }

        // subroutineName
        tokenizer.advance();
        if (tokenizer.getToken().matches("^[a-zA-Z_]{1}[a-zA-Z0-9_]*")) {
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }

        // (
        tokenizer.advance();
        if (!tokenizer.getToken().equals("(")) {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ",( expected.");
        }
        createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());

        // parameterList | empty
        tokenizer.advance();
        if (tokenizer.getToken().matches("^[a-zA-Z_]{1}[a-zA-Z0-9_]*")) {
            compileParameterList();
        } else if (tokenizer.getToken().equals(")")) {
            createAndAppendElement("parameterList", "");
            createAndAppendElement(tokenizer.tokenType(), ")");
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ",) expected.");
        }

        // SubroutineBody
        tokenizer.advance();
        if (!tokenizer.getToken().equals("{")) {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ",{ expected.");
        } else {
            Element bodyElement = this.document.createElement("subroutineBody");
            this.curRoot.appendChild(bodyElement);
            this.curRoot = bodyElement;

            createAndAppendElement(tokenizer.tokenType(), "{");

            tokenizer.advance();
            if (tokenizer.getToken().equals("var")) {
                while (tokenizer.getToken().equals("var")) {
                    compileVarDec();
                    curRoot = (Element) curRoot.getParentNode();
                    tokenizer.advance();
                }
            }

            while (!tokenizer.getToken().equals("}"))
                compileStatements();

            createAndAppendElement(tokenizer.tokenType(), "}");
        }
    }

    public void compileParameterList() {
        Element parameterListElement = this.document.createElement("parameterList");
        curRoot.appendChild(parameterListElement);
        curRoot = parameterListElement;

        createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        tokenizer.advance();
        if (tokenizer.getToken().matches("^[a-zA-Z_]{1}[a-zA-Z0-9_]*")) {
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }

        // Read other token,until ")"
        tokenizer.advance();
        while (!tokenizer.getToken().equals(")")) {
            // The next expected token is ",".
            if (tokenizer.getToken().equals(",")) {
                createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
            } else {
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", \",\" expected");
            }
            tokenizer.advance();

            // The next expected token is identifier.
            if (tokenizer.getToken().matches("^[a-zA-Z_]{1}[a-zA-Z0-9_]*")) {
                createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
            } else {
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
            }
            tokenizer.advance();

            // The next expected token is identifier.
            if (tokenizer.getToken().matches("^[a-zA-Z_]{1}[a-zA-Z0-9_]*")) {
                createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
            } else {
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
            }
            tokenizer.advance();
        }

        // Add the ")" in the front of the queue.
        tokenizer.putBack();
        this.curRoot = (Element) this.curRoot.getParentNode();
    }

    public void compileVarDec() {
        Element varDec = this.document.createElement("varDec");
        this.curRoot.appendChild(varDec);
        this.curRoot = varDec;

        // Get "var" token firstly.
        createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());

        // The next expected token is type.
        tokenizer.advance();
        if (tokenizer.getToken().matches("^[a-zA-Z_]{1}[a-zA-Z0-9_]*")) {
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }
        tokenizer.advance();

        // The next expected token is identifier.
        if (tokenizer.getToken().matches("^[a-zA-Z_]{1}[a-zA-Z0-9_]*")) {
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }

        // Read other token until ";"
        tokenizer.advance();
        while (!tokenizer.getToken().equals(";")) {
            // The next expected token is ",".
            if (tokenizer.getToken().equals(",")) {
                createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
            } else {
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected. \",\" expeced.");
            }
            tokenizer.advance();

            // The next expected token is identifier.
            if (tokenizer.getToken().matches("^[a-zA-Z_]{1}[a-zA-Z0-9_]*")) {
                createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
            } else {
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
            }
            tokenizer.advance();
        }

        // Append ";"
        createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
    }

    public void compileStatements() {
        Element statementsElement = this.document.createElement("statements");
        curRoot.appendChild(statementsElement);
        curRoot = statementsElement;

        while (!tokenizer.getToken().equals("}")) {
            switch (tokenizer.getToken()) {
                case "let":
                    compileLet();
                    break;
                case "if":
                    compileIf();
                    break;
                case "while":
                    compileWhile();
                    break;
                case "do":
                    compileDo();
                    break;
                case "return":
                    compileReturn();
                    break;
                case "var":
                    compileVarDec();
                    break;
                default:
                    throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ",statement expected.");
            }
            tokenizer.advance();
        }

    }

    public void compileDo() {
        Element doStatement = this.document.createElement("doStatement");
        this.curRoot.appendChild(doStatement);
        this.curRoot = doStatement;

        // Append the current token firstly. In fact this token is "do".
        createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());

        // The next expected token is identifier.
        tokenizer.advance();
        if (tokenizer.getToken().matches("^[a-zA-Z_]{1}[a-zA-Z0-9_]*")) {
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }
        tokenizer.advance();

        // Maybe the next expected token is "." or "(".
        if (tokenizer.getToken().equals(".")) {
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());

            tokenizer.advance();
            if (tokenizer.getToken().matches("^[a-zA-Z_]{1}[a-zA-Z0-9_]*")) {
                createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
            } else {
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
            }
        } else if (tokenizer.getToken().equals("(")) {
            tokenizer.putBack();
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }
        tokenizer.advance();

        // The next expected token is "(".
        if (tokenizer.getToken().equals("(")) {
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }

        if (tokenizer.getTokenStrings().peek().equals(")")) {
            createAndAppendElement("expressionList", "");
        } else if (this.isExpression(tokenizer.getTokenStrings().peek())) {
            CompileExpressionList();
        }

        tokenizer.advance();
        if (tokenizer.getToken().equals(")")) {
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        } else {
            throw new RuntimeException(
                    "Syntax error: " + tokenizer.getToken() + ", unexpected, ) expected.");
        }
        tokenizer.advance();

        // The next expected token is ";".
        if (tokenizer.getToken().equals(";")) {
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        } else {
            throw new RuntimeException(
                    "Syntax error: " + tokenizer.getToken() + ", unexpected, ) expected.");
        }
        this.curRoot = (Element) this.curRoot.getParentNode();
    }

    public void compileLet() {
        Element letStatement = this.document.createElement("letStatement");
        this.curRoot.appendChild(letStatement);
        this.curRoot = letStatement;

        // Append the current "let" token firstly.
        createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());

        // The next expected token is identifier.
        tokenizer.advance();
        if (tokenizer.getToken().matches("^[a-zA-Z_]{1}[a-zA-Z0-9_]*")) {
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }
        tokenizer.advance();

        // The next expected token is "[" or "=".
        boolean startBrackets = false;
        if (tokenizer.getToken().equals("[")) {
            startBrackets = true;
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        } else if (tokenizer.getToken().equals("=")) {
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }
        tokenizer.advance();

        // The next expected token is term.
        String curToken = tokenizer.getToken();
        if (this.isExpression(curToken)) {
            CompileExpression();
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }

        // The right brackets
        if (startBrackets) {
            // next expected token is right brackets
            tokenizer.advance();
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
            startBrackets = false;

            // next token "="?
            tokenizer.advance();
            if (tokenizer.getToken().equals("=")) {
                createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
            } else {
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
            }

            // next expected tokens is expression
            tokenizer.advance();
            if (this.isExpression(tokenizer.getToken())) {
                CompileExpression();
            } else {
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
            }

            // next expected token is ";"
            if (!tokenizer.getTokenStrings().peek().equals(";")) {
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
            }
        }

        // The end token is ";"
        tokenizer.advance();
        if (tokenizer.getToken().equals(";")) {
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }

        this.curRoot = (Element) this.curRoot.getParentNode();
    }

    public void compileWhile() {
        Element term = this.document.createElement("whileStatement");
        this.curRoot.appendChild(term);
        this.curRoot = term;

        createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());

        tokenizer.advance();
        if (tokenizer.getToken().equals("(")) {
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }

        tokenizer.advance();
        if (this.isExpression(tokenizer.getToken())) {
            CompileExpression();
        } else if (tokenizer.getToken().equals(")")) {
            tokenizer.putBack();
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }

        tokenizer.advance();
        if (tokenizer.getToken().equals(")")) {
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }

        tokenizer.advance();
        if (tokenizer.getToken().equals("{")) {
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }

        tokenizer.advance();
        if (this.isStatement(tokenizer.getToken()))
            compileStatements();
        else if (tokenizer.getToken().equals("}"))
            tokenizer.putBack();
        else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }

        if (tokenizer.getToken().equals("}"))
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        else
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");

        this.curRoot = (Element) this.curRoot.getParentNode();
    }

    public void compileReturn() {
        Element term = this.document.createElement("returnStatement");
        this.curRoot.appendChild(term);
        this.curRoot = term;

        // Append the current token firstly.
        createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());

        tokenizer.advance();
        if (tokenizer.getToken().equals(";")) {
            tokenizer.putBack();
        } else if (this.isExpression(tokenizer.getToken())) {
            CompileExpression();
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }

        // next token ";"
        tokenizer.advance();
        if (tokenizer.getToken().equals(";")) {
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }

        this.curRoot = (Element) this.curRoot.getParentNode();
    }

    public void compileIf() {
        Element term = this.document.createElement("ifStatement");
        this.curRoot.appendChild(term);
        this.curRoot = term;

        // Append the current token firstly.
        createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());

        // next token "("
        tokenizer.advance();
        if (tokenizer.getToken().equals("(")) {
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }

        // next token expression
        tokenizer.advance();
        if (this.isExpression(tokenizer.getToken())) {
            CompileExpression();
        } else if (tokenizer.getToken().equals(")")) {
            tokenizer.putBack();
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }

        // next token ")"
        tokenizer.advance();
        if (tokenizer.getToken().equals(")")) {
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }

        // next token "{"
        tokenizer.advance();
        if (tokenizer.getToken().equals("{")) {
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }

        // next tokens statements
        tokenizer.advance();
        if (this.isStatement(tokenizer.getToken())) {
            compileStatements();
        } else if (tokenizer.getToken().equals("}")) {
            tokenizer.putBack();
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }

        // next token "}"
        tokenizer.advance();
        if (tokenizer.getToken().equals("}")) {
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
        }

        // next token maybe else or others
        if (tokenizer.getTokenStrings().peek().equals("else")) {
            // Append the token
            tokenizer.advance();
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());

            // next token "{"
            tokenizer.advance();
            if (tokenizer.getToken().equals("{")) {
                createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
            } else {
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
            }

            // next tokens "statements"
            tokenizer.advance();
            if (this.isStatement(tokenizer.getToken())) {
                compileStatements();
            } else if (tokenizer.getToken().equals("}")) {
                tokenizer.putBack();
            } else {
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
            }

            // next token "}"
            tokenizer.advance();
            if (tokenizer.getToken().equals("}")) {
                createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
            } else {
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
            }
        }

        this.curRoot = (Element) this.curRoot.getParentNode();
    }

    public void CompileExpression() {
        Element expression = this.document.createElement("expression");
        this.curRoot.appendChild(expression);
        this.curRoot = expression;

        // Compile current token
        CompileTerm();

        // The next expected token is operator.
        // If the next token is "]" or ";" or ")" or ",", end this compile.
        tokenizer.advance();
        Boolean endFlag = tokenizer.getToken().equals("]") ||
                tokenizer.getToken().equals(")") ||
                tokenizer.getToken().equals(";") ||
                tokenizer.getToken().equals(",");
        while (!endFlag) {
            // Operator
            if (tokenizer.getToken().matches("\\+|-|\\*|/|\\&|\\||<|=|>")) {
                createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
            } else {
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ", unexpected");
            }
            // term
            tokenizer.advance();
            String curToken = tokenizer.getToken();
            if (this.isExpression(curToken)) {
                CompileTerm();
            }

            // Judge if end.
            tokenizer.advance();
            endFlag = tokenizer.getToken().equals("]") ||
                    tokenizer.getToken().equals(")") ||
                    tokenizer.getToken().equals(";") ||
                    tokenizer.getToken().equals(",");
        }
        tokenizer.putBack();
        this.curRoot = (Element) this.curRoot.getParentNode();
    }

    public void CompileTerm() {
        Element term = this.document.createElement("term");
        this.curRoot.appendChild(term);
        this.curRoot = term;

        // integer constant
        if (tokenizer.tokenType().equals("integerConstant")) {
            int num = Integer.parseInt(tokenizer.getToken());
            if (num >= 0 && num < 32767) {
                createAndAppendElement("integerConstant", num + "");
            } else {
                throw new RuntimeException("Integer over than max Integer: " + tokenizer.getToken());
            }
        } else if (tokenizer.tokenType().equals("stringConstant")) {
            createAndAppendElement("stringConstant",
                    tokenizer.getToken().substring(1, tokenizer.getToken().length() - 1));
        } else if (tokenizer.getToken().equals("true") ||
                tokenizer.getToken().equals("false") ||
                tokenizer.getToken().equals("null") ||
                tokenizer.getToken().equals("this")) {
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
        } else if (tokenizer.tokenType().equals("identifier")) {
            String head = tokenizer.getTokenStrings().peek();
            // mean the structure is varName [expression]
            if (head.equals("[")) {
                // append varName
                createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
                tokenizer.advance();
                // append "["
                createAndAppendElement(tokenizer.tokenType(), "[");
                tokenizer.advance();
                if (this.isExpression(tokenizer.getToken())) {
                    CompileExpression();
                } else {
                    throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected.");
                }
                // append "]"
                tokenizer.advance();
                if (tokenizer.getToken().equals("]")) {
                    createAndAppendElement(tokenizer.tokenType(), "]");
                } else {
                    throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected.");
                }
                // means the structure is subroutineName ( expressionList )
            } else if (head.equals("(")) {
                // append varName
                createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
                tokenizer.advance();
                // append "("
                createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
                if (tokenizer.getTokenStrings().peek().matches("^[a-zA-Z_]{1}[a-zA-Z0-9_]*")) {
                    CompileExpressionList();
                } else if (tokenizer.getTokenStrings().peek().equals(")")) {

                } else {
                    throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected.");
                }

                // get next token
                tokenizer.advance();
                if (tokenizer.getToken().equals(")")) {
                    createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
                } else {
                    throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected.");
                }

                // means the structure is className|varName . subroutineName ( expressionList )
            } else if (head.equals(".")) {
                // append className of varName
                createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
                tokenizer.advance();
                createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());

                // next token
                tokenizer.advance();
                if (tokenizer.getToken().matches("^[a-zA-Z_]{1}[a-zA-Z0-9_]*")) {
                    // append the subroutineName first
                    createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
                    // next token
                    tokenizer.advance();
                    if (tokenizer.getToken().equals("(")) {
                        // append it
                        createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());

                        // Judge is next token is belong to expressionList.
                        // The beginning of the expressionList is a expression and the beginning of the
                        // expression is a term
                        if (this.isExpression(tokenizer.getTokenStrings().peek())) {
                            CompileExpressionList();
                        } else if (tokenizer.getTokenStrings().peek().equals(")")) {
                            createAndAppendElement("expressionList", "  ");
                        } else {
                            throw new RuntimeException(
                                    "Syntax error: " + tokenizer.getToken() + " unexpected.");
                        }
                        // CompileExpressionList();

                        // next token
                        tokenizer.advance();
                        if (tokenizer.getToken().equals(")")) {
                            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
                        } else {
                            throw new RuntimeException(
                                    "Syntax error: " + tokenizer.getToken() + " unexpected.");
                        }
                    } else {
                        throw new RuntimeException(
                                "Syntax error: " + tokenizer.getToken() + " unexpected.");
                    }
                } else {
                    throw new RuntimeException(
                            "subroutine call error: " + tokenizer.getToken() + " unexpected.");
                }
                // means the next token is a symbol, we just append the current identifier
            } else if (head.matches("\\+|-|\\*|/|\\&|\\||<|=|>")) {
                createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
            } else if (head.equals(")") || head.equals("]") || head.equals(";") || head.equals(",")) {
                createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
            } else {
                throw new RuntimeException(
                        "subroutine call error: " + tokenizer.getToken() + " unexpected.");
            }
        } else if (tokenizer.getToken().equals("(")) {
            // append "(" firstly
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
            // next token
            tokenizer.advance();
            String curToken = tokenizer.getToken();
            if (this.isExpression(curToken)) {
                CompileExpression();
            } else {
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected.");
            }
            // next token
            tokenizer.advance();
            if (tokenizer.getToken().equals(")")) {
                createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
            } else {
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected.");
            }
            // Means this structure is "unaryOp term"
        } else if (tokenizer.getToken().matches("\\-|~")) {
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
            // The next expected token is term
            tokenizer.advance();
            if (this.isExpression(tokenizer.getToken())) {
                CompileTerm();
            } else {
                throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected.");
            }
        } else {
            throw new RuntimeException(
                    "Term token compile error: " + tokenizer.getToken() + " unexpected.");
        }

        this.curRoot = (Element) this.curRoot.getParentNode();
    }

    public void CompileExpressionList() {
        Element term = this.document.createElement("expressionList");
        this.curRoot.appendChild(term);
        this.curRoot = term;

        // next token
        tokenizer.advance();
        if (this.isExpression(tokenizer.getToken())) {
            CompileExpression();
        }

        // next token maybe "," or ")"
        if (tokenizer.getToken().equals(",")) {
            String curToken = tokenizer.getTokenStrings().peek();
            while (!curToken.equals(")")) {
                if (curToken.equals(",")) {
                    tokenizer.advance();
                    createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
                } else {
                    throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected.");
                }

                tokenizer.advance();
                if (this.isExpression(tokenizer.getToken())) {
                    CompileExpression();
                } else {
                    throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected.");
                }
                curToken = tokenizer.getTokenStrings().peek();
            }
        } else if (tokenizer.getTokenStrings().peek().equals(")")) {

        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + " unexpected.");
        }
        this.curRoot = (Element) this.curRoot.getParentNode();
    }

    private void createAndAppendElement(Object object, String textContent) {
        Element element = null;
        if (object instanceof KeyWord) {
            element = this.document.createElement(((KeyWord) object).toString().toLowerCase());
        } else if (object instanceof TokenType) {
            element = this.document.createElement(object.toString().toLowerCase());
        } else {
            element = this.document.createElement(object.toString());
        }
        element.setTextContent(" " + textContent + " ");
        this.curRoot.appendChild(element);
        docSave(this.output, this.root);
    }

    private Document getDocument() {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document document = documentBuilder.newDocument();
        return document;
    }

    private void docSave(File xmlFile, Element root) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource domSource = new DOMSource(root);
            StreamResult streamResult = new StreamResult(new FileOutputStream(xmlFile));
            transformer.transform(domSource, streamResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isStatement(String curToken) {
        if (curToken.equals("let") ||
                curToken.equals("if") ||
                curToken.equals("else") ||
                curToken.equals("while") ||
                curToken.equals("do") ||
                curToken.equals("return")) {
            return true;
        } else {
            return false;
        }
    }
}
