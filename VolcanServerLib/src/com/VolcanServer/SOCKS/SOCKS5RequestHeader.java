/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.VolcanServer.SOCKS;

import com.VolcanServer.Serialization.Serializeable;
import java.io.*;

/**
 *
 * @author MonkeyBreath
 */
public class SOCKS5RequestHeader implements Serializeable {

    public static SOCKS5RequestHeader CreateSOCKS5RequestHeader(InputStream stream) throws IOException {
        SOCKS5RequestHeader req = new SOCKS5RequestHeader();
        req.DeSerialize(stream);
        return req;
    }
    
    @Override
    public void Serialize(OutputStream stream) throws IOException {
        DataOutputStream outputStream = new DataOutputStream(stream);
        outputStream.writeByte(version);
        outputStream.writeByte(cmd);
        outputStream.writeByte(rsv);
        outputStream.writeByte(atyp);
    }
    
    @Override
    public void DeSerialize(InputStream stream) throws IOException {
        DataInputStream inputStream = new DataInputStream(stream);
        version = inputStream.readByte();
        cmd = inputStream.readByte();
        rsv = inputStream.readByte();
        atyp = inputStream.readByte();
    }
    
    public byte version;
    public byte cmd;
    public byte rsv; /* = 0x00 */
    public byte atyp;
}
