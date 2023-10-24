
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;


public class HackAssembler {

    private File file;
    private Parser parser;
    private Code code;
    private SymbolTable symbolTable;

    public HackAssembler(File file) {
        this.file = file;
        this.parser = new Parser(file);
        this.code = new Code();
        this.symbolTable = new SymbolTable();
    }


    public void convertToBinary() {
        try {
            PrintWriter writer = new PrintWriter("Prog.hack");

            
            int romAddress = 0;
            // First pass
            while (parser.hasMoreCommands()) {
                parser.advance();
                if (parser.commandType() == Parser.CommandType.C_COMMAND) {
                    romAddress++;
                } else if (parser.commandType() == Parser.CommandType.A_COMMAND) {
                    romAddress++;
                } else if (parser.commandType() == Parser.CommandType.L_COMMAND) {
                    symbolTable.addEntry(parser.symbol(), romAddress);
                }
            }

            this.parser = new Parser(file); // Go back to the beginning of the file

            // Second pass
            int ramAddress = 16;

            while (parser.hasMoreCommands()) {
                parser.advance();
                StringBuilder command = new StringBuilder();
                if (parser.commandType() == Parser.CommandType.A_COMMAND) {
                    String symbol = parser.symbol();
                    // @Xxx where Xxx is a symbol
                    if (!symbol.matches("[0-9]+")) {
                        // @Xxx where Xxx is a symbol and is not in symbol table
                        if (!symbolTable.contains(symbol)) {
                            symbolTable.addEntry(symbol, ramAddress);
                            writer.println(constructBinaryPadding(ramAddress));
                            ramAddress++;
                        } else // @Xxx where Xxx is a symbol and is in symbol table
                            writer.println(constructBinaryPadding(symbolTable.GetAddress(symbol)));
                    } else if (symbol.matches("[0-9]+"))// @Xxx where Xxx is a number                        
                        writer.println(constructBinaryPadding(Integer.parseInt(symbol)));
                } else if (parser.commandType() == Parser.CommandType.C_COMMAND) {
                    command.append("111");
                    String comp = parser.comp();
                    String dest = parser.dest();
                    String jump = parser.jump();
                    command.append(code.comp(comp));
                    command.append(code.dest(dest));
                    command.append(code.jump(jump));
                    writer.println(command.toString());
                }
            }


            writer.close();
        } catch (FileNotFoundException fe) {
            fe.printStackTrace();
        }
    }


    private String constructBinaryPadding(int number) {
        StringBuilder command = new StringBuilder();
        command.append("0");
        String binary = Integer.toBinaryString(number);
        int padding = 15 - binary.length();
        for (int i = 0; i < padding; i++)
            command.append("0");
        command.append(binary);

        return command.toString();
    }

    public static void main(String[] args) {
        File file = new File(args[0]);
        HackAssembler assembler = new HackAssembler(file);
        assembler.convertToBinary();
    }
}



