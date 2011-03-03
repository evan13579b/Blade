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
@Serializable()
public class CharCreationMessage extends Message{
    public int clientID;

    public CharCreationMessage(int clientID){
        this.clientID=clientID;
    }
}
