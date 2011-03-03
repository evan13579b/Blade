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
    protected float prevElbowWristAngle;
    protected long timeOfLastUpdate = 0;

    public ClientCharacterEntity(Node model) {
        super(model);
    }

    @Override
    public void onRemoteUpdate(float latencyDelta) {
  //      System.out.println("remoteUpdate");
        if (upperArmAngles != null) {

            Vector3f extrapolatedSelfAngles, upperArmAngleDiffs, extrapolatedForeignAngles, newUpperArmAngles = new Vector3f();
            float lowerArmAngleDiff, extrapolatedSelfElbowWristAngle, extrapolatedForeignElbowWristAngle;
            float timeDiff;
            if (prevUpArmAngle != null) {
                timeDiff = ((float) (System.currentTimeMillis() - timeOfLastUpdate) / 1000);
                extrapolatedSelfAngles = CharMovement.extrapolateUpperArmAngles(prevUpArmAngle, this.upperArmVelocity, timeDiff);
                extrapolatedForeignAngles = CharMovement.extrapolateUpperArmAngles(upperArmAngles, this.upperArmVelocity, latencyDelta);
                upperArmAngleDiffs = extrapolatedForeignAngles.subtract(extrapolatedSelfAngles);
                extrapolatedSelfElbowWristAngle = CharMovement.extrapolateLowerArmAngles(prevElbowWristAngle, elbowWristVel, timeDiff);
                extrapolatedForeignElbowWristAngle = CharMovement.extrapolateLowerArmAngles(elbowWristAngle, elbowWristVel, latencyDelta);
                lowerArmAngleDiff = elbowWristAngle - extrapolatedForeignElbowWristAngle;

                if (Math.abs(lowerArmAngleDiff) < 0.5) {
                    elbowWristAngle = extrapolatedSelfElbowWristAngle;
                } else {
                    elbowWristAngle = extrapolatedForeignElbowWristAngle;
                }

                if (Math.abs(upperArmAngleDiffs.x) < 0.5) {
                    newUpperArmAngles.x = extrapolatedSelfAngles.x;
                } else {
                    newUpperArmAngles.x = extrapolatedForeignAngles.x;
                }

                if (Math.abs(upperArmAngleDiffs.y) < 0.5) {
                    newUpperArmAngles.y = extrapolatedSelfAngles.y;
                } else {
                    newUpperArmAngles.y = extrapolatedForeignAngles.y;
                }

                if (Math.abs(upperArmAngleDiffs.z) < 0.5) {
                    newUpperArmAngles.z = extrapolatedSelfAngles.z;
                } else {
                    newUpperArmAngles.z = extrapolatedForeignAngles.z;
                }

                upperArmAngles = new Vector3f(newUpperArmAngles);
            }

            prevElbowWristAngle = elbowWristAngle;
            prevUpArmAngle = upperArmAngles.clone();

            timeOfLastUpdate = System.currentTimeMillis();
 //           System.out.println("upperArmAngles:"+upperArmAngles.x+","+upperArmAngles.y+","+upperArmAngles.z);
        }
    }

    private void setTransforms(float upArmAngle) {
        Quaternion q = new Quaternion();
        q.fromAngles(0, upArmAngle, 0);
        Bone bone = this.model.getControl(AnimControl.class).getSkeleton().getBone("UpArmL");
        bone.setUserControl(true);
        bone.setUserTransforms(Vector3f.ZERO, q, Vector3f.UNIT_XYZ);
    }

    private float interpolateAngles(float angle1, float angle2, float blendAmount) {
        return angle1 + blendAmount * (angle2 - angle1);
    }

    @Override
    public void onLocalUpdate() {
        if (prevUpArmAngle != null) {
            float timeDiff = ((float) (System.currentTimeMillis() - timeOfLastUpdate) / 1000);
            CharMovement.setUpperArmTransform(CharMovement.extrapolateUpperArmAngles(prevUpArmAngle, this.upperArmVelocity, timeDiff), this.model);
            CharMovement.setLowerArmTransform(CharMovement.extrapolateLowerArmAngles(prevElbowWristAngle, elbowWristVel, timeDiff), model);
        }
    }

    @Override
    public void interpolate(float blendAmount) {
        if (prevUpArmAngle != null && upperArmAngles != null) {
            float tempUpArmAngle[] = {0, 0, 0};

            tempUpArmAngle[2] = interpolateAngles(prevUpArmAngle.z, upperArmAngles.z, blendAmount);

            setTransforms(tempUpArmAngle[2]);
        }
    }

    @Override
    public void extrapolate(float tpf) {
        if (prevUpArmAngle != null) {
            setTransforms(extrapolateAngles(prevUpArmAngle.z, tpf));
        }
    }

    public float extrapolateAngles(float currentAngle, float tpf) {

        float tempUpArmAngle[] = {0, 0, 0};
        tempUpArmAngle[2] = currentAngle + (FastMath.HALF_PI / 2f) * tpf * 5f * upperArmVelocity.z;

        return tempUpArmAngle[2];
    }
}
