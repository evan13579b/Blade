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
    public boolean controllable;

    public CharCreationMessage(long playerID,boolean controllable){
        super();
        this.playerID=playerID;
        this.controllable=controllable;
    }

    public CharCreationMessage(){
        super();
    }
}
