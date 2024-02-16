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
        if(loggedIn) {
            System.out.println("USER> You are already logged in.");
        } else {
            System.out.print("Welcome...\nUSER> Please enter your username: ");
            String username = scanner.nextLine();
            System.out.print("USER> Enter password: ");
            String password = scanner.nextLine();

            // Send login credentials to server
            output.println(username);
            output.println(password);
            output.flush();

            // Receive status from server
            String status = read.readLine();
            if (status.equals("0")) {
                loggedIn = true;
            }
            String response = read.readLine();
            System.out.println(response);
        }
        showMenu();
    }

    public void showMenu() throws IOException {

        while (true) {
            System.out.println("\n---Menu---");
            System.out.println("1. Login");
            System.out.println("2. Create File");
            System.out.println("3. Share File");
            System.out.println("4. Delete File");
            System.out.println("5. Logout\n");

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
                if (menuOption < 1 || menuOption > 5) {
                    System.out.println("USER> Menu option is invalid.");
                }
            } while (menuOption < 1 || menuOption > 5);

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
            }
        }
    }

    public void createFile() throws IOException {
        //TODO
    }

    public void shareFile() throws IOException {
        // TODO
    }

    public void deleteFile() throws IOException {
        //TODO
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


