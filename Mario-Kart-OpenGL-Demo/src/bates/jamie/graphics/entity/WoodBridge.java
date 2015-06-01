package bates.jamie.graphics.entity;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.Material;
import bates.jamie.graphics.scene.Model;
import bates.jamie.graphics.scene.SceneNode;
import bates.jamie.graphics.scene.SceneNode.RenderMode;
import bates.jamie.graphics.util.TextureLoader;
import bates.jamie.graphics.util.Vec3;

import com.jogamp.opengl.util.texture.Texture;

public class WoodBridge
{
	static Model large_bridge_model = new Model("large_wooden_bridge");
	static Model small_bridge_model = new Model("small_wooden_bridge");
	
	static Material polished_wood;
	
	SceneNode large_bridge_node;
	SceneNode small_bridge_node;
	
	float rotation = 0;
	Vec3  position;
	
	boolean use_large_model;
	
	public WoodBridge(GL2 gl, Vec3 p, float r, boolean large)
	{
		use_large_model = large;
		
		if(polished_wood == null)
		{
			Texture diffuse = TextureLoader.load(gl, "tex/plank_COLOR.jpg");
			Texture normal  = TextureLoader.load(gl, "tex/plank_NRM.jpg");
			Texture	height  = TextureLoader.load(gl, "tex/plank_DISP.jpg");
			
			polished_wood = new Material(diffuse, normal, height);
		}
		
		position = p;
		
		large_bridge_node = new SceneNode(large_bridge_model);
		large_bridge_node.setMaterial(polished_wood);
		large_bridge_node.setTranslation(p);
		large_bridge_node.setScale(new Vec3(15));
		large_bridge_node.setRotation(new Vec3(0, r, 0));
		large_bridge_node.setRenderMode(RenderMode.BUMP_TEXTURE);
		large_bridge_node.useParallax(false);
		
		small_bridge_node = new SceneNode(small_bridge_model);
		small_bridge_node.setMaterial(polished_wood);
		small_bridge_node.setTranslation(p);
		small_bridge_node.setScale(new Vec3(15));
		small_bridge_node.setRotation(new Vec3(0, r, 0));
		small_bridge_node.setRenderMode(RenderMode.BUMP_TEXTURE);
		small_bridge_node.useParallax(false);
	}
	
	public Vec3 getPosition() { return position; }
	
	public void render(GL2 gl)
	{	
		if(use_large_model) large_bridge_node.render(gl);
		else small_bridge_node.render(gl);
	}
	
}
