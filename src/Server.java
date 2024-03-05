package src;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Server implements AutoCloseable {

    // Thread class to handle client connections
    class ClientHandler implements Runnable {
        Socket client;
        String loggedInUser;
        BufferedReader input;
        PrintWriter output;

        public ClientHandler(Socket clientSocket) {
            this.client = clientSocket;
            System.out.println("SERVER> Client joining on socket port: " + client.getPort());
        }

        @Override
        public void run() {
            try {
                input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                output = new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true);

                while (true) {
                    String request = input.readLine();
                    switch (request) {
                        case "1":
                            globalContext.increaseRpcCount();
                            login();
                            break;
                        case "2":
                            globalContext.increaseRpcCount();
                            createFile();
                            break;
                        case "3":
                            globalContext.increaseRpcCount();
                            shareFile();
                            break;
                        case "4":
                            globalContext.increaseRpcCount();
                            deleteFile();
                            break;
                        case "5":
                            globalContext.increaseRpcCount();
                            logout();
                            break;
                        case "6":
                            globalContext.increaseRpcCount();
                            listFiles();
                            break;
                    }
                }
            } catch (IOException ignored) {
            } finally {
                try {
                    client.close();
                } catch (IOException ignored) {
                }
            }
        }

        public void login() throws IOException {
            if (loggedInUser == null) {
                String username = input.readLine();
                System.out.println("SERVER> username: " + username);

                String password = input.readLine();
                System.out.println("SERVER> password: " + password);

                if (userPassword.containsKey(username) && password.equals(userPassword.get(username))) {
                    output.println(STATUS.SUCCESS.ordinal());
                    output.println("SERVER> Welcome, " + username);
                    loggedInUser = username;
                    System.out.println("SERVER> " + loggedInUser + " connected on socket port: " + client.getPort());
                } else {
                    output.println(STATUS.FAIL.ordinal());
                    output.println("SERVER> Login Failed");
                }
                output.flush();
            } else {
                output.println((STATUS.FAIL.ordinal()));
                output.println("You are already logged in.");
            }
        }

        public void logout() throws IOException {
            if (loggedInUser != null && !loggedInUser.isEmpty()) {
                output.println(STATUS.SUCCESS.ordinal());
                output.println("USER> Logout successful. Goodbye, " + loggedInUser + "!");
                System.out.println("SERVER> " + loggedInUser + " disconnected");
                loggedInUser = null;
            } else {
                output.println(STATUS.FAIL.ordinal());
                output.println("USER> Logout failed. No user was logged in.");
            }
        }

        public void listFiles() {
            if (loggedInUser == null) {
                output.println("SERVER> You must be logged in to list files");
                return;
            }

            File userDir = new File("storage/" + loggedInUser);
            if (!userDir.exists() || !userDir.isDirectory()) {
                output.println("No files found");
                output.println("END_OF_LIST");
                return;
            }

            String[] files = userDir.list();
            if (files == null || files.length == 0) {
                output.println("No files found");
            } else {
                for (String file : files
                ) {
                    output.println(file);
                }
            }
            output.println("END_OF_LIST");
        }

        public void createFile() throws IOException {
            String response;

            if (loggedInUser == null) {
                response = "SERVER> You must be logged in to create a file";
                output.println(response);
                return;
            }

            String fileName = input.readLine();

            File directory = new File("storage/" + loggedInUser);
            if (!directory.exists()) {
                response = "no directory found, creating new directory...";
                output.println(response);
                directory.mkdirs(); // create the directory if it doesn't exist
            }

            try {
                File file = new File("storage/" + loggedInUser + "/" + fileName);
                if (file.createNewFile()) {
                    response = "File created successfully at path: " + file.getPath();
                } else {
                    response = "File already exists: " + fileName;
                    System.out.println(file.list());
                }
                output.println(response);
            } catch (IOException e) {
                response = "An error occurred while creating file: " + fileName;
                System.err.println(response);
                output.println(response);
            }

        }

        public void shareFile() throws IOException {
            if (loggedInUser == null || loggedInUser.isEmpty()) {
                output.println(STATUS.FAIL.ordinal());
                output.println("SERVER> Must be logged in to share a file");
                return;
            }

            String fileName = input.readLine();
            String recipient = input.readLine();

            File sourceFile = new File("storage/" + loggedInUser + "/" + fileName);
            if (!sourceFile.exists()) {
                output.println(STATUS.FAIL.ordinal());
                output.println("SERVER> File does not exist.");
                return;
            }

            File recipientDir = new File("storage/" + recipient);
            if (!recipientDir.exists() || !recipientDir.isDirectory()) {
                output.println(STATUS.FAIL.ordinal());
                output.println("SERVER> Recipient does not exist.");
                return;
            }

            File destFile = new File(recipientDir, fileName);
            try {
                Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                output.println(STATUS.SUCCESS.ordinal());
                output.println("SERVER> File shared successfully.");
            } catch (IOException e) {
                output.println(STATUS.FAIL.ordinal());
                output.println("SERVER> Error sharing file.");
            }
        }

        public void deleteFile() throws IOException {
            String filename = input.readLine();
            String response;
            if (loggedInUser != null && !loggedInUser.isEmpty()) {
                File fileToDelete = new File("storage/" + loggedInUser + "/" + filename);
                if (fileToDelete.exists() && fileToDelete.delete()) {
                    response = "Successfully deleted: " + filename;
                } else {
                    response = "Failed to delete: " + filename;
                }
            } else {
                response = "You must be logged in to delete a file";
            }
            output.println(response);
        }
    }


    enum STATUS {
        SUCCESS(),
        FAIL();
//
//        private final int code;
//
//        STATUS(int code) {
//            this.code = code;
//        }
//
//        public int getCode() {
//            return this.code;
//        }
    }

    // Global context for server
    static class GlobalContext {
        private final ThreadPoolExecutor threadPool;
        int rpcCount;
        // maybe used in future
        String lastUserWhoSubmittedRPC;
        int numberLogons;

        GlobalContext(int max_threads) {
            threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(max_threads);
            rpcCount = 0;
        }

        synchronized void increaseRpcCount() {
            rpcCount++;
        }

        synchronized void setLastUser(String user) {
            this.lastUserWhoSubmittedRPC = user;
        }

    }

    Map<String, String> userPassword;
    ServerSocket serversocket;
    GlobalContext globalContext;
    String loggedInUser;
    int port;

    public Server(int port, int max_threads) {
        this.port = port;
        userPassword = new HashMap<>();
        userPassword.put("u1", "p1");
        userPassword.put("u2", "p2");
        userPassword.put("u3", "p3");
        userPassword.put("u4", "p4");
        userPassword.put("u5", "p5");
        globalContext = new GlobalContext(max_threads);
    }

    public void start() throws IOException {
        serversocket = new ServerSocket(port);
        System.out.println("SERVER> Connection Starting on local port: " + serversocket.getLocalPort());

        while (true) {
            Socket client = serversocket.accept();
            // Spawn a new thread for each client connection
            globalContext.threadPool.execute(new ClientHandler(client));
        }
    }

    @Override
    public void close() {
        System.out.println("Shutting down server");
        this.globalContext.threadPool.shutdown();
    }


    public static void main(String[] args) {
        final int PORT = 12345;
        final int MAX_THREADS = 200;
        try (Server server = new Server(PORT, MAX_THREADS)) {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}