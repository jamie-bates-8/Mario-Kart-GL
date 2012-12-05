import static graphics.util.Matrix.getRotationMatrix;

import static graphics.util.Renderer.displayColoredObject;
import static graphics.util.Renderer.displayPartiallyTexturedObject;
import static graphics.util.Renderer.displayTexturedObject;
import static graphics.util.Renderer.displayTransparentObject;
import static graphics.util.Renderer.displayWireframeObject;

import static graphics.util.Vector.add;
import static graphics.util.Vector.multiply;
import static graphics.util.Vector.normalize;
import static graphics.util.Vector.subtract;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.asin;
import static java.lang.Math.atan;
import static java.lang.Math.toDegrees;

import graphics.util.Face;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.awt.TextRenderer;

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

public class Car
{
	/** Model Fields **/
	private static final List<Face> CAR_FACES         = OBJParser.parseTriangles("obj/car.obj");
	private static final List<Face> WHEEL_FACES       = OBJParser.parseTriangles("obj/wheel.obj");
	private static final List<Face> WINDOW_FACES      = OBJParser.parseTriangles("obj/windows.obj");
	private static final List<Face> DOOR_WINDOW_FACES = OBJParser.parseTriangles("obj/door_windows.obj");
	private static final List<Face> DOOR_FACES        = OBJParser.parseTriangles("obj/door.obj");
	
	private static final float[] ORIGIN = {0, 1.8f, 0};
	
	private static int carList = -1;
	
	private float[] color = {1.0f, 0.4f, 0.4f};
	private float[] windowColor = {0.4f, 0.8f, 1.0f};
	
	private static final float[] BLACK = {0.0f, 0.0f, 0.0f};
	
	public boolean displayModel = true;
	public boolean enableWireframe = false;
	
	
	/** Vehicle Fields **/
	public float trajectory; 
	
	private float scale = 1.5f;
	
	private float yRotation_Wheel = 0.0f;
	private float zRotation_Wheel = 0.0f;
	
	private float[][] offsets_Wheel =
		{{ 2.4f, -0.75f,  1.75f},  //back-left
		 {-2.4f, -0.75f,  1.75f},  //front-left
		 { 2.4f, -0.75f, -1.75f},  //back-right
		 {-2.4f, -0.75f, -1.75f}}; //front-right
	
	private float[] offsets_LeftDoor =  {-1.43f, 0.21f,  1.74f};
	private float[] offsets_RightDoor = {-1.43f, 0.21f, -1.74f};
	
	
	/** Fields that define the vehicle's motion **/
	public float velocity = 0;
	public static final float TOP_SPEED = 1.2f;
	public double acceleration = 0.012;
	public boolean accelerating = false;
	public boolean reversing = false;
	public boolean invertReverse = false;
	
	public double gravity = 0.05;
	public boolean falling = false;
	private float fallRate = 0.0f;
	private static final double TOP_FALL_RATE = 2.5;
	
	public double turnRate = 0;
	private static final double TOP_TURN_RATE = 2.0;
	private double turnIncrement = 0.1;
	public boolean turning = false;
	
	private enum Direction {STRAIGHT, LEFT, RIGHT};
	private Direction direction = Direction.STRAIGHT;
	
	private Direction drift = Direction.STRAIGHT;
	private boolean driftCounter = false;
	private enum DriftState {YELLOW, RED, BLUE};
	private DriftState driftState = DriftState.YELLOW;
	
	public double distance = 0;
	
	
	/** Collision Detection Fields **/
	public OBB bound;
	public boolean colliding = false;
	public List<Bound> collisions = new ArrayList<Bound>();
	public float[] heights = {0, 0, 0, 0};
	
	
	/** Particle Fields **/
	private ParticleGenerator generator = new ParticleGenerator();
	
	
	/** Item Fields **/
	private ItemRoulette roulette = new ItemRoulette();
	private ItemState itemState = ItemState.NO_ITEM;
	private Queue<Item> items = new LinkedList<Item>();
	private Queue<Integer> itemCommands = new ArrayBlockingQueue<Integer>(100);
	
