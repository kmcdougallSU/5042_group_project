import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws InterruptedException
    {
        Socket socket = null;
        InputStreamReader inputStraemReader = null;
        OutputStreamWriter outputSreamReader = null;
        BufferedReader bufferReader = null;
        BufferedWriter bufferWriter = null;

        try{
            socket = new Socket("localhost", 12345);

            inputStraemReader = new InputStreamReader(socket.getInputStream());
            outputSreamReader = new OutputStreamWriter(socket.getOutputStream());

            bufferReader = new BufferedReader(inputStraemReader);
            bufferWriter = new BufferedWriter(outputSreamReader);

            Scanner scanner = new Scanner(System.in);

            while (true)
            {
                System.out.print("Input: ");
                String messageToSend = scanner.nextLine();
                bufferWriter.write(messageToSend);
                bufferWriter.newLine();
                bufferWriter.flush();

                System.out.println("Server: " + bufferReader.readLine());

                if (messageToSend.equalsIgnoreCase("BYE"))
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                if (socket != null)
                {
                    socket.close();
                }
                if (inputStraemReader != null)
                {
                    inputStraemReader.close();
                }
                if(outputSreamReader != null)
                {
                    outputSreamReader.close();
                }
                if (bufferReader != null)
                {
                    bufferReader.close();
                }
                if (bufferWriter != null)
                {
                    bufferWriter.close();
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
