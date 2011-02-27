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
    protected @Sync Vector3f upperArmAngle;
    protected @Sync Vector3f upperArmVelocity;

    protected Node model;

    public CharacterEntity(Node model) {
        this.model=model;
    }

    public void setUpperArmAngle(Vector3f upperArmAngle){
        this.upperArmAngle=new Vector3f(upperArmAngle);
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