	private boolean slipping = false;
	private float[] slipVector = ORIGIN;
	public float slipTrajectory = 0;
	private int slipDuration = 0;
	
	private boolean miniature = false;
	private int miniatureDuration = 0;
	
	private boolean cursed = false;
	private int curseDuration = 0;
	
	private boolean boosting = false;
	private boolean superBoosting = false;
	private int boostDuration = 0;
	
	private boolean starPower = false;
	private int starDuration = 0;
	private float[] starColor = {255, 0, 0};
	private int spectrum = 0;
	private boolean whiten = true;
	private static final int COLOR_INCREMENT = 17; //1, 3, 5, 15, 17, 51, 85, 255
	
	private boolean invisible = false;
	private int booDuration = 0;
	private float booColor = 0.5f;
	private float fadeIncrement = 0.0125f;
	
	public int itemDuration = 0;
	
	private enum Aim {FORWARDS, DEFAULT, BACKWARDS};
	private Aim aiming = Aim.DEFAULT;
	
	/** Scene Fields **/
	private Scene scene;
	public Camera camera;
	
	/** Controller Fields **/
	private GamePad controller;
	
	private HUD hud;
	
	public Car(GL2 gl, float[] c, float xrot, float yrot, float zrot, Scene scene)
	{
		//Using a display list ensures that the complex car model is displayed quickly
		if(carList == -1)
		{
			carList = gl.glGenLists(1);
			gl.glNewList(carList, GL2.GL_COMPILE);
			displayPartiallyTexturedObject(gl, CAR_FACES, color);
			gl.glEndList();
			
			System.out.println("Car: " + "\n" +
			"\t" + "Body: "        + CAR_FACES.size()         + " faces" + "\n" +
			"\t" + "Wheel: "       + WHEEL_FACES.size()       + " faces" + "\n" +
			"\t" + "Window: "      + WINDOW_FACES.size()      + " faces" + "\n" +
			"\t" + "Door Window: " + DOOR_WINDOW_FACES.size() + " faces" + "\n" +
			"\t" + "Door: "        + DOOR_FACES.size()        + " faces");
		}
	    
	    bound = new OBB(
				c[0], c[1], c[2],
	    		xrot, yrot, zrot,
	    		5.5f, 2.0f, 2.7f);
	    
	    trajectory = yrot;
	    
	    this.scene = scene; 
	    camera = new Camera();
	    
	    controller = new GamePad();
	    
		Font font = new Font("Calibri", Font.PLAIN, 18);
		TextRenderer renderer = new TextRenderer(font);
	    hud = new HUD(scene, this, renderer);
	}
	
	public HUD getHUD() { return hud; }
	
	public Bound getBound() { return bound; }
	
	public void collide(Car car) { collisions.add(car.getBound()); }
	
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
	
	public float[][] getRotation() { return bound.u; }
	
	public void aimForwards()  { aiming = Aim.FORWARDS;  }
	
	public void aimBackwards() { aiming = Aim.BACKWARDS; }
	
	public void resetAim()     { aiming = Aim.DEFAULT;   }
	
