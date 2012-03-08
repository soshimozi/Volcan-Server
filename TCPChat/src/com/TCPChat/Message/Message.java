/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.TCPChat.Message;

/**
 *
 * @author MonkeyBreath
 */
public class Message {
    
    private final String text;
    public Message(String text) {
        this.text = text;
    }
    
    public String getText() {
        return text;
    }
}
