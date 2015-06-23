package bates.jamie.graphics.particle;


import static bates.jamie.graphics.util.Vec3.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import bates.jamie.graphics.entity.Vehicle;
import bates.jamie.graphics.particle.FireParticle.FireType;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.Vector;

import com.jogamp.opengl.util.texture.Texture;

public class ParticleGenerator
{
	private Random generator;
	
	private int pulse    = 0;
	private int counter  = 0;
	private int quantity = 0;
	
	private Vec3 source = new Vec3();
	
	public enum GeneratorType
	{
		BLAST,
		SPARK,
		SPARKLE,
		STAR,
		FIRE,
		RAY;
	}
	
	private GeneratorType type;
	
	public ParticleGenerator() { generator = new Random(); }
	
	public ParticleGenerator(int pulse, int quantity, GeneratorType type, Vec3 source)
	{
		generator = new Random();
		
		this.pulse = pulse;
		this.quantity = quantity;
		this.type = type;
		
		this.source = source;
	}
	
	public void setPulse(int pulse) { this.pulse = pulse; }
	public void setQuantity(int quantity) { this.quantity = quantity; }
	public void setSource(Vec3 source) { this.source = source; }
	
	public boolean update()
	{
		counter++;
		counter %= pulse;
		
		return counter == 0;
	}
	
	public List<Particle> generate()
	{
		switch(type)
		{
			case BLAST   : return generateBlastParticles  (source, quantity);
			case SPARK   : return generateSparkParticles  (source, getRandomVector(), quantity, 0, null);
			case RAY     : return generateRayParticles    (source, quantity);
			case SPARKLE : return generateSparkleParticles(source, quantity, false, null);
			case FIRE    : return generateFireParticles   (source, quantity, new Vec3(0, 0.9, 0), null, 0, FireType.RED);
			
			default: return null;
		}
	}
	
	private int colorID = 0;
	
	public List<Particle> generateFireParticles(Vec3 source, int n, Vec3 direction, Vehicle car, int sourceID, FireType type)
	{
		List<Particle> particles = new ArrayList<Particle>();
		
		Random generator = new Random();
		
		for(int i = 0; i < n; i++)
		{	
			Vec3 t = getRandomVector(getRandomVector(), 0.25f, generator.nextFloat() * (type == FireType.BLUE ? 0.15f : 0.10f));
			int duration = 30 + generator.nextInt(70) + (generator.nextFloat() < 0.05 ? 5 : 0);
			int textureID = generator.nextInt(5);
			
			boolean spark = false;
			
			if(generator.nextFloat() < 0.03)
			{
				t = getRandomVector(direction, 0.5f, 0.15f + generator.nextFloat() * 0.10f);
				textureID = 0;
				spark = true;
				duration += 45;
			}
			
			float scale = 1 - t.magnitude();
			
			scale *= type == FireType.BLUE ? 1.75 : 1.5;
			duration *= 0.5;
			
			particles.add(new FireParticle(car != null ? getRandomVector(0.05f) : source, t, direction, 0, duration, textureID, scale, spark, type, car, sourceID));
		}
		
		return particles;
	}
	
	public List<Particle> generateRayParticles(Vec3 source, int n)
	{
		List<Particle> particles = new ArrayList<Particle>();
		
		float[][] colors = {RGB.WHITE, RGB.RED, RGB.ORANGE, RGB.YELLOW, RGB.GREEN, RGB.BLUE, RGB.INDIGO, RGB.VIOLET};
		
		for(int i = 0; i < n; i++)
		{
			colorID++;
			colorID %= colors.length;
			
			float[]  color = colors[colorID];
			
			double incline = generator.nextFloat() * Math.PI * (generator.nextBoolean() ? 1 : -1);
			double azimuth = 0;
			
			float rayX = (float) (Math.sin(incline) * Math.cos(azimuth));
			float rayY = (float) (Math.cos(incline));
			float rayZ = (float) (Math.sin(incline) * Math.sin(azimuth));
			
			Vec3 t0 = new Vec3(rayX, rayY, rayZ);
			
			incline += 0.15 + generator.nextFloat() * 0.05;
			
			rayX = (float) (Math.sin(incline) * Math.cos(azimuth));
			rayY = (float) (Math.cos(incline));
			rayZ = (float) (Math.sin(incline) * Math.sin(azimuth));
			
			Vec3 t1 = new Vec3(rayX, rayY, rayZ);
			
			particles.add(new RayParticle(this, t0.normalize(), t1.normalize(), color, 120));
		}
		
		return particles;
	}
	
	public List<Particle> generateTerrainParticles(Vec3 source, int n, Texture texture)
	{
		List<Particle> particles = new ArrayList<Particle>();
		
		for(int i = 0; i < n; i++)
		{
			float[] texCoords = {generator.nextFloat(), generator.nextFloat()};
			
			Vec3 t = getRandomVector();
			t.y = Math.abs(t.y * 0.75f);
			
			particles.add(new TerrainParticle(source, t, 0, 12, texCoords, texture));
		}
		
		return particles;
	}
	
