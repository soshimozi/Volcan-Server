/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.VolcanServer.Net;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

/**
 *
 * @author MonkeyBreath
 */
public class TCPSocketListener implements TransportListener {

    private final boolean secure;
    private final String keystoreFile;
    private final String keystorePassword;
    private ServerSocket socket = null;
    private ServerSocketFactory factory = null;
    
    public TCPSocketListener(boolean secure, String keystoreFile, String keystorePassword) {
        this.secure = secure;
        this.keystoreFile = keystoreFile;
        this.keystorePassword = keystorePassword;
    }
    
    public TCPSocketListener() {
        this.secure = false;
        this.keystoreFile = "";
        this.keystorePassword = "";
    }
    
    @Override
    public void initialize(int listenPort) throws IOException {
        
        try {
            initalizeFactory();
        } catch (CertificateException ex) {
            Logger.getLogger(TCPSocketListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyStoreException ex) {
            Logger.getLogger(TCPSocketListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TCPSocketListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(TCPSocketListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyManagementException ex) {
            Logger.getLogger(TCPSocketListener.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnrecoverableKeyException ex) {
            Logger.getLogger(TCPSocketListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if( factory != null ) {
            socket = factory.createServerSocket(listenPort);
        }

        if( socket != null ) {
            ServerSocketChannel channel = socket.getChannel();
            channel.configureBlocking(false);
        }

    }

    @Override
    public NetworkTransport accept() throws IOException {
        return new SocketTransport(socket.accept());
    }
    
    @Override
    public void close() {
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(TCPSocketListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initalizeFactory() throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException {
        if( secure ) {
            KeyManagerFactory kmf = getKeyStore();
            
            SSLContext sslcontext = 
                SSLContext.getInstance("SSLv3");

            sslcontext.init(kmf.getKeyManagers(), null, null);
            factory = sslcontext.getServerSocketFactory();
        } else {
            factory = ServerSocketFactory.getDefault();
        }
    }

    private KeyManagerFactory getKeyStore() throws CertificateException, KeyStoreException, UnrecoverableKeyException, IOException, NoSuchAlgorithmException {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(keystoreFile), keystorePassword.toCharArray());
        KeyManagerFactory kmf = 
            KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, keystorePassword.toCharArray());
        return kmf;
    }
    
}
