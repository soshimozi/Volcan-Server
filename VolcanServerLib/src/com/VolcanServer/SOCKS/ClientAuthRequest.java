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
public class ClientAuthRequest {
    public byte version;
    public String username;
    public String password;
    
    public ClientAuthRequest() { 
        version = 1;
    }
    
    public static void Serialize(OutputStream stream, ClientAuthRequest response) throws IOException {
        DataOutputStream outputStream = new DataOutputStream(stream);
        
        outputStream.writeByte(response.version);
        
        byte [] usernameBuffer = response.username.getBytes();
        byte [] passwordBuffer = response.password.getBytes();
        
        outputStream.writeByte(usernameBuffer.length);
        outputStream.write(usernameBuffer);
        outputStream.writeByte(passwordBuffer.length);
        outputStream.write(passwordBuffer);
    }    

    public static ClientAuthRequest DeSerialize(InputStream stream) throws IOException {
        
        ClientAuthRequest req = new ClientAuthRequest();
        DataInputStream inputStream = new DataInputStream(stream);
        
        req.version = inputStream.readByte();
        
        byte [] usernameBuffer = new byte[inputStream.readByte()];
        inputStream.read(usernameBuffer);
        req.username = new String(usernameBuffer);
        
        byte [] passwordBuffer = new byte[inputStream.readByte()];
        inputStream.read(passwordBuffer);
        req.password = new String(passwordBuffer);
        
        return req;
    }    
}
