/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.messages;

import com.jme3.math.Vector3f;
import com.jme3.network.message.Message;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author blah
 */
@Serializable(id=9002)
public class SwordBodyCollisionMessage extends Message{
    public Vector3f coordinates;

    public SwordBodyCollisionMessage(Vector3f coordinates) {
        super();
        this.coordinates = new Vector3f(coordinates);
    }

    public SwordBodyCollisionMessage() {
        super();
    }
}
