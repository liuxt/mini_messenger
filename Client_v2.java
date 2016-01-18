/**
 * Created by liuxt on 15/12/20.
 */
import com.sun.xml.internal.bind.api.impl.NameConverter;

import java.net.*;
import java.rmi.UnexpectedException;
import java.util.*;
import java.io.*;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;

public class Client_v2 {
    private String userName;
    private String address = "localhost";
    private int port = 4444;
    private ArrayList<String> usersList;
    private int passWord;
    private Socket userSocket;
    private PrintWriter userWriter;
    private BufferedReader userReader;
    private UserInfo userInfo;
    private boolean isconnected = false;
    private boolean closed = false;
    private BufferedReader standardReader = new BufferedReader(new InputStreamReader(System.in));

    public Client_v2(){
        startRunning();
    }

    private void startRunning(){
        try{
            printMessage("Trying to new a client socket...");
            userSocket = new Socket(address, port);
            printMessage("Client socket successfully created");
            userWriter = new PrintWriter(userSocket.getOutputStream(), true);
            userReader = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
            printMessage("Client streams successfully set up");
            if(!isconnected) {
                createListenThread();
                while(!closed){
                    if(isconnected) break;
                    userWriter.println(standardReader.readLine().trim());
                }
                while(!closed && isconnected){
                    String command = "";
                    String toWhom = "";
                    printMessage("Now enter your command:");
                    command = standardReader.readLine();
                    commandResolver(command);
                }
            }
            else{
                printMessage("Already connected, close connection");
                closeCrap(userSocket, userWriter, userReader);
            }
        } catch (IOException ioException){
            System.out.println("Failed to start client");
            ioException.printStackTrace();
        } finally {
            closeCrap(userSocket, userWriter, userReader);
        }
    }

    private void createListenThread(){
        Thread listener = new Thread(new IncommingReader());
        listener.start();
        printMessage("Listener successfully created!");
    }

    private class IncommingReader implements Runnable{
        @Override
        public void run() {
            try {
                String inCommingMessage;
                while ((inCommingMessage = userReader.readLine()) != null) {
                    printMessage(inCommingMessage);
                    if(inCommingMessage.equals("Server Connected")){
                        userName = userReader.readLine();
                        printMessage("your user name is " + userName);
                        isconnected = true;
                        usersList = new ArrayList<String>();
                        while( (inCommingMessage = userReader.readLine()) != null){
                            if(inCommingMessage.equals("done")) break;
                            usersList.add(inCommingMessage);
                            printMessage("Users are:" + inCommingMessage);
                        }
                        printMessage("Next stage is interpret! Please press ENTER to continue");
                        break;
                    }
                }
                while( (inCommingMessage = userReader.readLine()) != null){
                    interpret(inCommingMessage);
                }
            } catch (IOException ioException){
                printMessage("Failed to listen from server");
                ioException.printStackTrace();
            }
        }
    }

    private void interpret(String requestRaw){
        ServerRequest request = new ServerRequest(requestRaw);
        ArrayList<String> args = request.getArgs();
        switch (request.getType()){
            case USERADD:
                userAddToUserList(args.get(1));
                break;
            case USERDEL:
                userDelFromUserList(args.get(1));
                break;
            case PRIVATEMSG:
                showPrivateMessage(args.get(1), args.get(2));
                break;
            case CHATMSG:
                showChatMessage(args.get(1), args.get(2));
                break;
            case READYRECV:
                sendFileToServer(args.get(1));
                break;
            case PRIVATEFILE:
                prepare4PrivateFile(args.get(1), args.get(2), args.get(3));
                break;
            case CHATFILE:
                prepare4ChatFile(args.get(1), args.get(2), args.get(3));
                break;
            default:
                printMessage("Error: request invalid!");
                break;
        }
    }
    private void userAddToUserList(String user){
        if(usersList.contains(user)){
            printMessage("Error: failed to add user " + user + ", user already exists");
        }
        else{
            usersList.add(user);
            printMessage("Successfully add user " + user + ", users are:");
            for(String s: usersList){
                printMessage(s);
            }

        }
    }
    private void userDelFromUserList(String user){
        if(!usersList.contains(user)){
            printMessage("Error: failed to del user " + user + ", user not exists");
        }
        else{
            usersList.remove(user);
            printMessage("Successfully del user " + user + ", users are:");
            for(String s: usersList){
                printMessage(s);
            }

        }
    }
    private void showPrivateMessage(String sender, String content){
        String s = "PrivateMessage From " + sender + " :" + content;
        printMessage(s);
    }
    private void showChatMessage(String sender, String content){
        String s = "ChatMessage From " + sender + " :" + content;
        printMessage(s);
    }
    private void sendFileToServer(String fileName){
        try{
            synchronized (userSocket) {
                printMessage("Receiving file " + fileName + ", dont send other things for now!");
                byte[] bytes = new byte[8192];
                int count = 0;
                int result = 0;
                File file = new File(fileName);
                System.out.println(file.length());
                InputStream fileIn = new FileInputStream(file);
                OutputStream out = userSocket.getOutputStream();
                out.flush();
                while ((count = fileIn.read(bytes)) != -1) {
                    out.write(bytes, 0, count);
                    result += count;
                }
                printMessage("Sending file" + fileName + " successes, now continue!");
            }

        } catch (IOException ioExcetption){
            ioExcetption.printStackTrace();
        }
    }

