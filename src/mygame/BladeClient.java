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

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.Bone;
import mygame.messages.InputMessages;
import mygame.messages.CharPositionMessage;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bounding.BoundingVolume;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.network.connection.Client;
import com.jme3.network.events.ConnectionListener;
import com.jme3.network.events.MessageListener;
import com.jme3.network.message.Message;
import com.jme3.network.serializing.Serializer;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3tools.converters.ImageToAwt;
import mygame.messages.CharCreationMessage;
import mygame.messages.CharDestructionMessage;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Matrix3f;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;

/**
 *
 * @author blah
 */


public class BladeClient extends SimpleApplication implements MessageListener, RawInputListener, ConnectionListener, AnimEventListener {

    private ChaseCamera chaseCam;
    private Node model;
    ConcurrentHashMap<Long, Node> modelMap = new ConcurrentHashMap();
    ConcurrentHashMap<Long, Vector3f> upperArmAnglesMap = new ConcurrentHashMap();
    ConcurrentHashMap<Long, Vector3f> upperArmVelsMap = new ConcurrentHashMap();
    ConcurrentHashMap<Long, Float> elbowWristAngleMap = new ConcurrentHashMap();
    ConcurrentHashMap<Long, Float> elbowWristVelMap = new ConcurrentHashMap();
    HashSet<Long> playerSet = new HashSet();
    ConcurrentHashMap<Long, Vector3f> charPositionMap = new ConcurrentHashMap();
    ConcurrentHashMap<Long, Vector3f> charVelocityMap = new ConcurrentHashMap();
    ConcurrentHashMap<Long, Float> charAngleMap = new ConcurrentHashMap();
    ConcurrentHashMap<Long, Float> charTurnVelMap = new ConcurrentHashMap();
    ConcurrentHashMap<Long, AnimChannel> animChannelMap = new ConcurrentHashMap();

    ConcurrentHashMap<Long, Vector3f> prevUpperArmAnglesMap = new ConcurrentHashMap();
    //ConcurrentHashMap<Long, Vector3f> prevUpperArmVelsMap = new ConcurrentHashMap();
    ConcurrentHashMap<Long, Float> prevElbowWristAngleMap = new ConcurrentHashMap();
    //ConcurrentHashMap<Long, Float> prevElbowWristVelMap = new ConcurrentHashMap();
    ConcurrentHashMap<Long, Vector3f> prevCharPositionMap = new ConcurrentHashMap();
    //ConcurrentHashMap<Long, Vector3f> prevCharVelocityMap = new ConcurrentHashMap();
    ConcurrentHashMap<Long, Float> prevCharAngleMap = new ConcurrentHashMap();
    //ConcurrentHashMap<Long, Float> prevCharTurnVelMap = new ConcurrentHashMap();
    
    private BulletAppState bulletAppState;
    private TerrainQuad terrain;
    Material mat_terrain;
    Material wall_mat;
    Material stone_mat;
    Material floor_mat;
    private RigidBodyControl terrain_phy;
    private RigidBodyControl basic_phy;
    private RigidBodyControl body_phy;
    CharacterControl character;
    CompoundCollisionShape collisionShape;
    BoundingVolume ballBound;
    Geometry block;
    BoxCollisionShape leftShoulder;

    Client client;
    boolean clientSet = false;
    private long playerID = 0;

