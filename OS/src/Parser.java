import org.jetbrains.annotations.NotNull;

public class Parser {
    private String commandName;
    private String[] args;

    public boolean parse(@NotNull String input) {
        String[] parts = input.split(" ");

        if (parts.length > 0) {
            commandName = parts[0];
        } else {
            commandName = "";
        }

        if (parts.length > 1) {
            args = parts[1].split(" ");
        } else {
            args = new String[0];
        }

        return true;
    }

    public String getCommandName() {
        return commandName;
    }

    public String[] getArgs() {
        return args;
    }
}