/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.TCPChat.Event;

import com.TCPChat.Message.Message;
import java.util.EventObject;

/**
 *
 * @author MonkeyBreath
 */
public class MessageEvent extends EventObject {

        private final Message message;
        public MessageEvent(Object source, Message message) {
            super(source);
            
            this.message = message;
        }
        
        public Message getMessage() {
            return message;
        }
}
