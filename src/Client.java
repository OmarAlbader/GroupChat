import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    //client socket to communicate with server
    private Socket socket;

    //buffer used to read the data from the server
    private BufferedReader bufferedReader;

    //buffer used to send data to the server
    private BufferedWriter bufferedWriter;

    //client's username
    private String username;


    //client Constructor
    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            this.username = username;
//            System.out.println("You are ready to chat");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    //method to send data that client enters to server
    public void sendMessage() {
        try {
            //send client's username to server
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();


            Scanner scanner = new Scanner(System.in);

            while (socket.isConnected()) {
                //scan client messages from terminal
                String messageToSend = scanner.nextLine();

                //send message to server
                bufferedWriter.write(messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    //method to read data coming from server
    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;

                while (socket.isConnected()) {
                    try {
                        //reading data from server
                        msgFromGroupChat = bufferedReader.readLine();

                        //printing data received from server
                        System.out.println(msgFromGroupChat);
                    } catch (IOException e) {
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
    }

    //method to handle IOException
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null)
                bufferedReader.close();

            if (bufferedWriter != null)
                bufferedWriter.close();

            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        //creating scanner to scan client's username from terminal
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your username for the group chat: ");

        String username = scanner.nextLine();

        //create socket object with server's host and port number
        Socket socket = new Socket("localhost", 1234);

        //create Client object with socket contains server's host and port number, and client's username;
        Client client = new Client(socket, username);

        //run listenForMessage and sendMessage method to start receiving and sending messages from and to the server
        client.listenForMessage();
        client.sendMessage();
    }

}
