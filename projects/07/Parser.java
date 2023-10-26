

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Pattern;



public class Parser {
    private Scanner scanner;
    private String currLine;

    public static enum CommandType {
        C_ARITHMETIC, C_PUSH, C_POP, C_LABEL, C_GOTO, C_IF, C_FUNCTION, C_RETURN, C_CALL
    }
    private CommandType currCommandType;

    public Parser(File file) {
        try {
            scanner = new Scanner(file);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /* See if the next line has an input */
    public boolean hasMoreCommands() {
        return scanner.hasNextLine();
    }

    /* Reads the next command from the input and makes it the current command. */
    public void advance() {
        if (hasMoreCommands()) {
            currLine = scanner.nextLine().replaceAll("\\s+|//.*", ""); // Remove comments

            if (currLine.isEmpty()) {
                advance();
            }
        }
    }

    /*
     * Returns the type of the current command: C_ARITHMETIC, C_PUSH, C_POP,
     * C_LABEL, C_GOTO, C_IF, C_FUNCTION, C_RETURN, C_CALL
     * 
     * If the current command is an arithmetic command, returns C_ARITHMETIC.
     */
    public CommandType commandType() {
        Pattern arithLogic = Pattern.compile("add|sub|neg|eq|gt|lt|and|or|not");
        String command = currLine.split(" ")[0];
        
        switch (command) {
            case "push":
                currCommandType = CommandType.C_PUSH;
                break;
            case "pop":
                currCommandType = CommandType.C_POP;
                break;
            case "label":
                currCommandType = CommandType.C_LABEL;
                break;
            case "goto":
                currCommandType = CommandType.C_GOTO;
                break;
            case "if-goto":
                currCommandType = CommandType.C_IF;
                break;
            case "function":
                currCommandType = CommandType.C_FUNCTION;
                break;
            case "call":
                currCommandType = CommandType.C_CALL;
                break;
            case "return":
                currCommandType = CommandType.C_RETURN;
                break;
            default:
                if (arithLogic.matcher(command).matches()) {
                    currCommandType = CommandType.C_ARITHMETIC;
                } else {
                    throw new IllegalArgumentException("Invalid command");
                }
        }
        return currCommandType;
    }

    /*
     * Returns the first argument of the current command. In the case of
     * C_ARITHMETIC, the command itself (add, sub, etc.) is returned. Should not
     * be called if the current command is C_RETURN.
     */
    public String arg1() {
        if (currCommandType == CommandType.C_RETURN) {
            throw new IllegalArgumentException("C_RETURN should not be called");
        }

        if (currCommandType == CommandType.C_ARITHMETIC) {
            return currLine;
        } else {
            return currLine.split(" ")[1];
        }
    }

    /*
     * Returns the second argument of the current command. Should be called only
     * if the current command is C_PUSH, C_POP, C_FUNCTION, or C_CALL.
     */
    public int arg2() {
        if (currCommandType != CommandType.C_PUSH && currCommandType != CommandType.C_POP
                && currCommandType != CommandType.C_FUNCTION && currCommandType != CommandType.C_CALL) {
            throw new IllegalArgumentException("C_PUSH, C_POP, C_FUNCTION, or C_CALL should be called");
        }
        return Integer.parseInt(currLine.split(" ")[2]);
    }
}
