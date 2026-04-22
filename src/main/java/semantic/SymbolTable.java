package semantic;

import java.util.*;

public class SymbolTable {
    private final Deque<Map<String, Symbol>> scopes = new ArrayDeque<>();
    private int depth = 0;

    public SymbolTable() {
        enterScope();
    }

    public void enterScope() {
        scopes.push(new LinkedHashMap<>());
        depth++;
    }

    public void exitScope() {
        if (scopes.size() <= 1) {
            throw new IllegalStateException("Cannot exit global scope.");
        }
        scopes.pop();
        depth--;
    }

    public boolean insert(Symbol symbol) {
        Map<String, Symbol> current = scopes.peek();
        if (current.containsKey(symbol.name)) {
            return false;
        }
        current.put(symbol.name, symbol);
        return true;
    }

    public Optional<Symbol> lookup(String name) {
        for (Map<String, Symbol> scope : scopes) {
            Symbol sym = scope.get(name);
            if (sym != null) return Optional.of(sym);
        }
        return Optional.empty();
    }

    public Optional<Symbol> lookupLocal(String name) {
        return Optional.ofNullable(scopes.peek().get(name));
    }

    public int getDepth() { return depth; }

    public String dump() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Symbol Table Dump ===\n");
        int level = 0;

        List<Map<String, Symbol>> ordered = new ArrayList<>(scopes);
        Collections.reverse(ordered);
        for (Map<String, Symbol> scope : ordered) {
            String indent = "  ".repeat(level++);
            sb.append(indent).append("[Scope depth ").append(level - 1).append("]\n");
            if (scope.isEmpty()) {
                sb.append(indent).append("  (empty)\n");
            }
            for (Symbol sym : scope.values()) {
                sb.append(indent).append("  ").append(sym).append("\n");
            }
        }
        return sb.toString();
    }
}