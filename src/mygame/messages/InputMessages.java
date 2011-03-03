/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mygame.messages;

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
    static final int ROTUARMCC=1,ROTUARMC=2,MOUSEMOVEMENT=3,STOPROTATETWIST=4,STOPMOUSEMOVEMENT=5,LARMUP=6,LARMDOWN=7,STOPLARM=8;
    static public void registerInputClasses(){
        Serializer.registerClass(RotateUArmCC.class);
        Serializer.registerClass(RotateUArmC.class);
        Serializer.registerClass(StopRotateTwist.class);
        Serializer.registerClass(MouseMovement.class);
        Serializer.registerClass(StopMouseMovement.class);
        Serializer.registerClass(LArmUp.class);
        Serializer.registerClass(LArmDown.class);
        Serializer.registerClass(StopLArm.class);
    }

    static public void addInputMessageListeners(Object connector,MessageListener listener){

        if(connector instanceof Server){
            Server con=(Server)connector;
            con.addMessageListener(listener,RotateUArmCC.class,RotateUArmC.class,StopRotateTwist.class,
                    MouseMovement.class,StopMouseMovement.class,LArmUp.class,LArmDown.class,StopLArm.class);
        }
        else if(connector instanceof Client){
            Client con=(Client)connector;
            con.addMessageListener(listener,RotateUArmCC.class,RotateUArmC.class,StopRotateTwist.class,
                    MouseMovement.class,StopMouseMovement.class,LArmUp.class,LArmDown.class,StopLArm.class);
        }

    }
    
    @Serializable(id=ROTUARMCC) static public class RotateUArmCC extends Message{public RotateUArmCC(){super(false);}}
    @Serializable(id=ROTUARMC) static public class RotateUArmC extends Message{public RotateUArmC(){super(false);}}
    @Serializable(id=LARMUP) static public class LArmUp extends Message{public LArmUp(){super(false);}}
    @Serializable(id=LARMDOWN) static public class LArmDown extends Message{public LArmDown(){super(false);}}
    @Serializable(id=STOPLARM) static public class StopLArm extends Message{public StopLArm(){super(false);}}
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
