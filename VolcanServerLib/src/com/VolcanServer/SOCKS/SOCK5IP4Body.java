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
public class SOCK5IP4Body implements Serializeable {

    public static SOCK5IP4Body CreateSOCK5IP4Body(InputStream stream) throws IOException {
        SOCK5IP4Body req = new SOCK5IP4Body();
        req.DeSerialize(stream);
        return req;
    }
    
    @Override
    public void Serialize(OutputStream stream) throws IOException {
        DataOutputStream outputStream = new DataOutputStream(stream);
        outputStream.writeInt(ip);
        outputStream.writeShort(NetworkUtils.HostToNetworkOrder(port));
    }

    @Override
    public void DeSerialize(InputStream stream) throws IOException {
        
        //SOCK5IP4RequestBody body = new SOCK5IP4RequestBody();
        DataInputStream inputStream = new DataInputStream(stream);
        ip = inputStream.readInt();
        port = NetworkUtils.NetworkToHostOrder(inputStream.readShort());
    }
    
    public int ip;
    public short port;    
}
