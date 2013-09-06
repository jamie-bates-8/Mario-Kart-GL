package bates.jamie.graphics.entity;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_POINTS;
import static javax.media.opengl.GL.GL_TEXTURE_2D;

import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.util.Vector;

import static bates.jamie.graphics.util.Vector.*;

public class LightingStrike
{
	private float[] origin;
	private float[] vector;
	private float   length;
	
	private float[][]  points;
	private float[][] _points;
	
	public int timer    = 0;
	public int duration = 60;
	
	public int size;
	public int intervals;
	
	private Random generator = new Random();
	
	private static Texture spark;
	
	static
	{
		try
		{			
			spark = TextureIO.newTexture(new File("tex/lightning_spark.png"), true);
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	
	public LightingStrike(float[] start, float[] end)
	{
		size = 2 + generator.nextInt(7);
		intervals = 20 + generator.nextInt(6) * 2;
		
		origin = start;
		length = length(end, start);
		vector = normalize(subtract(end, start));
		
		points = setupPoints();
	}
	
	private float[][] setupPoints()
	{
		float ratio = (float) timer / duration;
		int intervals = (int) (ratio * this.intervals);
		
		 points = new float[intervals + 2][3];
		_points = new float[intervals + 2][3];
		
		points[0] = Arrays.copyOf(origin, 3);
		float[] p = Arrays.copyOf(origin, 3);
		
		float[] direction = Vector.multiply(vector, length);
		direction = Vector.multiply(direction, ratio);
		direction = Vector.multiply(direction, 1.0f / (intervals + 1));
		
		for(int i = 1; i <= intervals + 1; i++)
		{
			p = Vector.add(p, direction);
			points[i] = Arrays.copyOf(p, 3);
		}
		
		return points;
	}
	
	public void render(GL2 gl)
	{
		gl.glPushMatrix();
		{		
			boolean multisample = gl.glIsEnabled(GL2.GL_MULTISAMPLE);
			
			gl.glDisable(GL_LIGHTING);
			gl.glDisable(GL_TEXTURE_2D);
			
			gl.glLineWidth(size);
			
			if(!multisample)
			{
				gl.glEnable(GL_BLEND);
				gl.glEnable(GL2.GL_LINE_SMOOTH);
			}

			gl.glColor3f(1.0f, 1.0f, 0.5f);
			
			if(Scene.enableAnimation)
			{
				timer++;
				if(timer >= duration) timer = 0;
				points = setupPoints();
				
				gl.glBegin(GL2.GL_LINE_STRIP);
				{
					for(int i = 0; i < points.length; i++)
					{
						float[] p = points[i];
						if(i > 0 && i < points.length - 1)
							p = Vector.add(p, getRandomVector(0.4f + (float) timer / duration * 0.4f));
						gl.glVertex3fv(p, 0);
						
						_points[i] = Arrays.copyOf(p, 3);
					}
				}
				gl.glEnd();
			}
			else
			{
				gl.glBegin(GL2.GL_LINE_STRIP);
				{
					for(int i = 0; i < _points.length; i++)
					{
						float[] p = _points[i];
						gl.glVertex3f(p[0], p[1], p[2]);
					}
				}
				gl.glEnd();
			}
			
			float[] direction = getRandomVector(2.5f);
			float[] p = Vector.add(points[generator.nextInt(points.length - 1)], direction);

			gl.glColor3f(1, 1, 1);
			gl.glPointSize(generator.nextBoolean() ? 50 : 40);
			
			gl.glEnable(GL_BLEND);

			gl.glEnable(GL2.GL_TEXTURE_2D);
			gl.glEnable(GL2.GL_POINT_SPRITE);
			gl.glTexEnvi(GL2.GL_POINT_SPRITE, GL2.GL_COORD_REPLACE, GL2.GL_TRUE);

			gl.glDepthMask(false);
			gl.glDisable(GL_LIGHTING);

			spark.bind(gl);

			gl.glBegin(GL_POINTS);
			{
				gl.glVertex3f(p[0], p[1], p[2]);
			}
			gl.glEnd();
			
			if(multisample) gl.glEnable(GL2.GL_MULTISAMPLE);

			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
			gl.glDepthMask(true);
			
			gl.glLineWidth(1);
			gl.glColor3f(1, 1, 1);
		}
		gl.glPopMatrix();
	}
	
	private float[] getRandomVector(float scalar)
	{
		float xVel = (generator.nextBoolean()) ? generator.nextFloat() : -generator.nextFloat();
		float yVel = (generator.nextBoolean()) ? generator.nextFloat() : -generator.nextFloat();
		float zVel = (generator.nextBoolean()) ? generator.nextFloat() : -generator.nextFloat();
		
		return new float[] {xVel * scalar, yVel * scalar, zVel * scalar};
	}
}
