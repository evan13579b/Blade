/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mygame;

import com.jme3.math.Vector3f;
import com.jme3.network.message.Message;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author blah
 */
@Serializable()
public class InputMessage extends Message{
    public Vector3f armRotVel;
    public InputMessage(Vector3f armRotVel){
        this.armRotVel=armRotVel;
    }
}
