import java.io.IOException;

public class Commands {
    enum Segment {
        local("LCL"),argument("ARG");

        private final String symbol;

        Segment(String symbol) {
            this.symbol = symbol;
        }
    }

    public static Command comment(String line) {
        return new Comment(line);
    }

    public static Command pushConstant(String line) {
        return new PushConstantCommand(line);
    }

    public static Command popCommand(String line) {
        return new PopCommand(line);
    }

    public static Command commented(String originalLine, Command command) {
        return new CommentedCommand(originalLine, command);
    }

    public interface Symbols {
        String STACK_POINTER = "SP";
        String LOCAL_MEMORY_SEGMENT = "LCL";

    }

    public interface Command {
        void translateTo(CodeWriter output) throws IOException;

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
    private static class PushConstantCommand implements Command {

        private final int id;

        public PushConstantCommand(String originalSourceLine) {
            id = Integer.parseInt(originalSourceLine.split(" ")[2]);

        }
        public void translateTo(CodeWriter output) {
            output.ainstValue(id);
            output.assignAddressRegisterToDataRegister();
            output.ainstSymbol(Symbols.STACK_POINTER);
            output.incrementMemoryAndAssignToAddressRegister();
            output.decrementAddressRegister();
            output.assignDataRegisterToMemory();
            output.incrementStackPointer();
        }

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
    private static class PopCommand implements Command {

        private final int i;
        private final Segment segment;

        public PopCommand(String line) {
            String[] parts = line.split(" ");
            segment = Segment.valueOf(parts[1]);
            i = Integer.parseInt(parts[2]);
        }
        @Override
        public void translateTo(CodeWriter o) throws IOException {
            o.ainstSymbol(segment.symbol);
            o.assignMemoryToDataRegister();
            o.ainstValue(i);
            o.raw("D=D+A");
            o.ainstSymbol("R13");
            o.assignDataRegisterToMemory();
            o.ainstSymbol(Symbols.STACK_POINTER);
            o.decrementMemoryAndAssignToAddressRegister();
            o.assignMemoryToDataRegister();
            o.ainstSymbol("R13");
            o.assignMemoryToAddressRegister();
            o.assignDataRegisterToMemory();
        }

    }
}