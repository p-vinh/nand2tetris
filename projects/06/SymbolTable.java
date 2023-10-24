import java.util.Map;
// Handles the symbol table for the compiler
public class SymbolTable {
    private Map<String, Integer> symbolTable;

    public SymbolTable() {
        LoadPredefined loader = new LoadPredefined();
        symbolTable = loader.loadPredefinedTable();
    }


    /**
     * Adds the pair (symbol, address) to the table
     * @param symbol the symbol to add
     * @param address the address to add
     */
    public void addEntry(String symbol, int address) {
        symbolTable.put(symbol, address);
    }

    /**
     * Does the symbol table contain the given symbol?
     * @param symbol the symbol to check
     * @return true if the symbol table contains the given symbol, false otherwise
     */
    public boolean contains(String symbol) {
        return symbolTable.containsKey(symbol);
    }

    /**
     * Returns the address associated with the symbol
     * @param symbol the symbol to get the address of
     * @return the address associated with the symbol
     */
    public int GetAddress(String symbol) {
        return symbolTable.get(symbol);
    }    
}
