/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package echoserver;

import java.util.EventListener;

/**
 *
 * @author Monkeybone
 */
public abstract interface ClientDisconnectedListener extends EventListener {
    void clientDisconnected(ClientDisconnectedEvent evt);
}
