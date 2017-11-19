import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class VMTranslatorTest {
    private VMTranslatorTestBuilder GivenSourceCode(String line) {
        return new VMTranslatorTestBuilder(line);
    }

    private VMTranslatorTestBuilder GivenSourceCode(String ... lines) {
        return new VMTranslatorTestBuilder(String.join("\n", lines));
    }

    @Test
    public void emptyFile() {
        GivenSourceCode("")
                .ThenTheTranslatedOutputIs("");
    }

    @Test
    public void comment() {
        GivenSourceCode("// comment")
                .ThenTheTranslatedCommandsAre((cmds) -> {
                    cmds.comment("// comment");
                });
        }

    @Test
    public void pushConstantTddStyle() {
        GivenSourceCode("push constant 123")
                .ThenTheTranslatedCommandsAre((asm) -> {
                    asm.pushesConstant(123);
                });
    }

    @Test
    public void pushConstantSpecStyle() {
        GivenSourceCode("push constant 123")
                .ThenTheResultingExecutionStateIs((cpu) -> {
                    assertThat(cpu.stack().peek(), is((short)123));
                });
    }

    @Test
    public void pushFromLocal() {
        GivenSourceCode("push local 456")
                .ThenTheTranslatedCommandsAre((cmds) -> {
                    cmds.pushesLocal(456);
                });
    }

    @Test
    public void pushFromTemp() {
        GivenSourceCode("push temp 2")
                .ThenTheTranslatedCommandsAre((cmds) -> cmds.pushesTempWithLeadingComment(2));
    }

    @Test
    public void pushFromThis() {
        GivenSourceCode("push this 3")
                .ThenTheTranslatedCommandsAre((cmds) -> cmds.pushesFromThis(3));
    }

    @Test
    public void pushFromThat() {
        GivenSourceCode("push that 4")
                .ThenTheTranslatedCommandsAre((cmds) -> cmds.pushesFromThat(4));
    }

    @Test
    public void pushFromArgument() {
        GivenSourceCode("push argument 5")
                .ThenTheTranslatedCommandsAre((cmds) -> cmds.pushesFromArgument(5));
    }

    @Test
    public void popToLocal() {
        GivenSourceCode("pop local 0", "pop local 1 // ignore this")
                .ThenTheTranslatedCommandsAre((cmds) -> {
                    cmds.popsToLocal(0);
                    cmds.popsToLocal(1);
                });
    }

    @Test
    public void popToArgument() {
        GivenSourceCode("pop argument 1")
                .ThenTheTranslatedCommandsAre((cmds) -> cmds.popsStandardStackToArgAt(1));
    }

    @Test
    public void popToThis() {
        GivenSourceCode("pop this 2")
                .ThenTheTranslatedCommandsAre((cmds) -> cmds.popsToThis(2));
    }

    @Test
    public void popToThat() {
        GivenSourceCode("pop that 3")
                .ThenTheTranslatedCommandsAre((cmds) -> cmds.popsToThat(3));
    }

    @Test
    public void popToTemp() {
        GivenSourceCode("pop temp 4")
                .ThenTheTranslatedCommandsAre((cmds) -> cmds.popsToTemp(4));
    }

    @Test
    public void add() {
        GivenSourceCode("add")
                .ThenTheTranslatedCommandsAre((cmds) -> cmds.adds());
    }

    @Test
    public void sub() {
        GivenSourceCode("sub")
                .ThenTheTranslatedCommandsAre((cmds) -> cmds.subtracts());
    }

    @Test
    public void eq() {
        GivenSourceCode("eq", "eq")
                .ThenTheTranslatedCommandsAre((cmds) -> {
            cmds.eq(0);
            cmds.eq(1);
        });
    }

    @Test
    public void gt() {
        GivenSourceCode("gt", "gt")
                .ThenTheTranslatedCommandsAre((cmds) -> {
                    cmds.gt(0);
                    cmds.gt(1);
                });
    }

    @Test
    public void lt() {
        GivenSourceCode("lt", "lt")
                .ThenTheTranslatedCommandsAre((cmds) -> {
                    cmds.lt(0);
                    cmds.lt(1);
                });
    }


    @Test
    public void unaryOperations() {
        GivenSourceCode("neg", "not")
                .ThenTheTranslatedCommandsAre((cmds) -> {
                    cmds.neg();
                    cmds.not();
                });
    }

    @Test
    public void logicalOperations() {
        GivenSourceCode("and", "or")
                .ThenTheTranslatedCommandsAre((cmds) -> {
                    cmds.and(0);
                    cmds.or(1);
                });
    }

    @Test
    public void popToStatic() {
        GivenSourceCode("pop static 123")
                .ThenTheTranslatedCommandsAre((cmds) -> {
                    cmds.popsToStatic(123);
                });
    }

    @Test
    public void pushStatic() {
        GivenSourceCode("push static 123")
                .ThenTheTranslatedCommandsAre((cmds) -> {
                    cmds.pushFromStatic(123);
                });
    }

    @Test
    public void pushPointer() {
        GivenSourceCode("push pointer 0", "push pointer 1")
                .ThenTheTranslatedCommandsAre((asm) -> {
                    asm.pushesThis();
                    asm.pushesThat();
                });
    }

    @Test
    public void popPointer() {
        GivenSourceCode("pop pointer 1", "pop pointer 0")
                .ThenTheTranslatedCommandsAre((cmds) -> {
                    cmds.popsThat();
                    cmds.popsThis();
                });
    }

    @Test
    public void label() {
        GivenSourceCode("label L1", "if-goto L2", "goto L3")
                .ThenTheTranslatedCommandsAre((asm) -> {
                    asm.label("L1");
                    asm.ifGoto("L2");
                    asm.goTo("L3");
                });
    }

    @Test
    public void emptyFunction() {
        GivenSourceCode("function SimpleFunction.test 3", "return")
                .ThenTheTranslatedCommandsAre((asm) -> {
                    asm.functionStartWithArgCount("SimpleFunction.test", 3);
                    asm.functionReturn();
                });
    }

}