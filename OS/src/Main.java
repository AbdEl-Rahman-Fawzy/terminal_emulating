import java.util.Scanner;

public class Main {
    public static void main(String[] userArgs) {
        Terminal terminal = new Terminal();
        Parser parser = new Parser();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("-->");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                break;
            }

            if (parser.parse(input)) {
                String commandName = parser.getCommandName();
                String[] args = parser.getArgs();
                terminal.executeCommand(commandName, args);
            } else {
                System.out.println("invalid input. Please try again!!!");
            }
        }
        scanner.close();
    }
}
