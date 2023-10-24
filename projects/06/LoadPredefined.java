import java.util.HashMap;
import java.util.Map;

public class LoadPredefined {

    public LoadPredefined(){
    }

    public Map<String, Integer> loadPredefinedTable() {
        Map<String, Integer> symbolTable = new HashMap<>();
        symbolTable.put("SP", 0);
        symbolTable.put("LCL", 1);
        symbolTable.put("ARG", 2);
        symbolTable.put("THIS", 3);
        symbolTable.put("THAT", 4);

        // Registers
        for (int i = 0; i < 16; i++) {
            String keyRegister = "R" + i;
            symbolTable.put(keyRegister, i);
        }

        // Screen and keyboard
        symbolTable.put("SCREEN", 16384);
        symbolTable.put("KBD", 24576);

        return symbolTable;
    }

    public Map<String, String> loadPredefinedDest() {
        Map<String, String> predefDest = new HashMap<>(8);
        predefDest.put(null, "000");
        predefDest.put("M", "001");
        predefDest.put("D", "010");
        predefDest.put("MD", "011");
        predefDest.put("A", "100");
        predefDest.put("AM", "101");
        predefDest.put("AD", "110");
        predefDest.put("AMD", "111");

        return predefDest;
    }

    public Map<String, String> loadPredefindJump() {
        Map<String, String> predefJump = new HashMap<>(8);
        predefJump.put(null, "000");
        predefJump.put("JGT", "001");
        predefJump.put("JEQ", "010");
        predefJump.put("JGE", "011");
        predefJump.put("JLT", "100");
        predefJump.put("JNE", "101");
        predefJump.put("JLE", "110");
        predefJump.put("JMP", "111");

        return predefJump;
    }

    public Map<String, String> loadPredefindComp() {
        Map<String, String> predefComp = new HashMap<>(28);
        // When a == 0
        predefComp.put("0", "0101010");
        predefComp.put("1", "0111111");
        predefComp.put("-1", "0111010");
        predefComp.put("D", "0001100");
        predefComp.put("A", "0110000");
        predefComp.put("!D", "0001101");
        predefComp.put("!A", "0110001");
        predefComp.put("-D", "0001111");
        predefComp.put("-A", "0110011");
        predefComp.put("D+1", "0011111");
        predefComp.put("A+1", "0110111");
        predefComp.put("D-1", "0001110");
        predefComp.put("A-1", "0110010");
        predefComp.put("D+A", "0000010");
        predefComp.put("D-A", "0010011");
        predefComp.put("A-D", "0000111");
        predefComp.put("D&A", "0000000");
        predefComp.put("D|A", "0010101");

        // When a == 1
        predefComp.put("M", "1110000");
        predefComp.put("!M", "1110001");
        predefComp.put("-M", "1110011");
        predefComp.put("M+1", "1110111");
        predefComp.put("M-1", "1110010");
        predefComp.put("D+M", "1000010");
        predefComp.put("D-M", "1010011");
        predefComp.put("M-D", "1000111");
        predefComp.put("D&M", "1000000");
        predefComp.put("D|M", "1010101");

        return predefComp;
    }
}
