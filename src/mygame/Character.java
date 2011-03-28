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
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.collision.shapes.GImpactCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @author blah
 */
public class Character{
    public static CompoundCollisionShape collisionShape1;
    public static CompoundCollisionShape collisionShape2;
    public static CompoundCollisionShape collisionShape3;
    public static CompoundCollisionShape collisionShape4;
    public static CompoundCollisionShape collisionShape5;
    public static CompoundCollisionShape collisionShape6;
    public static CompoundCollisionShape collisionShape7;
    public static BoxCollisionShape leftArm = new BoxCollisionShape(new Vector3f(1f,1f,1f));
    public static BoxCollisionShape rightArm = new BoxCollisionShape(new Vector3f(1f,1f,1f));
    public static BoxCollisionShape body = new BoxCollisionShape(new Vector3f(1f,1f,1f));
    public static BoxCollisionShape sword = new BoxCollisionShape(new Vector3f(1f,1f,1f));
    public static BoxCollisionShape rLeg = new BoxCollisionShape(new Vector3f(1f,1f,1f));
    public static BoxCollisionShape lLeg = new BoxCollisionShape(new Vector3f(1f,1f,1f));
    public static BoxCollisionShape head = new BoxCollisionShape(new Vector3f(1f,1f,1f));
    public static RigidBodyControl controlLArm;
    public static RigidBodyControl controlRArm;
    public static RigidBodyControl controlBody;
    public static RigidBodyControl controlSword;
    public static RigidBodyControl controlLLeg;
    public static RigidBodyControl controlRLeg;
    public static RigidBodyControl controlHead;
    
    static public Node createCharacter(String assetPath,AssetManager assetManager,BulletAppState bulletAppState,boolean applyPhysics, long playerID){
        collisionShape1 = new CompoundCollisionShape();
        collisionShape2 = new CompoundCollisionShape();
        collisionShape3 = new CompoundCollisionShape();
        collisionShape4 = new CompoundCollisionShape();
        collisionShape5 = new CompoundCollisionShape();
        collisionShape6 = new CompoundCollisionShape();
        collisionShape7 = new CompoundCollisionShape();
        Node model=(Node)assetManager.loadModel(assetPath);
        model.scale(1.0f, 1.0f, 1.0f);
        model.rotate(0.0f, FastMath.HALF_PI, 0.0f);
        model.setLocalTranslation(0.0f, 100.0f, -10.0f);

        if (applyPhysics) {
            CapsuleCollisionShape capsule = new CapsuleCollisionShape(1.5f, 6f);
            collisionShape1.addChildShape(leftArm, new Vector3f(2f,0,0));
            collisionShape2.addChildShape(rightArm, new Vector3f(-2f,0,0));
            collisionShape3.addChildShape(body, new Vector3f(0,0,0));
            collisionShape4.addChildShape(sword, new Vector3f(-2f,0,2f));
            collisionShape5.addChildShape(rLeg, new Vector3f(-2f,2f,0f));
            collisionShape6.addChildShape(lLeg, new Vector3f(2f,2f,0f));
            collisionShape7.addChildShape(head, new Vector3f(0f,-2f,0f));
            //Mesh mesh = findMesh(model);
            
            //CollisionShape gimpact = new GImpactCollisionShape(mesh);

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

            CharacterControl charControl = new CharacterControl(capsule, 0.01f);
            charControl.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
            charControl.removeCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
            model.addControl(charControl);
            model.setName(Long.toString(playerID));

            bulletAppState.getPhysicsSpace().add(controlLArm);
            bulletAppState.getPhysicsSpace().add(controlRArm);
            bulletAppState.getPhysicsSpace().add(controlBody);
            bulletAppState.getPhysicsSpace().add(controlSword);
            bulletAppState.getPhysicsSpace().add(controlLLeg);
            bulletAppState.getPhysicsSpace().add(controlRLeg);
            bulletAppState.getPhysicsSpace().add(controlHead);
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
