import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

public class TranslatorExecutionTests {

    @Test
    public void ltWhereFirstEqualsSecond() {
        GivenSourceCode("push constant 11", "push constant 11", "lt")
                .ThenTheResultingExecutionStateIs((cpu) -> {
                    assertThat(cpu.stack().peek(), is((short)0));
                });
    }

    private MemoryBasedTranslatorTestBuilder GivenSourceCode(String ... args) {
        return TranslatorTranslationTests.GivenSourceCode(args);
    }

    @Test
    public void ltWhereFirstLessThanSecond() {
        GivenSourceCode("push constant 11", "push constant 22", "lt")
                .ThenTheResultingExecutionStateIs((cpu) -> {
                    cpu.stack().contains(-1);
                });
    }

    @Test
    public void ltWhereFirstGreaterThanSecond() {
        GivenSourceCode("push constant 22", "push constant 11", "lt")
                .ThenTheResultingExecutionStateIs((cpu) -> {
                    cpu.stack().contains(0);
                });
    }

    @Test
    public void callNoArgFunction() {
        GivenSourceCode("call Test 0", "function Test 0")
                .ThenTheResultingExecutionStateIs((state) -> {
                    final short[] stack = state.stack().contents();
                    assertThat("ARG = (SP prior to pushing segment pointers)",
                            state.argSegment().currentValue(), is(state.stack().initialValue()));
                    assertThat("return address pushed", (int)stack[0], greaterThan(0));
                    assertThat("saved LCL pushed", stack[1], is(state.localSegment().initialValue()));
                    assertThat("saved ARG pushed", stack[2], is(state.argSegment().initialValue()));
                    assertThat("saved THIS pushed", stack[3], is(state.thisSegment().initialValue()));
                    assertThat("saved THAT pushed", stack[4], is(state.thatSegment().initialValue()));
                });
    }

    @Test
    public void callOneArgFunction_setsArgPointerCorrectly() {
        GivenSourceCode("push constant 123", "call Test 1", "function Test 0")
                .ThenTheResultingExecutionStateIs((state) -> {
                    assertThat(state.stack().pointer(), is((short)262)); // [<123s>, <return address>, <-1s>, <-2s>, <-3s>, <-4s>]
                    assertThat(state.argSegment().currentValue(), is((short)256));
                });
    }

    /**
     * See https://www.coursera.org/learn/nand2tetris2/lecture/gxfcB/unit-2-6-function-call-and-return-implementation @ 12:13
     */
    @Test
    public void callTwoArgFunction_setsArgPointerCorrectly() {
        GivenSourceCode("push constant 123", "push constant 456", "call Test 2", "function Test 0")
                .ThenTheResultingExecutionStateIs((state) -> {
                    assertThat(state.stack().pointer(), is((short)263)); // [<123s>, <456s>, <return address>, <-1s>, <-2s>, <-3s>, <-4s>]
                    assertThat(state.argSegment().currentValue(), is((short)256));
                    assertThat(state.localSegment().currentValue(), is(state.stack().pointer()));
                });
    }

    @Test
    public void returnFromSimulatedVoidFunctionWithoutArgs() {
        GivenSourceCode("return")
                .AndInitialRam((ram) -> {
                    ram.setValueAt(256, (short) 100, true); // return address
                    ram.setValueAt(257, (short) -1, true);  // saved LCL
                    ram.setValueAt(258, (short) -2, true);  // saved ARG
                    ram.setValueAt(259, (short) -3, true);  // saved THIS
                    ram.setValueAt(260, (short) -4, true);  // saved THAT
                    ram.setValueAt(0, (short) 261, true);   // current SP
                    ram.setValueAt(1, (short) 261, true);   // current LCL = SP
                    ram.setValueAt(2, (short) 256, true);   // current ARG = previous SP
                })
                .ThenTheResultingExecutionStateIs((state) -> {
                    assertThat("return value popped: " + state.dump(), state.memAt(256), is (-4));
                    assertThat("stack SP", state.memAt(0), is(257));
                    assertThat("stack top", state.stack().peek(), is((short)-4));
                    assertThat("return address popped", state.programCounter(), equalTo(100));
                    assertThat("saved LCL popped", state.memAt(1), is(-1));
                    assertThat("saved ARG popped", state.memAt(2), is(-2));
                    assertThat("saved THIS popped", state.memAt(3), is(-3));
                    assertThat("saved THAT popped", state.memAt(4), is(-4));
                });
    }

    @Test
    public void callVoidFunctionNoArgsNoLocals() {
        GivenSourceCode(
                "push constant 11",
                "call Test 0",
                "goto END",
                "function Test 0",
                "return",
                "label END",
                "pop temp 0")
                .ThenTheResultingExecutionStateIs((state) -> {
                    state.stack().contains(11);
                    state.localSegment().hasNotChanged();
                    state.argSegment().hasNotChanged();
                    state.thisSegment().hasNotChanged();
                    state.thatSegment().hasNotChanged();
                });
    }

    @Test
    public void returnFromFunctionWithOneArg() {
        GivenSourceCode("push constant 11", "call Add22 1", "goto END", "function Add22 0", "push argument 0", "push constant 22", "add", "return", "label END")
                .ThenTheResultingExecutionStateIs((cpu) -> {
                    cpu.stack().contains(33);
                });
    }

    @Test
    public void popThis() {
        GivenSourceCode("push constant 11", "pop pointer 0", "goto END", "label END")
                .ThenTheResultingExecutionStateIs((state) -> {
                    assertThat(state.thisSegment().currentValue(), is((short)11));
                });
    }

    @Test
    public void pushThis() {
        GivenSourceCode("push pointer 0", "goto END", "label END")
                .ThenTheResultingExecutionStateIs((state) -> {
                    assertThat(state.stack().peek(), is((short)-3));
                });
    }

    @Test
    public void popThat() {
        GivenSourceCode("push constant 11", "pop pointer 1", "goto END", "label END")
                .ThenTheResultingExecutionStateIs((state) -> {
                    assertThat(state.thatSegment().currentValue(), is((short)11));
                });
    }

    @Test
    public void pushThat() {
        GivenSourceCode("push pointer 1", "goto END", "label END")
                .ThenTheResultingExecutionStateIs((state) -> {
                    assertThat(state.stack().peek(), is((short)-4));
                });
    }

    /**
     * More elaborate tests following the expected state documented at
     * http://www.nand2tetris.org/projects/08/FunctionCalls/NestedCall/NestedCallStack.html
     */
    @Test
    public void testEntryToSysInit() {
        //Bootstrap init is taken care of by default
        GivenSourceDirectory("NestedCall")
                .withFile(f -> f.withName("Sys.vm")
                        .withLines("function Sys.init 0"))
                .ThenTheResultingExecutionStateIs(state -> {
                    assertThat(state.memAt(0), is(261));
                    assertThat(state.memAt(1), is(261));
                    assertThat(state.memAt(2), is(256));
                    assertThat(state.memAt(3), is(-3));
                    assertThat(state.memAt(4), is(-4));
                });
    }

    private VMTranslatorDirectoryTestBuilder GivenSourceDirectory(String name) {
        return new VMTranslatorDirectoryTestBuilder(name);
    }
}