package bates.jamie.graphics.particle;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_FLOAT;
import static javax.media.opengl.GL.GL_POINTS;

import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import static javax.media.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY;
import static javax.media.opengl.fixedfunc.GLPointerFunc.GL_COLOR_ARRAY;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.util.Vector;

import com.jogamp.common.nio.Buffers;

public class Blizzard
{
	private Random generator = new Random();
	
	public List<WeatherParticle> flakes;
	public FloatBuffer vBuffer;
	public int flakeLimit;
	
	public float[] wind;
	
	public int timer = 0;
	
	public enum StormType {SNOW, RAIN};
	public StormType type;
	
	public Blizzard(int flakeLimit, float[] wind, StormType type)
	{
		flakes = new ArrayList<WeatherParticle>();
		
		this.flakeLimit = flakeLimit;
		this.wind = wind;
		this.type = type;
	}
	
	public void update()
	{
		if(flakes.size() < flakeLimit - 5)
		{
			int i = 1 + generator.nextInt(5);
			
			while(i > 0)
			{
				float[] source = getSource();
				float[] vector = type == StormType.SNOW ? getRandomVector(1) : getRandomVector(0.25f);
				float alpha = type == StormType.SNOW ? generator.nextFloat() : generator.nextFloat() * 0.40f;
				
				flakes.add(new WeatherParticle(source, vector, alpha));
				i--;
			}
		}
		
		for(WeatherParticle flake : flakes)
		{
			flake.update(wind);
			if(Scene.outOfBounds(flake.c)) flake.c = getSource();
		}
	}
	
	private float[] getSource()
	{
		float x = generator.nextFloat() * (generator.nextBoolean() ? 150 : -150);
		float y = generator.nextFloat() * (generator.nextBoolean() ?  10 : - 10);
		float z = generator.nextFloat() * (generator.nextBoolean() ? 150 : -150);
		
		return new float[] {x, y + 200, z};
	}
	
	private float[] getRandomVector(float scalar)
	{
		float xVel = (generator.nextBoolean()) ? generator.nextFloat() : -generator.nextFloat();
		float yVel = (generator.nextBoolean()) ? generator.nextFloat() : -generator.nextFloat();
		float zVel = (generator.nextBoolean()) ? generator.nextFloat() : -generator.nextFloat();
		
		return Vector.multiply(new float[] {xVel, yVel, zVel}, scalar);
	}
	
	public void render(GL2 gl)
	{
		switch(type)
		{
			case RAIN: renderRain(gl); break;
			case SNOW: renderSnow(gl); break;
		}
	}
	
	public void renderSnow(GL2 gl)
	{
		gl.glPushMatrix();
		{	
			gl.glDisable(GL2.GL_TEXTURE_2D);
						
			gl.glEnableClientState(GL_VERTEX_ARRAY);
			gl.glEnableClientState(GL_COLOR_ARRAY);
			
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
				
			gl.glEnable(GL2.GL_POINT_SMOOTH);
			gl.glPointSize(6);
			gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_NICEST);
			
			FloatBuffer vertices = Buffers.newDirectFloatBuffer(flakes.size() * 3);
			
			for(Particle particle : flakes) vertices.put(particle.c);
			vertices.position(0);
			
			FloatBuffer colors = Buffers.newDirectFloatBuffer(flakes.size() * 4);
			
			for(WeatherParticle particle : flakes) colors.put(new float[] {1, 1, 1, particle.alpha});
			colors.position(0);
			
			gl.glVertexPointer(3, GL_FLOAT, 0, vertices);
			gl.glColorPointer(4, GL_FLOAT, 0, colors);
			gl.glDrawArrays(GL_POINTS, 0, flakes.size());
			
			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
			
			gl.glDisableClientState(GL_VERTEX_ARRAY);
			gl.glDisableClientState(GL_COLOR_ARRAY);
					
			gl.glEnable(GL2.GL_TEXTURE_2D);
		}
		gl.glPopMatrix();
	}
	
	public void renderRain(GL2 gl)
	{
		gl.glPushMatrix();
		{	
			gl.glDisable(GL2.GL_TEXTURE_2D);
						
			gl.glEnableClientState(GL_VERTEX_ARRAY);
			gl.glEnableClientState(GL_COLOR_ARRAY);
			
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
				
			gl.glEnable(GL2.GL_POINT_SMOOTH);
			gl.glPointSize(6);
			gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_NICEST);
			
			FloatBuffer vertices = Buffers.newDirectFloatBuffer(flakes.size() * 2 * 3);
			
			for(WeatherParticle particle : flakes)
			{
				vertices.put(particle.c);
				vertices.put(particle.getDirectionVector(wind, 4));
			}
			vertices.position(0);  
			
			FloatBuffer colors = Buffers.newDirectFloatBuffer(flakes.size() * 2 * 4);
			
			for(WeatherParticle particle : flakes)
			{
				colors.put(new float[] {1, 1, 1, 0.00f});
				colors.put(new float[] {1, 1, 1, particle.alpha});
			}
			colors.position(0);
			
			gl.glVertexPointer(3, GL_FLOAT, 0, vertices);
			gl.glColorPointer(4, GL_FLOAT, 0, colors);
			gl.glDrawArrays(GL2.GL_LINES, 0, flakes.size() - 1);
			
			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
			
			gl.glDisableClientState(GL_VERTEX_ARRAY);
			gl.glDisableClientState(GL_COLOR_ARRAY);
					
			gl.glEnable(GL2.GL_TEXTURE_2D);
		}
		gl.glPopMatrix();
	}
}
