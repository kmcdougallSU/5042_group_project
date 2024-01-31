package src;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client implements AutoCloseable {
    enum STATUS {
        SUCCESS,
        FAIL
    }

    Socket socket;
    BufferedReader read;
    PrintWriter output;

    public void startClient() throws IOException {
        socket = new Socket("localhost", 12345);
        output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

        Scanner myObj = new Scanner(System.in);

        System.out.print("Welcome...\nPlease enter your username: ");
        String username = myObj.nextLine();
        output.println(username);

        System.out.print("Enter password: ");
        String password = myObj.nextLine();
        output.println(password);
        output.flush();

        read = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String statusCodeStr = read.readLine(); // Read the status code as a string
        int errorCode = Integer.parseInt(statusCodeStr.trim()); // Convert to int
        String response = read.readLine(); // Read the response message
        System.out.println("\n" + response);

        if (errorCode == STATUS.SUCCESS.ordinal()) {
            showMenu();
        } else {
            System.out.println("Please try again.");
        }

    }

    public void showMenu() throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("---Menu---");
        System.out.println("""
                1. Logout
                """);

        int menuOption;
        do {
            System.out.print("Enter the menu option: ");
            while (!scanner.hasNextInt()) {
                System.out.println("Menu option is invalid");
                scanner.next();
            }
            menuOption = scanner.nextInt();
            if (menuOption != 1) {
                System.out.println("Menu option is invalid.");
            }
        } while (menuOption != 1);

        output.println(menuOption);
        output.flush();

        String logoutResponse = read.readLine(); // Read logout response from the server
        System.out.println(logoutResponse);

        output.println(menuOption);
        output.flush();

        String serverMessage;
        while (!(serverMessage = read.readLine()).equals("END_OF_MESSAGE")) {
            System.out.println(serverMessage);
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
}
