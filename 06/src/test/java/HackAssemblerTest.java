import org.junit.Test;

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

        public void ThenTheCompiledCodeIsEmpty() {
            ThenTheCompiledCodeIs();
        }
    }
}