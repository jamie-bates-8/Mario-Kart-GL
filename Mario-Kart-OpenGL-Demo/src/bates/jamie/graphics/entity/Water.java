package bates.jamie.graphics.entity;

import static javax.media.opengl.GL.GL_TEXTURE_2D;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.util.Matrix;
import bates.jamie.graphics.util.Shader;
import bates.jamie.graphics.util.TextureLoader;
import bates.jamie.graphics.util.Vec3;

import com.jogamp.opengl.util.texture.Texture;

public class Water
{
	private static final String BUMP_MAPS = "tex/bump_maps/";
	
	private Scene scene;
	
	private float timer = 0;
	private float increment = 0.05f;
	
	public boolean frozen = true;
	public boolean magma = false;
	
	public Water(Scene scene)
	{
		this.scene = scene;
	}
	
	public int reflectTexture;
	public int refractTexture;
	
	public Texture perturbTexture;
	public Texture frostTexture;
	public Texture magmaTexture;
	
	public void createTextures(GL2 gl)
	{
		int[] id = new int[2];
		gl.glGenTextures(2, id, 0);
		reflectTexture = id[0];
		refractTexture = id[1];

		perturbTexture = TextureLoader.load(gl, BUMP_MAPS + "water.png"); 
		frostTexture = TextureLoader.load(gl, "tex/ice.jpg");
		magmaTexture = TextureLoader.load(gl, "tex/magma.png");

		gl.glActiveTexture(GL2.GL_TEXTURE1);
		
		gl.glBindTexture(GL2.GL_TEXTURE_2D, refractTexture);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		
		gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA8, scene.getWidth(), scene.getHeight(), 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, null);
		
		gl.glActiveTexture(GL2.GL_TEXTURE0);
		
		gl.glBindTexture(GL2.GL_TEXTURE_2D, reflectTexture);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		
		gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA8, scene.getWidth(), scene.getHeight(), 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, null);
	}
	
	public void setReflection(GL2 gl)
	{
		gl.glActiveTexture(GL2.GL_TEXTURE0);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, reflectTexture);
		
		gl.glEnable(GL2.GL_TEXTURE_2D);
		
		gl.glCopyTexImage2D(GL_TEXTURE_2D, 0, GL2.GL_RGBA8, 0, 0, scene.getWidth(), scene.getHeight(), 0);
	}
	
	public void setRefraction(GL2 gl)
	{
		gl.glActiveTexture(GL2.GL_TEXTURE1);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, refractTexture);
		
		gl.glEnable(GL2.GL_TEXTURE_2D);
		
		gl.glCopyTexImage2D(GL_TEXTURE_2D, 0, GL2.GL_RGBA8, 0, 0, scene.getWidth(), scene.getHeight(), 0);
		
		gl.glActiveTexture(GL2.GL_TEXTURE0);
	}
	
	public void render(GL2 gl, Vec3 p)
	{	
		gl.glPushMatrix();
		{
			gl.glColor4f(timer, 1, 1, 1);
			
			Shader shader = magma ? Shader.get("magma") : Shader.get("water");
			
			if(shader != null && Shader.enabled) shader.enable(gl);
			
			gl.glActiveTexture(GL2.GL_TEXTURE3); if(magma) magmaTexture.bind(gl); else frostTexture.bind(gl);
			gl.glActiveTexture(GL2.GL_TEXTURE2); perturbTexture.bind(gl);
			gl.glActiveTexture(GL2.GL_TEXTURE1); gl.glBindTexture(GL2.GL_TEXTURE_2D, refractTexture);
			gl.glActiveTexture(GL2.GL_TEXTURE0); gl.glBindTexture(GL2.GL_TEXTURE_2D, reflectTexture);
			
			shader.setSampler(gl, "reflectionSampler", 0);
			shader.setSampler(gl, "refractionSampler", 1);
			shader.setSampler(gl, "normalSampler"    , 2);
			shader.setSampler(gl, "iceSampler"       , 3);
			
			shader.setUniform(gl, "frozen", frozen);
			
			shader.loadModelMatrix(gl, Matrix.IDENTITY_MATRIX_16);
			// TODO temporary fix, should pass actual camera position to shader
			shader.setUniform(gl, "cameraPos", p.toArray());
			
			gl.glBegin(GL2.GL_QUADS);
			{
				float size = 500;
				
				gl.glVertexAttrib3f(1, 0, 0, 1);
				gl.glNormal3f(0, 1, 0);
				
				gl.glVertex3f(+size, 0, +size);
				gl.glVertex3f(+size, 0, -size);
				gl.glVertex3f(-size, 0, -size);
				gl.glVertex3f(-size, 0, +size);
			}
			gl.glEnd();
			
			Shader.disable(gl);
			
			if(Scene.enableAnimation && (!frozen || magma)) timer += increment;
			
			gl.glColor4f(1, 1, 1, 1);
			
			gl.glActiveTexture(GL2.GL_TEXTURE3); gl.glDisable(GL2.GL_TEXTURE_2D);
			gl.glActiveTexture(GL2.GL_TEXTURE2); gl.glDisable(GL2.GL_TEXTURE_2D);
			gl.glActiveTexture(GL2.GL_TEXTURE1); gl.glDisable(GL2.GL_TEXTURE_2D);
			gl.glActiveTexture(GL2.GL_TEXTURE0);
		}
		gl.glPopMatrix();
	}
}
