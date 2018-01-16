import java.io.PrintWriter;
import java.io.Writer;

public class CodeWriter {
    private final PrintWriter output;
    private final String compilationUnitName;
    private int opCounter;
    private String currentFunctionName;
    private int currentReturnOpCounter;
    private int currentFunctionArgumentCount;

    public CodeWriter(String compilationUnitName, Writer output) {
        this.output = new PrintWriter(output);

        this.opCounter = 0;
        this.compilationUnitName = compilationUnitName;

        this.currentFunctionName = compilationUnitName;
        this.currentReturnOpCounter = 0;
    }

    public void printComment(String line) {
        output.print("// ");
        output.println(line);
    }

    public void incrementMemoryByDataRegisterValue() {
        output.println(assign().mPlusD().toM());
    }

    private Assigner assign() {
        return Assigner.assign();
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
        ainstSymbol("R14");
        assignDataRegisterToMemory();
        ainstSymbol(sp);
        decrementMemoryAndAssignToAddressRegister();
        assignMemoryToDataRegister();
        ainstSymbol("R14");
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

    void popUsingBasePointer(String sp, Segment segment) {
        popUsingBasePointer(sp, segment, -1);
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

    public void endFunction() {
        currentFunctionName = null;
    }

    public String newReturnLabel() {
        return String.format("%s$ret.%s", currentFunctionName, currentReturnOpCounter++);
    }

    public void pushSymbolAddress(String symbolName) {
        ainstSymbol(symbolName);
        assignAddressRegisterToDataRegister();
        ainstSymbol("SP");
        incrementMemoryAndAssignToAddressRegister();
        decrementAddressRegister();
        assignDataRegisterToMemory();
    }

    public void pushPointerValue(String symbolName) {
        ainstSymbol(symbolName);
        assignMemoryToDataRegister();
        ainstSymbol("SP");
        incrementMemoryAndAssignToAddressRegister();
        decrementAddressRegister();
        assignDataRegisterToMemory();
    }

    public int currentFunctionArgumentCount() {
        return currentFunctionArgumentCount;
    }

    public void writeCall(String functionName, int nargs) {
        //
        // 1 Compute arg pointer (SP - nargs)
        // see https://www.coursera.org/learn/nand2tetris2/lecture/zJVns/unit-2-4-function-call-and-return-implementation-preview @ 8:05
        //
        // Note: this is done in two steps so as not to override ARG before we first saved the caller's frame.
        //
        raw("@" + nargs);
        raw("D=A");
        raw("@SP");
        raw("D=M-D");
        raw(assign(Register.D).to(Register.D2));
        Register calleeArgsPtr = Register.D2;

        //
        // 2. Saves the caller's frame
        // see https://www.coursera.org/learn/nand2tetris2/lecture/zJVns/unit-2-4-function-call-and-return-implementation-preview @ 8:46
        // (return address, LCL, ARG, THIS, THAT)
        //
        String returnLabel = newReturnLabel();
        pushSymbolAddress(returnLabel);
        pushPointerValue("LCL");
        pushPointerValue("ARG");
        pushPointerValue("THIS");
        pushPointerValue("THAT");

        // 1.b Sets arg pointer
        raw(assign(calleeArgsPtr).to(Register.ARG));

        //
        // 3. Jumps to execute function
        // see see https://www.coursera.org/learn/nand2tetris2/lecture/zJVns/unit-2-4-function-call-and-return-implementation-preview @ 10:17
        //
        raw("@" + functionName);
        raw("0;JMP");
        currentFunctionArgumentCount = nargs;

        // 2.b Define the label for the function to jump to upon return
        raw(String.format("(%s)", returnLabel));
    }

    private void raw(Assigner assigner) {
        raw(assigner.toStrings());
    }

    private Assigner assign(Register d) {
        return Assigner.assign(d);
    }

    private void raw(String[] statements) {
        for (String s: statements) {
            raw(s);
        }
    }

    /**
     * see https://www.coursera.org/learn/nand2tetris2/lecture/zJVns/unit-2-4-function-call-and-return-implementation-preview @ 23:44
     */
    void writeFunction(String name, int nvars) {
        beginFunction(name);
        label(name);

        //
        // Sets up the local segment (LCL = SP)
        //
        raw("@SP");
        raw("D=M");
        raw("@LCL");
        raw("M=D");

        //
        // Initialize each local variable to zero
        //
        for (int i = 0; i < nvars; i++) {
            push(Segment.CONSTANT, 0);
        }
    }

    public void beginFunction(String functionName) {
        currentFunctionName = functionName;
        currentReturnOpCounter = 0;
    }

    public void label(String name) {
        output.println(String.format("(%s)", name));
    }

    void writeReturn() {
        // save return address to avoid clobbering
        raw("@" + currentFunctionArgumentCount());
        raw("D=A");
        raw("@ARG");
        raw("A=M+D");
        raw("D=M");
        raw(assign(Register.D).to(Register.D2));

        // move SP (return value) to ARG 0
        pop(Segment.ARGUMENT, 0);

        // restore stack pointer
        raw("@ARG");
        raw("D=M+1");
        raw("@SP");
        raw("M=D");

        // restore segments
        final String lcl = Segment.LOCAL.memoryLocation(null, 0);
        popUsingBasePointer(lcl, Segment.POINTER, 1); // restore THAT
        popUsingBasePointer(lcl, Segment.POINTER, 0); // restore THIS
        popUsingBasePointer(lcl, Segment.ARGUMENT);
        popUsingBasePointer(lcl, Segment.LOCAL);

        // Jump to caller provided return address
        raw("@R13");
        raw("A=M");
        raw("0;JMP");
        endFunction();
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

    public void incrementAddressRegisterByDataRegisterValue() {
        output.println(assign().aPlusD().toA());
    }

    public void incrementMemoryAndAssignToAddressRegister() {
        output.println(assign().mPlusOne().toA().andToM());
    }

    public void decrementAddressRegister() {
        output.println(assign().aMinusOne().toA());
    }
}