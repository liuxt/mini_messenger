import com.sun.xml.internal.fastinfoset.sax.SystemIdResolver;
import com.sun.xml.internal.ws.api.server.ThreadLocalContainerResolver;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.util.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.Thread;

/**
 * Created by liuxt on 15/12/20.
 */
public class Server_v2 {
    private static final int PORT = 4444;
    private static final int BACKLOG = 100;
    private UserInfoManager userInfoManager;
    private ServerSocket server;

    private class ClientHandler implements Runnable{
        private BufferedReader reader;
        private PrintWriter writer;
        private Socket connection;

        public ClientHandler(Socket sock, BufferedReader bufferedReader, PrintWriter printWriter){
            try {
                System.out.println("Try to new a Thread");
                this.reader = bufferedReader;
                this.writer = printWriter;
                this.connection = sock;
            } catch (NullPointerException nullPointerException){
                System.out.println("Failed to new a ClientHandler");
                nullPointerException.printStackTrace();
            }
        }

        public void run(){
            try {
                System.out.println("Thread running, try to listen from client");
                String message;
                while ((message = reader.readLine()) != null){
                    System.out.println(message);
                }
            } catch(Exception exception){
                System.out.println("Failed to receive message");
                exception.printStackTrace();
            } finally{
               closeCrap(connection, writer, reader);
            }
        }
    }
    // constructor
    public Server_v2(){
        System.out.println("Server is created, waiting for activation");
    }

    // Start the server
    public void startRunning(){
        try {
            server = new ServerSocket(PORT, BACKLOG);
            userInfoManager = new UserInfoManager();
            while(true) {
                try {
                    Socket connection = waitForConnection();
                    SocketPair sockPair = setupStreams(connection);
                    Thread listener = new Thread(new ClientHandler(sockPair.getSock(), sockPair.getReader(), sockPair.getWriter()));
                    listener.start();
                } catch (Exception eofException) { // later use
                    System.out.println("Server ended the connection");
                } finally {
                    //closeCrap();
                }
            }
        } catch(IOException ioException){
            System.out.println("Fail to start run server");
            ioException.printStackTrace();
        }
    }

    private Socket waitForConnection(){
        System.out.println("Wait for incoming connection");
        try {
            Socket connection = server.accept();
            System.out.println("Successfully get connection from " + connection.getInetAddress().getHostName());
            return connection;
        } catch(IOException ioException){
            System.out.println("Unable to accept client socket");
            ioException.printStackTrace();
            return null;
        }
    }


    private SocketPair setupStreams(Socket connection){
       try{
           System.out.println("Trying to setup Streams");
           PrintWriter printWriter = new PrintWriter(connection.getOutputStream());
           BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
           return new SocketPair(connection, bufferedReader, printWriter);

       } catch (IOException ioexception){
           System.out.println("Failed to setup Streams");
           ioexception.printStackTrace();
           return null;
       }
    }

    private void closeCrap(Socket connection, PrintWriter writer, BufferedReader reader){
        try{
            writer.close();
            reader.close();
            connection.close();
            System.out.println("Successfully close streams");
        } catch (IOException ioException){
            System.out.println("Failed to close streams.");
            ioException.printStackTrace();
        }
    }

    public static void main(String[] args){
        Server_v2 server_v2 = new Server_v2();
        server_v2.startRunning();

    }

}
