package bgu.spl.net.impl.stomp.commands;
import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.net.impl.stomp.LiveFeed;
import bgu.spl.net.impl.stomp.StomProtocol;
import bgu.spl.net.impl.stomp.StompFrameType;

public class MessageStompCommand implements StompCommand<LiveFeed>{
    final  StompFrameType frameType = StompFrameType.MESSAGE;
    public final int subscriptionId;
    public final int messageId;
    public final String destination;
    public final String body;
    static AtomicInteger messageIdCounter = new AtomicInteger(0);

    public MessageStompCommand(int subscriptionId, int messageId, String destination, String body) {
        this.subscriptionId = subscriptionId;
        this.messageId = messageId;
        this.destination = destination;
        this.body = body;
    }

    @Override
    public String execute(StomProtocol stomProtocol) {
        return stomProtocol.Parse(this);
    }

    @Override
    public Integer getReceiptId() {
        return null;
    }

    public static int getNewMessageId() {
        return messageIdCounter.incrementAndGet();
    }
}