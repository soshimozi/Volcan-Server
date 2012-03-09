/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.TCPChat.Engine;

import com.TCPChat.Controller.TCPChatController;
import com.TCPChat.Event.*;
import com.TCPChat.Listener.*;
import com.TCPChat.Message.Message;
import com.TCPChat.Security.SecureKeyManager;
import com.VolcanServer.Net.X509CacheingTrustManager;
import java.io.*;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.SocketFactory;
import javax.net.ssl.*;

/**
 *
 * @author MonkeyBreath
 */
public class ClientEngine {
    
    private final List<MessageListener> messageListeners = new CopyOnWriteArrayList<MessageListener>();
    private final List<ErrorListener> errorListeners = new CopyOnWriteArrayList<ErrorListener>();
    private final List<DisconnectedListener> disconnectedListeners = new CopyOnWriteArrayList<DisconnectedListener>();
    private final List<HandshakeCompletedListener> handshakeCompletedListeners = new CopyOnWriteArrayList<HandshakeCompletedListener>();
    private final List<HandshakeBeganListener> handshakeBeganListeners = new CopyOnWriteArrayList<HandshakeBeganListener>();
    private final List<InstallCertificateListener> installCertificateListeners = new CopyOnWriteArrayList<InstallCertificateListener>();
    private final BlockingQueue<Message> outboundQueue = new LinkedBlockingQueue<Message>();
    private final SecureKeyManager keyManager;
    
    private Socket socket = null;
    
    public ClientEngine(SecureKeyManager keyManager) {
        this.keyManager = keyManager;
    }

    public boolean connectSecure(String host, int port) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, KeyManagementException, CertificateEncodingException, IOException {
        if( socket == null ) {
            
            fireHandshakeBegan(new HandshakeBeganEvent(this));
            
            socket = getSecureSocket(host, port);

            if( socket != null ) {
                startClientEngine();
                return true;
            }
        }
        
        return false;
    }
    
    public boolean connect(String host, int port) throws IOException {
        if( socket == null ) {
            socket = getSocket(host, port);
            
            if( socket != null ) {
                startClientEngine();
                return true;
            }
        }
        
        return false;
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            // trigger send thread to quit
            outboundQueue.put(new Message(null));
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
        socket = null;
    }
    
    public void send(Message message) {
        
        try {
            outboundQueue.put(message);
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addHandshakeBeganListener(HandshakeBeganListener listener) {
        handshakeBeganListeners.add(listener);
    }
    
    public void removeHandshakeBeganListener(HandshakeBeganListener listener) {
        handshakeBeganListeners.remove(listener);
    }

    public void addHandshakeCompletedListener(HandshakeCompletedListener listener) {
        handshakeCompletedListeners.add(listener);
        if( socket != null && socket instanceof SSLSocket) {
            SSLSocket secureSocket = (SSLSocket) socket;
            secureSocket.addHandshakeCompletedListener(listener);
        }
    }
    
    public void removeHandshakeCompletedListener(HandshakeCompletedListener listener) {
        handshakeCompletedListeners.remove(listener);
        if( socket != null && socket instanceof SSLSocket) {
            SSLSocket secureSocket = (SSLSocket) socket;
            secureSocket.removeHandshakeCompletedListener(listener);
        }
    }
    
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }
    
    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }
    
    public void addErrorListener(ErrorListener listener) {
        errorListeners.add(listener);
    }
    
    public void removeErrorListener(ErrorListener listener) {
        errorListeners.remove(listener);
    }

    public void addDisconnectedListener(DisconnectedListener listener) {
        disconnectedListeners.add(listener);
    }
    
    public void removeDisconnectedListener(DisconnectedListener listener) {
        disconnectedListeners.remove(listener);
    }

    public void addInstallCertificateListener(InstallCertificateListener listener) {
        installCertificateListeners.add(listener);
    }
    
    public void removeInstallCertificateListener(InstallCertificateListener listener) {
        installCertificateListeners.remove(listener);
    }

    protected void fireInstallCertificate(InstallCertificateEvent event) {
        InstallCertificateListener [] listeners = new InstallCertificateListener[installCertificateListeners.size()];
        installCertificateListeners.toArray(listeners);
        
        for(InstallCertificateListener listener : listeners) {
            listener.InstallCertificate(event);
        }
        
    }
    
    protected void fireMessageReceived(MessageEvent event) {
        MessageListener [] listeners = new MessageListener[messageListeners.size()];
        messageListeners.toArray(listeners);
        
        for(MessageListener listener : listeners) {
            listener.MessageReceived(event);
        }        
    } 
    
    protected void fireError(ErrorEvent event) {
        ErrorListener [] listeners = new ErrorListener[errorListeners.size()];
        errorListeners.toArray(listeners);
        
        for(ErrorListener listener : listeners) {
            listener.OnError(event);
        }        
    } 

    protected void fireDisconnected(DisconnectedEvent event) {
        DisconnectedListener [] listeners = new DisconnectedListener[disconnectedListeners.size()];
        disconnectedListeners.toArray(listeners);
        
        for(DisconnectedListener listener : listeners) {
            listener.Disconnected(event);
        }        
    } 

