/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mygame;

import com.jme3.network.connection.Client;
import com.jme3.network.connection.Server;
import com.jme3.network.events.MessageListener;
import com.jme3.network.message.Message;
import com.jme3.network.serializing.Serializable;
import com.jme3.network.serializing.Serializer;

/**
 *
 * @author blah
 */
public class InputMessages{
    static final int ROTARMCC=1,ROTARMC=2,ROTARMUP=3,ROTARMDOWN=4,ROTARMLEFT=5,ROTARMRIGHT=6;
    static public void registerInputClasses(){
        Serializer.registerClass(RotateArmCC.class);
        Serializer.registerClass(RotateArmC.class);
        Serializer.registerClass(RotateArmUp.class);
        Serializer.registerClass(RotateArmDown.class);
        Serializer.registerClass(RotateArmLeft.class);
        Serializer.registerClass(RotateArmRight.class);
    }

    static public void addInputMessageListeners(Object connector,MessageListener listener){

        if(connector instanceof Server){
            Server con=(Server)connector;
            con.addMessageListener(listener,RotateArmCC.class,RotateArmC.class,RotateArmUp.class,RotateArmUp.class,RotateArmLeft.class,RotateArmRight.class);
        }
        else if(connector instanceof Client){
            Client con=(Client)connector;
            con.addMessageListener(listener,RotateArmCC.class,RotateArmC.class,RotateArmUp.class,RotateArmUp.class,RotateArmLeft.class,RotateArmRight.class);
        }

    }
    
    @Serializable(id=ROTARMCC) static public class RotateArmCC extends Message{}
    @Serializable(id=ROTARMC) static public class RotateArmC extends Message{}
    @Serializable(id=ROTARMUP) static public class RotateArmUp extends Message{}
    @Serializable(id=ROTARMDOWN) static public class RotateArmDown extends Message{}
    @Serializable(id=ROTARMLEFT) static public class RotateArmLeft extends Message{}
    @Serializable(id=ROTARMRIGHT) static public class RotateArmRight extends Message{}
}
