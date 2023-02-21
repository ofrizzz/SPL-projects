package bgu.spl.net.impl.stomp.commands;

import java.util.concurrent.ConcurrentHashMap;
import bgu.spl.net.impl.stomp.LiveFeed;
import bgu.spl.net.impl.stomp.StomProtocol;
import bgu.spl.net.impl.stomp.StompFrameType;

public class SendStompCommand implements StompCommand<LiveFeed> {

    final StompFrameType frameType = StompFrameType.SEND;
    final String destination;
    final String body;
    final Integer receiptId;

    public SendStompCommand(String destination, String body, Integer receiptId) {
        this.destination = destination;
        this.body = body;
        this.receiptId = receiptId;
    }

    @Override
    public String execute(StomProtocol stomProtocol) {
        if(stomProtocol.getUsername()== null || !stomProtocol.getLiveFeed().isLoggedIn(stomProtocol.getUsername()))
        {
            new ErrorStompCommand("You are not logged in!", receiptId, null).execute(stomProtocol);
            return null;
        }

        if(!stomProtocol.getConections().isSubscribed(stomProtocol.getConnectionId(), destination))
        {
            new ErrorStompCommand("You are not subscribed to this channel!", receiptId, null).execute(stomProtocol);
        }
        else{
            //stomProtocol.getConections().send(destination, new MessageStompCommand(stomProtocol.getConnectionId(), MessageStompCommand.getNewMessageId(), destination, body).execute(stomProtocol));
            new ReceiptStompCommand(receiptId).execute(stomProtocol);

            int messageId = MessageStompCommand.getNewMessageId();
            ConcurrentHashMap<Integer,Integer> channelConnections =  stomProtocol.getConections().getChannelSubscriptions(destination);
            channelConnections.forEach((connectionId, subscriptionId) -> {
                String msg = StomProtocol.Parse(new MessageStompCommand(subscriptionId, messageId, destination, body));
                stomProtocol.getConections().send(connectionId, msg);
            });
        }
        
        return null;
    }

    @Override
    public Integer getReceiptId() {
        return receiptId;
    }
}