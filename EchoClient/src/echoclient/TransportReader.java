/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package echoclient;

import com.VolcanServer.Net.NetworkTransport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author MonkeyBreath
 */
public class TransportReader implements Runnable {

    private final InputStream stream;
    public TransportReader(NetworkTransport transport) throws IOException {
        stream = transport.getInStream();
    }
            
    @Override
    public void run() {
        try {
            InputStreamReader reader = new InputStreamReader(stream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            
            String line = null;
            while((line = bufferedReader.readLine()) != null) {
                
                System.out.println(line + "\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(TransportReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
