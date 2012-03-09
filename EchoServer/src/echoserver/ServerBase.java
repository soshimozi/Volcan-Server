/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package echoserver;

import com.EchoServer.Event.ClientConnectedEvent;
import com.EchoServer.Event.ClientConnectedListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author MonkeyBreath
 */
public abstract class ServerBase {

    protected final TransportListener serverListener;

    public ServerBase(TransportListener listener) {
        serverListener = listener;
    }
//    public static ServerBase createSecureServer(int port, String keyStoreFile, String keyStorePassword) {
//        return new SecureServer(port, keyStoreFile, keyStorePassword);
//    }
//
//    static ServerBase createServer(int port) {
//       return new SocketServer(port);
//    }
    
//    private final List<ClientConnectedListener> connectedListeners = new CopyOnWriteArrayList<ClientConnectedListener>();
    
    public abstract void Start();
    public abstract void Stop();
    
//    public void addClientConnectedListener(ClientConnectedListener listener) {
//        connectedListeners.add(listener);
//    }
//    
//    public void removeClientConnectedListener(ClientConnectedListener listener) {
//        connectedListeners.remove(listener);
//    }
//    
//    protected void fireClientConnected(ClientConnectedEvent evt) {
//        ClientConnectedListener [] listeners = new ClientConnectedListener[connectedListeners.size()];
//        connectedListeners.toArray(listeners);
//        
//        for(ClientConnectedListener listener : listeners ) {
//            listener.ClientConnected(evt);
//        }
//    }
    
//    protected final void setTransportListener(TransportListener value) {
//        serverListener = value;
//    }
//    
//    protected final TransportListener getTransportListener() {
//        return serverListener;
//    }
}
