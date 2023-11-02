import org.jetbrains.annotations.NotNull;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;
import static java.nio.file.Files.*;
import static java.nio.file.Files.list;
import static java.nio.file.Files.newDirectoryStream;
import static java.util.Collections.*;

public class Terminal {
    private static final List<String> commandHistory = new ArrayList<>();
    Parser parser;

    public static void echo(String argument) {

        System.out.println(argument);
    }

    public static void pwd() {
        String currentDirectory = System.getProperty("user.dir");
        System.out.println(currentDirectory);
    }

    public static void cd(@NotNull String argument) {
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        //1.
        if (argument.isEmpty()) {
            currentPath = Paths.get(System.getProperty("user.home"));
        }
        //2.
        else if (argument.equals("..")) {
            currentPath = currentPath.getParent();
        }
        //3.
        else {
            Path X_path = Paths.get(argument);

            if (X_path.isAbsolute()) {
                currentPath = X_path;
            } else {
                currentPath = currentPath.resolve(X_path);
            }
        }

        if (exists(currentPath) && isDirectory(currentPath)) {
            System.setProperty("user.dir", currentPath.toString());
        } else {
            System.err.println("given argument doesn't exist!!!!!");
        }
    }

    public static void ls() {
        try {
            Path currentPath = Paths.get(System.getProperty("user.dir"));
            try (DirectoryStream<Path> curr_directory = newDirectoryStream(currentPath)) {
                for (Path file : curr_directory) {
                    System.out.println(file.getFileName());
                }
            }
        } catch (IOException exp) {
            System.err.println("Error: " + exp.getMessage());
        }
    }

    public static void lsReverse() {
        try {
            File currentDirectory = new File(System.getProperty("user.dir"));
            File[] files_in_current_directory = currentDirectory.listFiles();

            if (files_in_current_directory != null) {
                List<File> fileList = Arrays.asList(files_in_current_directory);
                reverse(fileList);

                for (File file : files_in_current_directory) {
                    System.out.println(file.getName());
                }
            }
        } catch (SecurityException exp) {
            System.err.println("Error: " + exp.getMessage());
        }
    }

    public static void mkdir(String @NotNull ... directories) {
        try {
            for (String dir : directories) {
                //path
                if (dir.endsWith("/") || dir.endsWith("\\")) {
                    Path dirPath = Paths.get(dir);
                    createDirectories(dirPath);
                }
                //current
                else {
                    Path currentPath = Paths.get(System.getProperty("user.dir"));
                    Path new_path = currentPath.resolve(dir);
                    createDirectories(new_path);
                }

                System.out.println("Created: " + dir);
            }
        } catch (FileAlreadyExistsException e) {
            System.err.println("err: The directory already exists.");
        } catch (IOException e) {
            System.err.println("err: " + e.getMessage());
        }
    }

