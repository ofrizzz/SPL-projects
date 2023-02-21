package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.impl.stomp.StompMessagingProtocol;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final MessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
    
    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, MessagingProtocol<T> protocol) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
    }
        
    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, MessagingProtocol<T> protocol,Connections<T> connections) {
        this(sock, reader, protocol);

        int connectionId = connections.connect(this);
        ((StompMessagingProtocol) protocol).start(connectionId, connections);
    }

    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;

            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());
            
            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                T nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    T response = protocol.process(nextMessage);
                    if (response != null) {
                        //Write lock!
                        synchronized(out){
                            out.write(encdec.encode(response));
                            out.flush();
                        }
                    }
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
        System.out.println("Connection closed");
    }

    @Override
    public void send(T msg) {
        if(this.sock.isClosed())
            return;

            
        synchronized(out){
            try{
                out.write(encdec.encode(msg));
                out.flush();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
            //WRITE LOCK!
        // try{
        //     out.write(encdec.encode(msg));
        //     out.flush();
        // }
        // catch (IOException ex) {
        //     ex.printStackTrace();
        // }
    }
}
