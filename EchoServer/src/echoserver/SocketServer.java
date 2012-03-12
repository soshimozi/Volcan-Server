/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package echoserver;

import com.EchoServer.Event.ClientDisconnectedEvent;
import com.EchoServer.Event.ClientDisconnectedListener;
import com.EchoServer.Event.MessageEvent;
import com.EchoServer.Event.MessageListener;
import com.EchoServer.Network.ClientConnection;
import com.VolcanServer.Net.NetworkTransport;
import com.VolcanServer.Net.TransportListener;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author MonkeyBreath
 */
public class SocketServer extends ServerBase implements ClientDisconnectedListener, MessageListener {

    private final List<ClientConnection> connections = new CopyOnWriteArrayList<ClientConnection>();

    private final Semaphore resetSemaphore = new Semaphore(0);
    private volatile boolean started = false;
    private final int listenPort;
    
    public SocketServer(TransportListener listener, int listenPort) {
        super(listener);
        this.listenPort = listenPort;
    }
    
    public boolean getIsRunning() {
        return started;
    }
    
    @Override
    public void Start() {
        
        Thread serverThread = new Thread(
                new Runnable()
                {
                    @Override
                    public void run() {
                        try {
                            // initalize the listener
                            serverListener.initialize(listenPort);

                            started = true;

                            // now enter accept loop

                            while(true) {
                                NetworkTransport transport;
                                transport = serverListener.accept();
                                
                                handleClientConnection(transport);
                            }

                        } catch (IOException ex) {
                            Logger.getLogger(SocketServer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                });
        
        serverThread.start();
                
    }

    @Override
    public void Stop() {
        serverListener.close();
    }
    
    private void handleClientConnection(NetworkTransport transport) {
        ClientConnection connection = new ClientConnection(transport);
        
        connections.add(connection);
        
        connection.addClientDisconnectedListener(this);
        connection.addMessageListener(this);

        Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, "Client connected from {0}", connection.getTransport().getSocketAddress());
    }

    @Override
    public void clientDisconnected(ClientDisconnectedEvent evt) {
        ClientConnection connection = (ClientConnection) evt.getSource();
        connection.close();
        connections.remove(connection);
        
        Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, "Client disconnected from {0}", connection.getTransport().getSocketAddress());
    }

    @Override
    public void onMessage(MessageEvent event) {
        for(ClientConnection c : connections) {
            String address = c.getTransport().getSocketAddress().toString();
            c.send(address + ": " + event.getMessage());
        }
    }
   
    
}