    public static void rmdir(String path) {
        try {
            Path working_directory = Paths.get(path);
            //"*" case
            if (path.equals("*")) {
                try {
                    List<Path> directories_to_delete = list(working_directory)
                            .filter(Files::isDirectory).
                            toList();
                    for (Path directory : directories_to_delete) {
                        try {
                            delete(directory);
                            System.out.println("Removed directory: " + directory);
                        } catch (DirectoryNotEmptyException exp) {
                            System.err.println("The directory is not empty: " + directory);
                        } catch (IOException exp) {
                            System.err.println("Error: " + exp.getMessage());
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error: " + e.getMessage());
                }

            }
            // path case
            else {
                if (isDirectory(working_directory) && isDirectoryEmpty(working_directory)) {
                    delete(working_directory);
                    System.out.println("Removed directory: " + working_directory);
                } else {
                    System.err.println("Error: The specified directory is not empty or doesn't exist.");
                }
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    //dependency for rmdir command|^|
    private static boolean isDirectoryEmpty(Path directory) throws IOException {
        try (Stream<Path> files = list(directory)) {
            return files.noneMatch(path -> true);
        }
    }

    public static void touch(String path) {
        try {
            Path filePath = Paths.get(path);
            //if relative path
            if (filePath.getParent() != null) {
                createDirectories(filePath.getParent());
            }
            if (!exists(filePath)) {
                createFile(filePath);
                System.out.println("Created file: " + filePath);
            } else {
                System.out.println("File already exists: " + filePath);
            }

        } catch (FileAlreadyExistsException exp) {
            System.out.println("File already exists: " + path);
        } catch (IOException exp) {
            System.err.println("Error: " + exp.getMessage());
        }
    }

    public static void cp(String sourceFile, String destinationFile) {
        try {
            Path sourcepath = Paths.get(sourceFile);
            Path targetpath = Paths.get(destinationFile);

            copy(sourcepath, targetpath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Copied file: " + sourceFile + " to " + destinationFile);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }


    public static void cpRecursive(String sourceDirectory, String destinationDirectory) {
        try {
            Path sourcePath = Paths.get(sourceDirectory);
            Path targetpath = Paths.get(destinationDirectory);

            // Copy the source directory and its content to the destination directory
            walkFileTree(sourcePath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path targetDir = targetpath.resolve(sourcePath.relativize(dir));
                    createDirectories(targetDir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes content) throws IOException {
                    copy(file, targetpath.resolve(sourcePath.relativize(file)));
                    return FileVisitResult.CONTINUE;
                }
            });

            System.out.println("Copied directory and its content: " + sourceDirectory + " to " + destinationDirectory);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }


    public static void rm(String fileName) {
        try {
            Path filePath = Paths.get(fileName);

            if (exists(filePath) && !isDirectory(filePath)) {
                delete(filePath);
                System.out.println("Removed file: " + fileName);
            } else {
                System.err.println("Error: The specified file does not exist or is a directory.");
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void cat(String file1, String file2) {
        try {
            //one file
            if (file2 == null) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file1))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }
            }
            //two files
            else {
                try (BufferedReader reader1 = new BufferedReader(new FileReader(file1));
                     BufferedReader reader2 = new BufferedReader(new FileReader(file2))) {
                    String line;
                    while ((line = reader1.readLine()) != null) {
                        System.out.println(line);
                    }
                    while ((line = reader2.readLine()) != null) {
                        System.out.println(line);
                    }
                }
            }
        } catch (IOException exp) {
            System.err.println("Error: " + exp.getMessage());
        }
    }


    public static void wc(String fileName) {
        try {
            int lines_ctr = 0;
            int word_ctr = 0;
            int char_ctr = 0;

            try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines_ctr++;
                    char_ctr += line.length();
                    String[] words = line.split("\\s+");
                    word_ctr += words.length;
                }
            }
            System.out.println("lines: " + lines_ctr + " words: " + word_ctr +
                    " characters: " + char_ctr + " from file: " + fileName);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Command: history
    // Takes no parameters and displays an enumerated list with the commands used in the past.
    public static void history() {
        if (commandHistory.isEmpty()) {
            System.out.println("Command history is empty.");
        } else {
            for (int i = 0; i < commandHistory.size(); i++) {
                System.out.println((i + 1) + " " + commandHistory.get(i));
            }
        }
    }

    public void executeCommand(@NotNull String commandName, String[] args) {

        switch (commandName) {
            case "echo" -> {
                if (args.length == 1) {
                    echo(args[0]);
                } else {
                    System.err.println("Usage: echo <message>");
                }
            }
            case "pwd" -> {
                if (args.length == 0) {
                    pwd();
                } else {
                    System.err.println("Usage: pwd");
                }
            }
            case "cd" -> {
                if (args.length == 0) {
                    cd(System.getProperty("user.home"));
                } else if (args.length == 1) {
                    cd(args[0]);
                } else {
                    System.err.println("Usage: cd [path]");
                }
            }
            case "ls" -> {
                if (args.length == 0) {
                    ls();
                } else {
                    System.err.println("Usage: ls");
                }
            }
            case "ls -r" -> {
                if (args.length == 0) {
                    lsReverse();
                } else {
                    System.err.println("Usage: ls -r");
                }
            }
            case "mkdir" -> {
                if (args.length > 0) {
                    mkdir(args);
                } else {
                    System.err.println("Usage: mkdir <directory1> [directory2] ...");
                }
            }
            case "rmdir" -> {
                if (args.length == 1) {
                    if ("*".equals(args[0])) {
                        // Handle removing empty directories in the current directory
                        // Implement the logic for this case.
                    } else {
                        rmdir(args[0]);
                    }
                } else {
                    System.err.println("Usage: rmdir <* | path>");
                }
            }
            case "touch" -> {
                if (args.length == 1) {
                    touch(args[0]);
                } else {
                    System.err.println("Usage: touch <file>");
                }
            }
            case "cp" -> {

            }
            case "cp -r" -> {

            }
            case "cat" -> {

            }
            case "history" -> {
                history();
            }
            case "wc" -> {
                wc(Arrays.toString(args));
            }
            case "rm" -> {

            }


            default -> System.err.println("Unknown command: " + commandName);
        }
    }

}
