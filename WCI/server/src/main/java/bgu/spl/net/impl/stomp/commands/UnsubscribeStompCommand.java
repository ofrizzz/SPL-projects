package bgu.spl.net.impl.stomp.commands;

import bgu.spl.net.impl.stomp.LiveFeed;
import bgu.spl.net.impl.stomp.StomProtocol;
import bgu.spl.net.impl.stomp.StompFrameType;

public class UnsubscribeStompCommand implements StompCommand<LiveFeed> {

    final StompFrameType frameType = StompFrameType.UNSUBSCRIBE;
    final int id;
    final Integer receiptId;

    public UnsubscribeStompCommand(int id, Integer receiptId) {
        this.id = id;
        this.receiptId = receiptId;
    }

    @Override
    public String execute(StomProtocol stomProtocol) {
        if(stomProtocol.getUsername()== null || !stomProtocol.getLiveFeed().isLoggedIn(stomProtocol.getUsername()))
        {
            new ErrorStompCommand("You are not logged in!", receiptId, null).execute(stomProtocol);
            return null;
        }
        String removedChannelName = stomProtocol.getUser().removeSubscription(id);
        boolean success = removedChannelName != null;
        success = success && stomProtocol.getConections().unsubscribe(stomProtocol.getConnectionId(), removedChannelName);

        if(success){
            new ReceiptStompCommand(receiptId).execute(stomProtocol);
        }
        else{
            new ErrorStompCommand("You are not subscribed to this channel!", receiptId, null).execute(stomProtocol);
        }
        return null;
    }

    @Override
    public Integer getReceiptId() {
        return receiptId;
    }

}
