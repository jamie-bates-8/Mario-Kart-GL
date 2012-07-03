import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static graphics.util.Vector.multiply;
import static graphics.util.Vector.normalize;

public class ParticleGenerator
{
	public static final float[] RED    = {237.0f,  28.0f,  36.0f};
	public static final float[] ORANGE = {242.0f, 101.0f,  34.0f};
	public static final float[] YELLOW = {255.0f, 228.0f,   0.0f};
	public static final float[] GREEN  = { 57.0f, 180.0f,  74.0f};
	public static final float[] BLUE   = {  0.0f, 173.0f, 239.0f};
	public static final float[] INDIGO = {  0.0f, 114.0f, 188.0f};
	public static final float[] VIOLET = {102.0f,  45.0f, 145.0f};
	public static final float[] WHITE  = {255.0f, 255.0f, 255.0f}; 
	
	private Random generator;
	
	public ParticleGenerator() { generator = new Random(); }
	
	public List<Particle> generateItemBoxParticles(float[] source, int n)
	{
		List<Particle> particles = new ArrayList<Particle>();
		
		float[][] colors = {RED, ORANGE, YELLOW, GREEN, BLUE, INDIGO, VIOLET};
		
		for(int i = 0; i < n; i++)
		{
			float[]  color = colors[generator.nextInt(colors.length)];
			float[] _color = {color[0]/255, color[1]/255, color[2]/255}; 
			
			float[] t = getRandomVector();
			
			particles.add(new ItemBoxParticle(source, t, 0, _color, generator.nextBoolean()));
		}
		
		return particles;
	}
	
	public List<Particle> generateSparkParticles(float[] source, int n)
	{
		List<Particle> particles = new ArrayList<Particle>();
		
		float[][] colors = {WHITE, YELLOW, ORANGE};
		
		for(int i = 0; i < n; i++)
		{
			float[]  color = colors[generator.nextInt(colors.length)];
			float[] _color = {color[0]/255, color[1]/255, color[2]/255};
			
			float[] t = multiply(getRandomVector(), 0.5f);
			t[1] = Math.abs(t[1]);
			
			particles.add(new SparkParticle(source, t, 0, 8, _color));
		}
		
		return particles;
	}
	
	public List<Particle> generateDriftParticles(float[] source, int n, int color)
	{
		List<Particle> particles = new ArrayList<Particle>();
		
		for(int i = 0; i < n; i++)
		{	
			float rotation = -45 + generator.nextInt(90);
			
			particles.add(new DriftParticle(source, rotation, color, generator.nextBoolean()));
		}
		
		return particles;
	}
	
	public List<Particle> generateBlastParticles(float[] source, int n)
	{
		List<Particle> particles = new ArrayList<Particle>();
		
		for(int i = 0; i < n; i++)
		{
			float[] t = getRandomVector();
			t = multiply(normalize(t), 2.5f);
			
			int duration = 30 + generator.nextInt(30);
			
			particles.add(new BlastParticle(source, t, 0, duration));
		}
		
		return particles;
	}
	
	public List<Particle> generateStarParticles(float[] source, int n)
	{
		List<Particle> particles = new ArrayList<Particle>();
		
		for(int i = 0; i < n; i++)
		{
			float[] t = getRandomVector();
			
			float scale = generator.nextFloat() * 2.5f;
			
			particles.add(new StarParticle(source, t, 5, scale));
		}
		
		return particles;
	}
	
	public List<Particle> generateFakeItemBoxParticles(float[] source, int n)
	{
		List<Particle> particles = new ArrayList<Particle>();
		
		Random generator = new Random();
		
		for(int i = 0; i < n; i++)
		{
			float[]  color = RED;
			float[] _color = {color[0]/255, color[1]/255, color[2]/255}; 
			
			float[] t = getRandomVector();
			
			particles.add(new ItemBoxParticle(source, t, 45, _color, generator.nextBoolean()));
		}
		
		return particles;
	}
	
	public List<Particle> generateBoostParticles(float[] source, int n, boolean special)
	{
		List<Particle> particles = new ArrayList<Particle>();
		
		Random generator = new Random();
		
		for(int i = 0; i < n; i++)
		{	
			float[] t = getRandomVector();
			
			float k = (special) ? 0.55f : 0.5f;
			t = multiply(t, k);
			
			int duration = (special) ? 1 : 1;
			
			particles.add(new BoostParticle(source, t, 0, duration, generator.nextFloat() * 2.5f, special));
		}
		
		return particles;
	}
	
	private float[] getRandomVector()
	{
		float xVel = (generator.nextBoolean()) ? generator.nextFloat() : -generator.nextFloat();
		float yVel = (generator.nextBoolean()) ? generator.nextFloat() : -generator.nextFloat();
		float zVel = (generator.nextBoolean()) ? generator.nextFloat() : -generator.nextFloat();
		
		return new float[] {xVel, yVel, zVel};
	}
}
