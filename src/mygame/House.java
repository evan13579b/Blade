/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.scene.Node;

/**
 *
 * @author blah
 */
public class House{
    static RigidBodyControl box_phy;
    static public Node createHouse(String assetPath,AssetManager assetManager,BulletAppState bulletAppState,boolean applyPhysics){

        Node model=(Node)assetManager.loadModel(assetPath);
        model.scale(1.0f, 1.0f, 1.0f);
        //model.rotate(0.0f, FastMath.HALF_PI, 0.0f);
        model.setLocalTranslation(5.0f, -11.0f, -10.0f);

        if (applyPhysics) {
           box_phy = new RigidBodyControl(0.0f);
           model.addControl(box_phy);
           bulletAppState.getPhysicsSpace().add(box_phy);
            
        }

        return model;
    }
}
