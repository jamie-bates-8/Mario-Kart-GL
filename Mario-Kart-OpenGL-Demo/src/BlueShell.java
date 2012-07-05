
import static graphics.util.Matrix.getEulerAngles;
import static graphics.util.Renderer.displayWildcardObject;
import static graphics.util.Renderer.displayColoredObject;

import graphics.util.Face;

import java.io.File;
import java.util.List;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;


public class BlueShell extends Shell
{
	private static int shellList = -1;
	private static int spikeList = -1;
	private static final List<Face> SPIKE_FACES = OBJParser.parseTriangles("obj/spikes.obj");
	
	private static Texture[] textures;
	private static Texture shellTop;
	
	static
	{
		try
		{
			shellTop = TextureIO.newTexture(new File("tex/blueShellTop.jpg"), true);
			
			textures = new Texture[] {shellTop};
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	private static final float INITIAL_VELOCITY = 1.5f;
	private static final float TOP_SPEED = 3.0f;
	
	private float acceleration = 0.0125f;
	
	private Car target;
	
	public ParticleGenerator generator;

	private List<Particle> particles;
	
	private float[] _heights = {0, 0, 0, 0};
	
	public BlueShell(GL2 gl, Car car, float[] c, float trajectory, Car target, List<Particle> particles)
	{
		super(gl, car, c, trajectory);
		
		if(shellList == -1)
		{
			shellList = gl.glGenLists(1);
			gl.glNewList(shellList, GL2.GL_COMPILE);
			displayWildcardObject(gl, SHELL_FACES, textures);
			gl.glEndList();
		}
		
		if(spikeList == -1)
		{
			spikeList = gl.glGenLists(1);
			gl.glNewList(spikeList, GL2.GL_COMPILE);
			displayColoredObject(gl, SPIKE_FACES, 1);
			gl.glEndList();
		}
	    
		velocity = INITIAL_VELOCITY;
			
		this.target = target;
		this.particles = particles;
		
		generator = new ParticleGenerator();
	}
	
	@Override
	public void render(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{
			float[] angles = getEulerAngles(u);
			
			gl.glTranslatef(bound.c[0], bound.c[1], bound.c[2]);
			gl.glRotatef(rotation, 0, 1, 0);
			gl.glScalef(1.5f, 1.5f, 1.5f);
			
			gl.glCallList(shellList);
			gl.glCallList(rimList);
			gl.glCallList(spikeList);
		}
		gl.glPopMatrix();
	}
	
	@Override
	public void update(List<Bound> bounds)
	{	
		setPosition(getPositionVector());
		if(falling) fall();

		for(Bound bound : bounds)
			if(bound.testBound(this.bound))
				{ destroy(); break; }

		rotation += 10 * velocity;
	}
	
	public void destroy()
	{
		dead = true;
		particles.addAll(generator.generateBlastParticles(getPosition(), 600));
	}
}
