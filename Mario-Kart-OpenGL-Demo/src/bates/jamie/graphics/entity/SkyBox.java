package bates.jamie.graphics.entity;

import static javax.media.opengl.GL.GL_TEXTURE_2D;

import java.io.File;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.util.shader.Shader;

import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class SkyBox
{	
	private float[] horizon  = new float[] {0.88f, 1.00f, 1.00f};
	private float[] skyColor = new float[] {0.18f, 0.56f, 1.00f};
	
	protected static Texture randomTexture;
	
	static
	{
		try
		{
			randomTexture = TextureIO.newTexture(new File("tex/ambient_noise.png"), true);
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public SkyBox(GL2 gl)
	{
		
	}
	
	public float[] getSkyColor() { return skyColor; }
	public float[] getHorizonColor() { return horizon; }
	
	public void setSkyColor(float[] color) { skyColor = color; }
	public void setHorizonColor(float[] color) { horizon = color; }
	
	public void render(GL2 gl)
	{
		gl.glDisable(GL2.GL_LIGHTING);
		
		Shader shader = Shader.get("clear_sky");
		if(shader != null) shader.enable(gl);
		
		gl.glPushMatrix();
		{
			shader.setUniform(gl, "horizon" , horizon );
			shader.setUniform(gl, "skyColor", skyColor);
			
			shader.setUniform(gl, "timer", Scene.sceneTimer);
			
			shader.setSampler(gl, "noiseSampler", 0); gl.glActiveTexture(GL2.GL_TEXTURE0); randomTexture.bind(gl);
				
			GLUT glut = new GLUT();
			glut.glutSolidSphere(800, 32, 32);
		}	
		gl.glPopMatrix();
		
		Shader.disable(gl);
		
		gl.glEnable(GL2.GL_LIGHTING);
	}
}
