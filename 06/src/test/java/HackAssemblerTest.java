import org.junit.Ignore;
import org.junit.Test;

import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

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

    private HackAssemblerTestBuilder GivenSourceCommand(String input) {
        return new HackAssemblerTestBuilder(input);
    }

    private HackAssemblerTestBuilder GivenSourceCode(String ... input) {
        return new HackAssemblerTestBuilder(String.join("\n", input));
    }

    private class HackAssemblerTestBuilder {
        private final String input;

        public HackAssemblerTestBuilder(String input) {
            this.input = input;
        }

        public void ThenTheCompiledCodeIs(String ... output) {
            String expected = String.join("\n", output);
            expected = output.length > 0 ? expected + '\n' : expected;
            assertThat(new HackAssembler(input).compile(), is(expected));
        }

        public void ThenTheCompiledCommandIs(Consumer<HackCompiledCommandTestBuilder.HackCompiledCommandAsserter> consumer) {
            String compiledCommand = new HackAssembler(input).compile();
            assertThat("there is only one command in the output", compiledCommand.indexOf("/n"), is(-1));
            consumer.accept(new HackCompiledCommandTestBuilder.HackCompiledCommandAsserter(compiledCommand));
        }

        public void ThenTheCompiledCodeIsEmpty() {
            ThenTheCompiledCodeIs();
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

        private static class HackCompiledCommand {
            private final String cmd;

            enum Bits {
                cinst,reserved1,reserved2,a,c1,c2,c3,c4,c5,c6,d1,d2,d3,j1,j2,j3,crlf
            }

            public HackCompiledCommand(String cmd) {
                this.cmd = cmd;
            }

            public boolean cinst() {
                return isSet(Bits.cinst) & isSet(Bits.reserved1) & isSet(Bits.reserved2);
            }

            private boolean isSet(Bits position) {
                return '1' == cmd.charAt(position.ordinal());
            }

            public String destDecodedString() {
                return  (isSet(Bits.d1)? "A" : "") +
                        (isSet(Bits.d2)? "D" : "") +
                        (isSet(Bits.d3)? "M" : "");
            }

            public String compDecodedString() {
                switch (compBinaryString()) {
                    case "101010": return "0";
                    case "111111": return "1";
                    case "111010": return "-1";
                    case "001100": return "D";
                    case "110000": return isSet(Bits.a)? "M": "A";
                    case "001101": return "!D";
                    case "110001": return isSet(Bits.a)? "!M": "!A";
                    case "001111": return "-D";
                    case "110011": return isSet(Bits.a)? "-M": "-A";
                    case "011111": return "D+1";
                    case "110111": return isSet(Bits.a)? "M+1": "A+1";
                    case "001110": return "D-1";
                    case "110010": return isSet(Bits.a)? "M-1": "A-1";
                    case "000010": return isSet(Bits.a)? "D+M": "D+A";
                    case "010011": return isSet(Bits.a)? "D-M": "D-A";
                    case "000111": return isSet(Bits.a)? "M-D": "A-D";
                    case "000000": return isSet(Bits.a)? "D&M": "D&A";
                    case "010101": return isSet(Bits.a)? "D|M": "D|A";
                }
                return null;
            }

            private String compBinaryString() {
                return cmd.substring(Bits.c1.ordinal(), Bits.d1.ordinal());
            }

            public String jmpDecodedString() {
                switch(jmpBinaryString()) {
                    case "000": return null;
                    case "001": return "JGT";
                    case "010": return "JEQ";
                    case "011": return "JGE";
                    case "100": return "JLT";
                    case "101": return "JNE";
                    case "110": return "JLE";
                    case "111": return "JMP";
                }
                assert false : jmpBinaryString();
                return "XXX";
            }

            private String jmpBinaryString() {
                return cmd.substring(Bits.j1.ordinal(), Bits.crlf.ordinal());
            }


            @Override
            public String toString() {
                return "HackCompiledCommand{" +
                        "cmd='" + cmd + '\'' +
                        '}';
            }
        }

        private static class HackCompiledCommandAsserter {
            private final HackCompiledCommand cmd;

            public HackCompiledCommandAsserter(String input) {
                this.cmd = new HackCompiledCommand(input);
            }

            public HackCompiledCommandAsserter cinst() {
                assertTrue("cinst", cmd.cinst());
                return this;
            }

            public HackCompiledCommandAsserter dest(String expectedDest) {
                assertThat(String.format("dest(%s)", cmd), cmd.destDecodedString(), is(expectedDest));
                return this;
            }

            public HackCompiledCommandAsserter comp(String expectedComp) {
                assertThat(String.format("comp(%s)", cmd), cmd.compDecodedString(), is(expectedComp));
                return this;
            }

            public HackCompiledCommandAsserter nojump() {
                assertThat("no jmp", cmd.jmpDecodedString(), is(nullValue()));
                return this;
            }

            public HackCompiledCommandAsserter jump() {
                assertThat("jmp", cmd.jmpDecodedString(), is("JMP"));
                return this;
            }
        }
    }
}