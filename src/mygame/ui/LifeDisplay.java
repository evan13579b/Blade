/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.ui;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 * Encapsulates a life display.
 * 
 * @author blah
 */
public class LifeDisplay extends Node{
    private BitmapText lifeText;
    
    public LifeDisplay(BitmapFont font,boolean partOfHUD){
        super();
        lifeText=new BitmapText(font,false);
        lifeText.setBox(new Rectangle(0,0,200,200));
        lifeText.setSize(font.getPreferredSize());
        if(!partOfHUD){
            lifeText.scale(0.02f);
            lifeText.setLocalTranslation(-1,4.5f,0);
        }
        this.attachChild(lifeText);
    }
    
    public void setLifeDisplayValue(float life){
        lifeText.setText("Life: "+life*100+"%");
    }
}
