/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tcpchat;

import java.awt.BorderLayout; 
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

/**9
 *
 * @author MonkeyBreath
 */
public class TCPChat {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        ChatConfiguration config = readConfiguration();
        
        TCPChatController controller = new TCPChatController(config);
        TCPChatModel model = new TCPChatModel();
        TCPChatView view = new TCPChatView(model);

        controller.addView(view);
        controller.addModel(model);

        model.initDefault();
        
        JFrame displayFrame = new JFrame("TCP Chat");
        displayFrame.getContentPane().add(view, BorderLayout.CENTER);
        displayFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        displayFrame.validate();
        displayFrame.pack();
        displayFrame.setVisible(true);
    }

    private static ChatConfiguration readConfiguration() {
        
        ChatConfiguration config = new ChatConfiguration();
        
        XMLConfiguration xmlConfiguration = null;
        try {
            xmlConfiguration = new XMLConfiguration("config.xml");
        } catch (ConfigurationException ex) {
            Logger.getLogger(TCPChat.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        if( xmlConfiguration != null ) {
            
            String keystoreFile = xmlConfiguration.getString("security.keystore-file");
            String keystorePassword = xmlConfiguration.getString("security.keystore-password");
            Boolean useSSL = xmlConfiguration.getBoolean("security.usessl");
            
            //copy values from configuration
            config.setSecurity(ChatConfiguration.CreateSecurityConfiguration());
            
            config.getSecurity().setKeyStoreFile(keystoreFile);
            config.getSecurity().setKeystorePassword(keystorePassword);
            config.getSecurity().setUseSSL(useSSL);
            
        }

        return config;
    }
}
