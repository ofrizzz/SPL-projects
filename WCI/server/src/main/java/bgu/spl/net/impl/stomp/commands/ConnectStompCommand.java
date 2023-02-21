package bgu.spl.net.impl.stomp.commands;

import java.util.List;

import bgu.spl.net.impl.stomp.LiveFeed;
import bgu.spl.net.impl.stomp.LoginAttemptResponseType;
import bgu.spl.net.impl.stomp.StomProtocol;
import bgu.spl.net.impl.stomp.StompFrameType;

public class ConnectStompCommand implements StompCommand<LiveFeed> {
    final StompFrameType frameType = StompFrameType.CONNECT;

    final float version;
    final String host;
    final String username;
    final String passcode;
    final Integer receiptId;

    public ConnectStompCommand(float version, String host, String username, String passcode, Integer receiptId) {
        this.version = version;
        this.host = host;
        this.username = username;
        this.passcode = passcode;
        this.receiptId = receiptId;
    }

    @Override
    public String execute(StomProtocol stomProtocol) { 
        // for connection handler to know if connection should be closed
        LoginAttemptResponseType loginResponse = stomProtocol.getLiveFeed().login(username, passcode);

        switch (loginResponse) {
            case SUCCESS_LOGIN_USER_EXISTS:
                stomProtocol.SetUsername(username); // ? user already exists
                // Check user is not already connected? should be here or in login function?
                new ConnectedStompCommand(version,receiptId).execute(stomProtocol);
                break;
            case SUCCESS_LOGIN_USER_CREATED: // ? channel list should be empty
                stomProtocol.SetUsername(username);
                new ConnectedStompCommand(version,receiptId).execute(stomProtocol);
                break;
            case ERR_WRONG_PASSWORD:
                new ErrorStompCommand("Wrong password", receiptId, null).execute(stomProtocol);
                break;
            case ERR_USER_ALREADY_CONNECTED:
                new ErrorStompCommand("User already logged in", receiptId, null).execute(stomProtocol);
                break;
            default:
                break;
        }

        return null;
    }

    @Override
    public Integer getReceiptId() {
        return receiptId;
    }
}
