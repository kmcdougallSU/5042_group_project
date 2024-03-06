package src;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Client implements AutoCloseable {
    Socket socket;
    BufferedReader read;
    PrintWriter output;
    Scanner scanner = new Scanner(System.in);
    boolean loggedIn = false;

    public void startClient() throws IOException {
        socket = new Socket("localhost", 12345);
        output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        read = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        showMenu();
    }

    public void login(int menuOption) throws IOException {
        if (loggedIn) {
            System.out.println("USER> You are already logged in.");
            return;
        }

        // send menuOption to tell server about this method
        output.println(menuOption);
        output.flush();

        System.out.print("Welcome...\nUSER> Please enter your username: ");
        String username = scanner.nextLine();
        System.out.print("USER> Enter password: ");
        String password = scanner.nextLine();

        // Send login credentials to server
        output.println(username);
        output.println(password);

        // Receive status from server
        String status = read.readLine();
        if ("0".equals(status)) {
            loggedIn = true;
            String response = read.readLine();
            System.out.println(response);

            System.out.println("\nPress enter to continue...");
            scanner.nextLine();
        } else {
            String response = read.readLine();
            System.out.println(response);
        }
    }


    public void showMenu() throws IOException {
        while (true) {
            System.out.println("\n---Menu---");
            System.out.println("1. Login");
            System.out.println("2. Create File");
            System.out.println("3. Share File");
            System.out.println("4. Delete File");
            System.out.println("5. Logout");
            System.out.println("6. List Files");
            System.out.println("7. Write to File\n");

            int menuOption;
            do {
                System.out.print("USER> Enter the menu option: ");
                while (!scanner.hasNextInt()) {
                    System.out.println("USER> Menu option is invalid");
                    scanner.nextLine();
                    System.out.print("USER> Enter the menu option: ");
                }
                menuOption = scanner.nextInt();

                // After reading the int, read the new line
                scanner.nextLine();
                if (menuOption < 1 || menuOption > 7) {
                    System.out.println("USER> Menu option is invalid.");
                }
            } while (menuOption < 1 || menuOption > 7);

            switch (menuOption) {
                case 1:
                    login(menuOption);
                    break;
                case 2:
                    createFile(menuOption);
                    break;
                case 3:
                    shareFile(menuOption);
                    break;
                case 4:
                    deleteFile(menuOption);
                    break;
                case 5:
                    logout(menuOption);
                    break;
                case 6:
                    listFiles(menuOption);
                    break;
                case 7:
                    writeFile(menuOption);
                    break;
            }
        }
    }

    public void createFile(int menuOption) throws IOException {
        if (!loggedIn) {
            System.out.println("USER> You must be logged in to create a file.");
            return;
        }

        // send menuOption to tell server about this method
        output.println(menuOption);
        output.flush();

        System.out.print("USER> Enter the name of the file to create: ");
        String filename = scanner.nextLine();
        output.println(filename);
        String response = read.readLine();
        System.out.println("SERVER> " + response);
    }

    public void listFiles(int menuOption) throws IOException {
        if (!loggedIn) {
            System.out.println("USER> You must be logged in to view files");
            return;
        }

        // send menuOption to tell server about this method
        output.println(menuOption);
        output.flush();

        while (true) {
            String response = read.readLine();
            if ("END_OF_LIST".equals(response)) {
                break;
            }
            System.out.println("SERVER> " + response);
        }
    }

    public void writeFile(int menuOption) {
        if (!loggedIn) {
            System.out.println("USER> You must be logged in to write to a file.");
            return;
        }

        // send menuOption to tell server about this method
        output.println(menuOption);
        output.flush();

        try {
            System.out.print("USER> Enter the name of the file you want to write to: ");
            String filename = scanner.nextLine(); // Get the filename from the user

            System.out.println("USER> Enter the content you want to write to the file (end with an empty line): ");
            StringBuilder contentBuilder = new StringBuilder();
            String line;
            while (!(line = scanner.nextLine()).isEmpty()) {
                contentBuilder.append(line).append("\n");
            }
            String content = contentBuilder.toString();

            output.println(filename);
            output.println(content);
            output.println("END_OF_CONTENT");


            String response = read.readLine();
            System.out.println("SERVER> " + response);
        } catch (IOException e) {
            System.out.println("An error occurred while trying to write to the file: " + e.getMessage());
        }
    }

    public void shareFile(int menuOption) throws IOException {
        if (!loggedIn) {
            System.out.println("USER> You must be logged in to share a file.");
            return;
        } else {
            // send menuOption to tell server about this method
            output.println(menuOption);
            output.flush();

            System.out.print("USER> Enter filename to share: ");
            String sharedFile = scanner.nextLine();
            System.out.print("USER> Enter recipient username: ");
            String fileDestination = scanner.nextLine();

            output.println(sharedFile);
            output.println(fileDestination);
            output.flush();

            String status = read.readLine();
            String response = read.readLine();
            if (status.equals("0")) {
                System.out.println("USER> File shared successfully");
            } else {
                System.out.println("USER> Failed to share file");
            }
            System.out.println(response);
        }
    }

    public void deleteFile(int menuOption) throws IOException {
        if (!loggedIn) {
            System.out.println("USER> You must be logged in to delete a file");
            return;
        }
        // send menuOption to tell server about this method
        output.println(menuOption);
        output.flush();

        System.out.print("USER> Enter name of file to delete: ");
        String filename = scanner.nextLine();
        output.println(filename);
        String response = read.readLine();
        System.out.println("SERVER> " + response);
    }

    public void logout(int menuOption) throws IOException {
        if (!loggedIn) {
            System.out.println("USER> No user is logged in.");
            return;
        }

        // send menuOption to tell server about this method
        output.println(menuOption);
        output.flush();

        // Receive status from server
        String status = read.readLine();
        String response = read.readLine();
        if (status.equals("0")) {
            loggedIn = false;
        }
        System.out.println("SERVER> " + response);
    }

    @Override
    public void close() throws IOException {
        if (socket != null) {
            socket.close();
        }
        if (read != null) {
            read.close();
        }
        if (output != null) {
            output.close();
        }
        System.out.println("todo: disconnect");
    }

    public static void main(String[] args) {
        try (Client client = new Client()) {
            client.startClient();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
