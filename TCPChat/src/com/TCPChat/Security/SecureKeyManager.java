/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.TCPChat.Security;

import com.TCPChat.Event.InstallCertificateEvent;
import com.TCPChat.Listener.InstallCertificateListener;
import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author MonkeyBreath
 */
public class SecureKeyManager {
    
    private final String keystoreLocation;
    private final String keystorePassword;
    
    
    public SecureKeyManager(String keystoreLocation, String keystorePassword) {
        this.keystoreLocation = keystoreLocation;
        this.keystorePassword = keystorePassword;
    }
    
    private String getCertPath(boolean loadDefault) {
        
        File file = new File(keystoreLocation);
        if (file.isFile() == false && loadDefault) {
            
            // TODO: refactor hardcoded values
            // load the default cert file
            File directory = new File(
                    System.getProperty("java.home") + 
                    File.separatorChar + "lib" + File.separatorChar + "security");

            file = new File(directory, "cacerts");
        }

        return file.toString();
    }
    
    private KeyStore loadKeyStore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException {
        InputStream in = null;
        
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        File file = new File(getCertPath(true));
        
        if( file.isFile() ) {
            try {
                in = new FileInputStream(file);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SecureKeyManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if( in != null ) {

            try {
                ks.load(in, keystorePassword.toCharArray());
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(SecureKeyManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return ks;
    }
    
    private void saveCertificate(KeyStore ks) throws FileNotFoundException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        OutputStream out = new FileOutputStream(getCertPath(false));
        ks.store(out, keystorePassword.toCharArray());
        out.close();
    }
   
    public X509TrustManager CreateTrustManager() throws NoSuchAlgorithmException, KeyStoreException, CertificateException {
        TrustManagerFactory tmf =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(loadKeyStore());
        return (X509TrustManager) tmf.getTrustManagers()[0];
    }        
    
//    public KeyStore setCertificateEntry(String entryName, X509Certificate certificate) {
//        KeyStore ks = loadKeyStore();
//        ks.setCertificateEntry(entryName, certificate);
//        return ks;
//    }
    
    public void installCertificate(X509Certificate cert, String alias) throws NoSuchAlgorithmException, CertificateEncodingException, KeyStoreException, FileNotFoundException, IOException, CertificateException {

        KeyStore ks = loadKeyStore();
        ks.setCertificateEntry(alias, cert);
        saveCertificate(ks);
    }    
}
