import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) throws InterruptedException, IOException {
        Socket socket = null;
        InputStreamReader inputStraemReader = null;
        OutputStreamWriter outputSreamReader = null;
        BufferedReader bufferReader = null;
        BufferedWriter bufferWriter = null;
        ServerSocket serverSocket = null;

        serverSocket = new ServerSocket(12345);

        while(true) {
            try {
                socket = serverSocket.accept();

                inputStraemReader = new InputStreamReader(socket.getInputStream());
                outputSreamReader = new OutputStreamWriter(socket.getOutputStream());

                bufferReader = new BufferedReader(inputStraemReader);
                bufferWriter = new BufferedWriter(outputSreamReader);

                while (true) {
                    String messageFromClient = bufferReader.readLine();

                    System.out.println("Client: " + messageFromClient);

                    bufferWriter.write("Message received.");
                    bufferWriter.newLine();
                    bufferWriter.flush();

                    if (messageFromClient.equalsIgnoreCase("BYE"))
                        break;
                }

                socket.close();
                inputStraemReader.close();
                outputSreamReader.close();
                bufferWriter.close();
                bufferReader.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
