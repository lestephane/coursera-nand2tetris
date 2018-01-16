import java.util.function.Consumer;

class MemoryBasedTranslatorTestBuilder {
    public static final String DEFAULT_TEST_COMPILATION_UNIT_NAME = "Junit";
    private String fileName = DEFAULT_TEST_COMPILATION_UNIT_NAME + ".vm";
    private String vmCode;
    private MemoryInitializer ramInitializer = MemoryInitializer.DEFAULT;

    MemoryBasedTranslatorTestBuilder(String vmCode) {
        this.vmCode = vmCode;
    }

    public MemoryBasedTranslatorTestBuilder HavingFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    void ThenTheTranslatedCommandsAre(Consumer<AsmAsserter> asmConsumer) {
        new VMTranslatorAssertThat(source()).translatesTo(asmConsumer);
    }

    private VMTranslatorSource source() {
        String unitName = unit();
        return VMTranslatorSource.fromString(unitName, vmCode);
    }

    private String unit() {
        return fileName.replace(".vm", "");
    }

    public void ThenTheResultingExecutionStateIs(Consumer<ExecutionStateAsserter> execConsumer) {
        new TranslatorExecutor(unit()).executeTranslation(source(), ramInitializer, execConsumer);
    }

    public MemoryBasedTranslatorTestBuilder AndInitialRam(MemoryInitializer ram) {
        this.ramInitializer = ram;
        return this;
    }
}