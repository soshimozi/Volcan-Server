/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.VolcanServer.SOCKS.Enum;

/**
 *
 * @author MonkeyBreath
 */
public enum AddressTypes {
    ATYP_IPV4(1),
    ATYP_DNAME(3),
    ATYP_IPV6(4);

    private final int value;
    
    private AddressTypes(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
}
