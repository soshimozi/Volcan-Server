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
public class ClientAuthRequest implements Serializable {
    public byte version;
    public String username;
    public String password;
    
    public static ClientAuthRequest CreateClientAuthRequest(InputStream stream) throws IOException {
        ClientAuthRequest req = new ClientAuthRequest();
        req.DeSerialize(stream);
        return req;
    }
    
    public ClientAuthRequest() { 
        version = 1;
    }
    
    public void Serialize(OutputStream stream) throws IOException {
        DataOutputStream outputStream = new DataOutputStream(stream);
        
        outputStream.writeByte(version);
        
        outputStream.writeByte(username.length());
        outputStream.writeBytes(username);
        outputStream.writeByte(password.length());
        outputStream.writeBytes(password);
    }    

    public void DeSerialize(InputStream stream) throws IOException {
        
        DataInputStream inputStream = new DataInputStream(stream);
        
        version = inputStream.readByte();
        
        byte [] usernameBuffer = new byte[inputStream.readByte()];
        inputStream.read(usernameBuffer);
        username = new String(usernameBuffer);
        
        byte [] passwordBuffer = new byte[inputStream.readByte()];
        inputStream.read(passwordBuffer);
        password = new String(passwordBuffer);
    }    
}
