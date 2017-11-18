import java.io.PrintWriter;
import java.io.Writer;

public class CodeWriter {
    private int opCounter;
    private final PrintWriter output;
    private final String compilationUnitName;

    public CodeWriter(String compilationUnitName, Writer output) {
        this.compilationUnitName = compilationUnitName;
        this.opCounter = 0;
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
        output.println(assign().mPlusOne().toA().andToM());
    }

    public void decrementMemoryAndAssignToAddressRegister() {
        output.println(assign().mMinusOne().toA().andToM());
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

    void pop(Segment segment, int i) {
        ainstSymbol(segment.memoryLocation(compilationUnitName, i));
        if (segment.usesBasePointer()) {
            assignMemoryToDataRegister();
        } else {
            assignAddressRegisterToDataRegister();
        }
        if (segment.usesPointerArithmetic()) {
            ainstValue(i);
            raw("D=D+A");
        }
        ainstSymbol("R13");
        assignDataRegisterToMemory();
        ainstSymbol("SP");
        decrementMemoryAndAssignToAddressRegister();
        assignMemoryToDataRegister();
        ainstSymbol("R13");
        assignMemoryToAddressRegister();
        assignDataRegisterToMemory();
    }

    void popToDataRegister() {
        pop(Segment.TEMP, 0);
        atTemp(0);
        raw("D=M");
    }

    void push(Segment segment, int i) {
        if (segment == Segment.CONSTANT) {
            ainstValue(i);
            assignAddressRegisterToDataRegister();
        } else {
            ainstSymbol(segment.memoryLocation(compilationUnitName, i));
            if (segment.usesPointerArithmetic()) {
                if (segment.usesBasePointer()) {     // local, argument
                    assignMemoryToDataRegister();
                } else { // static, temp
                    assignAddressRegisterToDataRegister();
                }
                ainstValue(i);
                incrementAddressRegisterByDataRegisterValue();
            }
            assignMemoryToDataRegister();
        }
        ainstSymbol("SP");
        incrementMemoryAndAssignToAddressRegister();
        decrementAddressRegister();
        assignDataRegisterToMemory();
    }

    public void comparisonOperation(String opName) {
        comp(opName, "-", opName);
    }

    public void logicalOperation(String opOperator) {
        pop(Segment.TEMP, 0);
        pop(Segment.TEMP, 1);
        atTemp(1);
        assignMemoryToDataRegister();
        atTemp(0);
        raw("M=D" + opOperator + "M");
        push(Segment.TEMP, 0);
    }

    void comp(String operationName, String opOperator, String opTrueJumpCondition) {
        pop(Segment.TEMP, 1);
        final String y = "@6"; // y comes from temp 1
        pop(Segment.TEMP, 0);
        final String x = "@5"; // x comes from temp 0
        final String f = "@5"; // the result will be stored in temp 0
        final String operationLabel = operationName + opCounter;
        final String operationEndLabel = operationName + "END" + opCounter;
        raw(x);
        raw("D=M");
        raw(y);
        raw("D=D" + opOperator + "M");
        jumpIfDataRegister(opTrueJumpCondition, operationLabel);
        raw("D=0");       // x != y
        raw("@" + operationEndLabel);
        raw("0;JMP");
        raw("(" + operationLabel + ")");     // x == y
        raw("D=-1"); // result is true = 11111111
        raw("(" + operationEndLabel + ")");
        raw(f);        // temp[0] holds result
        raw("M=D");
        push(Segment.TEMP, 0);
        opCounter++;
    }

    private void jumpIfDataRegister(String opTrueJumpCondition, String operationLabel) {
        raw("@" + operationLabel);
        raw("D;J" + opTrueJumpCondition);
    }

    void jumpIfDataRegisterIsTruthy(String label) {
        jumpIfDataRegister("NE", label); // truthy is the same as != 0
    }

    public void atTemp(int i) {
        ainstValue(5 + i);
    }

    public void negateMemory() {
        output.println(assign().negatedM().toM());
    }

    public void binaryNotMemory() {
        output.println(assign().notM().toM());
    }

    public void label(String name) {
        output.println(String.format("(%s)", name));
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

        public AssignmentOperationBuilder andToM() {
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

        public AssignmentOperationBuilder negatedM() {
            from = "-M";
            return this;
        }

        public AssignmentOperationBuilder notM() {
            from = "!M";
            return this;
        }
    }
}