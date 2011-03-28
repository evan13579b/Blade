/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.math.FastMath;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

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

            CharacterControl charControl = new CharacterControl(capsule, 0.01f);

            model.addControl(charControl);
            model.setName(Long.toString(playerID));

            bulletAppState.getPhysicsSpace().add(ghost);
            bulletAppState.getPhysicsSpace().add(charControl);
        }

        
        return model;
    }

    private static Mesh findMesh(Spatial spatial) {
        if (spatial instanceof Node) {
            Node node = (Node) spatial;
            for (int i = 0; i < node.getQuantity(); i++) {
                Spatial child = node.getChild(i);
                Mesh result = findMesh(child);
                if (result != null) {
                    //System.out.println("FOUND MESH");
                    return result;
                }
            }
        } else if (spatial instanceof Geometry) {
            return ((Geometry) spatial).getMesh();
        }
        return null;
    }
}
