
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    public enum Kind {
        STATIC, FIELD, ARG, VAR, NONE
    }

    private class Symbol {
        private String type;
        private Kind kind;
        private int index;

        public Symbol(String type, Kind kind, int index) {
            this.type = type;
            this.kind = kind;
            this.index = index;
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

    // INDEXES
    private int staticCount = 0;
    private int fieldCount = 0;
    private int argCount = 0;
    private int varCount = 0;

    private Map<String, Symbol> classTable;
    private Map<String, Symbol> subroutineTable;

    public SymbolTable() {
        this.classTable = new HashMap<>();
        this.subroutineTable = new HashMap<>();
    }

    public void reset() {
        this.classTable.clear();
        this.subroutineTable.clear();
    }

    public void define(String name, String type, Kind kind) {
        if (kind == Kind.STATIC) {
            if (!classTable.containsKey(name))
                classTable.put(name, new Symbol(type, kind, staticCount++));
        } else if (kind == Kind.FIELD) {
            classTable.put(name, new Symbol(type, kind, fieldCount++));
        } else if (kind == Kind.ARG) {
            subroutineTable.put(name, new Symbol(type, kind, argCount++));
        } else if (kind == Kind.VAR) {
            subroutineTable.put(name, new Symbol(type, kind, varCount++));
        }
    }

    public int varCount(Kind kind) {
        if (kind == Kind.STATIC)
            return staticCount;
        else if (kind == Kind.FIELD)
            return fieldCount;
        else if (kind == Kind.ARG)
            return argCount;
        else if (kind == Kind.VAR)
            return varCount;
        else
            return 0;
    }

    public Kind kindOf(String name) {
        if (subroutineTable.containsKey(name))
            return subroutineTable.get(name).getKind();
        else if (classTable.containsKey(name))
            return classTable.get(name).getKind();
        else
            return Kind.NONE;
    }

    public String typeOf(String name) {
        if (subroutineTable.containsKey(name))
            return subroutineTable.get(name).getType();
        else if (classTable.containsKey(name))
            return classTable.get(name).getType();
        else
            return "NONE";
    }

    public int indexOf(String name) {
        if (subroutineTable.containsKey(name))
            return subroutineTable.get(name).getIndex();
        else if (classTable.containsKey(name))
            return classTable.get(name).getIndex();
        else
            return -1;
    }

}
