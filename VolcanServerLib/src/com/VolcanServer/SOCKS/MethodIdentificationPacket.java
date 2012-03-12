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
public class MethodIdentificationPacket {
    public byte version;
    public byte nmethods;
    public byte [] methods;
    
    public static MethodIdentificationPacket DeSerialize(InputStream stream) throws IOException {
        
        MethodIdentificationPacket mip = new MethodIdentificationPacket();
        
        DataInputStream inputStream = new DataInputStream(stream);
        mip.version = inputStream.readByte();
        mip.nmethods = inputStream.readByte();
        
        if( mip.nmethods > 0 ) {
            mip.methods = new byte[mip.nmethods];
            inputStream.read(mip.methods);
        }
        
        return mip;
    }

    public static void Serialize(OutputStream stream, MethodIdentificationPacket value) throws IOException {
        
        DataOutputStream outputStream = new DataOutputStream(stream);
        outputStream.writeByte(value.version);
        outputStream.writeByte(value.nmethods);
        if( value.nmethods > 0 ) {
            outputStream.write(value.methods);
        }
    }

}
