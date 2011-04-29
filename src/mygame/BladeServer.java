/*
 * The Init terrain and init materials functions were both taken from the JME example code
 * and modified. The rest of the code is almost entirely written from scratch.
 */

/*
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package mygame;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Bone;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mygame.messages.InputMessages;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.connection.Client;
import com.jme3.network.connection.Server;
import com.jme3.network.events.ConnectionListener;
import com.jme3.network.events.MessageListener;
import com.jme3.network.message.Message;
import com.jme3.network.sync.ServerSyncService;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import mygame.messages.CharCreationMessage;
import mygame.messages.CharDestructionMessage;
import mygame.messages.CharStatusMessage;
import mygame.messages.ClientReadyMessage;
import mygame.messages.HasID;
import mygame.messages.SwordBodyCollisionMessage;
import mygame.messages.SwordSwordCollisionMessage;

public class BladeServer extends BladeBase implements MessageListener,ConnectionListener{
    ConcurrentHashMap<Long,Client> clientMap=new ConcurrentHashMap();
    ConcurrentHashMap<Client,Long> playerIDMap=new ConcurrentHashMap();

    ConcurrentHashMap<Long, Deque<Vector3f[]>> prevStates = new ConcurrentHashMap();
   
    ConcurrentHashMap<Long, Float> charLifeMap = new ConcurrentHashMap();
    ConcurrentHashMap<Long, Long> timeOfLastCollisionMap = new ConcurrentHashMap();

    private final long timeBetweenSyncs=10;
    private final int numPrevStates = 9;
    private final int goBackNumStates = 1;

    private Queue<Callable> actions = new ConcurrentLinkedQueue<Callable>();
    private long timeOfLastSync=0;
    
    private long currentPlayerID=0;
    private static BladeServer app;
    private boolean updateNow;
    float airTime = 0;

    Server server;
    ServerSyncService serverSyncService;

    public static void main(String[] args) {
        app = new BladeServer();
        AppSettings appSettings=new AppSettings(true);
        appSettings.setFrameRate(30);
        app.setPauseOnLostFocus(false);
        app.setSettings(appSettings);
        //app.start();
        app.start(JmeContext.Type.Headless);
    }

    @Override
    public void simpleInitApp() {
        super.simpleInitApp();
        
       
        try {
            server = new Server(BladeMain.port,BladeMain.port);
            server.start();
        }
        catch(Exception e){
            e.printStackTrace();
        }

        InputMessages.addInputMessageListeners(server, this);
        server.addConnectionListener(this);
        server.addMessageListener(this,SwordSwordCollisionMessage.class,SwordBodyCollisionMessage.class,CharCreationMessage.class,CharDestructionMessage.class,CharStatusMessage.class,ClientReadyMessage.class);
        
        flyCam.setMoveSpeed(50);

        


        flyCam.setEnabled(true);
        this.getStateManager().getState(BulletAppState.class).getPhysicsSpace().enableDebug(this.getAssetManager());
        
        PhysicsCollisionListener physListener = new PhysicsCollisionListener() {

            public void collision(PhysicsCollisionEvent event) {

                final PhysicsCollisionObject a = event.getObjectA();
                final PhysicsCollisionObject b = event.getObjectB();
                final Vector3f collisionCoordinate = event.getPositionWorldOnA();
                if ((a != null && b != null && a instanceof ControlID && b instanceof ControlID
                        && ((ControlID) a).getID() != ((ControlID) b).getID())) {

                    System.out.println("Collision!");

                    actions.add(new Callable() {

                        public Object call() throws Exception {

                            long hurtPlayerID;
                            if ((a instanceof SwordControl) && (b instanceof BodyControl)) {
                                hurtPlayerID = ((ControlID) b).getID();
                                charLifeMap.put(hurtPlayerID, charLifeMap.get(hurtPlayerID) * 0.999f);
                            } else if ((b instanceof SwordControl) && (a instanceof BodyControl)) {
                                hurtPlayerID = ((ControlID) a).getID();
                                charLifeMap.put(hurtPlayerID, charLifeMap.get(hurtPlayerID) * 0.999f);
                            }

                            // we limit the amount of collision messages per second to two
                            // and we do this based on the id of the lowest ID whose sword collided.
                            // this is to make sure that we don't get two rapid collisions
                            // when both swords collided with each
                            long effectPlayerID = -1;
                            boolean swordSword = false;
                            if ((a instanceof SwordControl)) {
                                effectPlayerID = ((ControlID) a).getID();
                                if (b instanceof SwordControl) {
                                    swordSword = true;
                                    if (effectPlayerID > ((ControlID) b).getID()) {
                                        effectPlayerID = ((ControlID) b).getID();
                                    }
                                }
                            } else if ((b instanceof SwordControl)) {
                                effectPlayerID = ((ControlID) b).getID();
                            }

                            if (effectPlayerID != -1) {
                                long currentTime = System.currentTimeMillis();


                                if (currentTime - timeOfLastCollisionMap.get(effectPlayerID) > 500) {
                                    createEffect(collisionCoordinate);
                                    Message message;
                                    if (swordSword) {
                                        message = new SwordSwordCollisionMessage(new Vector3f(collisionCoordinate));
                                    } else {
                                        message = new SwordBodyCollisionMessage(new Vector3f(collisionCoordinate));
                                    }

                                    for (Iterator<Long> playerIterator = playerSet.iterator(); playerIterator.hasNext();) {
                                        long destPlayerID = playerIterator.next();
                                        clientMap.get(destPlayerID).send(message);
                                    }
                                    timeOfLastCollisionMap.put(effectPlayerID, currentTime);
                                }
                            }

                            long playerID1 = Long.valueOf(((ControlID) a).getID());
                            long playerID2 = Long.valueOf(((ControlID) b).getID());

                            Deque player1Deque = prevStates.get(playerID1);
                            Deque player2Deque = prevStates.get(playerID2);

                            // go back some number of states
                            for (int i = 1; i < goBackNumStates; i++) {
                                player1Deque.pollLast();
                                player2Deque.pollLast();
                            }

                            Vector3f[] p1State = (Vector3f[]) player1Deque.pollLast();
                            Vector3f[] p2State = (Vector3f[]) player2Deque.pollLast();

                            // replace the removed states

                            Vector3f[] p1Next = (Vector3f[]) player1Deque.pollLast();
                            Vector3f[] p2Next = (Vector3f[]) player2Deque.pollLast();

                            for (int i = 0; i < goBackNumStates; i++) {
                                player1Deque.offerLast(p1Next);
                                player2Deque.offerLast(p2Next);
                            }

                            // reposition the character as recorded in the previous state
                            upperArmAnglesMap.put(playerID1, p1State[0]);
                            upperArmAnglesMap.put(playerID2, p2State[0]);

                            elbowWristAngleMap.put(playerID1, p1State[1].getX());
                            elbowWristAngleMap.put(playerID2, p2State[1].getX());

                            charAngleMap.put(playerID1, p1State[1].getY());
                            charAngleMap.put(playerID2, p2State[1].getY());

                            charPositionMap.put(playerID1, p1State[2]);
                            charPositionMap.put(playerID2, p2State[2]);

                            modelMap.get(playerID1).getControl(CharacterControl.class).setPhysicsLocation(charPositionMap.get(playerID1));
                            modelMap.get(playerID2).getControl(CharacterControl.class).setPhysicsLocation(charPositionMap.get(playerID2));
                            //updateCharacters(timer.getTimePerFrame());
                            return null;
                        }
                    });
                }
            }
        };
           
        PhysicsTickListener physTickListener = new PhysicsTickListener() {
            public void prePhysicsTick(PhysicsSpace space, float f) {
                Callable action;
                
                while ((action = actions.poll()) != null) {
                    try {
                        action.call();
                    } catch (Exception ex) {
                        Logger.getLogger(BladeServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }                
                
                updateCharacters(timer.getTimePerFrame());
                rootNode.updateGeometricState();
                //System.out.println("tpf: " + timer.getTimePerFrame() + " fps: " + timer.getFrameRate());
            }

            public void physicsTick(PhysicsSpace space, float f) {
                updateClients();
            }
        };
        
        //bulletAppState.getPhysicsSpace().setAccuracy((float)(1.0/120.0));
        System.out.println("Accuracy: " + bulletAppState.getPhysicsSpace().getAccuracy());
        
        this.getStateManager().getState(BulletAppState.class).getPhysicsSpace().addCollisionListener(physListener);
        this.getStateManager().getState(BulletAppState.class).getPhysicsSpace().addTickListener(physTickListener);
        updateNow = false;
    }

    @Override
    public void simpleUpdate(float tpf){
        //updateCharacters(tpf);
    }

    public void updateCharacters(float tpf) {
        for(Iterator<Long> playerIterator=playerSet.iterator(); playerIterator.hasNext();){
            long playerID = playerIterator.next();
            Vector3f upperArmAngles = upperArmAnglesMap.get(playerID);
            
            Vector3f[] prevState = new Vector3f[3];
            
            prevState[0] = upperArmAnglesMap.get(playerID);
            upperArmAnglesMap.put(playerID, CharMovement.extrapolateUpperArmAngles(upperArmAngles,
                    upperArmVelsMap.get(playerID), tpf));

            prevState[1] = new Vector3f(elbowWristAngleMap.get(playerID), 0f, 0f);
            elbowWristAngleMap.put(playerID, CharMovement.extrapolateLowerArmAngles(elbowWristAngleMap.get(playerID),
                    elbowWristVelMap.get(playerID), tpf));

            prevState[1].setY(charAngleMap.get(playerID));
            charAngleMap.put(playerID, CharMovement.extrapolateCharTurn(charAngleMap.get(playerID),
                    charTurnVelMap.get(playerID), tpf));

            CharacterControl control=modelMap.get(playerID).getControl(CharacterControl.class);
            float xDir,zDir;
            zDir=FastMath.cos(charAngleMap.get(playerID));
            xDir=FastMath.sin(charAngleMap.get(playerID));
            Vector3f viewDirection=new Vector3f(xDir,0,zDir);
            control.setViewDirection(viewDirection);

            Vector3f forward,up,left;
            float xVel,zVel;
            xVel=charVelocityMap.get(playerID).x;
            zVel=charVelocityMap.get(playerID).z;
            forward=new Vector3f(viewDirection);
            up=new Vector3f(0,1,0);
            left=up.cross(forward);

            control.setWalkDirection(left.mult(xVel).add(forward.mult(zVel)));
            
            prevState[2] = charPositionMap.get(playerID).clone();
            charPositionMap.get(playerID).set(modelMap.get(playerID).getControl(CharacterControl.class).getPhysicsLocation()); // getLocalTranslation
            
            CharMovement.setUpperArmTransform(upperArmAnglesMap.get(playerID), modelMap.get(playerID));
            CharMovement.setLowerArmTransform(elbowWristAngleMap.get(playerID), modelMap.get(playerID));

            // Adjust the sword collision shape in accordance with arm movement.
            // first, get rotation and position of hand
            Bone hand = modelMap.get(playerID).getControl(AnimControl.class).getSkeleton().getBone("swordHand");
            Matrix3f rotation = hand.getModelSpaceRotation().toRotationMatrix();
            Vector3f position = hand.getModelSpacePosition();

            // set the position of the sword to the position of the hand
            Node swordNode = (Node) modelMap.get(playerID).getChild("sword");
            Bone swordBone = swordNode.getControl(AnimControl.class).getSkeleton().getBone("swordBone");
            swordNode.setLocalRotation(rotation);
            swordNode.setLocalTranslation(position);

            // adjust for difference in rotation
            Quaternion swordRot = swordBone.getModelSpaceRotation();
            Quaternion adjust = (new Quaternion()).fromAngles(FastMath.HALF_PI, 0, 0);
            Matrix3f swordRotMat = swordRot.mult(adjust).toRotationMatrix();

            // adjust for difference in position of wrist and middle of sword
            Vector3f shiftPosition = swordRot.mult(new Vector3f(0f, 1.8f, 0f));

            // build new collision shape
            CompoundCollisionShape cShape = new CompoundCollisionShape();
            Vector3f boxSize = new Vector3f(.1f, .1f, 2.25f);
            cShape.addChildShape(new BoxCollisionShape(boxSize), Vector3f.ZERO, swordRotMat);
            CollisionShapeFactory.shiftCompoundShapeContents(cShape, shiftPosition);

            // remove GhostControl from PhysicsSpace, apply change, put in PhysicsSpace
            SwordControl sword = modelMap.get(playerID).getChild("sword").getControl(SwordControl.class);
            bulletAppState.getPhysicsSpace().remove(sword);
            sword.setCollisionShape(cShape);
            bulletAppState.getPhysicsSpace().add(sword);

            // get rid of oldest, add newest previous state
            if (prevStates.get(playerID).size() >= numPrevStates) {
                prevStates.get(playerID).pollFirst();
            }
            prevStates.get(playerID).offerLast(prevState);
        }
    }
    
    public void updateClients() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - timeOfLastSync > timeBetweenSyncs)|| updateNow) {
            timeOfLastSync = currentTime;
            List<Long> playerList=new LinkedList();
            playerList.addAll(playerSet);
            for (Long sourcePlayerID:playerList) {
                for (Long destPlayerID:playerList) {
                    try {
                        clientMap.get(destPlayerID).send(new CharStatusMessage(upperArmAnglesMap.get(sourcePlayerID), 
                                upperArmVelsMap.get(sourcePlayerID),charPositionMap.get(sourcePlayerID),
                                charVelocityMap.get(sourcePlayerID),elbowWristAngleMap.get(sourcePlayerID),
                                elbowWristVelMap.get(sourcePlayerID),charAngleMap.get(sourcePlayerID),
                                charTurnVelMap.get(sourcePlayerID),sourcePlayerID,charLifeMap.get(sourcePlayerID)));
                    } catch (IOException ex) {
                        Logger.getLogger(BladeServer.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (NullPointerException ex){
                        if(playerSet.contains(destPlayerID))
                            playerSet.remove(destPlayerID); // if the client has disconnected, remove its id
                    }
                }
            }
            updateNow = false;
        }
    }

  

    public void messageReceived(Message message) {
        

        if (message instanceof ClientReadyMessage) {
            try {
                long newPlayerID = currentPlayerID++;
                Client client = message.getClient();
                System.out.println("Received ClientReadyMessage");

                final Node model = Character.createCharacter("Models/Female.mesh.j3o", "Models/sword.mesh.j3o", assetManager, bulletAppState, true, newPlayerID);
                
                //rootNode.attachChild(model);
                //rootNode.attachChild(geom1);
                modelMap.put(newPlayerID, model);
                upperArmAnglesMap.put(newPlayerID, new Vector3f());
                upperArmVelsMap.put(newPlayerID, new Vector3f());
                elbowWristAngleMap.put(newPlayerID, new Float(CharMovement.Constraints.lRotMin));
                elbowWristVelMap.put(newPlayerID, new Float(0f));
                charPositionMap.put(newPlayerID, new Vector3f());
                charVelocityMap.put(newPlayerID, new Vector3f());
                charAngleMap.put(newPlayerID, 0f);
                charTurnVelMap.put(newPlayerID, 0f);
                charLifeMap.put(newPlayerID, 1f);
                
                prevStates.put(newPlayerID, new ArrayDeque<Vector3f[]>(numPrevStates));
                
                client.send(new CharCreationMessage(newPlayerID, true));
                for (Iterator<Long> playerIterator = playerSet.iterator(); playerIterator.hasNext();) {
                    long destPlayerID = playerIterator.next();
                    clientMap.get(destPlayerID).send(new CharCreationMessage(newPlayerID, false)); // send this new character to all other clients
                    client.send(new CharCreationMessage(destPlayerID, false)); // send all other client's characters to this client
                    System.out.println("Sent CharCreationMessage");
                }
                System.out.println("client connected:" + newPlayerID + "," + client);
                playerSet.add(newPlayerID);
                clientMap.put(newPlayerID, client);
                playerIDMap.put(client, newPlayerID);
                timeOfLastCollisionMap.put(newPlayerID, 0L);
                
                actions.add(new Callable() {                    
                    public Object call() throws Exception {
                        rootNode.attachChild(model);
                        return null;
                    }
                });
                                
            } catch (IOException ex) {
                Logger.getLogger(BladeServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            HasID hasID=(HasID)message;
            final long playerID=hasID.getID();
            
            if (playerSet.contains(playerID)) {
                if (message instanceof InputMessages.RotateUArmCC) {
                    System.out.println("rotateCC");
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            upperArmVelsMap.get(playerID).z = -1;
                            return null;
                        }
                    });
                } else if (message instanceof InputMessages.RotateUArmC) {
                    System.out.println("rotateC");
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            upperArmVelsMap.get(playerID).z = 1;
                            return null;
                        }
                    });
                } else if (message instanceof InputMessages.StopRotateTwist) {
                    System.out.println("rotateStop");
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            upperArmVelsMap.get(playerID).z = 0;
                            return null;
                        }
                    });
                } else if (message instanceof InputMessages.MouseMovement) {
                    final InputMessages.MouseMovement mouseMovement = (InputMessages.MouseMovement) message;
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            upperArmVelsMap.get(playerID).x = FastMath.cos(mouseMovement.angle);
                            upperArmVelsMap.get(playerID).y = FastMath.sin(mouseMovement.angle);
                            return null;
                        }
                    });
                } else if (message instanceof InputMessages.StopMouseMovement) {
                    
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            upperArmVelsMap.get(playerID).x = upperArmVelsMap.get(playerID).y = 0;
                            return null;
                        }
                    });
                    
                } else if (message instanceof InputMessages.LArmUp) {
                    System.out.println("arm up");
                    
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            elbowWristVelMap.put(playerID, 1f);
                            return null;
                        }
                    });
                    
                } else if (message instanceof InputMessages.LArmDown) {
                    System.out.println("arm down");
                    
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            elbowWristVelMap.put(playerID, -1f);
                            return null;
                        }
                    });
                    
                } else if (message instanceof InputMessages.StopLArm) {
                    System.out.println("arm stop");
                    
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            elbowWristVelMap.put(playerID, 0f);
                            return null;
                        }
                    });
                    
                } else if (message instanceof InputMessages.MoveCharBackword) {
                    System.out.println("Move foreward");
                    
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            charVelocityMap.get(playerID).z = -CharMovement.charBackwordSpeed;
                            return null;
                        }
                    });
                    
                } else if (message instanceof InputMessages.MoveCharForward) {
                    System.out.println("Move backword");
                    
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            charVelocityMap.get(playerID).z = CharMovement.charForwardSpeed;
                            return null;
                        }
                    });
                    
                } else if (message instanceof InputMessages.MoveCharLeft) {
                    System.out.println("Move left");
                    
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            charVelocityMap.get(playerID).x = CharMovement.charStrafeSpeed;
                            return null;
                        }
                    });
                    
                } else if (message instanceof InputMessages.MoveCharRight) {
                    System.out.println("Move right");
                    
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            charVelocityMap.get(playerID).x = -CharMovement.charStrafeSpeed;
                            return null;
                        }
                    });
                    
                } else if (message instanceof InputMessages.TurnCharLeft) {
                    
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            charTurnVelMap.put(playerID, 1f);
                            return null;
                        }
                    });
                    
                } else if (message instanceof InputMessages.TurnCharRight) {
                    
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            charTurnVelMap.put(playerID, -1f);
                            return null;
                        }
                    });
                    
                } else if (message instanceof InputMessages.StopCharTurn) {
                    
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            charTurnVelMap.put(playerID, 0f);
                            return null;
                        }
                    });
                    
                } else if (message instanceof InputMessages.StopForwardMove) {
                    
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            charVelocityMap.get(playerID).z = 0;
                            return null;
                        }
                    });
                    
                } else if (message instanceof InputMessages.StopLeftRightMove) {
                    System.out.println("Stop Left Right Move");
                    
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            charVelocityMap.get(playerID).x = 0;
                            return null;
                        }
                    });
                    
                }
            } else {
                System.out.println("PlayerID " + playerID + " is not in the set");
            }
        }
    }

    public void messageSent(Message message) {
  //      System.out.println("Sending message to "+message.getClient());
  //      System.out.println("Sending message "+message.getClass());
    }

    public void objectReceived(Object object) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void objectSent(Object object) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clientConnected(Client client) {
         
    }

    public void clientDisconnected(Client client) {
        System.out.println("client disconnecting is " + client);
        final long playerID = playerIDMap.get(client);
        List<Long> players = new LinkedList();
        Node model=modelMap.get(playerID);
        bulletAppState.getPhysicsSpace().remove(model.getChild("sword").getControl(SwordControl.class));
        bulletAppState.getPhysicsSpace().remove(model.getControl(BodyControl.class));
        bulletAppState.getPhysicsSpace().remove(model.getControl(CharacterControl.class));
        //rootNode.detachChild(modelMap.get(playerID));
        playerIDMap.remove(client);
        clientMap.remove(playerID);
        players.addAll(playerSet);
        playerSet.remove(playerID);
        players.remove(playerID);
        prevStates.remove(playerID);
        for (Long destID : players) {
            try {
                clientMap.get(destID).send(new CharDestructionMessage(playerID));
            } catch (IOException ex) {
                Logger.getLogger(BladeServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }        
        actions.add(new Callable() {
            public Object call() throws Exception {
                rootNode.detachChild(modelMap.get(playerID));
                modelMap.remove(playerID);
                return null;
            }
        });
    }
}
