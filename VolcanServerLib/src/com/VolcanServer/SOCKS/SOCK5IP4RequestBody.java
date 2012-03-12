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
public class SOCK5IP4RequestBody {

    public static void Serialize(OutputStream stream, SOCK5IP4RequestBody value) throws IOException {
        DataOutputStream outputStream = new DataOutputStream(stream);
        outputStream.writeInt(value.ip_dst);
        outputStream.writeShort(NetworkUtils.HostToNetworkOrder(value.port));
    }

    public static SOCK5IP4RequestBody DeSerialize(InputStream stream) throws IOException {
        
        SOCK5IP4RequestBody body = new SOCK5IP4RequestBody();
        DataInputStream inputStream = new DataInputStream(stream);
        body.ip_dst = inputStream.readInt();
        body.port = NetworkUtils.NetworkToHostOrder(inputStream.readShort());
        
        return body;
    }
    
    public int ip_dst;
    public short port;    
}
