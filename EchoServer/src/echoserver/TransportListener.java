/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package echoserver;

import com.VolcanServer.Net.NetworkTransport;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 *
 * @author MonkeyBreath
 */
public abstract interface TransportListener {
    public void initialize(int listenPort) throws IOException;
    public NetworkTransport accept() throws IOException;
    public void close();
    
//    public void Start() throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException;
//    public void Stop();
}
