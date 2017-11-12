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
    public void pushConstant() {
        GivenSourceCode("push constant 10")
                .ThenTheTranslatedCommandsAre((cmds) -> {
                    cmds.get(0).isCode("// push constant 10");
                    cmds.get(1).isCode("@10");
                    cmds.get(2).isCode("D=A");
                    cmds.get(3).isCode("@SP");
                    cmds.get(4).isCode("AM=M+1");
                    cmds.get(5).isCode("A=A-1");
                    cmds.get(6).isCode("M=D");
                });
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

    private VmTranslatorTestBuilder GivenSourceCode(String line) {
        return new VmTranslatorTestBuilder(line);
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
        public void popsToLocal(int i) {
            pushesTo("local", "LCL", i);
        }

        public void popsToArgument(int i) {
            pushesTo("argument", "ARG", i);
        }

        private void pushesTo(String segmentName, String segmentSymbol, int i) {
            get(0).isCode("// pop " + segmentName + " " + i);
            get(1).isCode("@" + segmentSymbol);
            get(2).isCode("D=M");
            get(3).isCode("@" + i);
            get(4).isCode("D=D+A");
            get(5).isCode("@R13");
            get(6).isCode("M=D");
            get(7).isCode("@SP");
            get(8).isCode("AM=M-1");
            get(9).isCode("D=M");
            get(10).isCode("@R13");
            get(11).isCode("A=M");
            get(12).isCode("M=D");
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