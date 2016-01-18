/**
 * Created by liuxt on 16/1/17.
 */
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.Pack200;

/**
 * Created by liuxt on 16/1/16.
 */
public class ClientRequest {
    private String message;
    private List<String> args;
    private ClientRequestType type;
    public enum ClientRequestType{
        Disconnect,
        PrivateMsg,
        ChatMsg,
        PrivateFile,
        ChatFile,
        ReadyRecv
    }
    public ClientRequest(String msg){
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
            tokens.add(tokenizer.nextToken());              // 2 = to whom
            String text = "";
            while(tokenizer.hasMoreTokens()){             // 3 = text
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

        else if(tokens.get(0).equals("Disconnect")){
            tokens.add(tokenizer.nextToken());            // 1 = who is to be disconnected
        }

        else if (tokens.get(0).equals("PrivateFile")){
            tokens.add(tokenizer.nextToken());            // 1 = from whom
            tokens.add(tokenizer.nextToken());            // 2 = to whom
            tokens.add(tokenizer.nextToken());            // 3 = what is the filename
            tokens.add(tokenizer.nextToken());            // 4 = how many bytes it has
        }

        else if(tokens.get(0).equals("ChatFile")){
            tokens.add(tokenizer.nextToken());            // 1 = from whom
            tokens.add(tokenizer.nextToken());            // 2 = what is the filename
            tokens.add(tokenizer.nextToken());            // 3 = how many bytes it has
        }

        else if(tokens.get(0).equals("ReadyReceive")){
            tokens.add(tokenizer.nextToken());            // 1 = from whom
            tokens.add(tokenizer.nextToken());            // 2 = what file client waits for
        }

        else{
            return null;                                  // invalid message
        }

        return tokens;
    }
    private ClientRequestType setType(){
        if(this.args.get(0).equals("PrivateMessage")){
            return ClientRequestType.PrivateMsg;
        }
        else if(this.args.get(0).equals("ChatMessage")){
            return ClientRequestType.ChatMsg;
        }
        else if(this.args.get(0).equals("Disconnect")){
            return ClientRequestType.Disconnect;
        }
        else if(this.args.get(0).equals("PrivateFile")){
            return ClientRequestType.PrivateFile;
        }
        else if(this.args.get(0).equals(("ChatFile"))){
            return ClientRequestType.ChatFile;
        }
        else if(this.args.get(0).equals(("ReadyReceive"))){
            return ClientRequestType.ReadyRecv;
        }
        else{
            return null;                // invalid
        }
    }
    public ClientRequestType getType(){
        return this.type;
    }
    public ArrayList<String> getArgs(){
        return new ArrayList<String>(this.args);
    }

}

