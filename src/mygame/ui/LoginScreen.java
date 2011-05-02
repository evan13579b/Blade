/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.ui;

import com.jme3.network.connection.Client;
import com.jme3.network.events.ConnectionListener;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.label.LabelControl;
import de.lessvoid.nifty.controls.listbox.ListBoxControl;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import mygame.BladeClient;

/**
 * Creates teh login screen for the blade client.
 * 
 * @author blah
 */
public class LoginScreen implements ScreenController, ConnectionListener {    
    private Nifty ui;
    private Screen screen;
    private Map<String,String> ipAddressMap;
    private ListBox addressListBox;
    private Label statusDisplay;
    private Client client;
    private int portNum;
    private BladeClient bladeClient;
    
    public LoginScreen(Map<String,String> ipAddressMap,Client client,int portNum,BladeClient bladeClient){
        this.ipAddressMap=new HashMap(ipAddressMap);
        this.client=client;
        this.portNum=portNum;
        this.bladeClient=bladeClient;
    }

    public void bind(Nifty ui, Screen screen) {
        this.ui=ui;
        this.screen=screen;
        this.addressListBox=screen.findControl("addressList",ListBoxControl.class);
        this.statusDisplay=screen.findControl("statusDisplay",LabelControl.class);
    }

    public void onStartScreen() {
        List<Entry> list=new ArrayList();
        for(Entry<String,String> entry:ipAddressMap.entrySet()){
            list.add(entry);
        }
        addressListBox.addAllItems(list);
        screen.findElementByName("playButton").disable();
    }

    public void onEndScreen() {
        
        
    }
    
    public void connect() {
        Entry<String,String> entry=(Entry<String,String>)addressListBox.getSelection().get(0);
        statusDisplay.setText("Connecting...");
        try {
            client=new Client(entry.getValue(),portNum,portNum);
            client.start();
            client.addConnectionListener(this);
            
        } catch (IOException ex) {
            Logger.getLogger(LoginScreen.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void clientConnected(Client client) {
        statusDisplay.setText("Connected!");
        screen.findElementByName("connectButton").disable();
        screen.findElementByName("playButton").enable();
    }

    public void clientDisconnected(Client client) {
        statusDisplay.setText("Connection Refused!");
    }

    public void playPressed(){
        client.removeConnectionListener(this);
        screen.endScreen(null);
        bladeClient.isReadyToStart();
        bladeClient.setClient(client);
        ui.exit();
    }
}
