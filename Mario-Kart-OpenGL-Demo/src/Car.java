import static java.lang.Math.*;

import static graphics.util.Vector.*;
import static graphics.util.Matrix.*;

import static graphics.util.Renderer.*;

import graphics.util.Face;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.media.opengl.GL2;

public class Car
{
	/** Model Fields **/
	private static final List<Face> CAR_FACES         = OBJParser.parseTriangles("obj/car.obj");
	private static final List<Face> WHEEL_FACES       = OBJParser.parseTriangles("obj/wheel.obj");
	private static final List<Face> WINDOW_FACES      = OBJParser.parseTriangles("obj/windows.obj");
	private static final List<Face> DOOR_WINDOW_FACES = OBJParser.parseTriangles("obj/door_windows.obj");
	private static final List<Face> DOOR_FACES        = OBJParser.parseTriangles("obj/door.obj");
	
	private static final float[] ORIGIN = {0, 1.8f, 0};
	
	private static int carList;
	
	private float[] color = {1.0f, 0.4f, 0.4f};
	private float[] windowColor = {0.4f, 0.8f, 1.0f};
	
	public boolean displayModel = true;
	
	
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
	private double acceleration = 0.012;
	public boolean accelerating = false;
	public boolean reversing = false;
	
	private double gravity = 0.05;
	public boolean falling = false;
	private float fallRate = 0.0f;
	private static final double TOP_FALL_RATE = 2.5;
	
	private double turnRate = 0;
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
	private List<Particle> particles;
	private ParticleGenerator generator = new ParticleGenerator();
	
	
	/** Item Fields **/
	private ItemRoulette roulette = new ItemRoulette();
	private ItemState itemState = ItemState.NO_ITEM;
	private Queue<Item> items = new LinkedList<Item>();
	private Queue<Integer> worldItemQueue;
	public List<Item> worldItemList;
	
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
	
	
	public Car(GL2 gl, float[] c, float xrot, float yrot, float zrot,
			List<Item> worldItemList, Queue<Integer> worldItemQueue, List<Particle> particles)
	{
		//Using a display list ensures that the complex car model is displayed quickly
		carList = gl.glGenLists(1);
		gl.glNewList(carList, GL2.GL_COMPILE_AND_EXECUTE);
	    displayPartiallyTexturedObject(gl, CAR_FACES, color);
	    gl.glEndList();
	    
	    bound = new OBB(
				c[0], c[1], c[2],
	    		xrot, yrot, zrot,
	    		5.5f, 2.0f, 2.7f);
	    
	    this.worldItemList = worldItemList;
	    this.worldItemQueue = worldItemQueue;
	    this.particles = particles;
		
	    trajectory = yrot;	
	}
	
	public Queue<Item> getItems() { return items; }
	
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
			
			worldItemQueue.add(itemID);
			
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
			case  0: items.add(new GreenShell(gl, this, trajectory, false)); break;
			case  1:
			{
				items.add(new GreenShell(gl, this, trajectory,       true));
				items.add(new GreenShell(gl, this, trajectory + 120, true));
				items.add(new GreenShell(gl, this, trajectory - 120, true));

				break;
			}
			case  2: items.add(new RedShell(gl, this, trajectory, false, this)); break;
			case  3:
			{
				items.add(new RedShell(gl, this, trajectory,       true, this));
				items.add(new RedShell(gl, this, trajectory + 120, true, this));
				items.add(new RedShell(gl, this, trajectory - 120, true, this));

				break;
			}
			case  6: itemDuration = 400; break;
			case  7: items.add(new FakeItemBox(gl, this, particles)); break;
			case  8: items.add(new Banana(gl, this, 1)); break;
			case  9:
			{
				items.add(new Banana(gl, this, 3));
				items.add(new Banana(gl, this, 2));
				items.add(new Banana(gl, this, 1));
				break;
			}
			case 13:
			{
				BlueShell shell =
					new BlueShell(gl, this, trajectory, particles);
					
				shell.throwUpwards();
					
				worldItemList.add(shell);
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
					
				worldItemList.add(shell);
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
				}
					
