package src;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;

public class Server {
    enum STATUS {
        SUCCESS,
        FAIL,
    }
    Map<String, String> userPassword;
    ServerSocket serversocket;
    Socket client;
    BufferedReader input;
    PrintWriter output;
    String loggedInUser;

    public void doSignup() {
        userPassword = new HashMap<>();
        userPassword.put("User1", "Password1");
        userPassword.put("User2", "Password2");
        userPassword.put("User3", "Password3");
        userPassword.put("User4", "Password4");
    }

    public void start() throws IOException {
        doSignup();
        serversocket = new ServerSocket(12345);
        System.out.println("Connection Starting on port:" + serversocket.getLocalPort());

        while (true) {
            //accept connection from client
            client = serversocket.accept();

            //open buffered reader for reading data from client
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));

            //open print writer for writing data to client
            output = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));

            System.out.println("Waiting for connection from client");

            try {
                process();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void process() throws IOException {
        login();
        int menuOption = input.read();
        if (menuOption == 1)
        {
            login();
        } else if (menuOption == 2) {
            logout();
        }
    }

    public void login() throws IOException {

        String username = input.readLine();
        System.out.println("username: " + username);

        String password = input.readLine();
        System.out.println("password: " + password);

        if(userPassword.containsKey(username) && password.equals(userPassword.get(username)))
        {
            output.print(STATUS.SUCCESS.ordinal());
            output.println("Welcome, " + username + "\n");
            loggedInUser = username;
        }
        else
        {
            output.print(STATUS.FAIL.ordinal());
            output.println("Login Failed");
        }
        output.flush();
    }

    public void logout() throws IOException {
        // TODO
    }

    public static void main(String[] args) {
        Server server = new Server();
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}