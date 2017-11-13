import org.junit.Test;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class VmTranslatorTest {
    @Test
    public void emptyFile() {
        GivenSourceCode("")
                .ThenTheTranslatedOutputIs("");
    }

    @Test
    public void comment() {
        GivenSourceCode("// comment")
                .ThenTheTranslatedCommandsAre((cmds) -> {
                    cmds.get(0).isCode("// // comment");
                });
        }

    @Test
    public void pushFromConstant() {
        GivenSourceCode("push constant 1")
                .ThenTheTranslatedCommandsAre((cmds) -> {
                    assertThat(cmds.size(), is(7));
                    cmds.get(0).isCode("// push constant 1");
                    cmds.get(1).isCode("@1");
                    cmds.get(2).isCode("D=A");
                    cmds.get(3).isCode("@SP");
                    cmds.get(4).isCode("AM=M+1");
                    cmds.get(5).isCode("A=A-1");
                    cmds.get(6).isCode("M=D");
                });
    }

    @Test
    public void pushFromLocal() {
        GivenSourceCode("push local 2")
                .ThenTheTranslatedCommandsAre((cmds) -> {
                    cmds.get(0).isCode("// push local 2");
                    cmds.get(1).isCode("@LCL");
                    cmds.get(2).isCode("D=M");
                    cmds.get(3).isCode("@2");
                    cmds.get(4).isCode("A=D+A");
                    cmds.get(5).isCode("D=M");
                    cmds.get(6).isCode("@SP");
                    cmds.get(7).isCode("AM=M+1");
                    cmds.get(8).isCode("A=A-1");
                    cmds.get(9).isCode("M=D");
                });
    }

    @Test
    public void pushFromTemp() {
        GivenSourceCode("push temp 2")
                .ThenTheTranslatedCommandsAre((cmds) -> cmds.pushesTempWithLeadingComment(2));
    }

    @Test
    public void pushFromThis() {
        GivenSourceCode("push this 3")
                .ThenTheTranslatedCommandsAre((cmds) -> cmds.pushesFromThis(3));
    }

    @Test
    public void pushFromThat() {
        GivenSourceCode("push that 4")
                .ThenTheTranslatedCommandsAre((cmds) -> cmds.pushesFromThat(4));
    }

    @Test
    public void pushFromArgument() {
        GivenSourceCode("push argument 5")
                .ThenTheTranslatedCommandsAre((cmds) -> cmds.pushesFromArgument(5));
    }

    @Test
    public void popToLocal() {
        GivenSourceCode("pop local 0")
                .ThenTheTranslatedCommandsAre((cmds) -> cmds.popsToLocal(0));
    }

    @Test
    public void popToArgument() {
        GivenSourceCode("pop argument 1")
                .ThenTheTranslatedCommandsAre((cmds) -> cmds.popsToArgument(1));
    }

    @Test
    public void popToThis() {
        GivenSourceCode("pop this 2")
                .ThenTheTranslatedCommandsAre((cmds) -> cmds.popsToThis(2));
    }

    @Test
    public void popToThat() {
        GivenSourceCode("pop that 3")
                .ThenTheTranslatedCommandsAre((cmds) -> cmds.popsToThat(3));
    }

    @Test
    public void popToTemp() {
        GivenSourceCode("pop temp 4")
                .ThenTheTranslatedCommandsAre((cmds) -> cmds.popsToTemp(4));
    }

    @Test
    public void add() {
        GivenSourceCode("add")
                .ThenTheTranslatedCommandsAre((cmds) -> cmds.adds());
    }

    @Test
    public void sub() {
        GivenSourceCode("sub")
                .ThenTheTranslatedCommandsAre((cmds) -> cmds.subtracts());
    }

    @Test
    public void eq() {
        GivenSourceCode("eq", "eq")
                .ThenTheTranslatedCommandsAre((cmds) -> {
            cmds.testsForEquality(0);
            cmds.testsForEquality(1);
        });
    }

    private VmTranslatorTestBuilder GivenSourceCode(String line) {
        return new VmTranslatorTestBuilder(line);
    }

    private VmTranslatorTestBuilder GivenSourceCode(String ... lines) {
        return new VmTranslatorTestBuilder(String.join("\n", lines));
    }

    private class VmTranslatorTestBuilder {

        private final String input;

        public VmTranslatorTestBuilder(String input) {
            this.input = input;
        }

        public void ThenTheTranslatedOutputIs(String output) {
            assertThat(translatedOutputForInput(input), is(output));
        }

        private String translatedOutputForInput(String input) {
            StringWriter output = new StringWriter();
            new VmTranslator(input).translateTo(output);
            return output.toString();
        }

        private void ThenTheTranslatedCommandsAre(
                Consumer<HackAssemblerCommandsAsserter> cmdConsumer) {
            final HackAssemblerCommandsAsserter cmds = new HackAssemblerCommandsAsserter();
            final String output = translatedOutputForInput(input);

            int lineNumber = 1;
            for (String s : List.<String>of(output.split("\n"))) {
                cmds.add(new HackAssemblerCommandAsserter(lineNumber++, s.trim()));
            }

            cmdConsumer.accept(cmds);
        }
    }

    private class HackAssemblerCommandsAsserter extends ArrayList<HackAssemblerCommandAsserter> {
        private int pos;
        private int eqTestCount;

        public HackAssemblerCommandsAsserter() {
            this.pos = 0;
            this.eqTestCount = 0;
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

        private void popsTo(Segment segment, int i) {
            get(pos++).isCode("@" + segment.symbol());
            get(pos++).isCode(segment.usesBasePointer()? "D=M" : "D=A");
            get(pos++).isCode("@" + i);
            get(pos++).isCode("D=D+A");
            get(pos++).isCode("@R13");
            get(pos++).isCode("M=D");
            get(pos++).isCode("@SP");
            get(pos++).isCode("AM=M-1");
            get(pos++).isCode("D=M");
            get(pos++).isCode("@R13");
            get(pos++).isCode("A=M");
            get(pos++).isCode("M=D");
        }

        public void pushesTempWithLeadingComment(int i) {
            pushesFromWithLeadingComment(Segment.TEMP, i);
        }

        private void pushesTempWithoutLeadingComment(int i) {
            pushesFrom(Segment.TEMP, i);
        }

        public void pushesFromThis(int i) {
            pushesFromWithLeadingComment(Segment.THIS, i);
        }

        public void pushesFromThat(int i) {
            pushesFromWithLeadingComment(Segment.THAT, i);
        }

        public void pushesFromArgument(int i) {
            pushesFromWithLeadingComment(Segment.ARGUMENT, i);
        }

        private void pushesFromWithLeadingComment(Segment segment, int i) {
            get(pos++).isCode("// push " + segment.name().toLowerCase() + " " + i);
            pushesFrom(segment, i);
        }

        private void pushesFrom(Segment segment, int i) {
            get(pos++).isCode("@" + segment.symbol());
            get(pos++).isCode(segment.usesBasePointer()? "D=M" : "D=A");
            get(pos++).isCode("@" + i);
            get(pos++).isCode("A=D+A");
            get(pos++).isCode("D=M");
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

        @Test
        public void testsForEquality(int eqCounter) {
            get(pos++).isCode("// eq");
            popsToTempWithoutLeadingComment(1);
            popsToTempWithoutLeadingComment(0);
            get(pos++).isCode("@5");        // x
            get(pos++).isCode("D=M");
            get(pos++).isCode("@6");        // y
            get(pos++).isCode("D=D-M");
            get(pos++).isCode("@EQ" + eqCounter);
            get(pos++).isCode("D;JEQ");
            get(pos++).isCode("D=0");       // x != y
            get(pos++).isCode("@EQEND" + eqCounter);
            get(pos++).isCode("0;JMP");
            get(pos++).isCode("(EQ" + eqCounter + ")");     // x == y
            get(pos++).isCode("D=1");
            get(pos++).isCode("(EQEND" + eqCounter + ")");
            get(pos++).isCode("@5");        // temp[0] holds result
            get(pos++).isCode("M=D");
            pushesTempWithoutLeadingComment(0);
            eqTestCount ++;
        }
    }

    private class HackAssemblerCommandAsserter {
        private final int lineNumber;

        private final String code;

        private HackAssemblerCommandAsserter(int lineNumber, String code) {
            this.code = code;
            this.lineNumber = lineNumber;
        }
        public void isCode(String expectedCode) {
            assertThat("code at line number " + lineNumber, this.code, is(expectedCode));
        }
    }
}