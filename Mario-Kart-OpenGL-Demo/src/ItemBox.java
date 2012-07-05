
import static graphics.util.Renderer.*;
import graphics.util.Face;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import static javax.media.opengl.GL2.*;


public class ItemBox
{
	private static final List<Face> BOX_FACES = OBJParser.parseTriangles("obj/itemBox.obj");
	private static float rotation = 45.0f; 
	public static final float SCALE = 1.75f;
	public static final int RESPAWN_TIME = 60;
	
	private static Texture questionMark;
	
	static
	{
		try { questionMark = TextureIO.newTexture(new File("tex/questionMark.png"), true); }
		catch (Exception e) { e.printStackTrace(); }
	}
	
	private int respawnTimer = 0;
	private Sphere bound;
	
	private ParticleGenerator generator = new ParticleGenerator();
	private List<Particle> particles;
	
	public ItemBox(float[] c, List<Particle> particles)
	{
		c[1] += 3;
		
		bound = new Sphere(c, 2.5f);
		
		this.particles = particles;
	}
	
	public ItemBox(float x, float y, float z, List<Particle> particles)
	{
		bound = new Sphere(x, y + 3, z, 2.5f);
		
		this.particles = particles;
	}
	
	public float[] getPosition() { return bound.c; }
	
	public static void increaseRotation() { rotation += 4; }
	
	public void render(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
			gl.glBlendFunc(GL2.GL_DST_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
			gl.glDepthMask(false);
			
			gl.glTranslatef(bound.c[0], bound.c[1], bound.c[2]);
			gl.glRotatef(trajectory - 90, 0, 1, 0);
			gl.glRotatef(180, 0, 0, 1);
			gl.glScalef(3.0f, 3.0f, 3.0f);
			
			questionMark.bind(gl);

			gl.glBegin(GL_QUADS);
			{
				gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-0.5f, -0.5f, 0.0f);
				gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-0.5f,  0.5f, 0.0f);
				gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f( 0.5f,  0.5f, 0.0f);
				gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 0.5f, -0.5f, 0.0f);
			}
			gl.glEnd();
			
			gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
			gl.glDepthMask(true);
		}
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		{
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
			
			gl.glTranslatef(bound.c[0], bound.c[1], bound.c[2]);
			gl.glRotatef(rotation, 1, 1, 1);
			gl.glScalef(SCALE, SCALE, SCALE);
			
			displayPartiallyTexturedObject(gl, BOX_FACES, new float[] {0.5f, 0.5f, 0.5f});
			
			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
		}
		gl.glPopMatrix();
	}
	
	public void destroy() { respawnTimer = RESPAWN_TIME; }
	
	public boolean isDead() { return respawnTimer > 0; }
	
	public static List<ItemBox> generateSquare(float x, float y, List<Particle> particles)
	{
		List<ItemBox> boxes = new ArrayList<ItemBox>();
		
		boxes.add(new ItemBox( x,  y,  x, particles));
		boxes.add(new ItemBox(-x,  y,  x, particles));
		boxes.add(new ItemBox(-x,  y, -x, particles));
		boxes.add(new ItemBox( x,  y, -x, particles));
		
		return boxes;
	}
	
	public static List<ItemBox> generateDiamond(float x, float y, List<Particle> particles)
	{
		List<ItemBox> boxes = new ArrayList<ItemBox>();
		
		boxes.add(new ItemBox( x,  y,  0, particles));
		boxes.add(new ItemBox(-x,  y,  0, particles));
		boxes.add(new ItemBox( 0,  y,  x, particles));
		boxes.add(new ItemBox( 0,  y, -x, particles));
		
		return boxes;
	}
	
	public void update(Car car)
	{
		if(!isDead())
		{
			if(car.bound.testSphere(bound))
			{
				destroy();
				
				ItemRoulette roulette = car.getRoulette();
				
				if(!roulette.isAlive() && !car.isCursed())
				{
					roulette.spin();
					roulette.secondary = car.hasItem();
				}
				
				particles.addAll(generator.generateItemBoxParticles(getPosition(), 64));
			}
		}
		else respawnTimer--;
	}
}
