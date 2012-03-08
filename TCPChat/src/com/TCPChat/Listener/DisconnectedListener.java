/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.TCPChat.Listener;

import com.TCPChat.Event.DisconnectedEvent;
import java.util.EventListener;

/**
 *
 * @author MonkeyBreath
 */
public abstract interface DisconnectedListener extends EventListener {
    void Disconnected(DisconnectedEvent event);
}
