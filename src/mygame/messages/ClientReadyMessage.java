/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.messages;

import com.jme3.network.message.Message;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author blah
 */
@Serializable(id=9000)
public class ClientReadyMessage extends Message{
    public ClientReadyMessage(){
        
    }
}
