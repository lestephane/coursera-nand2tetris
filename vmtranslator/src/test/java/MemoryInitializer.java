import Hack.CPUEmulator.RAM;

interface MemoryInitializer {
    MemoryInitializer DEFAULT = ram -> {
        ram.setValueAt(0, (short) 256, true); // stack pointer
        ram.setValueAt(1, (short) -1, true); // base address of the local segment
        ram.setValueAt(2, (short) -2, true); // base address of the argument segment
        ram.setValueAt(3, (short) -3, true); // base address of the this segment
        ram.setValueAt(4, (short) -4, true); // base address of the that segment
    };

    void initializeRam(RAM ram);
}