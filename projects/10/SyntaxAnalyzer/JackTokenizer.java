package SyntaxAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.LinkedList;
import java.util.Set;

public class JackTokenizer {
    private final String[] SYMBOLS = { "{", "}", "(", ")", "[", "]", ".", ",", ";", "+", "-", "*", "/", "&", "|", "<",
            ">", "=", "~" };
    private String fileName;
    private String currentToken; // Current token
    private String currentString;
    private String tokenType; // Type of current token
    private Set<String> keywords;
    private Set<String> symbols;
    private Queue<String> tokenStrings;
    private Scanner scanner;

    public JackTokenizer(File file) throws IOException {
        this.fileName = file.getName();
        this.currentToken = "";
        this.tokenStrings = new LinkedList<>();
        initKeywords();
        initSymbols();
        this.scanner = new Scanner(file);
    }

    private void initKeywords() {
        keywords = new HashSet<>();
        keywords.add("class");
        keywords.add("constructor");
        keywords.add("function");
        keywords.add("method");
        keywords.add("field");
        keywords.add("static");
        keywords.add("var");
        keywords.add("int");
        keywords.add("char");
        keywords.add("boolean");
        keywords.add("void");
        keywords.add("true");
        keywords.add("false");
        keywords.add("null");
        keywords.add("this");
        keywords.add("let");
        keywords.add("do");
        keywords.add("if");
        keywords.add("else");
        keywords.add("while");
        keywords.add("return");
    }

    private void initSymbols() {
        symbols = new HashSet<>();
        for (String symbol : SYMBOLS) {
            symbols.add(symbol);
        }
    }

    /*
     * Gets the next token from the input and makes it the current token. This
     * method should only be called if hasMoreTokens() is true. Initially there is
     * no current token.
     */
    public void advance() {
        scanner.useDelimiter("/\\*\\*.*?\\*/|//|\\s+|\\n"); // Skip whitespace (including newlines)
        if (!tokenStrings.isEmpty()) {
            currentToken = tokenStrings.remove();
            return;
        } else {
            if (hasMoreTokens()) {
                currentToken = "";
                String currentLine = scanner.nextLine().trim();

                if (currentLine.equals("")) {
                    return;
                }

                String comment = currentLine.length() >= 3 ? currentLine.substring(0, 3) : "";
                if (comment.equals("/**")) {
                    return;
                }

                char[] lineBuffer = currentLine.toCharArray();

                StringBuffer token = new StringBuffer();
                for (char character : lineBuffer) {
                    if (symbols.contains(character + "")) {
                        token.append(" " + character + " ");
                    } else {
                        token.append(character);
                    }
                }

                String[] tokens = token.toString().replaceAll("\\s+", " ").split(" ");
                StringBuffer sb = new StringBuffer();

                for (int i = 0; i < tokens.length; i++) {
                    if (tokens[i].contains("\"")) {
                        sb.append(tokens[i++].replace("\"", ""));

                        while (tokens[i].contains("\"") == false)
                            sb.append(" " + tokens[i++]);

                        tokenStrings.add(sb.toString());

                    } else
                        tokenStrings.add(tokens[i]);
                }
            }

            currentToken = tokenStrings.remove();
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
            if (keywords.contains(currentToken)) {
                tokenType = currentToken.toUpperCase();
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

    public String getToken() {
        return currentToken;
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
