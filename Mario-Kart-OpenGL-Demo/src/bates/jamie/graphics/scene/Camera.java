package bates.jamie.graphics.scene;

import static bates.jamie.graphics.util.Vector.multiply;
import static bates.jamie.graphics.util.Vector.subtract;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

import java.awt.event.MouseEvent;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

//TODO Zoom camera in when obscured by an obstacle

public class Camera extends AnchorPoint
{
	private CameraMode mode = CameraMode.DYNAMIC_VIEW;
	
	private float[] position = {0, 0, 0};
	
	public float zoom = 1.5f;
	
	private int width  = 860;
	private int height = 640;
	
	public boolean  rearview = false;
	public boolean   shaking = true;
	public boolean trackball = false;
	
	public int mouseX = 0;
	public int mouseY = 0;
	
	private static final float EPSILON = 0.001f;
	
	public float incline = (float) Math.toRadians( -60);
	public float azimuth = (float) Math.toRadians(-180);
	
	
	
	public Camera() {}
	
	public CameraMode getMode() { return mode; }
	
	public void setRotation(float ry) { this.ry = ry; }
	
	public void setRearview(boolean mirror) { rearview = mirror; }
	
	public void mouseDragged(MouseEvent e)
	{
		int x = e.getX();
		int y = e.getY();
		
		azimuth += (x - mouseX) * 0.01f; // 0 < theta < PI
		incline += (y - mouseY) * 0.01f; // 0 < phi < 2 PI
		
		if(incline == 0) incline = EPSILON;
		
		mouseX = x; 
		mouseY = y; 
	}
	
	public void mouseMoved(MouseEvent e)
	{
		int x = e.getX();
		int y = e.getY();
		
		mouseX = x; 
		mouseY = y; 
	}
	
	@Override
	public float[] getPosition() { return position; }
	
	public float[] getSphericalCoordinate()
	{
		float cameraX = (float) (c[0] + zoom * 10 * Math.sin(incline) * Math.cos(azimuth));
		float cameraY = (float) (c[1] + zoom * 10 * Math.cos(incline));
		float cameraZ = (float) (c[2] + zoom * 10 * Math.sin(incline) * Math.sin(azimuth));
		
		return new float[] {cameraX, cameraY, cameraZ};
	}
	
	public void setupView(GL2 gl, GLU glu)
	{
		switch(mode)
		{	
			//Cause the camera to follow the car dynamically as it moves along the track 
			case DYNAMIC_VIEW:
			{
				if(!trackball)
				{
					int rear = rearview ? 0 : -180;
					
					incline = (float) Math.toRadians(     -60);
					azimuth = (float) Math.toRadians(rear -ry);
					   zoom = 1.5f;
				}
				
				position = getSphericalCoordinate();
				
				glu.gluLookAt(position[0], position[1], position[2], c[0], c[1], c[2], 0, 1, 0);

				break;
			}
			//Focus the camera on the centre of the track from a bird’s eye view
			case BIRDS_EYE_VIEW:
			{		
				float ratio = (float) width / height;
				int size = 220;
				
				position = new float[] {c[0], 150, c[2]};
				
				gl.glMatrixMode(GL_PROJECTION);
				gl.glLoadIdentity();
				
				gl.glOrtho(-size * ratio, size * ratio, -size, size, 1, 2000);
				glu.gluLookAt(c[0], 150, c[2], c[0], 0, c[2], 0, 0, 1);
				// up-vector cannot be 'up' when camera is looking 'down'
				
				gl.glMatrixMode(GL_MODELVIEW);
				gl.glLoadIdentity();

				break;
			}
			//Setup the camera to view the scene from the driver's perspective
			case DRIVERS_VIEW:
			{	
				gl.glTranslatef(0, -3.0f, 0);
				gl.glRotated(ry, 0.0f, -1.0f, 0.0f);
				
				position = c;
				
				float[] v = shaking ? u[1] : new float[] {0, 1, 0};
				
				glu.gluLookAt(c[0] +  0, c[1], c[2],
							  c[0] - 10, c[1], c[2],
					          v[0], v[1], v[2]);
				
				break;
			}
			case FREE_LOOK_VIEW:
			{		
				float[] p = subtract(c, multiply(u[0], 20));
				
				position = c;
				
				glu.gluLookAt(c[0], c[1], c[2],
						      p[0], p[1], p[2],
				              u[1][0], u[1][1], u[1][2]);
			}
			
			default: break;
		}
	}
	
	public float[] to3DPoint(int x, int y, int width, int height)
	{
		float i = (float) x / width ;
		float j = (float) y / height;
		
		float ratio = (float) width / height;
		
		float[] p = {0, 0, 0};
		p[0] = 220 * ratio - i * 440 * ratio;
		p[2] = 220 - j * 440;
		
		return p;
	}
	
	public float getRadius(float retical, int scale)
	{	
		return 440 * retical / scale;
	}
	
	public boolean isDynamic()     { return mode == CameraMode.DYNAMIC_VIEW;   }
	public boolean isAerial()      { return mode == CameraMode.BIRDS_EYE_VIEW; }
	public boolean isFirstPerson() { return mode == CameraMode.DRIVERS_VIEW;   }
	public boolean isFree()        { return mode == CameraMode.FREE_LOOK_VIEW; }
	
	public void cycleMode()
	{
		mode = CameraMode.cycle(mode);
		
		if(isFree()) reset();
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

	public void setDimensions(int width, int height)
	{
		this.width  = width ;
		this.height = height;
	}
	
	public void zoom(int increments)
	{
		zoom += increments * 0.05;
		
		if(zoom < 1.00) zoom = 1.00f;
		if(zoom > 2.50) zoom = 2.50f;
	}
}
