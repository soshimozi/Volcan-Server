/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package socksclient;

/**
 *
 * @author MonkeyBreath
 */
interface ConnectionState<TOut, TIn> {
    
    public void handle(TOut out);
        
    
}
