import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class HackAssemblerCommandsAsserter extends ArrayList<HackAssemblerCommandsAsserter.HackAssemblerCommandAsserter> {
    private int pos;

    public HackAssemblerCommandsAsserter() {
        this.pos = 0;
    }

    public void popsToLocal(int i) {
        popsToWithLeadingComment(Segment.LOCAL, i);
    }

    public void popsStandardStackToArgAt(int i) {
        popsToWithLeadingComment(Segment.ARGUMENT, i);
    }

    public void popsToThis(int i) {
        popsToWithLeadingComment(Segment.THIS, i);
    }

    public void popsToThat(int i) {
        popsToWithLeadingComment(Segment.THAT, i);
    }

    public void popsToTemp(int i) {
        popsToWithLeadingComment(Segment.TEMP, i);
    }

    private void popsToTempWithoutLeadingComment(int i) {
        popsToWithoutLeadingComment(Segment.TEMP, i);
    }

    private void popsToWithLeadingComment(Segment segment, int i) {
        asm("// pop " + segment.name().toLowerCase() + " " + i);
        popsToWithoutLeadingComment(segment, i);
    }

    public void popsToStatic(int i) {
        asm("// pop static " + i);
        asm("@Junit." + i);
        asm("D=A");
        popToAddressPointedToByDataRegister();
    }

    private void popsToWithoutLeadingComment(Segment segment, int i) {
        asm("@" + segment.memoryLocation("Junit", i));
        asm(segment.usesBasePointer()? "D=M" : "D=A");
        if (segment.usesPointerArithmetic()) {
            asm("@" + i);
            asm("D=D+A");
        }
        popToAddressPointedToByDataRegister();
    }

    public void pushesTempWithLeadingComment(int i) {
        pushesFromWithLeadingCommentGeneric(Segment.TEMP, i);
    }

    private void pushesTempWithoutLeadingComment(int i) {
        pushesFrom(Segment.TEMP, i);
    }

    public void pushesFromThis(int i) {
        pushesFromWithLeadingCommentGeneric(Segment.THIS, i);
    }

    public void pushesFromThat(int i) {
        pushesFromWithLeadingCommentGeneric(Segment.THAT, i);
    }

    public void pushesFromArgument(int i) {
        pushesFromWithLeadingCommentGeneric(Segment.ARGUMENT, i);
    }

    public void pushFromStatic(int i) {
        asm("// push static " + i);
        asm("@" + Segment.STATIC.memoryLocation("Junit", i));
        asm("D=M");
        pushDataRegister();
    }

    private void pushesFromWithLeadingCommentGeneric(Segment segment, int i) {
        asm("// push " + segment.name().toLowerCase() + " " + i);
        pushesFrom(segment, i);
    }

    public void pushesThis() {
        pushesFromWithLeadingCommentGeneric(Segment.POINTER, 0);
    }

    public void pushesThat() {
        pushesFromWithLeadingCommentGeneric(Segment.POINTER, 1);
    }

    public void popsThis() {
        popsToWithLeadingComment(Segment.POINTER, 0);
    }

    public void popsThat() {
        popsToWithLeadingComment(Segment.POINTER, 1);
    }

    public void restoresSegmentPointers() {
        setTmp0ToMemoryOf("LCL");
        popsTmp0StackTo("THAT");
        popsTmp0StackTo("THIS");
        popsTmp0StackTo("ARG");
        popsTmp0StackTo("LCL");
        popsTmp0StackTo("PC");
    }

    private void popsTmp0StackTo(String symbol) {
        asm("@" + symbol);
        asm("D=A");
        popToAddressPointedToByDataRegisterUsingStackPointer("@5");
    }

    private void popToAddressPointedToByDataRegisterUsingStackPointer(String sp) {
        asm("@R13");
        asm("M=D");
        asm(sp);
        asm("AM=M-1");
        asm("D=M");
        asm("@R13");
        asm("A=M");
        asm("M=D");
    }

    private void popToAddressPointedToByDataRegister() {
        popToAddressPointedToByDataRegisterUsingStackPointer("@SP");
    }

    private void setTmp0ToMemoryOf(String symbol) {
        asm("@" + symbol);
        asm("D=M");
        asm("@5");
        asm("M=D");
    }

    void pushesConstant(int i) {
        asm("// push constant " + i);
        pushesConstantWithoutLeadingComment(i);
    }

    private void pushesConstantWithoutLeadingComment(int i) {
        asm("@" + i);
        asm("D=A");
        asm("@SP");
        asm("AM=M+1");
        asm("A=A-1");
        asm("M=D");
    }

    private void pushesFrom(Segment segment, int i) {
        if (segment == Segment.CONSTANT) {
            asm("@" + i);
            asm("D=A");
        } else {
            asm("@" + segment.memoryLocation("Junit", i));
            if (segment.usesPointerArithmetic()) {
                asm(segment.usesBasePointer() ? "D=M" : "D=A");
                asm("@" + i);
                asm("A=D+A");
            }
            asm("D=M");
        }
        pushDataRegister();
    }

    private void pushDataRegister() {
        asm("@SP");
        asm("AM=M+1");
        asm("A=A-1");
        asm("M=D");
    }

    public void adds() {
        asm("// add");
        popsToTempWithoutLeadingComment(0);
        popsToTempWithoutLeadingComment(1);
        asm("@6");
        asm("D=M");
        asm("@5");
        asm("M=D+M");
        pushesTempWithoutLeadingComment(0);
    }

    public void subtracts() {
        asm("// sub");
        popsToTempWithoutLeadingComment(0);
        popsToTempWithoutLeadingComment(1);
        asm("@5");
        asm("D=M");
        asm("@6");
        asm("M=M-D");
        pushesTempWithoutLeadingComment(1);
    }

    public void eq(int opCounter) {
        performsComparisonOperationWithLeadingComment(opCounter, "EQ");
    }

    private void performsComparisonOperationWithLeadingComment(int opCounter, String op) {
        asm("// " + op.toLowerCase());
        performsComparisonOperation(opCounter, op);
    }

    private void performsComparisonOperation(int opCounter, String op) {
        String operator = "-";
        performsOperation(opCounter, op, operator, op);
    }

    private void performsOperation(int opCounter, String opName, String operator, String jumpCondition) {
        assert jumpCondition.length() == 2;
        assert jumpCondition.equals(jumpCondition.toUpperCase());

        popsToTempWithoutLeadingComment(1);
        popsToTempWithoutLeadingComment(0);
        asm("@5");        // x
        asm("D=M");
        asm("@6");        // y
        asm("D=D" + operator + "M");
        asm("@" + opName + opCounter);
        asm("D;J" + jumpCondition);
        asm("D=0");       // x != y
        asm("@" + opName + "END" + opCounter);
        asm("0;JMP");
        asm("(" + opName + opCounter + ")");     // x == y
        asm("D=-1");
        asm("(" + opName + "END" + opCounter + ")");
        asm("@5");        // temp[0] holds result
        asm("M=D");
        pushesTempWithoutLeadingComment(0);
    }

    public void gt(int opCounter) {
        performsComparisonOperationWithLeadingComment(opCounter, "GT");
    }

    public void lt(int opCounter) {
        performsComparisonOperationWithLeadingComment(opCounter, "LT");
    }

    public void neg() {
        unaryOp("neg", "-");
    }

    private void unaryOp(String opName, String operator) {
        asm("// " + opName);
        popsToTempWithoutLeadingComment(0);
        asm("@5");
        asm("M=" + operator + "M");
        pushesTempWithoutLeadingComment(0);
    }

    public void not() {
        unaryOp("not", "!");
    }

    public void and(int opCounter) {
        dualLogicalOp(opCounter, "AND", "&");
    }

    private void dualLogicalOp(int opCounter, String op, String operator) {
        asm("// " + op.toLowerCase());
        popsToTempWithoutLeadingComment(0);
        popsToTempWithoutLeadingComment(1);
        asm("@6");        // x
        asm("D=M");
        asm("@5");        // y
        asm("M=D" + operator + "M");
        pushesTempWithoutLeadingComment(0);
    }

    public void or(int opCounter) {
        dualLogicalOp(opCounter, "OR", "|");
    }

    public void addLine(String line) {
        add(new HackAssemblerCommandsAsserter.HackAssemblerCommandAsserter(size(), line));
    }

    void pushesLocal(int i) {
        final String expectedCode = "// push local " + i;
        asm(expectedCode);
        asm("@LCL");
        asm("D=M");
        asm("@" + i);
        asm("A=D+A");
        asm("D=M");
        asm("@SP");
        asm("AM=M+1");
        asm("A=A-1");
        asm("M=D");
    }

    void asm(String expectedCode) {
        assertTrue(String.format("expect %s at position %s of %s", expectedCode, pos, this), pos < size());
        get(pos++).isCode(expectedCode);
    }

    public void nothingLeft() {
        assertThat("there should be no asm command left", pos, is(equalTo(this.size())));
    }

    public void label(String name) {
        comment("label " + name);
        labelWithoutLeadingComment(name);
    }

    private void labelWithoutLeadingComment(String name) {
        asm(String.format("(%s)", name));
    }

    public void ifGoto(String label) {
        asm("// if-goto " + label);
        popsToTempWithoutLeadingComment(0);
        asm("@5");
        asm("D=M");
        asm("@" + label);
        asm("D;JNE");
    }

    public void goTo(String label) {
        asm("// goto " + label);
        asm("@" + label);
        asm("0;JMP");
    }

    void comment(String expectedComment) {
        asm(String.format("// %s", expectedComment));
    }

    private void comment(String format, Object ... varargs) {
        comment(String.format(format, varargs));
    }

    public void setStandardStackPointerToAddressOfArg1() {
        asm("@ARG");
        asm("A=M+1");
        asm("D=A");
        asm("@SP");
        asm("M=D");
    }

    public void functionStartWithArgCount(String name, int nargs) {
        comment("function %s %s", name, nargs);
        labelWithoutLeadingComment(name);
        for (int i = 0; i < nargs; i++) {
            pushesConstantWithoutLeadingComment(0);
        }
    }

    //
    // https://www.coursera.org/learn/nand2tetris2/lecture/zJVns/unit-2-4-function-call-and-return-implementation-preview (@ 13:58)
    //
    void functionReturn() {
        comment("return");

        // 1. the function's return value is transferred to arg[0]
        popsToWithoutLeadingComment(Segment.ARGUMENT, 0);

        // 3. clear the stack (set it to the correct value from the point of view of the caller)
        //    (needs to be done out of step with 2. because after 2. the ARG pointer will be clobbered.
        setStandardStackPointerToAddressOfArg1();

        // 2. restore the segment pointers of the caller
        restoresSegmentPointers();
    }

    class HackAssemblerCommandAsserter {
        private final int lineNumber;

        private final String code;

        HackAssemblerCommandAsserter(int lineNumber, String code) {
            this.code = code;
            this.lineNumber = lineNumber;
        }
        public void isCode(String expectedCode) {
            assertThat(String.format("code at line number %s of %s", lineNumber, HackAssemblerCommandsAsserter.this), this.code, is(expectedCode));
        }

        public String toString() {
            return lineNumber + ":'" + code + "'\n";
        }
    }
}