import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.*; 

public class NwClient {

    private static DataOutputStream dataOutputStream = null;
    private static DataInputStream dataInputStream = null;
    private static String ipAddress;
    private static File selectedFile;
    private static Socket clientSocket;

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
            setLayout(new GridLayout(3, 1));

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
            JButton selectFileButton = new JButton("Select File");
            selectFileButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int returnValue = fileChooser.showOpenDialog(null);
                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        selectedFile = fileChooser.getSelectedFile();
                    }
                }
            });
            add(selectFileButton);

            JButton sendFileButton = new JButton("Send File");
            sendFileButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (selectedFile != null) {
                        new Thread(() -> sendSelectedFile(selectedFile)).start();
                    }
                }
            });
            add(sendFileButton);
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

        private void sendSelectedFile(File file) {
            try {
                byte[] fileData = Files.readAllBytes(file.toPath());
                dataOutputStream.writeLong(fileData.length);
                dataOutputStream.write(fileData, 0, fileData.length);
                dataOutputStream.flush();
                System.out.println("File sent: " + file.getName());

                // Close the socket after sending the file
                dataInputStream.close();
                dataOutputStream.close();
                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}