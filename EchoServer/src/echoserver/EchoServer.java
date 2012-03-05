/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package echoserver;

import com.VolcanServer.Net.SocketTransport;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;

/**
 *
 * @author Monkeybone
 */
public class EchoServer implements ClientDisconnectedListener, MessageListener, Runnable {

    private final List<ClientConnection> connections = new CopyOnWriteArrayList();
    
    String keystore;
    char keystorepass[] = "2et?S-#57JUXu!eThe6".toCharArray();
    char keypassword[] = "2et?S-#57JUXu!eThe6".toCharArray();
    public static final int HTTPS_PORT = 4000;
   
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Runnable runnable = new EchoServer();
        runnable.run();
    }

    @Override
    public void clientDisconnected(ClientDisconnectedEvent evt) {
        ClientConnection connection = (ClientConnection) evt.getSource();
        connection.close();
        connections.remove(connection);
        
        Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, "Client disconnected from {0}", connection.getTransport().getSocketAddress());
    }

    @Override
    public void run() {
        try {
            
            String filename = "volcansoft.com.key";
            File file = new File(filename);
            if (file.isFile() == false) {
                char SEP = File.separatorChar;
                
                String javaHome = System.getProperty("java.home");
                
                File dir = new File(javaHome + SEP
                        + "lib" + SEP + "security");
                file = new File(dir, filename);
            }
            
            keystore = file.getAbsolutePath();
            
            ServerSocket server = getServer();
            Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, "Server started and listening on port {0}", HTTPS_PORT);
            
            while(true) {
                try {
                    Socket clientSocket = server.accept();
                    
                    SocketTransport transport = new SocketTransport(clientSocket);
                    ClientConnection newConnection = new ClientConnection(transport);
                    
                    newConnection.addClientDisconnectedListener(this);
                    newConnection.addMessageListener(this);
                    
                    connections.add(newConnection);
                    
                    Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, "Incomming connection excepted from {0}", transport.getSocketAddress());
                    
                } catch (IOException ex) {
                    Logger.getLogger(EchoServer.class.getName()).log(Level.SEVERE, null, ex);
                    
                    return;
                }
            }
         
        } catch (IOException ex) {
            Logger.getLogger(EchoServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(EchoServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void onMessage(MessageEvent event) {
        // echo to all clients
        
        for(ClientConnection c : connections) {
            String address = c.getTransport().getSocketAddress().toString();
            c.send(address + ": " + event.getMessage());
        }
        
    }
    
    public ServerSocket getServer() throws Exception {

        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(keystore), keystorepass);
        
        KeyManagerFactory kmf = 
            KeyManagerFactory.getInstance("SunX509");
        
        kmf.init(ks, keystorepass);
        SSLContext sslcontext = 
            SSLContext.getInstance("SSLv3");
        
        sslcontext.init(kmf.getKeyManagers(), null, null);
        ServerSocketFactory ssf = 
            sslcontext.getServerSocketFactory();
        
        SSLServerSocket serversocket = (SSLServerSocket) 
            ssf.createServerSocket(HTTPS_PORT);
        return serversocket;

    }
    
}
