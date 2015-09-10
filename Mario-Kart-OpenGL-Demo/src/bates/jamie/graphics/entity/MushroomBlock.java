package bates.jamie.graphics.entity;

import java.util.ArrayList;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;

import bates.jamie.graphics.scene.Material;
import bates.jamie.graphics.scene.Model;
import bates.jamie.graphics.scene.SceneNode;
import bates.jamie.graphics.scene.SceneNode.RenderMode;
import bates.jamie.graphics.util.OBJParser;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.TextureLoader;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.shader.Shader;
import bates.jamie.graphics.util.shader.Uniform;

public class MushroomBlock
{
	public static Model mush_block_model = OBJParser.parseTexturedTriangleMesh("mushroom_block");
	
	private SceneNode mush_block_node;
	
	private static Texture mix_factor_map;
	
	
	public MushroomBlock(GL2 gl, Vec3 position, Vec3 rotation)
	{
		if(mix_factor_map == null)
		{
			mix_factor_map = TextureLoader.load(gl, "tex/mushroom_mask.png");
		}
		
		ArrayList<Uniform> uniforms = new ArrayList<Uniform>();
		
		uniforms.add(Uniform.getSampler("texture", 0));
		uniforms.add(Uniform.getUniform("primary_color", new Vec3(0.788, 0.455, 0.239)));
		uniforms.add(Uniform.getUniform("primary_color", new Vec3(1.0, 0.6, 0.2)));
		uniforms.add(Uniform.getUniform("secondary_color", new Vec3(1.0f, 0.8f, 0.4f)));
		
		Material mat = new Material(mix_factor_map);
		mat.setSpecular(RGB.WHITE);
		
		mush_block_node = new SceneNode(mush_block_model);
		mush_block_node.setTranslation(position);
		mush_block_node.setRotation(rotation);
		mush_block_node.setScale(new Vec3(4.5));
		mush_block_node.setRenderMode(RenderMode.COLOR);
		mush_block_node.setShader(Shader.get("phong_mix"));
		mush_block_node.setUniforms(uniforms);
		mush_block_node.setMaterial(mat);
	}
	
	public void render(GL2 gl)
	{
		mush_block_node.render(gl);
	}
}
