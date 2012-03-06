/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.EchoServer.Event;

import java.util.EventObject;

/**
 *
 * @author MonkeyBreath
 */
public class MessageEvent extends EventObject {
    
    private final String message;
    public MessageEvent(Object source, String message) {
        super(source);
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
}
