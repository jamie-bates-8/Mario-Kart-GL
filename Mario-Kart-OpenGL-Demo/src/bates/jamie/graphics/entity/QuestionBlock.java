package bates.jamie.graphics.entity;

import java.io.File;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.Material;
import bates.jamie.graphics.scene.Model;
import bates.jamie.graphics.scene.Reflector;
import bates.jamie.graphics.scene.SceneNode;
import bates.jamie.graphics.scene.SceneNode.MatrixOrder;
import bates.jamie.graphics.scene.SceneNode.RenderMode;
import bates.jamie.graphics.util.Renderer;
import bates.jamie.graphics.util.Vec3;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class QuestionBlock
{
	static Model block_model  = new Model("question_block_box");
	static Model symbol_model = new Model("question_block_symbol");
	
	static Texture colourMap, normalMap, heightMap;
	
	static
	{
		try
		{
			colourMap = TextureIO.newTexture(new File("tex/question_block_colour.png"), true);
			normalMap = TextureIO.newTexture(new File("tex/question_block_normal.png"), true);
			heightMap = TextureIO.newTexture(new File("tex/question_block_height.png"), true);
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	SceneNode blockNode;
	SceneNode symbolNode;
	
	SceneNode simpleNode;
	
	float rotation = 0;
	Vec3  position;
	
	public Reflector reflector;
	
	public QuestionBlock(Vec3 p, float scale)
	{
		position = p;

		reflector = new Reflector(1.0f);
		
		blockNode = new SceneNode(null, -1, block_model, MatrixOrder.T_RY_RX_RZ_S, new Material(new float[] {1, 1, 1}));
		blockNode.setTranslation(p);
		blockNode.setScale(new Vec3(scale));
		blockNode.setReflector(reflector);
		blockNode.setReflectivity(0.75f);
		blockNode.setRenderMode(RenderMode.COLOR);
		blockNode.setColor(new float[] {1.0f, 0.8f, 0.0f});
		
		symbolNode = new SceneNode(null, -1, symbol_model, MatrixOrder.T_RY_RX_RZ_S, new Material(new float[] {1, 1, 1}));
		symbolNode.setReflector(reflector);
		symbolNode.setReflectivity(0.75f);
		symbolNode.setRenderMode(RenderMode.COLOR);
		symbolNode.setColor(new float[] {1.0f, 1.0f, 1.0f});
		
		blockNode.addChild(symbolNode);
		
		Renderer.multi_tex_bevelled_cube_model.colourMap = colourMap;
		Renderer.multi_tex_bevelled_cube_model.normalMap = normalMap;
		Renderer.multi_tex_bevelled_cube_model.heightMap = heightMap;
		
		Renderer.multi_tex_bevelled_cube_model.calculateTangents();
		
		simpleNode = new SceneNode(null, -1, Renderer.multi_tex_bevelled_cube_model, MatrixOrder.T_RY_RX_RZ_S, new Material(new float[] {1, 1, 1}));
		simpleNode.setTranslation(p);
		simpleNode.setScale(new Vec3(scale));
		simpleNode.setReflector(reflector);
		simpleNode.setReflectivity(0.85f);
		simpleNode.setRenderMode(RenderMode.BUMP_TEXTURE);
		simpleNode.setColor(new float[] {1.0f, 1.0f, 1.0f});
		simpleNode.useParallax(false);
	}
	
	public void setPosition(Vec3 p)
	{
		position = p;
		
		blockNode.setTranslation(p);
	}
	
	public Vec3 getPosition() { return position; }
	
	public void render(GL2 gl)
	{	
		
		blockNode.setRotation(new Vec3(0, rotation, 0));
//		blockNode.renderGhost(gl, 1, Shader.get("aberration"));
		blockNode.render(gl);
		
		gl.glColor3f(1, 1, 1);
	}
	
	public void updateReflection(GL2 gl) { blockNode.updateReflection(gl); }
}
