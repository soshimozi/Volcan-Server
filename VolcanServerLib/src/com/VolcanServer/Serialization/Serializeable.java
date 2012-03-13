/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.VolcanServer.Serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author MonkeyBreath
 */
public interface Serializeable {
    void Serialize(OutputStream stream) throws IOException;
    void DeSerialize(InputStream stream) throws IOException;
}
