/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.VolcanServer.Net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.channels.SocketChannel;
import javax.net.SocketFactory;

/**
 *
 * @author Monkeybone
 */
public class SocketTransport implements NetworkTransport {
    
    private Socket socket = null;
    public SocketTransport(Socket socket) {
        this.socket = socket;
    }

    public SocketTransport() {
    }

    public static NetworkTransport CreateSocketTransport(String address, int port) throws UnknownHostException, IOException {
        
        SocketFactory factory = SocketFactory.getDefault();
//        return factory.createSocket(host, port);
//        Socket socket = new Socket(address, port);
//        //socket.//
        return new SocketTransport(factory.createSocket(address, port));
    }
    
    @Override
    public SocketAddress getSocketAddress() {
        return socket.getRemoteSocketAddress();
    }

    @Override
    public boolean getIsConnected() {
        return socket != null && socket.isConnected();
    }

    @Override
    public InputStream getInStream() throws IOException {
        return socket.getInputStream();
    }

    @Override
    public OutputStream getOutStream() throws IOException {
        return socket.getOutputStream();
    }

    @Override
    public int getReceiveTimeout() throws SocketException {
        return socket.getSoTimeout();
    }

    @Override
    public void setReceiveTimeout(int value) throws SocketException {
        socket.setSoTimeout(value);
    }

    @Override
    public void setSendTimeout(int value) throws SocketException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getSendTimeout() throws SocketException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void open(InetAddress address, int port) throws IOException {
        if( !getIsConnected() ) {
            socket = new Socket(address, port);
        }
    }

    @Override
    public void open(String address, int port) throws UnknownHostException, IOException {
        if( !getIsConnected() ) {
            socket = new Socket(address, port);
        }
    }

    @Override
    public void close(boolean force) throws IOException {
        if( getIsConnected() )
            socket.close();
    }

    @Override
    public SocketChannel getSocketChannel() {
        
        //if( getIsConnected() ) {
            return socket.getChannel();
        //} else {
        //    return null;
       // }
    }

    @Override
    public Socket getSocket() {
        return socket;
    }
}
