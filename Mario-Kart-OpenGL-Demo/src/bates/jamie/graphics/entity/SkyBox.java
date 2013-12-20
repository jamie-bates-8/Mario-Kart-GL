package bates.jamie.graphics.entity;

import javax.media.opengl.GL2;

import bates.jamie.graphics.util.Shader;

import com.jogamp.opengl.util.gl2.GLUT;

public class SkyBox
{	
	private float[] horizon  = new float[] {0.88f, 1.00f, 1.00f};
	private float[] skyColor = new float[] {0.18f, 0.56f, 1.00f};
	
	public SkyBox(GL2 gl) {}
	
	public float[] getSkyColor() { return skyColor; }
	public float[] getHorizonColor() { return horizon; }
	
	public void setSkyColor(float[] color) { skyColor = color; }
	public void setHorizonColor(float[] color) { horizon = color; }
	
	public void render(GL2 gl)
	{
		gl.glDisable(GL2.GL_LIGHTING);
		
		Shader shader = Shader.get("clear_sky");
		
		if(Shader.enabled && shader != null) shader.enable(gl);
		
		gl.glPushMatrix();
		{
			if(Shader.enabled)
			{
				shader.setUniform(gl, "horizon" , horizon );
				shader.setUniform(gl, "skyColor", skyColor);
				
				GLUT glut = new GLUT();
				glut.glutSolidSphere(800, 32, 32);
			}
		}	
		gl.glPopMatrix();
		
		Shader.disable(gl);
		
		gl.glEnable(GL2.GL_LIGHTING);
	}
}
