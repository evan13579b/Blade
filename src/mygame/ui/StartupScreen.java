/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.ui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.listbox.ListBoxControl;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author blah
 */
public class StartupScreen implements ScreenController {
    private Nifty ui;
    private Screen screen;
    private Map<String,String> ipAddressMap;
    
    public StartupScreen(Map<String,String> ipAddressMap){
        this.ipAddressMap=new HashMap(ipAddressMap);
    }

    public void bind(Nifty ui, Screen screen) {
        this.ui=ui;
        this.screen=screen;
    }

    public void onStartScreen() {
        ListBox listBox;
        listBox=screen.findControl("addressList",ListBoxControl.class);
        List<Entry> list=new ArrayList();
        for(Entry<String,String> entry:ipAddressMap.entrySet()){
            list.add(entry);
        }
        listBox.addAllItems(list);
    }

    public void onEndScreen() {
    }
    
    public void connect() {
        System.out.println("Connect");
    }
}
