package bates.jamie.graphics.entity;

import java.nio.FloatBuffer;
import java.util.List;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.Reflector;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.scene.process.BloomStrobe;
import bates.jamie.graphics.util.Renderer;
import bates.jamie.graphics.util.TextureLoader;
import bates.jamie.graphics.util.TimeQuery;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.shader.Shader;

import com.jogamp.opengl.util.texture.Texture;

public class BrickWall
{
	float scale;
	
	List<BrickBlock> brickBlocks;
	
	static Texture[] colourMaps = new Texture[3];
	static Texture[] normalMaps = new Texture[3];
	static Texture[] heightMaps = new Texture[3];
	
	static Texture colourMap, normalMap, heightMap;	
	
	RenderMode mode = RenderMode.SIMPLE_MODEL_PARALLAX_INSTANCED;
	
	public TimeQuery timeQuery = new TimeQuery(TimeQuery.FOLIAGE_ID);
	
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
				
				colourMap = TextureLoader.load(gl, "tex/brick_colour.png");
				normalMap = TextureLoader.load(gl, "tex/brick_normal.png");
				heightMap = TextureLoader.load(gl, "tex/brick_height.png");
			}
			catch (Exception e) { e.printStackTrace(); }
		}
		
		brickBlocks = blocks;
		
		this.scale = scale;

		initializeData(gl);
	}
	
	public void initializeData(GL2 gl)
	{
		FloatBuffer positions = FloatBuffer.allocate(brickBlocks.size() * 4);
		
		for(BrickBlock block : brickBlocks)
		{
			Vec3 position = new Vec3(block.getPosition());

			positions.put(position.toArray());
			positions.put(scale);
		}
		positions.position(0);
		
		Renderer.multi_tex_cube_model.setInstanceData(positions);
		BrickBlock.brick_block.setInstanceData(positions);
		BrickBlock.mortar_block.setInstanceData(positions);
	}
	
	public void render(GL2 gl)
	{
		timeQuery.getResult(gl);
		timeQuery.begin(gl);
		
		boolean useHDR = BloomStrobe.isEnabled();
		
		if(Scene.reflectMode) BloomStrobe.end(gl);
		
		switch(mode)
		{
			case COMPLEX_MODEL                   : renderComplexModel(gl); break;
			case COMPLEX_MODEL_INSTANCED         : renderComplexModelInstanced(gl); break;
			case SIMPLE_MODEL                    : renderSimpleModel(gl); break;
			case SIMPLE_MODEL_PARALLAX           : renderSimpleModelParallax(gl); break;
			case SIMPLE_MODEL_PARALLAX_INSTANCED : renderSimpleModelParallaxInstanced(gl); break;
		}
		
		if(useHDR) BloomStrobe.begin(gl);
		
		Shader.disable(gl);
		
		timeQuery.end(gl);
	}

	private void renderComplexModelInstanced(GL2 gl)
	{
		gl.glColor3f(0.636f, 0.201f, 0.031f);
			
		Shader shader = Shader.get("phong_instance");
		if(shader != null) shader.enable(gl);
			
		BrickBlock.brick_block.renderInstanced(gl, brickBlocks.size());
		gl.glColor3f(0.1f, 0.1f, 0.1f);
		BrickBlock.mortar_block.renderInstanced(gl, brickBlocks.size());
	}

	private void renderSimpleModelParallaxInstanced(GL2 gl)
	{
		Shader shader = Shader.get("bump_instance");
		if(shader != null) shader.enable(gl);
		
		shader.setSampler(gl, "texture"  , 0);
		shader.setSampler(gl, "bumpmap"  , 1);
		shader.setSampler(gl, "heightmap", 2);
		
		gl.glActiveTexture(GL2.GL_TEXTURE2); heightMap.bind(gl);
		gl.glActiveTexture(GL2.GL_TEXTURE1); normalMap.bind(gl);
		gl.glActiveTexture(GL2.GL_TEXTURE0); colourMap.bind(gl);
		
		Renderer.multi_tex_cube_model.renderInstanced(gl, brickBlocks.size());
	}
	
	private void renderComplexModel(GL2 gl)
	{
		for(BrickBlock block : brickBlocks)
			block.render(gl);
	}
	
	private void renderSimpleModel(GL2 gl)
	{
		Shader shader = Shader.getLightModel("texture"); shader.enable(gl);
		
		colourMaps[0].bind(gl);
		
		for(BrickBlock block : brickBlocks)
			Renderer.displayTexturedCuboid(gl, block.position, new Vec3(scale),
					0, colourMaps, scale);
	}
	
	private void renderSimpleModelParallax(GL2 gl)
	{
		Shader shader = Shader.get("bump_rain"); shader.enable(gl);
		
		shader.setSampler(gl, "colourMap"  , 0);
		shader.setSampler(gl, "bumpmap"  , 1);
		shader.setSampler(gl, "heightmap", 2);
		shader.setSampler(gl, "rainMap", 3);
		
		shader.setUniform(gl, "timer", Scene.sceneTimer);

		gl.glActiveTexture(GL2.GL_TEXTURE3); Scene.singleton.rain_normal.bind(gl);
		gl.glActiveTexture(GL2.GL_TEXTURE2); heightMap.bind(gl);
		gl.glActiveTexture(GL2.GL_TEXTURE1); normalMap.bind(gl);
		gl.glActiveTexture(GL2.GL_TEXTURE0); colourMap.bind(gl);
		
		shader.setSampler(gl, "cubeMap", Reflector.CUBE_MAP_TEXTURE_UNIT);
		shader.setUniform(gl, "shininess", 0.85f);
		
		float[] camera = Scene.singleton.getCars().get(0).camera.getMatrix();
		shader.loadMatrix(gl, "cameraMatrix", camera);
		
		if(Scene.enableParallax)
		{
			Scene.singleton.cubeReflector.enable(gl);
			
			for(BrickBlock block : brickBlocks)
				Renderer.displayBumpMappedCube(gl, block.position, scale, 0);
			
			Scene.singleton.cubeReflector.disable(gl);
		}
		else
		{
			for(BrickBlock block : brickBlocks)
				Renderer.displayBumpMappedCubeAccelerated(gl, block.position, scale, 0);
		}
	}
	
	
	public void cycleMode()
	{
		mode = RenderMode.cycle(mode);
		timeQuery.reset();
		TimeQuery.resetCache();
	}
	
	public enum RenderMode
	{
		COMPLEX_MODEL,
		COMPLEX_MODEL_INSTANCED,
		SIMPLE_MODEL,
		SIMPLE_MODEL_PARALLAX,
		SIMPLE_MODEL_PARALLAX_INSTANCED;
		
		public static RenderMode cycle(RenderMode mode)
		{
			return values()[(mode.ordinal() + 1) % values().length];
		}
	}		
}
