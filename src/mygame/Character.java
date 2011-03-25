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
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
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

            /*
            BoundingVolume bv = new BoundingBox(Vector3f.ZERO, 0.25f, 0.25f, 0.25f);
            model.setModelBound(bv);
            model.updateModelBound();
            model.updateGeometricState();
             *
             */
            //CollisionShape box = CollisionShapeFactory.createBoxShape(model);
            /*
            CollisionShape box = new BoxCollisionShape(new Vector3f(2.0f, 6.0f, 2.0f));

            RigidBodyControl rigidControl = new RigidBodyControl(box, 0.01f);

            rigidControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
            rigidControl.removeCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
            rigidControl.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
            rigidControl.setKinematic(true);
            model.addControl(rigidControl);
             *
             */
            CharacterControl charControl = new CharacterControl(capsule, 0.01f);

            model.addControl(charControl);
            model.setName(Long.toString(playerID));

            //bulletAppState.getPhysicsSpace().add(rigidControl);
            bulletAppState.getPhysicsSpace().add(charControl);
        }

        
        return model;
    }   
}