    public static void main(String[] args) {
        BladeClient app = new BladeClient();
        AppSettings appSettings=new AppSettings(true);
        appSettings.setFrameRate(30);
        app.setSettings(appSettings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Serializer.registerClass(CharPositionMessage.class);
        Serializer.registerClass(CharCreationMessage.class);
        Serializer.registerClass(CharDestructionMessage.class);
        InputMessages.registerInputClasses();
        
        flyCam.setMoveSpeed(50);
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Skysphere.jpg", true));
        initMaterials();
        initTerrain();
               
        try {
            client = new Client(BladeMain.serverIP, BladeMain.port, BladeMain.port);
            client.start();

            client.addMessageListener(this, CharCreationMessage.class, CharDestructionMessage.class, CharPositionMessage.class);

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        InputMessages.addInputMessageListeners(client, this);

        client.addConnectionListener(this);
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
        rootNode.addLight(sun);

        DirectionalLight sun2 = new DirectionalLight();
        sun2.setDirection(new Vector3f(0.1f, 0.7f, 1.0f));
        rootNode.addLight(sun2);

        flyCam.setEnabled(false);

        PhysicsCollisionListener physListener = new PhysicsCollisionListener() {

            public void collision(PhysicsCollisionEvent event) {

                Spatial a = event.getNodeA();
                Spatial b = event.getNodeB();

                if ((a.getControl(GhostControl.class) != null
                        && b.getControl(GhostControl.class) != null)
                        || (a.getControl(CharacterControl.class) != null
                        && b.getControl(CharacterControl.class) != null)) {
                    System.out.println("Collision!");

                    long playerID1 = Long.valueOf(a.getName());
                    long playerID2 = Long.valueOf(b.getName());
                    upperArmAnglesMap.put(playerID1, prevUpperArmAnglesMap.get(playerID1));
                    upperArmAnglesMap.put(playerID2, prevUpperArmAnglesMap.get(playerID2));

                    elbowWristAngleMap.put(playerID1, prevElbowWristAngleMap.get(playerID1));
                    elbowWristAngleMap.put(playerID2, prevElbowWristAngleMap.get(playerID2));

                    charAngleMap.put(playerID1, prevCharAngleMap.get(playerID1));
                    charAngleMap.put(playerID2, prevCharAngleMap.get(playerID2));

                    charPositionMap.put(playerID1, prevCharPositionMap.get(playerID1));
                    charPositionMap.put(playerID2, prevCharPositionMap.get(playerID2));

                    CharMovement.setUpperArmTransform(upperArmAnglesMap.get(playerID1), modelMap.get(playerID1));
                    CharMovement.setLowerArmTransform(elbowWristAngleMap.get(playerID1), modelMap.get(playerID1));
                    CharMovement.setUpperArmTransform(upperArmAnglesMap.get(playerID2), modelMap.get(playerID2));
                    CharMovement.setLowerArmTransform(elbowWristAngleMap.get(playerID2), modelMap.get(playerID2));

                    upperArmVelsMap.put(playerID1, upperArmVelsMap.get(playerID1).mult(-1.0f));
                    upperArmVelsMap.put(playerID2, upperArmVelsMap.get(playerID2).mult(-1.0f));
                    elbowWristVelMap.put(playerID1, elbowWristVelMap.get(playerID1)*-1.0f);
                    elbowWristVelMap.put(playerID2, elbowWristVelMap.get(playerID2)*-1.0f);
                    charVelocityMap.put(playerID1, charVelocityMap.get(playerID1).mult(-1.0f));
                    charVelocityMap.put(playerID2, charVelocityMap.get(playerID2).mult(-1.0f));
                    charTurnVelMap.put(playerID1, charTurnVelMap.get(playerID1)*-1.0f);
                    charTurnVelMap.put(playerID2, charTurnVelMap.get(playerID2)*-1.0f);
                    
                }
            }
        };

        /*
        PhysicsTickListener physTickListener = new PhysicsTickListener() {
            public void prePhysicsTick(PhysicsSpace space, float f) {

            }

            public void physicsTick(PhysicsSpace space, float f) {

            }
        };
         *
         */
        this.getStateManager().getState(BulletAppState.class).getPhysicsSpace().addCollisionListener(physListener);
        this.getStateManager().getState(BulletAppState.class).getPhysicsSpace().enableDebug(this.getAssetManager());

    }
    private boolean mouseCurrentlyStopped = true;

    @Override
    public void simpleUpdate(float tpf) {
        
        if (clientSet) {
            characterUpdate(tpf);
            if ((System.currentTimeMillis() - timeOfLastMouseMotion) > mouseMovementTimeout && !mouseCurrentlyStopped) {
                try {

                    client.send(new InputMessages.StopMouseMovement(playerID));
                } catch (IOException ex) {
                    Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
                }
                currentMouseEvents = 0;
                timeOfLastMouseMotion = System.currentTimeMillis();
                mouseCurrentlyStopped = true;
            }
        }
        //rootNode.updateGeometricState();
    }

    /*
    private void handleCollisions(Long playerID) {

        CollisionResults results = new CollisionResults();
        Node player = modelMap.get(playerID);
        for (Map.Entry<Long, Node> playerEntry : modelMap.entrySet()) {
            if (playerEntry.getKey() != playerID) {
                long pID = playerEntry.getKey();

                BoundingVolume bv = modelMap.get(pID).getWorldBound();
                
                player.collideWith(player, results);
                
                //block.collideWith(block, results);
                if (results.size() > 0) {
                    System.out.println("Client: COLLISION DETECTED");
                }
                
            }
        }
    }
     *
     */
    
    public void characterUpdate(float tpf) {
        for (Iterator<Long> playerIterator = playerSet.iterator(); playerIterator.hasNext();) {
            long nextPlayerID = playerIterator.next();
            prevUpperArmAnglesMap.put(nextPlayerID, upperArmAnglesMap.get(nextPlayerID));
            upperArmAnglesMap.put(nextPlayerID, CharMovement.extrapolateUpperArmAngles(upperArmAnglesMap.get(nextPlayerID), upperArmVelsMap.get(nextPlayerID), tpf));
            
            prevElbowWristAngleMap.put(nextPlayerID, elbowWristAngleMap.get(nextPlayerID));
            elbowWristAngleMap.put(nextPlayerID, CharMovement.extrapolateLowerArmAngles(elbowWristAngleMap.get(nextPlayerID), elbowWristVelMap.get(nextPlayerID), tpf));

            prevCharAngleMap.put(nextPlayerID, charAngleMap.get(nextPlayerID));
            charAngleMap.put(nextPlayerID, CharMovement.extrapolateCharTurn(charAngleMap.get(nextPlayerID), charTurnVelMap.get(nextPlayerID), tpf));

            prevCharPositionMap.put(nextPlayerID, charPositionMap.get(nextPlayerID));
            charPositionMap.put(nextPlayerID, CharMovement.extrapolateCharMovement(charPositionMap.get(nextPlayerID),
                    charVelocityMap.get(nextPlayerID), charAngleMap.get(nextPlayerID),tpf));

            CharMovement.setUpperArmTransform(upperArmAnglesMap.get(nextPlayerID), modelMap.get(nextPlayerID));
            CharMovement.setLowerArmTransform(elbowWristAngleMap.get(nextPlayerID), modelMap.get(nextPlayerID));

            Vector3f extrapolatedPosition,currentPosition;
            extrapolatedPosition=charPositionMap.get(nextPlayerID);
            currentPosition=modelMap.get(nextPlayerID).getLocalTranslation();
            float diffLength=FastMath.sqrt(FastMath.sqr(extrapolatedPosition.x-currentPosition.x)+FastMath.sqr(extrapolatedPosition.z-currentPosition.z));
            CharacterControl control=modelMap.get(nextPlayerID).getControl(CharacterControl.class);
            if(diffLength>5){
                  control.setPhysicsLocation(extrapolatedPosition);//new Vector3f(extrapolatedPosition.x,currentPosition.y+1,extrapolatedPosition.z));
            }

            float xDir,zDir;
            zDir=FastMath.cos(charAngleMap.get(nextPlayerID));
            xDir=FastMath.sin(charAngleMap.get(nextPlayerID));
            Vector3f viewDirection=new Vector3f(xDir,0,zDir);
            modelMap.get(nextPlayerID).getControl(CharacterControl.class).setViewDirection(viewDirection);

            Vector3f forward,up,left;
            float xVel,zVel;
            xVel=charVelocityMap.get(nextPlayerID).x;
            zVel=charVelocityMap.get(nextPlayerID).z;
            forward=new Vector3f(viewDirection);
            up=new Vector3f(0,1,0);
            left=up.cross(forward);

            if(nextPlayerID==playerID){
                cam.setDirection(viewDirection);
                cam.setLocation(modelMap.get(nextPlayerID).getLocalTranslation().add(new Vector3f(0,4,0)).subtract(viewDirection.mult(8)));
            }

            control.setWalkDirection(left.mult(xVel).add(forward.mult(zVel)));
            //handleCollisions(nextPlayerID);

            // Adjust the sword collision shape in accordance with arm movement.
            // first, get rotation and position of hand
            Bone hand = modelMap.get(nextPlayerID).getControl(AnimControl.class).getSkeleton().getBone("HandR");
            Matrix3f rotation = hand.getModelSpaceRotation().toRotationMatrix();
            Vector3f position = hand.getModelSpacePosition();

            // adjust for difference in position of wrist and middle of sword
            Vector3f shiftPosition = rotation.mult(new Vector3f(0f, .5f, 2.5f));

            // build new collision shape
            CompoundCollisionShape cShape = new CompoundCollisionShape();
            Vector3f boxSize = new Vector3f(.1f, .1f, 2.25f);
            cShape.addChildShape(new BoxCollisionShape(boxSize), position, rotation);
            CollisionShapeFactory.shiftCompoundShapeContents(cShape, shiftPosition);
            cShape.addChildShape(new CapsuleCollisionShape(1.5f, 6f), Vector3f.ZERO);
            // remove GhostControl from PhysicsSpace, apply change, put in PhysicsSpace
            GhostControl ghost = modelMap.get(nextPlayerID).getControl(GhostControl.class);
            bulletAppState.getPhysicsSpace().remove(ghost);
            ghost.setCollisionShape(cShape);
            bulletAppState.getPhysicsSpace().add(ghost);
        }
    }

    public void registerInput() {
        inputManager.addRawInputListener(this);
        inputManager.setCursorVisible(false);
    }

    public void initTerrain() {


        mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");

        mat_terrain.setTexture("m_Alpha", assetManager.loadTexture("Textures/alpha1.1.png"));

        Texture grass = assetManager.loadTexture("Textures/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("m_Tex1", grass);
        mat_terrain.setFloat("m_Tex1Scale", 64f);

        Texture dirt = assetManager.loadTexture("Textures/TiZeta_SmlssWood1.jpg");
        dirt.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("m_Tex2", dirt);
        mat_terrain.setFloat("m_Tex2Scale", 32f);

        Texture rock = assetManager.loadTexture("Textures/TiZeta_cem1.jpg");
        rock.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("m_Tex3", rock);
        mat_terrain.setFloat("m_Tex3Scale", 128f);

        AbstractHeightMap heightmap = null;
        Texture heightMapImage = assetManager.loadTexture("Textures/flatland.png");
        heightmap = new ImageBasedHeightMap(
                ImageToAwt.convert(heightMapImage.getImage(), false, true, 0));
        heightmap.load();
        terrain = new TerrainQuad("my terrain", 65, 1025, heightmap.getHeightMap());
        terrain.setMaterial(mat_terrain);
        
        terrain.setLocalTranslation(0, -100, 0);
//<<<<<<< HEAD:src/mygame/BladeClient.java
 //       terrain.setLocalScale(1f, 1f, 1f);
        
//
        terrain.setLocalScale(2f, 2f, 2f);

//>>>>>>> 384534a1ee69e55357271112fa5003cb47fd87df:src/mygame/BladeClient.java
        rootNode.attachChild(terrain);



        terrain_phy = new RigidBodyControl(0.0f);
        terrain_phy.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
        terrain_phy.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_03);
        terrain.addControl(terrain_phy);
        bulletAppState.getPhysicsSpace().add(terrain_phy);
        



    }

    public void initMaterials() {
        wall_mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        TextureKey key = new TextureKey("Textures/road.jpg");
        key.setGenerateMips(true);
        Texture tex = assetManager.loadTexture(key);
        wall_mat.setTexture("ColorMap", tex);

        stone_mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        TextureKey key2 = new TextureKey("Textures/road.jpg");
        key2.setGenerateMips(true);

        Texture tex2 = assetManager.loadTexture(key2);
        stone_mat.setTexture("ColorMap", tex2);

        floor_mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
        TextureKey key3 = new TextureKey("Textures/grass.jpg");
        key3.setGenerateMips(true);
        Texture tex3 = assetManager.loadTexture(key3);
        tex3.setWrap(WrapMode.Repeat);
        floor_mat.setTexture("ColorMap", tex3);
    }

    public void messageReceived(Message message) {
        if (message instanceof CharDestructionMessage){
            CharDestructionMessage destroMessage=(CharDestructionMessage)message;
            long destroyedPlayerID=destroMessage.playerID;
            System.out.println("my id is "+playerID+", and destroID is "+destroyedPlayerID);
            playerSet.remove(destroyedPlayerID);
            rootNode.detachChild(modelMap.get(destroyedPlayerID));
            modelMap.remove(destroyedPlayerID);
        }
        else if (message instanceof CharCreationMessage) {
            System.out.println("Creating character");
            CharCreationMessage creationMessage = (CharCreationMessage) message;
            long newPlayerID = creationMessage.playerID;
            Node newModel = Character.createCharacter("Models/FighterRight.mesh.xml", assetManager, bulletAppState, true, newPlayerID);

            rootNode.attachChild(newModel);
            
            if (creationMessage.controllable) {
                playerID = newPlayerID;
                model = newModel;
                System.out.println("claiming player id " + playerID);
/*
                chaseCam = new ChaseCamera(cam, model, inputManager);
                chaseCam.setSmoothMotion(true);
                chaseCam.setDefaultVerticalRotation(FastMath.HALF_PI / 4f);
                chaseCam.setLookAtOffset(new Vector3f(0.0f, 4.0f, 0.0f));
 */
                registerInput();
                clientSet = true;
            }
            modelMap.put(newPlayerID, newModel);
            
            playerSet.add(newPlayerID);
            upperArmAnglesMap.put(newPlayerID, new Vector3f());
            upperArmVelsMap.put(newPlayerID, new Vector3f());
            elbowWristAngleMap.put(newPlayerID, new Float(CharMovement.Constraints.lRotMin));
            elbowWristVelMap.put(newPlayerID, new Float(0f));
            charPositionMap.put(newPlayerID, new Vector3f());
            charVelocityMap.put(newPlayerID, new Vector3f());
            charAngleMap.put(newPlayerID, 0f);
            charTurnVelMap.put(newPlayerID, 0f);
            animChannelMap.put(newPlayerID, modelMap.get(newPlayerID).getControl(AnimControl.class).createChannel());
            animChannelMap.get(newPlayerID).setAnim("stand");

            prevUpperArmAnglesMap.put(newPlayerID, new Vector3f());
            prevElbowWristAngleMap.put(newPlayerID, new Float(CharMovement.Constraints.lRotMin));
            prevCharPositionMap.put(newPlayerID, new Vector3f());
            prevCharAngleMap.put(newPlayerID, 0f);
            
        } else if (message instanceof CharPositionMessage) {
            if (clientSet) {

                CharPositionMessage charPosition = (CharPositionMessage) message;
                long messagePlayerID = charPosition.playerID;

                upperArmAnglesMap.put(messagePlayerID, charPosition.upperArmAngles.clone());
                upperArmVelsMap.put(messagePlayerID, charPosition.upperArmVels.clone());
                elbowWristAngleMap.put(messagePlayerID, charPosition.elbowWristAngle);
                elbowWristVelMap.put(messagePlayerID, charPosition.elbowWristVel);
                charPositionMap.put(messagePlayerID, charPosition.charPosition);
                charVelocityMap.put(messagePlayerID, charPosition.charVelocity);
                charAngleMap.put(messagePlayerID, charPosition.charAngle);
                charTurnVelMap.put(messagePlayerID, charPosition.charTurnVel);
                if (animChannelMap.get(messagePlayerID) != null) {
                    if (charVelocityMap.get(messagePlayerID).equals(new Vector3f(0, 0, 0))) {
                        if (animChannelMap.get(messagePlayerID).getAnimationName().equals("walk")) {
                            animChannelMap.get(messagePlayerID).setAnim("stand");
                        }
                    } else {
                        if (animChannelMap.get(messagePlayerID).getAnimationName().equals("stand")) {
                            animChannelMap.get(messagePlayerID).setAnim("walk");
                        }
                    }
                }
            }
        }
    }

    public void messageSent(Message message) {
        System.out.println(message.getClass());
    }

    public void objectReceived(Object object) {
    }

    public void objectSent(Object object) {
    }

    public void beginInput() {
    }

    public void endInput() {
    }

    public void onJoyAxisEvent(JoyAxisEvent evt) {
    }

    public void onJoyButtonEvent(JoyButtonEvent evt) {
    }
    private final int eventsPerPacket = 10; // how many events should happen before next packet is sent
    private final long mouseMovementTimeout = 100; // how long until we propose to send a StopMouseMovement message
    private long timeOfLastMouseMotion = 0; // how long since last movement
    private int currentMouseEvents = 0;
    private int currentDX = 0;
    private int currentDY = 0;
    private int prevDeltaWheel = 0;

    public void onMouseMotionEvent(MouseMotionEvent evt) {

        float dy = evt.getDY(), dx = evt.getDX();
        if (dy != 0 || dx != 0) {
            currentMouseEvents++;
            currentDX += dx;
            currentDY += dy;

            if (currentMouseEvents >= eventsPerPacket) {
                try {
                    float angle = FastMath.atan2(currentDY, currentDX);
                    if (angle < 0) {
                        angle = FastMath.TWO_PI + angle;
                    }
                    client.send(new InputMessages.MouseMovement(angle, playerID));
                } catch (IOException ex) {
                    Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
                }

                currentMouseEvents = 0;
                currentDX = 0;
                currentDY = 0;
            }

            timeOfLastMouseMotion = System.currentTimeMillis();
            mouseCurrentlyStopped = false;
        }

        try {
            if (evt.getDeltaWheel() > 0) {
                if (prevDeltaWheel < 0 && !(elbowWristAngleMap.get(playerID) == CharMovement.Constraints.lRotMax)) {
                    client.send(new InputMessages.StopLArm(playerID));
                } else {
                    client.send(new InputMessages.LArmDown(playerID));
                }
                prevDeltaWheel = 1;
            } else if (evt.getDeltaWheel() < 0) {
                if (prevDeltaWheel > 0 && !(elbowWristAngleMap.get(playerID) == CharMovement.Constraints.lRotMin)) {
                    client.send(new InputMessages.StopLArm(playerID));
                } else {
                    client.send(new InputMessages.LArmUp(playerID));
                }
                prevDeltaWheel = -1;
            }
        } catch (IOException ex) {
            Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    private boolean prevPressed = false;

    public void onMouseButtonEvent(MouseButtonEvent evt) {
        if (evt.isPressed() != prevPressed) {
            if (evt.isPressed()) {
                if (evt.getButtonIndex() == MouseInput.BUTTON_LEFT) {
                    try {
                        client.send(new InputMessages.RotateUArmCC(playerID));
                    } catch (IOException ex) {
                        Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else if (evt.getButtonIndex() == MouseInput.BUTTON_RIGHT) {
                    try {
                        client.send(new InputMessages.RotateUArmC(playerID));
                    } catch (IOException ex) {
                        Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else {
                try {
                    client.send(new InputMessages.StopRotateTwist(playerID));
                } catch (IOException ex) {
                    Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        prevPressed = evt.isPressed();
        inputManager.setCursorVisible(false);

        if (evt.getButtonIndex() == MouseInput.BUTTON_MIDDLE) {
            try {
                client.send(new InputMessages.StopLArm(playerID));
            } catch (IOException ex) {
                Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void onKeyEvent(KeyInputEvent evt) {
       updateShoulder();
        try {
            int key = evt.getKeyCode();
            if (!evt.isRepeating()) {
                switch (key) {
                    case KeyInput.KEY_E:
                        if (evt.isPressed()) {
                            client.send(new InputMessages.MoveCharForward(playerID));
                        } else {
                            client.send(new InputMessages.StopForwardMove(playerID));
                        }
                        break;
                    case KeyInput.KEY_S:
                        if (evt.isPressed()) {
                            client.send(new InputMessages.MoveCharLeft(playerID));
                        } else {
                            client.send(new InputMessages.StopLeftRightMove(playerID));
                        }
                        break;
                    case KeyInput.KEY_D:
                        if (evt.isPressed()) {
                            client.send(new InputMessages.MoveCharBackword(playerID));
                        } else {
                            client.send(new InputMessages.StopForwardMove(playerID));
                        }
                        break;
                    case KeyInput.KEY_F:
                        if (evt.isPressed()) {
                            client.send(new InputMessages.MoveCharRight(playerID));
                        } else {
                            client.send(new InputMessages.StopLeftRightMove(playerID));
                        }
                        break;
                    case KeyInput.KEY_W:
                        if (evt.isPressed()) {
                            client.send(new InputMessages.TurnCharLeft(playerID));
                        } else {
                            client.send(new InputMessages.StopCharTurn(playerID));
                        }
                        break;
                    case KeyInput.KEY_R:
                        if (evt.isPressed()) {
                            client.send(new InputMessages.TurnCharRight(playerID));
                        } else {
                            client.send(new InputMessages.StopCharTurn(playerID));
                        }
                        break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            client.disconnect();
        } catch (Throwable ex) {
            Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void clientConnected(Client client) {
    }

    public void clientDisconnected(Client client) {
    }

    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {

    }

    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
         
    }
    public void updateShoulder(){
        //collisionShape.removeChildShape(leftShoulder);
        //collisionShape.addChildShape(leftShoulder, character.getPhysicsLocation().add(new Vector3f(2f,2f,2f)));
    }
    public void createCollisionShape(){
       /* leftShoulder = new BoxCollisionShape(new Vector3f(2f,2f,2f));
        body_phy = new RigidBodyControl(0.1f);
        //modelMap.get(1l).addControl(body_phy);
        //bulletAppState.getPhysicsSpace().add(body_phy);
        collisionShape.addChildShape(leftShoulder,Vector3f.ZERO);
        */

    }
    //Controling collisions!
    public void collision(PhysicsCollisionEvent event) {
        //System.out.println("Collision Detected");
        Object hold = event.getObjectA();
        Object hold1 = event.getObjectB();
        if(hold.equals(terrain_phy) || hold1.equals(terrain_phy)){
          //  System.out.println("OMG ITS A MIRACLE");
        }
    //    if(hold.equals(Character.controlLArm)){
    //     System.out.println("left Arm");
    //    }
   //     if(hold.equals(Character.controlRArm)){
   //         System.out.println("Right ARm");
          //System.out.println(Character.leftShoulder.toString() + " " + hold3.getCollisionShape().toString());
          
   //     }
        
        //System.out.println(hold.hashCode() + " " + hold1.hashCode());
        //System.out.println("Obj " + event.getObjectA().toString() + " " + event.getObjectB().toString());
        //System.out.println("node " + event.getNodeA().getName() + " " + event.getNodeB().getName());
        //System.out.println(event.);
    }
}


