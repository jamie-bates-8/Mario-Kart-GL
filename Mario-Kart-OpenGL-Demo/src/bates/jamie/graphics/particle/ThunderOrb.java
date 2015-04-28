package bates.jamie.graphics.particle;

import static javax.media.opengl.GL.GL_POINTS;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.media.opengl.GL2;

import bates.jamie.graphics.entity.LightningStrike;
import bates.jamie.graphics.entity.LightningStrike.RenderStyle;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.Vec3;

public class ThunderOrb
{
	private Vec3 position;
	
	private int duration = -30;
	
	private List<LightningStrike> bolts;
	
	private Random generator;
	
	
	public ThunderOrb(Vec3 position)
	{
		this.position = position;
		
		bolts = new ArrayList<LightningStrike>();
		
		generator = new Random(); 
		
		for(int i = 0; i < 8; i++)
		{
			Vec3 direction = Vec3.getRandomVector();
			Vec3 start = position;
			Vec3 end   = position.add(direction.multiply(3));
			
			LightningStrike bolt = new LightningStrike(start, end, 1, true, true, RenderStyle.SINGLE_FLASH);
			bolt.setIntervals(2, 16);
			bolt.offsetScale = 0.15f;
			bolt.duration = -30 - generator.nextInt(240);
			
			bolts.add(bolt);
		}
	}
	
	public void render(GL2 gl, Vec3 eye_direction)
	{
		for(LightningStrike bolt : bolts)
		{
			if(bolt.isDead())
			{
				Vec3 direction = Vec3.getRandomVector();
				bolt.setOrigin(position);
				bolt.setTarget(position.add(direction.multiply(3)));
				bolt.duration = -30 - generator.nextInt(240);
			}
		}
		
		gl.glPushMatrix();
		{
			gl.glDepthMask(false);
			
			gl.glDisable(GL2.GL_LIGHTING);
			gl.glEnable (GL2.GL_BLEND);
			gl.glDisable(GL2.GL_TEXTURE_2D);
			
			gl.glPointSize(40 + 10 * (float) Math.abs(Math.cos(Math.PI * 2 * (Scene.sceneTimer % 60) / 60)));
			
			duration++;
			float alpha = 1.0f - ((float) Math.abs(duration) / 120);
			
			gl.glColor4fv(RGB.toRGBA(RGB.BRIGHT_BLUE, alpha), 0);
			
			gl.glBegin(GL_POINTS);
			{
				gl.glVertex3fv(position.toArray(), 0);
			}
			gl.glEnd();
			
			gl.glDepthMask(true);
			
			gl.glEnable (GL2.GL_TEXTURE_2D);
			gl.glDisable(GL2.GL_BLEND);
			gl.glEnable (GL2.GL_LIGHTING);
			
			gl.glColor3f(1, 1, 1);
			
		}
		gl.glPopMatrix();
		
		for(LightningStrike bolt : bolts) bolt.render(gl, eye_direction);
	}
	
	public boolean isDead() { return duration > 120; }
}
