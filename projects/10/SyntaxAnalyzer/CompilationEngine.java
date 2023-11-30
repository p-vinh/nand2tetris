package SyntaxAnalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

public class CompilationEngine {
    private File output;
    private File input;
    private Document document;

    private Element root;
    private Element curRoot;
    private JackTokenizer tokenizer;

    private enum TokenType {
        KEYWORD, SYMBOL, IDENTIFIER, INT_CONST, STRING_CONST
    }

    private enum KeyWord {
        CLASS, METHOD, FUNCTION, CONSTRUCTOR, INT, BOOLEAN, CHAR, VOID, VAR, STATIC, FIELD, LET, DO, IF, ELSE, WHILE,
        RETURN, TRUE, FALSE, NULL, THIS
    }

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

    public void CompileClass() {
        tokenizer.advance();
        String curToken = tokenizer.keyWord();
        if (!curToken.equals("class")) {
            throw new RuntimeException("Syntax error on token \"class\"");
        }
        createAndAppendElement(tokenizer.tokenType(), tokenizer.identifier());

        tokenizer.advance();
        createAndAppendElement(tokenizer.tokenType(), tokenizer.identifier());

        tokenizer.advance();
        if (!tokenizer.symbol().equals("{")) {
            throw new RuntimeException("Syntax error on class declare, { expected at the end");
        }
        createAndAppendElement(tokenizer.tokenType(), "{");

        tokenizer.advance();
        while (!tokenizer.getToken().equals("}")) {
            if (tokenizer.getToken().equals("static") ||
                    tokenizer.getToken().equals("field")) {
                CompileClassVarDec();
            } else if (tokenizer.getToken().equals("constructor") ||
                    tokenizer.getToken().equals("function") ||
                    tokenizer.getToken().equals("method")) {
                CompileSubroutine();
            } else {
                throw new RuntimeException("Unknown class declare!");
            }
            tokenizer.advance();
        }

        if (!tokenizer.symbol().equals("}")) {
            throw new RuntimeException("Syntax error on the file end, } expected at the end");
        }
        Element endElement = this.document.createElement("symbol");
        endElement.setTextContent(" } ");
        this.root.appendChild(endElement);
        docSave(this.output, this.root);
    }

    public void CompileClassVarDec() {

    }

    public void CompileSubroutine() {
        Element subElement = this.document.createElement("subroutineDec");
        this.root.appendChild(subElement);
        this.curRoot = subElement;

        // constructor | function | method
        createAndAppendElement(tokenizer.tokenType(), tokenizer.identifier());

        // void | type
        tokenizer.advance();
        if (!tokenizer.getToken().matches("^[a-zA-Z_]{1}[a-zA-Z0-9_]*")) {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ",void or other data type expected.");
        }
        createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());

        // subroutineName
        tokenizer.advance();
        if (!tokenizer.getToken().matches("^[a-zA-Z_]{1}[a-zA-Z0-9_]*")) {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ",identifier expected.");
        }
        createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());

        // (
        tokenizer.advance();
        if (!tokenizer.getToken().equals("(")) {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ",( expected.");
        }
        createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());

        // parameterList | empty
        tokenizer.advance();
        if (tokenizer.getToken().matches("^[a-zA-Z_]{1}[a-zA-Z0-9_]*")) {
            compileParameterList(); // TODO
        } else if (tokenizer.getToken().equals(")")) {
            createAndAppendElement("parameterList", "");
            createAndAppendElement(tokenizer.tokenType(), ")");
        } else {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ",) expected.");
        }

        // subroutineBody
        tokenizer.advance();
        if (!tokenizer.getToken().equals("{")) {
            throw new RuntimeException("Syntax error: " + tokenizer.getToken() + ",{ expected.");
        } else {
            createAndAppendElement(tokenizer.tokenType(), tokenizer.getToken());
            
        }
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
}
