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
public class SOCKS5RequestHeader {

    public static void Serialize(OutputStream stream, SOCKS5RequestHeader value) throws IOException {
        DataOutputStream outputStream = new DataOutputStream(stream);
        outputStream.writeByte(value.version);
        outputStream.writeByte(value.cmd);
        outputStream.writeByte(value.rsv);
        outputStream.writeByte(value.atyp);
    }
    
    public static SOCKS5RequestHeader DeSerialize(InputStream stream) throws IOException {
        SOCKS5RequestHeader header = new SOCKS5RequestHeader();
        
        DataInputStream inputStream = new DataInputStream(stream);
        header.version = inputStream.readByte();
        header.cmd = inputStream.readByte();
        header.rsv = inputStream.readByte();
        header.atyp = inputStream.readByte();
        
        return header;
    }
    
    public byte version;
    public byte cmd;
    public byte rsv; /* = 0x00 */
    public byte atyp;
}
