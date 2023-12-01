

import java.io.File;
import java.io.IOException;
import java.util.Deque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Scanner;
import java.util.LinkedList;
import java.util.Set;

public class JackTokenizer {
    private final String[] SYMBOLS = { "{", "}", "(", ")", "[", "]", ".", ",", ";", "+", "-", "*", "/", "&", "|", "<",
            ">", "=", "~" };
    private final String[] KEYWORDS = { "class", "constructor", "function", "method", "field", "static", "var", "int",
            "char", "boolean", "void", "true", "false", "null", "this", "let", "do", "if", "else", "while", "return" };
    private String currentToken; // Current token
    private Set<String> keywords;
    private Set<String> symbols;
    private Deque<String> tokenStrings;
    private Scanner scanner;

    public JackTokenizer(File file) throws IOException {
        this.currentToken = "";
        this.tokenStrings = new LinkedList<>();
        initKeywords();
        initSymbols();
        this.scanner = new Scanner(file);
    }

    private void initKeywords() {
        keywords = new HashSet<>();
        for (String keyword : KEYWORDS) {
            keywords.add(keyword);
        }
    }

    private void initSymbols() {
        symbols = new HashSet<>();
        for (String symbol : SYMBOLS) {
            symbols.add(symbol);
        }
    }

    public void putBack() {
        tokenStrings.addFirst(currentToken);
    }

    /*
     * Gets the next token from the input and makes it the current token. This
     * method should only be called if hasMoreTokens() is true. Initially there is
     * no current token.
     */
    public void advance() {
        scanner.useDelimiter("/\\*\\*.*?\\*/|\\s+|\\n"); // Skip whitespace (including newlines)
        if (!tokenStrings.isEmpty()) {
            currentToken = tokenStrings.remove();
            return;
        } else {
            if (hasMoreTokens()) {
                String currentLine = scanner.nextLine().replaceAll("//.*", "").trim();
                

                // Process All comments
                String comment = currentLine.length() >= 3 ? currentLine.substring(0, 3) : "";
                if (currentLine.equals("") || currentLine.startsWith("//") || comment.equals("/**") || currentLine.startsWith("*") || comment.equals("*/")) {
                    advance();
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
                StringBuffer sb;
                for (int i = 0; i < tokens.length; i++) {
                    if (tokens[i].equals("")) {
                        continue;
                    } else
                        if (tokens[i].startsWith("\"")) {
                            sb = new StringBuffer();
                            sb.append(tokens[i++].replace("\"", "")); // Skip the first quote

                            // Keep appending until we find the end quote
                            while (!tokens[i].endsWith("\""))
                                sb.append(" " + tokens[i++]);
                            sb.append(" " + tokens[i].replace("\"", "")); // Skip the last quote
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
                // return tokenType = currentToken.toUpperCase();
                return "keyword";
            } else if (symbols.contains(currentToken)) {
                return "symbol";
            } else if (currentToken.matches("^[0-9]+")) {
                return "integerConstant";
            } else if (currentToken.matches("^[a-zA-Z_]{1}[a-zA-Z0-9_]*")) {
                return "identifier";
            } else {
                return "stringConstant";
            }
        }
        return "UNKNOWN";
    }

    /*
     * Returns the keyword which is the current token as a constant. Should be
     * called only when
     * tokenType() is KEYWORD.
     */
    public String keyWord() {
        return currentToken;
    }

    public String getToken() {
        return currentToken;
    }

    /*
     * Returns the character which is the current token. Should be called only when
     * tokenType() is SYMBOL.
     */
    public String symbol() {
        return currentToken;
    }

    /*
     * Returns the identifier or integer value of the current token. Should be
     * called only when tokenType() is IDENTIFIER or INT_CONST.
     */
    public String identifier() {
        return currentToken;
    }

    /*
     * Returns the integer value of the current token. Should be called only when
     * tokenType() is INT_CONST.
     */
    public int intVal() {
        return Integer.parseInt(currentToken);
    }

    /*
     * Returns the string value of the current token, without the double quotes.
     * Should be called only when tokenType() is STRING_CONST.
     */
    public String stringVal() {
        return this.currentToken;
    }

    public Queue<String> getTokenStrings() {
        return tokenStrings;
    }
}
