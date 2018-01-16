import Hack.Assembler.HackAssemblerTranslator;
import Hack.CPUEmulator.CPU;
import Hack.CPUEmulator.CPUEmulator;
import Hack.CPUEmulator.RAM;
import Hack.CPUEmulator.ROM;
import Hack.Controller.ProgramException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

class VMTranslatorInMemoryTestBuilder {
    public static final String DEFAULT_TEST_COMPILATION_UNIT_NAME = "Junit";
    private String fileName = DEFAULT_TEST_COMPILATION_UNIT_NAME + ".vm";
    private String vmCode;
    private RamInitializer ramInitializer = this::initializeSegmentPointers;

    VMTranslatorInMemoryTestBuilder(String vmCode) {
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

    public void ThenTheResultingExecutionStateIs(Consumer<ExecutionStateAsserter> execConsumer) {
        try(TemporaryAsmFile tmpFile = new TemporaryAsmFile()) {
            String translatedCode = translate();
            System.out.println(translatedCode);
            Files.write(tmpFile.getFile(), Collections.singletonList(translatedCode));
            runProgram(ramInitializer, tmpFile, execConsumer);
        } catch (IOException ioe){
            throw new UncheckedIOException(ioe);
        }
    }

    private String translate() {
        return new VMTranslator(source()).translate(unit());
    }

    private void runProgram(RamInitializer ramInitializer, TemporaryAsmFile tmpFile, Consumer<ExecutionStateAsserter> execConsumer) {
        CPUEmulator emu = new CPUEmulator();
        CPU cpu = null;
        try {
            Field cpuField = emu.getClass().getDeclaredField("cpu");
            cpuField.setAccessible(true);
            cpu = (CPU) cpuField.get(emu);
            Field romField = cpu.getClass().getDeclaredField("rom");
            romField.setAccessible(true);
            ROM rom = (ROM) romField.get(cpu);
            ramInitializer.initializeRam(cpu.getRAM());
            ExecutionStateAsserter cpuAsserter = new ExecutionStateAsserter(cpu);
            rom.loadProgram(tmpFile.getFile().toAbsolutePath().toString());
            while (rom.getValueAt(cpu.getPC().get()) != HackAssemblerTranslator.NOP) {
                System.out.println(dumpState(cpu));
                cpu.executeInstruction();
            }
            System.out.println("stopping execution at PC " + cpu.getPC().get());
            execConsumer.accept(cpuAsserter);
        } catch (ReflectiveOperationException | ProgramException e) {
            String msg = dumpState(cpu);
            throw new RuntimeException(e.toString() + " " + msg, e);
        }
    }

    private static String dumpState(CPU cpu) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("PC=%1$-3s", cpu.getPC().get()));
        sb.append(String.format(" A=%1$-3s", cpu.getA().get()));
        sb.append(String.format(" D=%1$-3s", cpu.getD().get()));
        sb.append(String.format(" SP=%1$-3s", cpu.getRAM().getValueAt(0)));
        sb.append(String.format(" LCL=%1$-3s", cpu.getRAM().getValueAt(1)));
        sb.append(String.format(" ARG=%1$-3s", cpu.getRAM().getValueAt(2)));
        sb.append(String.format(" THIS=%s", cpu.getRAM().getValueAt(3)));
        sb.append(String.format(" THAT=%s", cpu.getRAM().getValueAt(4)));
        sb.append(String.format(" R13=%1$-3s", cpu.getRAM().getValueAt(13)));
        sb.append(String.format(" R14=%1$-3s", cpu.getRAM().getValueAt(14)));
        sb.append('>');
        for (int i = 256, sp = cpu.getRAM().getValueAt(0); i < sp; i++) {
            sb.append(String.format(" [%1$-3s]=%2$-3s", i, cpu.getRAM().getValueAt(i)));
        }
        sb.append('<');
        return sb.toString();
    }

    private void initializeSegmentPointers(RAM ram) {
        ram.setValueAt(0, (short) 256, true); // stack pointer
        ram.setValueAt(1, (short) -1, true); // base address of the local segment
        ram.setValueAt(2, (short) -2, true); // base address of the argument segment
        ram.setValueAt(3, (short) -3, true); // base address of the this segment
        ram.setValueAt(4, (short) -4, true); // base address of the that segment
    }

    public VMTranslatorInMemoryTestBuilder AndInitialRam(RamInitializer ram) {
        this.ramInitializer = ram;
        return this;
    }

    public interface RamInitializer {
        void initializeRam(RAM ram);
    }

    public static class TemporaryAsmFile implements AutoCloseable {

        private final Path file;

        TemporaryAsmFile() throws IOException {
            file = Files.createTempFile(null, ".asm");
        }

        Path getFile() {
            return file;
        }

        public void close() throws IOException {
            Files.deleteIfExists(file);
        }
    }

    public static class ExecutionStateAsserter {
        private final CPU cpu;
        private final StackAsserter stack;
        private final SegmentPointerAsserter localSegment;
        private final SegmentPointerAsserter argSegment;
        private final SegmentPointerAsserter thisSegment;
        private final SegmentPointerAsserter thatSegment;

        ExecutionStateAsserter(CPU aCpu) {
            cpu = aCpu;
            stack = new StackAsserter();
            localSegment = new SegmentPointerAsserter("LCL", 1);
            argSegment = new SegmentPointerAsserter("ARG", 2);
            thisSegment = new SegmentPointerAsserter("THIS", 3);
            thatSegment = new SegmentPointerAsserter("THAT", 4);
        }

        public StackAsserter stack() {
            return stack;
        }

        public SegmentPointerAsserter localSegment() {
            return localSegment;
        }

        public SegmentPointerAsserter argSegment() {
            return argSegment;
        }

        public SegmentPointerAsserter thisSegment() {
            return thisSegment;
        }

        public SegmentPointerAsserter thatSegment() {
            return thatSegment;
        }

        public int programCounter() {
            return cpu.getPC().get();
        }

        public int memAt(int pos) {
            return cpu.getRAM().getValueAt(pos);
        }

        public String dump() {
            return dumpState(cpu);
        }

        public class StackAsserter extends SegmentPointerAsserter {
            StackAsserter() {
                super("SP", 0);
            }

            public short peek() {
                short stackPointer = pointer();
                return cpu.getRAM().getValueAt(stackPointer - 1);
            }

            short pointer() {
                return cpu.getRAM().getValueAt(0);
            }

            public void contains(int ... values) {
                assertThat(dump(), contents(), is(arrayOf(values)));
            }

            public short[] contents() {
                return Arrays.copyOfRange(cpu.getRAM().getContents(), 256, pointer());
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

        class SegmentPointerAsserter {
            private final String name;
            private final int ptr;
            private final short initialValue;

            SegmentPointerAsserter(String aName, int aPtr) {
                name = aName;
                ptr = aPtr;
                initialValue = currentValue();
            }

            public short currentValue() {
                return cpu.getRAM().getValueAt(ptr);
            }

            public void hasNotChanged() {
                assertThat(name + " segment pointer has not changed", currentValue(), is(initialValue));
            }

            public short initialValue() {
                return initialValue;
            }
        }
    }
}