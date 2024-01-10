import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{
    //static list contains all client handlers
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    //static list contains all client usernames
    public static ArrayList<String> names = new ArrayList<>();

    //socket to communicate with the user
    private Socket socket;

    //buffer used to read the data from the user
    private BufferedReader bufferedReader;

    //buffer used to write data to the user
    private BufferedWriter bufferedWriter;

    //client's username
    public String clientUsername;


    //ClientHandler constructor
    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();

            // ensuring the first client entering the chat haven't empty name
            if (names.isEmpty()) {
                while (clientUsername.equals("")) {
                    bufferedWriter.write("Username can't be empty. Try Again");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    this.clientUsername = bufferedReader.readLine();
                }

                //greeting the user
                bufferedWriter.write("welcome " + clientUsername.toUpperCase() + ", You are ready to chat");
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }

            // ensuring each client has distinct name and no one have empty name
            else {
                while (names.contains(this.clientUsername) || clientUsername.equals("")) {
                    // if username is empty write error message to client
                    if (clientUsername.equals("")) {
                        bufferedWriter.write("Username can't be empty. Try Again");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }
                    else {
                        //if username already exist write error message to client
                        bufferedWriter.write("This name already exist. Try Again.");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }

                    this.clientUsername = bufferedReader.readLine();
                }

                //greeting the user
                bufferedWriter.write("Welcome " + clientUsername.toUpperCase() + ", You are ready to chat");
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }

            //add client handler to clientHandlers list
            clientHandlers.add(this);

            //add client username to names list
            names.add(this.clientUsername);

            //broadcast a user entered the chat to all other clients
            broadcastMessage("SERVER: [" + clientUsername + "] has entered the chat!");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        while(socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();

                //ensure messages are not empty
                while (messageFromClient.equals("")) {
                    messageFromClient = bufferedReader.readLine();
                }

                System.out.println("{" + messageFromClient + "} received from: [" + clientUsername + "]");

                System.out.println("Broadcasting {" + messageFromClient + "}...");
                broadcastMessage(clientUsername + ": " + messageFromClient);

                System.out.println("{" + messageFromClient + "} Broadcasted successfully\n");
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    //method to broadcast message to all clients in the clientHandlers list
    public void broadcastMessage(String messageToSend) {
        //loop through each clientHandler in clientHandlers list
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                //broadcasting messages to all clients except sender client
                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    //method to remove client from clientHandler
    public void removeClientHandler() {
        clientHandlers.remove(this);

        //broadcast to all other clients that this client left the chat
        broadcastMessage("SERVER: " + clientUsername + " has left the chat!");
    }

    //method to handle IOException
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();

        //ensuring bufferReader, bufferWriter and socket != null to avoid NullPointerException
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
}
