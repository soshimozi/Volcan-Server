/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.VolcanServer.Net;

import java.io.IOException;

/**
 *
 * @author MonkeyBreath
 */
public abstract interface TransportListener {
    public void initialize(int listenPort) throws IOException;
    public NetworkTransport accept() throws IOException;
    public void close();

}
