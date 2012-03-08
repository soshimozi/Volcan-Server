/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.TCPChat.Event;

import com.TCPChat.Security.SecureKeyManager;
import java.security.cert.X509Certificate;
import java.util.EventObject;

/**
 *
 * @author MonkeyBreath
 */
public class InstallCertificateEvent extends EventObject {
    
    private final SecureKeyManager keyManager;
    private final X509Certificate certificate;
    public InstallCertificateEvent(Object source, SecureKeyManager keyManager, X509Certificate certificate) {
        super(source);
        
        this.keyManager = keyManager;
        this.certificate = certificate;
    }
    
    public SecureKeyManager getSecureKeyManager() {
        return keyManager;
    }
    
    public X509Certificate getCertificate() {
        return certificate;
    }
}
