/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package echoserver;

import java.util.EventObject;

/**
 *
 * @author Monkeybone
 */
public class ClientDisconnectedEvent extends EventObject {
    
    private final Exception error;
    public ClientDisconnectedEvent(Object source, Exception error) {
        super(source);
        this.error = error;
    }
    
    public Exception getError() {
        return error;
    }
}
