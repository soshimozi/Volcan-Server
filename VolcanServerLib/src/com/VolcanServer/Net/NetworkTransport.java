package com.VolcanServer.Net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.channels.SocketChannel;

public abstract interface NetworkTransport {
    
    public SocketAddress getSocketAddress();

    public void open(InetAddress address, int port) throws IOException;
    public void open(String address, int port) throws UnknownHostException, IOException;
    public void close(boolean force) throws IOException;

    public  boolean getIsConnected();
    
    public InputStream getInStream() throws IOException;
    public OutputStream getOutStream() throws IOException;
    
    public int getReceiveTimeout() throws SocketException;
    public void setReceiveTimeout(int value) throws SocketException;
    
    public void setSendTimeout(int value) throws SocketException;
    public int getSendTimeout() throws SocketException;
    
    public SocketChannel getSocketChannel();
    public Socket getSocket();
}
