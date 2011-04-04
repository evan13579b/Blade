/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
<<<<<<< HEAD:src/mygame/Character.java
import com.jme3.bullet.collision.shapes.CollisionShape;
<<<<<<< HEAD:src/mygame/Character.java
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.GImpactCollisionShape;
=======
>>>>>>> 18f1277b0549f77dedce6de8c02ba6b7c7b83568:src/mygame/Character.java
=======
>>>>>>> 982d0203d2766f275bb9ec646967eba7b1787615:src/mygame/Character.java
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author blah
 */
public class Character{
    static public Node createCharacter(String assetPath,AssetManager assetManager,BulletAppState bulletAppState,boolean applyPhysics, long playerID){
        
        Node model=(Node)assetManager.loadModel(assetPath);
        model.scale(1.0f, 1.0f, 1.0f);
        model.rotate(0.0f, FastMath.HALF_PI, 0.0f);
        model.setLocalTranslation(0.0f, 100.0f, 0.0f);

        if (applyPhysics) {
            CapsuleCollisionShape capsule = new CapsuleCollisionShape(1.5f, 6f);
<<<<<<< HEAD:src/mygame/Character.java
            collisionShape1.addChildShape(leftArm, new Vector3f(2f,0,0));
            collisionShape2.addChildShape(rightArm, new Vector3f(-2f,0,0));
            collisionShape3.addChildShape(body, new Vector3f(0,0,0));
            collisionShape4.addChildShape(sword, new Vector3f(-2f,0,2f));
            collisionShape5.addChildShape(rLeg, new Vector3f(-2f,2f,0f));
            collisionShape6.addChildShape(lLeg, new Vector3f(2f,2f,0f));
            collisionShape7.addChildShape(head, new Vector3f(0f,-2f,0f));
            //Mesh mesh = findMesh(model);
            
            //CollisionShape gimpact = new GImpactCollisionShape(mesh);

<<<<<<< HEAD:src/mygame/Character.java
            controlLArm = new RigidBodyControl(collisionShape1, 0.01f);
            controlLArm.setKinematic(true);
            model.addControl(controlLArm);
            controlRArm = new RigidBodyControl(collisionShape2, 0.01f);
            controlRArm.setKinematic(true);
            model.addControl(controlRArm);
            controlBody = new RigidBodyControl(collisionShape3, 0.01f);
            controlBody.setKinematic(true);
            model.addControl(controlBody);
            controlSword = new RigidBodyControl(collisionShape4, 0.01f);
            controlSword.setKinematic(true);
            model.addControl(controlSword);
            controlLLeg = new RigidBodyControl(collisionShape5, 0.01f);
            controlLLeg.setKinematic(true);
            model.addControl(controlLLeg);
            controlRLeg = new RigidBodyControl(collisionShape6, 0.01f);
            controlRLeg.setKinematic(true);
            model.addControl(controlRLeg);
            controlHead = new RigidBodyControl(collisionShape7, 0.01f);
            controlHead.setKinematic(true);
            model.addControl(controlHead);
=======
            /*
            Mesh mesh = findMesh(model);
            CollisionShape gimpact = new GImpactCollisionShape(mesh);
            RigidBodyControl rigidControl = new RigidBodyControl(gimpact, 0.01f);
            rigidControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
            rigidControl.removeCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
            rigidControl.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
            rigidControl.setKinematic(true);
            model.addControl(rigidControl);
            */
            //bulletAppState.getPhysicsSpace().add(rigidControl);

            GhostControl ghost = new GhostControl(capsule);
            ghost.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
            ghost.removeCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
            ghost.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
            model.addControl(ghost);
>>>>>>> 18f1277b0549f77dedce6de8c02ba6b7c7b83568:src/mygame/Character.java
=======
>>>>>>> 982d0203d2766f275bb9ec646967eba7b1787615:src/mygame/Character.java

            
            CharacterControl charControl = new CharacterControl(capsule, 0.01f);

             model.addControl(charControl);
            model.setName(Long.toString(playerID));

<<<<<<< HEAD:src/mygame/Character.java
<<<<<<< HEAD:src/mygame/Character.java
            bulletAppState.getPhysicsSpace().add(controlLArm);
            bulletAppState.getPhysicsSpace().add(controlRArm);
            bulletAppState.getPhysicsSpace().add(controlBody);
            bulletAppState.getPhysicsSpace().add(controlSword);
            bulletAppState.getPhysicsSpace().add(controlLLeg);
            bulletAppState.getPhysicsSpace().add(controlRLeg);
            bulletAppState.getPhysicsSpace().add(controlHead);
=======
            bulletAppState.getPhysicsSpace().add(ghost);
>>>>>>> 18f1277b0549f77dedce6de8c02ba6b7c7b83568:src/mygame/Character.java
=======
            //bulletAppState.getPhysicsSpace().add(rigidControl);
>>>>>>> 982d0203d2766f275bb9ec646967eba7b1787615:src/mygame/Character.java
            bulletAppState.getPhysicsSpace().add(charControl);
        }

        
        return model;
    }   
}
