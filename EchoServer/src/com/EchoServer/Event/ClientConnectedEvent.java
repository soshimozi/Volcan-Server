/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.EchoServer.Event;

import com.VolcanServer.Net.NetworkTransport;
import java.util.EventObject;

/**
 *
 * @author MonkeyBreath
 */
public class ClientConnectedEvent extends EventObject {
    
    private final NetworkTransport connection;
    public ClientConnectedEvent(Object source, NetworkTransport connection) {
        super(source);
        
        this.connection = connection;
    }
    
    public NetworkTransport getNetworkTransport() {
        return connection;
    }
}
