import java.util.ArrayList;

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
        get(pos++).isCode("// pop " + segment.name().toLowerCase() + " " + i);
        popsTo(segment, i);
    }

    public void popsToStatic(int i) {
        get(pos++).isCode("// pop static " + i);
        get(pos++).isCode("@Junit." + i);
        get(pos++).isCode("D=A");
        popToAddressPointedToByDataRegister();
    }

    private void popToAddressPointedToByDataRegister() {
        get(pos++).isCode("@R13");
        get(pos++).isCode("M=D");
        get(pos++).isCode("@SP");
        get(pos++).isCode("AM=M-1");
        get(pos++).isCode("D=M");
        get(pos++).isCode("@R13");
        get(pos++).isCode("A=M");
        get(pos++).isCode("M=D");
    }

    private void popsTo(Segment segment, int i) {
        get(pos++).isCode("@" + segment.memoryLocation("Junit", i));
        get(pos++).isCode(segment.usesBasePointer()? "D=M" : "D=A");
        if (segment.usesPointerArithmetic()) {
            get(pos++).isCode("@" + i);
            get(pos++).isCode("D=D+A");
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
        get(pos++).isCode("// push static " + i);
        get(pos++).isCode("@" + Segment.STATIC.memoryLocation("Junit", i));
        get(pos++).isCode("D=M");
        pushDataRegister();
    }

    private void pushesFromWithLeadingCommentGeneric(Segment segment, int i) {
        get(pos++).isCode("// push " + segment.name().toLowerCase() + " " + i);
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
        get(0).isCode("// push constant " + i);
        get(1).isCode("@" + i);
        get(2).isCode("D=A");
        get(3).isCode("@SP");
        get(4).isCode("AM=M+1");
        get(5).isCode("A=A-1");
        get(6).isCode("M=D");
    }

    private void pushesFrom(Segment segment, int i) {
        if (segment == Segment.CONSTANT) {
            get(pos++).isCode("@" + i);
            get(pos++).isCode("D=A");
        } else {
            get(pos++).isCode("@" + segment.memoryLocation("Junit", i));
            if (segment.usesPointerArithmetic()) {
                get(pos++).isCode(segment.usesBasePointer() ? "D=M" : "D=A");
                get(pos++).isCode("@" + i);
                get(pos++).isCode("A=D+A");
            }
            get(pos++).isCode("D=M");
        }
        pushDataRegister();
    }

    private void pushDataRegister() {
        get(pos++).isCode("@SP");
        get(pos++).isCode("AM=M+1");
        get(pos++).isCode("A=A-1");
        get(pos++).isCode("M=D");
    }

    public void adds() {
        get(pos++).isCode("// add");
        popsToTempWithoutLeadingComment(0);
        popsToTempWithoutLeadingComment(1);
        get(pos++).isCode("@6");
        get(pos++).isCode("D=M");
        get(pos++).isCode("@5");
        get(pos++).isCode("M=D+M");
        pushesTempWithoutLeadingComment(0);
    }

    public void subtracts() {
        get(pos++).isCode("// sub");
        popsToTempWithoutLeadingComment(0);
        popsToTempWithoutLeadingComment(1);
        get(pos++).isCode("@5");
        get(pos++).isCode("D=M");
        get(pos++).isCode("@6");
        get(pos++).isCode("M=M-D");
        pushesTempWithoutLeadingComment(1);
    }

    public void eq(int opCounter) {
        performsComparisonOperationWithLeadingComment(opCounter, "EQ");
    }

    private void performsComparisonOperationWithLeadingComment(int opCounter, String op) {
        get(pos++).isCode("// " + op.toLowerCase());
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
        get(pos++).isCode("@5");        // x
        get(pos++).isCode("D=M");
        get(pos++).isCode("@6");        // y
        get(pos++).isCode("D=D" + operator + "M");
        get(pos++).isCode("@" + opName + opCounter);
        get(pos++).isCode("D;J" + jumpCondition);
        get(pos++).isCode("D=0");       // x != y
        get(pos++).isCode("@" + opName + "END" + opCounter);
        get(pos++).isCode("0;JMP");
        get(pos++).isCode("(" + opName + opCounter + ")");     // x == y
        get(pos++).isCode("D=-1");
        get(pos++).isCode("(" + opName + "END" + opCounter + ")");
        get(pos++).isCode("@5");        // temp[0] holds result
        get(pos++).isCode("M=D");
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
        get(pos++).isCode("// " + opName);
        popsToTempWithoutLeadingComment(0);
        get(pos++).isCode("@5");
        get(pos++).isCode("M=" + operator + "M");
        pushesTempWithoutLeadingComment(0);
    }

    public void not() {
        unaryOp("not", "!");
    }

    public void and(int opCounter) {
        dualLogicalOp(opCounter, "AND", "&");
    }

    private void dualLogicalOp(int opCounter, String op, String operator) {
        get(pos++).isCode("// " + op.toLowerCase());
        popsToTempWithoutLeadingComment(0);
        popsToTempWithoutLeadingComment(1);
        get(pos++).isCode("@6");        // x
        get(pos++).isCode("D=M");
        get(pos++).isCode("@5");        // y
        get(pos++).isCode("M=D" + operator + "M");
        pushesTempWithoutLeadingComment(0);
    }

    public void or(int opCounter) {
        dualLogicalOp(opCounter, "OR", "|");
    }

    public void addLine(String line) {
        add(new HackAssemblerCommandsAsserter.HackAssemblerCommandAsserter(size(), line));
    }

    void pushesLocal(int i) {
        get(0).isCode("// push local " + i);
        get(1).isCode("@LCL");
        get(2).isCode("D=M");
        get(3).isCode("@" + i);
        get(4).isCode("A=D+A");
        get(5).isCode("D=M");
        get(6).isCode("@SP");
        get(7).isCode("AM=M+1");
        get(8).isCode("A=A-1");
        get(9).isCode("M=D");
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