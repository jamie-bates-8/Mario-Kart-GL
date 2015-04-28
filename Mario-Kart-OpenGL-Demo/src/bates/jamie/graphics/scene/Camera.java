package bates.jamie.graphics.scene;

import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import bates.jamie.graphics.util.Matrix;
import bates.jamie.graphics.util.Vec3;

//TODO Zoom camera in when obscured by an obstacle

public class Camera extends AnchorPoint
{
	private CameraMode mode = CameraMode.DYNAMIC_VIEW;
	
	private Vec3 position = new Vec3();
	
	public float zoom = 1.5f;
	
	private int width  = 860;
	private int height = 640;
	
	public boolean  rearview = false;
	public boolean   shaking = true;
	public boolean trackball = false;
	
	public int mouseX = 0;
	public int mouseY = 0;
	
	private static final float EPSILON = 1E-3f;
	
	private static final int INCLINE = -60;
	private static final int AZIMUTH = -90;
	
	public float incline = (float) Math.toRadians(INCLINE);
	public float azimuth = (float) Math.toRadians(AZIMUTH);
	
	private float[] viewMatrix = Arrays.copyOf(Matrix.IDENTITY_MATRIX_16, 16);
	
	
	
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
	public Vec3 getPosition() { return position; }
	
	public Vec3 getSphericalCoordinate()
	{
		float cameraX = (float) (c.x + zoom * 10 * Math.sin(incline) * Math.cos(azimuth));
		float cameraY = (float) (c.y + zoom * 10 * Math.cos(incline));
		float cameraZ = (float) (c.z + zoom * 10 * Math.sin(incline) * Math.sin(azimuth));
		
		return new Vec3(cameraX, cameraY, cameraZ);
	}
	
	public void setupView(GL2 gl)
	{
		GLU glu = new GLU();
		
		switch(mode)
		{	
			//Cause the camera to follow the car dynamically as it moves along the track 
			case DYNAMIC_VIEW:
			{
				if(!trackball)
				{
					int rear = rearview ? 90 : AZIMUTH;
					
					incline = (float) Math.toRadians(INCLINE);
					azimuth = (float) Math.toRadians(rear + ry);
				}
				
				position = getSphericalCoordinate();
				
				glu.gluLookAt(position.x, position.y, position.z, c.x, c.y, c.z, 0, 1, 0);
				
				u.yAxis = new Vec3(0, 1, 0);
				u.zAxis = position.subtract(c).normalize();
				u.xAxis = u.yAxis.cross(u.zAxis).normalize();

				break;
			}
			//Focus the camera on the centre of the track from a bird’s eye view
			case BIRDS_EYE_VIEW:
			{		
				float ratio = (float) width / height;
				int size = 220;
				
				position = new Vec3(c.x, 150, c.z);
				
				gl.glMatrixMode(GL_PROJECTION);
				gl.glLoadIdentity();
				
				gl.glOrtho(-size * ratio, size * ratio, -size, size, 1, 2000);
				glu.gluLookAt(c.x, 150, c.z, c.x, 0, c.z, 0, 0, 1);
				// up-vector cannot be 'up' when camera is looking 'down'
				
				gl.glMatrixMode(GL_MODELVIEW);
				gl.glLoadIdentity();

				break;
			}
			//Setup the camera to view the scene from the driver's perspective
			case DRIVERS_VIEW:
			{	
				gl.glTranslatef(0, -3.0f, 0);
				gl.glRotated(ry, 0.0f, 1.0f, 0.0f);
				
				position = c;
				
				Vec3 v = shaking ? u.yAxis : Vec3.POSITIVE_Y_AXIS;
				
				glu.gluLookAt(c.x, c.y, c.z +  0,
							  c.x, c.y, c.z - 10,
					          v.x, v.y, v.z);
				
				break;
			}
			case FREE_LOOK_VIEW:
			{		
				Vec3 p = c.subtract(u.zAxis.multiply(20));
				Vec3 u = this.u.yAxis;
				
				position = c;
				
				glu.gluLookAt(c.x, c.y, c.z,
						      p.x, p.y, p.z,
				              u.x, u.y, u.z);
			}
			
			default: break;
		}
		
		gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, viewMatrix, 0);
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
	
	/**
	 * This method returns the rotation matrix that describes the current
	 * orientation of this camera. This negative z-axis of this matrix is
	 * considered to be the direction that the camera is looking in. It
	 * should be noted that a full 16-value array in column-major format
	 * is returned, not just the orientation. Any values that represent
	 * scale or translation are set to identity values.
	 * 
	 * @return the orientation of this camera as a 16-value array in
	 * column-major format, equivalent to the transformation matrices
	 * used internally by OpenGL.
	 */
	public float[] getMatrix()
	{
		viewMatrix[ 3] = viewMatrix[ 7] = viewMatrix[11] = 0;
		viewMatrix[12] = viewMatrix[13] = viewMatrix[14] = 0;
		viewMatrix[15] = 1;
		
		return viewMatrix;
	}
}
