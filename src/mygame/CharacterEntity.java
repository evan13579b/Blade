/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mygame;

import com.jme3.math.Vector3f;
import com.jme3.network.sync.Sync;
import com.jme3.network.sync.SyncEntity;
import com.jme3.scene.Node;

/**
 *
 * @author blah
 */
public class CharacterEntity implements SyncEntity{
    protected @Sync Vector3f upperArmAngles;
    protected @Sync Vector3f upperArmVelocity;
    protected @Sync Float elbowWristAngle;
    protected @Sync Float elbowWristVel;

    protected Node model;

    public CharacterEntity(Node model) {
        this.model=model;
    }

    public void setElbowWrist(Float elbowWristAngle,Float elbowWristVel){
        this.elbowWristAngle=new Float(elbowWristAngle);
        this.elbowWristVel=new Float(elbowWristVel);
    }

    public void setUpperArmAngles(Vector3f upperArmAngles){
        this.upperArmAngles=new Vector3f(upperArmAngles);
  //      System.out.println("set upperArmAngles:"+upperArmAngles.x+","+upperArmAngles.y+","+upperArmAngles.z);
    }

    public void setUpArmVelocity(Vector3f upperArmVelocity){
        this.upperArmVelocity=new Vector3f(upperArmVelocity);
    }

    public void extrapolate(float tpf){}
    public void interpolate(float blendAmount){}
    public void onLocalUpdate(){

    }
    public void onRemoteCreate(){}
    public void onRemoteDelete(){}
    public void onRemoteUpdate(float latencyDelta){}
}
