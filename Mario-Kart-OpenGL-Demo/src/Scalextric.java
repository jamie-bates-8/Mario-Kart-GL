import static javax.media.opengl.GL2.*;
import static java.lang.Math.*;

import static graphics.util.Renderer.*;

import graphics.util.Face;

import java.util.List;

import javax.media.opengl.GL2;

public class Scalextric
{
	private static final List<Face> CAR_FACES         = OBJParser.parseTriangles("obj/car.obj");
	private static final List<Face> WHEEL_FACES       = OBJParser.parseTriangles("obj/wheel.obj");
	private static final List<Face> WINDOW_FACES      = OBJParser.parseTriangles("obj/windows.obj");
	private static final List<Face> DOOR_WINDOW_FACES = OBJParser.parseTriangles("obj/door_windows.obj");
	private static final List<Face> DOOR_FACES        = OBJParser.parseTriangles("obj/door.obj");
	
	private static int carList;
	
	/** Vehicle Fields **/
	public float trajectory;
	
	public float x, y, z;
	public float xr, yr, zr;
	
	private float scale = 1.5f;
	
	
	/** Wheel Fields **/
	private float yRotation_Wheel = 0.0f;
	private float zRotation_Wheel = 0.0f;
	
	private float[][] offsets_Wheel =
		{{ 2.4f, -0.75f,  1.75f},  //back-left
		 {-2.4f, -0.75f,  1.75f},  //front-left
		 { 2.4f, -0.75f, -1.75f},  //back-right
		 {-2.4f, -0.75f, -1.75f}}; //front-right
	
	
	/** Door Fields **/
	private float[] offsets_LeftDoor =  {-1.43f, 0.21f,  1.74f};
	private float[] offsets_RightDoor = {-1.43f, 0.21f, -1.74f};
	
	
	/** Fields that define the vehicle's motion **/
	public float velocity;
	public static final float TOP_SPEED = 1.2f;
	
	private double acceleration = 0.012;
	public boolean accelerating;
	
	public double distance;
	private static final double TRACK_LENGTH = (2 * 24 * PI) + (2 * 32);
	
	private double theta = 270.0;
	private double roundaboutRadius = 16.0;
	
	public boolean displayModel;
	
	public Scalextric(GL2 gl, float[] c, float xr, float yr, float zr)
	{
		//Using a display list ensures that the complex car model is displayed quickly
		carList = gl.glGenLists(1);
		gl.glNewList(carList, GL2.GL_COMPILE_AND_EXECUTE);
	    displayTexturedObject(gl, CAR_FACES);
	    gl.glEndList();
		
	    setPosition(c);
	    setRotation(xr, yr, zr);
	    
	    trajectory = yr;
		velocity = 0.0f;

		accelerating = false;

		displayModel = true;
		distance = 0.0;
	}
	
	public void setRotation(float xr, float yr, float zr)
	{	
		this.xr = xr;
		this.yr = yr;
		this.zr = zr;
	}
	
	public void setPosition(float[] p)
	{
		this.x = p[0];
		this.y = p[1];
		this.z = p[2];
	}
	
