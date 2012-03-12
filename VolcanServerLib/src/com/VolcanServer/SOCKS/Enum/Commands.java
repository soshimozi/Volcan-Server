/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.VolcanServer.SOCKS.Enum;

/**
 *
 * @author MonkeyBreath
 */
public enum Commands {
    CMD_CONNECT(1),
    CMD_BIND(2),
    CMD_UDP_ASSOCIATIVE(3);    
    
    private int value;    

    private Commands(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }    
}
