/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.EchoServer.Event;

import com.EchoServer.Event.ClientDisconnectedEvent;
import java.util.EventListener;

/**
 *
 * @author Monkeybone
 */
public abstract interface ClientDisconnectedListener extends EventListener {
    void clientDisconnected(ClientDisconnectedEvent evt);
}
