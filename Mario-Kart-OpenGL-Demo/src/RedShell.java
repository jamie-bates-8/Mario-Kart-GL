import static graphics.util.Renderer.displayWildcardObject;
import static graphics.util.Vector.*;

import java.io.File;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class RedShell extends Shell
{
	private static int shellList = -1;
	
	private static Texture[] textures;
	private static Texture shellTop;
	
	static
	{
		try
		{
			shellTop = TextureIO.newTexture(new File("tex/redShellTop.jpg"), true);
			
			textures = new Texture[] {shellTop};
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	private static final float INITIAL_VELOCITY = 2.0f;
	private static final float TOP_SPEED = 3.0f;
	
	private float acceleration = 0.0125f;
	
	private Car target;
	private boolean locked = true;
	
	public RedShell(GL2 gl, Scene scene, Car car, float trajectory, boolean orbiting)
	{
		super(gl, scene, car, trajectory);
		
		if(shellList == -1)
		{
			shellList = gl.glGenLists(1);
			gl.glNewList(shellList, GL2.GL_COMPILE);
			displayWildcardObject(gl, SHELL_FACES, textures);
			gl.glEndList();
			
			System.out.println("Red Shell: " + (SHELL_FACES.size() + RIM_FACES.size()) + " faces");
		}
	    
		velocity = INITIAL_VELOCITY;
		
		this.orbiting = orbiting;
		
		Car target = null;
		float min_distance = Float.MAX_VALUE;
		
		for(Car c : scene.getCars())
		{
			if(!c.equals(car))
			{
				float distance = dot(c.getPosition(), car.getPosition());
				if(distance < min_distance)
				{
					min_distance = distance;
					target = c;
				}
			}
		}
		
		if(target == null) locked = false;
		this.target = target;
	}
	
	@Override
	public void render(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{
			gl.glTranslatef(bound.c[0], bound.c[1], bound.c[2]);
			gl.glRotatef(rotation, 0, 1, 0);
			gl.glScalef(1.5f, 1.5f, 1.5f);
			
			gl.glCallList(shellList);
			gl.glCallList(rimList);
		}
		gl.glPopMatrix();
	}
	
	@Override
	public void update()
	{	
		if(target != null)
			if(target.isInvisible()) locked = false; 
		
		if(velocity < TOP_SPEED && !locked) velocity += acceleration;
		
		setPosition(getPositionVector());
		if(falling) fall();
		
		detectCollisions();
		resolveCollisions();
		
		if(!thrown && locked && target != null) trajectory = target.trajectory;
		setRotation(getRotationAngles(getHeights()));
		rotation += 10 * velocity;	
	}
	
	@Override
	public float[] getPositionVector()
	{
		if(thrown || !locked)
			return subtract(getPosition(), multiply(u[0], velocity));
		else
		{
			float[]  t = subtract(target.getPosition(), getPosition());
			float[] _t = normalize(t);
			_t[1] = 0;
			
			return add(getPosition(), multiply(_t, velocity));
		}
	}
	
	@Override
	public void rebound(Bound b) { destroy(); }
	
	@Override
	public void collide(Car car) { car.spin(); destroy(); }
}
