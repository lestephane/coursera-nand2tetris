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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

class VMTranslatorInMemoryTestBuilder {
    public static final String DEFAULT_TEST_COMPILATION_UNIT_NAME = "Junit";
    private String fileName = DEFAULT_TEST_COMPILATION_UNIT_NAME + ".vm";
    private String vmCode;

    public VMTranslatorInMemoryTestBuilder(String vmCode) {
        this.vmCode = vmCode;
    }

    public VMTranslatorInMemoryTestBuilder HavingFileName(String fileName) {
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

    public void ThenTheResultingExecutionStateIs(Consumer<CpuAsserter> execConsumer) {
        try(TemporaryAsmFile tmpFile = new TemporaryAsmFile()) {
            String translatedCode = translate();
            Files.write(tmpFile.getFile(), Arrays.asList(translatedCode));
            runProgram(tmpFile, execConsumer);
        } catch (IOException ioe){
            throw new UncheckedIOException(ioe);
        }
    }

    private String translate() {
        return new VMTranslator(source()).translate(unit());
    }

    private void runProgram(TemporaryAsmFile tmpFile, Consumer<CpuAsserter> execConsumer) {
        CPUEmulator emu = new CPUEmulator();
        try {
            Field cpuField = emu.getClass().getDeclaredField("cpu");
            cpuField.setAccessible(true);
            CPU cpu = (CPU) cpuField.get(emu);
            Field romField = cpu.getClass().getDeclaredField("rom");
            romField.setAccessible(true);
            ROM rom = (ROM) romField.get(cpu);
            int romSize = rom.getSize();
            int romContentSize = rom.getContents().length;
            initializeSegmentPointers(cpu);
            rom.loadProgram(tmpFile.getFile().toAbsolutePath().toString());
            while (rom.getValueAt(cpu.getPC().get()) != HackAssemblerTranslator.NOP) {
                System.out.println("executing instruction at PC " + cpu.getPC().get());
                cpu.executeInstruction();
            }
            System.out.println("stopping execution at PC " + cpu.getPC().get());
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
            stack = new StackAsserter();
        }

        public StackAsserter stack() {
            return stack;
        }

        public class StackAsserter {
            public short peek() {
                short stackPointer = pointer();
                short stackTop = cpu.getRAM().getValueAt(stackPointer - 1);
                return stackTop;
            }

            public short pointer() {
                return cpu.getRAM().getValueAt(0);
            }

            public short[] contents() {
                return Arrays.copyOfRange(cpu.getRAM().getContents(), 256, pointer());
            }

            public void contains(int ... values) {
                assertThat(contents(), is(arrayOf(values)));
            }

            private short[] arrayOf(int ... val) {
                short inShort[] = new short[val.length];
                for(int i = 0; i < inShort.length; i++)
                {
                    inShort[i] = (short)val[i];
                }
                return inShort;
            }
        }
    }
}