	public void removeItems()
	{
		List<Item> toRemove = new ArrayList<Item>();
		
		for(Item item : items)
			if(item.isDead()) toRemove.add((Item) item);
		
		items.removeAll(toRemove); 
		
		for(int i = 0; i < toRemove.size(); i++)
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

	public void setRotation(float x, float y, float z) { bound.u = getRotationMatrix(x, y, z); }
	
	public float[] getForwardVector() { return multiply(bound.u[0], velocity); }
	
	public float[] getSlipVector() { return subtract(bound.c, multiply(slipVector, velocity)); }
	
	public void setRotation(float[] angles) { bound.u = getRotationMatrix(angles[0], angles[1], angles[2]); }
	
	public void setPosition(float[] c) { bound.setPosition(c); }
	
	public float[] getPosition() { return bound.getPosition(); }
	
	public void render(GL2 gl)
	{
		gl.glPushMatrix();
		{
			/** Vehicle Transformations **/
			gl.glTranslatef(bound.c[0], bound.c[1], bound.c[2]);
			gl.glMultMatrixf(getRotationMatrix(bound.u), 0);
			gl.glScalef(scale, scale, scale);
			//Display the car model by calling a display list
			if(displayModel)
			{	
				if(starPower)
				{
					cycleColor();
					float[] _color = {starColor[0]/255, starColor[1]/255, starColor[2]/255};
					displayColoredObject(gl, CAR_FACES, _color);
				}
				else if(invisible)
				{
					if(booColor > 0 && booDuration > 0) booColor -= fadeIncrement;
					displayTransparentObject(gl, CAR_FACES, booColor);
				}
				else if(enableWireframe) displayWireframeObject(gl, CAR_FACES, color);
				else gl.glCallList(carList);
				
				gl.glColor3f(1, 1, 1);
				
				for(int wheel = 0; wheel < 4; wheel++)
				{
					gl.glPushMatrix();
					{
						float x = offsets_Wheel[wheel][0];
						float y = offsets_Wheel[wheel][1];
						float z = offsets_Wheel[wheel][2];
						
						/** Wheel Transformations **/
						gl.glTranslatef(x, y, z);
						
						if(wheel % 2 != 0) // Only turn the front wheels
							gl.glRotatef(yRotation_Wheel, 0.0f, 1.0f, 0.0f);
	
						gl.glRotatef(zRotation_Wheel, 0.0f, 0.0f, 1.0f);
						
						gl.glScalef(0.6f, 0.6f, 0.6f);
						
						if(starPower)
						{
							float[] _color = {starColor[0]/255, starColor[1]/255, starColor[2]/255};
							displayColoredObject(gl, WHEEL_FACES, _color);
						}
						else if(invisible) displayTransparentObject(gl, WHEEL_FACES, booColor);
						else if(enableWireframe) displayWireframeObject(gl, WHEEL_FACES, BLACK);
						else displayTexturedObject(gl, WHEEL_FACES);
					}
					gl.glPopMatrix();
				}
				
				/** Left Door Transformations **/
				gl.glPushMatrix();
				{
					float x = offsets_LeftDoor[0];
					float y = offsets_LeftDoor[1];
					float z = offsets_LeftDoor[2];
					
					gl.glTranslatef(x, y, z);
					
					if(starPower)
					{
						float[] _color = {starColor[0]/255, starColor[1]/255, starColor[2]/255};
						displayColoredObject(gl, DOOR_FACES, _color);
					}
					else if(invisible) displayTransparentObject(gl, DOOR_FACES, booColor);
					else if(enableWireframe) displayWireframeObject(gl, DOOR_FACES, color);
					else displayColoredObject(gl, DOOR_FACES, color);
					
					/** Left Door Window Transformations **/
					gl.glPushMatrix();
					{
						if(invisible) displayTransparentObject(gl, DOOR_WINDOW_FACES, booColor);
						else if(enableWireframe) displayWireframeObject(gl, DOOR_WINDOW_FACES, windowColor);
						else displayTransparentObject(gl, DOOR_WINDOW_FACES, windowColor);
					}
					gl.glPopMatrix();	
				}
				gl.glPopMatrix();
				
				/** Right Door Transformations **/
				gl.glPushMatrix();
				{
					float x = offsets_RightDoor[0];
					float y = offsets_RightDoor[1];
					float z = offsets_RightDoor[2];
					
					gl.glTranslatef(x, y, z);
					
					//Display the door model reflected in the z-axis
					gl.glScalef(1, 1, -1);
					
					if(starPower)
					{
						float[] _color = {starColor[0]/255, starColor[1]/255, starColor[2]/255};
						displayColoredObject(gl, DOOR_FACES, _color);
					}
					else if(invisible) displayTransparentObject(gl, DOOR_FACES, booColor);
					else if(enableWireframe) displayWireframeObject(gl, DOOR_FACES, color);
					else displayColoredObject(gl, DOOR_FACES, color);
					
					/** Right Door Window Transformations **/
					gl.glPushMatrix();
					{	
						if(invisible) displayTransparentObject(gl, DOOR_WINDOW_FACES, booColor);
						else if(enableWireframe) displayWireframeObject(gl, DOOR_WINDOW_FACES, windowColor);
						else displayTransparentObject(gl, DOOR_WINDOW_FACES, windowColor);
					}
					gl.glPopMatrix();
				}
				gl.glPopMatrix();
				
				/** Window Transformations **/
				gl.glPushMatrix();
				{			
					gl.glTranslatef(0.3f, -1.2f, 0);
					
					if(invisible) displayTransparentObject(gl, WINDOW_FACES, booColor);
					else if(enableWireframe) displayWireframeObject(gl, WINDOW_FACES, windowColor);
					else displayTransparentObject(gl, WINDOW_FACES, windowColor);
				}
				gl.glPopMatrix();
				
				gl.glFlush();
			}
		}
		gl.glPopMatrix();
	
		gl.glColor3f(1, 1, 1);
	
		for(Item item : items) item.render(gl, trajectory);
	}

	private void cycleColor()
	{
		if(starColor[0] == 255 && starColor[1] == 255 && starColor[2] == 255) whiten = false;
		
		if(whiten)
		{
			if(starColor[0] < 255) starColor[0] += COLOR_INCREMENT;
			if(starColor[1] < 255) starColor[1] += COLOR_INCREMENT;
			if(starColor[2] < 255) starColor[2] += COLOR_INCREMENT;
		}
		else
		{
			switch(spectrum)
			{
				case 0: if(starColor[0] == 255 && starColor[1] ==   0 && starColor[2] ==   0) { whiten = true; spectrum++; }
				else { starColor[1] -= COLOR_INCREMENT; starColor[2] -= COLOR_INCREMENT; } break;
				
				case 1: if(starColor[0] == 255 && starColor[1] == 255 && starColor[2] ==   0) { whiten = true; spectrum++; }
				else { starColor[2] -= COLOR_INCREMENT; } break;
				
				case 2: if(starColor[0] ==   0 && starColor[1] == 255 && starColor[2] ==   0) { whiten = true; spectrum++; }
				else { starColor[0] -= COLOR_INCREMENT; starColor[2] -= COLOR_INCREMENT; } break;
				
				case 3: if(starColor[0] ==   0 && starColor[1] == 255 && starColor[2] == 255) { whiten = true; spectrum++; }
				else { starColor[0] -= COLOR_INCREMENT; } break;
				
				case 4: if(starColor[0] ==   0 && starColor[1] ==   0 && starColor[2] == 255) { whiten = true; spectrum++; }
				else { starColor[0] -= COLOR_INCREMENT; starColor[1] -= COLOR_INCREMENT; } break;
				
				case 5: if(starColor[0] == 255 && starColor[1] ==   0 && starColor[2] == 255) { whiten = true; spectrum++; }
				else { starColor[1] -= COLOR_INCREMENT; } break;
			}
		}
		
		spectrum = spectrum % 6;
	}

	public long renderHUD(GL2 gl, GLU glu) { return hud.render(gl, glu); }

	public float[] getHeights()
	{
		falling = true;
		
		float[] _heights = heights;
		heights = new float[] {0, 0, 0, 0};
		
		for(Bound collision : collisions)
		{
			if(collision instanceof OBB) setHeights((OBB) collision);
		}
		
		if(collisions.isEmpty()) heights = _heights;
		
		return heights;
	}
	
	public float[] getHeights(HeightMap map)
	{
		float[][] vertices = bound.getVertices();

		for(int i = 0; i < 4; i++)
		{
			float h = map.getHeight(vertices[i]);
			heights[i] = h;
		}
		
		float h = (heights[0] + heights[1] + heights[2] + heights[3]) / 4;
		h += bound.e[1];
		bound.c[1] = h;
		
		return heights;
	}

	/**
	 * This method calculates the height of the vehicles as determined by the OBB
	 * passed as a parameter; note that it is assumed that the vehicle is colliding
	 * with the top of the OBB (see resolveOBB(OBB) for side and bottom collisions)
	 */
	private void setHeights(OBB obb)
	{
		float[] face = obb.getFaceVector(getPosition());

		//if the side of collision is the upwards face
		if(Arrays.equals(face, obb.getUpVector(1)))
		{
			float[][] vertices = bound.getVertices();

			//calculate the height at each wheel
			for(int i = 0; i < 4; i++)
			{
				float h = obb.closestPointOnPerimeter(vertices[i])[1];
				if(h > heights[i]) heights[i] = h;
			}

			//calculate the height at the centre of the vehicle
			float h = obb.closestPointOnPerimeter(getPosition())[1]
					+ (bound.e[1] * 0.9f);
			
			if(h > bound.c[1]) bound.c[1] = h;

			falling = false; fallRate = 0; //disable falling
		}
	}
	
	public float[] getRotationAngles(float[] h)
	{
		float frontHeight = (h[0] + h[1]) / 2; 
		float backHeight  = (h[2] + h[3]) / 2;
		float leftHeight  = (h[1] + h[3]) / 2;
		float rightHeight = (h[0] + h[2]) / 2;
	
		float xrot = (float) -toDegrees(atan((leftHeight - rightHeight) / (bound.e[2] * 2)));
		float zrot = (float) -toDegrees(atan((frontHeight - backHeight) / (bound.e[0] * 2)));
		
		return new float[] {xrot, trajectory, zrot};
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
		
		//TODO car collisions could be improved
		for(Car car : scene.getCars())
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
		List<float[]> vectors = new ArrayList<float[]>();
		
		for(Bound collision : collisions)
		{
			if     (collision instanceof OBB   ) vectors.add(resolveOBB   ((OBB)    collision));
			else if(collision instanceof Sphere) vectors.add(resolveSphere((Sphere) collision));
		}
		
		for(float[] vector : vectors) setPosition(add(getPosition(), vector));
	}

	/*
	 * TODO
	 * 
	 * There are currently no obstacles in the scene that use a sphere as its
	 * bounding geometry; therefore, this method is no used and may be inaccurate
	 * 
	 */
	private float[] resolveSphere(Sphere sphere)
	{
		float[] face = sphere.getFaceVector(getPosition());
		face[1] = 0;

		float s = this.bound.getPenetration(sphere);

		velocity *= (slipping) ? 0 : 0.9;

		return multiply((normalize(subtract(face, sphere.getPosition()))), s);
	}

	/**
	 * This method resolves a collision with an OBB passed as a parameter;
	 * note that it is assumed that the vehicle is colliding with the side or
	 * bottom of the OBB (setHeights(OBB) for top collisions) 
	 */
	private float[] resolveOBB(OBB obb)
	{
		float[] face = obb.getFaceVector(getPosition());
		
		/*
		 * the vehicles must be colliding with the side or bottom of the OBB
		 * the face must be a valid collision (not all sides are considered)
		 * the bottom of the vehicle must be lower than the top of the OBB
		 */
		if(!Arrays.equals(face, obb.getUpVector(1)) && obb.isValidCollision(face)
		   && (getPosition()[1] - bound.e[1]) < (obb.c[1] + obb.e[1]))
		{
			float p = bound.getPenetration(obb);
			
			if(slipping) velocity = 0;
			else velocity *= 0.9;
			
			return multiply(subtract(face, obb.getPosition()), p);
		}
		else return new float[] {0, 0, 0};
	}

	public void update()
	{	
		getHeights(scene.getHeightMap());
//		getHeights();
		
		setRotation(getRotationAngles(heights));
		
		detectCollisions();
		if(colliding) resolveCollisions();
		
		if(superBoosting && !slipping) boost();
		
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
		
		double currentDistance = distance;
		
		if(falling) fall();
	
		velocity = (velocity > 2 * TOP_SPEED) ? (2 * TOP_SPEED) : velocity;
	
		if(!slipping) setPosition(getPositionVector());
		else setPosition(getSlipVector());
		
		distance += velocity;
		
		turnWheels();
		
		//The wheels are rotated in relation to the distance travelled
		zRotation_Wheel += 360 * (distance - currentDistance) / (2 * PI * 0.5); //0.5 is the wheel radius
		
		if(drift != Direction.STRAIGHT && !falling)
		{
			for(float[] source : getDriftVectors())
			{
				scene.addParticles(generator.generateDriftParticles(source, 10, driftState.ordinal(), miniature));
				scene.addParticles(generator.generateSparkParticles(source,  2, miniature));
			}
		}
		
		updateStatus();
		
		if(!controller.isNull() && controller.isEnabled() && !camera.isFree())
		{
			if     (controller.getXAxis() > (isDrifting() ?  0.9f : 0)) steerLeft();
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
	 * these effects are caused by using or collising with items certain items
	 */
	private void updateStatus()
	{
		if(miniatureDuration > 0) miniatureDuration--;
		else if(miniature)
		{
			miniature = false;
			bound.e = multiply(bound.e, 2);
			scale *= 2;
		}
		
		if(boostDuration > 0) boostDuration--;
		else boosting = false;
		
		if(boosting)
		{
			for(float[] source : getBoostVectors())
				scene.addParticles(generator.generateBoostParticles(source, boostDuration / 4, superBoosting, miniature));
		}
		
		if(curseDuration > 0) curseDuration--;
		else cursed = false;
		
		if(cursed)
			scene.addParticles(generator.generateFakeItemBoxParticles(getPosition(), 2, miniature));
		
		if(slipDuration > 0) slipDuration--;
		else slipping = false;
		
		if(slipping) trajectory += 15;
		
		if(starDuration > 0) starDuration--;
		else
		{
			starPower = false;
			turnIncrement = 0.1;
		}
		
		if(starPower)
			scene.addParticles(generator.generateStarParticles(getPosition(), 2, miniature));
		
		if(booDuration > 0) booDuration--;
		else if(booColor < 0.5f) booColor += 0.0125f;
		else invisible = false;
		
		if(itemDuration > 0) itemDuration--;
		else if(ItemState.isTimed(itemState))
		{
			superBoosting = false;
			itemState = ItemState.NO_ITEM;
		}
	}

	private void fall()
	{
		if(fallRate < TOP_FALL_RATE) fallRate += gravity;
		bound.c[1] -= fallRate;
	}
	
	public void drift() { drift = direction; }
	
	public void miniTurbo()
	{
		drift = Direction.STRAIGHT;
		
		if(driftState == DriftState.BLUE)
		{
			boosting = true;
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
		if(reversing) velocity += (velocity < -TOP_SPEED) ? acceleration : -acceleration;
		else velocity += (velocity < TOP_SPEED) ? acceleration : -acceleration;
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
		if(!controller.isEnabled() || controller.getXAxis() <= 0)
		{
			if(turnRate < TOP_TURN_RATE) turnRate += turnIncrement;
		}
		else
		{
			if(turnRate < TOP_TURN_RATE * controller.getXAxis()) turnRate += turnIncrement;
		}
		
		double k = 1; 
		
		if(drift == Direction.LEFT)
		{
			if(direction == Direction.LEFT) k = 1.25;
			else if(direction == Direction.RIGHT) k = 0.5;
		}
		
		if(velocity != 0) trajectory += turnRate * k;
	}

	private void turnRight()
	{
		if(!controller.isEnabled() || controller.getXAxis() >= 0)
		{
			if(turnRate > -TOP_TURN_RATE) turnRate -= turnIncrement;
		}
		else
		{
			if(turnRate > TOP_TURN_RATE * controller.getXAxis()) turnRate -= turnIncrement;
		}
		
		double k = 1;
		
		if(drift == Direction.RIGHT)
		{
			if(direction == Direction.LEFT) k = 0.5;
			else if(direction == Direction.RIGHT) k = 1.25;
		}
		
		if(velocity != 0) trajectory += turnRate * k;
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
		double turnRatio = turnRate / TOP_TURN_RATE;
	
		if(turnRatio > 1) turnRatio = 1;
		else if(turnRatio < -1) turnRatio = -1;
	
		yRotation_Wheel = (float) (toDegrees(asin(turnRatio)) / 2);
		if(velocity < 0) yRotation_Wheel = -yRotation_Wheel;
	}

	public float[] getPositionVector()
	{
		float _velocity = (miniature) ?  velocity * 0.75f :  velocity;
		      _velocity = (starPower) ? _velocity * 1.25f : _velocity;
		
		return subtract(bound.c, multiply(bound.u[0], _velocity));
	}

	public float[][] getBoostVectors()
	{	
		float[] eu0 = multiply(bound.u[0], bound.e[0]);
		float[] eu2 = multiply(bound.u[2], bound.e[2] * 0.75f);
		
		return new float[][]
		{
			subtract(add(getPosition(), eu0), eu2), //right exhaust
			     add(add(getPosition(), eu0), eu2)  //left exhaust
		};
	}
	
	public float[][] getDriftVectors()
	{
		float[] eu0 = multiply(bound.u[0], bound.e[0] * 0.75f);
		float[] eu1 = multiply(bound.u[1], bound.e[1] * 0.75f);
		float[] eu2 = multiply(bound.u[2], bound.e[2] * 1.25f);
		
		return new float[][]
		{
			subtract(subtract(add(getPosition(), eu0), eu1), eu2),
			     add(subtract(add(getPosition(), eu0), eu1), eu2)
		};
	}
	
	public float[] getLightningVector()
	{
		return add(bound.c, multiply(bound.u[1], 20));
	}
	
	public float[] getBackwardItemVector(Item item, int iteration)
	{
		float radius = item.getMaximumExtent() * 1.5f * iteration;
		
		return add(bound.c, multiply(bound.u[0], bound.e[0] + radius));
	}
	
	public float[] getBackwardItemVector(Item item)
	{
		float radius = item.getMaximumExtent() * 1.5f;
		
		return add(getUpItemVector(item), multiply(bound.u[0], bound.e[0] + radius));
	}
	
	public float[] getForwardItemVector(Item item)
	{
		float radius = item.getMaximumExtent() * 1.5f;
		
		return subtract(getUpItemVector(item), multiply(bound.u[0], bound.e[0] + radius));
	}
	
	public float[] getUpItemVector(Item item)
	{
		float radius = item.getMaximumExtent() * 1.5f;
		
		return add(bound.c, multiply(bound.u[1], bound.e[1] + radius));
	}
	
	public void reset()
	{
		trajectory = 0;
		
		bound.setRotation(0, 0, 0);
		bound.setPosition(ORIGIN);
		
		yRotation_Wheel = zRotation_Wheel = 0.0f;

		turnRate = velocity = 0.0f;
		
		accelerating = reversing = turning = false;
		
		direction = Direction.STRAIGHT;
	}
	
	public void boost()
	{
		boosting = true;
		boostDuration = 60;
		velocity = 2 * TOP_SPEED;
	}

	public void spin()
	{
		if(!slipping)
		{
			slipVector = bound.u[0];
			slipTrajectory = trajectory;
			slipping = true;
			slipDuration = 48;
			turnRate = 0;
		}
	}
	
	public void useLightningBolt()
	{
		for(Car car : scene.getCars())
			if(!car.equals(this)) car.struckByLightning();
	}
	
	public void struckByLightning()
	{
		if(!starPower && !invisible)
		{
			if(!miniature)
			{
				bound.e = multiply(bound.e, 0.5f);
				scale /= 2;
			}
			
			miniature = true;
			miniatureDuration = 400;
			velocity = 0;
			
			if(slipping) slipDuration += 24;
			else spin();
		}
		
		float[] source = getLightningVector(); 
		scene.addParticle(new LightningParticle(source));
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
		turnIncrement = 0.15;
	}
	
	public boolean isSlipping()   { return slipping;  }

	public boolean isCursed()     { return cursed;    }
	
	public boolean isMiniature()  { return miniature; }
	
	public boolean hasStarPower() { return starPower; }
	
	public boolean isInvisible()  { return invisible; }
	
	public boolean isBoosting()   { return boosting;  }
	
	public boolean isDrifting()   { return drift != Direction.STRAIGHT; }

	public void keyPressed(KeyEvent e)
	{
		if(camera.isFree())
		{
			switch(e.getKeyCode())
			{
				case KeyEvent.VK_W: camera.moveForward(5);  break;
				case KeyEvent.VK_S: camera.moveBackward(5); break;
				case KeyEvent.VK_A: camera.moveLeft(5);     break;
				case KeyEvent.VK_D: camera.moveRight(5);    break;
				
				case KeyEvent.VK_UP:    camera.lookUp(5);    break;
				case KeyEvent.VK_DOWN:  camera.lookDown(5);  break;
				case KeyEvent.VK_LEFT:  camera.lookLeft(5);  break;
				case KeyEvent.VK_RIGHT: camera.lookRight(5); break;
				
				case KeyEvent.VK_MINUS:  camera.decreaseSensitivity(); break;
				case KeyEvent.VK_EQUALS: camera.increaseSensitivity(); break;
			}
		}
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
			
			case KeyEvent.VK_X: invertReverse = !invertReverse; break;
			
			case KeyEvent.VK_M: camera.cycleMode(); break;
			
			case KeyEvent.VK_9: if(!camera.isFirstPerson()) displayModel = !displayModel; break;
			
			case KeyEvent.VK_F3: enableWireframe = !enableWireframe; break;
			
			case KeyEvent.VK_F4: hud.setTextColor(Color.BLACK); break;
			case KeyEvent.VK_F5: hud.setVisibility(!hud.getVisibility()); break;
			case KeyEvent.VK_F6: hud.decreaseStretch(); break; 
			case KeyEvent.VK_F7: hud.increaseStretch(); break; 
			case KeyEvent.VK_F8: hud.cycleGraphMode(); break;
			case KeyEvent.VK_F9: hud.nextComponent(); break;
			
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
		if(!camera.isFree())
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
				case  5: camera.increaseSensitivity(); break;
				case  7: camera.decreaseSensitivity(); break;
			}
		}
		
		switch(e)
		{
			case  8: camera.cycleMode(); break;
		}
	}
	
	public void buttonReleased(int e)
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
	
	public void setupCamera(GL2 gl, GLU glu)
	{
		float _trajectory = trajectory;
		
		switch(camera.getMode())
		{	
			case DYNAMIC_VIEW:	 displayModel =  true;
								 if(slipping) _trajectory = slipTrajectory; break;
			case BIRDS_EYE_VIEW: break;
			case DRIVERS_VIEW:   displayModel = false; break;
			case FREE_LOOK_VIEW:
			{
				displayModel =  true;
				
				if(controller.isEnabled())
				{
					float x  = controller.getXAxis();
					float y  = controller.getYAxis();
					float rx = controller.getXRotation();
					float ry = controller.getYRotation();
					
					if     (x > 0) camera.moveLeft(x);
					else if(x < 0) camera.moveRight(-x);
					
					if     (y > 0) camera.moveForward(y);
					else if(y < 0) camera.moveBackward(-y);
					
					if     (rx > 0) camera.lookRight(rx);
					else if(rx < 0) camera.lookLeft(-rx);
					
					if     (ry > 0) camera.lookDown(ry);
					else if(ry < 0) camera.lookUp(-ry);
				}
				
				break;
			}
			
			default: break;	
		}
		camera.setupView(gl, glu, getPosition(), _trajectory);
	}
}