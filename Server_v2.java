import com.sun.xml.internal.fastinfoset.sax.SystemIdResolver;
import com.sun.xml.internal.ws.api.server.ThreadLocalContainerResolver;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.util.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.Thread;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by liuxt on 15/12/20.
 */
public class Server_v2 {
    private static final int PORT = 4444;
    private static final int BACKLOG = 100;
    private ConcurrentHashMap<String, String> userNameAndPassword;
    private ConcurrentHashMap<String, Socket> onlineNameAndSocket;
    private UserInfoManager userInfoManager;
    private ServerSocket server;
    private ConcurrentLinkedDeque<ClientRequest> eventQueue;

    private class ClientHandler implements Runnable{
        private String clientName;
        private BufferedReader reader;
        private PrintWriter writer;
        private Socket connection;
        private SocketPair socketPair;

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
                System.out.println("Thread running...");
                register();
                login();
                reader.readLine();      // consume one enter
                System.out.println("Now the interpreter begins!");
                String rawRequest;
                while ((rawRequest = reader.readLine()) != null){
                    ClientRequest request = new ClientRequest(rawRequest);
                    if(request.getType().equals(ClientRequest.ClientRequestType.PrivateFile) || request.getType().equals(ClientRequest.ClientRequestType.ChatFile)){
                        if(request.getType().equals(ClientRequest.ClientRequestType.PrivateFile)) {
                            recvPrivateFile(request);
                        }
                        else{
                            recvChatFile(request);
                        }
                    }
                    eventQueue.add(request);
                }
            } catch(Exception exception){
                System.out.println("Failed to receive message");
                exception.printStackTrace();
            } finally{
               closeCrap(connection, writer, reader);
            }
        }

        private void register(){
            try {
                int registerCount = 3;
                while(registerCount > 0) {
                    writer.println("Server ready for register, if you want to register type Y");
                    String userInput = reader.readLine();
                    if (userInput.equals("Y")) {
                        String name;
                        String passWord;
                        writer.println("Please enter a username for register");
                        name = reader.readLine();
                        if (userNameAndPassword.containsKey(name)) {
                            writer.println("User name: " + name + " has been used. try again!");
                        }
                        else {
                            writer.println("User name:" + name + " good to use, now type your password.");
                            passWord = reader.readLine();
                            writer.println("Type again to verify.");
                            String passWordAgain = reader.readLine();
                            if (passWordAgain.equals(passWord)) {
                                userNameAndPassword.put(name, passWord);
                                writer.println("Register successfully!");
                                break;
                            }
                            else {
                                writer.println("Password not match, try again!");
                            }
                        }
                    }
                    else{
                        writer.println("You don't want to register, go ahead for login!");
                        break;
                    }
                    registerCount--;
                }
                if (registerCount == 0){
                    writer.println("Register Failed! Go ahead for login");
                }
            }catch (IOException ioexception){
                System.out.println("Error: Failed to register!");
                ioexception.printStackTrace();
            }
        };

        private void login(){
            try {
                int loginCount = 3;
                String name;
                String passWord;
                writer.println("Server ready for login...");
                while (loginCount > 0) {
                    writer.println("Please enter your name. Type enter to continue.");
                    name = reader.readLine();
                    if(userNameAndPassword.containsKey(name)) {
                        writer.println( name  + ", please enter your passWord");
                        passWord = reader.readLine();
                        if(passWord.equals(userNameAndPassword.get(name))){
                            if(!onlineNameAndSocket.containsKey(name)) {
                                clientName = name;
                                writer.println("Server Connected");
                                writer.println(clientName);
                                sendOnlineUsers();
                                knock();
                                return;
                            }
                            else{
                                writer.println("User " + name + " already online! Login failed.");
                                loginCount--;
                            }
                        }
                        else{
                            writer.println("Password not correct! Try again!");
                            loginCount--;
                        }
                    }
                    else{
                        writer.println("User name: " + name + " not exists, try again!");
                        loginCount--;
                    }
                }
                if(loginCount == 0){
                    writer.println("Login failed, you are disconnected");
                    closeCrap(connection, writer, reader);
                    return;
                }
            } catch (IOException ioException){
                System.out.println("Error: Failed to login!");
                ioException.printStackTrace();
            }
        }

        private void sendOnlineUsers(){
            onlineNameAndSocket.put(clientName, connection);
            System.out.println("clientName and connection: " + clientName + connection);
            Enumeration<String> onlineUsers = onlineNameAndSocket.keys();
            while(onlineUsers.hasMoreElements()){
                writer.println(onlineUsers.nextElement());
            }
            writer.println("done");
        }

        private void knock(){
            try {
                for (Socket s : onlineNameAndSocket.values()) {
                    if (!s.equals(connection))
                        synchronized (s) {
                            PrintWriter tempWriter = new PrintWriter(s.getOutputStream(), true);
                            tempWriter.println("UserAdd " + clientName);
                            System.out.println("knock!" + "connection: " + s);
                        }
                }
            } catch (IOException ioException){
                System.out.println("Error: Failed to knock others");
                ioException.printStackTrace();
            }
        }
        private void recvPrivateFile(ClientRequest request){
            try{
                ArrayList<String> args = request.getArgs();
                OutputStream outFile = new FileOutputStream(args.get(3)+"receiverSide");
                Socket s = onlineNameAndSocket.get(args.get(1));
                byte[] bytes = new byte[8192];
                int fileLength = Integer.parseInt(args.get(4));
                int result = 0;
                int count = 0;
                synchronized (s) {
                    PrintWriter fileNoticer = new PrintWriter(s.getOutputStream(), true);
                    System.out.println("Ready to receive file " + args.get(3));
                    fileNoticer.println("ReadyReceive " + args.get(3));
                    InputStream inComingFile = s.getInputStream();
                    while (result < fileLength && (count = inComingFile.read(bytes)) > 0) {
                        result += count;
                        outFile.write(bytes, 0, count);
                        System.out.println(result);
                    }
                }
                System.out.println("recvFile done!");
            } catch(IOException ioException){
                ioException.printStackTrace();
            }
        }
        private void recvChatFile(ClientRequest request){
            try{
                ArrayList<String> args = request.getArgs();
                OutputStream outFile = new FileOutputStream(args.get(2)+"receiverSide");
                Socket s = onlineNameAndSocket.get(args.get(1));
                byte[] bytes = new byte[8192];
                int fileLength = Integer.parseInt(args.get(3));
                int result = 0;
                int count = 0;
                synchronized (s) {
                    PrintWriter fileNoticer = new PrintWriter(s.getOutputStream(), true);
                    System.out.println("Ready to receive file " + args.get(2));
                    fileNoticer.println("ReadyReceive " + args.get(2));
                    InputStream inComingFile = s.getInputStream();
                    while (result < fileLength && (count = inComingFile.read(bytes)) > 0) {
                        result += count;
                        outFile.write(bytes, 0, count);
                        System.out.println(result);
                    }
                }
                System.out.println("recvFile done!");
            } catch(IOException ioException){
                ioException.printStackTrace();
            }
        }

    }

    // constructor
    public Server_v2(){
        init();
        System.out.println("Server is created, waiting for activation");
    }

    // Start the server
    public void startRunning(){
        try {
            server = new ServerSocket(PORT, BACKLOG);
            userInfoManager = new UserInfoManager();
            Thread mainThread = runMainThread();
            mainThread.start();
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
           PrintWriter printWriter = new PrintWriter(connection.getOutputStream(), true);
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

    private void init(){
        userNameAndPassword = new ConcurrentHashMap<String, String>();
        userNameAndPassword.put("alice", "123");
        userNameAndPassword.put("bob", "456");
        userNameAndPassword.put("jack", "123");
        userNameAndPassword.put("liuxt", "123");
        onlineNameAndSocket = new ConcurrentHashMap<String, Socket>();
        eventQueue = new ConcurrentLinkedDeque<ClientRequest>();
    }

    private Thread runMainThread(){
        Thread handleQueueEvent = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    ClientRequest request = eventQueue.poll();
                    if(request != null){
                        interpret(request);
                    }
                }
            }
        });
        return handleQueueEvent;
    }
    private void interpret(ClientRequest request){
        ArrayList<String> args = request.getArgs();
        switch (request.getType()){
            case PrivateMsg:
                sendToClient(args.get(1), args.get(2), args.get(3));
                break;
            case ChatMsg:
                sendToAll(args.get(1), args.get(2));
                break;
            case PrivateFile:
                sendPrivateFileNoticerToClient(args.get(1), args.get(2), args.get(3), args.get(4));
                break;
            case ChatFile:
                sendChatFileNoticerToAll(args.get(1), args.get(2), args.get(3));
                break;
            case ReadyRecv:
                sendFileToClient(args.get(1), args.get(2));
                break;
            case Disconnect:
                broadcastUserDel(args.get(1));
                break;
            default:
                System.out.println("ERROR: unknown clientRequest");
        }
    }
    private void sendToClient(String sender, String receiver, String content){
        try{
            Socket receiverSocket;
            String message;
            if(onlineNameAndSocket.containsKey(receiver)){
                receiverSocket = onlineNameAndSocket.get(receiver);
                synchronized (receiverSocket){
                    message = "PrivateMessage" + " " + sender + " " + content;
                    PrintWriter printWriter = new PrintWriter(receiverSocket.getOutputStream(), true);
                    printWriter.println(message);
                }
            }
            else{
                System.out.println("ERROR: receiver not online!");
            }
        } catch (IOException ioException){
            ioException.printStackTrace();
        }
    }
    private void sendToAll(String sender, String content){
        try {
            Socket senderSocket = onlineNameAndSocket.get(sender);
            String message;
            for (Socket receiverSocket: onlineNameAndSocket.values()) {
                if (!receiverSocket.equals(senderSocket)) {
                    synchronized (receiverSocket) {
                        message = "ChatMessage" + " " + sender + " " + content;
                        PrintWriter printWriter = new PrintWriter(receiverSocket.getOutputStream(), true);
                        printWriter.println(message);
                    }
                }
            }
        } catch (IOException ioException){
            ioException.printStackTrace();
        }
    }
    private void sendPrivateFileNoticerToClient(String sender, String receiver, String content, String fileLength){
        try {
            if(onlineNameAndSocket.containsKey(receiver)) {
                Socket receiverSocket = onlineNameAndSocket.get(receiver);
                synchronized (receiverSocket) {
                    PrintWriter printWriter = new PrintWriter(receiverSocket.getOutputStream(), true);
                    printWriter.println("PrivateFile" + " " + sender + " " + content + " " + fileLength);
                }
            }
            else {
                System.out.println("ERROR: fileReceiver not online!");
            }
        } catch (IOException ioException){

        }
    }
    private void sendChatFileNoticerToAll(String sender, String content, String fileLength){
        try {
            Socket senderSocket = onlineNameAndSocket.get(sender);
            for(Socket receiverSocket: onlineNameAndSocket.values()) {
                if(!senderSocket.equals(receiverSocket)) {
                    synchronized (receiverSocket) {
                        PrintWriter printWriter = new PrintWriter(receiverSocket.getOutputStream(), true);
                        printWriter.println("ChatFile" + " " + sender + " " + content + " " + fileLength);
                    }
                }
            }
        } catch (IOException ioException){

        }
    }
    private void sendFileToClient(String receiver, String fileName){
        try{
            System.out.println("going to send " + fileName);
            if(onlineNameAndSocket.containsKey(receiver)) {

                Socket receiverSocket = onlineNameAndSocket.get(receiver);
                synchronized (receiverSocket) {
                    byte[] bytes = new byte[8192];
                    int count = 0;
                    File file = new File(fileName+"receiverSide");
                    InputStream fileIn = new FileInputStream(file);
                    OutputStream out = receiverSocket.getOutputStream();
                    out.flush();
                    while ((count = fileIn.read(bytes)) != -1) {
                        out.write(bytes, 0, count);
                    }
                    System.out.println("Done!");
                }
            }
            else{
                System.out.println("Error: file " + fileName + " not exists");
            }

        } catch (IOException ioExcetption){
            ioExcetption.printStackTrace();
        }
    }
    private void broadcastUserDel(String theOneToLeave){
        try {
            String message;
            onlineNameAndSocket.remove(theOneToLeave);          // remove theOneToLeave
            for (Socket receiverSocket: onlineNameAndSocket.values()) {
                synchronized (receiverSocket) {
                    message = "UserDel" + " " + theOneToLeave;
                    PrintWriter printWriter = new PrintWriter(receiverSocket.getOutputStream(), true);
                    printWriter.println(message);
                }
            }
        } catch (IOException ioException){
            ioException.printStackTrace();
        }
    }
    public static void main(String[] args){
        Server_v2 server_v2 = new Server_v2();
        server_v2.startRunning();

    }

}
