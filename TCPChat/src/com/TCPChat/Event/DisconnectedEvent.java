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
public class DisconnectedEvent extends EventObject {
    
    public DisconnectedEvent(Object source) {
        super(source);
    }
}
