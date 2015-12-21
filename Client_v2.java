/**
 * Created by liuxt on 15/12/20.
 */
import java.net.*;
import java.rmi.UnexpectedException;
import java.util.*;
import java.io.*;

public class Client_v2 {
    private String userName;
    private SocketPair userSockPair;
    private UserInfo userInfo;

    public Client_v2(String userName, SocketPair userSockPair, UserInfo userInfo) {
        this.userName = userName;
        this.userSockPair = userSockPair;
        this.userInfo = userInfo;
    }

    public static void main(String[] args) throws IOException{
        String name = args[0];
        String message;
        Socket sock = new Socket("localhost", 4444);
        PrintWriter writer= new PrintWriter(sock.getOutputStream(), true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while(true){
            message = reader.readLine();
            System.out.println("message is: " + message + "\n from " + name);
            writer.println(name + " : " + message);
        }
    }
}
