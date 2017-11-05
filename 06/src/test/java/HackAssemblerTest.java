import org.junit.Test;

import java.io.File;
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

    @Test
    public void predefinedSymbolLowRegister() {
        GivenSourceCode(
                "@R0", "@R15", "@SCREEN", "@KBD",
                "@SP", "@LCL", "@ARG", "@THIS", "@THAT")
                .ThenTheCompiledCommandsAre((cmds)-> {
                    cmds.get(0).ainst(0);
                    cmds.get(1).ainst(15);
                    cmds.get(2).ainst(16384);
                    cmds.get(3).ainst(24576);
                    cmds.get(4).ainst(0);
                    cmds.get(5).ainst(1);
                    cmds.get(6).ainst(2);
                    cmds.get(7).ainst(3);
                    cmds.get(8).ainst(4);
                });
    }

    @Test
    public void labelDeclaredBeforeUse() {
        GivenSourceCode("(TOTO)", "@TOTO")
            .ThenTheCompiledCommandsAre((cmds) -> {
                assertThat(cmds.size(), is(1));
                cmds.get(0).ainst(0); // 1 is the program counter of the instruction following (TOTO)
            });
    }

    @Test
    public void labelDeclaredAfterUse() {
        GivenSourceCode("@TOTO", "(TOTO)")
                .ThenTheCompiledCommandsAre((cmds) -> {
                    assertThat(cmds.size(), is(1));
                    cmds.get(0).ainst(1); // 1 is the program counter of the instruction following (TOTO)
                });
    }

    @Test
    public void variable() {
        GivenSourceCode("@firstvar", "@secondvar")
                .ThenTheCompiledCommandsAre((cmds) -> {
                    assertThat(cmds.size(), is(2));
                    cmds.get(0).ainst(16);
                    cmds.get(1).ainst(17);
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