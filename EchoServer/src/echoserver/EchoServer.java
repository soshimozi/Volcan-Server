/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package echoserver;

import com.EchoServer.Configuration.ServerConfiguration;
import com.EchoServer.Configuration.ServerConfiguration.Server;
import com.EchoServer.Event.*;
import com.EchoServer.Network.ClientConnection;
import com.VolcanServer.Net.NetworkTransport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

/**
 *
 * @author Monkeybone
 */
public class EchoServer implements Runnable {

    private ServerConfiguration configuration;
    private final List<ServerBase> servers = new ArrayList<ServerBase>();
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Runnable runnable = new EchoServer();
        runnable.run();
    }

    @Override
    public void run() {
        
        configuration = loadServerConfiguration();

        for(Server serverConfiguration : configuration.getServers() ) {
            TransportListener listener = new TCPSocketListener(serverConfiguration.getSecurity().getUseSSL(), serverConfiguration.getSecurity().getKeyStoreFile(), 
                    serverConfiguration.getSecurity().getKeyStorePassword());

            SocketServer server = new SocketServer(listener, serverConfiguration.getPort());
            server.Start();
            servers.add(server);

            Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, "Server started and listening on port {0}", serverConfiguration.getPort());
        }
    }

    private ServerConfiguration loadServerConfiguration() {
        
        ServerConfiguration configuration = new ServerConfiguration();
        
        XMLConfiguration xmlConfig = null;
        try {
            xmlConfig = new XMLConfiguration("config.xml");
        } catch (ConfigurationException ex) {
            Logger.getLogger(EchoServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if( xmlConfig != null ) {
            
            Object prop = xmlConfig.getProperty("server");   
            
            List<HierarchicalConfiguration> servers = xmlConfig.configurationsAt("servers.server");
            for(HierarchicalConfiguration serverConfig : servers) {
                int port = serverConfig.getInt("listen-port");

                String keystoreFilename = serverConfig.getString("security.keystore-file");
                String keystorePassword = serverConfig.getString("security.keystore-password");
                Boolean useSSL = serverConfig.getBoolean("security.use-ssl");
                
                Server server = ServerConfiguration.CreateServer();
                
                configuration.addServer(server);
                server.setPort(port);
                server.setSecurity(Server.CreateSecurity());
                server.getSecurity().setKeyStoreFile(keystoreFilename);
                server.getSecurity().setKeyStorePassword(keystorePassword);
                server.getSecurity().setUseSSL(useSSL);
            }
        }
        
        return configuration;
    }
}
