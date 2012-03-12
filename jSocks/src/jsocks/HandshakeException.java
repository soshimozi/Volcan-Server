/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsocks;

/**
 *
 * @author MonkeyBreath
 */
public class HandshakeException extends Exception {
    public HandshakeException() {
        super();
    }
    
    public HandshakeException(String message) {
        super(message);
    }
}
