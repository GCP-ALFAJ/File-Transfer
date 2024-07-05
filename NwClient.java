import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import javax.swing.*;

public class NwClient {

    private static DataOutputStream dataOutputStream = null;
    private static DataInputStream dataInputStream = null;
    private static String ipAddress;
    private static File[] selectedFiles;
    private static Socket clientSocket;
    private static JProgressBar progressBar;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new ClientFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }

    public static class ClientFrame extends JFrame {
        private JTextField ipAddressField;
        private JButton connectButton;
        private JFileChooser fileChooser;

        public ClientFrame() {
            createAndShowGUI();
        }

        private void createAndShowGUI() {
            setTitle("Client");
            setSize(400, 200);
            setLayout(new GridLayout(4, 1));

            JPanel ipAddressPanel = new JPanel();
            ipAddressPanel.add(new JLabel("IP Address:"));
            ipAddressField = new JTextField(15);
            ipAddressPanel.add(ipAddressField);
            connectButton = new JButton("Connect");
            connectButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ipAddress = ipAddressField.getText();
                    connectButton.setEnabled(false);
                    new Thread(() -> connectToServer()).start();
                }
            });
            ipAddressPanel.add(connectButton);
            add(ipAddressPanel);

            fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true); // Allow multiple file selection
            JButton selectFileButton = new JButton("Select Files");
            selectFileButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int returnValue = fileChooser.showOpenDialog(null);
                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        selectedFiles = fileChooser.getSelectedFiles();
                    }
                }
            });
            add(selectFileButton);

            JButton sendFileButton = new JButton("Send Files");
            sendFileButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (selectedFiles != null && selectedFiles.length > 0) {
                        new Thread(() -> sendSelectedFiles(selectedFiles)).start();
                    }
                }
            });
            add(sendFileButton);

            progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(true);
            add(progressBar);
        }

        private void connectToServer() {
            try {
                clientSocket = new Socket(ipAddress, 900);
                dataInputStream = new DataInputStream(clientSocket.getInputStream());
                dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                System.out.println("Connected to the server.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void sendSelectedFiles(File[] files) {
            try {
                dataOutputStream.writeInt(files.length); // Send number of files
                for (File file : files) {
                    byte[] fileData = Files.readAllBytes(file.toPath());
                    dataOutputStream.writeUTF(file.getName()); // Send file name
                    dataOutputStream.writeLong(fileData.length); // Send file size

                    long totalBytes = fileData.length;
                    long bytesSent = 0;
                    int bytes;
                    byte[] buffer = new byte[4 * 1024];
                    InputStream fileInputStream = new FileInputStream(file);

                    while ((bytes = fileInputStream.read(buffer)) != -1) {
                        dataOutputStream.write(buffer, 0, bytes);
                        bytesSent += bytes;
                        int progress = (int) ((bytesSent * 100) / totalBytes);
                        progressBar.setValue(progress);
                    }

                    fileInputStream.close();
                    dataOutputStream.flush();
                    System.out.println("File sent: " + file.getName());
                }

                // Close the socket after sending all files
                dataInputStream.close();
                dataOutputStream.close();
                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
