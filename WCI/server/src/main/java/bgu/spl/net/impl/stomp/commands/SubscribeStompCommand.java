package bgu.spl.net.impl.stomp.commands;

import bgu.spl.net.impl.stomp.LiveFeed;
import bgu.spl.net.impl.stomp.StomProtocol;
import bgu.spl.net.impl.stomp.StompFrameType;

public class SubscribeStompCommand implements StompCommand<LiveFeed> {

    final StompFrameType frameType = StompFrameType.SUBSCRIBE;
    final String destination;
    final int id;
    final Integer receiptId;

    public SubscribeStompCommand(String destination, int id, Integer receiptId) {
        this.destination = destination;
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
        boolean success = stomProtocol.getConections().subscribe(stomProtocol.getConnectionId(), destination, id);

        if(!success)
        {
            new ErrorStompCommand("You are already subscribed to this channel!", receiptId, null).execute(stomProtocol);
        }
        else{
            stomProtocol.getUser().addSubscription(id, destination);
            new ReceiptStompCommand(receiptId).execute(stomProtocol);
        }

        return null;
    }

    @Override
    public Integer getReceiptId() {
        return receiptId;
    }
}
