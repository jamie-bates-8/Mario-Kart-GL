package bates.jamie.graphics.entity;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.asin;
import static java.lang.Math.atan;
import static java.lang.Math.toDegrees;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import bates.jamie.graphics.collision.Bound;
import bates.jamie.graphics.collision.OBB;
import bates.jamie.graphics.collision.Sphere;
import bates.jamie.graphics.entity.LightningStrike.BoltType;
import bates.jamie.graphics.entity.LightningStrike.RenderStyle;
import bates.jamie.graphics.io.GamePad;
import bates.jamie.graphics.io.HUD;
import bates.jamie.graphics.io.HoloTag;
import bates.jamie.graphics.item.Banana;
import bates.jamie.graphics.item.BlueShell;
import bates.jamie.graphics.item.BobOmb;
import bates.jamie.graphics.item.FakeItemBox;
import bates.jamie.graphics.item.GreenShell;
import bates.jamie.graphics.item.Item;
import bates.jamie.graphics.item.ItemRoulette;
import bates.jamie.graphics.item.ItemState;
import bates.jamie.graphics.item.RedShell;
import bates.jamie.graphics.item.Shell;
import bates.jamie.graphics.particle.ParticleGenerator;
import bates.jamie.graphics.particle.ThunderOrb;
import bates.jamie.graphics.particle.FireParticle.FireType;
import bates.jamie.graphics.scene.AnchorPoint;
import bates.jamie.graphics.scene.Camera;
import bates.jamie.graphics.scene.Light;
import bates.jamie.graphics.scene.Material;
import bates.jamie.graphics.scene.Model;
import bates.jamie.graphics.scene.Reflector;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.scene.SceneGraph;
import bates.jamie.graphics.scene.SceneNode;
import bates.jamie.graphics.scene.process.BloomStrobe;
import bates.jamie.graphics.util.Face;
import bates.jamie.graphics.util.OBJParser;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.RotationMatrix;
import bates.jamie.graphics.util.TimeQuery;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.shader.Shader;

/* TODO
 * 
 * Improve vehicle collision response with spheres
 * Driving onto a slope from the side is jittery
 * 
 * Add exhaust particles
 * 
 * Vehicle collisions could be more realistic (focus on top + bottom collisions)
 * Star collisions should be different to normal collisions
 * 
 * Combine heights calculated by height maps and bounding volumes
 * 
 * Lightning Bolts should cause players to drop items
 */

public class Vehicle
{
	/** Model Fields **/
	private static List<Face> wheel_faces;
	private static List<Face> base_faces;
	
	private static Model all_windows;
	private static Model car_body;
	private static Model head_lights;
	
	private static final float[] ORIGIN = {0, 1.8f, 0};
	
	public TerrainPatch patch;
	
	private float[]  color = {1.0f, 0.4f, 0.4f};
	private float[] windowColor = {1.0f, 1.0f, 1.0f};
	
	public boolean displayModel = true;
	public boolean displayBalloons = false;
	public int renderMode = 0;
	
	
	/** Vehicle Fields **/
	public float trajectory; 
	
	private float scale = 1.5f;
	
	private float yRotation_Wheel = 0.0f;
	private float xRotation_Wheel = 0.0f;
	
	private Vec3[] offsets_Wheel =
	{
		new Vec3(+1.75f, -0.55f, +2.4f), // back-left
		new Vec3(+1.75f, -0.55f, -2.4f), // front-left
		new Vec3(-1.75f, -0.55f, +2.4f), // back-right
		new Vec3(-1.75f, -0.55f, -2.4f)  // front-right
	}; 
	
	
	/** Motion Fields **/
	public Vec3 previousPosition = new Vec3();
	
	public float velocity = 0;
	public static final float TOP_SPEED = 1.2f;
	public float acceleration = 0.012f;
	public boolean accelerating = false;
	public boolean reversing = false;
	public boolean invertReverse = false;
	public float friction = 1;
	
	public float gravity = 0.05f;
	public boolean falling = false;
	public float fallRate = 0.0f;
	private static final float TOP_FALL_RATE = 2.5f;
	
	public float turnRate = 0;
	private static final float TOP_TURN_RATE = 2.0f;
	private float turnIncrement = 0.1f;
	public boolean turning = false;
	
	private enum Direction {STRAIGHT, LEFT, RIGHT};
	private Direction direction = Direction.STRAIGHT;
	
	private Direction drift = Direction.STRAIGHT;
	private boolean driftCounter = false;
	private enum DriftState {YELLOW, RED, BLUE};
	private DriftState driftState = DriftState.YELLOW;
	public Light[] driftLights = new Light[2];
	
	public float distance = 0;
	
	private AnchorPoint anchor;
	private Motion motionMode = Motion.DEFAULT;
	
	
	/** Collision Detection Fields **/
	public OBB bound;
	public boolean colliding = false;
	public List<Bound> collisions = new ArrayList<Bound>();
	public float[] heights = {0, 0, 0, 0};
	public boolean enableDeform = true;
	
	
	/** Particle Fields **/
	private ParticleGenerator generator = new ParticleGenerator();
	
	
	/** Item Fields **/
	private ItemRoulette roulette = new ItemRoulette();
	private ItemState itemState = ItemState.NO_ITEM;
	private Queue<Item> items = new LinkedList<Item>();
	private Queue<Integer> itemCommands = new ArrayBlockingQueue<Integer>(100);
	
	private boolean slipping = false;
	private Vec3 slipVector = new Vec3();
	public float slipTrajectory = 0;
	private int slipDuration = 0;
	
	private boolean sliding = false;
	private Vec3 slideVector = new Vec3();
	
	private boolean miniature = false;
	private int miniatureDuration = 0;
	
	private boolean cursed = false;
	private int curseDuration = 0;
	
	private boolean boosting = false;
	private boolean superBoosting = false;
	public int boostDuration = 0;
	
	private boolean starPower = false;
	private int starDuration = 0;
	private float[] starColor = {0, 1, 1};
	public Light starLight;
	
	private boolean invisible = false;
	private int booDuration = 0;
	private float booColor = 0.5f;
	private float fadeIncrement = 0.01f;
	
	public int itemDuration = 0;
	
	private enum Aim {FORWARDS, DEFAULT, BACKWARDS};
	private Aim aiming = Aim.DEFAULT;
	
	/** Scene Fields **/
	private Scene scene;
	public Camera camera;
	
	/** Controller Fields **/
	private GamePad controller;
	
	private HUD hud;
	private HoloTag tag;
	
	public boolean smooth = false;
	
	private SceneGraph graph;
	
	public Reflector reflector;
	
