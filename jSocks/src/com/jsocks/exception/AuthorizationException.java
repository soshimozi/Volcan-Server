/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jsocks.exception;

/**
 *
 * @author MonkeyBreath
 */
public class AuthorizationException extends Exception {
    
    public AuthorizationException() {
        super();
    }
    
    public AuthorizationException(String message) {
        super(message);
    }
}
