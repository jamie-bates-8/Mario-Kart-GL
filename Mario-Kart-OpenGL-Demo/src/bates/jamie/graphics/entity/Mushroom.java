package bates.jamie.graphics.entity;

import static bates.jamie.graphics.util.Renderer.displayTexturedObject;

import java.util.List;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.Material;
import bates.jamie.graphics.scene.IndexedModel;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.scene.SceneNode;
import bates.jamie.graphics.scene.SceneNode.MatrixOrder;
import bates.jamie.graphics.scene.SceneNode.RenderMode;
import bates.jamie.graphics.util.Face;
import bates.jamie.graphics.util.OBJParser;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.Shader;
import bates.jamie.graphics.util.Vec3;

public class Mushroom
{
	private static final List<Face> CAP_FACES = OBJParser.parseTriangles("mushroom_cap");
	
	static IndexedModel mushroom_base = OBJParser.parseTriangleMesh("mushroom_base");
	static IndexedModel mushroom_eyes = OBJParser.parseTriangleMesh("mushroom_eyes");
	
	private static int mushroom_cap = -1;
	
	SceneNode baseNode;
	SceneNode eyesNode;
	SceneNode capNode;
	
	float rotation = 0;
	Vec3  position;
	
	public Mushroom(GL2 gl, Vec3 p)
	{
		position = p;
		
		if(mushroom_cap == -1)
		{
			mushroom_cap = gl.glGenLists(1);
			gl.glNewList(mushroom_cap, GL2.GL_COMPILE);
			displayTexturedObject(gl, CAP_FACES);
		    gl.glEndList();
		}
		
		baseNode = new SceneNode(null, -1, mushroom_base, MatrixOrder.T_RY_RX_RZ_S, new Material(new float[] {1, 1, 1}));
		baseNode.setTranslation(p);
		baseNode.setScale(new Vec3(0.75));
		baseNode.setRenderMode(RenderMode.COLOR);
		baseNode.setColor(new float[] {0.991f, 0.816f, 0.338f});
		
		eyesNode = new SceneNode(null, -1, mushroom_eyes, MatrixOrder.T_RY_RX_RZ_S, new Material(new float[] {1, 1, 1}));
		eyesNode.setRenderMode(RenderMode.COLOR);
		eyesNode.setColor(RGB.BLACK);
		
		capNode = new SceneNode(null, mushroom_cap, null, MatrixOrder.T_RY_RX_RZ_S, new Material(new float[] {1, 1, 1}));
		capNode.setRenderMode(RenderMode.TEXTURE); 
		
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