	public Balloon[] balloons = new Balloon[3];
	
	
	public Vehicle(GL2 gl, Vec3 c, float xrot, float yrot, float zrot, Scene scene)
	{
		if(car_body == null)
		{
			wheel_faces = OBJParser.parseTriangles("new_wheel");
			base_faces  = OBJParser.parseTriangles("car_base");
			
			all_windows = OBJParser.parseTriangleMesh("windows_all");
			car_body    = OBJParser.parseTriangleMesh("car_body");

			head_lights = OBJParser.parseTriangleMesh("head_lights");
		}
	    
	    bound = new OBB(
				c.x, c.y, c.z,
	    		xrot, yrot, zrot,
	    		2.7f, 2.0f, 5.5f);
	    
	    trajectory = yrot;
	    
	    this.scene = scene; 
	    camera = new Camera();
	    
	    controller = new GamePad();
	    
	    hud = new HUD(scene, this);
	    tag = new HoloTag(String.format("(%+3.2f, %+3.2f, %+3.2f)", c.x, c.y, c.z), c);
	    
	    anchor = new AnchorPoint();
	    
	    reflector = new Reflector(0.75f, 320, true);
	    
	    balloons[0] = new Balloon(c);
	    balloons[1] = new Balloon(c);
	    balloons[2] = new Balloon(c);
	    
	    setupLights(gl);
	    resetGraph();
	}

	private void setupLights(GL2 gl)
	{
		Vec3 c = getPosition();
		
		starLight = new Light(gl, c, RGB.WHITE, RGB.WHITE, RGB.WHITE);
	    starLight.setConstantAttenuation (0.80f);
	    starLight.setQuadraticAttenuation(0.04f);
	    starLight.enableAttenuation = true;
	    
	    Light leftLight, rightLight;
	    float[] sparkColor = RGB.YELLOW;
	    leftLight = new Light(gl, c, sparkColor, sparkColor, sparkColor);
	    leftLight.setConstantAttenuation (1.00f);
	    leftLight.setLinearAttenuation   (0.20f);
	    leftLight.setQuadraticAttenuation(0.20f);
	    leftLight.enableAttenuation = true;
	    leftLight.disable(gl);
	    
	    rightLight = new Light(gl, c, sparkColor, sparkColor, sparkColor);
	    rightLight.setConstantAttenuation (1.00f);
	    rightLight.setLinearAttenuation   (0.20f);
	    rightLight.setQuadraticAttenuation(0.20f);
	    rightLight.enableAttenuation = true;
	    rightLight.disable(gl);
	    
	    driftLights[0] = leftLight;
	    driftLights[1] = rightLight;
	}
	
	public boolean enableChrome = true;
	
	public void setupGraph()
	{
		Material shiny = new Material(new float[] {1, 1, 1});
		Material mat = new Material(new float[] {0, 0, 0});
		
		SceneNode body = new SceneNode(null, -1, car_body, SceneNode.MatrixOrder.T_M_S, shiny);
		body.setColor(color);
		body.setTranslation(bound.c);
		body.setOrientation(bound.u.toArray());
		body.setScale(new Vec3(scale));
		body.setReflector(reflector);
		
		if(enableChrome)
		{
			body.setRenderMode(SceneNode.RenderMode.REFLECT);
			body.setReflectivity(0.75f);
		}
		else body.setRenderMode(SceneNode.RenderMode.COLOR);
		
		graph = new SceneGraph(body);
		
		for(int i = 0; i < 4; i++)
		{
			SceneNode wheel = new SceneNode(wheel_faces, -1, null, SceneNode.MatrixOrder.T_RY_RX_RZ_S, mat);
			wheel.setColor(new float[] {1, 1, 1});
			wheel.setRenderMode(SceneNode.RenderMode.TEXTURE);
			wheel.setTranslation(offsets_Wheel[i]);
			wheel.setRotation(new Vec3());
			wheel.setScale(new Vec3(0.6f));
			
			body.addChild(wheel);
		}
		
		
		SceneNode headlights = new SceneNode(null, -1, head_lights, SceneNode.MatrixOrder.T, shiny);
		headlights.setColor(new float[] {0.6f, 0.6f, 1.0f});
		headlights.setTranslation(new Vec3());
		headlights.setRenderMode(SceneNode.RenderMode.COLOR);
		
		body.addChild(headlights);
		
		SceneNode car_base = new SceneNode(base_faces, -1, null, SceneNode.MatrixOrder.T, shiny);
		car_base.setColor(new float[] {1, 1, 1});
		car_base.setTranslation(new Vec3());
		car_base.setRenderMode(SceneNode.RenderMode.TEXTURE);
		
		body.addChild(car_base);
		
		SceneNode windows = new SceneNode(null, -1, all_windows, SceneNode.MatrixOrder.NONE, shiny);
		windows.setColor(windowColor);
		windows.setRenderMode(SceneNode.RenderMode.GLASS);
		
		body.addChild(windows);
		
		
	}
	
	public void updateGraph()
	{
		SceneNode car_body = graph.getRoot();
		
		car_body.setTranslation(bound.c);
		car_body.setOrientation(bound.u.toArray());
		car_body.setScale(new Vec3(scale));
		
		for(int i = 0; i < 4; i++)
		{
			SceneNode wheel = car_body.getChildren().get(i);
			
			// only front wheels can turn left/right
			if(i % 2 != 0) wheel.setRotation(new Vec3(xRotation_Wheel, yRotation_Wheel, 0));
			else           wheel.setRotation(new Vec3(xRotation_Wheel, 0, 0));
		}
	}
	
	public HUD getHUD() { return hud; }
	
	public Bound getBound() { return bound; }
	
	public void collide(Vehicle car) { collisions.add(car.getBound()); }
	
	public Queue<Item> getItems() { return items; }
	
	public Queue<Integer> getItemCommands() { return itemCommands; }
	
	private void useItem()
	{
		if(hasItem())
		{
			pressItem();
			if(!roulette.secondary) roulette.update();
		}
		
		else if(roulette.hasItem())
		{
			int itemID = roulette.getItem();
			roulette.secondary = false;
			ItemState state = ItemState.get(itemID);
			setItemState(state);
			
			itemCommands.add(itemID);
			
			if(ItemState.isTimed(state)) roulette.setTimer();
			
			if(ItemState.isInstantUse(state)) pressItem();
			roulette.update();
		}
	}
	
	public ItemRoulette getRoulette() { return roulette; }
	
	public RotationMatrix getRotation() { return bound.u; }
	
	public void aimForwards()  { aiming = Aim.FORWARDS;  }
	
	public void aimBackwards() { aiming = Aim.BACKWARDS; }
	
	public void resetAim()     { aiming = Aim.DEFAULT;   }
	
	public void removeItems()
	{
		for(int i = 0; i < Item.removeItems(items); i++)
		{
			itemState = ItemState.press(itemState);
			itemState = ItemState.release(itemState);
		}
	}
	
	public boolean hasItem() { return itemState != ItemState.NO_ITEM; }

	public ItemState getItemState() { return itemState; }
		
	public void setItemState(ItemState state) { this.itemState = state; }
	
