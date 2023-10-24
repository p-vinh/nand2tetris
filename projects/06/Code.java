import java.util.Map;

// Generates binary code for the given input
public class Code {
    private Map<String, String> predefComp;
    private Map<String, String> predefDest;
    private Map<String, String> predefJump;

    // Generate the predefined table
    public Code() { 
        LoadPredefined loader = new LoadPredefined();  
        predefDest = loader.loadPredefinedDest();
        predefComp = loader.loadPredefindComp();
        predefJump = loader.loadPredefindJump();
    }


    /**
     * Returns the binary code of the dest mnemonic
     * @param mnemonic
     * @return the binary code of the dest mnemonic (3 bits)
     */
    public String dest(String mnemonic) {
        return predefDest.get(mnemonic);
    }

    /**
     * Returns the binary code of the comp mnemonic
     * @param mnemonic
     * @return the binary code of the comp mnemonic (7 bits)
     */
    public String comp(String mnemonic) {
        return predefComp.get(mnemonic);
    }

    /**
     * Returns the jump binary code for the given mnemonic
     * @param mnemonic
     * @return the jump binary code for the given mnemonic (3 bits)
     */
    public String jump(String mnemonic) {
        return predefJump.get(mnemonic);
    }

    
}
