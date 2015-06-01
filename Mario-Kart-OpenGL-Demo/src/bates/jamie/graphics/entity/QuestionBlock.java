package bates.jamie.graphics.entity;

import java.io.File;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.Material;
import bates.jamie.graphics.scene.Model;
import bates.jamie.graphics.scene.Reflector;
import bates.jamie.graphics.scene.SceneNode;
import bates.jamie.graphics.scene.SceneNode.RenderMode;
import bates.jamie.graphics.util.Vec3;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class QuestionBlock
{
	static Model block_model  = new Model("question_block_box");
	static Model symbol_model = new Model("question_block_symbol");
	static Model bevelled_cube_model = new Model("bevelled_block");
	
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
		
		blockNode = new SceneNode(block_model);
		blockNode.setTranslation(p);
		blockNode.setScale(new Vec3(scale));
		blockNode.setReflector(reflector);
		blockNode.setReflectivity(0.75f);
		blockNode.setRenderMode(RenderMode.COLOR);
		blockNode.setColor(new float[] {1.0f, 0.8f, 0.0f});
		
		symbolNode = new SceneNode(symbol_model);
		symbolNode.setReflector(reflector);
		symbolNode.setReflectivity(0.75f);
		symbolNode.setRenderMode(RenderMode.COLOR);
		
		blockNode.addChild(symbolNode);
		
		Material simple_mat = new Material(colourMap, normalMap, heightMap);
		
		bevelled_cube_model.calculateTangents();
		
		simpleNode = new SceneNode(bevelled_cube_model);
		simpleNode.setTranslation(p);
		simpleNode.setScale(new Vec3(scale));
		simpleNode.setReflector(reflector);
		simpleNode.setReflectivity(0.85f);
		simpleNode.setRenderMode(RenderMode.BUMP_TEXTURE);
		simpleNode.setMaterial(simple_mat);
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
