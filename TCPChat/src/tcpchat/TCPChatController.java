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
import java.security.*;
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
            
    
    private SSLSocket socket;
    
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
    
    final void send() {
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

            String line = null;
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
            
    
    
    private SSLSocket getSocket(X509CacheingTrustManager tm, String address, int port) throws KeyManagementException, IOException, NoSuchAlgorithmException {
        SSLContext context = SSLContext.getInstance("TLS");       
        context.init(null, new TrustManager[]{tm}, null);
        SSLSocketFactory factory = context.getSocketFactory();
        return (SSLSocket) factory.createSocket(address, port);
    }

    private X509CacheingTrustManager getTrust(KeyStore ks) throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory tmf =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
        return new X509CacheingTrustManager(defaultTrustManager);
    }
    
    private static String getCertPath(String certfile, boolean loadDefault) {
        File file;
        char SEP = File.separatorChar;
        File dir = new File(System.getProperty("user.home") + SEP
                + ".volcansoft");
        file = new File(dir, certfile);
        if (file.isFile() == false && loadDefault) {
            // load the default cert file
            dir = new File(
                    System.getProperty("java.home") + 
                    SEP + "lib" + SEP + "security");

            file = new File(dir, "cacerts");
         
        }

        return file.toString();
    }
    
    private KeyStore loadKeyStore(String certfile, String certpassword) {
        InputStream in = null;
        
        try {
            
            File file = new File(getCertPath(certfile, true));
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
    
    private SSLSocket negotiateSecureSocket(String address, int port, String keystore, String password) throws KeyStoreException, KeyManagementException, CertificateEncodingException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore ks = loadKeyStore(keystore, password);
        X509CacheingTrustManager tm = getTrust(ks);

        SSLSocket client = getSocket(tm, address, port);
        
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
        installCertificateChain(tm.getChain(), ks, address, keystore, password);

        // get socket and reload keystore
        client = getSocket(
                getTrust(loadKeyStore(keystore, password)), 
                address, port);
        
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

    private void installCertificateChain(X509Certificate[] chain, KeyStore ks, String address, String keystore, String password) throws NoSuchAlgorithmException, CertificateEncodingException, KeyStoreException, FileNotFoundException, IOException, CertificateException {

        for (int i = 0; i < chain.length; i++) {
            if( showAcceptCertificateDialog(chain[i])) {
                String alias = address + "-" + (i + 1);
                ks.setCertificateEntry(alias, chain[i]);
                saveCertificate(ks, keystore, password);
            }
        }
    }
    
    
    //private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();
    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (int b : bytes) {
            b &= 0xff;
            sb.append("0123456789abcdef".toCharArray()[b >> 4]);
            sb.append("0123456789abcdef".toCharArray()[b & 15]);
            sb.append(' ');
        }
        return sb.toString();
    }

    private void saveCertificate(KeyStore ks, String keystore, String password) throws FileNotFoundException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        OutputStream out = new FileOutputStream(getCertPath(keystore, false));
        ks.store(out, password.toCharArray());
        out.close();
    }

    private boolean showAcceptCertificateDialog(X509Certificate cert) {
        
        MessageDigest sha1 = null;
        
        try {
            sha1 = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
        }

        StringBuilder outputBuilder = new StringBuilder();

        outputBuilder.append("Subject: ");
        outputBuilder.append(cert.getSubjectDN());
        outputBuilder.append("\nIssuer: ");
        outputBuilder.append(cert.getIssuerDN());
        
        if( sha1 != null ) {
            try {
                sha1.update(cert.getEncoded());
            } catch (CertificateEncodingException ex) {
                Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
            }

            outputBuilder.append("\nsha1: ");
            outputBuilder.append(toHexString(sha1.digest()));
        }
        
        if( md5 != null ) {
            try {
                md5.update(cert.getEncoded());
            } catch (CertificateEncodingException ex) {
                Logger.getLogger(TCPChatController.class.getName()).log(Level.SEVERE, null, ex);
            }

            outputBuilder.append("\nmd5: ");
            outputBuilder.append(toHexString(md5.digest()));
        }
        
        outputBuilder.append("\n");

        outputBuilder.append("Trust certificate from server?");
        
        final JOptionPane optionPane = new JOptionPane(
                outputBuilder.toString(),       
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
                        //If you were going to check something
                        //before closing the window, you'd do
                        //it here.
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
