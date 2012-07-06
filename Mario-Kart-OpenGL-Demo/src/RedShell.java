
import static graphics.util.Renderer.displayWildcardObject;
import static graphics.util.Vector.*;

import java.io.File;
import java.util.List;

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
	
	public RedShell(GL2 gl, Car car, float trajectory, boolean orbiting, Car target)
	{
		super(gl, car, trajectory);
		
		if(shellList == -1)
		{
			shellList = gl.glGenLists(1);
			gl.glNewList(shellList, GL2.GL_COMPILE);
			displayWildcardObject(gl, SHELL_FACES, textures);
			gl.glEndList();
		}
	    
		velocity = INITIAL_VELOCITY;
		
		this.orbiting = orbiting;	
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
	public void update(List<Bound> bounds)
	{	
		if(velocity < TOP_SPEED) velocity += acceleration;
		
		setPosition(getPositionVector());
		if(falling) fall();
		
		detectCollisions(bounds);
		resolveCollisions();
		
		if(!thrown) trajectory = target.trajectory;
		setRotation(getRotationAngles(getHeights()));
		rotation += 10 * velocity;	
	}
	
	@Override
	public float[] getPositionVector()
	{
		if(thrown)
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
}
