/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mygame;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Bone;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
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
        model.setName(Long.toString(playerID));
        
        if (applyPhysics) {
            CapsuleCollisionShape capsule = new CapsuleCollisionShape(1.5f, 6f);

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
            
            // create collision shape for sword
            Bone hand = model.getControl(AnimControl.class).getSkeleton().getBone("HandR");
            Matrix3f rotation = hand.getModelSpaceRotation().toRotationMatrix();
            Vector3f position = hand.getModelSpacePosition();
            Vector3f shiftPosition = rotation.mult(new Vector3f(0f, .5f, 2.5f));
            CompoundCollisionShape cShape = new CompoundCollisionShape();
            Vector3f boxSize = new Vector3f(.1f, .1f, 2.25f);
            cShape.addChildShape(new BoxCollisionShape(boxSize), position, rotation);
            CollisionShapeFactory.shiftCompoundShapeContents(cShape, shiftPosition);
            
            // create control for sword
            SwordControl sword = new SwordControl(cShape, playerID);
            sword.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
            sword.removeCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
            sword.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
            
            // create control for body
            BodyControl body = new BodyControl(capsule, playerID);
            body.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
            body.removeCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
            body.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
            
            // create control for character
            CharacterControl charControl = new CharacterControl(capsule, 0.01f);
            
            model.addControl(sword); // control 0
            model.addControl(body);// control 1
            model.addControl(charControl); // control 2
            
            bulletAppState.getPhysicsSpace().add(sword);
            bulletAppState.getPhysicsSpace().add(body);
            bulletAppState.getPhysicsSpace().add(charControl);
        }
        return model;
    }       
}