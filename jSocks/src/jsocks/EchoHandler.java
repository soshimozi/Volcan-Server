/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsocks;

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

    private final Socket readSocket;
    private final Socket writeSocket;
    public EchoHandler(Socket readSocket, Socket writeSocket) {
        this.readSocket = readSocket;
        this.writeSocket = writeSocket;
    }
    
    @Override
    public void run() {

        int bufferSize = 1024;
        byte [] buffer = new byte[bufferSize];
        
            try {
                
                InputStream in = readSocket.getInputStream();
                OutputStream out = writeSocket.getOutputStream();

                while(true) {
                    int count = in.read(buffer);
                    out.write(buffer, 0, count);
                }
                
            } catch (IOException ex) {
                Logger.getLogger(EchoHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
}