	public void registerItem(GL2 gl, int itemID)
	{
		switch(itemID)
		{
			case  0: items.add(new GreenShell(gl, scene, this, trajectory, false)); break;
			case  1:
			{
				items.add(new GreenShell(gl, scene, this, trajectory,       true));
				items.add(new GreenShell(gl, scene, this, trajectory + 120, true));
				items.add(new GreenShell(gl, scene, this, trajectory - 120, true));

				break;
			}
			case  2: items.add(new RedShell(gl, scene, this, trajectory, false)); break;
			case  3:
			{
				items.add(new RedShell(gl, scene, this, trajectory,       true));
				items.add(new RedShell(gl, scene, this, trajectory + 120, true));
				items.add(new RedShell(gl, scene, this, trajectory - 120, true));

				break;
			}
			case  6: itemDuration = 400; break;
			case  7: items.add(new FakeItemBox(gl, scene, this)); break;
			case  8: items.add(new Banana(gl, scene, this, 1)); break;
			case  9:
			{
				items.add(new Banana(gl, scene, this, 3));
				items.add(new Banana(gl, scene, this, 2));
				items.add(new Banana(gl, scene, this, 1));
				break;
			}
			case 13:
			{
				BlueShell shell = new BlueShell(gl, scene, this, trajectory);
					
				shell.throwUpwards();
					
				scene.addItem(shell);
				break;
			}
			case 14:
			{
				BobOmb bomb = new BobOmb(new Vec3(), this, false);
				
				bomb.throwUpwards();
					
				scene.addItem(bomb);
				break;
			}
			default: break;
		}
	}
	
	public void pressItem()
	{
		switch(itemState)
		{
			case THREE_ORBITING_GREEN_SHELLS:
			case TWO_ORBITING_GREEN_SHELLS:
			case ONE_ORBITING_GREEN_SHELL:
			case THREE_ORBITING_RED_SHELLS:
			case TWO_ORBITING_RED_SHELLS:
			case ONE_ORBITING_RED_SHELL:
			{			
				Shell shell = (Shell) items.remove();
					
				switch(aiming)
				{
					case FORWARDS:
					case DEFAULT:   shell.throwForwards();  break;
					case BACKWARDS: shell.throwBackwards(); break;
				}
					
				scene.addItem(shell);
				break;
			}
			
			case ONE_MUSHROOM:
			case TWO_MUSHROOMS:
			case THREE_MUSHROOMS: boost(); break;

			case GOLDEN_MUSHROOM: superBoosting = true; break;
			
			case ONE_BANANA:
			case TWO_BANANAS:
			case THREE_BANANAS:
			{
				Banana banana = (Banana) items.remove();
					
				switch(aiming)
				{
					case FORWARDS: banana.throwUpwards(); break;
					default: break;
				}
					
				scene.addItem(banana);
				break;
			}
			
			case LIGHTNING_BOLT: useLightningBolt(); break;
			case POWER_STAR: usePowerStar(); break;
			case BOO: useBoo(); break;
			
			default: break;
		}
		
		itemState = ItemState.press(itemState);
	}

	public void releaseItem()
	{
		switch(itemState)
		{
			case HOLDING_GREEN_SHELL:
			case HOLDING_RED_SHELL:
			{			
				Shell shell = (Shell) items.remove();
				
				switch(aiming)
				{
					case FORWARDS:
					case DEFAULT:   shell.throwForwards();  break;
					case BACKWARDS: shell.throwBackwards(); break;
				}
					
				scene.addItem(shell);
				break;
			}
			
			case FAKE_ITEM_BOX:
			case HOLDING_BANANA:
			{
				Item item = items.remove();
					
				switch(aiming)
				{
					case FORWARDS: item.throwUpwards(); break;
					default: break;
				}
						
				scene.addItem(item);
				break;
			}

			case GOLDEN_MUSHROOM: superBoosting = false; break;
			
			default: break;
		}
		
		itemState = ItemState.release(itemState);
	}
	
	public float getThrowVelocity() { return TOP_SPEED * 1.5f + abs(velocity); }

	public void setRotation(float x, float y, float z) { bound.u = new RotationMatrix(x, y, z); }
	
	public Vec3 getForwardVector() { return bound.u.zAxis.multiply(velocity); }
	
	public Vec3 getSlipVector() { return bound.c.subtract(slipVector.multiply(velocity)); }
	
	public void setRotation(Vec3 angles) { bound.u = new RotationMatrix(angles.x, angles.y, angles.z); }
	
	public void setPosition(Vec3 c) { bound.setPosition(c); }
	
	public Vec3 getPosition() { return bound.getPosition(); }
	
	public float[] getColor() { return color; }
	
	public void setColor(float[] color)
	{
		this.color = color;
		resetGraph();
	}
	
	public void resetGraph()
	{
		setupGraph();
	}
	
	public boolean enableAberration = true;
	public float opacity = 0.25f;
	
	private TimeQuery timeQuery = new TimeQuery(TimeQuery.VEHICLE_ID);

	public void render(GL2 gl)
	{
		if(!Scene.shadowMode && !Scene.reflectMode && !Scene.depthMode) updateColor();
		updateLights(gl);
		
		timeQuery.getResult(gl);
		timeQuery.begin(gl);
		
		if(renderMode == 1)
		{
			if(smooth)
			{
				gl.glEnable(GL2.GL_BLEND);
				gl.glEnable(GL2.GL_LINE_SMOOTH);
			}
			
			gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
		}
		
		boolean useHDR = BloomStrobe.isEnabled();
		
		if(displayModel)
		{
			Light.saveRimState();
			
			if(cursed)
			{
				scene.rimLighting = true;
				
				Light.rimPower = 3.0f;
				Light.rimColor = new float[] {.7f, .0f, .0f};
				
				Light.setepRimLighting(gl);
			}
			else if(miniature)
			{
				BloomStrobe.end(gl);
				
				scene.rimLighting = true;
				
				Light.rimPower = 1.0f;
				Light.rimColor = Scene.sceneTimer % 10 < 5 ? RGB.BLUE : RGB.INDIGO;
				
				Light.setepRimLighting(gl);
			}
			
			if(invisible)
			{
				String name = enableAberration ? "aberration" : "ghost";
				Shader shader = Shader.get(name);
				
				if(shader != null)
				{
					shader.enable(gl);
					float fade = opacity + (1 - opacity) * (booColor * 2);
					shader.setUniform(gl, "opacity", fade);
					Shader.disable(gl);
				}

				graph.renderGhost(gl, booColor, shader);
			}
			else if(starPower) graph.renderColor(gl, starColor, enableChrome ? reflector : null);
			else graph.render(gl);
			
			Light.restoreRimState(gl);
		}
		
		if(useHDR) BloomStrobe.begin(gl);
		
		if(bolt != null) 				  bolt.render(gl, camera.getOrientation().zAxis);
		if(orb  != null && !orb.isDead())  orb.render(gl, camera.getOrientation().zAxis);
		
		gl.glColor3f(1, 1, 1);
		
		for(Item item : items)
		{
			if(Item.renderMode == 1) item.renderWireframe(gl, trajectory);
			else item.render(gl, trajectory);
		}
		
		if(!camera.isFirstPerson() && displayBalloons) renderBalloons(gl);
		
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
		
		gl.glDisable(GL2.GL_BLEND);
		gl.glDisable(GL2.GL_LINE_SMOOTH);
		
		if(displayTag && !camera.isFirstPerson())
		{
			float ry = slipping ? slipTrajectory : trajectory; 
			if(camera.isFree()) ry = camera.getRotation().y;
			
			tag.render(gl, ry);
		}
		
		timeQuery.end(gl);
	}

