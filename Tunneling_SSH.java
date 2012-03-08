SSLSocketFactory factory =
                  (SSLSocketFactory)SSLSocketFactory.getDefault();
         
         /*
         * Set up a socket to do tunneling through the proxy.
         * Start it off as a regular socket, then layer SSL
         * over the top of it.
         */
         tunnelHost = System.getProperty("https.proxyHost");
         tunnelPort = Integer.getInteger("https.proxyPort").intValue();
         
         Socket tunnel = new Socket(tunnelHost, tunnelPort);
         doTunnelHandshake(tunnel, host, port);
         
         /*
         * Ok, let's overlay the tunnel socket with SSL.
         */
         SSLSocket socket =
               (SSLSocket)factory.createSocket(tunnel, host, port, true);
         
         /*
         * register a callback for handshaking completion event
         */
         socket.addHandshakeCompletedListener(
            new HandshakeCompletedListener() {
               public void handshakeCompleted(
                  HandshakeCompletedEvent event) {
                  System.out.println("Handshake finished!");
                  System.out.println(
                  "\t CipherSuite:" + event.getCipherSuite());
                  System.out.println(
                  "\t SessionId " + event.getSession());
                  System.out.println(
                  "\t PeerHost " + event.getSession().getPeerHost());
               }
            }
         );


        
        /*
        * send http request
        *
        * See SSLSocketClient.java for more information about why
        * there is a forced handshake here when using PrintWriters.
        */
        socket.startHandshake();
        
         PrintWriter out = new PrintWriter(
                              new BufferedWriter(
                                 new OutputStreamWriter(
                                    socket.getOutputStream())));
         
         out.println("GET http://www.verisign.com/index.html HTTP/1.0");
         out.println();
         out.flush();