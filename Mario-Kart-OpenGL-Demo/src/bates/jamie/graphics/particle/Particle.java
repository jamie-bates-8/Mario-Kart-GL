package bates.jamie.graphics.particle;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.media.opengl.GL2;

import bates.jamie.graphics.util.Vec3;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public abstract class Particle
{
	protected static final String TEXTURE_DIRECTORY = "tex/particles/";
	
	protected static Texture reset;
	protected static Texture current;
	
	protected static Texture redFlare;
	protected static Texture orangeFlare;
	protected static Texture yellowFlare;
	protected static Texture greenFlare;
	protected static Texture blueFlare;
	protected static Texture indigoFlare;
	protected static Texture violetFlare;
	protected static Texture whiteFlare;
	
	protected static Texture yellowSpark;
	protected static Texture redSpark;
	protected static Texture blueSpark;
	protected static Texture whiteSpark;
	
	protected static Texture whiteStar;
	
	protected static Texture cloud1;
	
	static
	{
		try
		{
			redFlare    = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "redFlare.png"), true);
			orangeFlare = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "orangeFlare.png"), true);
			yellowFlare = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "yellowFlare.png"), true);
			greenFlare  = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "greenFlare.png"), true);
			blueFlare   = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "blueFlare.png"), true);
			indigoFlare = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "indigoFlare.png"), true);
			violetFlare = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "violetFlare.png"), true);
			whiteFlare  = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "whiteFlare.png"), true);
			
			yellowSpark = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "yellowSpark.png"), true);
			redSpark    = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "redSpark.png"), true);
			blueSpark   = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "blueSpark.png"), true);
			whiteSpark  = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "whiteSpark.png"), true);
			
			whiteStar   = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "whiteStar.png"), true);
			
			reset       = TextureIO.newTexture(new File("tex/default.jpg"), true);
			
			current = reset;
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public Vec3 c;
	public Vec3 t;
	public float rotation;
	
	public int duration;
	
	public Particle(Vec3 c, Vec3 t, float rotation, int duration)
	{
		this.c = c;
		this.t = t;
		this.rotation = rotation;

		this.duration = duration;
	}
	
	public static int removeParticles(Collection<Particle> particles)
	{
		List<Particle> toRemove = new ArrayList<Particle>();
		
		for(Particle particle : particles)
			if(particle.isDead()) toRemove.add(particle);
		
		particles.removeAll(toRemove);
		
		return toRemove.size();
	}
	
	public Vec3 getPosition() { return c; }
	
	public boolean isDead() { return (duration < 1); }
	
	public abstract void render(GL2 gl, float trajectory);
	
	public void update()
	{
		c = c.add(t);
		duration--;
	}
	
	public static void resetTexture() { current = reset; }
}
