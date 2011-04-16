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
    static public final float upperArmSpeed=2;
    static public final float lowerArmSpeed=2;
    static public final float charTurnSpeed=1f;
    static public final float charStrafeSpeed=0.2f;
    static public final float charForwardSpeed=0.2f;
    static public final float charBackwordSpeed=0.2f;
    static public final float walkSpeedFactor=4f;

    static public final class Constraints{
        static public final float uYRotMax=FastMath.PI-FastMath.QUARTER_PI;
        static public final float uYRotMin=0;
        static public final float uXRotMax=FastMath.HALF_PI;
        static public final float uXRotMin=-FastMath.HALF_PI;
        static public final float lRotMax=FastMath.QUARTER_PI;//FastMath.HALF_PI;
        static public final float lRotMin=-FastMath.QUARTER_PI/2;
        static public final float uZRotMax=FastMath.HALF_PI+FastMath.QUARTER_PI;
        static public final float uZRotMin=-FastMath.HALF_PI;
    }

    static private Quaternion armPosInit=(new Quaternion()).fromAngleAxis(3*FastMath.QUARTER_PI/4, new Vector3f(-1,0,0));
    static Quaternion createUpperArmTransform(Vector3f upperArmAngles){
        Quaternion horiQ = new Quaternion();
        horiQ.fromAngleAxis(upperArmAngles.x,new Vector3f(0,1,-1));

        Quaternion vertQ = new Quaternion();
        vertQ.fromAngleAxis(upperArmAngles.y,new Vector3f(0,1,1));

        Quaternion twistQ = new Quaternion();
        twistQ.fromAngleAxis(upperArmAngles.z,new Vector3f(0,1,-1));

       return horiQ.mult(vertQ).mult(twistQ).mult(armPosInit);
    }

    static Quaternion createLowerArmTransform(Float elbowWristRotation){
        Quaternion rotation=new Quaternion();
        rotation.fromAngles(0,0,elbowWristRotation);
 //       System.out.println(rotation);
        return rotation;
    }

    static private Quaternion wristRotInit=(new Quaternion()).fromAngleAxis(3*FastMath.QUARTER_PI/4, new Vector3f(-1,0,0));
    static Quaternion createWristTransform(Float elbowWristRotation){
        Quaternion rotation=new Quaternion();
        rotation.fromAngles(elbowWristRotation,0,0);
 //       System.out.println(rotation);
        return rotation.mult(wristRotInit);
    }

    static void setUpperArmTransform(Vector3f upperArmAngles,Node model){
        Bone upperArm=model.getControl(AnimControl.class).getSkeleton().getBone("UpArmR");
        upperArm.setUserControl(true);
        upperArm.setUserTransforms(Vector3f.ZERO, createUpperArmTransform(upperArmAngles), Vector3f.UNIT_XYZ);
    }

    static void setLowerArmTransform(Float elbowWristRotation,Node model){
        Bone lowerArm=model.getControl(AnimControl.class).getSkeleton().getBone("LowArmR");
        Bone hand=model.getControl(AnimControl.class).getSkeleton().getBone("HandR");
        
        lowerArm.setUserControl(true);
        hand.setUserControl(true);
     //   System.out.println(Vector3f.UNIT_XYZ);
        hand.setUserTransforms(Vector3f.ZERO, createWristTransform(elbowWristRotation), Vector3f.UNIT_XYZ);
        lowerArm.setUserTransforms(Vector3f.ZERO, createLowerArmTransform(elbowWristRotation), Vector3f.UNIT_XYZ);
    }
 
    static public Vector3f extrapolateUpperArmAngles(Vector3f upperArmAngles,Vector3f upperArmVel,float tpf){
        Vector3f newUpperArmAngles=new Vector3f(upperArmAngles);
        newUpperArmAngles.x += (FastMath.HALF_PI / 2f) * tpf * upperArmSpeed * upperArmVel.x;
        if(newUpperArmAngles.x<Constraints.uXRotMin){
            newUpperArmAngles.x=Constraints.uXRotMin;
        }
        if(newUpperArmAngles.x>Constraints.uXRotMax){
            newUpperArmAngles.x=Constraints.uXRotMax;
        }
        newUpperArmAngles.y += (FastMath.HALF_PI / 2f) * tpf * upperArmSpeed * upperArmVel.y;
        if(newUpperArmAngles.y<Constraints.uYRotMin){
            newUpperArmAngles.y=Constraints.uYRotMin;
        }
        if(newUpperArmAngles.y>Constraints.uYRotMax){
            newUpperArmAngles.y=Constraints.uYRotMax;
        }
        newUpperArmAngles.z += (FastMath.HALF_PI / 2f) * tpf * upperArmSpeed * upperArmVel.z;
        if(newUpperArmAngles.z<Constraints.uZRotMin){
            newUpperArmAngles.z=Constraints.uZRotMin;
        }
        if(newUpperArmAngles.z>Constraints.uZRotMax){
            newUpperArmAngles.z=Constraints.uZRotMax;
        }

        return newUpperArmAngles;
    }

    static public float extrapolateLowerArmAngles(Float elbowWristAngle,Float elbowWristVel,Float tpf){
        float newElbowWristAngle=elbowWristAngle + (FastMath.HALF_PI / 2f) * tpf * lowerArmSpeed * elbowWristVel;
        if(newElbowWristAngle<Constraints.lRotMin){
            newElbowWristAngle=Constraints.lRotMin;
            elbowWristVel=0f;
        }
        if(newElbowWristAngle>Constraints.lRotMax){
            newElbowWristAngle=Constraints.lRotMax;
            elbowWristVel=0f;
        }
        return newElbowWristAngle;
    }

    static public float extrapolateCharTurn(float charAngle,float charTurnVel,float tpf){
        return charAngle+(FastMath.HALF_PI / 2f) * tpf * charTurnSpeed * charTurnVel;
    }

    static public Vector3f extrapolateCharMovement(Vector3f charPosition,Vector3f charVelocity,float charAngle,float tpf){
            float xDir,zDir;
            zDir=FastMath.cos(charAngle);
            xDir=FastMath.sin(charAngle);
            Vector3f viewDirection=new Vector3f(xDir,0,zDir);

      //      Vector3f walkDirection=new Vector3f(


            Vector3f forward,up,left;
            float xVel,zVel;
            xVel=charVelocity.x;
            zVel=charVelocity.z;
            forward=new Vector3f(viewDirection);
            up=new Vector3f(0,1,0);
            left=up.cross(forward);


            return charPosition.add((left.mult(xVel).add(forward.mult(zVel)).mult(charForwardSpeed*walkSpeedFactor)));
    }
}