	private void renderBalloons(GL2 gl)
	{
		Vec3 p = getPosition();
		
		for(int i = 0; i < balloons.length; i++)
		{
			float r = 0;
			if(i == 1) r = -30;
			if(i == 2) r = +30;
			
			gl.glPushMatrix();
			{
				gl.glTranslatef(p.x, p.y, p.z);
				gl.glRotatef(trajectory, 0, -1, 0);
				gl.glRotatef(r, 0, 0, 1);
				gl.glTranslatef(0, 4, 0);
				gl.glScalef(1.5f, 1.5f, 1.5f);
				
				balloons[i].render(gl);
			}
			gl.glPopMatrix();
		}
	}

	private void updateLights(GL2 gl)
	{
		if(miniature)
		{
			starLight.setAmbience(RGB.BRIGHT_YELLOW);
			starLight.setConstantAttenuation (0.80f * Scene.sceneTimer % 5);
		}
		else
		{
			starLight.setAmbience(starColor);
			starLight.setConstantAttenuation (0.80f);
		}
		
		if(starDuration > 0 || miniature) starLight.enable(gl);
		else starLight.disable(gl);
		
		float[] driftColor = RGB.YELLOW;
		
		switch(driftState)
		{
			case YELLOW : driftColor = RGB.YELLOW; break;
			case RED    : driftColor = RGB.RED;    break;
			case BLUE   : driftColor = RGB.BLUE;   break;
		}
		
		if(drift != Direction.STRAIGHT && !reversing && velocity > 0 && !miniature) for(Light l : driftLights)
		{
			l.enable(gl);
			l.setAmbience(driftColor);
			l.setSpecular(driftColor);
			l.setDiffuse (driftColor);
		}
		else for(Light l : driftLights) l.disable(gl);
	}
	
	public boolean displayTag = false;

	private void updateColor()
	{
		if(starPower)
		{
			starColor = RGB.HSVToRGB(new float[] {Scene.sceneTimer * 0.01f, 1, 1});
		}
		if(invisible)
		{
			if(booColor > 0 && booDuration > 0) booColor -= fadeIncrement;
		}
	}

	public long renderHUD(GL2 gl, GLU glu) { return hud.render(gl, glu); }

	public float[] getHeights()
	{
		falling = true;
		
		float h = Float.MAX_VALUE;
		
		for(int i = 0; i < heights.length; i++) if(heights[i] < h) h = heights[i];
		for(int i = 0; i < heights.length; i++) heights[i] -= h;
		
		for(int i = 0; i < heights.length; i++)
			if(fallRate < TOP_FALL_RATE * 0.1) heights[i] -= 1;
		
		if(collisions.isEmpty() && fallRate > TOP_FALL_RATE * 0.1)
			for(int i = 0; i < heights.length; i++) heights[i] *= 0.9;
		
		for(Bound collision : collisions)
			if(collision instanceof OBB) setHeights((OBB) collision);
		
		return heights;
	}
	
	public float[] getHeights(Quadtree tree, int lod)
	{
		Vec3[] vertices = bound.getVertices();
		
		long start = System.nanoTime();
		
		for(int i = 0; i < 4; i++)
		{
			float[] vertex = vertices[i].toArray();
			
			Quadtree cell = tree.getCell(vertex, lod);
			heights[i] = (cell != null) ? cell.getHeight(vertex) : 0;
		}
		
		if(accelerating && enableDeform)
		{	
			float ratio = abs(velocity / TOP_SPEED);
			float k = ratio > 0.5 ? 0.5f : 1 - ratio;
			      k = ratio < 0.1 ? 0.1f : k;
			float depression = k * 0.05f;
			
			for(int i = 0; i < 4; i++)
			{
				float[] vertex = vertices[i].toArray();
				
				Quadtree cell = tree.getCell(vertex, Quadtree.MAXIMUM_LOD);
				if(cell != null) cell.subdivide();
				tree.deform(vertex, 1.5f, -depression);
			}
		}
		
		scene.updateTimes[Scene.frameIndex][2] = System.nanoTime() - start;
		scene.updateTimes[Scene.frameIndex][3] = 0;
		
		float h = (heights[0] + heights[1] + heights[2] + heights[3]) / 4;
		h += bound.e.y;
		bound.c.y = h;
		
		return heights;
	}
	
	public float[] getHeights(Collection<Quadtree> trees)
	{
		Vec3[] vertices = bound.getVertices();
		
		long start = System.nanoTime();
		
		Quadtree[] _trees = new Quadtree[4];
		
		for(int i = 0; i < 4; i++)
		{
			float max = Integer.MIN_VALUE;
			float[] vertex = vertices[i].toArray();
			
			for(Quadtree tree : trees)
			{
				Quadtree cell = tree.getCell(vertex, tree.detail);
				float h = (cell != null) ? cell.getHeight(vertex) : 0;
				if(h > max && !tree.enableBlending) { max = h; _trees[i] = tree; }
			}

			Water water = scene.water;
			
			if(water.frozen && !water.magma)
			{
				heights[i] = max > 0 ? max : 0;
				sliding = max <= 0;
			}
			else heights[i] = max;
		}
		
		if(accelerating && enableDeform)
		{		
			float ratio = abs(velocity / TOP_SPEED);
			float k = ratio > 0.5 ? 0.5f : 1 - ratio;
			      k = ratio < 0.1 ? 0.1f : k;
			float depression = k * 0.05f;
			
			for(int i = 0; i < 4; i++)
			{
				float[] vertex = vertices[i].toArray();
				
				Quadtree cell = _trees[i].getCell(vertex, Quadtree.MAXIMUM_LOD);
				if(cell != null) cell.subdivide();
				_trees[i].deform(vertex, 1.5f, -depression);
			}
		}
		
		scene.updateTimes[Scene.frameIndex][2] = System.nanoTime() - start;
		scene.updateTimes[Scene.frameIndex][3] = 0;
		
		float h = (heights[0] + heights[1] + heights[2] + heights[3]) / 4;
		
		if(bound.c.y - bound.getMaximumExtent() <= h)
		{
			h += bound.e.y;
			bound.c.y = h;
			
			falling  = false;
			fallRate = 0;
		}
		else falling = true;
		
		return heights;
	}
	
