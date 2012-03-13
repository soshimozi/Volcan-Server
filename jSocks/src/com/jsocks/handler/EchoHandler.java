/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jsocks.handler;

import com.VolcanServer.Net.NetworkTransport;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author MonkeyBreath
 */
public class EchoHandler implements Runnable {

    private final NetworkTransport readSocket;
    private final NetworkTransport writeSocket;
    public EchoHandler(NetworkTransport readSocket, NetworkTransport writeSocket) {
        this.readSocket = readSocket;
        this.writeSocket = writeSocket;
    }
    
    @Override
    public void run() {

        int bufferSize = 4096;
        byte [] buffer = new byte[bufferSize];
        
        try {

            InputStream in = readSocket.getInStream();
            OutputStream out = writeSocket.getOutStream();

            int bytesRead = 0;
            while(bytesRead >= 0) {
                bytesRead = in.read(buffer);
                if( bytesRead > 0 ) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            
            Logger.getLogger(EchoHandler.class.getName()).log(Level.INFO, "EchoHandler thread exiting.");

        } catch (IOException ex) {
            Logger.getLogger(EchoHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            
            try {
                readSocket.close(true);
            } catch (IOException ex) {
            }

            try {
                writeSocket.close(true);
            } catch (IOException ex) {
            }
        }

    }
}
