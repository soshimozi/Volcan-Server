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
public class ErrorEvent extends EventObject {
    
    private final Exception exception;
    public ErrorEvent(Object source, Exception exception) {
        super(source);
        this.exception = exception;
    }
    
    public Exception getException() {
        return (exception);
    }
             
}
