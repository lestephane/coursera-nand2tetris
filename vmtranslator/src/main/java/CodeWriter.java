import java.io.PrintWriter;
import java.io.Writer;

public class CodeWriter {
    private final PrintWriter output;

    public CodeWriter(Writer output) {
        this.output = new PrintWriter(output);
    }

    public void ainstValue(int value) {
        output.print('@');
        output.println(value);
    }

    public void ainstSymbol(String symbol) {
        output.print('@');
        output.println(symbol);
    }

    public void printComment(String line) {
        output.print("// ");
        output.println(line);
    }

    public void assignAddressRegisterToDataRegister() {
        output.println(assign().fromA().toD());
    }

    public void assignMemoryToAddressRegister() {
        output.println(assign().fromM().toA());
    }

    public void assignMemoryToDataRegister() {
        output.println(assign().fromM().toD());
    }

    public void assignDataRegisterToMemory() {
        output.println(assign().fromD().toM());
    }

    public void incrementMemoryAndAssignToAddressRegister() {
        output.println(assign().mPlusOne().toA().andM());
    }

    public void decrementMemoryAndAssignToAddressRegister() {
        output.println(assign().mMinusOne().toA().andM());
    }

    public void raw(String s) {
        output.println(s);
    }

    public void decrementAddressRegister() {
        output.println(assign().aMinusOne().toA());
    }

    public void incrementAddressRegisterByDataRegisterValue() {
        output.println(assign().aPlusD().toA());
    }

    public void incrementMemoryByDataRegisterValue() {
        output.println(assign().mPlusD().toM());
    }

    public void decrementMemoryByDataRegisterValue() {
        output.println(assign().mMinusD().toM());
    }

    public void close() {
        output.close();
    }

    private AssignmentOperationBuilder assign() {
        return new AssignmentOperationBuilder();
    }

    private class AssignmentOperationBuilder {
        private String from;
        private String to;

        public AssignmentOperationBuilder fromM() {
            from = "M";
            return this;
        }

        public AssignmentOperationBuilder toA() {
            to = "A";
            return this;
        }

        public AssignmentOperationBuilder fromA() {
            from = "A";
            return this;
        }

        public AssignmentOperationBuilder toD() {
            to = "D";
            return this;
        }

        public String toString() {
            return to + "=" + from;
        }

        public AssignmentOperationBuilder fromD() {
            from = "D";
            return this;
        }

        public AssignmentOperationBuilder toM() {
            to = "M";
            return this;
        }

        public AssignmentOperationBuilder mPlusOne() {
            from = "M+1";
            return this;
        }

        public AssignmentOperationBuilder andM() {
            to += "M";
            return this;
        }

        public AssignmentOperationBuilder aMinusOne() {
            from = "A-1";
            return this;
        }

        public AssignmentOperationBuilder mMinusOne() {
            from = "M-1";
            return this;
        }

        public AssignmentOperationBuilder aPlusD() {
            from = "D+A";
            return this;
        }

        public AssignmentOperationBuilder mPlusD() {
            from = "D+M";
            return this;
        }

        public AssignmentOperationBuilder mMinusD() {
            from = "M-D";
            return this;
        }
    }
}