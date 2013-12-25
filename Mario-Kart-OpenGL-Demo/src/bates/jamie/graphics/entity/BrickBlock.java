package bates.jamie.graphics.entity;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.IndexedModel;
import bates.jamie.graphics.scene.Material;
import bates.jamie.graphics.scene.SceneNode;
import bates.jamie.graphics.scene.SceneNode.MatrixOrder;
import bates.jamie.graphics.scene.SceneNode.RenderMode;
import bates.jamie.graphics.util.OBJParser;
import bates.jamie.graphics.util.Vec3;

public class BrickBlock
{
	static IndexedModel brick_block  = OBJParser.parseTriangleMesh("brick_block");
	static IndexedModel mortar_block = OBJParser.parseTriangleMesh("mortar_block");
	
	SceneNode brickNode;
	SceneNode mortarNode;
	
	float rotation = 0;
	Vec3  position;
	
	
	
	public BrickBlock(Vec3 p, float scale)
	{
		position = p;
		
//		this.rotation = rotation;
		
		brickNode = new SceneNode(null, -1, brick_block, MatrixOrder.T_RY_RX_RZ_S, new Material(new float[] {1, 1, 1}));
		brickNode.setTranslation(p);
		brickNode.setScale(new Vec3(scale));
		brickNode.setRenderMode(RenderMode.COLOR);
		brickNode.setColor(new float[] {0.736f, 0.221f, 0.031f});
		
		mortarNode = new SceneNode(null, -1, mortar_block, MatrixOrder.T_RY_RX_RZ_S, new Material(new float[] {1, 1, 1}));
		mortarNode.setRenderMode(RenderMode.COLOR);
		mortarNode.setColor(new float[] {0.2f, 0.2f, 0.2f});
		
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
