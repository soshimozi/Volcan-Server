/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.TCPChat.Controller;

import com.TCPChat.Configuration.ChatConfiguration;
import com.TCPChat.Engine.ClientEngine;
import com.TCPChat.Event.*;
import com.TCPChat.Listener.*;
import com.TCPChat.Message.Message;
import com.TCPChat.Model.StatusEnum;
import com.TCPChat.Model.TCPChatModel;
import com.TCPChat.Security.SecureKeyManager;
import com.TCPChat.Util.CertificateFormatter;
import com.mvc.controller.BaseController;
import com.mvc.view.AbstractViewPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSocket;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author MonkeyBreath
 */
public class TCPChatController extends BaseController implements InstallCertificateListener, MessageListener, DisconnectedListener, ErrorListener, HandshakeCompletedListener, HandshakeBeganListener {
    public static String ELEMENT_STATUS_PROPERTY = "Status";
    public static String ELEMENT_CHAT_ENTRY_PROPERTY = "ChatEntry";
    public static String ELEMENT_CONNECT_ACTION = "Connect";
    public static String ELEMENT_HOST_PROPERTY = "Host";
    public static String ELEMENT_PORT_PROPERTY = "Port";
    public static String ELEMENT_DISCONNECT_ACTION = "Disconnect";
    public static String ELEMENT_SEND_ACTION = "Send";
    public static String ELEMENT_INCOMMING_MESSAGE_PROPERTY = "IncommingMessage";
    public static String ELEMENT_LAST_ERROR_PROPERTY = "LastError";
            
    
    //private Socket socket;
    private final ChatConfiguration config;
    private final ClientEngine engine;
    public  TCPChatController(ChatConfiguration config) {
        SecureKeyManager keyManager = new SecureKeyManager(config.getSecurity().getKeystoreFile(), config.getSecurity().getKeystorePassword());
        engine = new ClientEngine(this, keyManager);

        keyManager.addInstallCertificateListener(this);
        engine.addMessageListener(this);
        engine.addErrorListener(this);
        engine.addDisconnectedListener(this);
        engine.addHandshakeCompletedListener(this);
        engine.addHandshakeBeganListener(this);
        
        this.config = config;
    }
    
