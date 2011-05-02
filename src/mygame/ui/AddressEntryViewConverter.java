/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.ui;

import de.lessvoid.nifty.controls.ListBox.ListBoxViewConverter;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import java.util.Map.Entry;

/**
 * 
 * 
 * @author blah
 */
public class AddressEntryViewConverter implements ListBoxViewConverter{

    public void display(Element elmnt, Object t) {
        Entry<String,String> mapEntry=(Entry<String,String>)t;
        elmnt.getRenderer(TextRenderer.class).setText(mapEntry.getKey()+" ("+mapEntry.getValue()+")");
    }

    public int getWidth(Element elmnt, Object t) {
        Entry<String,String> mapEntry=(Entry<String,String>)t;
        String viewString=mapEntry.getKey()+" ("+mapEntry.getValue()+")";
        return elmnt.getRenderer(TextRenderer.class).getFont().getWidth(viewString);
    }
    
}
