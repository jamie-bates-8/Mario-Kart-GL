package bates.jamie.graphics.particle;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import javax.media.opengl.GL2;

import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.util.Vector;

public class SparkParticle extends Particle
{
	private float[] color;
	private float[] origin;
	
	private int length;
	private int timer = 5;
	
	private Car car;
	
	public SparkParticle(Car car, float[] c, float[] t,
		int duration, float[] color, int length)
	{
		super(c, t, 0, duration);
		
		this.length = length;
		this.origin = c;
		this.color = color;
		
		this.car = car;
	}

	@Override
	public void render(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{		
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
			gl.glDisable(GL_TEXTURE_2D);
			
			gl.glLineWidth(2);
			gl.glEnable(GL2.GL_LINE_SMOOTH);

			gl.glColor3f(color[0], color[1], color[2]);
			
			gl.glBegin(GL2.GL_LINE_STRIP);
			{
				int size = timer;
				if(size > length) size = length;
				
				for(int i = 0; i < size; i++)
				{
					float time = (timer - i) * 0.2f;
					if(car != null && car.isMiniature()) time /= 2;
					
					float[] p = Vector.add(origin, Vector.multiply(t, time));
					p[1] -= 4.9f * time * time * 0.1f;
					
					gl.glVertex3f(p[0], p[1], p[2]);
				}
			}
			gl.glEnd();
			
			gl.glLineWidth(1);

			gl.glEnable(GL_TEXTURE_2D);
			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
			
			gl.glColor3f(1, 1, 1);
		}
		gl.glPopMatrix();
	}
	
	@Override
	public void update()
	{
		duration--;
		
		if(car != null) origin = Vector.subtract(origin, car.getVector());
		timer++;
	}
}
