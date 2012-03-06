/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tcpchat;

/**
 *
 * @author Monkeybone
 */
public class ChatConfiguration {
    
    private SecurityConfiguration security;
    
    public static SecurityConfiguration CreateSecurityConfiguration() {
        return new SecurityConfiguration();
    }
    
    static class SecurityConfiguration {
        
        private boolean useSSL;
        private String keystoreFile;
        private String keystorePassword;
        
        public String getKeystorePassword() {
            return keystorePassword;
        }
        
        public void setKeystorePassword(String value) {
            keystorePassword = value;
        }
        
        public boolean getUseSSL() {
            return useSSL;
        }
        
        public void setUseSSL(boolean value) {
            this.useSSL = value;
        }
        
        public String getKeystoreFile() {
            return keystoreFile;
        }
        
        public void setKeyStoreFile(String value) {
            this.keystoreFile = value;
        }
        
    }
    
    public SecurityConfiguration getSecurity() {
        return security;
    }
    
    public void setSecurity(SecurityConfiguration value) {
        security = value;
    }
}
