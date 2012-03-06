/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.EchoServer.Event;

import com.EchoServer.Event.MessageEvent;
import java.util.EventListener;

/**
 *
 * @author MonkeyBreath
 */
public abstract interface MessageListener extends EventListener {
    public void onMessage(MessageEvent event);
}
