import static javax.media.opengl.GL.GL_FRONT;

import static javax.media.opengl.GL2ES1.GL_LIGHT_MODEL_AMBIENT;

import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_AMBIENT;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_DIFFUSE;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_EMISSION;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_FLAT;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHT0;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_POSITION;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SHININESS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SMOOTH;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SPECULAR;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SPOT_CUTOFF;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SPOT_DIRECTION;

import javax.media.opengl.GL2;


public class Light extends AnchorPoint
{

	private float[] ambience = {0.3f, 0.3f, 0.3f, 1.0f};
	private float[] diffuse = {0.7f, 0.7f, 0.7f, 1.0f};
	private float[] emission = {0.0f, 0.0f, 0.0f, 1.0f};

	private float[] specular = {1.0f, 1.0f, 1.0f};
	private int shininess = 128;

	public float[] direction = {0, -1, 0};

	public boolean smooth = true;
	public boolean parallel = false;
	public boolean secondary = true;

	public Light(GL2 gl)
	{
		setPosition(new float[] {100, 200, 100});

		gl.glEnable(GL_LIGHTING);
		gl.glEnable(GL_LIGHT0);

		gl.glEnable(GL2.GL_NORMALIZE);
		gl.glShadeModel(GL_SMOOTH);

		gl.glEnable(GL2.GL_COLOR_MATERIAL);
		gl.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);
	}

	public void setup(GL2 gl, boolean spotlight)
	{
		if(smooth) gl.glShadeModel(GL_SMOOTH);
		else gl.glShadeModel(GL_FLAT);

		gl.glLightModelfv(GL_LIGHT_MODEL_AMBIENT, ambience, 0);
		
		if(secondary) gl.glLightModeli(GL2.GL_LIGHT_MODEL_COLOR_CONTROL, GL2.GL_SEPARATE_SPECULAR_COLOR);
		else gl.glLightModeli(GL2.GL_LIGHT_MODEL_COLOR_CONTROL, GL2.GL_SINGLE_COLOR);

		gl.glLightfv(GL_LIGHT0, GL_AMBIENT, ambience, 0);
		gl.glLightfv(GL_LIGHT0, GL_DIFFUSE, diffuse, 0);
		gl.glLightfv(GL_LIGHT0, GL_SPECULAR, specular, 0);

		if(spotlight)
		{
			gl.glLightf(GL_LIGHT0, GL_SPOT_CUTOFF, 30.0f);
			gl.glLightfv(GL_LIGHT0, GL_SPOT_DIRECTION, direction, 0);
		}

		float[] p = getPosition();
		float[] _p = {p[0], p[1], p[2], parallel ? 0 : 1};
		gl.glLightfv(GL_LIGHT0, GL_POSITION, _p, 0);

		gl.glMaterialfv(GL_FRONT, GL_SPECULAR, specular, 0);
		gl.glMateriali(GL_FRONT, GL_SHININESS, shininess);
		gl.glMaterialfv(GL_FRONT, GL_EMISSION, emission, 0);
	}

	public void setAmbience(float[] ambience) { this.ambience = ambience; }
	
	public void setEmission(float[] emission) { this.emission = emission; }
	
	public void useSpecular(GL2 gl, boolean on)
	{
		if(on) gl.glLightfv(GL_LIGHT0, GL_SPECULAR, specular, 0);
		else   gl.glLightfv(GL_LIGHT0, GL_SPECULAR, new float[] {0, 0, 0}, 0);
	} 
}
