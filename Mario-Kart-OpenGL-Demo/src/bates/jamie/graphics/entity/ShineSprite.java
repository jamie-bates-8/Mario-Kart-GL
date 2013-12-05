package bates.jamie.graphics.entity;

import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL2.GL_QUADS;

import java.util.List;

import javax.media.opengl.GL2;

import bates.jamie.graphics.particle.Particle;
import bates.jamie.graphics.particle.ParticleGenerator;
import bates.jamie.graphics.scene.Light;
import bates.jamie.graphics.scene.Material;
import bates.jamie.graphics.scene.Model;
import bates.jamie.graphics.scene.Reflector;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.scene.SceneNode;
import bates.jamie.graphics.scene.SceneNode.MatrixOrder;
import bates.jamie.graphics.scene.SceneNode.RenderMode;
import bates.jamie.graphics.util.OBJParser;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.Shader;
import bates.jamie.graphics.util.Vec3;

public class ShineSprite
{
	static Model shineSprite = OBJParser.parseTriangleMesh("shine_sprite");
	static Model shineEyes   = OBJParser.parseTriangleMesh("shine_eyes");
	
	SceneNode shineNode;
	SceneNode eyeNode;
	
	public Reflector reflector;
	
	ParticleGenerator rayGenerator;
	ParticleGenerator sparkleGenerator;
	
	float rotation = 0;
	Vec3  position;
	
	boolean collected = false;
	
	public ShineSprite(Vec3 p)
	{
		position = p;
		
		reflector = new Reflector(1.0f);
		
		shineNode = new SceneNode(null, -1, shineSprite, MatrixOrder.T_RY_RX_RZ_S, new Material(new float[] {1, 1, 1}));
		shineNode.setTranslation(p);
		shineNode.setScale(new Vec3(1.75));
		shineNode.setReflector(reflector);
		shineNode.setReflectivity(0.75f);
		shineNode.setRenderMode(RenderMode.REFLECT);
		shineNode.setColor(new float[] {1, 1, 0.2f});
		
		eyeNode = new SceneNode(null, -1, shineEyes, MatrixOrder.T_RY_RX_RZ_S, new Material(new float[] {1, 1, 1}));
		eyeNode.setTranslation(p);
		eyeNode.setScale(new Vec3(1.75));
		eyeNode.setRenderMode(RenderMode.COLOR);
		eyeNode.setColor(RGB.BLACK);
		
		List<ParticleGenerator> generators = Scene.singleton.generators;
		
		rayGenerator     = new ParticleGenerator(20, 1, ParticleGenerator.GeneratorType.RAY,     p);
		sparkleGenerator = new ParticleGenerator( 3, 2, ParticleGenerator.GeneratorType.SPARKLE, p);
		
		generators.add(rayGenerator);
		generators.add(sparkleGenerator);
	}
	
	public void setPosition(Vec3 p)
	{
		position = p;
		
		shineNode.setTranslation(p);
		eyeNode.setTranslation(p);
		
		rayGenerator.setSource(p);
		sparkleGenerator.setSource(p);
	}
	
	public Vec3 getPosition() { return position; }
	
	public void render(GL2 gl)
	{	
		if(Scene.enableAnimation) rotation += 4.0;
		
		float   rimPower = Light.rimPower;
		float[] rimColor = Light.rimColor;
		
		Light.rimPower = 1.0f;
		Light.rimColor = new float[] {.7f, .7f, .7f};
		
		Light.setepRimLighting(gl);
		
		shineNode.setRotation(new Vec3(0, rotation, 0));
		
		if(collected) shineNode.renderGhost(gl, 1, Shader.get("aberration"));
		else          shineNode.render(gl);
		
		eyeNode.setRotation(new Vec3(0, rotation, 0));
		eyeNode.render(gl);
		
		Light.rimPower = rimPower;
		Light.rimColor = rimColor;
		
		Light.setepRimLighting(gl);
		
		Shader.disable(gl);
		
		gl.glColor3f(1, 1, 1);
	}
	
	public void renderFlare(GL2 gl, float rotation)
	{	
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
		
		Car car = Scene.singleton.getCars().get(0);
		
		gl.glPushMatrix();
		{	
			gl.glTranslatef(position.x, position.y, position.z);
			gl.glRotatef(rotation, 0, -1, 0);
			gl.glScalef(10, 10, 10);
			
			Particle.lens_flare_1.bind(gl);
			
			gl.glEnable(GL2.GL_TEXTURE_2D);
			gl.glEnable(GL_BLEND);
			
			gl.glBegin(GL_QUADS);
			{
				gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(-0.5f, -0.5f, 0.0f);
				gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(-0.5f,  0.5f, 0.0f);
				gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f( 0.5f,  0.5f, 0.0f);
				gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f( 0.5f, -0.5f, 0.0f);
			}
			gl.glEnd();
			
			gl.glDisable(GL_BLEND);
		}
		gl.glPopMatrix();
	}

	public boolean isCollected() { return collected; }
	
	public void setCollected(boolean collected) { this.collected = collected; }
	
	
}