    private void prepare4PrivateFile(String sender, String fileName, String fileLength){
        try{
            OutputStream outFile = new FileOutputStream(fileName+"from"+sender+"to"+userName);
            byte[] bytes = new byte[8192];
            int count = 0;
            int result = 0;
            int length = Integer.parseInt(fileLength);
            synchronized (userSocket) {
                PrintWriter fileNoticer = new PrintWriter(userSocket.getOutputStream(), true);
                printMessage("Ready to receive file " + fileName);
                fileNoticer.println("ReadyReceive" + " " + userName + " " + fileName);
                InputStream inComingFile = userSocket.getInputStream();
                while (result < length && (count = inComingFile.read(bytes)) != -1) {
                    result += count;
                    outFile.write(bytes, 0, count);
                }
            }
        } catch(IOException ioException) {
            ioException.printStackTrace();
        }
    }
    private void prepare4ChatFile(String sender, String fileName, String fileLength){
        try{
            OutputStream outFile = new FileOutputStream(fileName+"from"+sender+"to"+userName+"_chatroom");
            byte[] bytes = new byte[8192];
            int count = 0;
            int result = 0;
            int length = Integer.parseInt(fileLength);
            synchronized (userSocket) {
                PrintWriter fileNoticer = new PrintWriter(userSocket.getOutputStream(), true);
                printMessage("Ready to receive file " + fileName);
                fileNoticer.println("ReadyReceive" + " " + userName + " " + fileName);
                InputStream inComingFile = userSocket.getInputStream();
                while (result < length && (count = inComingFile.read(bytes)) != -1) {
                    result += count;
                    outFile.write(bytes, 0, count);
                }
            }
        } catch(IOException ioException) {
            ioException.printStackTrace();
        }
    }
    private void commandResolver(String command){
        String receiver = "";
        String content = "";
        String request = "";
        if(command.equals("PrivateMessage")){
            printMessage("Please enter the receiver:");
            receiver = readMessage();
            if(usersList.contains(receiver)){
                printMessage("Please enter the content:");
                content = readMessage();
                request = command + " " + userName + " " + receiver + " " + content;
                userWriter.println(request);
            }
            else{
                printMessage("Receiver " + receiver + " not online! Try again");
            }
        }
        else if(command.equals("ChatMessage")){
            printMessage("Please enter the content:");
            content = readMessage();
            request = command + " " + userName + " " + receiver + " " + content;
            userWriter.println(request);
        }
        else if(command.equals("PrivateFile")){
            printMessage("Please enter the receiver:");
            receiver = readMessage();
            if(usersList.contains(receiver)){
                printMessage("Please enter the content:");
                content = readMessage();
                File checkfile = new File(content);
                if(checkfile.exists()) {
                    request = command + " " + userName + " " + receiver + " " + content + " " + checkfile.length();
                    userWriter.println(request);
                }
                else{
                    printMessage("File " + content + " not exist! Please input the right file name");
                }
            }
            else{
                printMessage("Receiver " + receiver + " not online! Try again");
            }
        }
        else if(command.equals("ChatFile")){
            printMessage("Please enter the content:");
            content = readMessage();
            File checkfile = new File(content);
            if(checkfile.exists()) {
                request = command + " " + userName + " " + content + " " + checkfile.length();
                userWriter.println(request);
            }
            else{
                printMessage("File " + content + " not exist! Please input the right file name");
            }
        }
        else if(command.equals("Disconnect")){
            printMessage("Please type Y to confirm!");
            String response = readMessage();
            if(response.equals("Y")){
                userWriter.println("Disconnect" + " " + userName);
                closeCrap(userSocket, userWriter, userReader);
            }
        }
        else{
            printMessage("Unvalid command! Try again");
        }
    }
    private String readMessage(){
        try {
            return standardReader.readLine();
        } catch(IOException ioException){
            printMessage("Failed to read from standard input");
            ioException.printStackTrace();
            return null;
        }
    }

    private void printMessage(String s){
        System.out.println(s);
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

    public static void main(String[] args) throws IOException {
//        String name = args[0];
//        String message;
//        Socket sock = new Socket("localhost", 4444);
//        PrintWriter writer= new PrintWriter(sock.getOutputStream(), true);
//        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//        while(true){
//            message = reader.readLine();
//            System.out.println("message is: " + message + "\n from " + name);
//            writer.println(name + " : " + message);
//        }
        Client_v2 client = new Client_v2();
    }
}
