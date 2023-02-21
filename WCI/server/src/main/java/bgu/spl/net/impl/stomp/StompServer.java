package bgu.spl.net.impl.stomp;
import bgu.spl.net.srv.Server;

public class StompServer {

    public static void main(String[] args) {
        if(args.length < 2){
            System.out.println("Invalid number of arguments! use: <port> <tpc/reactor>");
            return;
        }
        Integer port = Integer.parseInt(args[0]);
        String serverType = args[1];

        LiveFeed liveFeed = new LiveFeed();
        if (serverType.equals("tpc")) {
            Server.threadPerClient(
                    port, // port
                    () -> new StomProtocol(liveFeed), 
                    StompMessageEncoderDecoder::new 
            ).serve();
        } else if (serverType.equals("reactor")) {
            Server.reactor(
                    Runtime.getRuntime().availableProcessors(),
                    port, // port
                    () -> new StomProtocol<LiveFeed>(liveFeed),
                    StompMessageEncoderDecoder::new 
            ).serve();
        }
        else{
            System.out.println("Invalid server type!");
        }

    }
}
