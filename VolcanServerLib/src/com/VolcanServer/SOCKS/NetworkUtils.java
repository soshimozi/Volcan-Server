/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.VolcanServer.SOCKS;

import java.nio.ByteBuffer;

/**
 *
 * @author MonkeyBreath
 */
public class NetworkUtils {
    
    public static String intToIp(int i) {
        
        return ((i >> 24 ) & 0xFF) + "." +

               ((i >> 16 ) & 0xFF) + "." +

               ((i >>  8 ) & 0xFF) + "." +

               ( i        & 0xFF);
    }
    
    public static int ipToInt(String addr) {
        String[] addrArray = addr.split("\\.");

        int num = 0;

        for (int i=0;i<addrArray.length;i++) {

            int power = 3-i;

            num += ((Integer.parseInt(addrArray[i])%256 * Math.pow(256,power)));

        }

        return num;
    }    

    public static short NetworkToHostOrder(short value) {
        ByteBuffer bb = ByteBuffer.allocate(256);
        bb.putShort(value);
        
        bb.flip();
        return bb.getShort();
    }    
    
    public static short HostToNetworkOrder(short value) {
        ByteBuffer bb = ByteBuffer.allocate(256);
        bb.putShort(value);
        
        bb.flip();
        return bb.getShort();
    }
}
