package bates.jamie.graphics.scene;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHT0;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SPECULAR;

import javax.media.opengl.GL2;


public class Material
{
	private float[] specular;
	
	public Material(float[] specular)
	{
		this.setSpecular(specular);
	}

	public float[] getSpecular() { return specular; }

	public void setSpecular(float[] specular) { this.specular = specular; }
	
	public void load(GL2 gl)
	{
		gl.glLightfv(GL_LIGHT0, GL_SPECULAR, specular, 0);
	}
}
