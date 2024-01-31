package src;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server implements AutoCloseable {
    enum STATUS {
        SUCCESS,
        FAIL
    }

    Map<String, String> userPassword;
    ServerSocket serversocket;
    Socket client;
    BufferedReader input;
    PrintWriter output;
    String loggedInUser;

    public Server() {
        userPassword = new HashMap<>();
        userPassword.put("User1", "Password1");
        userPassword.put("User2", "Password2");
        userPassword.put("User3", "Password3");
        userPassword.put("User4", "Password4");
    }

    public void start() throws IOException {
        serversocket = new ServerSocket(12345);
        System.out.println("SERVER> Connection Starting on port:" + serversocket.getLocalPort());

        while (true) {
            //accept connection from client
            client = serversocket.accept();

            //open buffered reader for reading data from client
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));

            //open print writer for writing data to client
            output = new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true);

            System.out.println("SERVER> Waiting for connection from client");

            try {
                process();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (client != null) {
                    client.close();
                }
            }
        }
    }

    public void process() throws IOException {
        login();
        while (true) { // Loop to handle multiple requests
            String menuOptionStr = input.readLine(); // Read the line
            int menuOption = Integer.parseInt(menuOptionStr.trim()); // Convert to int
            if (menuOption == 1) {
                logout();
                break;
            }
        }
    }

    public void login() throws IOException {
        String username = input.readLine();
        System.out.println("SERVER> username: " + username);

        String password = input.readLine();
        System.out.println("SERVER> password: " + password);

        if (userPassword.containsKey(username) && password.equals(userPassword.get(username))) {
            output.println(STATUS.SUCCESS.ordinal());
            output.println("USER> Welcome, " + username); // Send the welcome message to the client
            loggedInUser = username;
            System.out.println("SERVER> " + loggedInUser + " connected");
        } else {
            output.println(STATUS.FAIL.ordinal());
            output.println("USER> Login Failed");
        }
        output.flush();

    }

    public void logout() throws IOException {
        if (loggedInUser != null && !loggedInUser.isEmpty()) {
            output.println("USER> Logging out: " + loggedInUser);
            System.out.println("SERVER> " + loggedInUser + " disconnected");
            output.println("USER> Logout successful. Goodbye, " + loggedInUser + "!");
            loggedInUser = null;
        } else {
            output.println("USER> No user is currently logged in.");
            output.println("USER> Logout failed. No user was logged in.");
        }
        output.println("USER> END_OF_MESSAGE");
        output.flush();
    }


    public static void main(String[] args) {
        try (Server server = new Server()) {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        if (serversocket != null) {
            serversocket.close();
        }
        if (input != null) {
            input.close();
        }
        if (output != null) {
            output.close();
        }
    }
}
