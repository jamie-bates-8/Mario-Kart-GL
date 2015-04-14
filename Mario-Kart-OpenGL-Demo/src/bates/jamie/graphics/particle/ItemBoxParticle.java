package bates.jamie.graphics.particle;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_LINE_LOOP;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL2.GL_QUADS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;

import javax.media.opengl.GL2;

import bates.jamie.graphics.entity.Vehicle;
import bates.jamie.graphics.util.Vec3;



public class ItemBoxParticle extends Particle
{
	private float[] color;
	private boolean fill;
	private boolean miniature;
	private boolean fake;
	
	private Vehicle car;
	
	public ItemBoxParticle(Vec3 c, Vec3 t, float rotation, float[] color, boolean fill, boolean miniature, boolean fake, Vehicle car)
	{
		super(c, t, rotation, 30);
		
		this.color = color;
		this.fill = fill;
		this.miniature = miniature;
		this.fake = fake;
		
		this.car = car;
	}

	@Override
	public void render(GL2 gl, float trajectory)
	{
		gl.glPushMatrix();
		{
			gl.glTranslatef(c.x, c.y, c.z);
			gl.glRotatef(trajectory + rotation + 5 * duration, 0, -1, 0);
			if(fake) gl.glRotatef(45, 0, 0, 1);
			
			Vec3 scale = new Vec3(0.5);
			if(miniature) scale = scale.multiply(0.5f);
			
			gl.glScalef(scale.x, scale.y, scale.z);
			
			gl.glDepthMask(false);
			gl.glDisable(GL_LIGHTING);
			gl.glEnable(GL_BLEND);
			gl.glDisable(GL_TEXTURE_2D);
			
			float alpha = (float) duration / 30.0f;
			
			gl.glColor4f(color[0], color[1], color[2], alpha);

			if(fill)
			{
				gl.glBegin(GL_QUADS);
				{
					gl.glVertex3f(-0.5f, -0.5f, 0.0f);
					gl.glVertex3f(-0.5f,  0.5f, 0.0f);
					gl.glVertex3f( 0.5f,  0.5f, 0.0f);
					gl.glVertex3f( 0.5f, -0.5f, 0.0f);
				}
				gl.glEnd();
			}
			else
			{
				gl.glBegin(GL_LINE_LOOP);
				{
					gl.glVertex3f(-0.5f, -0.5f, 0.0f);
					gl.glVertex3f(-0.5f,  0.5f, 0.0f);
					gl.glVertex3f( 0.5f,  0.5f, 0.0f);
					gl.glVertex3f( 0.5f, -0.5f, 0.0f);
				}
				gl.glEnd();
			}

			gl.glEnable(GL_TEXTURE_2D);
			gl.glDisable(GL_BLEND);
			gl.glEnable(GL_LIGHTING);
			gl.glDepthMask(true);
		}
		gl.glPopMatrix();
	}
	
	@Override
	public void update()
	{
		t.y += (car != null) ? 0.010 : 0.025;
		t = t.multiply(0.85f);
		super.update();	
		
		if(car != null) c = c.add(car.getPosition().subtract(car.previousPosition));
	}
}
