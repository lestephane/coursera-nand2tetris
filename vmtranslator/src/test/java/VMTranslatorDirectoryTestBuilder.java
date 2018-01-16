import java.util.function.Consumer;

class VMTranslatorDirectoryTestBuilder {
    private final TestDirectoryBuilder dir;
    private final String name;

    VMTranslatorDirectoryTestBuilder(String name) {
        dir = new TestDirectoryBuilder(name);
        this.name = name;
    }

    VMTranslatorDirectoryTestBuilder withFile(Consumer<TestFileBuilder> consumer) {
        dir.withFile(consumer);
        return this;
    }

    void ThenTheTranslatedCommandsAre(Consumer<AsmAsserter> asm) {
        new VMTranslatorAssertThat(VMTranslatorSource.fromDirectory(dir.build())).translatesTo(asm);
    }

    public void ThenTheResultingExecutionStateIs(Consumer<ExecutionStateAsserter> stateAsserter) {
        final VMTranslatorSource source = VMTranslatorSource.fromDirectory(dir.build());
        new TranslatorExecutor(name).executeTranslation(source, MemoryInitializer.DEFAULT, stateAsserter);
    }
}
