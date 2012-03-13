/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.VolcanServer.SOCKS;

import java.io.*;

/**
 *
 * @author MonkeyBreath
 */
public class ClientAuthResponse implements Serializable {
    public byte version;
    public byte response;
    
    public static ClientAuthResponse CreateClientAuthResponse(InputStream stream) throws IOException {
        ClientAuthResponse auth = new ClientAuthResponse();
        auth.DeSerialize(stream);
        return auth;
    }
        
    public void DeSerialize(InputStream stream) throws IOException {
        
        DataInputStream inputStream = new DataInputStream(stream);
        version = inputStream.readByte();
        response = inputStream.readByte();
    }
    
    public void Serialize(OutputStream stream) throws IOException {
        
        DataOutputStream outputStream = new DataOutputStream(stream);
        outputStream.writeByte(version);
        outputStream.writeByte(response);
    }
}
