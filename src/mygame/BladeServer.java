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
import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
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
import com.jme3.math.Matrix4f;
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
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;
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
    private final int goBackNumStates = 2;

    private Queue<Callable> actions = new ConcurrentLinkedQueue<Callable>();
    private long timeOfLastSync=0;
    private long prevTimeSwordHit=0;
    private long currentPlayerID=0;
    private float deflectFrictionCoeff=0.7f;
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
        
       
        Logger.getLogger("").setLevel(Level.SEVERE);
        
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

            public void collision(final PhysicsCollisionEvent event) {
                final PhysicsCollisionObject a = event.getObjectA();
                final PhysicsCollisionObject b = event.getObjectB();
                final Vector3f collisionCoordinates = event.getPositionWorldOnA();
                if ((a != null && b != null && a instanceof ControlID && b instanceof ControlID
                        && ((ControlID) a).getID() != ((ControlID) b).getID())) {

                    System.out.println("Collision!");

                    final long playerID1 = Long.valueOf(((ControlID) a).getID());
                    final long playerID2 = Long.valueOf(((ControlID) b).getID());

                    final Character characterA=charMap.get(playerID1);
                    final Character characterB=charMap.get(playerID2);
                    final Node modelA = characterA.bodyModel;
                    final Node modelB = characterB.bodyModel;

                    actions.add(new Callable() {

                        public Object call() throws Exception {

                            long curTime = System.currentTimeMillis();
                    
                            if ((a instanceof SwordControl) && (b instanceof SwordControl)
                                    && (curTime - prevTimeSwordHit) > 100) {
                                // Handle sword deflection

                                /*********/
                                Vector3f localHandPosA1 = characterA.bodyModel.getControl(AnimControl.class).getSkeleton().getBone("swordHand").getModelSpacePosition();
                                Vector3f localHandPosB1 = characterB.bodyModel.getControl(AnimControl.class).getSkeleton().getBone("swordHand").getModelSpacePosition();

                                Vector3f worldHandPosA1 = characterA.bodyModel.localToWorld(localHandPosA1, null);
                                Vector3f worldHandPosB1 = characterB.bodyModel.localToWorld(localHandPosB1, null);

                                Matrix4f rotA2 = new Matrix4f();
                                rotA2.fromAngleNormalAxis(characterA.upperArmVels.x, Vector3f.UNIT_Y);

                                Matrix4f rotB2 = new Matrix4f();
                                rotB2.fromAngleNormalAxis(characterB.upperArmVels.x, Vector3f.UNIT_Y);

                                Vector3f localHandPosA2 = rotA2.mult(modelA.worldToLocal(worldHandPosA1, null));
                                Vector3f localHandPosB2 = rotB2.mult(modelB.worldToLocal(worldHandPosB1, null));

                                Vector3f worldHandPosA2 = characterA.bodyModel.localToWorld(localHandPosA2, null);
                                Vector3f worldHandPosB2 = characterB.bodyModel.localToWorld(localHandPosB2, null);

                                System.out.println("Initial v1: " + characterA.upperArmVels.x + ", " + characterA.upperArmVels.y);
                                System.out.println("Initial v2: " + characterB.upperArmVels.x + ", " + characterB.upperArmVels.y);

                                System.out.println("HandA1: " + localHandPosA1 + " HandA2: " + localHandPosA2);
                                System.out.println("HandB1: " + localHandPosB1 + " HandB2: " + localHandPosB2);

                                // across body:  -x   away from body:  +x
                                // checking x direction of playerID2 from playerID1 reference
                                Vector3f locAHandPosB1 = modelA.worldToLocal(worldHandPosB1, null);
                                Vector3f locAHandPosB2 = modelA.worldToLocal(worldHandPosB2, null);
                                System.out.println("locAHandPosB1: " + locAHandPosB1);
                                System.out.println("locAHandPosB2: " + locAHandPosB2);
                                float locADirB = FastMath.sign(locAHandPosB2.x - locAHandPosB1.x);

                                System.out.println("***X direction of player2: " + locADirB);

                                // checking x direction of playerID1 from playerID2 reference
                                Vector3f locBHandPosA1 = modelB.worldToLocal(worldHandPosA1, null);
                                Vector3f locBHandPosA2 = modelB.worldToLocal(worldHandPosA2, null);
                                System.out.println("locBHandPosA1: " + locBHandPosA1);
                                System.out.println("locBHandPosA2: " + locBHandPosA2);
                                float locBDirA = FastMath.sign(locBHandPosA2.x - locBHandPosA1.x);

                                System.out.println("***X direction of player1: " + locBDirA);

                                float dx1 = FastMath.abs(characterB.upperArmVels.x) * locADirB - characterA.upperArmVels.x;
                                float dy1 = characterB.upperArmVels.y - characterA.upperArmVels.y;

                                float dx2 = characterB.upperArmVels.x - FastMath.abs(characterA.upperArmVels.x) * locBDirA;
                                float dy2 = characterB.upperArmVels.y - characterA.upperArmVels.y;

                                float collisionisionAngleA = FastMath.atan2(dy1, dx1);
                                float collisionisionAngleB = FastMath.atan2(dy2, dx2);

                                float direction1 = FastMath.atan2(characterA.upperArmVels.y, characterA.upperArmVels.x);
                                float direction2 = FastMath.atan2(characterB.upperArmVels.y, characterB.upperArmVels.x);

                                float newX1Vel = characterA.upperArmVels.length() * FastMath.cos(direction1 - collisionisionAngleA);
                                float newY1Vel = characterA.upperArmVels.length() * FastMath.sin(direction1 - collisionisionAngleA);
                                float newX2Vel = characterB.upperArmVels.length() * FastMath.cos(direction2 - collisionisionAngleB);
                                float newY2Vel = characterB.upperArmVels.length() * FastMath.sin(direction2 - collisionisionAngleB);

                                float x1Vel = FastMath.cos(collisionisionAngleA) * /*FastMath.abs(newX2Vel) * locADirB*/ newX2Vel + FastMath.cos(collisionisionAngleA + FastMath.PI / 2) * newY1Vel;
                                float y1Vel = FastMath.sin(collisionisionAngleA) * /*FastMath.abs(newX2Vel) * locADirB*/ newX2Vel + FastMath.sin(collisionisionAngleA + FastMath.PI / 2) * newY1Vel;
                                float x2Vel = FastMath.cos(collisionisionAngleB) * /*FastMath.abs(newX1Vel) * locBDirA*/ newX1Vel + FastMath.cos(collisionisionAngleB + FastMath.PI / 2) * newY2Vel;
                                float y2Vel = FastMath.sin(collisionisionAngleB) * /*FastMath.abs(newX1Vel) * locBDirA*/ newX1Vel + FastMath.sin(collisionisionAngleB + FastMath.PI / 2) * newY2Vel;

                                System.out.println("id: " + playerID1 + " x1Vel: " + x1Vel + " y1Vel: " + y1Vel);

                                System.out.println("id: " + playerID2 + " x2Vel: " + x2Vel + " Y2Vel: " + y2Vel);

                                characterA.upperArmDeflectVels=new Vector3f(x1Vel, y1Vel, 0f);
                                characterB.upperArmDeflectVels=new Vector3f(x2Vel, y2Vel, 0f);

                                /*********/
                                prevTimeSwordHit = curTime;
                            }



                            if ((a instanceof SwordControl) && (b instanceof BodyControl)) {
                                charLifeMap.put(playerID2, charLifeMap.get(playerID2) * 0.999f);
                            } else if ((b instanceof SwordControl) && (a instanceof BodyControl)) {
                                charLifeMap.put(playerID1, charLifeMap.get(playerID1) * 0.999f);
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
                                    
                                    Message message;
                                    if (swordSword) {
                                        message = new SwordSwordCollisionMessage(new Vector3f(collisionCoordinates));
                                        createEffect(collisionCoordinates,clankMat);
                                    } else {
                                        message = new SwordBodyCollisionMessage(new Vector3f(collisionCoordinates));
                                        createEffect(collisionCoordinates,bloodMat);
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
                            Character character1=charMap.get(playerID1);
                            Character character2=charMap.get(playerID2);
                            character1.upperArmAngles=p1State[0];
                            character2.upperArmAngles=p2State[0];
                            
                            character1.elbowWristAngle=p1State[1].getX();
                            character2.elbowWristAngle=p2State[1].getX();
                            
                            character1.charAngle=p1State[1].getY();
                            character2.charAngle=p2State[1].getY();
                            
                            character1.position=p1State[2];
                            character2.position=p2State[2];
                            
                            character1.charControl.setPhysicsLocation(character1.position); 
                            character2.charControl.setPhysicsLocation(character2.position); 

                            

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
            Character character=charMap.get(playerID);
            Vector3f[] prevState = new Vector3f[3];
            prevState[0]=character.upperArmAngles;
            prevState[1]=new Vector3f(character.elbowWristAngle,0f,0f);
            prevState[1].setY(character.charAngle);
            prevState[2] = character.position.clone();
            character.update(tpf,true);
            
            Vector3f upperArmDeflectVels = character.upperArmDeflectVels;
            
            float deflectMagnitude = upperArmDeflectVels.length();

            if (deflectMagnitude != 0f) {

                if (upperArmDeflectVels.length() < FastMath.FLT_EPSILON) {
                    character.upperArmDeflectVels=upperArmDeflectVels.mult(0);
                } else {
                    float newX, newY;
                    
                    if (FastMath.abs(upperArmDeflectVels.x) < FastMath.FLT_EPSILON) {
                        newX = 0f;
                    } else {
                        float signX = FastMath.sign(upperArmDeflectVels.x);
                        newX = upperArmDeflectVels.x + (-1f * signX * deflectFrictionCoeff * tpf);
                        newX = (FastMath.sign(newX) != signX)?0f:newX;
                    }
                    if (FastMath.abs(upperArmDeflectVels.y) < FastMath.FLT_EPSILON) {
                        newY = 0f;
                    } else {
                        float signY = FastMath.sign(upperArmDeflectVels.y);
                        newY = upperArmDeflectVels.y + (-1f * signY * deflectFrictionCoeff * tpf);
                        newY = (FastMath.sign(newY) != signY)?0f:newX;
                    }
                    
                    character.upperArmDeflectVels=new Vector3f(newX, newY, 0);
                }
            }
            
            
            // Adjust the sword collision shape in accordance with arm movement.
            // first, get rotation and position of hand
            Bone hand = character.bodyModel.getControl(AnimControl.class).getSkeleton().getBone("swordHand");
            Matrix3f rotation = hand.getModelSpaceRotation().toRotationMatrix();
            Vector3f position = hand.getModelSpacePosition();

            // set the position of the sword to the position of the hand
            Node swordNode = character.swordModel;
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
            SwordControl sword = character.swordControl;
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
                Character character=charMap.get(sourcePlayerID);
                CharStatusMessage message=new CharStatusMessage(character.upperArmAngles,character.upperArmVels,
                        character.position,character.velocity,character.elbowWristAngle,character.elbowWristVel,
                        character.charAngle,character.turnVel,sourcePlayerID,charLifeMap.get(sourcePlayerID));
                for (Long destPlayerID:playerList) {
                    try {
                        clientMap.get(destPlayerID).send(message);
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
                Character character=new Character(newPlayerID,bulletAppState,assetManager);
                charMap.put(newPlayerID, character);
                final Node model = character.bodyModel;//Character.createCharacter("Models/Female.mesh.j3o", "Models/sword.mesh.j3o", assetManager, bulletAppState, true, newPlayerID);

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
                final Character character=charMap.get(playerID);
                
                if (message instanceof InputMessages.RotateUArmCC) {
                    System.out.println("rotateCC");
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            character.upperArmVels.z = -1;
                            return null;
                        }
                    });
                } else if (message instanceof InputMessages.RotateUArmC) {
                    System.out.println("rotateC");
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            character.upperArmVels.z = 1;
                            return null;
                        }
                    });
                } else if (message instanceof InputMessages.StopRotateTwist) {
                    System.out.println("rotateStop");
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            character.upperArmVels.z = 0;
                            return null;
                        }
                    });
                } else if (message instanceof InputMessages.MouseMovement) {
                    final InputMessages.MouseMovement mouseMovement = (InputMessages.MouseMovement) message;
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            character.upperArmVels.x = FastMath.cos(mouseMovement.angle);
                            character.upperArmVels.y = FastMath.sin(mouseMovement.angle);
                            return null;
                        }
                    });
                } else if (message instanceof InputMessages.StopMouseMovement) {
                    
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            character.upperArmVels.x = character.upperArmVels.y = 0;
                            return null;
                        }
                    });
                    
                } else if (message instanceof InputMessages.LArmUp) {
                    System.out.println("arm up");
                    
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            character.elbowWristVel=1f;
                            return null;
                        }
                    });
                    
                } else if (message instanceof InputMessages.LArmDown) {
                    System.out.println("arm down");
                    
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            character.elbowWristVel=-1f;
                            return null;
                        }
                    });
                    
                } else if (message instanceof InputMessages.StopLArm) {
                    System.out.println("arm stop");
                    
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            character.elbowWristVel=0f;
                            return null;
                        }
                    });
                    
                } else if (message instanceof InputMessages.MoveCharBackword) {
                    System.out.println("Move foreward");
                    
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            character.velocity.z = -CharMovement.charBackwordSpeed;
                            return null;
                        }
                    });
                    
                } else if (message instanceof InputMessages.MoveCharForward) {
                    System.out.println("Move backword");
                    
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            character.velocity.z = CharMovement.charForwardSpeed;
                            return null;
                        }
                    });
                    
                } else if (message instanceof InputMessages.MoveCharLeft) {
                    System.out.println("Move left");
                    
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            character.velocity.x = CharMovement.charStrafeSpeed;
                            return null;
                        }
                    });
                    
                } else if (message instanceof InputMessages.MoveCharRight) {
                    System.out.println("Move right");
                    
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            character.velocity.x = -CharMovement.charStrafeSpeed;
                            return null;
                        }
                    });
                    
                } else if (message instanceof InputMessages.TurnCharLeft) {
                    System.out.println("Turn Left");
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            character.turnVel=1f;
                            return null;
                        }
                    });
                    
                } else if (message instanceof InputMessages.TurnCharRight) {
                    System.out.println("Turn Right");
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            character.turnVel=-1f;
                            return null;
                        }
                    });
                    
                } else if (message instanceof InputMessages.StopCharTurn) {
                    System.out.println("Stop Turn");
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            character.turnVel=0f;
                            return null;
                        }
                    });
                    
                } else if (message instanceof InputMessages.StopForwardMove) {
                    
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            character.velocity.z = 0;
                            return null;
                        }
                    });
                    
                } else if (message instanceof InputMessages.StopLeftRightMove) {
                    System.out.println("Stop Left Right Move");
                    
                    actions.add(new Callable() {
                        public Object call() throws Exception {
                            character.velocity.x = 0;
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
        Node model=charMap.get(playerID).bodyModel;
        bulletAppState.getPhysicsSpace().remove(model.getChild("sword").getControl(SwordControl.class));
        bulletAppState.getPhysicsSpace().remove(model.getControl(BodyControl.class));
        bulletAppState.getPhysicsSpace().remove(model.getControl(CharacterControl.class));

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
                rootNode.detachChild(charMap.get(playerID).bodyModel);
                charMap.remove(playerID);
                return null;
            }
        });
    }
}