	public float[] getHeights(Terrain map)
	{
		if(map.enableQuadtree) return getHeights(map.trees.values());
		
		Vec3[] vertices = bound.getVertices();

		for(int i = 0; i < 4; i++)
		{
			float[] vertex = vertices[i].toArray();
			
			float h = map.getHeight(vertex);
			heights[i] = h;
		}
		
		float h = (heights[0] + heights[1] + heights[2] + heights[3]) / 4;
		h += bound.e.y;
		bound.c.y = h;
		
		return heights;
	}
	
	// TODO Gravity causes car to pass through surfaces (example: bridges)

	/**
	 * This method calculates the height of the vehicles as determined by the OBB
	 * passed as a parameter; note that it is assumed that the vehicle is colliding
	 * with the top of the OBB (see resolveOBB(OBB) for side and bottom collisions)
	 */
	private void setHeights(OBB obb)
	{
		Vec3 face = obb.getFaceVector(getPosition());

		//if the side of collision is the upwards face
		if(face.equals(obb.getUpVector(1)))
		{
			Vec3[] vertices = bound.getVertices();

			// calculate the height at each wheel
			for(int i = 0; i < 4; i++)
			{
				float h = obb.closestPointOnPerimeter(vertices[i]).y;
				if(h > heights[i]) heights[i] = h;
			}

			// calculate the height at the centre of the vehicle
			float h = obb.closestPointOnPerimeter(getPosition()).y
					+ (bound.e.y * 0.9f);
			
			if(h > bound.c.y)
			{
				bound.c.y = h;
				
				if(motionMode == Motion.ANCHOR)
					anchor.getPosition().y = h;
			}

			falling = false; fallRate = 0; // disable falling
		}
	}
	
	public Vec3 getRotationAngles(float[] h)
	{
		float frontHeight = (h[0] + h[1]) / 2; 
		float backHeight  = (h[2] + h[3]) / 2;
		float leftHeight  = (h[1] + h[3]) / 2;
		float rightHeight = (h[0] + h[2]) / 2;
	
		float xrot = (float) toDegrees(atan((backHeight - frontHeight) / (bound.e.z * 2)));
		float zrot = (float) toDegrees(atan((leftHeight - rightHeight) / (bound.e.x * 2)));
		
		return new Vec3(xrot, trajectory, zrot);
	}

	public void detectCollisions()
	{
		colliding = false;
		
		collisions.clear();
		
		for(Bound collision : scene.getBounds())
		{
			if(bound.testBound(collision))
			{
				colliding = true;
				collisions.add(collision);
			}
		}
		
		//TODO car-car collisions could be improved
		for(Vehicle car : scene.getCars())
		{
			if(!car.equals(this) && !invisible && !car.isInvisible() &&
					bound.testBound(car.getBound()))
			{
				colliding = true;
				collisions.add(car.getBound());
			}
		}
	}

	private void resolveCollisions()
	{
		List<Vec3> vectors = new ArrayList<Vec3>();
		
		for(Bound collision : collisions)
		{
			if     (collision instanceof OBB   ) vectors.add(resolveOBB   ((OBB)    collision));
			else if(collision instanceof Sphere) vectors.add(resolveSphere((Sphere) collision));
		}
		
		for(Vec3 vector : vectors) setPosition(getPosition().add(vector));
		
		if(motionMode == Motion.ANCHOR) anchor.setPosition(getPosition());
	}

	/*
	 * TODO
	 * 
	 * There are currently no obstacles in the scene that use a sphere as its
	 * bounding geometry; therefore, this method is not used and may be inaccurate
	 * 
	 */
	private Vec3 resolveSphere(Sphere sphere)
	{
		Vec3 face = sphere.getFaceVector(getPosition());
		face.y = 0;
		
		float s = bound.getPenetration(sphere);
		
		face = face.subtract(sphere.getPosition());
		face = face.normalize().multiply(s);

		velocity *= (slipping) ? 0 : 0.9;

		return face;
	}

	/**
	 * This method resolves a collision with an OBB passed as a parameter;
	 * note that it is assumed that the vehicle is colliding with the side or
	 * bottom of the OBB (see setHeights(OBB) for top collisions) 
	 */
	private Vec3 resolveOBB(OBB obb)
	{
		Vec3 face = obb.getFaceVector(getPosition());
		
		/*
		 * the vehicles must be colliding with the side or bottom of the OBB
		 * the face must be a valid collision (not all sides are considered)
		 * the bottom of the vehicle must be lower than the top of the OBB
		 */
		if(!face.equals(obb.getUpVector(1)) && obb.isValidCollision(face)
		   && (getPosition().y - bound.e.y) < (obb.c.y + obb.e.y))
		{
			float p = bound.getPenetration(obb);
			
			if(slipping) velocity = 0;
			else velocity *= 0.9;
			
			return bound.getCollisionVector(obb).multiply(p);
		}
		else return new Vec3();
	}

	public void update()
	{
		previousPosition = getPosition();
		
		if(scene.enableTerrain) getHeights(scene.getTerrain());
		else getHeights();
		
		if(motionMode == Motion.ANCHOR)
		{
			setRotation(anchor.getRotation());
			trajectory = anchor.getRotation().y;
		}
		else setRotation(getRotationAngles(heights));
		
		if(motionMode == Motion.ANCHOR) setPosition(anchor.getPosition());
		else if(!slipping) setPosition(getPositionVector());
		else setPosition(getSlipVector());
		
		starLight.setPosition(getPosition());
		
		detectCollisions();
		if(colliding) resolveCollisions();
		
		if(superBoosting && itemDuration > 0 && !slipping) boost();
		
		if(accelerating && !slipping) accelerate();
		else decelerate();

		if(velocity <= 0 || slipping)
		{
			drift = Direction.STRAIGHT;
			driftState = DriftState.YELLOW;
			driftCounter = false;
		}
		
		if     (drift == Direction.LEFT ) turnLeft();
		else if(drift == Direction.RIGHT) turnRight();
		
		else if(turning && !slipping)
		{
			if(reversing && invertReverse)
			{
				if(direction == Direction.RIGHT) turnLeft();
				else turnRight();
			}
			else
			{
				if(direction == Direction.LEFT ) turnLeft();
				else turnRight();
			}
		}
		
		else stabilize();
		
		float currentDistance = distance;
		
		if(falling && motionMode != Motion.ANCHOR) fall();
	
		velocity = (velocity > 2 * TOP_SPEED) ? (2 * TOP_SPEED) : velocity;
		
		distance += velocity;
		
		turnWheels();
		
		// The wheels are rotated in relation to the distance travelled
		xRotation_Wheel -= 360 * (distance - currentDistance) / (2 * PI * 0.5); // 0.5 is the wheel radius
		
		if(drift != Direction.STRAIGHT && !falling && !reversing)
		{
			Vec3[] driftVectors = getDriftVectors();
			
			for(Vec3 vector : driftVectors)
			{
				Vec3 source = vector.add(getPosition());
				
				scene.addParticles(generator.generateDriftParticles(source, 10, driftState.ordinal(), miniature));
				if(Scene.sceneTimer % 2 == 0)
					scene.addParticles(generator.generateSparkParticles(source, vector, 1, driftState.ordinal(), this));
			}
			
			driftLights[0].setPosition(driftVectors[0].add(getPosition()));
			driftLights[1].setPosition(driftVectors[1].add(getPosition()));
		}
		
		if(scene.enableTerrain && !scene.getTerrain().enableQuadtree && patch != null && velocity != 0)
		{
			for(Vec3 source : getDriftVectors())
			{
				scene.addParticles(generator.generateTerrainParticles(source, 15, patch.texture));
			}
		}
		
		Vec3 p = new Vec3(bound.c.x, bound.c.y + 5, bound.c.z);
		
		tag.setPosition(p);
		tag.displayPosition();
		
		updateGraph();
		updateStatus();
		updateController();
	}
	
