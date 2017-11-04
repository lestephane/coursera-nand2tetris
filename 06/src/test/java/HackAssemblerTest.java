import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class HackAssemblerTest {
    @Test
    public void empty() {
        GivenSourceCode("")
                .ThenTheCompiledCodeIsEmpty();
    }

    @Test
    public void comment() {
        GivenSourceCode("// comment")
                .ThenTheCompiledCodeIsEmpty();
    }

    @Test
    public void commentAfterInstruction() {
        GivenSourceCode("@1 // comment")
                .ThenTheCompiledCommandIs((cmd) -> cmd.ainst(1));
    }

    @Test
    public void ainstZero() {
        GivenSourceCode("@0")
                .ThenTheCompiledCodeIs("0000000000000000");
    }

    @Test
    public void ainstNonZero() {
        GivenSourceCode("@2")
                .ThenTheCompiledCodeIs("0000000000000010");
    }

    @Test
    public void moreThanOneCommand() {
        GivenSourceCode("@1", "@3")
                .ThenTheCompiledCodeIs(
                        "0000000000000001",
                        "0000000000000011");
    }

    @Test
    public void whitespace() {
        GivenSourceCode(" ")
                .ThenTheCompiledCodeIsEmpty();
    }

    @Test
    public void cinstDsl() {
        GivenCompiledCommand("1110110000010000")
                .Then((cmd) -> {
                    cmd.cinst();
                    cmd.dest("D");
                    cmd.comp("A");
                    cmd.nojump();
                }
        );
    }

    @Test
    public void cinstSimpleComp() {
        GivenSourceCommand("D")
                .ThenTheCompiledCommandIs(cmd -> cmd.comp("D"));
    }

    @Test
    public void cinstCompAndDest() {
        GivenSourceCode("D=A")
                .ThenTheCompiledCodeIs("1110110000010000");
    }

    @Test
    public void cinstCompAndDestUsingTestDsl() {
        GivenSourceCommand("D=A")
                .ThenTheCompiledCommandIs((cmd) -> {
                    cmd.cinst();
                    cmd.dest("D");
                    cmd.comp("A");
                    cmd.nojump();
                });
    }

    @Test
    public void cinstSimpleJump() {
        GivenSourceCommand("0;JMP")
                .ThenTheCompiledCommandIs((cmd) -> {
                    cmd.cinst();
                    cmd.comp("0");
                    cmd.jump();
                });
    }

    @Test
    public void externalSourceFile() {
        GivenSourceFile("add/Add.asm")
                .ThenTheCompiledCommandsAre((cmds) ->{
                    cmds.get(0).ainst(2);
                    cmds.get(1).cinst().comp("A").dest("D");
                    cmds.get(3).cinst().comp("D+A");
                    cmds.get(5).cinst().comp("D").dest("M");
                });
    }

    private HackAssemblerTestBuilder GivenSourceFile(String name) {
        File file = new File(System.getProperty("user.dir"), name);
        return new HackAssemblerTestBuilder(file);
    }

    private HackAssemblerTestBuilder GivenSourceCommand(String input) {
        return new HackAssemblerTestBuilder(input);
    }

    private HackAssemblerTestBuilder GivenSourceCode(String ... input) {
        return new HackAssemblerTestBuilder(String.join("\n", input));
    }

    private class HackAssemblerTestBuilder {
        private final String input;
        private final File inputFile;

        public HackAssemblerTestBuilder(String input) {
            this.input = input;
            this.inputFile = null;
        }

        public HackAssemblerTestBuilder(File inputFile) {
            this.input = null;
            this.inputFile = inputFile;
        }

        public void ThenTheCompiledCodeIs(String ... output) {
            String expected = String.join("\n", output);
            expected = output.length > 0 ? expected + '\n' : expected;
            assertThat(performCompilation(), is(expected));
        }

        public void ThenTheCompiledCommandIs(Consumer<HackCompiledCommandAsserter> consumer) {
            String compiledCommand = performCompilation();
            assertThat("there is only one command in the output", compiledCommand.indexOf("/n"), is(-1));
            consumer.accept(new HackCompiledCommandAsserter(compiledCommand));
        }

        public void ThenTheCompiledCodeIsEmpty() {
            ThenTheCompiledCodeIs();
        }

        public void ThenTheCompiledCommandsAre(Consumer<List<HackCompiledCommandAsserter>> cmds) {
            List<HackCompiledCommandAsserter> compiledCommands = new ArrayList<>();
            String assemblerOutput = performCompilation();
            String[] lines = assemblerOutput.split("\n");
            for (String line: lines) {
                compiledCommands.add(new HackCompiledCommandAsserter(line));
            }
            cmds.accept(compiledCommands);
        }

        private String performCompilation() {
            StringWriter w = new StringWriter();
            try {
                makeHackAssembler().compileTo(w);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return w.toString();
        }

        private HackAssembler makeHackAssembler() {
            if (input != null) {
                return new HackAssembler(input);
            } else {
                return new HackAssembler(inputFile);
            }
        }
    }

    private HackCompiledCommandTestBuilder GivenCompiledCommand(String input) {
        return new HackCompiledCommandTestBuilder(input);
    }

    private static class HackCompiledCommandTestBuilder {
        private final String input;

        public HackCompiledCommandTestBuilder(String input) {
            this.input = input;
        }

        public void Then(Consumer<HackCompiledCommandAsserter> consumer) {
            consumer.accept(new HackCompiledCommandAsserter(input));
        }

    }
}