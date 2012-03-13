/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package socksclient;

import com.VolcanServer.Net.NetworkTransport;

/**
 *
 * @author MonkeyBreath
 */
public class blah<TOut, TIn> implements ConnectionState<TOut, TIn> {
    
    private final NetworkTransport client;
    public blah(NetworkTransport client) {
        this.client = client;
    }

    @Override
    public void handle(TOut out) {
        // serialize output
    }
    
    
    
    
}
