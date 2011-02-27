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
public class CharMovement {
    static private Quaternion armPosInit=(new Quaternion()).fromAngleAxis(FastMath.QUARTER_PI, new Vector3f(-1,0,0));
    static Quaternion createUpperArmTransform(Vector3f upperArmAngles){
    //   System.out.println("upArmAngles is "+this.currentUpArmAngle[0]+","+this.currentUpArmAngle[1]+","+this.currentUpArmAngle[2]);
        Quaternion horiQ = new Quaternion();
        horiQ.fromAngleAxis(upperArmAngles.x,new Vector3f(0,1,-1));

        Quaternion vertQ = new Quaternion();
        vertQ.fromAngleAxis(upperArmAngles.y,new Vector3f(0,-1,-1));

        Quaternion twistQ = new Quaternion();
        twistQ.fromAngleAxis(upperArmAngles.z,new Vector3f(0,1,-1));

   //     return horiQ.mult(vertQ).mult(twistQ).mult(armPosInit);
        return horiQ.mult(vertQ).mult(twistQ).mult(armPosInit);
    }

    static void setUpperArmTransforms(Vector3f upperArmAngles,Node model){
    //   System.out.println("upArmAngles is "+this.currentUpArmAngle[0]+","+this.currentUpArmAngle[1]+","+this.currentUpArmAngle[2]);
        Bone bone=model.getControl(AnimControl.class).getSkeleton().getBone("UpArmL");
        bone.setUserControl(true);
        bone.setUserTransforms(Vector3f.ZERO, createUpperArmTransform(upperArmAngles), Vector3f.UNIT_XYZ);
    }

    static public void extrapolateUpperArmPosition(Vector3f upperArmAngles,Vector3f armRotationVel,Node model,float tpf){
        float speedScale=5;
        upperArmAngles.x += (FastMath.HALF_PI / 2f) * tpf * speedScale * armRotationVel.x;
        upperArmAngles.y += (FastMath.HALF_PI / 2f) * tpf * speedScale * armRotationVel.y;
        upperArmAngles.z += (FastMath.HALF_PI / 2f) * tpf * speedScale * armRotationVel.z;

        CharMovement.setUpperArmTransforms(upperArmAngles, model);
    }
}
