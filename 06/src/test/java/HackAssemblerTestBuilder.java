import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class HackAssemblerTestBuilder {
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
