/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package echoclient;

import com.VolcanServer.Net.SocketTransport;
import com.VolcanServer.Security.X509CacheingTrustManager;
import javax.net.ssl.*;
import java.io.*;
import java.net.UnknownHostException;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
;

/**
 *
 * @author MonkeyBreath
 */
public class EchoClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {

            String address = "127.0.0.1";
            int port = 5555;
            
            System.out.println("Connecting to server on port 4000...");

            final SSLSocket client = negotiateCertificate(address, port, ".keystore", "changeit");
            if( client != null ) {

                System.out.println("Connected!");

                startMessagePump(client);

                InputStream inputstream = System.in;
                InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
                BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

                OutputStream outputstream = client.getOutputStream();
                OutputStreamWriter outputstreamwriter = new OutputStreamWriter(outputstream);
                BufferedWriter bufferedwriter = new BufferedWriter(outputstreamwriter);

                String string = null;
                while ((string = bufferedreader.readLine()) != null) {
                    bufferedwriter.write(string + '\n');
                    bufferedwriter.flush();
                }

                client.close();
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyStoreException ex) {
            Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyManagementException ex) {
            Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
        }
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

    private static SSLSocket negotiateCertificate(String host, int port, String certfile, String certpassword) throws FileNotFoundException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, KeyManagementException {

        int failCount = 0;
        boolean done = false;
        
        SSLSocket socket = null;
        while(!done && failCount < 3) {

            KeyStore ks = loadKeyStore(certfile, certpassword);

            SSLContext context = SSLContext.getInstance("TLS");
            X509CacheingTrustManager tm = getTrust(ks);
            context.init(null, new TrustManager[]{tm}, null);
            SSLSocketFactory factory = context.getSocketFactory();  

            System.out.println("Opening connection to " + host + ":" + port + "...");
            socket = (SSLSocket) factory.createSocket(host, port);
            try {
                System.out.println("Starting SSL handshake...");
                socket.startHandshake();
                System.out.println();
                System.out.println("No errors, certificate is already trusted");
                return socket;
            } catch (SSLException e) {
                System.out.println();

                socket.close();

                failCount++;

                try {

                    // try to get certificate
                    installCertificateChain(tm.getChain(), ks, host, certfile, certpassword);
                }
                catch (FileNotFoundException ex) {
                    Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
               
        return null;
    }
    
    private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();
    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (int b : bytes) {
            b &= 0xff;
            sb.append(HEXDIGITS[b >> 4]);
            sb.append(HEXDIGITS[b & 15]);
            sb.append(' ');
        }
        return sb.toString();
    }
    
    private static KeyStore loadKeyStore(String certfile, String certpassword) {
        InputStream in = null;
        
        try {
            
            File file = new File(getCertPath(certfile, true));
            System.out.println("Loading KeyStore " + file + "...");
            in = new FileInputStream(file);
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(in, certpassword.toCharArray());
            in.close();
            return ks;
            
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyStoreException ex) {
            Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
        } 
        finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return null;
    }

    private static void installCertificateChain(X509Certificate[] chain, KeyStore ks, String host, String certfile, String certpassword) throws NoSuchAlgorithmException, CertificateEncodingException, KeyStoreException, FileNotFoundException, IOException, CertificateException {
        
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(System.in));

        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        for (int i = 0; i < chain.length; i++) {
            System.out.println();
            System.out.println("Unknown signature from server:");
            System.out.println();

            X509Certificate cert = chain[i];
            System.out.println
                    (" " + (i + 1) + " Subject " + cert.getSubjectDN());
            System.out.println("   Issuer  " + cert.getIssuerDN());
            sha1.update(cert.getEncoded());
            System.out.println("   sha1    " + toHexString(sha1.digest()));
            md5.update(cert.getEncoded());
            System.out.println("   md5     " + toHexString(md5.digest()));
            System.out.println();
            
            System.out.println("Trust certificate from server? [y/n]: [n]");
            String line = null;
            try {
                line = reader.readLine().trim();
            } catch (IOException ex) {
                Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        
            if( line != null && "y".equals(line) ) {
                
                String alias = host + "-" + (i + 1);
                ks.setCertificateEntry(alias, cert);

                saveCertificate(ks, certfile, certpassword);

                //System.out.println();
                //System.out.println(cert);
                //System.out.println();
                System.out.println
                        ("Added certificate to keystore " + certfile + " using alias '"
                                + alias + "'");
            } else {
                System.out.println("KeyStore not changed");
            }
        }
    }

    private static X509CacheingTrustManager getTrust(KeyStore ks) throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory tmf =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
        return new X509CacheingTrustManager(defaultTrustManager);
    }

    private static void startMessagePump(final SSLSocket client) {
        
        Thread readerThread = new Thread(
                new Runnable()
                {
                    @Override
                    public void run() {

                        InputStream inputStream = null;

                        try {

                            inputStream = client.getInputStream();
                            InputStreamReader reader = new InputStreamReader(inputStream);
                            BufferedReader bufferedReader = new BufferedReader(reader);

                            String line = null;
                            while((line = bufferedReader.readLine()) != null) {
                                System.out.println(line + "\n");
                            }

                        } catch (IOException ex) {
                            Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
                        } finally {

                            try {
                                inputStream.close();
                            } catch (IOException ex) {
                                Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        }
                    }
                }
        );

        readerThread.start();
                
    }

    private static void saveCertificate(KeyStore ks, String certfile, String certpassword) throws FileNotFoundException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        OutputStream out = new FileOutputStream(getCertPath(certfile, false));
        ks.store(out, certpassword.toCharArray());
        out.close();
    }
    
}
