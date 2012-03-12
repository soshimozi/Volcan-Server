/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.VolcanServer.SOCKS.Enum;

/**
 *
 * @author MonkeyBreath
 */
public enum Responses {
    RESP_SUCCEDED(0),
    RESP_GEN_ERROR(1);

    private final int value;
    
    private Responses(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
}
