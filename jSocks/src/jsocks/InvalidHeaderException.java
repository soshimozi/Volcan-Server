/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsocks;

/**
 *
 * @author MonkeyBreath
 */
class InvalidHeaderException extends Exception {

    public InvalidHeaderException() {
        super();
    }
    
    public InvalidHeaderException(String message) {
        super(message);
    }
    
}
