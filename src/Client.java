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

    public void login() throws IOException {
        if (loggedIn) {
            System.out.println("USER> You are already logged in.");
            return;
        }

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
            String response = read.readLine(); // Expecting the welcome message after the status line
            System.out.println(response);

            System.out.println("\nPress enter to continue...");
            scanner.nextLine(); // This waits for the user to press Enter, which can pause the flow unnecessarily. Consider removing if not needed.
        } else {
            String response = read.readLine(); // This reads the failure message.
            System.out.println(response); // Show why login failed.
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
            System.out.println("6. List Files\n");

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
                if (menuOption < 1 || menuOption > 6) {
                    System.out.println("USER> Menu option is invalid.");
                }
            } while (menuOption < 1 || menuOption > 6);

            output.println(menuOption);
            output.flush();

            switch (menuOption) {
                case 1:
                    login();
                    break;
                case 2:
                    createFile();
                    break;
                case 3:
                    shareFile();
                    break;
                case 4:
                    deleteFile();
                    break;
                case 5:
                    logout();
                    break;
                case 6:
                    listFiles();
                    break;
            }
        }
    }

    public void createFile() throws IOException {
        if (!loggedIn) {
            System.out.println("USER> You must be logged in to create a file.");
            return;
        }
        System.out.print("USER> Enter the name of the file to create: ");
        String filename = scanner.nextLine();
        output.println(filename);
        String response = read.readLine();
        System.out.println("SERVER> " + response);
    }

    public void listFiles() throws IOException {
        if (!loggedIn) {
            System.out.println("USER> You must be logged in to view files");
            return;
        }

        while (true) {
            String response = read.readLine();
            if ("END_OF_LIST".equals(response)) {
                break;
            }
            System.out.println("SERVER> " + response);
        }
    }
    public void shareFile() throws IOException {
        if (!loggedIn) {
            System.out.println("USER> Please login");
            return;
        } else {
            System.out.print("USER> Enter filename to share: ");
            String sharedFile = scanner.nextLine();
            System.out.print("USER> Enter recipient username: ");
            String fileDestination = scanner.nextLine();

            output.println(sharedFile);
            output.println(fileDestination);
            output.flush();

            String status = read.readLine();
            if (status.equals("0")) {
                if ("0".equals(status)) {
                    System.out.println("File shared successfully");
                } else {
                    System.out.println("Failed to share file");
                }
            }
        }
        showMenu();
    }

    public void deleteFile() throws IOException {
        if (!loggedIn) {
            System.out.println("USER> You must be logged in to delete a file");
            return;
        }
        System.out.print("USER> Enter name of file to delete: ");
        String filename = scanner.nextLine();
        output.println(filename);
        String response = read.readLine();
        System.out.println("SERVER> " + response);
    }

    public void logout() throws IOException {

        // Receive status from server
        String status = read.readLine();
        String response = read.readLine();
        if (status.equals("0")) {
            loggedIn = false;
        }
        System.out.println(response);

        // We can also exit the program here.
        showMenu();
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