	public void updateController()
	{
		if(!controller.isNull() && controller.isEnabled() && !camera.isFree())
		{
			if(motionMode == Motion.ANCHOR)
			{
				controller.update();
				anchor.update(controller);
			}
			else if(controller.getXAxis() > (isDrifting() ?  0.9f : 0)) steerLeft();
			else if(controller.getXAxis() < (isDrifting() ? -0.9f : 0)) steerRight();
			else 
			{
				turning = false;
				straighten();
			}
		}
		
		if(!controller.isNull())
		{
			controller.update();
			
			while(!controller.getPressEvents().isEmpty()) buttonPressed(controller.getPressEvents().poll());
			while(!controller.getReleaseEvents().isEmpty()) buttonReleased(controller.getReleaseEvents().poll());
		}
	}

	/**
	 * This method updates the status effects currently inflicted on the player;
	 * these effects are caused by using or collising with certain items
	 */
	private void updateStatus()
	{
		if(miniatureDuration > 0) miniatureDuration--;
		else if(miniature)
		{
			miniature = false;
			bound.e = bound.e.multiply(2);
			scale *= 2;
			graph.getRoot().setScale(new Vec3(scale));
		}
		
		if(boostDuration > 0)
		{
			scene.focalBlur.enableRadial = true;
			scene.focalBlur.blurFactor = (float) boostDuration / 60.0f;
			scene.focalBlur.blurCentre = getPosition();
			boostDuration--;
		}
		else boosting = superBoosting = false;
		
		if(boosting)
		{
			Vec3[] sources = getBoostVectors();
			
			for(int i = 0; i < sources.length; i++)
				scene.addParticles(generator.generateFireParticles(sources[i], 5, getForwardVector(),
						this, i, superBoosting ? FireType.BLUE : FireType.RED));
		}
		
		if(curseDuration > 0) curseDuration--;
		else cursed = false;
		
		if(cursed && Scene.sceneTimer % 2 == 0)
			scene.addParticles(generator.generateFakeItemBoxParticles(getPosition(), 1, miniature, this));
		
		if(slipDuration > 0) slipDuration--;
		else slipping = false;
		
		if(slipping) trajectory += 15;
		
		if(starDuration > 0) starDuration--;
		else
		{
			starPower = false;
			turnIncrement = 0.1f;
		}
		
		if(starPower && Scene.sceneTimer % 5 == 0)
			scene.addParticles(generator.generateSparkleParticles(getPosition(), 2, miniature, this));
		
		if(booDuration > 0) booDuration--;
		else if(booColor < 0.5f) booColor += 0.0125f;
		else invisible = false;
		
		if(itemDuration > 0) itemDuration--;
		else if(ItemState.isTimed(itemState))
		{
			itemState = ItemState.NO_ITEM;
		}
	}

	private void fall()
	{
		if(fallRate < TOP_FALL_RATE) fallRate += gravity;
		bound.c.y -= fallRate;
	}
	
	public void drift() { drift = direction; }
	
	public void miniTurbo()
	{
		drift = Direction.STRAIGHT;
		
		if(driftState == DriftState.BLUE)
		{
			boosting = true;
			scene.focalBlur.enableRadial = true;
			boostDuration = 20;
			velocity += 0.6;
			
			velocity = (velocity > 2 * TOP_SPEED) ? (2 * TOP_SPEED) : velocity; 
		}
		
		driftState = DriftState.YELLOW;
	}

	/**
	 * Increase the speed of the car unless it is at its top speed
	 */
	public void accelerate()
	{
		if(reversing) velocity += (velocity < -TOP_SPEED) ? acceleration : (velocity > 0 ? -acceleration * 2 : -acceleration);
		else          velocity += (velocity <  TOP_SPEED) ? (velocity < 0 ? acceleration * 2 : acceleration) : -acceleration;
	}

	/**
	 * Decrease the speed of the car until it comes to a standstill
	 */
	public void decelerate()
	{
		if(velocity > acceleration) velocity -= acceleration;
		else if(velocity < 0) velocity += acceleration;
		else velocity = 0;
	}

	public void steerLeft()
	{
		turning = true;
		direction = Direction.LEFT;
		
		if(!falling)
		{
			if(drift == Direction.RIGHT) driftCounter = true;
			else if(drift == Direction.LEFT && driftCounter && driftState != DriftState.BLUE)
			{
				driftState = DriftState.values()[driftState.ordinal() + 1];
				driftCounter = false;
			}
		}
	}

	public void steerRight()
	{
		turning = true;
		direction = Direction.RIGHT;
		
		if(!falling)
		{
			if(drift == Direction.LEFT) driftCounter = true;
			else if(drift == Direction.RIGHT && driftCounter && driftState != DriftState.BLUE)
			{
				driftState = DriftState.values()[driftState.ordinal() + 1];
				driftCounter = false;
			}
		}
	}

	private void turnLeft()
	{
		if(!controller.isEnabled() || controller.getXAxis() >= 0)
		{
			if(turnRate > -TOP_TURN_RATE) turnRate -= turnIncrement;
		}
		else
		{
			if(turnRate > -TOP_TURN_RATE * controller.getXAxis()) turnRate -= turnIncrement;
		}
		
		float k = 1; 
		
		if(drift == Direction.LEFT)
		{
			if(direction == Direction.LEFT) k = 1.25f;
			else if(direction == Direction.RIGHT) k = 0.5f;
		}
		
		if(sliding) k *= 0.75;
		
		if(velocity != 0 || (sliding && !slideVector.isZeroVector())) trajectory += turnRate * k;
	}

	private void turnRight()
	{
		if(!controller.isEnabled() || controller.getXAxis() <= 0)
		{
			if(turnRate < TOP_TURN_RATE) turnRate += turnIncrement;
		}
		else
		{
			if(turnRate < TOP_TURN_RATE * controller.getXAxis()) turnRate += turnIncrement;
		}
		
		float k = 1;
		
		if(drift == Direction.RIGHT)
		{
			if(direction == Direction.LEFT) k = 0.5f;
			else if(direction == Direction.RIGHT) k = 1.25f;
		}
		
		if(sliding) k *= 0.75;
		
		if(velocity != 0 || (sliding && !slideVector.isZeroVector())) trajectory += turnRate * k;
	}
	
