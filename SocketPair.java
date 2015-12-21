import java.io.BufferedReader;
import java.net.*;
import java.io.PrintWriter;
/**
 * Created by liuxt on 15/12/20.
 */
public class SocketPair {
    private Socket sock;
    private BufferedReader reader;
    private PrintWriter writer;

    public SocketPair(Socket sock, BufferedReader reader, PrintWriter writer) {
        this.sock = sock;
        this.reader = reader;
        this.writer = writer;
    }

    public Socket getSock() {
        return sock;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }

    public void setSock(Socket sock) {
        this.sock = sock;
    }

    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }
}
