import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HackAssemblerCommandsAsserter extends ArrayList<HackAssemblerCommandsAsserter.HackAssemblerCommandAsserter> {
    private int pos;

    public HackAssemblerCommandsAsserter() {
        this.pos = 0;
    }

    public void popsToLocal(int i) {
        popsToWithLeadingComment(Segment.LOCAL, i);
    }

    public void popsToArgument(int i) {
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
        popsTo(Segment.TEMP, i);
    }

    private void popsToWithLeadingComment(Segment segment, int i) {
        asm("// pop " + segment.name().toLowerCase() + " " + i);
        popsTo(segment, i);
    }

    public void popsToStatic(int i) {
        asm("// pop static " + i);
        asm("@Junit." + i);
        asm("D=A");
        popToAddressPointedToByDataRegister();
    }

    private void popToAddressPointedToByDataRegister() {
        asm("@R13");
        asm("M=D");
        asm("@SP");
        asm("AM=M-1");
        asm("D=M");
        asm("@R13");
        asm("A=M");
        asm("M=D");
    }

    private void popsTo(Segment segment, int i) {
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

    void pushesConstant(int i) {
        asm("// push constant " + i);
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

    private void asm(String expectedCode) {
        get(pos++).isCode(expectedCode);
    }

    public void nothingLeft() {
        assertThat("there should be no asm command left", pos, is(equalTo(this.size())));
    }

    void comment(String expectedComment) {
        asm(expectedComment);
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