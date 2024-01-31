import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

    enum STATUS {
        SUCCESS,
        FAIL,
    }
    Socket socket;
    BufferedReader read;
    PrintWriter output;

    public void startClient() throws IOException{
        //Create socket connection
        socket = new Socket("localhost", 12345);

        //create print writer for sending login to server
        output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

        // Create a Scanner object
        Scanner myObj = new Scanner(System.in);

        // Prompt for username
        System.out.print("Enter username: ");

        // Read user input
        String username = myObj.nextLine();

        //send username to server
        output.println(username);

        //prompt for password
        System.out.print("Enter password: ");

        String password = myObj.nextLine();

        //send password to server
        output.println(password);
        output.flush();

        //create Buffered reader for reading response from server
        read = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        int errorCode = read.read();

        //read response from server
        String response = read.readLine();
        System.out.println("\n" + response);

        if(Character.getNumericValue(errorCode) == STATUS.SUCCESS.ordinal())
        {
            showMenu();
        }
        else
        {
            System.out.println("Please try again.");
        }
    }

    public void showMenu()
    {
        // Create a Scanner object
        Scanner scanner = new Scanner(System.in);

        System.out.println("---Menu---");
        System.out.println("""
                    1. Logout
                    """);

        int menuOption;
        do {
            System.out.print("Enter the menu option: ");
            menuOption = scanner.nextInt();
            if (menuOption != 1)
            {
                System.out.println("Menu option is invalid.");
            }
        } while (menuOption != 1);

        output.println(menuOption);
    }

    public static void main(String[] args){
        Client client = new Client();
        try {
            client.startClient();
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

