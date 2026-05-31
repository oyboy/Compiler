package semantic;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class Type {
    public static final PrimitiveType INT    = new PrimitiveType("int");
    public static final PrimitiveType FLOAT  = new PrimitiveType("float");
    public static final PrimitiveType BOOL   = new PrimitiveType("bool");
    public static final PrimitiveType VOID   = new PrimitiveType("void");
    public static final PrimitiveType STRING = new PrimitiveType("string");
    public static final PrimitiveType ERROR  = new PrimitiveType("<error>");
    public static final PrimitiveType POINTER = new PrimitiveType("pointer");

    public abstract boolean isCompatibleWith(Type other);
    public abstract String getName();

    @Override
    public String toString() { return getName(); }

    public static class PrimitiveType extends Type {
        private final String name;
        PrimitiveType(String name) { this.name = name; }

        @Override
        public String getName() { return name; }

        @Override
        public boolean isCompatibleWith(Type other) {
            if (this == ERROR || other == ERROR) return true;
            if (this == FLOAT && other == INT) return true;
            return this == other;
        }

        @Override
        public boolean equals(Object o) { return this == o; }

        @Override
        public int hashCode() { return name.hashCode(); }
    }

    public static class PointerType extends Type {
        public final Type pointeeType; // null → void*

        public PointerType(Type pointeeType) {
            this.pointeeType = pointeeType;
        }

        public static PointerType of(Type inner) {
            return new PointerType(inner);
        }

        @Override
        public String getName() {
            return (pointeeType == null ? "void" : pointeeType.getName()) + "*";
        }

        @Override
        public boolean isCompatibleWith(Type other) {
            if (other == ERROR)   return true;
            if (other == POINTER) return true;
            if (other instanceof PointerType pt) {
                if (this.pointeeType == null || pt.pointeeType == null) return true;
                return this.pointeeType.isCompatibleWith(pt.pointeeType);
            }
            return false;
        }

        @Override
        public boolean isNumeric() { return false; }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof PointerType pt)) return false;
            return Objects.equals(pointeeType, pt.pointeeType);
        }

        @Override
        public int hashCode() { return Objects.hash("ptr", pointeeType); }
    }

    public static class FunctionType extends Type {
        public final List<Type> paramTypes;
        public final Type returnType;

        public FunctionType(List<Type> paramTypes, Type returnType) {
            this.paramTypes = paramTypes;
            this.returnType = returnType;
        }

        @Override
        public String getName() {
            String params = paramTypes.stream()
                    .map(Type::getName)
                    .collect(Collectors.joining(", "));
            return "fn(" + params + ") -> " + returnType.getName();
        }

        @Override
        public boolean isCompatibleWith(Type other) {
            if (other == ERROR) return true;
            return this.equals(other);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof FunctionType)) return false;
            FunctionType ft = (FunctionType) o;
            return returnType.equals(ft.returnType) && paramTypes.equals(ft.paramTypes);
        }

        @Override
        public int hashCode() { return Objects.hash(paramTypes, returnType); }
    }

    public static class StructType extends Type {
        public final String name;
        public final Map<String, Type> fields;

        public StructType(String name, Map<String, Type> fields) {
            this.name = name;
            this.fields = fields;
        }

        @Override
        public String getName() { return "struct " + name; }

        @Override
        public boolean isCompatibleWith(Type other) {
            if (other == ERROR) return true;
            return this.equals(other);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof StructType)) return false;
            return name.equals(((StructType) o).name);
        }

        @Override
        public int hashCode() { return name.hashCode(); }
    }

    public boolean isNumeric() {
        return this == INT || this == FLOAT;
    }

    public boolean isPrimitive() {
        return this instanceof PrimitiveType;
    }

    public boolean isError() {
        return this == ERROR;
    }

    public static Type fromString(String typeName) {
        switch (typeName) {
            case "int":    return INT;
            case "float":  return FLOAT;
            case "bool":   return BOOL;
            case "void":   return VOID;
            case "string": return STRING;
            case "pointer": return POINTER;
            default:       return null;
        }
    }

    public static class ArrayType extends Type {
        public final Type elementType;
        public final int size;

        public ArrayType(Type elementType, int size) {
            this.elementType = elementType;
            this.size = size;
        }

        @Override
        public String getName() {
            return elementType.getName() + "[" + size + "]";
        }

        @Override
        public boolean isCompatibleWith(Type other) {
            if (this.isError() || (other != null && other.isError())) return true;
            if (other instanceof ArrayType) {
                return elementType.isCompatibleWith(((ArrayType) other).elementType);
            }
            return false;
        }
    }
}
