package bates.jamie.graphics.particle;

import java.util.Random;

import javax.media.opengl.GL2;

import bates.jamie.graphics.util.Vec3;

public class RayParticle extends Particle
{
	private static final int HALF_LIFE = 60;
	
	float[][] vertices = new float[7][3];
	float[][]   colors = new float[7][4];
	
	float offset;
	

	public RayParticle(Vec3 c, Vec3 t0, Vec3 t1, float[] color, int duration)
	{
		super(c, new Vec3(), 0, duration);
		
		Random generator = new Random();
		offset = generator.nextFloat() * 0.1f;
		
		colors[0] = new float[] {1, 1, 1, 0};
		colors[1] = new float[] {1, 1, 1, 0};
		colors[2] = new float[] {1, 1, 1, 0};
		colors[3] = new float[] {color[0], color[1], color[2], 1};
		colors[4] = new float[] {color[0], color[1], color[2], 1};
		colors[5] = new float[] {color[0], color[1], color[2], 0};
		colors[6] = new float[] {color[0], color[1], color[2], 0};
		
		vertices[0] = new float[] {0, 0, 0};
		vertices[1] = t0.multiply( 3).toArray();
		vertices[2] = t1.multiply( 3).toArray();
		vertices[3] = t0.multiply( 5).toArray();
		vertices[4] = t1.multiply( 5).toArray();
		vertices[5] = t0.multiply( 9).toArray();
		vertices[6] = t1.multiply( 9).toArray();
	}

	@Override
	public void render(GL2 gl, float trajectory)
	{
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
		
		gl.glDisable(GL2.GL_TEXTURE_2D);
		
		float alpha = 1.0f - ((float) Math.abs(HALF_LIFE - duration) / HALF_LIFE);
		
		gl.glPushMatrix();
		{
			gl.glTranslatef(0, 40, 0);
			gl.glRotatef(trajectory, 0, -1, 0);
			
			gl.glBegin(GL2.GL_TRIANGLE_STRIP);
			
			for(int i = 0; i < vertices.length; i++)
			{
				gl.glColor4f(colors[i][0], colors[i][1], colors[i][2], colors[i][3] * alpha);
				gl.glVertex3f(vertices[i][0], vertices[i][1], offset);	
			}
			gl.glEnd();
		}
		gl.glPopMatrix();
		
		gl.glDisable(GL2.GL_BLEND);
		
		gl.glColor3f(1, 1, 1);
	}

}
