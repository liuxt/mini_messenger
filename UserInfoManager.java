import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by liuxt on 15/12/18.
 */
public class UserInfoManager {
    private ArrayList<String> users;
    private ConcurrentHashMap<String, UserInfo> information;

    public UserInfoManager() {
        users = new ArrayList<String>();
        information = new ConcurrentHashMap<String, UserInfo>();
    }
    public boolean isContains(String username){
        return users.contains(username);
    }
    public void addUser(String username, int userPassWord, BufferedReader userReader, PrintWriter userWriter) {
        this.users.add(username);
        UserInfo info = new UserInfo(username, userPassWord, userReader, userWriter);
        this.information.put(username, info);
    }
    public void addUser(String username, int userPassWord){
        this.users.add(username);
        UserInfo info = new UserInfo(username, userPassWord);
        this.information.put(username, info);
    }
    public boolean isPassWordCorrect(String username, int password){
        int correctPassword = information.get(username).getUserPassWord();
        return password == correctPassword;
    }

    public static void main(String[] args){
        UserInfoManager uim = new UserInfoManager();
        uim.addUser("alice", 123);
        uim.addUser("bob", 456);
        uim.addUser("cat", 789);
        System.out.println(uim.isContains("alice"));
        System.out.println(uim.isContains("bob"));
        System.out.println(uim.isContains("cat"));
        System.out.println(uim.isContains("karen"));
        System.out.println(uim.isPassWordCorrect("alice",123));
        System.out.println(uim.isPassWordCorrect("alice", 456));

    }


}
