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
@Serializable(id=40)
public class CharPositionMessage extends Message {
    public Vector3f upperArmAngles,upperArmVels;
    public float elbowWristAngle,elbowWristVel;

    public CharPositionMessage(Vector3f upperArmAngles,Vector3f upperArmVels,float elbowWristAngle,float elbowWristVel){
        this.upperArmAngles=upperArmAngles.clone();
        this.upperArmVels=upperArmVels.clone();
        this.elbowWristAngle=elbowWristAngle;
        this.elbowWristVel=elbowWristVel;
    }
}
