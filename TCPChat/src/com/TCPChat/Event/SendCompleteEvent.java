/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.TCPChat.Event;

import java.util.EventObject;

/**
 *
 * @author MonkeyBreath
 */
public class SendCompleteEvent extends EventObject {
    
    private final boolean success;
    private final Exception ex;
    public SendCompleteEvent(Object source, boolean success, Exception ex) {
        super(source);
        this.success = success;
        this.ex = ex;
    }
    
    public boolean getSuccess() {
        return success;
    }
    
    public Exception getException() {
        return ex;
    }
}
