package bgu.spl.net.impl.stomp;

import java.util.HashMap;

import bgu.spl.net.impl.stomp.commands.ConnectStompCommand;
import bgu.spl.net.impl.stomp.commands.ConnectedStompCommand;
import bgu.spl.net.impl.stomp.commands.DisconnectStompCommand;
import bgu.spl.net.impl.stomp.commands.ErrorStompCommand;
import bgu.spl.net.impl.stomp.commands.MessageStompCommand;
import bgu.spl.net.impl.stomp.commands.ReceiptStompCommand;
import bgu.spl.net.impl.stomp.commands.SendStompCommand;
import bgu.spl.net.impl.stomp.commands.StompCommand;
import bgu.spl.net.impl.stomp.commands.SubscribeStompCommand;
import bgu.spl.net.impl.stomp.commands.UnsubscribeStompCommand;
import bgu.spl.net.srv.Connections;

public class StomProtocol<T> implements StompMessagingProtocol<String> {
    LiveFeed liveFeed;

    Connections<String> conections;
    int connectionId;
    String username;

    private boolean shouldTerminate = false;

    public void SetUsername(String username) {
        this.username = username;
    }

    public LiveFeed getLiveFeed() {
        return liveFeed;
    }

    public Connections<String> getConections() {
        return conections;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public String getUsername() {
        return username;
    }

    public LiveFeedUser getUser() {
        if(username==null){
            return null;
        }
        
        return this.liveFeed.getUser(username);
    }

    public StomProtocol(LiveFeed liveFeed) {
        this.liveFeed = liveFeed;
    }

    @Override
    public String process(String msg) {
        StompCommand cmd = this.Parse(msg);
        cmd.execute(this);

        return null;
    }

    public static StompCommand Parse(String msg) {
        String command;
        HashMap<String, String> properties;
        Integer receipt_id;
        try {
            int newLineIndex = msg.indexOf('\n');
            command = msg.substring(0, newLineIndex);
            String header_body = msg.substring(newLineIndex + 1);

            properties = new HashMap<String, String>();
            while (header_body.charAt(0) != '\n') {
                int colonIndex = header_body.indexOf(':');
                newLineIndex = header_body.indexOf('\n');

                properties.put(header_body.substring(0, colonIndex),
                        header_body.substring(colonIndex + 1, newLineIndex));

                header_body = header_body.substring(newLineIndex + 1);
            }

            properties.put("body", header_body.substring(1));

            receipt_id = properties.get("receipt") != null ? Integer.parseInt(properties.get("receipt")) : null;

        } catch (Exception e) {
            return new ErrorStompCommand("Malformed frame received",null,null);
        }

        switch (command) {
            case "CONNECT":
                return new ConnectStompCommand(Float.parseFloat(properties.get("accept-version")),
                        properties.get("host"), properties.get("login"), properties.get("passcode"),
                        receipt_id);
            case "SUBSCRIBE":
                return new SubscribeStompCommand(properties.get("destination"),
                        Integer.parseInt(properties.get("id")),
                        receipt_id);
            case "SEND":
                return new SendStompCommand(properties.get("destination"), properties.get("body"),
                        receipt_id);
            case "UNSUBSCRIBE":
                return new UnsubscribeStompCommand(Integer.parseInt(properties.get("id")),
                        receipt_id);
            case "DISCONNECT":
                return new DisconnectStompCommand(receipt_id);
            default:
                return new ErrorStompCommand("Malformed frame received",null,null);
        }
    }

    public static String Parse(StompCommand msg) {
        String className = msg.getClass().getSimpleName();

        switch (className) {
            case "ConnectedStompCommand": {
                ConnectedStompCommand cmd = (ConnectedStompCommand) msg;
                return "CONNECTED" + "\nversion:" + cmd.version + cmd.getReceiptIdHeader() + "\n\n";
            }
            case "MessageStompCommand": {
                MessageStompCommand cmd = (MessageStompCommand) msg;
                String body = cmd.body != null ? cmd.body : "";

                return "MESSAGE" + "\nsubscription:" + cmd.subscriptionId + "\nmessage-id:" + cmd.messageId
                        + "\ndestination:" + cmd.destination + "\n\n" + body;
            }
            case "ReceiptStompCommand": {
                ReceiptStompCommand cmd = (ReceiptStompCommand) msg;
                return "RECEIPT" + cmd.getReceiptIdHeader() + "\n\n";
            }
            case "ErrorStompCommand": {
                ErrorStompCommand cmd = (ErrorStompCommand) msg;
                String body = cmd.body != null ? cmd.body : "";
                return "ERROR" + "\nmessage:" + cmd.message + cmd.getReceiptIdHeader() + "\n\n" + body;
            }
            default:
                return null;
        }
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    public boolean terminate() {
        return shouldTerminate = true;
    }

    @Override
    public void start(int connectionId, Connections<String> connections) {
        this.conections = connections;
        this.connectionId = connectionId;
    }
}