	public void setPosition(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public float[] getPosition() { return new float[] {x, y, z}; }
	
	public void render(GL2 gl)
	{
		gl.glPushMatrix();
		{
			/***************************
			 * Vehicle Transformations *
			 ***************************/

			gl.glTranslatef(x, y, z);
			gl.glRotatef(xr, 1, 0, 0);
			gl.glRotatef(yr, 0, 1, 0);
			gl.glRotatef(zr, 0, 0, 1);
			gl.glScalef(scale, scale, scale);
			//Display the car model by calling a display list
			if(displayModel)
			{	
				gl.glCallList(carList);
				
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
			
						displayTexturedObject(gl, WHEEL_FACES);
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
					
					displayTexturedObject(gl, DOOR_FACES);
					
					/************************************
					 * Left Door Window Transformations *
					 ************************************/
					gl.glPushMatrix();
					{
						//Enable window transparency
						gl.glDisable(GL_LIGHTING);
						gl.glEnable(GL_BLEND);
	
						displayTexturedObject(gl, DOOR_WINDOW_FACES);
						
						gl.glDisable(GL_BLEND);
						gl.glEnable(GL_LIGHTING);
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
					
					gl.glScalef(1, 1, -1);
					
					//Display the door model reflected in the z-axis
					displayTexturedObject(gl, DOOR_FACES);
					
					/*************************************
					 * Right Door Window Transformations *
					 *************************************/
					gl.glPushMatrix();
					{	
						//Enable window transparency
						gl.glDisable(GL_LIGHTING);
						gl.glEnable(GL_BLEND);
	
						//Display the door window model reflected in the z-axis
						displayTexturedObject(gl, DOOR_WINDOW_FACES);
						
						gl.glDisable(GL_BLEND);
						gl.glEnable(GL_LIGHTING);
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
					
					//Enable window transparency
					gl.glDisable(GL_LIGHTING);
					gl.glEnable(GL_BLEND);
	
					displayTexturedObject(gl, WINDOW_FACES);
					
					gl.glDisable(GL_BLEND);
					gl.glEnable(GL_LIGHTING);
				}
				gl.glPopMatrix();
				
				gl.glFlush();
			}
		}
		gl.glPopMatrix();
	}
	
	/**
	 * Increase the speed of the car unless it is at its top speed
	 */
	public void accelerate()
	{
		if(velocity < TOP_SPEED) velocity += acceleration;
		else if(velocity > TOP_SPEED) velocity -= acceleration;
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

	/**
	 * Whenever the car is accelerating this method is called to update the
	 * translation and rotation of the model every frame. The section of the
	 * track that the car is currently on is computed by performing modular
	 * arithmetic on the total distance travelled.
	 */
	public void drive()
	{
		if(accelerating) accelerate();
		else decelerate();
		
		double currentDistance = distance;

		if     (distance % TRACK_LENGTH <  24 * PI) turnLeft();
		else if(distance % TRACK_LENGTH < (24 * PI) + 32) driveStraight();
		else if(distance % TRACK_LENGTH < (24 * PI) + 32 + (24 * PI)) turnRight();
		else crossBridge();
		
		//The wheels are rotated in relation to the distance travelled
		zRotation_Wheel += 360 * (distance - currentDistance) / (2 * PI * 0.5);
	}

	/**
	 * This method is called to create a realistic motion path around the right turn
	 * of the track.
	 */
	private void turnLeft()
	{
			//The turning angle of the car is proportional to its speed
			theta      += (270 * velocity) / (24 * PI);
			trajectory += (270 * velocity) / (24 * PI);
			//Parameters equations defining the coordinates of a circle
			x = (float) -((roundaboutRadius * cos(toRadians(theta))) + roundaboutRadius);
			z = (float)  ((roundaboutRadius * sin(toRadians(theta))) + roundaboutRadius);
			//The y-rotation of the wheels around the corner can be computed by the cosine function
			yRotation_Wheel = (float) (45 * -cos((theta - 270) * PI / 135.0f) + 45) / 2;
			
			setRotation(0, trajectory, 0);
			distance += velocity;
	}

	/**
	 * This method is called to make the car drive straight during the straight
	 * section of the track.
	 */
	private void driveStraight()
	{
		z -= velocity;
		distance += velocity;
		theta = 360.0;
		trajectory = 270.0f;
	}

	/**
	 * This method is called to create a realistic motion path around the left turn
	 * of the track.
	 */
	private void turnRight()
	{
		//The turning angle of the car is proportional to its speed 
		theta      -= (270 * velocity) / (24 * PI);
		trajectory -= (270 * velocity) / (24 * PI);
		//Parameters equations defining the coordinates of a circle
		x = (float) -((roundaboutRadius * cos(toRadians(theta))) - roundaboutRadius);
		z = (float)  ((roundaboutRadius * sin(toRadians(theta))) - roundaboutRadius);
		//The y-rotation of the wheels around the corner can be computed by the cosine function
		yRotation_Wheel = (float) (45 * cos((360 - theta) * PI / 135.0f) - 45) / 2;

		setRotation(0, trajectory, 0);
		distance += velocity;
	}

	/**
	 * This method is called to create a realistic motion path across the bridge
	 * section of the track.
	 */
	private void crossBridge()
	{		
		//The height of the car is computed by the cosine function
		y = (float) (3.75 * (cos(x * PI / 16) + 1));
		//The tangent to this function is then computed to determine the gradient
		float tangent = (float) (-3.75 * PI * sin(PI * x / 16) / 16);
		//The gradient is then converted into the angle at which the car should be to the z-axis
		float zrot = (float) toDegrees(atan(tangent));
		//Based on the tangent, the car moves slower up the hill and faster down it
		if(accelerating) velocity += (velocity / 6) * tangent;
		else velocity += (0.1) * tangent;
		x -= velocity;
		
		distance += velocity;
		
		if(velocity > 0) theta = 270.0;
		else
		{
			theta = 90.0;
			trajectory = 0.0f;
		}
		
		setRotation(0, trajectory, zrot);
	}
	
	public void reset()
	{
		setRotation(0, 0, 0);
		trajectory = 0;

		setPosition(-16.0f, 0.0f, 0.0f);
		
		yRotation_Wheel = 0.0f;
		zRotation_Wheel = 0.0f;

		velocity = 0.0f;
		accelerating = false;

		distance = 0;
		theta = 270.0;
		
		displayModel = true;
	}
}


