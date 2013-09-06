package bates.jamie.graphics.io;

import java.awt.Font;

import javax.media.opengl.GL2;

import bates.jamie.graphics.util.Vec3;

import com.jogamp.opengl.util.awt.TextRenderer;


public class HoloTag
{
	private String tag = "";
	private Vec3 p;
	private TextRenderer renderer;
	
	public HoloTag(String tag, Vec3 p)
	{
		this.tag = tag;
		setPosition(p);
		
		Font font = new Font("Calibri", Font.PLAIN, 24);
		renderer = new TextRenderer(font, true, false);
	}
	
	public void setPosition(Vec3 p)
	{
		this.p = p;
	}
	
	public void setText(String tag)
	{
		this.tag = tag;
	}
	
	public void displayPosition()
	{
		tag = String.format("(%+3.2f, %+3.2f, %+3.2f)", p.x, p.y, p.z);
	}
	
	public void render(GL2 gl, float trajectory)
	{
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_BLEND);
		gl.glDisable(GL2.GL_TEXTURE_2D);
		
		gl.glPushMatrix();
		{
			gl.glColor4f(0.5f, 0.5f, 0.5f, 0.0f);
			
			float width  = (float) renderer.getBounds(tag).getCenterX();
			float height = (float) renderer.getBounds(tag).getCenterY();
			
			float scale = 0.075f;
			
			float w = width  * scale * 1.25f;
			float h = height * scale * 1.25f;
			
			float centre = 5 + (height / 2);
			
			gl.glTranslatef(p.x, p.y, p.z);
			gl.glRotatef(trajectory + 90, 0, 1, 0);
			
			gl.glRectd(w, h, -w, -h);
			
			gl.glDisable(GL2.GL_DEPTH_TEST);
			
			renderer.setSmoothing(true);
			renderer.begin3DRendering();
		
			renderer.draw3D(tag, -width * scale, height * scale, 0, scale);
			
			renderer.end3DRendering();
			
			gl.glEnable(GL2.GL_DEPTH_TEST);
		}
		gl.glPopMatrix();
		
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glDisable(GL2.GL_BLEND);
		gl.glEnable(GL2.GL_TEXTURE_2D);
		
		gl.glColor4f(1, 1, 1, 1);
	}
}
