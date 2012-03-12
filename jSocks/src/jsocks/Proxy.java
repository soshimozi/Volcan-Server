/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsocks;

import com.VolcanServer.Net.NetworkTransport;
import com.VolcanServer.Net.SocketTransport;
import com.VolcanServer.SOCKS.Enum.AddressTypes;
import com.VolcanServer.SOCKS.Enum.Commands;
import com.VolcanServer.SOCKS.Enum.ConnectionMethods;
import com.VolcanServer.SOCKS.*;
import com.VolcanServer.SOCKS.Enum.Responses;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author MonkeyBreath
 */
public class Proxy implements Runnable {
    
    private NetworkTransport client = null;
    private final NetworkTransport proxyConnection;
    private final boolean allowNoAuth;
    private final String proxyUserName;
    private final String proxyPassword;
    private final short serverPort;
    
    public Proxy(
            NetworkTransport proxyConnection, 
            String proxyUserName, String proxyPassword, 
            boolean allowNoAuth, short serverPort) {
        this.proxyConnection = proxyConnection;
        this.allowNoAuth = allowNoAuth;
        
        this.proxyUserName = proxyUserName;
        this.proxyPassword = proxyPassword;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        try {
            
            handleHandshake();
            handleRequest();
            
            // update client count
        } catch (InvalidHeaderException ex) {
            Logger.getLogger(Proxy.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Proxy.class.getName()).log(Level.SEVERE, null, ex);
        } catch (HandshakeException ex) {
            Logger.getLogger(Proxy.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AuthorizationException ex) {
            Logger.getLogger(Proxy.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        
            try {
                proxyConnection.close(true);
            } catch (IOException ex) {
                Logger.getLogger(Proxy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void handleHandshake() throws IOException, HandshakeException, AuthorizationException {
        
        MethodIdentificationPacket packet = MethodIdentificationPacket.DeSerialize(proxyConnection.getInStream());
        
        if( packet == null ) {
            throw new HandshakeException("Incomplete packet read from client");
        }
        
        MethodSelectionPacket response = new MethodSelectionPacket((byte)ConnectionMethods.METHOD_NOTAVAILABLE.getValue());
        
        for(int i=0; i < packet.nmethods; i++) {
            if( allowNoAuth ) {
                if( packet.methods[i] == ConnectionMethods.METHOD_NOAUTH.getValue()) {
                    response.method = (byte) ConnectionMethods.METHOD_NOAUTH.getValue();
                }
            }
                
            if( packet.methods[i] == ConnectionMethods.METHOD_AUTH.getValue()) {
                response.method = (byte) ConnectionMethods.METHOD_AUTH.getValue();
            }
        }
        
        if( response.method == ConnectionMethods.METHOD_NOTAVAILABLE.getValue() ) {
            throw new HandshakeException("Invalid connection method");
        }
        
        MethodSelectionPacket.Serialize(proxyConnection.getOutStream(), response );
        
        if( response.method == ConnectionMethods.METHOD_AUTH.getValue()) {
            authorizeClient();
        }
    }

    private void authorizeClient() throws AuthorizationException, IOException {
        ClientAuthRequest authRequest = ClientAuthRequest.DeSerialize(proxyConnection.getInStream());
        
        if( !authRequest.username.equals(proxyUserName) || !authRequest.password.equals(proxyPassword)) {
            throw new AuthorizationException();
        }
        
        ClientAuthResponse authResponse = new ClientAuthResponse();
        authResponse.version = 1;
        authResponse.response = (byte) Responses.RESP_SUCCEDED.getValue();
        ClientAuthResponse.Serialize(proxyConnection.getOutStream(), authResponse);
    }

    private void handleRequest() throws IOException, InvalidHeaderException {
        SOCKS5RequestHeader header = SOCKS5RequestHeader.DeSerialize(proxyConnection.getInStream());
        
        if( header == null ) {
            throw new InvalidHeaderException();
        }
        
        if(header.version != 5 || header.cmd != Commands.CMD_CONNECT.getValue() || header.rsv != 0) {
            throw new InvalidHeaderException();
        }
        
        if( header.atyp == AddressTypes.ATYP_IPV4.getValue() ) {
            
            SOCK5IP4RequestBody req = SOCK5IP4RequestBody.DeSerialize(proxyConnection.getInStream());
            if( req == null ) {
                throw new InvalidHeaderException();
            }

            client = SocketTransport.CreateSocketTransport(NetworkUtils.intToIp(req.ip_dst), NetworkUtils.NetworkToHostOrder(req.port));
            
            SOCKS5Response response = new SOCKS5Response();
            response.ip_src = 0;
            response.port_src = serverPort;
            
            SOCKS5Response.Serialize(proxyConnection.getOutStream(), response);
            doProxy();
            
            client.close(true);
            
        } else {
            throw new InvalidHeaderException();
        }        
    }
    
    private void doProxy() throws IOException {
        
        // two threads
        // each thread reads from one socket and echos to the other
        
        Selector selector = Selector.open();
        
        // make sure to make the socket nonblocking so 
        // we can use a selector on it
        SocketChannel scProxy = proxyConnection.getSocketChannel();
        scProxy.configureBlocking(false);
        SelectionKey proxyKey = scProxy.register(selector, SelectionKey.OP_READ);
        
        SocketChannel scClient = client.getSocketChannel();
        scClient.configureBlocking(false);
        SelectionKey clientKey = scClient.register(selector, SelectionKey.OP_READ);
        
        while(true) {
            int selectionCount = selector.select();
            
            // no activity, continue
            if( selectionCount == 0 ) continue;
            
            Set<SelectionKey> keys = selector.selectedKeys();
            for(SelectionKey k : keys) {
                if( k.readyOps() == SelectionKey.OP_READ ) {
                    if(k == proxyKey) {
                        // read from proxy and send to client
                        int count = proxyConnection.getInStream().available();
                        byte[] buffer = new byte[count];
                        int bytesRead = proxyConnection.getInStream().read(buffer, 0, count);
                        
                        if( bytesRead > 0 ) {
                            client.getOutStream().write(buffer, 0, bytesRead );
                        }

                    } else if( k == clientKey ) {
                        // read from client and send to proxy
                        int count = client.getInStream().available();
                        byte[] buffer = new byte[count];
                        int bytesRead = client.getInStream().read(buffer, 0, count);
                        
                        if( bytesRead > 0 ) {
                            proxyConnection.getOutStream().write(buffer, 0, bytesRead);
                        }
                    }
                }
            }
            
            // remove selected keys because we've dealt with them
            keys.clear();
        }
    }
}
