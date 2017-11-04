import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class HackCompiledCommandAsserter {
    private final HackCompiledCommand cmd;

    public HackCompiledCommandAsserter(String input) {
        this.cmd = new HackCompiledCommand(input);
    }

    public HackCompiledCommandAsserter ainst(int value) {
        assertTrue("ainst", cmd.ainst());
        assertThat(cmd.ainstValue(), is(value));
        return this;
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
