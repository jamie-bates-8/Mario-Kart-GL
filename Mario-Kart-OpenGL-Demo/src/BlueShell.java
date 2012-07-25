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
	
	public ParticleGenerator generator;
	
	private int blastDuration = 0;
	private float blastRadius = 0;
	private float blastSpeed = 2.5f;
	
	public BlueShell(GL2 gl, Scene scene, Car car, float trajectory)
	{
		super(gl, scene, car, trajectory);
		
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
			
			System.out.println("Blue Shell: " + (SHELL_FACES.size() + RIM_FACES.size() + SPIKE_FACES.size()) + " faces");
		}
		
		generator = new ParticleGenerator();
	}
	
	@Override
	public void render(GL2 gl, float trajectory)
	{
		if(!dead)
		{
			gl.glPushMatrix();
			{
				gl.glTranslatef(bound.c[0], bound.c[1], bound.c[2]);
				gl.glRotatef(rotation, 0, 1, 0);
				gl.glScalef(1.5f, 1.5f, 1.5f);
				
				gl.glCallList(shellList);
				gl.glCallList(rimList);
				gl.glCallList(spikeList);
			}
			gl.glPopMatrix();
		}
	}
	
	@Override
	public void update()
	{
		if(!dead)
		{
			setPosition(getPositionVector());
			if(falling) fall();
	
			for(Bound bound : scene.getBounds())
				if(bound.testBound(this.bound))
					{ destroy(); break; }
	
			rotation += 10 * velocity;
		}
		else if(blastDuration > 0)
		{
			bound = new Sphere(getPosition(), blastRadius);
			blastRadius += blastSpeed;
			blastSpeed *= 0.9f;
			blastDuration--;
		}
	}
	
	@Override
	public void destroy()
	{
		if(!dead)
		{
			blastDuration = 60;
			scene.addParticles(generator.generateBlastParticles(getPosition(), 600));
		}
		
		dead = true;
	}
	
	@Override
	public boolean isDead() { return dead && blastDuration < 1; }
	
	@Override
	public void collide(Car car)
	{
		if(!dead) destroy();
		else if(blastDuration > 50)
		{
			car.velocity = 0;
			car.spin();
		}
		else if(blastDuration >  0) car.spin();
	}
}
