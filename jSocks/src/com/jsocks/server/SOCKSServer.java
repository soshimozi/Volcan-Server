/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jsocks.server;

import com.jsocks.handler.ProxyHandler;
import com.VolcanServer.Net.NetworkTransport;
import com.VolcanServer.Net.TCPSocketListener;
import com.VolcanServer.Net.TransportListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jsocks.JSocks;

/**
 *
 * @author MonkeyBreath
 */
public class SOCKSServer implements Runnable {
    
    private final short serverPort;
    private final String keyFile;
    private final String keyfilePassword;
    private final String proxyUser;
    private final String proxyPassword;
    private final boolean useSSL;

    private TransportListener serverListener;
    private int clientCount = 0;
    
    public SOCKSServer(boolean useSSL, String keyfile, String keyfilePassword, String proxyUser, String proxyPassword, short serverPort) {
        this.useSSL = useSSL;
        this.keyFile = keyfile;
        this.keyfilePassword = keyfilePassword;
        this.proxyUser = proxyUser;
        this.proxyPassword = proxyPassword;
        this.serverPort = serverPort;
    }
    
    @Override
    public void run() {
        try {
            serverListener = new TCPSocketListener(useSSL, keyFile, keyfilePassword);
            serverListener.initialize(serverPort);

            Logger.getLogger(JSocks.class.getName()).log(Level.INFO, "VolcanSOCKS server started on port {0}", serverPort);

            // now enter accept loop
            while(true) {
                NetworkTransport transport = serverListener.accept();
                handleClientConnection(transport, serverPort);
            }
        } catch (IOException ex) {
            Logger.getLogger(JSocks.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void handleClientConnection(NetworkTransport transport, short serverPort) {
        ProxyHandler proxy = new ProxyHandler(transport, proxyUser, proxyPassword, true, serverPort);
        
        Thread proxyThread = new Thread(proxy);
        proxyThread.start();
        
        //clientCount++;
    }    
    
}
