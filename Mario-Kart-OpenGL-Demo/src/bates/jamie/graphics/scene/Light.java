package bates.jamie.graphics.scene;

import static javax.media.opengl.GL.GL_FRONT;
import static javax.media.opengl.GL2ES1.GL_LIGHT_MODEL_AMBIENT;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_AMBIENT;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_CONSTANT_ATTENUATION;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_DIFFUSE;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_EMISSION;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_FLAT;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHT0;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LINEAR_ATTENUATION;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_POSITION;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_QUADRATIC_ATTENUATION;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SHININESS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SMOOTH;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SPECULAR;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SPOT_CUTOFF;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SPOT_DIRECTION;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import bates.jamie.graphics.util.Shader;
import bates.jamie.graphics.util.Vec3;


public class Light extends AnchorPoint
{
	public static int count = 0;
	
	private static float[] globalAmbience = {0.2f, 0.2f, 0.2f, 1.0f};
	
	public static boolean smoothShading    = true;
	public static boolean localViewer      = true;
	public static boolean seperateSpecular = true;
	
	public int id;
	
	public boolean parallel  = false;
	public boolean spotlight = false;
	
	private float[] ambience = {0.2f, 0.2f, 0.2f, 1.0f};
	private float[] diffuse  = {0.7f, 0.7f, 0.7f, 1.0f};
	private float[] specular = {1.0f, 1.0f, 1.0f, 1.0f};
	private float[] emission = {0.0f, 0.0f, 0.0f, 1.0f};
	
	public boolean enableAttenuation = true;
	
	private float constantAttenuation  = 0.50f;
	private float linearAttenuation    = 0.02f;
	private float quadraticAttenuation = 0.00f;
	
	private float originalAttenuation = 0.02f;
	
	public static int shininess = 128;

	public Vec3 direction = Vec3.NEGATIVE_Y_AXIS;

