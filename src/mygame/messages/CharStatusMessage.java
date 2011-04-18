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
public class CharStatusMessage extends Message {
    public long playerID;
    public Vector3f upperArmAngles,upperArmVels,charPosition,charVelocity;
    public float elbowWristAngle,elbowWristVel,charAngle,charTurnVel,life;

    public CharStatusMessage(Vector3f upperArmAngles,Vector3f upperArmVels,
            Vector3f charPosition,Vector3f charVelocity,float elbowWristAngle,
            float elbowWristVel,float charAngle,float charTurnVel,long playerID,float life){
        super();
        this.playerID=playerID;
        this.upperArmAngles=upperArmAngles.clone();
        this.upperArmVels=upperArmVels.clone();
        this.elbowWristAngle=elbowWristAngle;
        this.elbowWristVel=elbowWristVel;
        this.charPosition=charPosition;
        this.charVelocity=charVelocity;
        this.charAngle=charAngle;
        this.charTurnVel=charTurnVel;
        this.life=life;
    }

    public CharStatusMessage(){
        super();
 //       upperArmAngles=new Vector3f();
//        upperArmVels=new Vector3f();
 //       elbowWristAngle=0;
 //       elbowWristVel=0;
    }
}
