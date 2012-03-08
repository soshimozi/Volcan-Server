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
public class HandshakeBeganEvent extends EventObject {
    public HandshakeBeganEvent(Object source) {
        super(source);
    }
}
