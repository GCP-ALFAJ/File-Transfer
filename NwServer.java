import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class NwServer {

    private static DataOutputStream dataOutputStream = null;
    private static DataInputStream dataInputStream = null;
    private static JProgressBar progressBar;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new ServerFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });

        try (ServerSocket serverSocket = new ServerSocket(900)) {
            System.out.println("Server is Starting in Port 900");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Connected");
            dataInputStream = new DataInputStream(clientSocket.getInputStream());
            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            
            int numberOfFiles = dataInputStream.readInt(); // Read number of files
            for (int i = 0; i < numberOfFiles; i++) {
                String fileName = dataInputStream.readUTF(); // Read file name
                receiveFile(fileName);
            }

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
            System.out.println("Receiving file: " + fileName + " (Size: " + size + " bytes)");
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

                int progress = (int) ((received * 100) / size);
                progressBar.setValue(progress);
            }

            fileOutputStream.close();
            System.out.println("File is Received: " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class ServerFrame extends JFrame {
        public ServerFrame() {
            createAndShowGUI();
        }

        private void createAndShowGUI() {
            setTitle("Server");
            setSize(400, 100);
            setLayout(new GridLayout(1, 1));

            progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(true);
            add(progressBar);
        }
    }
}
