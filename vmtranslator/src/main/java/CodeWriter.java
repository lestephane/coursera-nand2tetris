import java.io.PrintWriter;
import java.io.Writer;

public class CodeWriter {
    private final PrintWriter output;
    private final String compilationUnitName;
    private int opCounter;

    public CodeWriter(String compilationUnitName, Writer output) {
        this.compilationUnitName = compilationUnitName;
        this.opCounter = 0;
        this.output = new PrintWriter(output);
    }

    public void printComment(String line) {
        output.print("// ");
        output.println(line);
    }

    public void incrementMemoryAndAssignToAddressRegister() {
        output.println(assign().mPlusOne().toA().andToM());
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

    private AssignmentOperationBuilder assign() {
        return new AssignmentOperationBuilder();
    }

    public void decrementMemoryByDataRegisterValue() {
        output.println(assign().mMinusD().toM());
    }

    public void close() {
        output.close();
    }

    void popToDataRegister() {
        pop(Segment.TEMP, 0);
        atTemp(0);
        raw("D=M");
    }

    void pop(Segment segment, int i) {
        popUsingBasePointer("SP", segment, i);
    }

    public void atTemp(int i) {
        ainstValue(5 + i);
    }

    public void raw(String s) {
        output.println(s);
    }

    void popUsingBasePointer(String bpSymbol, String targetSymbol) {
        popUsingBasePointer(bpSymbol, targetSymbol);
    }

    void popUsingBasePointer(String sp, Segment segment) {
        popUsingBasePointer(sp, segment, -1);
    }

    void popUsingBasePointer(String sp, Segment segment, int i) {
        ainstSymbol(segment.memoryLocation(compilationUnitName, i));
        if (segment.usesBasePointer() && i >= 0) {
            assignMemoryToDataRegister();
        } else {
            assignAddressRegisterToDataRegister();
        }
        if (segment.usesPointerArithmetic() && i >=0) {
            ainstValue(i);
            raw("D=D+A");
        }
        ainstSymbol("R13");
        assignDataRegisterToMemory();
        ainstSymbol(sp);
        decrementMemoryAndAssignToAddressRegister();
        assignMemoryToDataRegister();
        ainstSymbol("R13");
        assignMemoryToAddressRegister();
        assignDataRegisterToMemory();
    }

    public void ainstValue(int value) {
        output.print('@');
        output.println(value);
    }

    public void ainstSymbol(String symbol) {
        output.print('@');
        output.println(symbol);
    }

    public void assignMemoryToDataRegister() {
        output.println(assign().fromM().toD());
    }

    public void assignAddressRegisterToDataRegister() {
        output.println(assign().fromA().toD());
    }

    public void assignDataRegisterToMemory() {
        output.println(assign().fromD().toM());
    }

    public void decrementMemoryAndAssignToAddressRegister() {
        output.println(assign().mMinusOne().toA().andToM());
    }

    public void assignMemoryToAddressRegister() {
        output.println(assign().fromM().toA());
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

    void jumpIfDataRegisterIsTruthy(String label) {
        jumpIfDataRegister("NE", label); // truthy is the same as != 0
    }

    private void jumpIfDataRegister(String opTrueJumpCondition, String operationLabel) {
        raw("@" + operationLabel);
        raw("D;J" + opTrueJumpCondition);
    }

    public void jump() {
        raw("0;JMP");
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

    public void atSegment(Segment segment) {
        ainstSymbol(segment.memoryLocation(null, -1));
    }

    public void assignMemoryPlusOneToAddressRegister() {
        output.println(assign().mPlusOne().toA());
    }
}