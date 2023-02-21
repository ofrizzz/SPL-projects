package bgu.spl.net.impl.stomp.commands;

import bgu.spl.net.impl.stomp.LiveFeed;
import bgu.spl.net.impl.stomp.StomProtocol;
import bgu.spl.net.impl.stomp.StompFrameType;

public class DisconnectStompCommand implements StompCommand<LiveFeed> {

    final StompFrameType frameType = StompFrameType.DISCONNECT;
    final Integer receiptId;

    public DisconnectStompCommand(Integer receiptId) {
        this.receiptId = receiptId;
    }

    @Override
    public String execute(StomProtocol stomProtocol) {
        if (stomProtocol.getUsername() == null || !stomProtocol.getLiveFeed().isLoggedIn(stomProtocol.getUsername())) {
            new ErrorStompCommand("You are not logged in!", receiptId, null).execute(stomProtocol);
            return null;
        }

        if (receiptId == null) {
            new ErrorStompCommand("malformed frame received", receiptId, null).execute(stomProtocol);
            return null;
        }
        if(stomProtocol.getUser() != null)
        {
            stomProtocol.getUser().setLoggedOut();
        }

        stomProtocol.getConections().send(stomProtocol.getConnectionId(),
                stomProtocol.Parse(new ReceiptStompCommand(receiptId)));
        stomProtocol.getConections().disconnect(stomProtocol.getConnectionId());
        stomProtocol.terminate();
        return null;
    }

    @Override
    public Integer getReceiptId() {
        return receiptId;
    }
}