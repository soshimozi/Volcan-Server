/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tcpchat;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 *
 * @author MonkeyBreath
 */
public class TCPChat {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        TCPChatController controller = new TCPChatController();
        TCPChatModel model = new TCPChatModel();
        TCPChatView view = new TCPChatView(model);

        controller.addView(view);
        controller.addModel(model);

        model.initDefault();
        
        JFrame displayFrame = new JFrame("TCP Chat");
        displayFrame.getContentPane().add(view, BorderLayout.CENTER);
        displayFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        displayFrame.pack();
        displayFrame.setVisible(true);
    }
}
