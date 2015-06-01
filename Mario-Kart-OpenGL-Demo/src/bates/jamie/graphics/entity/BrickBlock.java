package bates.jamie.graphics.entity;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.Model;
import bates.jamie.graphics.scene.SceneNode;
import bates.jamie.graphics.scene.SceneNode.RenderMode;
import bates.jamie.graphics.util.Vec3;

public class BrickBlock
{
	static Model brick_block  = new Model("brick_block");
	static Model mortar_block = new Model("mortar_block");
	
	SceneNode brickNode;
	SceneNode mortarNode;
	
	float rotation = 0;
	Vec3  position;
	
	public static float[] BRICK_BROWN = {0.278f, 0.114f, 0.039f};
	public static float[] BRICK_BLACK = {0.022f, 0.022f, 0.022f};
	
	
	public BrickBlock(Vec3 p, float scale)
	{
		position = p;
		
		brickNode = new SceneNode(brick_block);
		brickNode.setTranslation(p);
		brickNode.setScale(new Vec3(scale));
		brickNode.setRenderMode(RenderMode.COLOR);
		brickNode.setColor(BRICK_BROWN);
		
		mortarNode = new SceneNode(mortar_block);
		mortarNode.setRenderMode(RenderMode.COLOR);
		mortarNode.setColor(BRICK_BLACK);
		
		brickNode.addChild(mortarNode);
	}
	
	public void setPosition(Vec3 p)
	{
		position = p;
		
		brickNode.setTranslation(p);
	}
	
	public Vec3 getPosition() { return position; }
	
	public void render(GL2 gl)
	{	
		
		brickNode.setRotation(new Vec3(0, rotation, 0));
		brickNode.render(gl);
		
		gl.glColor3f(1, 1, 1);
	}
}
