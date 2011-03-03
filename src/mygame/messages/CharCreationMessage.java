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
@Serializable(id=30)
public class CharCreationMessage extends Message{
    public long playerID;

    public CharCreationMessage(long playerID){
        super();
        this.playerID=playerID;
    }

    public CharCreationMessage(){
        super();
    }
}
