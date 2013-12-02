package bates.jamie.graphics.entity;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import bates.jamie.graphics.util.Shader;
import bates.jamie.graphics.util.TextureLoader;
import bates.jamie.graphics.util.Vec3;

import com.jogamp.opengl.util.texture.Texture;

public class EnergyField
{
	static Texture normalMap;
	static Texture runeTexture;
	
	float timer = 0;
	
	FieldType type = FieldType.LIGHT;
	
	Vec3 position = new Vec3();
	
	GLU glu = new GLU();
	
	public EnergyField(Vec3 p, FieldType type)
	{
		position = p;
		this.type = type;
	}
	
	public void render(GL2 gl)
	{
		if(normalMap   == null) normalMap   = TextureLoader.load(gl, "tex/bump_maps/water.png");
		if(runeTexture == null) runeTexture = TextureLoader.load(gl, "tex/runes.png");
		
		Shader shader = Shader.get("energy_field"); shader.enable(gl);
		
		timer += 0.1;
		shader.setUniform(gl, "timer", timer);
		
		shader.setUniform(gl, "rim_power", type.intensity);
		shader.setUniform(gl, "rim_color", type.color);
		
		shader.setUniform(gl, "enableRunes", type.enableRunes);
		
		normalMap.bind(gl);
		shader.setSampler(gl, "normalSampler", 0);
		
		gl.glActiveTexture(GL2.GL_TEXTURE1); runeTexture.bind(gl);
		shader.setSampler(gl, "runeSampler", 1);
		gl.glActiveTexture(GL2.GL_TEXTURE0);
		
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
		
		gl.glEnable(GL2.GL_TEXTURE_2D);
		
		GLUquadric sphere = glu.gluNewQuadric();
		
		glu.gluQuadricDrawStyle(sphere, GLU.GLU_FILL);
		glu.gluQuadricTexture  (sphere, true);
		
		gl.glPushMatrix();
		{
			gl.glTranslatef(position.x, position.y, position.z);
			gl.glRotatef( 90, 0, 0, 1);
			glu.gluSphere(sphere, 30, 64, 64);
		}
		gl.glPopMatrix();
		
		Shader.disable(gl);
		
		gl.glDisable(GL2.GL_BLEND);
	}
	
	public enum FieldType
	{
		LIGHT    (2.0f, new float[] {.4f, .7f, .9f}, false),
		ENERGIZED(2.0f, new float[] {  1,   1, .5f}, true),
		SUPER    (2.0f, new float[] {.8f, .4f, .4f}, true);
		
		public float   intensity;
		public float[] color;
		
		public boolean enableRunes;
		
		private FieldType(float intensity, float[] color, boolean enableRunes)
		{
			this.intensity = intensity;
			this.color = color;
			this.enableRunes = enableRunes;
		}
		
		public static FieldType cycle(FieldType type)
		{
			return values()[(type.ordinal() + 1) % values().length];
		}
	}
	
	public void cycle() { type = FieldType.cycle(type); }
}
