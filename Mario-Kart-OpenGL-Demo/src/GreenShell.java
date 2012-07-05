
import static graphics.util.Renderer.displayWildcardObject;

import java.io.File;
import java.util.List;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;


public class GreenShell extends Shell
{	
	private static int shellList = -1;
	
	private static Texture[] textures;
	private static Texture shellTop;
	
	static
	{
		try
		{
			shellTop = TextureIO.newTexture(new File("tex/greenShellTop.jpg"), true);
			
			textures = new Texture[] {shellTop};
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public static final float MAX_VELOCITY = 2.0f;
	public static final float MIN_VELOCITY = 1.0f;
	public static final float ACCELERATION = 0.0025f;
	public static final int MAX_DURABILITY = 10;
	
	public int durability;
	
	public GreenShell(GL2 gl, Car car, float[] c, float trajectory, boolean orbiting)
	{
		super(gl, car, c, trajectory);
		
		if(shellList == -1)
		{
			shellList = gl.glGenLists(1);
			gl.glNewList(shellList, GL2.GL_COMPILE);
			displayWildcardObject(gl, SHELL_FACES, textures);
			gl.glEndList();
		}
	    
		velocity = MAX_VELOCITY;
		durability = MAX_DURABILITY;
		
		this.orbiting = orbiting;
	}
	
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
	public void rebound(Bound b)
	{
		super.rebound(b);
		durability--;
	}
	
	public void decelerate() { if(velocity > MIN_VELOCITY) velocity -= ACCELERATION; }
	
	@Override
	public void update(List<Bound> bounds)
	{
		setPosition(getPositionVector());
		if(falling) fall();
		
		detectCollisions(bounds);
		resolveCollisions();

		decelerate();
		setRotation(getRotationAngles(getHeights()));
		rotation += 10 * velocity;
		
		if(durability < 1) destroy();
	}
}
