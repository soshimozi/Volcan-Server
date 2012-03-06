/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tcpchat;

import com.VolcanServer.Net.X509CacheingTrustManager;
import com.mvc.controller.BaseController;
import com.mvc.view.AbstractViewPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.*;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author MonkeyBreath
 */
public class TCPChatController extends BaseController {
    public static String ELEMENT_STATUS_PROPERTY = "Status";
    public static String ELEMENT_CHAT_ENTRY_PROPERTY = "ChatEntry";
    public static String ELEMENT_CONNECT_ACTION = "Connect";
    public static String ELEMENT_HOST_PROPERTY = "Host";
    public static String ELEMENT_PORT_PROPERTY = "Port";
    public static String ELEMENT_DISCONNECT_ACTION = "Disconnect";
    public static String ELEMENT_SEND_ACTION = "Send";
    public static String ELEMENT_INCOMMING_MESSAGE_PROPERTY = "IncommingMessage";
            
    
    private Socket socket;
    private final ChatConfiguration config;
    
    public  TCPChatController(ChatConfiguration config) {
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
    
    private Thread sendThread = null;
    
    final void send() {
           
        if( sendThread == null ) {
            sendThread = new Thread(
                    new Runnable() {

                        @Override
                        public void run() {
                            try {
                                String chatText = (String) getModelProperty(TCPChatModel.class, TCPChatController.ELEMENT_CHAT_ENTRY_PROPERTY);
                                BufferedWriter bufferedwriter 
                                        = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                                bufferedwriter.write(chatText + "\n");
                                bufferedwriter.flush();
                            } catch (IOException ex) {
                                Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
                            } 
                            setModelProperty(ELEMENT_CHAT_ENTRY_PROPERTY, "");

                            sendThread = null;
                        }

                    }
                );

            sendThread.start();
        }
        
        
    }
    
    final void disconnect() {
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
        }

        socket = null;
        setModelProperty(ELEMENT_STATUS_PROPERTY, StatusEnum.Disconnected);
    }
    
    final void connect() {

        Thread connectThread = new Thread(
                new Runnable()
                {

                    @Override
                    public void run() {
                        String address = (String) getModelProperty(TCPChatModel.class, ELEMENT_HOST_PROPERTY);
                        int port = (Integer) getModelProperty(TCPChatModel.class, ELEMENT_PORT_PROPERTY);

                        if( config.getSecurity().getUseSSL()) {
                            try {
                                socket = negotiateSecureSocket(address, port, ".keystore", "changeit");
                            } catch (NoSuchAlgorithmException ex) {
                                Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (KeyStoreException ex) {
                                Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (KeyManagementException ex) {
                                Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (CertificateEncodingException ex) {
                                Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (CertificateException ex) {
                                Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        if( socket != null ) {

                            // start thread up to read
                            // make sure thread fires close event

                            setModelProperty(ELEMENT_STATUS_PROPERTY, StatusEnum.Connected);
                            
                            // use this thread to pump messages
                            pumpMessages();

                        } else {

                            setModelProperty(ELEMENT_STATUS_PROPERTY, StatusEnum.Error);
                        }
                    }
                });
        
        
        connectThread.start();
                
    }

    private void pumpMessages() {
        InputStream inputStream = null;

        try {

            inputStream = socket.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);

            String line;
            while((line = bufferedReader.readLine()) != null) {
                setModelProperty(ELEMENT_INCOMMING_MESSAGE_PROPERTY, line + "\n");
            }

        } catch (IOException ex) {
            Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {

            try {
                inputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        
        setModelProperty(ELEMENT_STATUS_PROPERTY, StatusEnum.Disconnected);
        socket = null;
    }
            
    private SSLSocket getSecureSocket(X509TrustManager trust, String address, int port) throws KeyManagementException, IOException, NoSuchAlgorithmException {
        SSLContext context = SSLContext.getInstance("TLS");       
        context.init(null, new TrustManager[]{trust}, null);
        SSLSocketFactory factory = context.getSocketFactory();
        return (SSLSocket) factory.createSocket(address, port);
    }

    
    private String getCertPath(boolean loadDefault) {
        
        // load from xml configuration
        String keystoreFile = config.getSecurity().getKeystoreFile();

        // add .volcansoft directory if not there
        File directory = new File(System.getProperty("user.home") + File.separatorChar
                + ".volcansoft");
        
        if( !directory.exists()) {
            directory.mkdir();
        }
        
        File file = new File(directory, keystoreFile);
        if (file.isFile() == false && loadDefault) {
            // load the default cert file
            directory = new File(
                    System.getProperty("java.home") + 
                    File.separatorChar + "lib" + File.separatorChar + "security");

            file = new File(directory, "cacerts");
        }

        return file.toString();
    }
    
    private KeyStore loadKeyStore() {
        InputStream in = null;
        
        try {
            
            String certpassword = config.getSecurity().getKeystorePassword();
            
            File file = new File(getCertPath(true));
            in = new FileInputStream(file);
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(in, certpassword.toCharArray());
            in.close();
            return ks;
            
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyStoreException ex) {
            Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
        } 
        finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return null;
    }
    
    private SSLSocket negotiateSecureSocket(String host, int port, String keystore, String password) throws KeyStoreException, KeyManagementException, CertificateEncodingException, CertificateException, IOException, NoSuchAlgorithmException {

        X509CacheingTrustManager tm = 
                (X509CacheingTrustManager) X509CacheingTrustManager.CreateTrustManager(loadKeyStore());

        SSLSocket client = getSecureSocket(tm, host, port);

        setModelProperty(ELEMENT_STATUS_PROPERTY, StatusEnum.Handshaking);

        int soTimeout = client.getSoTimeout();
        client.setSoTimeout(10000);
        try {
            client.startHandshake();

            client.setSoTimeout(soTimeout);
            return client;
        } catch (SSLException e) {
            client.close();
        }

        // we got here so that means the handshake failed, so install cert chain
        installCertificateChain(tm.getChain(), loadKeyStore(), host);

        // get socket and reload keystore
        client = getSecureSocket(
                X509CacheingTrustManager.CreateTrustManager(loadKeyStore()), 
                host, port);

        soTimeout = client.getSoTimeout();
        client.setSoTimeout(10000);
        try {
            client.startHandshake();

            client.setSoTimeout(soTimeout);
            return client;
        } catch (SSLException e) {
            client.close();
        }

        // something happened while saving the cert, can't go on
        return null;
    }

    private void installCertificateChain(X509Certificate[] chain, KeyStore keystore, String hostname) throws NoSuchAlgorithmException, CertificateEncodingException, KeyStoreException, FileNotFoundException, IOException, CertificateException {

        //KeyStore ks = loadKeyStore(keystore, password);
        
        for (int i = 0; i < chain.length; i++) {
            if( showAcceptCertificateDialog(chain[i])) {
                String alias = hostname + "-" + (i + 1);
                keystore.setCertificateEntry(alias, chain[i]);
                saveCertificate(keystore);
            }
        }
    }
    
    private void saveCertificate(KeyStore ks) throws FileNotFoundException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        OutputStream out = new FileOutputStream(getCertPath(false));
        ks.store(out, config.getSecurity().getKeystorePassword().toCharArray());
        out.close();
    }

    private boolean showAcceptCertificateDialog(X509Certificate cert) {
        
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
               
}
