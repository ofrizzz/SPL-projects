package bgu.spl.net.impl.stomp.commands;

import bgu.spl.net.impl.stomp.LiveFeed;
import bgu.spl.net.impl.stomp.StomProtocol;
import bgu.spl.net.impl.stomp.StompFrameType;

public class ErrorStompCommand implements StompCommand<LiveFeed> {
    final private StompFrameType frameType = StompFrameType.ERROR;

    public final String message;
    final Integer receiptId;
    public final String body;

    public ErrorStompCommand(String message, Integer receiptId, String body) {
        this.message = message;
        this.receiptId = receiptId;
        this.body = body;
    }

    @Override
    public String execute(StomProtocol stomProtocol) {
        if(stomProtocol.getUser() != null)
        {
            stomProtocol.getUser().setLoggedOut();
        }
        
        stomProtocol.getConections().send(stomProtocol.getConnectionId(), stomProtocol.Parse(this));
        stomProtocol.getConections().disconnect(stomProtocol.getConnectionId());
        stomProtocol.terminate();
        return null;
    }

    @Override
    public Integer getReceiptId() {
        return receiptId;
    }
}
