package bates.jamie.graphics.entity;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.Material;
import bates.jamie.graphics.scene.Model;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.scene.SceneNode;
import bates.jamie.graphics.scene.SceneNode.RenderMode;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.TextureLoader;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.shader.Shader;

public class Mushroom
{
	static Model mushroom_cap  = new Model("mushroom_cap" );
	static Model mushroom_base = new Model("mushroom_base");
	static Model mushroom_eyes = new Model("mushroom_eyes");
	
	static Material cap_material;
	
	SceneNode baseNode;
	SceneNode eyesNode;
	SceneNode capNode;
	
	float rotation = 0;
	Vec3  position;
	
	public Mushroom(GL2 gl, Vec3 p)
	{
		position = p;
		
		baseNode = new SceneNode(mushroom_base);
		baseNode.setTranslation(p);
		baseNode.setScale(new Vec3(0.75));
		baseNode.setRenderMode(RenderMode.COLOR);
		baseNode.setColor(new float[] {0.991f, 0.816f, 0.338f});
		
		eyesNode = new SceneNode(mushroom_eyes);
		eyesNode.setRenderMode(RenderMode.COLOR);
		eyesNode.setColor(RGB.BLACK);
		
		if(cap_material == null) cap_material = new Material(TextureLoader.load(gl,"tex/items/red_mushroom_cap.png"));
		
		capNode = new SceneNode(mushroom_cap);
		capNode.setRenderMode(RenderMode.TEXTURE); 
		capNode.setMaterial(cap_material);
		
		baseNode.addChild(eyesNode);
		baseNode.addChild(capNode);
	}
	
	public void setPosition(Vec3 p)
	{
		position = p;
		
		baseNode.setTranslation(p);
	}
	
	public Vec3 getPosition() { return position; }
	
	public void render(GL2 gl)
	{	
		if(Scene.enableAnimation) rotation += 0.0;
		
		baseNode.setRotation(new Vec3(0, rotation, 0));
		baseNode.render(gl);
		
		Shader.disable(gl);
		
		gl.glColor3f(1, 1, 1);
	}
}
