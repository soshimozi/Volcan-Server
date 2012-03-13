/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package socksclient;

import com.VolcanServer.Net.NetworkTransport;
import com.VolcanServer.Net.SocketTransport;
import com.VolcanServer.SOCKS.Enum.AddressTypes;
import com.VolcanServer.SOCKS.Enum.Commands;
import com.VolcanServer.SOCKS.Enum.ConnectionMethods;
import com.VolcanServer.SOCKS.Enum.Responses;
import com.VolcanServer.SOCKS.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.net.Proxy.Type;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 *
 * @author MonkeyBreath
 */
public class SOCKSClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // TODO code application logic here
            
            // proxy address here
            String host = "127.0.0.1";
            int port = 5555;
            
            final Preferences prefs = Preferences.userRoot().node("/java/net/socks");            
            prefs.put("username", "USERNAME");
            prefs.put("password", "PASSWORD");
            
            InetSocketAddress proxyAddress = new InetSocketAddress(host, port);
            Socket socket = new Socket(new Proxy(Type.SOCKS, proxyAddress));
            
            InetSocketAddress address = new InetSocketAddress(host, 4444);
            socket.connect(address);
            
////            NetworkTransport client = SocketTransport.CreateSocketTransport("localhost", 5555);
////           
////            MethodIdentificationPacket mip = new MethodIdentificationPacket();
////            mip.version = 5;
////            mip.nmethods = 1;
////            mip.methods = new byte[1];
////            mip.methods[0] = (byte) ConnectionMethods.METHOD_AUTH.getValue();
////            
////            mip.Serialize(client.getOutStream());
////            
////            MethodSelectionPacket msp = MethodSelectionPacket.CreateMethodSelectionPacket(client.getInStream());
////            
////            if( msp.method == ConnectionMethods.METHOD_AUTH.getValue()) {
////                ClientAuthRequest authRequest = new ClientAuthRequest();
////                authRequest.username = "USERNAME";
////                authRequest.password = "PASSWORD";
////
////                authRequest.Serialize(client.getOutStream());
////                
////                ClientAuthResponse authResponse = ClientAuthResponse.CreateClientAuthResponse(client.getInStream());
////                if( authResponse.version == 1 && authResponse.response == Responses.RESP_SUCCEDED.getValue()) {
////                    SOCKS5RequestHeader requestHeader = new SOCKS5RequestHeader();
////                    
////                    requestHeader.version = 5;
////                    requestHeader.rsv = 0;
////                    requestHeader.cmd = (byte) Commands.CMD_CONNECT.getValue();
////                    requestHeader.atyp = (byte) AddressTypes.ATYP_IPV4.getValue();
////                    
////                    requestHeader.Serialize(client.getOutStream());
////                    SOCK5IP4RequestBody body = new SOCK5IP4RequestBody();
////                    
////                    // get ip for a particular address
////                    String hostname = "localhost";
////                    String ipAddress = InetAddress.getByName(hostname).getHostAddress();
////                    body.ip_dst = NetworkUtils.ipToInt(ipAddress);
////                    body.port = 4444;
////                    
////                    body.Serialize(client.getOutStream());
////                    
////                    SOCKS5Response response = SOCKS5Response.CreateSOCKS5Response(client.getInStream());
//                    if( response.cmd == Responses.RESP_SUCCEDED.getValue()) {
                        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                        
                        outputStream.writeUTF("Hello world!\n");
                        
                        // send something
                        while(true) {
                            
                            
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(SOCKSClient.class.getName()).log(Level.SEVERE, null, ex);
                                Thread.currentThread().interrupt();
                            }
                        //}
                    //}
                //}
            }
           
        } catch (UnknownHostException ex) {
            Logger.getLogger(SOCKSClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SOCKSClient.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        
    }
}
