import java.io.*;
import java.net.*;

public class NwServer {

    private static DataOutputStream dataOutputStream = null;
    private static DataInputStream dataInputStream = null;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(900)) {
            System.out.println("Server is Starting in Port 900");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Connected");
            dataInputStream = new DataInputStream(clientSocket.getInputStream());
            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            receiveFile("NewFile1.pdf");

            dataInputStream.close();
            dataOutputStream.close();
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void receiveFile(String fileName) throws Exception {
        try {
            long size = dataInputStream.readLong();
            System.out.println("File size: " + size);
            byte[] buffer = new byte[4 * 1024];
            long received = 0;
            int bytes;

            FileOutputStream fileOutputStream = new FileOutputStream(fileName);

            while (received < size) {
                bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, size - received));
                if (bytes == -1) {
                    throw new IOException("Unexpected end of file");
                }
                fileOutputStream.write(buffer, 0, bytes);
                received += bytes;
            }

            fileOutputStream.close();
            System.out.println("File is Received");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
