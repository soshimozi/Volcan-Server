/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package socksclient;

import com.VolcanServer.Net.NetworkTransport;
import com.VolcanServer.Net.SocketTransport;
import com.VolcanServer.SOCKS.*;
import com.VolcanServer.SOCKS.Enum.AddressTypes;
import com.VolcanServer.SOCKS.Enum.Commands;
import com.VolcanServer.SOCKS.Enum.ConnectionMethods;
import com.VolcanServer.SOCKS.Enum.Responses;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.SocketFactory;

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
            
            NetworkTransport client = SocketTransport.CreateSocketTransport("localhost", 5555);
           
            MethodIdentificationPacket mip = new MethodIdentificationPacket();
            mip.version = 5;
            mip.nmethods = 1;
            mip.methods = new byte[1];
            mip.methods[0] = (byte) ConnectionMethods.METHOD_AUTH.getValue();
            
            MethodIdentificationPacket.Serialize(client.getOutStream(), mip);
            
            MethodSelectionPacket msp = MethodSelectionPacket.DeSerialize(client.getInStream());
            
            if( msp.method == ConnectionMethods.METHOD_AUTH.getValue()) {
                ClientAuthRequest authRequest = new ClientAuthRequest();
                authRequest.username = "USERNAME";
                authRequest.password = "PASSWORD";

                ClientAuthRequest.Serialize(client.getOutStream(), authRequest);
                
                ClientAuthResponse authResponse = ClientAuthResponse.DeSerialize(client.getInStream());
                if( authResponse.version == 1 && authResponse.response == Responses.RESP_SUCCEDED.getValue()) {
                    SOCKS5RequestHeader requestHeader = new SOCKS5RequestHeader();
                    
                    requestHeader.version = 5;
                    requestHeader.rsv = 0;
                    requestHeader.cmd = (byte) Commands.CMD_CONNECT.getValue();
                    requestHeader.atyp = (byte) AddressTypes.ATYP_IPV4.getValue();
                    
                    SOCKS5RequestHeader.Serialize(client.getOutStream(), requestHeader);
                    SOCK5IP4RequestBody body = new SOCK5IP4RequestBody();
                    
                    // get ip for a particular address
                    String hostname = "www.google.com";
                    String ipAddress = InetAddress.getByName(hostname).getHostAddress();
                    body.ip_dst = NetworkUtils.ipToInt(ipAddress);
                    body.port = 80;
                    
                    SOCK5IP4RequestBody.Serialize(client.getOutStream(), body);
                    
                    SOCKS5Response response = SOCKS5Response.DeSerialize(client.getInStream());
                    if( response.cmd == Responses.RESP_SUCCEDED.getValue()) {
                        DataOutputStream outputStream = new DataOutputStream(client.getOutStream());
                        
                        // send something
                        while(true) {
                            
                            outputStream.writeUTF("Hello world!");
                            
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(SOCKSClient.class.getName()).log(Level.SEVERE, null, ex);
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                }
            }
           
        } catch (UnknownHostException ex) {
            Logger.getLogger(SOCKSClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SOCKSClient.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        
    }
}