	public Light(GL2 gl)
	{
		id = count++; count %= 8;
		
		setPosition(new Vec3(250));

		gl.glEnable(GL_LIGHTING);
		gl.glEnable(getLight(id));

		gl.glEnable(GL2.GL_COLOR_MATERIAL);
		gl.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);
	}
	
	public Light(GL2 gl, Vec3 p, float[] ambience, float[] diffuse, float[] specular)
	{
		id = count++; count %= 8;
		
		setPosition(p);

		gl.glEnable(GL_LIGHTING);
		gl.glEnable(getLight(id));
		
		this.ambience = ambience;
		this.diffuse  = diffuse;
		this.specular = specular;

		gl.glEnable(GL2.GL_COLOR_MATERIAL);
		gl.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);
	}
	
	public static int getLight(int id)
	{
		return GL2.GL_LIGHT0 + id;
	}
	
	public void render(GL2 gl)
	{
		GLU glu = new GLU();
		
		int[] attachments = {GL2.GL_COLOR_ATTACHMENT0, GL2.GL_COLOR_ATTACHMENT1};
		
		gl.glDrawBuffers(2, attachments, 0);
		gl.glColor4f(diffuse[0], diffuse[1], diffuse[2], 1);
		gl.glDisable(GL2.GL_TEXTURE_2D);
		
		GLUquadric sphere = glu.gluNewQuadric();
		glu.gluQuadricDrawStyle(sphere, GLU.GLU_FILL);
		
		gl.glPushMatrix();
		{	
			Vec3 p = getPosition();
			gl.glTranslatef(p.x, p.y, p.z);
			
			glu.gluSphere(sphere, 2, 32, 32);
			
			gl.glColor4f(diffuse[0], diffuse[1], diffuse[2], .5f);
			glu.gluQuadricDrawStyle(sphere, GLU.GLU_LINE);
			glu.gluSphere(sphere, getRadius(), 32, 32);
			
		}
		gl.glPopMatrix();
		
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glColor4f(1, 1, 1, 1);
		gl.glDrawBuffers(1, attachments, 0);
	}
	
	public static void setupModel(GL2 gl)
	{
		gl.glShadeModel(smoothShading ? GL_SMOOTH : GL_FLAT);
		
		gl.glLightModelfv(GL_LIGHT_MODEL_AMBIENT, globalAmbience, 0);
		
		gl.glLightModeli(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, localViewer ? GL2.GL_TRUE : GL2.GL_FALSE);
		gl.glLightModeli(GL2.GL_LIGHT_MODEL_COLOR_CONTROL, seperateSpecular ? GL2.GL_SEPARATE_SPECULAR_COLOR : GL2.GL_SINGLE_COLOR);
	}
	
	public static float[] rimColor = {0.7f, 0.7f, 0.7f};
	public static float   rimPower = 5.0f;
	
	public static void setepRimLighting(GL2 gl)
	{
		Shader shaders[] = {Shader.get("phong_rim"), Shader.get("texture_rim"), Shader.get("cube_rim"), Shader.get("energy_field")};
		
		for(Shader shader : shaders)
		{
			shader.enable(gl);
			
			shader.setUniform(gl, "rim_color", rimColor);
			shader.setUniform(gl, "rim_power", rimPower);
		}
		Shader.disable(gl);	
	}

	public void setup(GL2 gl)
	{
		int light = getLight(id);

		gl.glLightfv(light, GL_AMBIENT,  ambience, 0);
		gl.glLightfv(light, GL_DIFFUSE,  diffuse,  0);
		gl.glLightfv(light, GL_SPECULAR, specular, 0);
		
		gl.glLightf(light, GL_CONSTANT_ATTENUATION , enableAttenuation ? constantAttenuation  : 1);
		gl.glLightf(light, GL_LINEAR_ATTENUATION   , enableAttenuation ? linearAttenuation    : 0);
		gl.glLightf(light, GL_QUADRATIC_ATTENUATION, enableAttenuation ? quadraticAttenuation : 0);

		if(spotlight)
		{
			gl.glLightf (light, GL_SPOT_CUTOFF, 30.0f);
			gl.glLightfv(light, GL_SPOT_DIRECTION, direction.toArray(), 0);
		}

		Vec3 p = getPosition();
		float[] _p = {p.x, p.y, p.z, parallel ? 0 : 1};
		gl.glLightfv(light, GL_POSITION, _p, 0);

		gl.glMaterialfv(GL_FRONT, GL_SPECULAR,  specular, 0);
		gl.glMateriali (GL_FRONT, GL_SHININESS, shininess);
		gl.glMaterialfv(GL_FRONT, GL_EMISSION,  emission, 0);
	}
	
	public void enable(GL2 gl)
	{
		linearAttenuation = originalAttenuation;
	}
	
	public void disable(GL2 gl)
	{
		if(linearAttenuation != 100) originalAttenuation = linearAttenuation;
		linearAttenuation = 100;
	}
	
	public float getRadius()
	{
		float radius = 1E-5f;
		float attenuation = 1;
		
		while(attenuation > 0.1)
		{
			radius++;
			attenuation = 1.0f / (constantAttenuation  +
								  linearAttenuation    * radius +
								  quadraticAttenuation * radius * radius);
		}

		return radius;
	}
	
	public float[] getAmbience() { return ambience; }
	public float[] getEmission() { return emission; }
	public float[] getSpecular() { return specular; }
	public float[] getDiffuse () { return diffuse ; }

	public void setAmbience(float[] ambience) { this.ambience = ambience; }
	public void setEmission(float[] emission) { this.emission = emission; }
	public void setSpecular(float[] specular) { this.specular = specular; }
	public void setDiffuse (float[] diffuse ) { this.diffuse  = diffuse;  } 
	
	public void setConstantAttenuation (float c) { this.constantAttenuation  = c; }
	public void setLinearAttenuation   (float l) { this.linearAttenuation    = l; }
	public void setQuadraticAttenuation(float q) { this.quadraticAttenuation = q; }
	
	public static void globalSpecular(GL2 gl, float[] specular)
	{	
		gl.glLightfv(GL_LIGHT0, GL_SPECULAR, specular, 0);
	}

	public static void setShininess(int s)
	{
		shininess = s;
		
		if(shininess <   0) shininess =   0;
		if(shininess > 128) shininess = 128;
	}
}
