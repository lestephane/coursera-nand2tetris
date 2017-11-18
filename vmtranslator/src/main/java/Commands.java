import java.io.IOException;

public class Commands {
    public interface Command {

        void translateTo(CodeWriter o) throws IOException;
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

    public static Command gtCommand() {
        return new GreatherThanCommand();
    }
    public static Command ltCommand() {
        return new LessThanCommand();
    }
    public static Command negCommand() {
        return new NegateCommand();
    }
    public static Command notCommand() {
        return new NotCommand();
    }

    public static Command andCommand() {
        return new AndCommand();
    }

    public static Command orCommand() {
        return new OrCommand();
    }

    public static Command subCommand() {
        return new SubtractCommand();
    }

    public static Command labelCommand(String line) {
        return new LabelCommand(line);
    }

    public static Command ifGoto(String line) {
        return new IfGotoCommand(line);
    }

    private static class CommentedCommand implements Command {


        private final String line;
        private final Command command;

        public CommentedCommand(String originalLine, Command command) {
            this.line = originalLine;
            this.command = command;
        }

        public void translateTo(CodeWriter o) throws IOException {
            o.printComment(line);
            command.translateTo(o);
        }
    }

    private static class Comment implements Command {
        private final String line;

        public Comment(String line) {
            this.line = line;
        }

        public void translateTo(CodeWriter o) throws IOException {
            o.printComment(this.line);
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

        public void translateTo(CodeWriter o) {
            o.push(segment, id);
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
            o.pop(segment, i);
        }
    }

    private static class AddCommand implements Command {
        @Override
        public void translateTo(CodeWriter o) throws IOException {
            new PopCommand("pop temp 0").translateTo(o);
            new PopCommand("pop temp 1").translateTo(o);
            o.ainstValue(6); // tmp[1]
            o.assignMemoryToDataRegister();
            o.ainstValue(5); // tmp[0]
            o.incrementMemoryByDataRegisterValue();
            new PushCommand("push temp 0").translateTo(o);
        }
    }

    private static class SubtractCommand implements Command {
        @Override
        public void translateTo(CodeWriter o) throws IOException {
            new PopCommand("pop temp 0").translateTo(o);
            new PopCommand("pop temp 1").translateTo(o);
            o.ainstValue(5); // tmp[0]
            o.assignMemoryToDataRegister();
            o.ainstValue(6); // tmp[1]
            o.decrementMemoryByDataRegisterValue();
            new PushCommand("push temp 1").translateTo(o);
        }
    }

    private static class EqCommand implements Command {
        public void translateTo(CodeWriter o) throws IOException {
            o.comparisonOperation("EQ");
        }

    }

    private static class GreatherThanCommand implements Command {
        public void translateTo(CodeWriter o) throws IOException {
            o.comparisonOperation("GT");
        }
    }

    private static class LessThanCommand implements Command {
        public void translateTo(CodeWriter o) throws IOException {
            o.comparisonOperation("LT");
        }
    }

    private static class NegateCommand implements Command {
        public void translateTo(CodeWriter o) throws IOException {
            o.pop(Segment.TEMP, 0);
            o.atTemp(0);
            o.negateMemory();
            o.push(Segment.TEMP, 0);
        }
    }

    private static class NotCommand implements Command {
        public void translateTo(CodeWriter o) throws IOException {
            o.pop(Segment.TEMP, 0);
            o.atTemp(0);
            o.binaryNotMemory();
            o.push(Segment.TEMP , 0);
        }
    }

    private static class AndCommand implements Command {
        public void translateTo(CodeWriter o) throws IOException {
            o.logicalOperation("&");
        }

    }
    private static class OrCommand implements Command {
        public void translateTo(CodeWriter o) throws IOException {
            o.logicalOperation("|");
        }

    }

    private static class LabelCommand implements Command {
        private final String name;

        public LabelCommand(String line) {
            name = line.split(" ")[1];
        }

        public void translateTo(CodeWriter o) throws IOException {
            o.label(name);
        }
    }

    private static class IfGotoCommand implements Command {
        private final String name;

        public IfGotoCommand(String line) {
            name = line.split(" ")[1];
        }

        public void translateTo(CodeWriter o) throws IOException {
            o.popToDataRegister();
            o.jumpIfDataRegisterIsTruthy(name);
        }

    }
}