/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mygame;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Bone;
import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 * Our character class. Has the capacity to update itself by extrapolating based on the given float ellapsed time.
 * It uses many helper functions from CharMovement to assist in the updating of values.
 * 
 * @author blah
 */
public class Character{
    Node bodyModel;
    Node swordModel;
    
    SwordControl swordControl;
    BodyControl bodyControl;
    CharacterControl charControl;
    
    Vector3f upperArmAngles;
    Vector3f upperArmVels;
    Vector3f position;
    Vector3f velocity;
    
    Vector3f upperArmDeflectVels;
    
    float charAngle;
    float turnVel;
    float elbowWristAngle;
    float elbowWristVel;
    
    Long playerID;
    
    public Character(Long playerID,BulletAppState bulletAppState, AssetManager assetManager){
        this.playerID=playerID;
        
        bodyModel=(Node)assetManager.loadModel("Models/Female.mesh.j3o");
        bodyModel.scale(1.0f, 1.0f, 1.0f);
        bodyModel.rotate(0.0f, FastMath.HALF_PI, 0.0f);
        bodyModel.setLocalTranslation(0.0f, 20.0f, 0.0f);
        
        swordModel=(Node)assetManager.loadModel("Models/sword.mesh.j3o");
        swordModel.scale(1.0f, 1.0f, 1.0f);
        swordModel.setName("sword");
        
        bodyModel.attachChild(swordModel);
        
        upperArmDeflectVels=new Vector3f();
        upperArmAngles=new Vector3f();
        upperArmVels=new Vector3f();
        position=new Vector3f();
        velocity=new Vector3f();
        charAngle=0f;
        turnVel=0f;
        elbowWristAngle=CharMovement.Constraints.lRotMin;
        elbowWristVel=0f;
        
        applyPhysics(bulletAppState);
    }
    
    public void update(float tpf,boolean serverChar){
        upperArmAngles=CharMovement.extrapolateUpperArmAngles(upperArmAngles, upperArmVels, upperArmDeflectVels, tpf);
        elbowWristAngle=CharMovement.extrapolateLowerArmAngles(elbowWristAngle, elbowWristVel, tpf);
        charAngle=CharMovement.extrapolateCharTurn(charAngle, turnVel, tpf);
        CharMovement.setUpperArmTransform(upperArmAngles, bodyModel);
        CharMovement.setLowerArmTransform(elbowWristAngle, bodyModel);
        
        float xDir, zDir;
        zDir = FastMath.cos(charAngle);
        xDir = FastMath.sin(charAngle);
        Vector3f viewDirection = new Vector3f(xDir, 0, zDir);
        
        charControl.setViewDirection(viewDirection);
        Vector3f forward, up, left;
        float xVel, zVel;
        xVel = velocity.x;
        zVel = velocity.z;
        forward = new Vector3f(viewDirection);
        up = new Vector3f(0, 1, 0);
        left = up.cross(forward);
        
        Vector3f newVelocity = left.mult(xVel).add(forward.mult(zVel));
        
        if(serverChar){
            position = charControl.getPhysicsLocation();
        }
        else {
            Vector3f extrapolatedPosition, currentPosition;
            
            currentPosition = bodyModel.getLocalTranslation();
            extrapolatedPosition = position;
            Vector3f diffVect = new Vector3f(extrapolatedPosition.x - currentPosition.x, 0, extrapolatedPosition.z - currentPosition.z);
            float correctiveConstant = 0.2f;
            Vector3f correctiveVelocity = new Vector3f(diffVect.x * correctiveConstant, 0, diffVect.z * correctiveConstant);
      
            newVelocity.addLocal(correctiveVelocity);
        }
        
        charControl.setWalkDirection(newVelocity);
    }
    
    private void applyPhysics(BulletAppState bulletAppState){
        CapsuleCollisionShape capsule = new CapsuleCollisionShape(1f, 4.5f);

        // create collision shape for sword
        Bone hand = bodyModel.getControl(AnimControl.class).getSkeleton().getBone("swordHand");
        Matrix3f rotation = hand.getModelSpaceRotation().toRotationMatrix();
        Vector3f position = hand.getModelSpacePosition();

        swordModel.setLocalRotation(rotation);
        swordModel.setLocalTranslation(position);


        Vector3f shiftPosition = rotation.mult(new Vector3f(0f, 0f, 2.5f));
        CompoundCollisionShape cShape = new CompoundCollisionShape();
        Vector3f boxSize = new Vector3f(.1f, .1f, 2.25f);
        cShape.addChildShape(new BoxCollisionShape(boxSize), position, rotation);
        CollisionShapeFactory.shiftCompoundShapeContents(cShape, shiftPosition);

        // create control for sword
        swordControl = new SwordControl(cShape, playerID);
        swordControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
        swordControl.removeCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
        swordControl.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_02);

        // create control for body
        bodyControl = new BodyControl(capsule, playerID);
        bodyControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
        bodyControl.removeCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
        bodyControl.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_02);

        // create control for character
        charControl = new CharacterControl(capsule, 0.01f);

        swordModel.addControl(swordControl);
        bodyModel.addControl(bodyControl);
        bodyModel.addControl(charControl);

        bulletAppState.getPhysicsSpace().add(swordControl);
        bulletAppState.getPhysicsSpace().add(bodyControl);
        bulletAppState.getPhysicsSpace().add(charControl);
    } 
}