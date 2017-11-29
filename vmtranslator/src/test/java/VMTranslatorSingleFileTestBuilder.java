import Hack.Assembler.HackAssemblerTranslator;
import Hack.CPUEmulator.CPU;
import Hack.CPUEmulator.CPUEmulator;
import Hack.CPUEmulator.ROM;
import Hack.Controller.ProgramException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Consumer;

class VMTranslatorSingleFileTestBuilder implements VMTranslatorInput {
    public static final String DEFAULT_TEST_COMPILATION_UNIT_NAME = "Junit";
    private String fileName = DEFAULT_TEST_COMPILATION_UNIT_NAME + ".vm";
    private String vmCode;

    public VMTranslatorSingleFileTestBuilder(String vmCode) {
        this.vmCode = vmCode;
    }

    public VMTranslatorSingleFileTestBuilder HavingFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    void ThenTheTranslatedCommandsAre(Consumer<AsmAsserter> asmConsumer) {
        new VMTranslatorAssertThat(this).translatesTo(asmConsumer);
    }

    public String translationUnitName() {
        return fileName.replace(".vm", "");
    }

    public String vmSourceCode() {
        return vmCode;
    }

    public void ThenTheResultingExecutionStateIs(Consumer<CpuAsserter> execConsumer) {
        try(TemporaryAsmFile tmpFile = new TemporaryAsmFile()) {
            final String translatedCode = translate(vmCode);
            Files.write(tmpFile.getFile(), Arrays.asList(translatedCode));
            runProgram(tmpFile, execConsumer);
        } catch (IOException ioe){
            throw new UncheckedIOException(ioe);
        }
    }

    private String translate(String input) {
        return new VMTranslatorAssertThat(this).translate();
    }

    private void runProgram(TemporaryAsmFile tmpFile, Consumer<CpuAsserter> execConsumer) {
        final CPUEmulator emu = new CPUEmulator();
        try {
            final Field cpuField = emu.getClass().getDeclaredField("cpu");
            cpuField.setAccessible(true);
            final CPU cpu = (CPU) cpuField.get(emu);
            final Field romField = cpu.getClass().getDeclaredField("rom");
            romField.setAccessible(true);
            final ROM rom = (ROM) romField.get(cpu);
            final int romSize = rom.getSize();
            final int romContentSize = rom.getContents().length;
            initializeSegmentPointers(cpu);
            rom.loadProgram(tmpFile.getFile().toAbsolutePath().toString());
            while (rom.getValueAt(cpu.getPC().get()) != HackAssemblerTranslator.NOP) {
                cpu.executeInstruction();
            }
            execConsumer.accept(new CpuAsserter(cpu, rom));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        } catch (ProgramException e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeSegmentPointers(CPU cpu) {
        cpu.getRAM().setValueAt(0, (short) 256, true); // stack pointer
        cpu.getRAM().setValueAt(1, (short) 300, true); // base address of the local segment
        cpu.getRAM().setValueAt(2, (short) 400, true); // base address of the argument segment
        cpu.getRAM().setValueAt(3, (short) 3000, true); // base address of the this segment
        cpu.getRAM().setValueAt(4, (short) 3010, true); // base address of the that segment
    }

    public static class TemporaryAsmFile implements AutoCloseable {

        private final Path file;

        public TemporaryAsmFile() throws IOException {
            file = Files.createTempFile(null, ".asm");
        }

        public Path getFile() {
            return file;
        }

        public void close() throws IOException {
            Files.deleteIfExists(file);
        }
    }

    public static class CpuAsserter {
        private final CPU cpu;
        private final ROM rom;
        private final StackAsserter stack;

        public CpuAsserter(CPU cpu, ROM rom) {
            this.cpu = cpu;
            this.rom = rom;
            this.stack = new StackAsserter();
        }

        public StackAsserter stack() {
            return stack;
        }

        public class StackAsserter {
            public short peek() {
                final short stackPointer = cpu.getRAM().getValueAt(0);
                final short stackTop = cpu.getRAM().getValueAt(stackPointer - 1);
                return stackTop;
            }
        }
    }
}