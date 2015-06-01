package bates.jamie.graphics.entity;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.Light;
import bates.jamie.graphics.scene.Model;
import bates.jamie.graphics.scene.Reflector;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.scene.SceneNode;
import bates.jamie.graphics.scene.SceneNode.RenderMode;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.shader.Shader;

public class Balloon
{
	static Model balloon_model = new Model("balloon");
	
	SceneNode balloonNode;
	
	public Reflector reflector;
	
	Vec3  position;
	
	boolean collected = false;
	
	public Balloon(Vec3 p)
	{
		position = p;
		
		reflector = new Reflector(1.0f, 320, false);
		
		balloonNode = new SceneNode(balloon_model);
		balloonNode.setTranslation(p);
		balloonNode.setScale(new Vec3(3.00));
		balloonNode.setReflector(reflector);
		balloonNode.setReflectivity(0.95f);
		balloonNode.setRenderMode(RenderMode.REFLECT);
		balloonNode.setColor(new float[] {1, 0.4f, 0.4f});
	}
	
	public void setPosition(Vec3 p)
	{
		position = p;
		
		balloonNode.setTranslation(p);
	}
	
	public Vec3 getPosition() { return position; }
	
	public void render(GL2 gl)
	{	
		
		float   rimPower = Light.rimPower;
		float[] rimColor = Light.rimColor;
		
		Light.rimPower = 1.0f;
		Light.rimColor = new float[] {.7f, .7f, .7f};
		
		Light.setepRimLighting(gl);
		
		balloonNode.setColor(Scene.singleton.getCars().get(0).getColor());
		
		if(collected) balloonNode.renderGhost(gl, 1, Shader.get("aberration"));
		else          balloonNode.render(gl);
		
		Light.rimPower = rimPower;
		Light.rimColor = rimColor;
		
		Light.setepRimLighting(gl);
		
		Shader.disable(gl);
		
		gl.glColor3f(1, 1, 1);
	}

	public boolean isCollected() { return collected; }
	
	public void setCollected(boolean collected) { this.collected = collected; }
	
	
}
