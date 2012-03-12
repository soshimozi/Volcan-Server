/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.VolcanServer.SOCKS;

import com.VolcanServer.SOCKS.Enum.AddressTypes;
import com.VolcanServer.SOCKS.Enum.Responses;
import java.io.*;

/**
 *
 * @author MonkeyBreath
 */
public class SOCKS5Response {

    public static void Serialize(OutputStream stream, SOCKS5Response response) throws IOException {
        DataOutputStream outputStream = new DataOutputStream(stream);
        
        outputStream.writeByte(response.version);
        outputStream.writeByte(response.cmd);
        outputStream.writeByte(response.rsv);
        outputStream.writeByte(response.atyp);
        outputStream.writeInt(response.ip_src);
        outputStream.writeShort(NetworkUtils.HostToNetworkOrder(response.port_src));
    }

    public static SOCKS5Response DeSerialize(InputStream stream) throws IOException {
        
        SOCKS5Response response = new SOCKS5Response();
        
        DataInputStream inputStream = new DataInputStream(stream);
        response.version = inputStream.readByte();
        response.cmd = inputStream.readByte();
        response.rsv = inputStream.readByte();
        response.atyp = inputStream.readByte();
        response.ip_src = inputStream.readInt();
        response.port_src = NetworkUtils.NetworkToHostOrder(inputStream.readByte());
        
        return response;
    }
    
    public byte version;
    public byte cmd;
    public byte rsv; /* = 0x00 */
    public byte atyp;
    public int ip_src;
    public short port_src;

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