	public void straighten() { direction = Direction.STRAIGHT; }

	public void stabilize()
	{
		if(turnRate > turnIncrement) turnRate -= turnIncrement;
		else if(turnRate < 0) turnRate += turnIncrement;
		else turnRate = 0;
		
		trajectory += turnRate * (velocity != 0 ? 1 : 0);
	}

	public void turnWheels()
	{
		float turnRatio = -turnRate / TOP_TURN_RATE;
	
		     if(turnRatio > +1) turnRatio = +1;
		else if(turnRatio < -1) turnRatio = -1;
	
		yRotation_Wheel = (float) (toDegrees(asin(turnRatio)) / 2);
		if(velocity < 0) yRotation_Wheel = -yRotation_Wheel; // TODO
	}

	public Vec3 getPositionVector()
	{
		float _velocity = (miniature) ?  velocity * 0.75f :  velocity;
		      _velocity = (starPower) ? _velocity * 1.25f : _velocity;
		      
		Vec3 vector = bound.u.zAxis.multiply(_velocity * friction);
		Vec3 p = bound.c.subtract(vector);
		
		if(sliding)
		{
			acceleration = 0.006f;
			
			float k = 0.05f + Math.abs(turnRate / TOP_TURN_RATE) * 0.10f;
			
			slideVector = slideVector.subtract(vector);
		    slideVector = slideVector.multiply(k);
			
			p = p.add(slideVector);
		}
		else acceleration = 0.012f;
		
		return p;
	}
	
	public Vec3 getVector()
	{
		float _velocity = (miniature) ?  velocity * 0.75f :  velocity;
		      _velocity = (starPower) ? _velocity * 1.25f : _velocity;
		
		return bound.u.zAxis.multiply(_velocity * friction);
	}

	public Vec3[] getBoostVectors()
	{	
		Vec3 eu0 = bound.u.xAxis.multiply(bound.e.x * 0.60f);
		Vec3 eu2 = bound.u.zAxis.multiply(bound.e.z * 1.10f);
		
		return new Vec3[]
		{
			getPosition().subtract(eu0).add(eu2), // right exhaust
			getPosition().     add(eu0).add(eu2)  // left exhaust
		};
	}
	
	public Vec3[] getLightVectors()
	{	
		Vec3 v = bound.u.zAxis.multiply(bound.e.z);
		
		Vec3[] boost = getBoostVectors();
		
		return new Vec3[]
		{
			getPosition().subtract(v),
			bound.c.subtract(boost[0])
		};
	}
	
	public Vec3[] getDriftVectors()
	{
		Vec3 eu0 = bound.u.xAxis.multiply(bound.e.x * 1.20f);
		Vec3 eu1 = bound.u.yAxis.multiply(bound.e.y * 0.75f);
		Vec3 eu2 = bound.u.zAxis.multiply(bound.e.z * 0.75f);
		
		return new Vec3[]
		{
			eu2.subtract(eu1).subtract(eu0),
			eu2.subtract(eu1).     add(eu0)
		};
	}
	
	public Vec3[] getLightningVectors()
	{
		return new Vec3[]
		{
			bound.c.add(new Vec3(0, 32, 0)),
			bound.c.add(new Vec3(0,  6, 0)),
			bound.c.add(bound.u.yAxis.multiply(bound.e.y))
		};
	}
	
	public Vec3 getBackwardItemVector(Item item, int iteration)
	{
		float radius = item.getMaximumExtent() * 1.5f * iteration;
		
		return bound.c.add(bound.u.zAxis.multiply(bound.e.z + radius));
	}
	
	public Vec3 getBackwardItemVector(Item item)
	{
		float radius = item.getMaximumExtent() * 1.5f;
		
		return getUpItemVector(item).add(bound.u.zAxis.multiply(bound.e.z + radius));
	}
	
	public Vec3 getForwardItemVector(Item item)
	{
		float radius = item.getMaximumExtent() * 1.5f;
		
		return getUpItemVector(item).subtract(bound.u.zAxis.multiply(bound.e.z + radius));
	}
	
	public Vec3 getUpItemVector(Item item)
	{
		float radius = item.getMaximumExtent() * 1.5f;
		
		return bound.c.add(bound.u.yAxis.multiply(bound.e.y + radius));
	}
	
	public void reset()
	{
		trajectory = 0;
		
		bound.setRotation(0, 0, 0);
		bound.setPosition(ORIGIN);
		
		yRotation_Wheel = xRotation_Wheel = 0.0f;

		turnRate = velocity = 0.0f;
		
		accelerating = reversing = turning = false;
		
		direction = Direction.STRAIGHT;
	}
	
	public void boost()
	{
		boosting = true;
		scene.focalBlur.enableRadial = true;
		boostDuration = 60;
		velocity = 2 * TOP_SPEED;
	}

	public void spin()
	{
		if(!slipping)
		{
			slipVector = bound.u.zAxis;
			slipTrajectory = trajectory;
			slipping = true;
			slipDuration = 48;
			turnRate = 0;
			
			boosting = false;
		}
	}
	
	public void useLightningBolt() // TODO
	{
		for(Vehicle car : scene.getCars()) car.struckByLightning();
//			if(!car.equals(this)) car.struckByLightning();
	}
	
	LightningStrike bolt;
	ThunderOrb orb;
	
	public void struckByLightning()
	{
		if(!starPower && !invisible)
		{
			if(!miniature)
			{
				bound.e = bound.e.multiply(0.5f);
				scale /= 2;
				graph.getRoot().setScale(new Vec3(scale));
			}
			
			miniature = true;
			miniatureDuration = 400;
			velocity = 0;
			
			if(slipping) slipDuration += 24;
			else spin();
		}
		
		Vec3 start = getLightningVectors()[0]; 
		Vec3 end   = getLightningVectors()[starPower ? 1 : 2];
		
		bolt = new LightningStrike(start, end, 4, true, true, RenderStyle.SINGLE_FLASH);
		bolt.addChild(bolt.generateBolt(RenderStyle.SINGLE_FLASH, BoltType.SELF_ARCH, 2, end, 5, 32));
		
		float radius = bound.e.z * 2.0f;
		
		if(starPower)
		{
			for(int i = 0; i < 3; i++)
			{
				Vec3 p;
				
				if(starPower)
				{
					p= bound.u.zAxis.multiply(radius);
				    p = p.multiply(new RotationMatrix(bound.u.yAxis, i * 120));
				    p = bound.c.subtract(bound.u.yAxis.multiply(bound.e.y)).add(p);
				}
				else p = getLightningVectors()[3];
				
				bolt.addChild(bolt.generateBolt(RenderStyle.SINGLE_FLASH, BoltType.END_ARCH, 0.5f, p, 3, 16));
				bolt.addChild(bolt.generateBolt(RenderStyle.SINGLE_FLASH, BoltType.END_ARCH, 2.0f, p, 3, 16));
			}
		}
		
		orb = new ThunderOrb(getLightningVectors()[0]);
	}
	
