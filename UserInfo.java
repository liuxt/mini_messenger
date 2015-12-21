import java.io.BufferedReader;
import java.io.PrintWriter;

/**
 * Created by liuxt on 15/12/21.
 */
public class UserInfo {
    private String userName;
    private int userPassWord;
    private BufferedReader userReader;
    private PrintWriter userWriter;

    // constructor
    public UserInfo(String userName, int userPassWord, BufferedReader userReader, PrintWriter userWriter) {
        this.userName = userName;
        this.userPassWord = userPassWord;
        this.userReader = userReader;
        this.userWriter = userWriter;
    }

    public UserInfo(String userName, int userPassWord) {
        this.userName = userName;
        this.userPassWord = userPassWord;
    }

    // getters
    public String getUserName() {
        return userName;
    }

    public int getUserPassWord() {
        return userPassWord;
    }

    public BufferedReader getUserReader() {
        return userReader;
    }

    public PrintWriter getUserWriter() {
        return userWriter;
    }

    // setters
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserPassWord(int userPassWord) {
        this.userPassWord = userPassWord;
    }

    public void setUserReader(BufferedReader userReader) {
        this.userReader = userReader;
    }

    public void setUserWriter(PrintWriter userWriter) {
        this.userWriter = userWriter;
    }
}
