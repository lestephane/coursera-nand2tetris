import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class VmTranslatorTest {
    private VmTranslatorTestBuilder GivenSourceCode(String line) {
        return new VmTranslatorTestBuilder(line);
    }

    private VmTranslatorTestBuilder GivenSourceCode(String ... lines) {
        return new VmTranslatorTestBuilder(String.join("\n", lines));
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
                    cmds.get(0).isCode("// // comment");
                });
        }

    @Test
    public void pushFromConstant() {
        GivenSourceCode("push constant 1")
                .ThenTheTranslatedCommandsAre((cmds) -> {
                    assertThat(cmds.size(), is(7));
                    cmds.get(0).isCode("// push constant 1");
                    cmds.get(1).isCode("@1");
                    cmds.get(2).isCode("D=A");
                    cmds.get(3).isCode("@SP");
                    cmds.get(4).isCode("AM=M+1");
                    cmds.get(5).isCode("A=A-1");
                    cmds.get(6).isCode("M=D");
                });
    }

    @Test
    public void pushFromLocal() {
        GivenSourceCode("push local 2")
                .ThenTheTranslatedCommandsAre((cmds) -> {
                    cmds.get(0).isCode("// push local 2");
                    cmds.get(1).isCode("@LCL");
                    cmds.get(2).isCode("D=M");
                    cmds.get(3).isCode("@2");
                    cmds.get(4).isCode("A=D+A");
                    cmds.get(5).isCode("D=M");
                    cmds.get(6).isCode("@SP");
                    cmds.get(7).isCode("AM=M+1");
                    cmds.get(8).isCode("A=A-1");
                    cmds.get(9).isCode("M=D");
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
        GivenSourceCode("pop local 0")
                .ThenTheTranslatedCommandsAre((cmds) -> cmds.popsToLocal(0));
    }

    @Test
    public void popToArgument() {
        GivenSourceCode("pop argument 1")
                .ThenTheTranslatedCommandsAre((cmds) -> cmds.popsToArgument(1));
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
}