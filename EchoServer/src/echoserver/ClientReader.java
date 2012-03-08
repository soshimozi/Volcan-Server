/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package echoserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Semaphore;

/**
 *
 * @author Monkeybone
 */
public class ClientReader implements Runnable {

    private final Semaphore serverStopEvent = new Semaphore(0);
    
    private final ClientConnection connection;
    private final InputStreamWrapper inStreamWrapper;
    
    public ClientReader(ClientConnection connection, InputStream inputStream) {
        this.connection = connection;
        inStreamWrapper =  new InputStreamWrapper(inputStream);
    }
    
    @Override
    public void run() {
        boolean stopped = false;
        
        while(!stopped) {
            
            synchronized(serverStopEvent) {
                try {
                    serverStopEvent.wait(1);
                    
                    if( serverStopEvent.tryAcquire() ) {
                        // if we got here then the event was set
                        stopped = true;
                    }
                } catch (InterruptedException ex) {
                    // we are expecting this most of the time
                    // so do nothing
                    
                    //Logger.getLogger(ClientReader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            // read message from stream
            if( !stopped ) {
                try {
                    
                    InputStreamReader reader = new InputStreamReader(inStreamWrapper);
                    
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    
                    String line = bufferedReader.readLine();
                    
                    if( line == null ) {
                        connection.fireClientDisconnected(null);
                    }
                    else {
                        connection.fireMessage(new MessageEvent(connection, line));
                    }
                    
                } catch (IOException ex) {
                    
                    // notify any listeners that we have a problem
                    connection.fireClientDisconnected(ex);
                }
            }
        }
    }
    
    public void stop() {
        
        synchronized(serverStopEvent) {
            serverStopEvent.release();
        }
    }

    
    
}
