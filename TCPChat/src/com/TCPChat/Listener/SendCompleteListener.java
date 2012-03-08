/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.TCPChat.Listener;

import com.TCPChat.Event.SendCompleteEvent;
import java.util.EventListener;

/**
 *
 * @author MonkeyBreath
 */
public abstract interface SendCompleteListener extends EventListener {
    void SendComplete(SendCompleteEvent event);
}
