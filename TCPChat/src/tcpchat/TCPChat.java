/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tcpchat;

import com.TCPChat.Configuration.ChatConfiguration;
import com.TCPChat.Controller.TCPChatController;
import com.TCPChat.Model.TCPChatModel;
import com.TCPChat.View.ChatView;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
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
        ChatView chatView = new ChatView(model);

        controller.addView(chatView);
        controller.addModel(model);

        model.initDefault();
        
        JFrame displayFrame = new JFrame("TCP Chat");
        displayFrame.getContentPane().add(chatView, BorderLayout.CENTER);
        displayFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        displayFrame.setMinimumSize(new Dimension(561, 427));
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
            
            String programHome = System.getProperty("user.home") + File.separatorChar + ".volcansoft";
            
            File keystorePath = new File(programHome, keystoreFile);
            
            //copy values from configuration
            config.setSecurity(ChatConfiguration.CreateSecurityConfiguration());
            
            config.getSecurity().setKeyStoreFile(keystorePath.toString());
            config.getSecurity().setKeystorePassword(keystorePassword);
            config.getSecurity().setUseSSL(useSSL);
            
        }

        return config;
    }
}
