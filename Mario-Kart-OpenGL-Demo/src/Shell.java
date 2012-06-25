import static graphics.util.Renderer.displayColoredObject;
import static graphics.util.Vector.add;
import static graphics.util.Vector.multiply;
import static graphics.util.Vector.subtract;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;
import graphics.util.Face;

import java.util.List;

import javax.media.opengl.GL2;

public abstract class Shell extends Item
{
	protected static final List<Face> SHELL_FACES = OBJParser.parseTriangles("obj/shell.obj");
	protected static final List<Face> RIM_FACES   = OBJParser.parseTriangles("obj/shellRim.obj");
	
	protected static int rimList = -1;
	
	protected static final float RADIUS = 2.0f;
	
	public boolean orbiting;
	public float rotation;
	
	public Shell(GL2 gl, Car car, float[] c, float trajectory)
	{ 
		if(rimList == -1)
		{
			rimList = gl.glGenLists(1);
			gl.glNewList(rimList, GL2.GL_COMPILE);
		    displayColoredObject(gl, RIM_FACES, new float[] {1, 1, 1});
		    gl.glEndList();
		}
		
	    this.car = car;
	    
		bound = new Sphere(c, RADIUS);
		
		setRotation(0, trajectory, 0);
		this.trajectory = trajectory;
		
		rotation = trajectory;
	}

	@Override
	public void render(GL2 gl)
	{
		
	}

	@Override
	public void hold()
	{
		if(orbiting)
		{
			rotation -= 10;
			float radius = car.bound.e[0] * 1.1f + Shell.RADIUS;
			
			float c = (float) cos(toRadians(rotation));
			float s = (float) sin(toRadians(rotation));
					
			float x = car.getPosition()[0] + radius * c;
			float y = car.getPosition()[1];
			float z = car.getPosition()[2] + radius * s;

			setPosition(x, y, z);
		}
		else
		{	
			setPosition(car.getBackwardItemVector(this, 1));
			rotation = car.trajectory;
		}
	}

	@Override
	public void collide(Item item)
	{
		if(item instanceof Shell  ||
		   item instanceof Banana   )
		{
			this.destroy();
			item.destroy();
		}
	}
	
	public float[][] getAxisVectors()
	{
		return new float[][]
		{
			subtract(bound.c, multiply(u[0],  RADIUS)), //front
				 add(bound.c, multiply(u[0],  RADIUS)), //back
				 add(bound.c, multiply(u[2],  RADIUS)), //left
			subtract(bound.c, multiply(u[2],  RADIUS)), //right
		};
	}
	
	public float[] getRotationAngles(float[] heights)
	{
		float diameter = Shell.RADIUS * 2;

		float xrot = (float) -toDegrees(atan((heights[2] - heights[3]) / diameter));
		float yrot = trajectory;
		float zrot = (float) -toDegrees(atan((heights[0] - heights[1]) / diameter));
		
		return new float[] {xrot, yrot, zrot};
	}
}
