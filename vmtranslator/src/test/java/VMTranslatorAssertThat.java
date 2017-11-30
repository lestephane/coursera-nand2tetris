import java.io.StringWriter;
import java.util.function.Consumer;

class VMTranslatorAssertThat {
    private final VMTranslatorSource input;

    VMTranslatorAssertThat(VMTranslatorSource input) {
        this.input = input;
    }

    void translatesTo(Consumer<AsmAsserter> asmConsumer) {
        final String output = translate();
        final AsmAsserter cmds = new AsmAsserter(output);
        asmConsumer.accept(cmds);
        cmds.nothingLeft();
    }

    String translate() {
        StringWriter output = new StringWriter();
        new VMTranslator(input).translateTo(input.translationUnitName(), output);
        return output.toString();
    }
}