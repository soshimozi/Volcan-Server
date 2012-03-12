/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsocks;

/**
 *
 * @author MonkeyBreath
 */
public class JSocks {


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        short listenPort = 5555;

        boolean useSSL = false;
        String keyStoreFile = "";
        String keyStorePassword = "";
        String proxyUserName = "USERNAME";
        String proxyPassword = "PASSWORD";
        
        SOCKSServer server = new SOCKSServer(useSSL, keyStoreFile, keyStorePassword, proxyUserName, proxyPassword, listenPort);
        server.run();
    }


}
