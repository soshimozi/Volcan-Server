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
public class MethodSelectionPacket implements Serializable {

    public static MethodSelectionPacket CreateMethodSelectionPacket(InputStream stream) throws IOException {
        MethodSelectionPacket msp = new MethodSelectionPacket();
        msp.DeSerialize(stream);
        return msp;
    }
    
    public void Serialize(OutputStream stream) throws IOException {
        DataOutputStream outputStream = new DataOutputStream(stream);
        
        outputStream.writeByte(version);
        outputStream.writeByte(method);
    }
    
    public void DeSerialize(InputStream stream) throws IOException {

        DataInputStream inputStream = new DataInputStream(stream);
        version = inputStream.readByte();
        method = inputStream.readByte();
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
