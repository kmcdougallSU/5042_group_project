package src;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
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
                        case "7":
                            globalContext.increaseRpcCount();
                            writeFile();
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
        }

        public void logout() throws IOException {
            output.println(STATUS.SUCCESS.ordinal());
            output.println("USER> Logout successful. Goodbye, " + loggedInUser + "!");
            System.out.println("SERVER> " + loggedInUser + " disconnected");
            loggedInUser = null;
        }

        public void listFiles() {
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
            String response = "";
            String fileName = input.readLine();

            File directory = new File("storage/" + loggedInUser);
            if (!directory.exists()) {
                System.out.println("SERVER> No directory found, creating new directory: " + directory.getPath());
                directory.mkdirs(); // create the directory if it doesn't exist
            }

            try {
                File file = new File("storage/" + loggedInUser + "/" + fileName);
                if (file.createNewFile()) {
                    response = "File created successfully at path: " + file.getPath();
                } else {
                    response = "File already exists: " + fileName;
                    System.out.println(Arrays.toString(file.list()));
                }
            } catch (IOException e) {
                response = "An error occurred while creating file: " + fileName;
                System.err.println(response);
            } finally {
                // Only return 1 response for this method
                output.println(response);
            }
        }

        public void writeFile() throws IOException {
            String fileName = input.readLine();
            StringBuilder contentBuilder = new StringBuilder();
            String line;
            while (!(line = input.readLine()).equals("END_OF_CONTENT")) {
                contentBuilder.append(line).append("\n");
            }
            String content = contentBuilder.toString();

            File file = new File("storage/" + loggedInUser + "/" + fileName);
            try (FileWriter writer = new FileWriter(file, true)) { // false to overwrite
                writer.write(content);
                output.println("File updated successfully.");
            } catch (IOException e) {
                output.println("Failed to update file.");
            }
        }

        public void shareFile() throws IOException {
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
                output.println("SERVER> User has not set up file sharing yet.");
                return;
            }

            File destFile = new File(recipientDir, fileName);
            try {
                Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                output.println(STATUS.SUCCESS.ordinal());
                System.out.println();
                output.println("SERVER> File shared successfully.");
                System.out.println("SERVER> File: \"" + fileName + "\" shared \nfrom: " + loggedInUser + "\nto: " + recipient + "\nat: " + recipientDir);
            } catch (IOException e) {
                output.println(STATUS.FAIL.ordinal());
                output.println("SERVER> Error sharing file.");
            }
        }

        public void deleteFile() throws IOException {
            String filename = input.readLine();
            String response;
            File fileToDelete = new File("storage/" + loggedInUser + "/" + filename);
            if (fileToDelete.exists() && fileToDelete.delete()) {
                response = "Successfully deleted: " + filename;
            } else {
                response = "Failed to delete: " + filename;
            }
            output.println(response);
        }
    }

    enum STATUS {
        SUCCESS(),
        FAIL()
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