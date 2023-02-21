package bgu.spl.net.impl.stomp.commands;
import bgu.spl.net.impl.stomp.LiveFeed;
import bgu.spl.net.impl.stomp.StomProtocol;
import bgu.spl.net.impl.stomp.StompFrameType;

public class ReceiptStompCommand implements StompCommand<LiveFeed>{
    public final StompFrameType frameType = StompFrameType.RECEIPT;
    public final Integer receiptId;

    public ReceiptStompCommand(Integer receiptId) {
        this.receiptId = receiptId;
    }
    
    @Override
    public String execute(StomProtocol stomProtocol) {
        if(receiptId != null){
            stomProtocol.getConections().send(stomProtocol.getConnectionId(), stomProtocol.Parse(this));
        }
        
        return null;
    }

    @Override
    public Integer getReceiptId() {
        return receiptId;
    }
}