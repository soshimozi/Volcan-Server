   public SSLTunnelSocketFactory(String proxyhost, String proxyport){
      tunnelHost = proxyhost;
      tunnelPort = Integer.parseInt(proxyport);
      dfactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
   }



   public Socket createSocket(Socket s, String host, int port, 
                              boolean autoClose) 
                              throws IOException,UnknownHostException
   {
   
      Socket tunnel = new Socket(tunnelHost,tunnelPort);
      
      doTunnelHandshake(tunnel,host,port);
      
      SSLSocket result = (SSLSocket)dfactory.createSocket(
                                       tunnel,host,port,autoClose);
      
      result.addHandshakeCompletedListener(
         new HandshakeCompletedListener() {
         public void handshakeCompleted(HandshakeCompletedEvent event) {
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
   
      result.startHandshake();
   
      return result;
   }