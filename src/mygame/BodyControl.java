/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.GhostControl;

/**
 *
 * @author Thor
 */
public class BodyControl extends GhostControl implements ControlID {
    private long id;
    
    public BodyControl(CollisionShape cShape, long id) {
        super(cShape);
        this.id = id;
    }
    
    public long getID () {
        return id;
    }
}
