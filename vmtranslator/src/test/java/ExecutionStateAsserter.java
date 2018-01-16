import Hack.CPUEmulator.CPU;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ExecutionStateAsserter {
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

    String dump() {
        return ExecutionStateDumper.dumpState(cpu);
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
            assertThat(ExecutionStateDumper.dumpState(cpu), contents(), is(arrayOf(values)));
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
