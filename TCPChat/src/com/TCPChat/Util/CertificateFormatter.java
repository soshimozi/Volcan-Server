/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.TCPChat.Util;

import com.TCPChat.Controller.TCPChatController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Monkeybone
 */
public class CertificateFormatter {
    
    private final X509Certificate cert;
    public CertificateFormatter(X509Certificate cert) {
        this.cert = cert;
    }
    
    @Override
    public String toString() {
        MessageDigest sha1 = null;
        
        try {
            sha1 = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
        }

        StringBuilder outputBuilder = new StringBuilder();

        outputBuilder.append("Subject: ");
        outputBuilder.append(cert.getSubjectDN());
        outputBuilder.append("\nIssuer: ");
        outputBuilder.append(cert.getIssuerDN());
        
        if( sha1 != null ) {
            try {
                sha1.update(cert.getEncoded());
            } catch (CertificateEncodingException ex) {
                Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
            }

            outputBuilder.append("\nsha1: ");
            outputBuilder.append(toHexString(sha1.digest()));
        }
        
        if( md5 != null ) {
            try {
                md5.update(cert.getEncoded());
            } catch (CertificateEncodingException ex) {
                Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
            }

            outputBuilder.append("\nmd5: ");
            outputBuilder.append(toHexString(md5.digest()));
        }
        
        return outputBuilder.toString();
    }
    
    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (int b : bytes) {
            b &= 0xff;
            sb.append("0123456789abcdef".toCharArray()[b >> 4]);
            sb.append("0123456789abcdef".toCharArray()[b & 15]);
            sb.append(' ');
        }
        return sb.toString();
    }
    
}
