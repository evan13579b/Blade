/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.ui;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.scene.Node;
import mygame.BladeClient;

/**
 *
 * @author blah
 */
public class HUD extends Node{
    private BladeClient bladeClient;
    private BitmapText lifeText;
    private boolean lifeDisplayEnabled=false;
    
    public boolean lifeDisplayEnalbed(){
        return lifeDisplayEnabled;
    }
    
    public HUD(BladeClient bladeClient){
        super();
        this.bladeClient=bladeClient;
    }
    
    public void enableLifeDisplay(){
        BitmapFont font=bladeClient.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        lifeText=new BitmapText(font,false);
        lifeText.setBox(new Rectangle(0,0,200,200));
        lifeText.setSize(font.getPreferredSize());
        lifeText.setLocalTranslation(0, bladeClient.getSettings().getHeight(), 0);
        this.attachChild(lifeText);
        lifeDisplayEnabled=true;
    }
    
    public void setLifeDisplayValue(float life){
        if(lifeDisplayEnabled)
            lifeText.setText("Life: "+life*100+"%");
    }
}
