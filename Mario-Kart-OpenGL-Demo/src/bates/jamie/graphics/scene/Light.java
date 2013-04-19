package bates.jamie.graphics.scene;

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
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;


public class Light extends AnchorPoint
{
	public static int count = 0;
	
	public int id;
	
	private float[] ambience = {0.2f, 0.2f, 0.2f, 1.0f};
	private float[] diffuse  = {0.7f, 0.7f, 0.7f, 1.0f};
	private float[] specular = {1.0f, 1.0f, 1.0f, 1.0f};
	private float[] emission = {0.0f, 0.0f, 0.0f, 1.0f};
	
	private int shininess = 128;

	public float[] direction = {0, -1, 0};

	public boolean smooth    = true;
	public boolean parallel  = false;
	public boolean local     = false;
	public boolean secondary = true;

	public Light(GL2 gl)
	{
		id = count++; count %= 8;
		
		setPosition(new float[] {1, 1, 0});
		setPosition(new float[] {200, 200, 200});

		gl.glEnable(GL_LIGHTING);
		gl.glEnable(getLight(id));

		gl.glEnable(GL2.GL_NORMALIZE);
		gl.glShadeModel(GL_SMOOTH);

		gl.glEnable(GL2.GL_COLOR_MATERIAL);
		gl.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);
	}
	
	public static int getLight(int id)
	{
		switch(id)
		{
			case 0: return GL2.GL_LIGHT0;
			case 1: return GL2.GL_LIGHT1;
			case 2: return GL2.GL_LIGHT2;
			case 3: return GL2.GL_LIGHT3;
			case 4: return GL2.GL_LIGHT4;
			case 5: return GL2.GL_LIGHT5;
			case 6: return GL2.GL_LIGHT6;
			case 7: return GL2.GL_LIGHT7;
			
			default: return 0;
		}
	}
	
	public void render(GL2 gl, GLU glu)
	{
		gl.glColor3f(0.2f, 0.2f, 0.2f);
		
		gl.glDisable(GL2.GL_TEXTURE_2D);
		
		GLUquadric sphere = glu.gluNewQuadric();
		
		glu.gluQuadricDrawStyle(sphere, GLU.GLU_FILL);
		
		gl.glPushMatrix();
		{	
			float[] p = getPosition();
			
			gl.glTranslatef(p[0], p[1], p[2]);
			
			glu.gluSphere(sphere, 2, 32, 32);
		}
		gl.glPopMatrix();
		
		gl.glEnable(GL2.GL_TEXTURE_2D);
		
		gl.glColor3f(1, 1, 1);
	}

	public void setup(GL2 gl, boolean spotlight)
	{
		int light = getLight(id);
		
		if(smooth) gl.glShadeModel(GL_SMOOTH);
		else       gl.glShadeModel(GL_FLAT  );

		gl.glLightModelfv(GL_LIGHT_MODEL_AMBIENT, ambience, 0);
		gl.glLightModeli (GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, local ? GL2.GL_TRUE : GL2.GL_FALSE);
		
		if(secondary) gl.glLightModeli(GL2.GL_LIGHT_MODEL_COLOR_CONTROL, GL2.GL_SEPARATE_SPECULAR_COLOR);
		else          gl.glLightModeli(GL2.GL_LIGHT_MODEL_COLOR_CONTROL, GL2.GL_SINGLE_COLOR);

		gl.glLightfv(light, GL_AMBIENT,  ambience, 0);
		gl.glLightfv(light, GL_DIFFUSE,  diffuse,  0);
		gl.glLightfv(light, GL_SPECULAR, specular, 0);

		if(spotlight)
		{
			gl.glLightf (light, GL_SPOT_CUTOFF, 30.0f);
			gl.glLightfv(light, GL_SPOT_DIRECTION, direction, 0);
		}

		float[] p = getPosition();
		float[] _p = {p[0], p[1], p[2], parallel ? 0 : 1};
		gl.glLightfv(light, GL_POSITION, _p, 0);

		gl.glMaterialfv(GL_FRONT, GL_SPECULAR,  specular, 0);
		gl.glMateriali (GL_FRONT, GL_SHININESS, shininess  );
		gl.glMaterialfv(GL_FRONT, GL_EMISSION,  emission, 0);
	}
	
	public float[] getAmbience() { return ambience; }
	public float[] getEmission() { return emission; }
	public float[] getSpecular() { return specular; }
	public float[] getDiffuse () { return diffuse ; }

	public void setAmbience(float[] ambience) { this.ambience = ambience; }
	public void setEmission(float[] emission) { this.emission = emission; }
	public void setSpecular(float[] specular) { this.specular = specular; }
	public void setDiffuse (float[] diffuse ) { this.diffuse  = diffuse;  } 
	
	public static void globalSpecular(GL2 gl, float[] specular)
	{	
		gl.glLightfv(GL_LIGHT0, GL_SPECULAR, specular, 0);
	}

	public void setShininess(int shininess)
	{
		if(shininess <   0) shininess =   0;
		if(shininess > 128) shininess = 128;
		
		this.shininess = shininess;
	}
}
