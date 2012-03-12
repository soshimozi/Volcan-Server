/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.VolcanServer.SOCKS.Enum;

/**
 *
 * @author MonkeyBreath
 */
public enum ConnectionMethods {
    METHOD_NOAUTH(0),
    METHOD_AUTH(2),
    METHOD_NOTAVAILABLE(0xff);

    private final int value;
    
    private ConnectionMethods(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
}
