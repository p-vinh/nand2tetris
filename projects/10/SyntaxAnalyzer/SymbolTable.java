package SyntaxAnalyzer;

import java.util.HashMap;
import java.util.Map;


public class SymbolTable {

    private enum Kind {
        STATIC, FIELD, ARG, VAR, NONE
    }

    private Map<String, Symbol> classTable;

    public SymbolTable() {
        this.classTable = new HashMap<>();
    }

    public void reset() {

    }

    public void define(String name, String type, Kind kind) {

    }

    public int varCount(Kind kind) {
        return 0;
    }

    public Kind kindOf(String name) {
        return Kind.NONE;
    }

    public String typeOf(String name) {
        return "";
    }

    public int indexOf(String name) {
        return 0;
    }

    private class Symbol {
        private String name;
        private String type;
        private Kind kind;
        private int index;

        public Symbol(String name, String type, Kind kind, int index) {
            this.name = name;
            this.type = type;
            this.kind = kind;
            this.index = index;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public Kind getKind() {
            return kind;
        }

        public int getIndex() {
            return index;
        }
    }
}
