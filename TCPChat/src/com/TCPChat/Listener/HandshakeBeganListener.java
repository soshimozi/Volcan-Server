/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.TCPChat.Listener;

import com.TCPChat.Event.HandshakeBeganEvent;
import java.util.EventListener;

/**
 *
 * @author MonkeyBreath
 */
public interface HandshakeBeganListener extends EventListener {
    void handshakeBegan(HandshakeBeganEvent event);
}
