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
public class MethodIdentificationPacket implements Serializable {
    public byte version;
    public byte nmethods;
    public byte [] methods;
    
    public static MethodIdentificationPacket CreateMethodIdentificationPacket(InputStream stream) throws IOException {
        MethodIdentificationPacket mip = new MethodIdentificationPacket();
        mip.DeSerialize(stream);
        return mip;
    }
    
    public void DeSerialize(InputStream stream) throws IOException {
        
        DataInputStream inputStream = new DataInputStream(stream);
        version = inputStream.readByte();
        nmethods = inputStream.readByte();
        
        if( nmethods > 0 ) {
            methods = new byte[nmethods];
            inputStream.read(methods);
        }
    }

    public void Serialize(OutputStream stream) throws IOException {
        
        DataOutputStream outputStream = new DataOutputStream(stream);
        outputStream.writeByte(version);
        outputStream.writeByte(nmethods);
        if( nmethods > 0 ) {
            outputStream.write(methods);
        }
    }

}
