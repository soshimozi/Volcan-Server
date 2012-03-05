/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tcpchat;

import com.mvc.model.BaseModel;

/**
 *
 * @author MonkeyBreath
 */
public class TCPChatModel extends BaseModel {
 
    private StatusEnum status;
    private String host;
    private int port;
    private String chatEntry;
    private String incommingMessage;

    public void initDefault() {
        setStatus(StatusEnum.Disconnected);
        setHost("");
        setPort(0);
        setChatEntry("");
    }

    public String getHost() {
        return host;
    }
    
    public void setHost(String value) {
        String oldValue = this.host;
        this.host = value;

        firePropertyChange(TCPChatController.ELEMENT_HOST_PROPERTY, oldValue, host);
    }
    
    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        StatusEnum oldValue = this.status;
        this.status = status;

        firePropertyChange(TCPChatController.ELEMENT_STATUS_PROPERTY, oldValue, status);
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int value) {
        int oldValue = this.port;
        this.port = value;
        
        firePropertyChange(TCPChatController.ELEMENT_PORT_PROPERTY, oldValue, port);
    }

    public String getChatEntry() {
        return chatEntry;
    }
    
    public void setChatEntry(String value) {
        String oldValue = this.chatEntry;
        this.chatEntry = value;
        
        firePropertyChange(TCPChatController.ELEMENT_CHAT_ENTRY_PROPERTY, oldValue, value);
    }
    
    public String getIncommingMessage() {
        return incommingMessage;
    }
    
    public void setIncommingMessage(String value) {
        String oldValue = this.incommingMessage;
        this.incommingMessage = value;
        
        firePropertyChange(TCPChatController.ELEMENT_INCOMMING_MESSAGE_PROPERTY, oldValue, value);
    }    
   
}
