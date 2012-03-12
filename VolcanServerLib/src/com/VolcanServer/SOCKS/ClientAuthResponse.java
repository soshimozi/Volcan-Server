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
public class ClientAuthResponse {
    public byte version;
    public byte response;
    
    public static ClientAuthResponse DeSerialize(InputStream stream) throws IOException {
        
        ClientAuthResponse resp = new ClientAuthResponse();
        
        DataInputStream inputStream = new DataInputStream(stream);
        resp.version = inputStream.readByte();
        resp.response = inputStream.readByte();
        
        return resp;
    }
    
    public static void Serialize(OutputStream stream, ClientAuthResponse value) throws IOException {
        
        DataOutputStream outputStream = new DataOutputStream(stream);
        outputStream.writeByte(value.version);
        outputStream.writeByte(value.response);
    }
}
