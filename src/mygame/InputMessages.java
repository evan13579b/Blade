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
    static final int ROTARMCC=1,ROTARMC=2,MOUSEMOVEMENT=3,STOPROTATETWIST=4,STOPMOUSEMOVEMENT=5;
    static public void registerInputClasses(){
        Serializer.registerClass(RotateArmCC.class);
        Serializer.registerClass(RotateArmC.class);
        Serializer.registerClass(StopRotateTwist.class);
        Serializer.registerClass(MouseMovement.class);
        Serializer.registerClass(StopMouseMovement.class);
    }

    static public void addInputMessageListeners(Object connector,MessageListener listener){

        if(connector instanceof Server){
            Server con=(Server)connector;
            con.addMessageListener(listener,RotateArmCC.class,RotateArmC.class,StopRotateTwist.class,
                    MouseMovement.class,StopMouseMovement.class);
        }
        else if(connector instanceof Client){
            Client con=(Client)connector;
            con.addMessageListener(listener,RotateArmCC.class,RotateArmC.class,StopRotateTwist.class,
                    MouseMovement.class,StopMouseMovement.class);
        }

    }
    
    @Serializable(id=ROTARMCC) static public class RotateArmCC extends Message{public RotateArmCC(){super(false);}}
    @Serializable(id=ROTARMC) static public class RotateArmC extends Message{public RotateArmC(){super(false);}}
    @Serializable(id=STOPROTATETWIST) static public class StopRotateTwist extends Message{ public StopRotateTwist(){super(false);}}
    @Serializable(id=STOPMOUSEMOVEMENT) static public class StopMouseMovement extends Message{ public StopMouseMovement(){super(false);}}
    @Serializable(id=MOUSEMOVEMENT)
    static public class MouseMovement extends Message{
        public float angle;
        public MouseMovement() {
            super(false);
        }
        public MouseMovement(float angle){
            super(false);
            this.angle=angle;
        }
    }
}
