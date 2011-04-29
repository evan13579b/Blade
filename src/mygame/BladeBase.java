/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Plane;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializer;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;
import com.jme3.water.SimpleWaterProcessor;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import jme3tools.converters.ImageToAwt;
import mygame.messages.CharCreationMessage;
import mygame.messages.CharDestructionMessage;
import mygame.messages.CharStatusMessage;
import mygame.messages.ClientReadyMessage;
import mygame.messages.InputMessages;
import mygame.messages.SwordBodyCollisionMessage;
import mygame.messages.SwordSwordCollisionMessage;

/**
 *
 * @author blah
 */
public class BladeBase extends SimpleApplication{
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
    
    Material mat_terrain;
    Material lighting;
    Material house_mat;
    Material wall_mat;
    Material stone_mat;
    Material floor_mat;
    Material explosiveMat;
    
    private Node house;
    
    private TerrainQuad terrain;
    
    private RigidBodyControl terrain_phy;
    
    BulletAppState bulletAppState;
    
    @Override
    public void simpleInitApp() {
        Serializer.registerClass(CharStatusMessage.class);
        Serializer.registerClass(CharCreationMessage.class);
        Serializer.registerClass(CharDestructionMessage.class);
        Serializer.registerClass(ClientReadyMessage.class);
        Serializer.registerClass(SwordSwordCollisionMessage.class);
        Serializer.registerClass(SwordBodyCollisionMessage.class);
        InputMessages.registerInputClasses();
        
        bulletAppState=new BulletAppState();
        stateManager.attach(bulletAppState);
        initMaterials();
        initTerrain();
        initWater();
        
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
        rootNode.addLight(sun);

        DirectionalLight sun2 = new DirectionalLight();
        sun2.setDirection(new Vector3f(0.1f, 0.7f, 1.0f));
        rootNode.addLight(sun2);
        
        rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Skysphere.jpg", true));
    }
    
    public void initTerrain() {
        mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/TerrainLighting.j3md");
        
        mat_terrain.setBoolean("useTriPlanarMapping", false);
        mat_terrain.setBoolean("WardIso", true);
        mat_terrain.setTexture("AlphaMap", assetManager.loadTexture("Textures/alpha1.1.png"));

        Texture grass = assetManager.loadTexture("Textures/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("m_DiffuseMap", grass);
        mat_terrain.setFloat("m_DiffuseMap_0_scale", 64f);

        Texture dirt = assetManager.loadTexture("Textures/TiZeta_SmlssWood1.jpg");
        dirt.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("m_DiffuseMap_1", dirt);
        mat_terrain.setFloat("m_DiffuseMap_1_scale", 16f);
        
        Texture rock = assetManager.loadTexture("Textures/TiZeta_cem1.jpg");
        rock.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("m_DiffuseMap_2", rock);
        mat_terrain.setFloat("m_DiffuseMap_2_scale", 128f);

        Texture normalMap0 = assetManager.loadTexture("Textures/grass_normal.png");
        normalMap0.setWrap(WrapMode.Repeat);
        Texture normalMap1 = assetManager.loadTexture("Textures/dirt_normal.png");
        normalMap1.setWrap(WrapMode.Repeat);
        Texture normalMap2 = assetManager.loadTexture("Textures/road_normal.png");
        normalMap2.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("NormalMap", normalMap0);
        mat_terrain.setTexture("NormalMap_1", normalMap2);
        mat_terrain.setTexture("NormalMap_2", normalMap2);
        lighting = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        lighting.setTexture("DiffuseMap", grass);
        lighting.setTexture("NormalMap", normalMap1);
        lighting.setBoolean("WardIso", true);
               
        AbstractHeightMap heightmap = null;
        Texture heightMapImage = assetManager.loadTexture("Textures/flatland.png");
        heightmap = new ImageBasedHeightMap(
                ImageToAwt.convert(heightMapImage.getImage(), false, true, 0));
        heightmap.load();
        terrain = new TerrainQuad("my terrain", 65, 1025, heightmap.getHeightMap());
        terrain.setMaterial(mat_terrain);

        terrain.setLocalTranslation(0, -100, 0);
        terrain.setLocalScale(2f, 2f, 2f);

        rootNode.attachChild(terrain);

        terrain_phy = new RigidBodyControl(0.0f);
        terrain_phy.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
        terrain_phy.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_03);
        terrain.addControl(terrain_phy);
        bulletAppState.getPhysicsSpace().add(terrain_phy);

        house = (Node) assetManager.loadModel("Models/Cube.mesh.j3o");
        house.setLocalTranslation(0.0f, 3.0f, 70.0f);
        house.setShadowMode(ShadowMode.CastAndReceive);
        house.setLocalScale(13f);
        house.setMaterial(wall_mat);

        rootNode.attachChild(house);
        RigidBodyControl house_phy = new RigidBodyControl(0.0f);
        house_phy.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
        house_phy.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_03);
        house.addControl(house_phy);
        bulletAppState.getPhysicsSpace().add(house_phy);

        DirectionalLight light = new DirectionalLight();
        light.setDirection((new Vector3f(-0.5f, -1f, -0.5f)).normalize());
        rootNode.addLight(light);
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

        explosiveMat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        explosiveMat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flash.png"));
    }
    
    public void initWater() {
        SimpleWaterProcessor waterProcessor = new SimpleWaterProcessor(assetManager);
        waterProcessor.setReflectionScene(rootNode);
        Vector3f waterLocation = new Vector3f(0, -10, 0);
        waterProcessor.setPlane(new Plane(Vector3f.UNIT_Y, waterLocation.dot(Vector3f.UNIT_Y)));
        viewPort.addProcessor(waterProcessor);

        waterProcessor.setWaterDepth(40);
        waterProcessor.setDistortionScale(0.05f);
        waterProcessor.setWaveSpeed(0.06f);

        Quad quad = new Quad(1400, 1400);
        quad.scaleTextureCoordinates(new Vector2f(6f, 6f));

        Geometry water = new Geometry("water", quad);
        water.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X));
        water.setLocalTranslation(-200, -7, 250);
        water.setShadowMode(ShadowMode.Receive);
        water.setMaterial(waterProcessor.getMaterial());
        rootNode.attachChild(water);
    }
    
    public void createEffect(final Vector3f coords) {
        final BladeBase app=this;
        Future action = app.enqueue(new Callable() {

            public Object call() throws Exception {
                
                final ParticleEmitter explosion = new ParticleEmitter("SwordSword", ParticleMesh.Type.Triangle, 30);
                explosion.setMaterial(explosiveMat);
                rootNode.attachChild(explosion);
                explosion.setLocalTranslation(coords);
                explosion.emitAllParticles();
                TimerTask task = new TimerTask() {

                    public void run() {
                        Future action = app.enqueue(new Callable() {

                            public Object call() throws Exception {
                                rootNode.detachChild(explosion);
                                return null;
                            }
                        });

                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 1000l);
                return null;
            }
        });
    }
}
