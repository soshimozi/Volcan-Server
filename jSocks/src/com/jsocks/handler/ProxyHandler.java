/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jsocks.handler;

import com.VolcanServer.Net.NetworkTransport;
import com.VolcanServer.Net.SocketTransport;
import com.VolcanServer.SOCKS.Enum.AddressTypes;
import com.VolcanServer.SOCKS.Enum.Commands;
import com.VolcanServer.SOCKS.Enum.ConnectionMethods;
import com.VolcanServer.SOCKS.Enum.Responses;
import com.VolcanServer.SOCKS.*;
import com.jsocks.exception.AuthorizationException;
import com.jsocks.exception.HandshakeException;
import com.jsocks.exception.InvalidHeaderException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author MonkeyBreath
 */
public class ProxyHandler implements Runnable {
    
    private NetworkTransport client = null;
    private final NetworkTransport proxyConnection;
    private final boolean allowNoAuth;
    private final String proxyUserName;
    private final String proxyPassword;
    private final short serverPort;
    
    public ProxyHandler(
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
            Logger.getLogger(ProxyHandler.class.getName()).log(Level.SEVERE, null, ex);
            try {
                proxyConnection.close(true);
            } catch (IOException ex1) {
                Logger.getLogger(ProxyHandler.class.getName()).log(Level.SEVERE, null, ex1);
            }
            
        } catch (IOException ex) {
            Logger.getLogger(ProxyHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (HandshakeException ex) {
            Logger.getLogger(ProxyHandler.class.getName()).log(Level.SEVERE, null, ex);
            try {
                proxyConnection.close(true);
            } catch (IOException ex1) {
                Logger.getLogger(ProxyHandler.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (AuthorizationException ex) {
            Logger.getLogger(ProxyHandler.class.getName()).log(Level.SEVERE, null, ex);
            try {
                proxyConnection.close(true);
            } catch (IOException ex1) {
                Logger.getLogger(ProxyHandler.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }

    }
    
    private void handleHandshake() throws IOException, HandshakeException, AuthorizationException {
        
        MethodIdentificationPacket packet = MethodIdentificationPacket.CreateMethodIdentificationPacket(proxyConnection.getInStream());
        
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
        
        //if( response.method == ConnectionMethods.METHOD_NOTAVAILABLE.getValue() ) {
        //    throw new HandshakeException("Invalid connection method");
        //}
        
        response.Serialize(proxyConnection.getOutStream());
        
        if( response.method == ConnectionMethods.METHOD_AUTH.getValue()) {
            authorizeClient();
        }
    }

    private void authorizeClient() throws AuthorizationException, IOException {
        ClientAuthRequest authRequest = ClientAuthRequest.CreateClientAuthRequest(proxyConnection.getInStream());
        
        ClientAuthResponse authResponse = new ClientAuthResponse();
        if( !authRequest.username.equals(proxyUserName) || !authRequest.password.equals(proxyPassword)) {
            authResponse.version = 1;
            authResponse.response = (byte) Responses.RESP_GEN_ERROR.getValue();
        } else {
            authResponse.version = 1;
            authResponse.response = (byte) Responses.RESP_SUCCEDED.getValue();
        }
        
        authResponse.Serialize(proxyConnection.getOutStream());
    }

    private void handleRequest() throws IOException, InvalidHeaderException {
        SOCKS5RequestHeader header = SOCKS5RequestHeader.CreateSOCKS5RequestHeader(proxyConnection.getInStream());
        
        if( header == null ) {
            throw new InvalidHeaderException();
        }
        
        if(header.version != 5 || header.cmd != Commands.CMD_CONNECT.getValue() || header.rsv != 0) {
            SOCKS5Response response = new SOCKS5Response(false);
            response.Serialize(proxyConnection.getOutStream());

            throw new InvalidHeaderException();
        }
        
        if( header.atyp == AddressTypes.ATYP_IPV4.getValue() ) {
            
            SOCK5IP4Body req = SOCK5IP4Body.CreateSOCK5IP4Body(proxyConnection.getInStream());
            if( req == null ) {
                throw new InvalidHeaderException();
            }

            client = SocketTransport.CreateSocketTransport(NetworkUtils.intToIp(req.ip), NetworkUtils.NetworkToHostOrder(req.port));
            
            SOCKS5Response response = new SOCKS5Response();
            response.Serialize(proxyConnection.getOutStream());
            
            SOCK5IP4Body ip4Header = new SOCK5IP4Body();
            ip4Header.ip = 0;
            ip4Header.port = serverPort;
            ip4Header.Serialize(proxyConnection.getOutStream());
            doProxy();
            
        } else {
            throw new InvalidHeaderException();
        }        
    }
    
    private void doProxy() throws IOException {
        
        // two threads
        // each thread reads from one socket and echos to the other
        Thread echoConnectionToClient = new Thread(new EchoHandler(proxyConnection, client));
        Thread echoClientToConnection = new Thread(new EchoHandler(client, proxyConnection));
        
        echoConnectionToClient.start();
        echoClientToConnection.start();
    }
}
