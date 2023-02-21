package bgu.spl.net.impl.stomp.commands;

import bgu.spl.net.impl.stomp.LiveFeed;
import bgu.spl.net.impl.stomp.StomProtocol;
import bgu.spl.net.impl.stomp.StompFrameType;

public class ConnectedStompCommand implements StompCommand<LiveFeed> {
    final StompFrameType frameType = StompFrameType.CONNECTED;

    public final float version;
    public final Integer receiptId;

    public ConnectedStompCommand(float version, Integer receiptId) {
        this.version = version;
        this.receiptId = receiptId;
    }

    @Override
    public String execute(StomProtocol stomProtocol) {
        stomProtocol.getConections().send(stomProtocol.getConnectionId(),stomProtocol.Parse(this));
        
        return null;
    }

    @Override
    public Integer getReceiptId() {
        return receiptId;
    }
}
