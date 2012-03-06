/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.EchoServer.Network;

import com.EchoServer.Event.ClientDisconnectedEvent;
import com.EchoServer.Event.ClientDisconnectedListener;
import com.EchoServer.Event.MessageEvent;
import com.EchoServer.Event.MessageListener;
import com.EchoServer.Stream.OutputStreamWrapper;
import com.VolcanServer.Net.NetworkTransport;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Monkeybone
 */
public class ClientConnection{
    
    private final NetworkTransport transport;
    private final List<ClientDisconnectedListener> clientDisconnectedListeners = new CopyOnWriteArrayList<ClientDisconnectedListener>();
    private final List<MessageListener> messageListeners = new CopyOnWriteArrayList<MessageListener>();

    public ClientConnection(NetworkTransport transport) {
        this.transport = transport;
        
        startReceiveThread();
    }

    private Thread receiveThread = null;
    ClientReader reader = null;
    private void startReceiveThread() {
        
        if( receiveThread == null ) {
            
            try {
                reader = new ClientReader(this, transport.getInStream());

                receiveThread = new Thread(reader);
                receiveThread.start();                
                
            } catch (IOException ex) {
                Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }
    
    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }
    
    public void addClientDisconnectedListener(ClientDisconnectedListener listener) {
        clientDisconnectedListeners.add(listener);
    }
    
    public void removeClientDisconnectedListener(ClientDisconnectedListener listener) {
        clientDisconnectedListeners.remove(listener);
    }
    
    protected void fireClientDisconnected(Exception ex) {
        ClientDisconnectedListener [] listeners = new ClientDisconnectedListener[clientDisconnectedListeners.size()];
        clientDisconnectedListeners.toArray(listeners);
        
        for(ClientDisconnectedListener se : listeners) {
            se.clientDisconnected(new ClientDisconnectedEvent(this, ex));
        }
    }
    
    protected void fireMessage(MessageEvent evt) {
        MessageListener [] listeners = new MessageListener[messageListeners.size()];
        messageListeners.toArray(listeners);

        for(MessageListener m : listeners) {
            m.onMessage(evt);
        }
        
    }

    public NetworkTransport getTransport() {
        return transport;
    }
    
    public void close() {
        try {
            transport.close(true);
        } catch (IOException ex) {
            Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        reader.stop();
    }

    public void send(String data) {
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new OutputStreamWrapper(transport.getOutStream()));
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(data + "\r\n");        
            bufferedWriter.flush();

        } catch (IOException ex) {
            Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    
}
