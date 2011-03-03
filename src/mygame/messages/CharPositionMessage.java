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
    public long playerID;
    public Vector3f upperArmAngles,upperArmVels;
    public float elbowWristAngle,elbowWristVel;

    public CharPositionMessage(Vector3f upperArmAngles,Vector3f upperArmVels,
            float elbowWristAngle,float elbowWristVel,long playerID){
        super();
        this.playerID=playerID;
        this.upperArmAngles=upperArmAngles.clone();
        this.upperArmVels=upperArmVels.clone();
        this.elbowWristAngle=elbowWristAngle;
        this.elbowWristVel=elbowWristVel;
    }

    public CharPositionMessage(){
        super();
        upperArmAngles=new Vector3f();
        upperArmVels=new Vector3f();
        elbowWristAngle=0;
        elbowWristVel=0;
    }
}
