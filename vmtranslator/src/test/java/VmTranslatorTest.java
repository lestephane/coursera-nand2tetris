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
                .ThenTheTranslatedCommandsAre((cmds) -> {
                    cmds.get(0).isCode("// pop local 0");
                    cmds.get(1).isCode("@LCL");
                    cmds.get(2).isCode("D=M");
                    cmds.get(3).isCode("@0");
                    cmds.get(4).isCode("D=D+A");
                    cmds.get(5).isCode("@R13");
                    cmds.get(6).isCode("M=D");
                    cmds.get(7).isCode("@SP");
                    cmds.get(8).isCode("AM=M-1");
                    cmds.get(9).isCode("D=M");
                    cmds.get(10).isCode("@R13");
                    cmds.get(11).isCode("A=M");
                    cmds.get(12).isCode("M=D");
                });
    }

    @Test
    public void popToArgument() {
        GivenSourceCode("pop argument 1")
                .ThenTheTranslatedCommandsAre((cmds) -> {
                    cmds.get(0).isCode("// pop argument 1");
                    cmds.get(1).isCode("@ARG");
                    cmds.get(2).isCode("D=M");
                    cmds.get(3).isCode("@1");
                    cmds.get(4).isCode("D=D+A");
                    cmds.get(5).isCode("@R13");
                    cmds.get(6).isCode("M=D");
                    cmds.get(7).isCode("@SP");
                    cmds.get(8).isCode("AM=M-1");
                    cmds.get(9).isCode("D=M");
                    cmds.get(10).isCode("@R13");
                    cmds.get(11).isCode("A=M");
                    cmds.get(12).isCode("M=D");
                });
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
                Consumer<List<HackAssemblerCommandAsserter>> cmdConsumer) {
            final List<HackAssemblerCommandAsserter> cmds = new ArrayList<>();
            final String output = translatedOutputForInput(input);

            int lineNumber = 1;
            for (String s : List.<String>of(output.split("\n"))) {
                cmds.add(new HackAssemblerCommandAsserter(lineNumber++, s.trim()));
            }

            cmdConsumer.accept(cmds);
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