				worldItemList.add(banana);
				break;
			}
			
			case LIGHTNING_BOLT: useLightningBolt(); break;
			case POWER_STAR: usePowerStar(); break;
			case BOO: useBoo(); break;
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
					
				worldItemList.add(shell);
				break;
			}
			
			case FAKE_ITEM_BOX:
			case HOLDING_BANANA:
			{
				Item item = items.remove();
					
				switch(aiming)
				{
					case FORWARDS: item.throwUpwards(); break;
				}
						
				worldItemList.add(item);
				break;
			}

			case GOLDEN_MUSHROOM: superBoosting = false; break;
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
	
	public float[] getHeights()
	{
		float[] _heights = heights;
		heights = new float[] {0, 0, 0, 0};
		
		falling = true;
		
		for(Bound collision : collisions)
		{
			if(collision instanceof OBB) setHeights((OBB) collision);
		}
		
		if(collisions.isEmpty()) heights = _heights;
		
		return heights;
	}

	private void setHeights(OBB obb)
	{
		float[] face = obb.getFaceVector(getPosition());

		if(Arrays.equals(face, obb.getUpVector(1)))
		{
			float[][] vertices = bound.getVertices();

			for(int i = 0; i < 4; i++)
			{
				float h = obb.closestPointOnPerimeter(vertices[i])[1];
				if(h > heights[i]) heights[i] = h;
			}

			float h = obb.closestPointOnPerimeter(getPosition())[1]
					+ (bound.e[1] * 0.9f);
			
			if(h > bound.c[1]) bound.c[1] = h;

			falling = false;
			fallRate = 0;
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

	public void render(GL2 gl)
	{
		gl.glPushMatrix();
		{
			/***************************
			 * Vehicle Transformations *
			 ***************************/

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
				else gl.glCallList(carList);
				
				gl.glColor3f(1, 1, 1);
				
				for(int wheel = 0; wheel < 4; wheel++)
				{
					gl.glPushMatrix();
					{
						float x = offsets_Wheel[wheel][0];
						float y = offsets_Wheel[wheel][1];
						float z = offsets_Wheel[wheel][2];
						
						/*************************
						 * Wheel Transformations *
						 *************************/
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
						else displayTexturedObject(gl, WHEEL_FACES);
					}
					gl.glPopMatrix();
				}
				
				/*****************************
				 * Left Door Transformations *
				 *****************************/
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
					else displayColoredObject(gl, DOOR_FACES, color);
					
					/************************************
					 * Left Door Window Transformations *
					 ************************************/
					gl.glPushMatrix();
					{
						if(invisible) displayTransparentObject(gl, DOOR_WINDOW_FACES, booColor);
						else displayTransparentObject(gl, DOOR_WINDOW_FACES, windowColor);
					}
					gl.glPopMatrix();	
				}
				gl.glPopMatrix();
				
				/******************************
				 * Right Door Transformations *
				 ******************************/
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
					else displayColoredObject(gl, DOOR_FACES, color);
					
					/*************************************
					 * Right Door Window Transformations *
					 *************************************/
					gl.glPushMatrix();
					{	
						if(invisible) displayTransparentObject(gl, DOOR_WINDOW_FACES, booColor);
						else displayTransparentObject(gl, DOOR_WINDOW_FACES, windowColor);
					}
					gl.glPopMatrix();
				}
				gl.glPopMatrix();
				
				/**************************
				 * Window Transformations *
				 **************************/
				gl.glPushMatrix();
				{			
					gl.glTranslatef(0.3f, -1.2f, 0);
					
					if(invisible) displayTransparentObject(gl, WINDOW_FACES, booColor);
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

	public void detectCollisions(List<Bound> bounds)
	{
		colliding = false;
		
		collisions.clear();
		
		for(Bound collision : bounds)
		{
			if(bound.testBound(collision))
			{
				colliding = true;
				collisions.add(collision);
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

	private float[] resolveSphere(Sphere sphere)
	{
		if(colliding)
		{
			float[] face = sphere.getFaceVector(getPosition());
			face[1] = 0;
			
			float s = this.bound.getPenetration(sphere);
			
			velocity *= (slipping) ? 0 : 0.9;
			
			return multiply((normalize(subtract(face, sphere.getPosition()))), s);
		}
		else return new float[] {0, 0, 0};
	}

	private float[] resolveOBB(OBB obb)
	{
		float[] face = obb.getFaceVector(getPosition());

		if(colliding && !Arrays.equals(face, obb.getUpVector(1)) && obb.isValidCollision(face)
				&& (getPosition()[1] - bound.e[1]) < (obb.c[1] + obb.e[1]))
		{
			float p = bound.getPenetration(obb);
			
			if(slipping) velocity = 0;
			else velocity *= 0.9;
			
			return multiply(subtract(face, obb.getPosition()), p);
		}
		else return new float[] {0, 0, 0};
	}

	public void update(List<Bound> bounds)
	{
		setRotation(getRotationAngles(getHeights()));
		
		detectCollisions(bounds);
		resolveCollisions();
		
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
		
		else if(turning && velocity != 0 && !slipping)
		{
			if(direction == Direction.LEFT) turnLeft();
			else turnRight();
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
				particles.addAll(generator.generateDriftParticles(source, 10, driftState.ordinal()));
				particles.addAll(generator.generateSparkParticles(source,  2));
			}
		}
		
		updateStatus();
	}

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
				particles.addAll(generator.generateBoostParticles(source, boostDuration / 4, superBoosting));
		}
		
		if(curseDuration > 0) curseDuration--;
		else cursed = false;
		
		if(cursed)
			particles.addAll(generator.generateFakeItemBoxParticles(getPosition(), 2));
		
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
			particles.addAll(generator.generateStarParticles(getPosition(), 2));
		
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
		if(velocity != 0)
		{
			turning = true;
			direction = Direction.LEFT;
		}
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
		if(velocity != 0)
		{
			turning = true;
			direction = Direction.RIGHT;
		}
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
		if(turnRate < TOP_TURN_RATE) turnRate += turnIncrement;
		
		double k = 1;
		
		if(drift == Direction.LEFT)
		{
			if(direction == Direction.LEFT) k = 1.25;
			else if(direction == Direction.RIGHT) k = 0.5;
		}
		
		trajectory += turnRate * k;
	}

	private void turnRight()
	{
		if(turnRate > -TOP_TURN_RATE) turnRate -= turnIncrement;
		
		double k = 1;
		
		if(drift == Direction.RIGHT)
		{
			if(direction == Direction.LEFT) k = 0.5;
			else if(direction == Direction.RIGHT) k = 1.25;
		}
		
		trajectory += turnRate * k;
	}
	
	public void straighten() { direction = Direction.STRAIGHT; }

	public void stabilize()
	{
		if(turnRate > turnIncrement) turnRate -= turnIncrement;
		else if(turnRate < 0) turnRate += turnIncrement;
		else turnRate = 0;
		
		trajectory += turnRate;
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

	public void slipOnBanana()
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
			else slipOnBanana();
		}
		
		float[] source = getLightningVector(); 
		particles.add(new LightningParticle(source));
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
	
	public boolean isSlipping()   { return slipping; }

	public boolean isCursed()     { return cursed; }
	
	public boolean isMiniature()  { return miniature; }
	
	public boolean hasStarPower() { return starPower; }
	
	public boolean isInvisible()  { return invisible; }
	
	public boolean isBoosting()   { return boosting; }

	public void keyPressed(KeyEvent e)
	{
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
			
			case KeyEvent.VK_R: if(!cursed && !slipping) {aimForwards();  useItem();} break;
			case KeyEvent.VK_F: if(!cursed && !slipping) {resetAim();     useItem();} break;
			case KeyEvent.VK_C: if(!cursed && !slipping) {aimBackwards(); useItem();} break;
			
			case KeyEvent.VK_N: roulette.next(); break;
			case KeyEvent.VK_B: roulette.repeat(); break;
			case KeyEvent.VK_V: roulette.previous(); break;
			
			case KeyEvent.VK_Q: if(turning && !falling) drift(); break;
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
		}
	}
}


