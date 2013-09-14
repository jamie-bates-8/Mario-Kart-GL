package bates.jamie.graphics.entity;

import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY;

import java.nio.FloatBuffer;
import java.util.Arrays;

import javax.media.opengl.GL2;

import bates.jamie.graphics.util.Shader;
import bates.jamie.graphics.util.TextureLoader;

import com.jogamp.opengl.util.texture.Texture;

public class GrassPatch
{
	private Texture perturbMap;
	private int heightMap;
	
	private Quadtree surface;
	
	private float origin [];
	private int   length;
	private float spread;
	
	private float timer = 0.0f;
	
	private FloatBuffer vBuffer;
	
	public boolean update = false;
	
	private static final float[] MODEL_DATA =
	{
		-0.30f, 0.0f,
         0.30f, 0.0f,
        -0.20f, 1.0f,
         0.10f, 1.3f,
        -0.05f, 2.3f,
         0.00f, 3.3f
	};
	
	public GrassPatch(GL2 gl, Quadtree surface, float[] origin, int length, float spread)
	{
		this.surface = surface;
		
		this.origin = origin;
		this.length = length;
		this.spread = spread;
		
		createBuffers(gl);
		createTexture(gl);
		updateHeights(gl);
	}
	
	public int getHeightMap() { return heightMap; }
	
	public void setSpread(float spread) { this.spread = spread; }
	
	public void setOrigin(float[] origin) { this.origin = origin; }
	
	public void createBuffers(GL2 gl)
	{
		vBuffer = FloatBuffer.allocate(MODEL_DATA.length);
		vBuffer.put(MODEL_DATA);
		vBuffer.position(0);
	}
	
	public void createTexture(GL2 gl)
	{
		int[] id = new int[1];
		gl.glGenTextures(1, id, 0);
		heightMap = id[0];

		perturbMap = TextureLoader.load(gl, "tex/blast_noise.png");

		gl.glActiveTexture(GL2.GL_TEXTURE1);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, heightMap);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		
		gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA32F, length, length, 0, GL2.GL_RGBA, GL2.GL_FLOAT, null);
		
		gl.glActiveTexture(GL2.GL_TEXTURE0);
	}
	
	public void updateHeights(GL2 gl)
	{
		gl.glActiveTexture(GL2.GL_TEXTURE1);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, heightMap);
		
		FloatBuffer heights = FloatBuffer.allocate(length * length * 4);
		float[] position = Arrays.copyOf(origin, 3);
		
		for(int i = 0; i < length; i++)
		{
			for(int j = 0; j < length; j++)
			{
				Quadtree cell = surface != null ? surface.getCell(position, Quadtree.MAXIMUM_LOD) : null;
				float h = (cell != null ? cell.getHeight(position) : 0);
				heights.put(new float[] {h, h, h, h});
				
				position[0] += spread;
			}
			
			position[0]  = origin[0]; 
			position[2] += spread;
		}
		heights.position(0);
		
		gl.glTexSubImage2D(GL2.GL_TEXTURE_2D, 0, 0, 0, length, length, GL2.GL_RGBA, GL2.GL_FLOAT, heights);
		
		gl.glActiveTexture(GL2.GL_TEXTURE0);
	}
	
	public void render(GL2 gl)
	{
		if(update)
		{
			updateHeights(gl);
			update = false;
		}
		
		timer += 0.005f;
		
		gl.glEnableClientState(GL_VERTEX_ARRAY);
		
		gl.glVertexPointer(2, GL2.GL_FLOAT, 0, vBuffer);
		
		Shader shader = Shader.get("grass");
		if(shader == null) return; else shader.enable(gl);
		
		gl.glActiveTexture(GL2.GL_TEXTURE1); gl.glEnable(GL2.GL_TEXTURE_2D); gl.glBindTexture(GL2.GL_TEXTURE_2D, heightMap);
		gl.glActiveTexture(GL2.GL_TEXTURE0); perturbMap.bind(gl);
		
		shader.setSampler(gl, "perturbMap", 0);
		shader.setSampler(gl, "heightMap" , 1);
		
		shader.setUniform(gl, "length", length);
		shader.setUniform(gl, "spread", spread);
		shader.setUniform(gl, "origin", origin);
		shader.setUniform(gl,  "timer",  timer);
		
		gl.glDrawArraysInstanced(GL2.GL_TRIANGLE_STRIP, 0, 6, length * length);
		
		Shader.disable(gl);
		
		gl.glDisableClientState(GL_VERTEX_ARRAY);
		
		gl.glActiveTexture(GL2.GL_TEXTURE1); gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glActiveTexture(GL2.GL_TEXTURE0);
	}
}
