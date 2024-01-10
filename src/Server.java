import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    //creating server socket to accept any incoming request from clients
    private final ServerSocket serverSocket;


    //Server constructor
    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        try {
            System.out.println("Server is Ready.");

            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();

                //creating clientHandler for each client connects to the server
                ClientHandler clientHandler = new ClientHandler(socket);

                //printing client's username that connected to the server
                System.out.println("[" + clientHandler.clientUsername + "] has connected!");

                //creating new thread for each client (multithreading)
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    //method to handle IOException
    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main (String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
