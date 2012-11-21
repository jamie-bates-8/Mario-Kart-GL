import static graphics.util.Vector.multiply;
import static graphics.util.Vector.subtract;
import static graphics.util.Vector.add;
import static graphics.util.Matrix.getRotationMatrix;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

//TODO Zoom camera in when obscured by an obstacle

public class Camera
{
	private CameraMode mode = CameraMode.DYNAMIC_VIEW;
	
	private float zoom = 0.75f;
	
	private float[] c;
	private static final float[] ORIGIN = {0, 10, 0};
	
	private float[][] u;
	private static final float[][] DEFAULT_ROTATION = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
	private float rx, ry, rz;
	private float sensitivity = 2.0f;
	private static final float MIN_SENSITIVITY = 0.25f;
	private static final float MAX_SENSITIVITY = 4.0f;
	private static final float SENSITIVITY_INC = 0.25f;
	private static final float MAX_PITCH = 60.0f;
	
	private boolean rearview = false;
	
	public Camera()
	{
		c = ORIGIN;
		u = DEFAULT_ROTATION;
		rx = ry = rz = 0;
	}
	
	public CameraMode getMode() { return mode; }
	
	public void moveLeft(float d)     { c = 	 add(c, multiply(u[2], d)); }
	public void moveRight(float d)    { c = subtract(c, multiply(u[2], d)); }
	public void moveForward(float d)  { c = subtract(c, multiply(u[0], d)); }
	public void moveBackward(float d) { c =      add(c, multiply(u[0], d)); }
	
	public void lookLeft(float theta)  { ry += theta * sensitivity; u = getRotationMatrix(rx, ry, rz); }
	public void lookRight(float theta) { ry -= theta * sensitivity; u = getRotationMatrix(rx, ry, rz); }
	
	public void lookUp(float theta)
	{
		if(rz > -MAX_PITCH) rz -= theta * sensitivity;
		u = getRotationMatrix(rx, ry, rz);
	}
	
	public void lookDown(float theta)
	{
		if(rz <  MAX_PITCH) rz += theta * sensitivity;
		u = getRotationMatrix(rx, ry, rz);
	}
	
	public void increaseSensitivity()
	{
		if(sensitivity < MAX_SENSITIVITY) sensitivity += SENSITIVITY_INC;
	}
	
	public void decreaseSensitivity()
	{
		if(sensitivity > MIN_SENSITIVITY) sensitivity -= SENSITIVITY_INC;
	}
	
	public float getSensitivity() { return sensitivity; }
	
	public float[] getPosition() { return c; }
	
	public void setRearview(boolean mirror) { rearview = mirror; }
	
	public void setupView(GL2 gl, GLU glu, float[] p, float trajectory)
	{
		switch(mode)
		{	
			//Cause the camera to follow the car dynamically as it moves along the track 
			case DYNAMIC_VIEW:
			{	
				if(rearview)
				{
					gl.glTranslatef(0, -15.0f * zoom, -30.0f * zoom);
					gl.glRotated(trajectory + 180, 0.0f, -1.0f, 0.0f);
	
					glu.gluLookAt(p[0], p[1], p[2],
							      p[0] - 10, p[1], p[2],
							      0, 1, 0);
				}
				else
				{
					gl.glTranslatef(0, -15.0f * zoom, -30.0f * zoom);
					gl.glRotated(trajectory, 0.0f, -1.0f, 0.0f);
	
					glu.gluLookAt(p[0], p[1], p[2],
							      p[0] - 10, p[1], p[2],
							      0, 1, 0);
				}

				break;
			}
			//Focus the camera on the centre of the track from a bird’s eye view
			case BIRDS_EYE_VIEW:
			{
				gl.glMatrixMode(GL_PROJECTION);
				gl.glLoadIdentity();
				gl.glOrtho(-200, 200, -200, 200, 1, 200);
				glu.gluLookAt(0, 150, 0,
					          0, 0, 0,
					          0, 0, 1);
				gl.glMatrixMode(GL_MODELVIEW);
				gl.glLoadIdentity();

				break;
			}
			//Setup the camera to view the scene from the driver's perspective
			case DRIVERS_VIEW:
			{	
				gl.glTranslatef(0, -3.0f, 0);
				gl.glRotated(trajectory, 0.0f, -1.0f, 0.0f);
				
				glu.gluLookAt(p[0], p[1], p[2],
							  p[0] - 10, p[1], p[2],
					          0, 1, 0);
				
				break;
			}
			case FREE_LOOK_VIEW:
			{
				p = subtract(c, multiply(u[0], 20));
				
				glu.gluLookAt(c[0], c[1], c[2],
						      p[0], p[1], p[2],
				              u[1][0], u[1][1], u[1][2]);
			}
			
			default: break;
		}
	}
	
	public boolean isDynamic()     { return mode == CameraMode.DYNAMIC_VIEW;   }
	public boolean isAerial()      { return mode == CameraMode.BIRDS_EYE_VIEW; }
	public boolean isFirstPerson() { return mode == CameraMode.DRIVERS_VIEW;   }
	public boolean isFree()        { return mode == CameraMode.FREE_LOOK_VIEW; }
	
	public void cycleMode()
	{
		mode = CameraMode.cycle(mode);
		if(isFree())
		{
			c = ORIGIN;
			u = DEFAULT_ROTATION;
			rx = ry = rz = 0;
		}
	}
	
	public enum CameraMode
	{
		DYNAMIC_VIEW,
		BIRDS_EYE_VIEW,
		DRIVERS_VIEW,
		FREE_LOOK_VIEW;
		
		public static CameraMode cycle(CameraMode mode)
		{
			return values()[(mode.ordinal() + 1) % values().length];
		}
	}
}
