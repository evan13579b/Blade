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
    static final int ROTUARMCC=1,ROTUARMC=2,MOUSEMOVEMENT=3,STOPROTATETWIST=4,STOPMOUSEMOVEMENT=5,LARMUP=6,LARMDOWN=7,
            STOPLARM=8,TURNCHARLEFT=9,TURNCHARRIGHT=10,MOVECHARFORWARD=11,MOVECHARBACKWORD=12,MOVECHARLEFT=13,MOVECHARRIGHT=14,
            STOPFORWARDMOVE=15,STOPLEFTRIGHTMOVE=16,STOPCHARTURN=17;
    static public void registerInputClasses(){
        Serializer.registerClass(RotateUArmCC.class);
        Serializer.registerClass(RotateUArmC.class);
        Serializer.registerClass(StopRotateTwist.class);
        Serializer.registerClass(MouseMovement.class);
        Serializer.registerClass(StopMouseMovement.class);
        Serializer.registerClass(LArmUp.class);
        Serializer.registerClass(LArmDown.class);
        Serializer.registerClass(StopLArm.class);
        Serializer.registerClass(MoveCharBackword.class);
        Serializer.registerClass(MoveCharForward.class);
        Serializer.registerClass(MoveCharLeft.class);
        Serializer.registerClass(MoveCharRight.class);
        Serializer.registerClass(TurnCharLeft.class);
        Serializer.registerClass(TurnCharRight.class);
        Serializer.registerClass(StopCharTurn.class);
        Serializer.registerClass(StopForwardMove.class);
        Serializer.registerClass(StopLeftRightMove.class);

    }

    static public void addInputMessageListeners(Object connector,MessageListener listener){

        if(connector instanceof Server){
            Server con=(Server)connector;
            con.addMessageListener(listener,RotateUArmCC.class,RotateUArmC.class,StopRotateTwist.class,
                    MouseMovement.class,StopMouseMovement.class,LArmUp.class,LArmDown.class,StopLArm.class,
                    MoveCharForward.class,MoveCharBackword.class,MoveCharLeft.class,MoveCharRight.class,
                    TurnCharLeft.class,TurnCharRight.class,StopCharTurn.class,StopForwardMove.class,
                    StopLeftRightMove.class);
        }
        else if(connector instanceof Client){
            Client con=(Client)connector;
            con.addMessageListener(listener,RotateUArmCC.class,RotateUArmC.class,StopRotateTwist.class,
                    MouseMovement.class,StopMouseMovement.class,LArmUp.class,LArmDown.class,StopLArm.class,
                    MoveCharForward.class,MoveCharBackword.class,MoveCharLeft.class,MoveCharRight.class,
                    TurnCharLeft.class,TurnCharRight.class,StopCharTurn.class,StopForwardMove.class,
                    StopLeftRightMove.class);
        }

    }

    @Serializable(id=STOPLEFTRIGHTMOVE)
    static public class StopLeftRightMove extends Message implements HasID{
        public long playerID;
        public StopLeftRightMove(long playerID){
            super(false);
            this.playerID=playerID;
        }
        public long getID(){return playerID;}

        public StopLeftRightMove(){
            super(false);
        }
    }

    @Serializable(id=STOPCHARTURN)
    static public class StopCharTurn extends Message implements HasID{
        public long playerID;
        public StopCharTurn(long playerID){
            super(false);
            this.playerID=playerID;
        }
        public long getID(){return playerID;}

        public StopCharTurn(){
            super(false);
        }
    }

    @Serializable(id=STOPFORWARDMOVE)
    static public class StopForwardMove extends Message implements HasID{
        public long playerID;
        public StopForwardMove(long playerID){
            super(false);
            this.playerID=playerID;
        }
        public long getID(){return playerID;}

        public StopForwardMove(){
            super(false);
        }
    }

    @Serializable(id=MOVECHARBACKWORD)
    static public class MoveCharBackword extends Message implements HasID{
        public long playerID;
        public MoveCharBackword(long playerID){
            super(false);
            this.playerID=playerID;
        }
        public long getID(){return playerID;}

        public MoveCharBackword(){
            super(false);
        }
    }

    @Serializable(id=MOVECHARFORWARD)
    static public class MoveCharForward extends Message implements HasID{
        public long playerID;
        public MoveCharForward(long playerID){
            super(false);
            this.playerID=playerID;
        }
        public long getID(){return playerID;}

        public MoveCharForward(){
            super(false);
        }
    }

    @Serializable(id=MOVECHARRIGHT)
    static public class MoveCharRight extends Message implements HasID{
        public long playerID;
        public MoveCharRight(long playerID){
            super(false);
            this.playerID=playerID;
        }
        public long getID(){return playerID;}

        public MoveCharRight(){
            super(false);
        }
    }

    @Serializable(id=MOVECHARLEFT)
    static public class MoveCharLeft extends Message implements HasID{
        public long playerID;
        public MoveCharLeft(long playerID){
            super(false);
            this.playerID=playerID;
        }
        public long getID(){return playerID;}

        public MoveCharLeft(){
            super(false);
        }
    }

    @Serializable(id=TURNCHARRIGHT)
    static public class TurnCharRight extends Message implements HasID{
        public long playerID;
        public TurnCharRight(long playerID){
            super(false);
            this.playerID=playerID;
        }
        public long getID(){return playerID;}

        public TurnCharRight(){
            super(false);
        }
    }

    @Serializable(id=TURNCHARLEFT)
    static public class TurnCharLeft extends Message implements HasID{
        public long playerID;
        public TurnCharLeft(long playerID){
            super(false);
            this.playerID=playerID;
        }
        public long getID(){return playerID;}

        public TurnCharLeft(){
            super(false);
        }
    }

    @Serializable(id=ROTUARMCC)
    static public class RotateUArmCC extends Message implements HasID{
        public long playerID;
        public RotateUArmCC(long playerID){
            super(false);
            this.playerID=playerID;
        }
        public long getID(){return playerID;}

        public RotateUArmCC(){
            super(false);
        }
    }
    
    @Serializable(id=ROTUARMC)
    static public class RotateUArmC extends Message implements HasID{
        public long playerID;
        public RotateUArmC(long playerID){
            super(false);
            this.playerID=playerID;
        }
        public long getID(){return playerID;}

        public RotateUArmC(){
            super(false);
        }
    }

    @Serializable(id=LARMUP)
    static public class LArmUp extends Message implements HasID{
        public long playerID;
        public LArmUp(long playerID){
            super(false);
            this.playerID=playerID;
        }
        public long getID(){return playerID;}

        public LArmUp(){
            super(false);
        }
    }

    @Serializable(id=LARMDOWN)
    static public class LArmDown extends Message implements HasID{
        public long playerID;
        public LArmDown(long playerID){
            super(false);
            this.playerID=playerID;
        }
        public long getID(){return playerID;}

        public LArmDown(){
            super(false);
        }
    }

    @Serializable(id=STOPLARM)
    static public class StopLArm extends Message implements HasID{
        public long playerID;
        public StopLArm(long playerID){
            super(false);
            this.playerID=playerID;
        }
        public long getID(){return playerID;}

        public StopLArm(){
            super(false);
        }
    }

    @Serializable(id=STOPROTATETWIST)
    static public class StopRotateTwist extends Message implements HasID{
        public long playerID;
        public StopRotateTwist(long playerID){
            super(false);
            this.playerID=playerID;
        }
        public long getID(){return playerID;}

        public StopRotateTwist(){
            super(false);
        }
    }

    @Serializable(id=STOPMOUSEMOVEMENT)
    static public class StopMouseMovement extends Message implements HasID{
        public long playerID;
        public StopMouseMovement(long playerID){
            super(false);
            this.playerID=playerID;
        }

        public StopMouseMovement(){
            super(false);
        }
        public long getID(){return playerID;}
    }

    @Serializable(id=MOUSEMOVEMENT)
    static public class MouseMovement extends Message implements HasID{
        public long playerID;
        public float angle;

        public MouseMovement() {
            super(false);
            this.playerID=0;
        }
        
        public MouseMovement(float angle,long playerID){
            super(false);
            this.angle=angle;
            this.playerID=playerID;
        }

        public long getID(){return playerID;}
    }
}
