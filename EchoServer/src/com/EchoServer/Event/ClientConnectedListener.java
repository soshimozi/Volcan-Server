/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.EchoServer.Event;

/**
 *
 * @author MonkeyBreath
 */
public abstract interface ClientConnectedListener {
    public void ClientConnected(ClientConnectedEvent event);
}
