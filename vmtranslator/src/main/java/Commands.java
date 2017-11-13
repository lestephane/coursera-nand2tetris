import java.io.IOException;

public class Commands {
    public interface Command {

        void translateTo(CodeWriter output) throws IOException;


    }
    public static Command comment(String line) {
        return new Comment(line);
    }
    public static Command commented(String originalLine, Command command) {
        return new CommentedCommand(originalLine, command);
    }

    public static Command pushCommand(String line) {
        return new PushCommand(line);
    }

    public static Command popCommand(String line) {
        return new PopCommand(line);
    }

    public static Command addCommand() {
        return new AddCommand();
    }

    public static Command eqCommand() {
        return new EqCommand();
    }

    public static Command subCommand() {
        return new SubtractCommand();
    }

    private static class CommentedCommand implements Command {
        private final String line;
        private final Command command;

        public CommentedCommand(String originalLine, Command command) {
            this.line = originalLine;
            this.command = command;
        }

        public void translateTo(CodeWriter output) throws IOException {
            output.printComment(line);
            command.translateTo(output);
        }
    }

    private static class Comment implements Command {
        private final String line;

        public Comment(String line) {
            this.line = line;
        }

        public void translateTo(CodeWriter output) throws IOException {
            output.printComment(this.line);
        }
    }

    private static class PushCommand implements Command {
        private final int id;

        private final Segment segment;
        public PushCommand(String line) {
            final String[] parts = line.split(" ");
            segment = Segment.forName(parts[1]);
            id = Integer.parseInt(parts[2]);
        }

        public void translateTo(CodeWriter output) {
            if (segment == Segment.CONSTANT) {
                output.ainstValue(id);
                output.assignAddressRegisterToDataRegister();
            } else {
                output.ainstSymbol(segment.symbol());
                if (segment == Segment.TEMP) {
                    output.assignAddressRegisterToDataRegister();
                } else { // local, argument
                    output.assignMemoryToDataRegister();
                }
                output.ainstValue(id);
                output.incrementAddressRegisterByDataRegisterValue();
                output.assignMemoryToDataRegister();
            }
            output.ainstSymbol("SP");
            output.incrementMemoryAndAssignToAddressRegister();
            output.decrementAddressRegister();
            output.assignDataRegisterToMemory();
        }

    }
    private static class PopCommand implements Command {

        private final int i;

        private final Segment segment;

        public PopCommand(String line) {
            String[] parts = line.split(" ");
            segment = Segment.forName(parts[1]);
            i = Integer.parseInt(parts[2]);
        }

        @Override
        public void translateTo(CodeWriter o) throws IOException {
            o.ainstSymbol(segment.symbol());
            if (segment == Segment.TEMP) {
                o.assignAddressRegisterToDataRegister();
            } else { // local, argument
                o.assignMemoryToDataRegister();
            }
            o.ainstValue(i);
            o.raw("D=D+A");
            o.ainstSymbol("R13");
            o.assignDataRegisterToMemory();
            o.ainstSymbol("SP");
            o.decrementMemoryAndAssignToAddressRegister();
            o.assignMemoryToDataRegister();
            o.ainstSymbol("R13");
            o.assignMemoryToAddressRegister();
            o.assignDataRegisterToMemory();
        }
    }

    private static class AddCommand implements Command {
        @Override
        public void translateTo(CodeWriter output) throws IOException {
            new PopCommand("pop temp 0").translateTo(output);
            new PopCommand("pop temp 1").translateTo(output);
            output.ainstValue(6); // tmp[1]
            output.assignMemoryToDataRegister();
            output.ainstValue(5); // tmp[0]
            output.incrementMemoryByDataRegisterValue();
            new PushCommand("push temp 0").translateTo(output);
        }
    }

    private static class SubtractCommand implements Command {
        @Override
        public void translateTo(CodeWriter output) throws IOException {
            new PopCommand("pop temp 0").translateTo(output);
            new PopCommand("pop temp 1").translateTo(output);
            output.ainstValue(5); // tmp[0]
            output.assignMemoryToDataRegister();
            output.ainstValue(6); // tmp[1]
            output.decrementMemoryByDataRegisterValue();
            new PushCommand("push temp 1").translateTo(output);
        }
    }

    private static class EqCommand implements Command {
        private static int globalEqCounter = 0;
        private final int myEqCounter;

        public EqCommand() {
            this.myEqCounter = globalEqCounter++;
        }

        @Override
        public void translateTo(CodeWriter o) throws IOException {
            new PopCommand("pop temp 1").translateTo(o);
            new PopCommand("pop temp 0").translateTo(o);
            String x = "@5"; // x comes from temp 0
            String y = "@6"; // y comes from temp 1
            String f = "@5"; // where the result is stored
            String eqLabel = "EQ" + myEqCounter;
            String eqEndLabel = "EQEND" + myEqCounter;
            o.raw(x);
            o.raw("D=M");
            o.raw(y);
            o.raw("D=D-M");
            o.raw("@" + eqLabel);
            o.raw("D;JEQ");
            o.raw("D=0");       // x != y
            o.raw("@" + eqEndLabel);
            o.raw("0;JMP");
            o.raw("(" + eqLabel + ")");     // x == y
            o.raw("D=1");
            o.raw("(" + eqEndLabel + ")");
            o.raw(f);        // temp[0] holds result
            o.raw("M=D");
            new PushCommand("push temp 0").translateTo(o);
        }
    }
}