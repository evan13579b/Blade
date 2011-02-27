/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mygame;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Bone;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author blah
 */
public class ClientCharacterEntity extends CharacterEntity {
    protected Vector3f prevUpArmAngle;
    long timeOfLastUpdate=0;

    public ClientCharacterEntity(Node model){
        super(model);
    }

    @Override
    public void onRemoteUpdate(float latencyDelta){

        if(upperArmAngles!=null){
       //     setTransforms(upperArmAngles.z);
  //          float extrapolatedSelfAngle,angleDiff,extrapolatedForeignAngle,newupperArmAngles;
            Vector3f extrapolatedSelfAngles,angleDiffs,extrapolatedForeignAngles,newUpperArmAngles=new Vector3f();
            if(prevUpArmAngle!=null){
                extrapolatedSelfAngles=extrapolateAngles(prevUpArmAngle,((float)(System.currentTimeMillis()-timeOfLastUpdate)/1000));
  //              extrapolatedSelfAngle=extrapolateAngles(prevUpArmAngle.z,((float)(System.currentTimeMillis()-timeOfLastUpdate)/1000));
  //              extrapolatedForeignAngle=extrapolateAngles(upperArmAngles.z,latencyDelta);
                extrapolatedForeignAngles=extrapolateAngles(upperArmAngles,latencyDelta);
            //    System.out.println("extrapolatedAngle:"+extrapolatedSelfAngle+",actualAngle:"+extrapolatedForeignAngle);
   //             angleDiff=upperArmAngles.z-extrapolatedSelfAngle;
                angleDiffs=upperArmAngles.subtract(extrapolatedSelfAngles);

   /*             if(Math.abs(angleDiff)<0.5){
                    newUpperArmAngles=extrapolatedSelfAngle;
                }
                else{
                    newUpperArmAngles=(extrapolatedForeignAngle+extrapolatedSelfAngle)/2;
                }

                upperArmAngles=new Vector3f(0,0,newUpperArmAngles);*/

                if(Math.abs(angleDiffs.x)<0.5){
                    newUpperArmAngles.x=extrapolatedSelfAngles.x;
                }
                else{
                    newUpperArmAngles.x=extrapolatedForeignAngles.x;
                }

                if(Math.abs(angleDiffs.y)<0.5){
                    newUpperArmAngles.y=extrapolatedSelfAngles.y;
                }
                else{
                    newUpperArmAngles.y=extrapolatedForeignAngles.y;
                }

                if(Math.abs(angleDiffs.z)<0.5){
                    newUpperArmAngles.z=extrapolatedSelfAngles.z;
                }
                else{
                    newUpperArmAngles.z=extrapolatedForeignAngles.z;
                }

                upperArmAngles=new Vector3f(newUpperArmAngles);
            }

            
            prevUpArmAngle=upperArmAngles.clone();
   

            

        //    upperArmAngles=new Vector3f(0,0,extrapolateAngles(upperArmAngle.z,latencyDelta));
            timeOfLastUpdate=System.currentTimeMillis();
        }
     //   System.out.println("onRemoteUpdate");
    }


    private void setTransforms(float upArmAngle){
    //   System.out.println("upArmAngles is "+this.currentUpArmAngle[0]+","+this.currentUpArmAngle[1]+","+this.currentUpArmAngle[2]);
        Quaternion q = new Quaternion();
        q.fromAngles(0, upArmAngle, 0);
    //    System.out.println("in remote update");
        Bone bone=this.model.getControl(AnimControl.class).getSkeleton().getBone("UpArmL");
        bone.setUserControl(true);
        bone.setUserTransforms(Vector3f.ZERO, q, Vector3f.UNIT_XYZ);
    }

    private float interpolateAngles(float angle1,float angle2,float blendAmount){
        return angle1+blendAmount*(angle2-angle1);
    }

    @Override
    public void onLocalUpdate(){
        if(prevUpArmAngle!=null){
            CharMovement.setUpperArmTransforms(extrapolateAngles(prevUpArmAngle,((float)(System.currentTimeMillis()-timeOfLastUpdate)/1000)),
                    this.model);
        }
//       System.out.println("delta:"+delta);
    }

    @Override
    public void interpolate(float blendAmount){
        if (prevUpArmAngle != null && upperArmAngles != null) {
            float tempUpArmAngle[] = {0, 0, 0};

            tempUpArmAngle[2] = interpolateAngles(prevUpArmAngle.z, upperArmAngles.z, blendAmount);

            setTransforms(tempUpArmAngle[2]);
        }
    }

    @Override
    public void extrapolate(float tpf){
        if(prevUpArmAngle!=null){
            setTransforms(extrapolateAngles(prevUpArmAngle.z,tpf));
        }
    }

    public float extrapolateAngles(float currentAngle,float tpf){
       
        float tempUpArmAngle[]={0,0,0};
        tempUpArmAngle[2]=currentAngle+(FastMath.HALF_PI / 2f) * tpf * 5f * upperArmVelocity.z;
        
        return tempUpArmAngle[2];
    }

    public Vector3f extrapolateAngles(Vector3f currentAngles,float tpf){
        float speedScale=3;
        Vector3f extrapolatedAngles=new Vector3f();
        extrapolatedAngles.x=currentAngles.x+(FastMath.HALF_PI / 2f) * tpf * speedScale * upperArmVelocity.x;
        extrapolatedAngles.y=currentAngles.y+(FastMath.HALF_PI / 2f) * tpf * speedScale * upperArmVelocity.y;
        extrapolatedAngles.z=currentAngles.z+(FastMath.HALF_PI / 2f) * tpf * speedScale * upperArmVelocity.z;
        return extrapolatedAngles;
    }
}
