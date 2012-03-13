/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.VolcanServer.SOCKS;

import com.VolcanServer.SOCKS.Enum.AddressTypes;
import com.VolcanServer.SOCKS.Enum.Responses;
import com.VolcanServer.Serialization.Serializeable;
import java.io.*;

/**
 *
 * @author MonkeyBreath
 */
public class SOCKS5Response implements Serializeable {

    public static SOCKS5Response CreateSOCKS5Response(InputStream stream) throws IOException {
        SOCKS5Response req = new SOCKS5Response();
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
        //outputStream.writeInt(ip_src);
        //outputStream.writeShort(NetworkUtils.HostToNetworkOrder(port_src));
    }

    @Override
    public void DeSerialize(InputStream stream) throws IOException {
        
        DataInputStream inputStream = new DataInputStream(stream);
        version = inputStream.readByte();
        cmd = inputStream.readByte();
        rsv = inputStream.readByte();
        atyp = inputStream.readByte();
        //ip_src = inputStream.readInt();
        //port_src = NetworkUtils.NetworkToHostOrder(inputStream.readByte());
    }
    
    public byte version;
    public byte cmd;
    public byte rsv; /* = 0x00 */
    public byte atyp;
    //public int ip_src;
    //public short port_src;

    public SOCKS5Response() {
        this(true);
    }

    public SOCKS5Response(boolean succeeded)
    {
        version = 5;
        cmd = succeeded ? (byte) Responses.RESP_SUCCEDED.getValue() : (byte) Responses.RESP_GEN_ERROR.getValue();
        rsv = 0;
        atyp = (byte) AddressTypes.ATYP_IPV4.getValue();
    }
}
