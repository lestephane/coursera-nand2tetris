import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class AsmAsserter extends ArrayList<AsmAsserter.HackAssemblerCommandAsserter> {
    private int pos;

    public AsmAsserter(String asmCode) {
        for (String s : Arrays.asList(asmCode.split("\n"))) {
            addLine(s.trim());
        }
    }

    public void addLine(String line) {
        add(new AsmAsserter.HackAssemblerCommandAsserter(size(), line));
    }

    public void popsToLocal(int i) {
        popsToWithLeadingComment(Segment.LOCAL, i);
    }

    private void popsToWithLeadingComment(Segment segment, int i) {
        asm("// pop " + segment.name().toLowerCase() + " " + i);
        popsToWithoutLeadingComment(segment, i);
    }

    void asm(String expectedCode) {
        assertTrue(String.format("expect %s at position %s of %s", expectedCode, pos, this), pos < size());
        get(pos++).isCode(expectedCode);
    }

    private void popsToWithoutLeadingComment(Segment segment, int i) {
        asm("@" + segment.memoryLocation(MemoryBasedTranslatorTestBuilder.DEFAULT_TEST_COMPILATION_UNIT_NAME, i));
        asm(segment.usesBasePointer()? "D=M" : "D=A");
        if (segment.usesPointerArithmetic()) {
            asm("@" + i);
            asm("D=D+A");
        }
        popToAddressPointedToByDataRegister();
    }

    private void popToAddressPointedToByDataRegister() {
        popToAddressPointedToByDataRegisterUsingStackPointer("@SP");
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

    public void popsToStatic(int i) {
        asm("// pop static " + i);
        asm("@Junit." + i);
        asm("D=A");
        popToAddressPointedToByDataRegister();
    }

    public void pushesTempWithLeadingComment(int i) {
        pushesFromWithLeadingCommentGeneric(Segment.TEMP, i);
    }

    private void pushesFromWithLeadingCommentGeneric(Segment segment, int i) {
        asm("// push " + segment.name().toLowerCase() + " " + i);
        pushesFrom(segment, i);
    }

    private void pushesFrom(Segment segment, int i) {
        if (segment == Segment.CONSTANT) {
            asm("@" + i);
            asm("D=A");
        } else {
            asm("@" + segment.memoryLocation(MemoryBasedTranslatorTestBuilder.DEFAULT_TEST_COMPILATION_UNIT_NAME, i));
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
        final String compilationUnitName = MemoryBasedTranslatorTestBuilder.DEFAULT_TEST_COMPILATION_UNIT_NAME;
        pushCompilationNameScopedStatic(compilationUnitName, i);
    }

    void pushCompilationNameScopedStatic(String compilationUnitName, int i) {
        asm("// push static " + i);
        asm("@" + Segment.STATIC.memoryLocation(compilationUnitName, i));
        asm("D=M");
        pushDataRegister();
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

    void pushesConstantWithComment(int i) {
        asm("// push constant " + i);
        pushesConstantValue(i);
    }

    private void pushesConstantValue(int i) {
        pushesAt(String.valueOf(i), "A");
    }

    private void pushesAt(String symbolName, String addressingMode) {
        asm("@" + symbolName);
        asm("D=" + addressingMode);
        asm("@SP");
        asm("AM=M+1");
        asm("A=A-1");
        asm("M=D");
    }

    public void pushesPointerValue(String symbolName) {
        pushesAt(symbolName, "M");
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

    public void pushesSymbolAddress(String symbolName) {
        pushesAt(symbolName, "A");
    }

    private void popsToTempWithoutLeadingComment(int i) {
        popsToWithoutLeadingComment(Segment.TEMP, i);
    }

    private void pushesTempWithoutLeadingComment(int i) {
        pushesFrom(Segment.TEMP, i);
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

    public void nothingLeft() {
        assertThat("there should be no asm command left", pos, is(equalTo(this.size())));
    }

    public void label(String name) {
        comment("label " + name);
        labelWithoutLeadingComment(name);
    }

    void comment(String expectedComment) {
        asm(String.format("// %s", expectedComment));
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

    public void definesFunctionWithComment(String name, int nvars) {
        comment("function %s %s", name, nvars);
        labelWithoutLeadingComment(name);

        // sets up the local segment (LCL = SP)
        asm("@SP");
        asm("D=M");
        asm("@LCL");
        asm("M=D");

        // pushes zero for each local var (shifts SP by one for each additional local var)
        for (int i = 0; i < nvars; i++) {
            pushesConstantValue(0);
        }
    }

    public void callsFunctionWithComment(String caller, String callee, int nargs) {
        comment("call " + callee + " " + nargs);
        callsFunction(caller, callee, nargs);
    }

    private void callsFunction(String caller, String callee, int nargs) {
        // Computes ARG pointer and saves it to temp register
        asm("@" + nargs);
        asm("D=A");
        asm("@SP");
        asm("D=M-D");
        asm("@R13");
        asm("M=D");

        // Pushes caller's frame
        final String returnLabel = caller + "$ret.0";
        pushesSymbolAddress(returnLabel);
        pushesPointerValue("LCL");
        pushesPointerValue("ARG");
        pushesPointerValue("THIS");
        pushesPointerValue("THAT");

        // Sets arg pointer
        asm("@R13");
        asm("D=M");
        asm("@ARG");
        asm("M=D");

        // Jumps to callee
        asm("@" + callee);
        asm("0;JMP");

        // Declares label for callee to return to
        asm("(" +returnLabel + ")");
    }

    private void comment(String format, Object ... varargs) {
        comment(String.format(format, varargs));
    }

    // https://www.coursera.org/learn/nand2tetris2/lecture/zJVns/unit-2-4-function-call-and-return-implementation-preview (@ 13:58)
    void returns(int nargs) {
        comment("return");

        //
        // Save return address to @R13
        // otherwise it gets overriden when nargs = 0 (in that case, ARG[0] holds the return address)
        //
        asm("@" + nargs);
        asm("D=A");
        asm("@ARG");
        asm("A=M+D");
        asm("D=M");
        asm("@R13");
        asm("M=D");

        // Save return value (pop from stack) to ARG 0
        popsToWithoutLeadingComment(Segment.ARGUMENT, 0);

        // Restore stack pointer to ARG[1] so that the top of the stack (ARG[0]) is the return value...
        asm("@ARG");
        asm("D=M+1");
        asm("@SP");
        asm("M=D");

        // ... restore other segment pointers as a last step
        popsLclStackTo("THAT");
        popsLclStackTo("THIS");
        popsLclStackTo("ARG");
        popsLclStackTo("LCL");

        // Jump the return address saved earlier in R13
        asm("@R13");
        asm("A=M");
        asm("0;JMP");
    }

    private void popsLclStackTo(String symbol) {
        asm("@" + symbol);
        asm("D=A");
        popToAddressPointedToByDataRegisterUsingStackPointer("@LCL");
    }

    private void popToAddressPointedToByDataRegisterUsingStackPointer(String sp) {
        asm("@R14");
        asm("M=D");
        asm(sp);
        asm("AM=M-1");
        asm("D=M");
        asm("@R14");
        asm("A=M");
        asm("M=D");
    }

    private void jumpsToProgramCounterAddress() {
        asm("@PC");
        asm("A=M");
        asm("0;JMP");
    }

    public void bootstrapsVm(String caller) {
        asm("// bootstrap vm");
        asm("@256");
        asm("D=A");
        asm("@0");
        asm("M=D");
        callsFunction(caller, "Sys.init", 0);
    }

    private void gotoFunction(String function, int narg) {
        asm("@" + function);
        asm("0;JMP");
    }

    class HackAssemblerCommandAsserter {
        private final int lineNumber;

        private final String code;

        HackAssemblerCommandAsserter(int lineNumber, String code) {
            this.code = code;
            this.lineNumber = lineNumber;
        }
        public void isCode(String expectedCode) {
            assertThat(String.format("code at line number %s of %s", lineNumber, AsmAsserter.this), this.code, is(expectedCode));
        }

        public String toString() {
            return lineNumber + ":'" + code + "'\n";
        }
    }
}