	public List<Particle> generateItemBoxParticles(Vec3 source, int n)
	{
		List<Particle> particles = new ArrayList<Particle>();
				
		float hue = (float) Scene.sceneTimer % 1000;
		hue *= 0.001;
		
		for(int i = 0; i < n; i++)
		{
			float h = hue + generator.nextFloat() * 0.025f;
			if(h > 1) h -= 1;
			
			float[] color = RGB.HSVToRGB(new float[] {h, 1, 1});
			
			Vec3 t = getRandomVector(0.85f);
			t.y = Math.abs(t.y);
			
			particles.add(new ItemBoxParticle(source, t, generator.nextInt(360), color, generator.nextBoolean(), false, false, null));
		}
		
		return particles;
	}
	
	public List<Particle> generateSparkParticles(Vec3 source, Vec3 t, int n, int type, Vehicle car)
	{
		List<Particle> particles = new ArrayList<Particle>();
		
		float[][][] colors =
		{
			{RGB.WHITE,  RGB.BRIGHT_YELLOW},
			{RGB.RED,    RGB.BRIGHT_RED   },
			{RGB.BLUE,   RGB.BRIGHT_BLUE  },
		};
		
		for(int i = 0; i < n; i++)
		{
			float[] color = Vector.mix(colors[type][0], colors[type][1], generator.nextFloat());
			
			t = t.normalize().add(getRandomVector());
			t.y = Math.abs(t.y * (generator.nextBoolean() ? 1 : 2));
			
			int length = 5 + generator.nextInt(2);
			
			particles.add(new SparkParticle(car, source, t, 8, color, length));
		}
		
		return particles;
	}
	
	public List<Particle> generateDriftParticles(Vec3 source, int n, int color, boolean miniature)
	{
		List<Particle> particles = new ArrayList<Particle>();
		
		for(int i = 0; i < n; i++)
		{	
			float rotation = -30 + generator.nextInt(60);
			
			particles.add(new DriftParticle(source, rotation, color, generator.nextBoolean(), miniature));
		}
		
		return particles;
	}
	
	public List<Particle> generateBlastParticles(Vec3 source, int n)
	{
		List<Particle> particles = new ArrayList<Particle>();
		
		for(int i = 0; i < n; i++)
		{
			Vec3 t = getRandomVector();
			t = t.normalize().multiply(2.5f);
			
			int duration = 30 + generator.nextInt(30);
			
			particles.add(new BlastParticle(source, t, 0, duration));
		}
		
		return particles;
	}
	
	public List<Particle> generateSparkleParticles(Vec3 source, int n, boolean miniature, Vehicle car)
	{
		List<Particle> particles = new ArrayList<Particle>();
		
		for(int i = 0; i < n; i++)
		{
			Vec3 t = getRandomVector().normalize();
			if(miniature) t = t.multiply(0.3f); else t = t.multiply(0.6f); 
			
			int duration = 30 + generator.nextInt(30);
			
			particles.add(new SparkleParticle(source, t, miniature ? 1.0f : 1.25f, duration, car));
		}
		
		return particles;
	}
	
	public List<Particle> generateFakeItemBoxParticles(Vec3 source, int n, boolean miniature, Vehicle car)
	{
		List<Particle> particles = new ArrayList<Particle>();
		
		Random generator = new Random();
		
		for(int i = 0; i < n; i++)
		{
			float[] color = RGB.RED;
			
			Vec3 t = getRandomVector(0.85f);
			if(car != null && miniature) t = t.multiply(0.5f);
			t.y = Math.abs(t.y);
			
			if(car != null)
			{
				source = source.add(t.multiply(4));
				t.x *= 0.25;
				t.z *= 0.25;
			}
			
			particles.add(new ItemBoxParticle(source, t, generator.nextInt(360), color, generator.nextBoolean(), miniature, true, car));
		}
		
		return particles;
	}
	
	public List<Particle> generateBoostParticles(Vec3 source, int n, boolean special, boolean miniature)
	{
		List<Particle> particles = new ArrayList<Particle>();
		
		Random generator = new Random();
		
		for(int i = 0; i < n; i++)
		{	
			Vec3 t = getRandomVector();
			
			float k = (special) ? 0.55f : 0.5f;
			t = t.multiply(k);
			if(miniature) t = t.multiply(0.5f);
			
			float scale = generator.nextFloat() * ((miniature) ? 1.25f : 2.5f);
			
			particles.add(new BoostParticle(source, t, 0, 1, scale, special, miniature));
		}
		
		return particles;
	}

	public Vec3 getSource() { return source; }
}