    @Override
    public void addView(AbstractViewPanel view) {
        
        // let's subscribe to some events
        view.addActionListener(

                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // we have a connect event, let's do something
                        if( e.getActionCommand().equals(TCPChatController.ELEMENT_CONNECT_ACTION))  {
                            setModelProperty(ELEMENT_STATUS_PROPERTY, StatusEnum.Connecting);
                            connect();
                        } else if( e.getActionCommand().equals(TCPChatController.ELEMENT_DISCONNECT_ACTION))  {
                            setModelProperty(ELEMENT_STATUS_PROPERTY, StatusEnum.Disconnecting);
                            disconnect();
                        } else if( e.getActionCommand().equals(TCPChatController.ELEMENT_SEND_ACTION))  {
                            send();
                        }

                    }
                }
        );
            
        super.addView(view);
    }
    
    public void send() {
        
        String chatText = (String) getModelProperty(TCPChatModel.class, TCPChatController.ELEMENT_CHAT_ENTRY_PROPERTY);
        Message message = new Message(chatText);

        setModelProperty(TCPChatController.ELEMENT_CHAT_ENTRY_PROPERTY, "");
        engine.send(message);
    }
    
    public void disconnect() {
        
        engine.disconnect();
        setModelProperty(ELEMENT_STATUS_PROPERTY, StatusEnum.Disconnected);        
    }
    
    public void connect() {

        setModelProperty(ELEMENT_STATUS_PROPERTY, StatusEnum.Connecting);
        Thread connectThread = new Thread(
                new Runnable()
                {

            @Override
            public void run() {
                String address = (String) getModelProperty(TCPChatModel.class, ELEMENT_HOST_PROPERTY);
                int port = (Integer) getModelProperty(TCPChatModel.class, ELEMENT_PORT_PROPERTY);

                try {
                    if( config.getSecurity().getUseSSL() ) {
                        if( engine.connectSecure(address, port) )
                            setModelProperty(ELEMENT_STATUS_PROPERTY, StatusEnum.Connected);
                    } else {
                        if( engine.connect(address, port) )
                            setModelProperty(ELEMENT_STATUS_PROPERTY, StatusEnum.Connected);
                    }
                } catch (NoSuchAlgorithmException ex) {
                    setError(ex);
                } catch (KeyStoreException ex) {
                    setError(ex);
                } catch (KeyManagementException ex) {
                    setError(ex);
                } catch (CertificateEncodingException ex) {
                    setError(ex);
                } catch (CertificateException ex) {
                    setError(ex);
                } catch (IOException ex) {
                    setError(ex);
                }
            }

                });
        
        connectThread.start();
    }

    private void setError(Exception ex) {
        Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
        setModelProperty(ELEMENT_LAST_ERROR_PROPERTY, ex.getMessage());
        setModelProperty(ELEMENT_STATUS_PROPERTY, StatusEnum.Error);
    }
    

    public boolean showAcceptCertificateDialog(X509Certificate cert) {
        
        CertificateFormatter formatter = new CertificateFormatter(cert);
        
        String paneText = formatter.toString() + "\n" + "Trust certificate from server?";
        
        final JOptionPane optionPane = new JOptionPane(
                paneText,       
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION);

        final JDialog dialog = new JDialog((JFrame)null, 
                                    "Untrusted Certificate",
                                    true);
        
        dialog.setContentPane(optionPane);
        dialog.setDefaultCloseOperation(
            JDialog.DO_NOTHING_ON_CLOSE);
        
        optionPane.addPropertyChangeListener(
            new PropertyChangeListener() {
            @Override
                public void propertyChange(PropertyChangeEvent e) {
                    String prop = e.getPropertyName();

                    if (dialog.isVisible() 
                    && (e.getSource() == optionPane)
                    && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                        dialog.setVisible(false);
                    }
                }
            });
        
        dialog.pack();
        dialog.setVisible(true);        
        
        int value = ((Integer)optionPane.getValue()).intValue();
        return value == JOptionPane.YES_OPTION;
    }

    @Override
    public void MessageReceived(MessageEvent evt) {
        if( evt.getMessage().getText() != null ) {
            setModelProperty(ELEMENT_INCOMMING_MESSAGE_PROPERTY, evt.getMessage().getText() + "\n");
        }
    }

    @Override
    public void Disconnected(DisconnectedEvent event) {
        setModelProperty(ELEMENT_STATUS_PROPERTY, StatusEnum.Disconnected);
    }

    @Override
    public void OnError(ErrorEvent event) {
        setModelProperty(ELEMENT_LAST_ERROR_PROPERTY, event.getException().getMessage());
        setModelProperty(ELEMENT_STATUS_PROPERTY, StatusEnum.Error);
    }

    @Override
    public void handshakeCompleted(HandshakeCompletedEvent hce) {
        // checkout hce
        SSLSocket socket = hce.getSocket();
    }

    @Override
    public void handshakeBegan(HandshakeBeganEvent event) {
        setModelProperty(ELEMENT_STATUS_PROPERTY, StatusEnum.Handshaking);
    }

    @Override
    public void InstallCertificate(InstallCertificateEvent evt) {
        
        UUID id = UUID.randomUUID();    
        String host = (String) getModelProperty(TCPChatModel.class, ELEMENT_HOST_PROPERTY);

        if( showAcceptCertificateDialog(evt.getCertificate()) ) {
            try {
                evt.getSecureKeyManager().installCertificate(evt.getCertificate(), host + id.toString());
            } catch (NoSuchAlgorithmException ex) {
                setError(ex);
            } catch (CertificateEncodingException ex) {
                setError(ex);
            } catch (KeyStoreException ex) {
                setError(ex);
            } catch (FileNotFoundException ex) {
                setError(ex);
            } catch (IOException ex) {
                setError(ex);
            } catch (CertificateException ex) {
                setError(ex);
            }
        }
    }
               
}
