import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.Pack200;

/**
 * Created by liuxt on 16/1/16.
 */
public class ServerRequest {
    private String message;
    private List<String> args;
    private ServerRequestType type;
    public enum ServerRequestType{
        USERADD,
        USERDEL,
        PRIVATEMSG,
        CHATMSG,
        PRIVATEFILE,
        CHATFILE,
        READYRECV,
    }
    public ServerRequest(String msg){
        message = msg;
        args = setArgs(msg);
        type = setType();
    }
    private List<String> setArgs(String msg) {
        StringTokenizer tokenizer = new StringTokenizer(msg, " ");
        ArrayList<String> tokens = new ArrayList<String>();
        tokens.add(tokenizer.nextToken());

        if(tokens.get(0).equals("PrivateMessage")){
            tokens.add(tokenizer.nextToken());              // 1 = from whom
            String text = "";
            while(tokenizer.hasMoreTokens()){             // 2 = text
                text += tokenizer.nextToken();
                if(tokenizer.hasMoreTokens()){
                    text += " ";
                }
            }
            tokens.add(text);
        }

        else if(tokens.get(0).equals("ChatMessage")){
            tokens.add(tokenizer.nextToken());            // 1 = from whom
            String text = "";
            while(tokenizer.hasMoreTokens()){             // 2 = text
                text += tokenizer.nextToken();
                if(tokenizer.hasMoreTokens()){
                    text += " ";
                }
            }
            tokens.add(text);
        }

        else if(tokens.get(0).equals("UserAdd")){
            tokens.add(tokenizer.nextToken());            // 1 = who is to be added
        }

        else if(tokens.get(0).equals("UserDel")){
            tokens.add(tokenizer.nextToken());            // 1 = who is to be deleted
        }

        else if (tokens.get(0).equals("PrivateFile")){
            tokens.add(tokenizer.nextToken());            // 1 = from whom
            tokens.add(tokenizer.nextToken());            // 2 = what is the filename
            tokens.add(tokenizer.nextToken());            // 3 = how many bytes it has
        }

        else if(tokens.get(0).equals("ChatFile")){
            tokens.add(tokenizer.nextToken());            // 1 = from whom
            tokens.add(tokenizer.nextToken());            // 2 = what is the filename
            tokens.add(tokenizer.nextToken());            // 3 = how many bytes it has
        }

        else if(tokens.get(0).equals("ReadyReceive")){
            tokens.add(tokenizer.nextToken());            // 1 = what file server waits for
        }

        else{
            return null;                                  // invalid message
        }

        return tokens;
    }
    private ServerRequestType setType(){
        if(this.args.get(0).equals("PrivateMessage")){
            return ServerRequestType.PRIVATEMSG;
        }
        else if(this.args.get(0).equals("ChatMessage")){
            return ServerRequestType.CHATMSG;
        }
        else if(this.args.get(0).equals("UserAdd")){
            return ServerRequestType.USERADD;
        }
        else if(this.args.get(0).equals("UserDel")){
            return ServerRequestType.USERDEL;
        }
        else if(this.args.get(0).equals("PrivateFile")){
            return ServerRequestType.PRIVATEFILE;
        }
        else if(this.args.get(0).equals(("ChatFile"))){
            return ServerRequestType.CHATFILE;
        }
        else if(this.args.get(0).equals(("ReadyReceive"))){
            return ServerRequestType.READYRECV;
        }
        else{
            return null;                // invalid
        }
    }
    public ServerRequestType getType(){
        return this.type;
    }
    public ArrayList<String> getArgs(){
        return new ArrayList<String>(this.args);
    }

}
