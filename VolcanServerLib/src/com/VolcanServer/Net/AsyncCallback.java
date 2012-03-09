/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.VolcanServer.Net;

/**
 *
 * @author MonkeyBreath
 */
public interface AsyncCallback {
    void onSuccess(Object result);
    void onFailure(Throwable caught);
}
