import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertThat;

public class AssignerTest {
    @Test
    public void builtinToBuiltin() {
        assertThat(
                assign().fromA().toD().toStrings(),
                is("D=A"));
    }

    private Assigner assign() {
        return new Assigner();
    }

    private Matcher<String[]> is(String ... s) {
        return CoreMatchers.is(s);
    }

    @Test
    public void memToBuiltin() {
        assertThat(
                assign(Register.D)
                        .to(Register.D2).toStrings(),
                is("@R13", "M=D"));
    }

    private Assigner assign(Register r) {
        return Assigner.assign(r);
    }

    @Test
    public void builtinToMem() {
        assertThat(
                assign(Register.D3)
                        .to(Register.D).toStrings(),
                is("@R14", "D=M"));
    }

    @Test
    public void memToMem() {
        assertThat(
                assign(Register.D4).to(Register.D2).toStrings(),
                is("@R15", "D=M", "@R13", "M=D"));
    }

    @Test
    public void selfToself() {
        assertThat(
                assign(Register.D).to(Register.D).toStrings(),
                is(/* empty array */));
    }

    @Test
    public void memoryToSegmentPointer() {
        assertThat(
                assign(Register.D2).to(Register.ARG).toStrings(),
                is("@R13", "D=M", "@ARG", "M=D"));
    }
}