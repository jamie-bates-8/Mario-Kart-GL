import static graphics.util.Matrix.getEulerAngles;
import static graphics.util.Renderer.displayWildcardObject;

import java.io.File;
import java.util.Arrays;
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
	public static final int MAX_DURABILITY = 20;
	
	public int durability;
	
	private float[] _heights = {0, 0, 0, 0};
	
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
	
	public float[] getHeights()
	{
		float[] heights = {0, 0, 0, 0};
		
		falling = true;
		
		if(!detected.isEmpty())
		{
			for(Bound _bound : detected)
			{
				if(_bound instanceof OBB)
				{		
					OBB b = (OBB) _bound;
					
					float[] face = b.getFaceVector(getPosition());
	
					if(Arrays.equals(face, b.getUpVector(1)))
					{
						float[][] vertices = getAxisVectors();
						
						for(int i = 0; i < 4; i++)
						{
							float h = b.perimeterPointToPoint(vertices[i])[1];
							if(h > heights[i]) heights[i] = h;
						}
			
						float h = b.perimeterPointToPoint(getPosition())[1] + (getMaximumExtent() * 0.9f);
						if(h > bound.c[1]) bound.c[1] = h;
						
						falling = thrown = false;
						fallRate = 0;
					}
				}
			}
		}
		else return _heights;
		
		_heights = heights;
		
		return heights;
	}
	
	public void render(GL2 gl)
	{
		gl.glPushMatrix();
		{
			float[] angles = getEulerAngles(u);

			gl.glTranslatef(bound.c[0], bound.c[1], bound.c[2]);
			gl.glRotatef(rotation, 0, 1, 0);
			gl.glScalef(1.5f, 1.5f, 1.5f);
			
			{
				gl.glCallList(shellList);
				gl.glCallList(rimList);
			}
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
		fall();
		
		detected.clear();

		for(Bound bound : bounds)
			if(bound.testBound(this.bound))
				detected.add(bound);
		
		super.update(bounds);

		decelerate();
		setRotation(getRotationAngles(getHeights()));
		rotation += 10 * velocity;
		
		if(durability < 1) destroy();
	}
}
