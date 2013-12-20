package bates.jamie.graphics.entity;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.Random;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.util.Renderer;
import bates.jamie.graphics.util.Shader;
import bates.jamie.graphics.util.TextureLoader;
import bates.jamie.graphics.util.Vec3;

import com.jogamp.opengl.util.texture.Texture;

public class BrickWall
{
	int dataID;
	float scale;
	
	List<BrickBlock> brickBlocks;
	
	static Texture[] colourMaps = new Texture[3];
	static Texture[] normalMaps = new Texture[3];
	static Texture[] heightMaps = new Texture[3];
	
	RenderMode mode = RenderMode.SIMPLE_MODEL_PARALLAX;
	
	public BrickWall(GL2 gl, List<BrickBlock> blocks, float scale)
	{
		if(colourMaps[0] == null)
		{
			try
			{
				Texture front_colour = TextureLoader.load(gl, "tex/brick_front.png");
				Texture right_colour = TextureLoader.load(gl, "tex/brick_side.png");
				
				Texture front_normal = TextureLoader.load(gl, "tex/brick_front_normal.png");
				Texture right_normal = TextureLoader.load(gl, "tex/brick_side_normal.png");
				
				Texture front_height = TextureLoader.load(gl, "tex/brick_front_height.png");
				Texture right_height = TextureLoader.load(gl, "tex/brick_side_height.png");
				
				colourMaps = new Texture[] {right_colour, front_colour, front_colour};
				normalMaps = new Texture[] {right_normal, front_normal, front_normal};
				heightMaps = new Texture[] {right_height, front_height, front_height};
			}
			catch (Exception e) { e.printStackTrace(); }
		}
		
		brickBlocks = blocks;
		
		this.scale = scale;
		
		createTexture(gl);
		initializeData(gl);
	}
	
	public void createTexture(GL2 gl)
	{
		int[] id = new int[1];
		gl.glGenTextures(1, id, 0);
		dataID = id[0];

		gl.glActiveTexture(GL2.GL_TEXTURE1);
		gl.glBindTexture(GL2.GL_TEXTURE_1D, dataID);
		
		gl.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
		
		gl.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
		gl.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
		
		gl.glTexImage1D(GL2.GL_TEXTURE_1D, 0, GL2.GL_RGBA32F, brickBlocks.size(), 0, GL2.GL_RGBA, GL2.GL_FLOAT, null);
		
		gl.glActiveTexture(GL2.GL_TEXTURE0);
	}
	
	public void initializeData(GL2 gl)
	{
		gl.glActiveTexture(GL2.GL_TEXTURE1);
		gl.glBindTexture(GL2.GL_TEXTURE_1D, dataID);
		
		FloatBuffer positions = FloatBuffer.allocate(brickBlocks.size() * 4);
		
		Random generator = new Random();
		
		for(BrickBlock block : brickBlocks)
		{
			Vec3 position = new Vec3(block.getPosition());

			positions.put(position.toArray());
			positions.put(scale);
		}
		positions.position(0);
		
		gl.glTexSubImage1D(GL2.GL_TEXTURE_1D, 0, 0, brickBlocks.size(), GL2.GL_RGBA, GL2.GL_FLOAT, positions);
		gl.glActiveTexture(GL2.GL_TEXTURE0);
	}
	
	public void render(GL2 gl)
	{
		int[] attachments = {GL2.GL_COLOR_ATTACHMENT0, GL2.GL_COLOR_ATTACHMENT1};
		if(!Scene.reflectMode && Scene.singleton.enableBloom) gl.glDrawBuffers(2, attachments, 0);
		
		switch(mode)
		{
			case COMPLEX_MODEL           : renderComplexModels(gl); break;
			case COMPLEX_MODEL_INSTANCED : renderInstancedModels(gl); break;
			case SIMPLE_MODEL            : renderSimpleModels(gl); break;
			case SIMPLE_MODEL_PARALLAX   : renderNormalMappedModels(gl); break;
		}
		
		if(!Scene.reflectMode && Scene.singleton.enableBloom) gl.glDrawBuffers(1, attachments, 0);
		
		Shader.disable(gl);
	}

	private void renderInstancedModels(GL2 gl)
	{
		gl.glColor3f(0.636f, 0.201f, 0.031f);
		
		Shader shader = Shader.enabled ? Shader.get("phong_instance") : null;
		if(shader != null) shader.enable(gl);
		
		shader.setSampler(gl, "dataMap", 1);
		shader.setUniform(gl, "count", brickBlocks.size());
		
		BrickBlock.brick_block.renderInstanced(gl, brickBlocks.size(), dataID);
		gl.glColor3f(0.1f, 0.1f, 0.1f);
		BrickBlock.mortar_block.renderInstanced(gl, brickBlocks.size(), dataID);
	}
	
	private void renderComplexModels(GL2 gl)
	{
		for(BrickBlock block : brickBlocks)
			block.render(gl);
	}
	
	private void renderSimpleModels(GL2 gl)
	{
		Shader shader = Shader.getLightModel("texture"); shader.enable(gl);
		
		for(BrickBlock block : brickBlocks)
			Renderer.displayTexturedCuboid(gl, block.position, new Vec3(scale),
					0, colourMaps, scale);
	}
	
	private void renderNormalMappedModels(GL2 gl)
	{
		Shader shader = Shader.get("parallax_lights"); shader.enable(gl);
		
		shader.setSampler(gl, "texture"  , 0);
		shader.setSampler(gl, "bumpmap"  , 1);
		shader.setSampler(gl, "heightmap", 2);
		
		for(BrickBlock block : brickBlocks)
			Renderer.displayBumpMappedCuboid(gl, block.position, new Vec3(scale), 0,
					colourMaps, normalMaps, heightMaps, scale);
	}
	
	
	public void cycleMode() { mode = RenderMode.cycle(mode); }
	
	public enum RenderMode
	{
		COMPLEX_MODEL,
		COMPLEX_MODEL_INSTANCED,
		SIMPLE_MODEL,
		SIMPLE_MODEL_PARALLAX;
		
		public static RenderMode cycle(RenderMode mode)
		{
			return values()[(mode.ordinal() + 1) % values().length];
		}
	}		
}