	public void curse()
	{
		cursed = true;
		curseDuration = 500;
		itemDuration = 0; 
	}
	
	private void useBoo()
	{
		invisible = true;
		booDuration = 400;
	}

	public void usePowerStar()
	{
		starPower = true;
		cursed = false;
		starDuration = 500;
		turnIncrement = 0.15f;
	}
	
	public boolean isSlipping()    { return slipping;  }

	public boolean isCursed()      { return cursed;    }
	
	public boolean isMiniature()   { return miniature; }
	
	public boolean hasStarPower()  { return starPower; }
	
	public boolean isInvisible()   { return invisible; }
	
	public boolean isBoosting()    { return boosting;  }
	
	public boolean hasSuperBoost() { return superBoosting; }
	
	public boolean isDrifting()    { return drift != Direction.STRAIGHT; }

	public void keyPressed(KeyEvent e)
	{
		if(motionMode == Motion.ANCHOR) anchor.keyPressed(e);
		else if(camera.isFree()) camera.keyPressed(e);
		else
		{
			controller.disable();
			
			switch(e.getKeyCode())
			{
				case KeyEvent.VK_W:
				case KeyEvent.VK_UP:
					accelerating = true; break;
					
				case KeyEvent.VK_S:
				case KeyEvent.VK_DOWN:
					reversing = true; accelerating = true; break;
					
				case KeyEvent.VK_A:
				case KeyEvent.VK_LEFT:
					steerLeft(); break;
				
				case KeyEvent.VK_D:
				case KeyEvent.VK_RIGHT:
					steerRight(); break;
					
				case KeyEvent.VK_E: camera.setRearview(true); break;
			}
		}
		
		switch(e.getKeyCode())
		{
			case KeyEvent.VK_R: if(!cursed && !slipping) { aimForwards();  useItem(); } break;
			case KeyEvent.VK_F: if(!cursed && !slipping) { resetAim();     useItem(); } break;
			case KeyEvent.VK_C: if(!cursed && !slipping) { aimBackwards(); useItem(); } break;
			
			case KeyEvent.VK_N: roulette.next(); break;
			case KeyEvent.VK_B: roulette.repeat(); break;
			case KeyEvent.VK_V: roulette.previous(); break;
			
			case KeyEvent.VK_Q: if(turning && !falling) drift(); break;
			
			case KeyEvent.VK_M: camera.cycleMode(); displayModel = true; break;
			case KeyEvent.VK_G:
			{
				motionMode = Motion.cycle(motionMode);
				
				anchor.setPosition(getPosition()); 
				anchor.setRotation(getRotationAngles(heights));
				
				break;
			}
			
			case KeyEvent.VK_9: if(!camera.isFirstPerson()) displayModel = !displayModel; break;
			
			case KeyEvent.VK_F2: renderMode++; renderMode %= 2; break;
			
			case KeyEvent.VK_F4: hud.setVisibility(!hud.getVisibility()); break;
			case KeyEvent.VK_F5: hud.decreaseStretch(); break; 
			case KeyEvent.VK_F6: hud.increaseStretch(); break; 
			case KeyEvent.VK_F7: hud.cycleGraphMode(); break;
			case KeyEvent.VK_F8: hud.nextComponent(); break;
			
			case KeyEvent.VK_BACK_SPACE: reset(); break;
		}
	}

	public void keyReleased(KeyEvent e)
	{
		switch(e.getKeyCode())
		{		
			case KeyEvent.VK_UP:
			case KeyEvent.VK_W:
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_S:
				accelerating = false; reversing = false; break;
			case KeyEvent.VK_A:
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_D:
			case KeyEvent.VK_RIGHT:
				turning = false; straighten(); break;
			
			case KeyEvent.VK_R:
			case KeyEvent.VK_C:
			case KeyEvent.VK_F: if(hasItem()) releaseItem(); break;
		
			case KeyEvent.VK_Q: miniTurbo(); break;
			
			case KeyEvent.VK_E: camera.setRearview(false); break;
		}
	}
	
	public void buttonPressed(int e)
	{
		if(motionMode == Motion.ANCHOR)
		{
			
		}
		else if(!camera.isFree())
		{
			switch(e)
			{
				case  3: roulette.next(); break;
				case -3: roulette.previous(); break;
				case  4: if(!cursed && !slipping) { aimForwards();  useItem(); } break;
				case -4: if(!cursed && !slipping) { aimBackwards(); useItem(); } break; 
				case  5: accelerating = true; break;
				case  6: camera.setRearview(true); break;
				case  7: reversing = true; accelerating = true; break;
				case  9: 
				case 10: if(turning && !falling) drift(); break;
				case 14: roulette.repeat(); break;
			}
		}
		else
		{
			switch(e)
			{
				case  5: camera.setSpeed(15); break;
				case  7: camera.setSpeed(0.1f); break;
			}
		}
		
		switch(e)
		{
			case  8: camera.cycleMode(); displayModel = true; break;
			case 11: Scene.enableAnimation = !Scene.enableAnimation; break;
			case 12: System.exit(0); break;
		}
	}
	
	public void buttonReleased(int e)
	{
		if(!camera.isFree())
		{
			switch(e)
			{
				case  4: if(hasItem()) releaseItem(); break; 
				case  5:
				case  7: accelerating = false; reversing = false; break;
				case  6: camera.setRearview(false); break;
				case  9:
				case 10: miniTurbo(); break;
			}
		}
		else
		{
			switch(e)
			{
				case  5: camera.setSpeed(5); break;
				case  7: camera.setSpeed(5); break;
			}
		}
	}
	
	public void setupCamera(GL2 gl, GLU glu)
	{
		float _trajectory = trajectory;
		
		switch(camera.getMode())
		{	
			case DYNAMIC_VIEW:
			{
				camera.setPosition(getPosition());
				if(slipping) _trajectory = slipTrajectory;
				camera.setRotation(_trajectory);
				break;
			}
			case BIRDS_EYE_VIEW:
			{
				camera.setPosition(getPosition());
				break;
			}
			case DRIVERS_VIEW:
			{
				camera.setPosition(getPosition());
				camera.setRotation(_trajectory);
				camera.setOrientation(bound.u);
				displayModel = false;
				break;
			}
			case FREE_LOOK_VIEW:
			{	
				if(scene.moveLight) scene.light.setPosition(camera.getPosition());
				if(controller.isEnabled()) camera.update(controller);
				break;
			}
			
			default: break;	
		}

		camera.setupView(gl, glu);
	}
	
	public enum Motion
	{
		DEFAULT,
		ANCHOR;
		
		public static Motion cycle(Motion mode)
		{
			return values()[(mode.ordinal() + 1) % values().length];
		}
	}
}