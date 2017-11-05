import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, Integer> symbols = new HashMap<>();

    public SymbolTable() {
        for (int i = 0; i <= 15; i++) {
           addSymbol("R" + i, i);
        }
        addSymbol("SCREEN", 16384);
        addSymbol("KBD", 24576);
        addSymbol("SP", 0);
        addSymbol("LCL", 1);
        addSymbol("ARG", 2);
        addSymbol("THIS", 3);
        addSymbol("THAT", 4);
    }

    Integer addSymbol(String name, Integer value) {
        assert value != null : name;
        return this.symbols.put(name, value);
    }

    public Integer lookup(String symbol) {
        return symbols.get(symbol);
    }
}