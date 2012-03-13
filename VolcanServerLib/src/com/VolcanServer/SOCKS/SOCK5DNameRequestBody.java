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
public class SOCK5DNameRequestBody implements Serializeable {

    public static SOCK5DNameRequestBody CreateSOCK5DNameRequestBody(InputStream stream) throws IOException {
        SOCK5DNameRequestBody req = new SOCK5DNameRequestBody();
        req.DeSerialize(stream);
        return req;
    }
    
    @Override
    public void Serialize(OutputStream stream) throws IOException {
        DataOutputStream outputStream = new DataOutputStream(stream);
        outputStream.writeByte(length);
        outputStream.writeBytes(dname);
    }

    @Override
    public void DeSerialize(InputStream stream) throws IOException {
        DataInputStream inputStream = new DataInputStream(stream);
        length = inputStream.readByte();
        
        byte [] data = new byte[length];
        inputStream.read(data);
        
        dname = new String(data);
    }
    
    public byte length;
    public String dname;
    
}
