import Hack.CPUEmulator.CPU;

public class ExecutionStateDumper {
    static String dumpState(CPU cpu) {
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
}