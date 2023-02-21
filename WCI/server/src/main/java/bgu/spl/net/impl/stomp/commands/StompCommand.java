package bgu.spl.net.impl.stomp.commands;
import bgu.spl.net.impl.stomp.StomProtocol;

public interface StompCommand<T> 
{
    String execute(StomProtocol stomProtocol);

    Integer getReceiptId();
    
    default String getReceiptIdHeader(){
        if(getReceiptId() != null)
        {
            return "\nreceipt-id:" + getReceiptId();
        }
        return "";
    };
}