package SyntaxAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class JackTokenizer {
    private final String[] SYMBOLS = { "{", "}", "(", ")", "[", "]", ".", ",", ";", "+", "-", "*", "/", "&", "|", "<",
            ">", "=", "~" };
    private final String[] KEYWORDS = { "class", "constructor", "function", "method", "field", "static", "var", "int",
            "char", "boolean", "void", "true", "false", "null", "this", "let", "do", "if", "else", "while", "return" };
    private String fileName;
    private String currentToken; // Current token
    private String tokenType; // Type of current token
    private Map<String, String> keywords;
    private Scanner scanner;

    public JackTokenizer(File file) throws IOException {
        this.fileName = file.getName();
        this.currentToken = "";
        initKeywords();

        this.scanner = new Scanner(file);
    }

    private void initKeywords() {
        keywords = new HashMap<>();
        keywords.put("class", "CLASS");
        keywords.put("constructor", "CONSTRUCTOR");
        keywords.put("function", "FUNCTION");
        keywords.put("method", "METHOD");
        keywords.put("field", "FIELD");
        keywords.put("static", "STATIC");
        keywords.put("var", "VAR");
        keywords.put("int", "INT");
        keywords.put("char", "CHAR");
        keywords.put("boolean", "BOOLEAN");
        keywords.put("void", "VOID");
        keywords.put("true", "TRUE");
        keywords.put("false", "FALSE");
        keywords.put("null", "NULL");
        keywords.put("this", "THIS");
        keywords.put("let", "LET");
        keywords.put("do", "DO");
        keywords.put("if", "IF");
        keywords.put("else", "ELSE");
        keywords.put("while", "WHILE");
        keywords.put("return", "RETURN");
    }

    /*
     * Gets the next token from the input and makes it the current token. This
     * method should only be called if hasMoreTokens() is true. Initially there is
     * no current token.
     */
    public void advance() {
        scanner.skip("\\s+|\\n|/\\*\\*.*|//"); // Skip whitespace (including newlines)
        if (hasMoreTokens()) {
            currentToken = scanner.next();
            
            
        
            System.out.println(currentToken);

        }
    }

    /*
     * Returns true if there are more tokens in the input.
     */
    public boolean hasMoreTokens() {
        return scanner.hasNext();
    }

    /*
     * Returns the type of the current token, as a constant.
     */
    public String tokenType() {
        if (!currentToken.isEmpty()) {
            if (keywords.containsKey(currentToken)) {
                tokenType = keywords.get(currentToken);
            } else if (currentToken.matches("\\d+"))
                tokenType = "INT_CONST";
            else if (currentToken.matches("\".*\""))
                tokenType = "STRING_CONST";
            else if (currentToken.matches("\\w+"))
                tokenType = "IDENTIFIER";
            else
                tokenType = "SYMBOL";

        }
        return tokenType;
    }

    /*
     * Returns the keyword which is the current token as a constant. Should be
     * called only when
     * tokenType() is KEYWORD.
     */
    public String keyWord() {
        return "";
    }

    /*
     * Returns the character which is the current token. Should be called only when
     * tokenType() is SYMBOL.
     */
    public char symbol() {
        if (tokenType().equals("SYMBOL"))
            throw new IllegalStateException("Current token is not a symbol");
        return currentToken.charAt(0);
    }

    /*
     * Returns the identifier or integer value of the current token. Should be
     * called only when tokenType() is IDENTIFIER or INT_CONST.
     */
    public String identifier() {
        if (tokenType().equals("IDENTIFIER"))
            return currentToken;
        else if (tokenType().equals("INT_CONST"))
            return currentToken;
        else
            throw new IllegalStateException("Current token is not an identifier or integer");
    }

    /*
     * Returns the integer value of the current token. Should be called only when
     * tokenType() is INT_CONST.
     */
    public int intVal() {
        if (tokenType().equals("INT_CONST"))
            return Integer.parseInt(currentToken);
        else
            throw new IllegalStateException("Current token is not an integer");
    }

    /*
     * Returns the string value of the current token, without the double quotes.
     * Should be called only when tokenType() is STRING_CONST.
     */
    public String stringVal() {
        if (tokenType().equals("STRING_CONST"))
            return currentToken.substring(1, currentToken.length() - 1);
        else
            throw new IllegalStateException("Current token is not a string");
    }

}
