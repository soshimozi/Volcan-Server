/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.TCPChat.Listener;

import com.TCPChat.Event.ErrorEvent;
import java.util.EventListener;

/**
 *
 * @author MonkeyBreath
 */
public abstract interface ErrorListener extends EventListener {
    void OnError(ErrorEvent event);
}
