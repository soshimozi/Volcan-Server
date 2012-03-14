/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsocks;

import com.jsocks.server.SOCKSServer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

/**
 *
 * @author MonkeyBreath
 */
public class JSocks {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        XMLConfiguration xmlConfig = null;
        try {
            xmlConfig = new XMLConfiguration("config.xml");
        } catch (ConfigurationException ex) {
            Logger.getLogger(JSocks.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        short listenPort = 27354;
        boolean useSSL = false;
        boolean allowAnonAccess = false;
        String keyStoreFile = "";
        String keyStorePassword = "";
        String proxyUserName = "USERNAME";
        String proxyPassword = "PASSWORD";

        if( xmlConfig != null ) {
            
            Logger.getLogger(JSocks.class.getName()).log(Level.INFO, "Reading configuration...");
            
            listenPort = xmlConfig.getShort("listenPort");
            useSSL = xmlConfig.getBoolean("useSSL");
            keyStoreFile = xmlConfig.getString("keyStoreFile");
            keyStorePassword = xmlConfig.getString("keyStorePassword");
            proxyUserName = xmlConfig.getString("proxyUserName");
            proxyPassword = xmlConfig.getString("proxyPassword");
        }

        SOCKSServer server = new SOCKSServer(useSSL, keyStoreFile, keyStorePassword, proxyUserName, proxyPassword, listenPort, allowAnonAccess);
        server.run();
    }
}
