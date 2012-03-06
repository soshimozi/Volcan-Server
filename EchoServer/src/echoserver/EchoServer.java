/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package echoserver;

import com.EchoServer.Configuration.ServerConfiguration;
import com.EchoServer.Network.ClientConnection;
import com.EchoServer.Event.MessageListener;
import com.EchoServer.Event.ClientDisconnectedListener;
import com.EchoServer.Event.MessageEvent;
import com.EchoServer.Event.ClientDisconnectedEvent;
import com.VolcanServer.Net.SocketTransport;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

/**
 *
 * @author Monkeybone
 */
public class EchoServer implements ClientDisconnectedListener, MessageListener, Runnable {

    private final List<ClientConnection> connections = new CopyOnWriteArrayList();
    
    private ServerConfiguration serverConfiguration;
    //public static final int HTTPS_PORT = 4000;
   
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
            
            serverConfiguration = loadServerConfiguration();
            ServerSocket server = getServerSocket();

            Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, "Server started and listening on port {0}", serverConfiguration.getPort());
            
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
    
    public ServerSocket getServerSocket() throws Exception {

        ServerSocketFactory factory;
        if( serverConfiguration.getSecurity().getUseSSL() ) {
            KeyManagerFactory kmf = getKeyStore();
            
            SSLContext sslcontext = 
                SSLContext.getInstance("SSLv3");

            sslcontext.init(kmf.getKeyManagers(), null, null);
            factory = sslcontext.getServerSocketFactory();
        } else {
            factory = ServerSocketFactory.getDefault();
        }
            
        return factory.createServerSocket(serverConfiguration.getPort());
    }

    private KeyManagerFactory getKeyStore() throws CertificateException, KeyStoreException, UnrecoverableKeyException, IOException, NoSuchAlgorithmException {
        
        String keystoreFile = serverConfiguration.getSecurity().getKeyStoreFile();
        
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(keystoreFile), serverConfiguration.getSecurity().getKeyStorePassword().toCharArray());
        KeyManagerFactory kmf = 
            KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, serverConfiguration.getSecurity().getKeyStorePassword().toCharArray());
        return kmf;
    }

    private ServerConfiguration loadServerConfiguration() {
        
        ServerConfiguration configuration = new ServerConfiguration();
        
        configuration.setSecurity(ServerConfiguration.CreateSecurity());
        
        XMLConfiguration xmlConfig = null;
        try {
            xmlConfig = new XMLConfiguration("config.xml");
        } catch (ConfigurationException ex) {
            Logger.getLogger(EchoServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if( xmlConfig != null ) {
            int port = xmlConfig.getInt("listen-port");
            
            String keystoreFilename = xmlConfig.getString("security.keystore-file");
            String keystorePassword = xmlConfig.getString("security.keystore-password");
            Boolean useSSL = xmlConfig.getBoolean("security.use-ssl");
            
            configuration.getSecurity().setKeyStoreFile(keystoreFilename);
            configuration.getSecurity().setKeyStorePassword(keystorePassword);
            configuration.getSecurity().setUseSSL(useSSL);
            configuration.setPort(port);
        }
        
        return configuration;
    }
    
}
