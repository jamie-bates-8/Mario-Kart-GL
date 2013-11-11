package bates.jamie.graphics.entity;

import java.util.List;

import javax.media.opengl.GL2;

import bates.jamie.graphics.util.Face;
import bates.jamie.graphics.util.OBJParser;
import bates.jamie.graphics.util.Renderer;
import bates.jamie.graphics.util.Shader;

import com.jogamp.opengl.util.gl2.GLUT;

public class SkyBox
{
	private static final List<Face> SKYBOX_FACES = OBJParser.parseTriangles("environment");
	private static int environmentList = -1;
	
	private float[] horizon  = new float[] {0.88f, 1.00f, 1.00f};
	private float[] skyColor = new float[] {0.18f, 0.56f, 1.00f};
	
	public SkyBox(GL2 gl)
	{
		if(environmentList == -1)
		{
			environmentList = gl.glGenLists(1);
			gl.glNewList(environmentList, GL2.GL_COMPILE);
			Renderer.displayTexturedObject(gl, SKYBOX_FACES);
		    gl.glEndList();
		}
	}
	
	public float[] getSkyColor() { return skyColor; }
	public float[] getHorizonColor() { return horizon; }
	
	public void setSkyColor(float[] color) { skyColor = color; }
	public void setHorizonColor(float[] color) { horizon = color; }
	
	public void render(GL2 gl)
	{
		gl.glDisable(GL2.GL_LIGHTING);
		
		Shader shader = Shader.get("phong_lights");
		
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
			else
			{
				gl.glScalef(40.0f, 40.0f, 40.0f);
				gl.glCallList(environmentList);
			}
		}	
		gl.glPopMatrix();
		
		Shader.disable(gl);
		
		gl.glEnable(GL2.GL_LIGHTING);
	}
}