    private SSLSocket getSecureSocket(X509TrustManager trust, String host, int port) throws KeyManagementException, IOException, NoSuchAlgorithmException {
        SSLContext context = SSLContext.getInstance("TLS");       
        context.init(null, new TrustManager[]{trust}, null);
        SSLSocketFactory factory = context.getSocketFactory();
        
        return (SSLSocket) factory.createSocket(host, port);
    }
    
    private Socket getSocket(String host, int port) throws IOException  {
        SocketFactory factory = SocketFactory.getDefault();
        return factory.createSocket(host, port);
    }
            
    private Socket getSecureSocket(String host, int port) throws KeyStoreException, KeyManagementException, CertificateEncodingException, CertificateException, IOException, NoSuchAlgorithmException {

        X509CacheingTrustManager socketTrust = new X509CacheingTrustManager(keyManager.CreateTrustManager());
        SSLSocket client = getSecureSocket(socketTrust, host, port);

        connectHandshakeListeners(client);
        
        int soTimeout = client.getSoTimeout();
        client.setSoTimeout(10000);
        try {
            client.startHandshake();

            client.setSoTimeout(soTimeout);
            return client;
        } catch (SSLException ex) {
            fireError(new ErrorEvent(this, ex));
        }

        if( socketTrust.getChain() != null ) {
            
            Integer count = 0;
            for(X509Certificate cert : socketTrust.getChain()) {
                
                fireInstallCertificate(new InstallCertificateEvent(this, keyManager, cert));
                
                count++;
            }

            // get socket and reload keystore
            client = getSecureSocket(
                    new X509CacheingTrustManager(keyManager.CreateTrustManager()), 
                    host, port);

            soTimeout = client.getSoTimeout();
            client.setSoTimeout(10000);
            try {
                client.startHandshake();

                client.setSoTimeout(soTimeout);
                return client;
            } catch (SSLException ex) {
                client.close();
                fireError(new ErrorEvent(this, ex));
            }
        }
        
        // something happened while negotiating or saving the cert, can't go on
        return null;
    }

    private void pumpMessages() throws IOException {
        Thread messagePump = new Thread(
                new Runnable()
                {
                    @Override
                    public void run() {
                        InputStream inputStream = null;
                        
                        try {
                            inputStream = socket.getInputStream();
                            InputStreamReader reader = new InputStreamReader(inputStream);
                            BufferedReader bufferedReader = new BufferedReader(reader);
                            String line;
                            
                            try {
                                while((line = bufferedReader.readLine()) != null) {
                                    fireMessageReceived(new MessageEvent(this, new Message(line)));
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(ClientEngine.class.getName()).log(Level.SEVERE, null, ex);
                                fireError(new ErrorEvent(this, ex));
                            }
                            
                            try {
                                inputStream.close();
                            } catch (IOException ex) {
                                Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            
                        } catch (IOException ex) {
                            Logger.getLogger(ClientEngine.class.getName()).log(Level.SEVERE, null, ex);
                            fireError(new ErrorEvent(this, ex));
                        } 

                        fireDisconnected(new DisconnectedEvent(this));
                        socket = null;
                    }
                });
        
        messagePump.start();
        
    }
    
    private void startSendThread() {
   
        Thread sendThread 
            = new Thread( new Runnable() {

                    @Override
                    public void run() {
                        
                        BufferedWriter bufferedwriter = null ;
                        try {
                            bufferedwriter 
                                    = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        } catch (IOException ex) {
                            Logger.getLogger(ClientEngine.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        try {
                            while(socket != null ) {
                                Message message = outboundQueue.take();
                                
                                if( message.getText() == null ) {
                                    break;
                                }
                                
                                try {
                                    bufferedwriter.write(message.getText() + "\n");
                                    bufferedwriter.flush();
                                } catch (IOException ex) {
                                    Logger.getLogger(ClientEngine.class.getName()).log(Level.SEVERE, null, ex);
                                    fireError(new ErrorEvent(this, ex));
                                }
                            }
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ClientEngine.class.getName()).log(Level.SEVERE, null, ex);
                            Thread.currentThread().interrupt();
                        }
                    }
                });

        sendThread.start();
        
    }

    private void startClientEngine() throws IOException {
        outboundQueue.clear();
        pumpMessages();
        startSendThread();
    }

    private void fireHandshakeBegan(HandshakeBeganEvent handshakeBeganEvent) {
        HandshakeBeganListener [] listeners = new HandshakeBeganListener[handshakeBeganListeners.size()];
        handshakeBeganListeners.toArray(listeners);
        
        for(HandshakeBeganListener listener : listeners) {
            listener.handshakeBegan(handshakeBeganEvent);
        }     
    }

    private void connectHandshakeListeners(SSLSocket secureSocket) {
        
        HandshakeCompletedListener [] listeners = new HandshakeCompletedListener[handshakeCompletedListeners.size()];
        handshakeCompletedListeners.toArray(listeners);
        
        for(HandshakeCompletedListener listener : listeners) {
            secureSocket.addHandshakeCompletedListener(listener);
        }
    }
    
}
