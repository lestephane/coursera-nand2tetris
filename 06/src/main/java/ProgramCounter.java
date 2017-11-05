public class ProgramCounter {
    private int pc = 0;

    public void increment() {
        pc ++;
    }

    public int value() {
        return pc;
    }

    public void reset() {
        pc = 0;
    }
}
