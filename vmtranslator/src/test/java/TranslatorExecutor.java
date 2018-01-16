import Hack.Assembler.HackAssemblerTranslator;
import Hack.CPUEmulator.CPU;
import Hack.CPUEmulator.CPUEmulator;
import Hack.CPUEmulator.ROM;
import Hack.Controller.ProgramException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Collections;
import java.util.function.Consumer;

public class TranslatorExecutor {
    private final String translationUnitName;
    private final ExecutionStateDumper executionStateDumper = new ExecutionStateDumper();

    public TranslatorExecutor(String translationUnitName) {
        this.translationUnitName = translationUnitName;
    }

    public void executeTranslation(VMTranslatorSource source, MemoryInitializer memoryInitializer, Consumer<ExecutionStateAsserter> execConsumer) {
        try (TemporaryAsmFile tmpFile = new TemporaryAsmFile()) {
            String translatedCode = new VMTranslator(source).translate(translationUnitName);
            System.out.println(translatedCode);
            Files.write(tmpFile.getFile(), Collections.singletonList(translatedCode));
            runProgram(memoryInitializer, tmpFile, execConsumer);
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

    private void runProgram(MemoryInitializer ramInitializer, TemporaryAsmFile tmpFile, Consumer<ExecutionStateAsserter> execConsumer) {
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
                System.out.println(ExecutionStateDumper.dumpState(cpu));
                cpu.executeInstruction();
            }
            System.out.println("stopping execution at PC " + cpu.getPC().get());
            execConsumer.accept(cpuAsserter);
        } catch (ReflectiveOperationException | ProgramException e) {
            String msg = ExecutionStateDumper.dumpState(cpu);
            throw new RuntimeException(e.toString() + " " + msg, e);
        }
    }
}