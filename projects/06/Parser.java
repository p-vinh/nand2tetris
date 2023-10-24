
// Reads and parses each line of the input file

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Parser {


    private Scanner scanner;
    private String currLine;

    public static enum CommandType {
        A_COMMAND,
        C_COMMAND,
        L_COMMAND
    }

    private CommandType currCommandType;

    /**
     * Encapsulates access to the input code. Reads an assembly language command, parses it, and provides convenient access to the command’s components (fields and symbols).
     * In addition, removes all white space and comments.
     * @param fileName the name of the file to read from
     */
    public Parser(File fileName) {
        try {
            scanner = new Scanner(fileName);
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            e.printStackTrace();
        }
    }

    public boolean hasMoreCommands() {
        return scanner.hasNextLine();
    }

    /**
     * Reads the next command from the input and makes it the current command. Should be called only if hasMoreCommands() is true. Initially there is no current command.
     */
    public void advance() {
        currLine = scanner.nextLine();
        currLine = currLine.replaceAll("\\s+|//.*", ""); // Remove comments
        
        if (currLine.isEmpty()) {
            advance();
        } else if (currLine.startsWith("@")) {
            currCommandType = CommandType.A_COMMAND;
        } else if (currLine.startsWith("(")) {
            currCommandType = CommandType.L_COMMAND;
        } else {
            currCommandType = CommandType.C_COMMAND;
        }
    }

    /**
     * Returns the type of the current command:
     *          A_COMMAND for @Xxx where Xxx is either a symbol or a decimal number
     *          C_COMMAND for dest=comp;jump
     *          L_COMMAND (actually, pseudocommand) for (Xxx) where Xxx is a symbol.
     * @return the type of the current command
     */
    public CommandType commandType() {
        return currCommandType;
    }

    /**
     * Returns the symbol or decimal Xxx of the current command @Xxx or (Xxx). Should be called only when commandType() is A_COMMAND or L_COMMAND.
     * @return the symbol or decimal Xxx of the current command
     */
    public String symbol() {
        if (currCommandType == CommandType.A_COMMAND) {
            return currLine.substring(1);
        } else if (currCommandType == CommandType.L_COMMAND) {
            return currLine.substring(1, currLine.length() - 1);
        } 
        return null;
    }


    /**
     * Returns the instruction's dest field
     * @return the instruction's dest field
     */
    public String dest() {
        if (currLine != null) {
            if (currLine.contains("=")) {
                return currLine.split("=")[0];
            }
        }
        return null;
    }


    /**
     * Returns the instruction's comp field
     * @return the instruction's comp field
     */
    public String comp() {
        if (currLine != null) {
            if (currLine.contains("=")) {
                return currLine.split("=")[1];
            } else if (currLine.contains(";")) {
                return currLine.split(";")[0];
            }
        }
        return null;
    }

    /**
     * Returns the instruction's jump field
     * @return the instruction's jump field
     */
    public String jump() {
        if (currLine != null) {
            if (currLine.contains(";")) {
                return currLine.split(";")[1];
            }
        }
        return null;
    }

}
