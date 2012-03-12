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
public class MethodSelectionPacket {

    public static void Serialize(OutputStream stream, MethodSelectionPacket response) throws IOException {
        DataOutputStream outputStream = new DataOutputStream(stream);
        
        outputStream.writeByte(response.version);
        outputStream.writeByte(response.method);
    }
    
    public static MethodSelectionPacket DeSerialize(InputStream stream) throws IOException {

        DataInputStream inputStream = new DataInputStream(stream);
        
        MethodSelectionPacket msp = new MethodSelectionPacket();
        msp.version = inputStream.readByte();
        msp.method = inputStream.readByte();
        
        return msp;
    }

    public byte version;
    public byte method;
    
    public MethodSelectionPacket() {
        this((byte) 0);
    }
    
    public MethodSelectionPacket(byte met) 
    {
        version = 5;
        method = met;
    }
}
