package bates.jamie.graphics.entity;

import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.media.opengl.GL2;

import bates.jamie.graphics.util.Shader;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.Vector;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class GrassPatch
{
	public static final String TEXTURE_DIRECTORY = "tex/foliage/";
	
	private static Texture[] textures;
	
	static
	{
		try
		{
			textures = new Texture[4];
			
			textures[0] = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "plant1.png"), true);
			textures[1] = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "plant2.png"), true);
			textures[2] = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "plant3.png"), true);
			textures[3] = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "plant4.png"), true);
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	private Texture texture;
	private int positionMap;
	
	private Quadtree surface;
	
	private float origin[];
	private int   length;
	private float spread;
	
	private float timer = 0.0f;
	
	private FloatBuffer vBuffer;
	
	public boolean update = false;
	
	private static final float[] MODEL_DATA =
	{
		-.5f, 0, 0, +.5f, 0, 0,
		+.5f, 1, 0, -.5f, 1, 0,
		
		0, 0, +.5f, 0, 0, -.5f,
		0, 1, -.5f, 0, 1, +.5f
	};
	
	public GrassPatch(GL2 gl, Quadtree surface, float[] origin, int length, float spread)
	{
		this.surface = surface;
		
		this.origin = origin;
		this.length = length;
		this.spread = spread;
		
		Random generator = new Random();
		
//		this.texture = textures[generator.nextInt(textures.length)];
		this.texture = textures[3];
		
		createBuffers(gl);
		createTexture(gl);
		updateHeights(gl);
	}
	
	public int getPositionMap() { return positionMap; }
	
	public void setSpread(float spread) { this.spread = spread; }
	
	public void setOrigin(float[] origin) { this.origin = origin; }
	
	public void createBuffers(GL2 gl)
	{
		vBuffer = FloatBuffer.allocate(MODEL_DATA.length);
		vBuffer.put(Vector.multiply(MODEL_DATA, 6));
		vBuffer.position(0);
	}
	
	public void createTexture(GL2 gl)
	{
		int[] id = new int[1];
		gl.glGenTextures(1, id, 0);
		positionMap = id[0];

		gl.glActiveTexture(GL2.GL_TEXTURE1);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, positionMap);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
		
		gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA32F, length, length, 0, GL2.GL_RGBA, GL2.GL_FLOAT, null);
		
		gl.glActiveTexture(GL2.GL_TEXTURE0);
	}
	
	public void updateHeights(GL2 gl)
	{
		gl.glActiveTexture(GL2.GL_TEXTURE1);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, positionMap);
		
		FloatBuffer positions = FloatBuffer.allocate(length * length * 4);
		
		Random generator = new Random();
		
		for(int i = 0; i < length; i++)
		{
			for(int j = 0; j < length; j++)
			{
				Vec3 position = new Vec3(origin);
	    		
	    		double incline = Math.toRadians(generator.nextInt(3600) / 10.0);
	    		double azimuth = Math.toRadians(generator.nextInt(3600) / 10.0);
	    		
	    		position.x += (float) (Math.sin(incline) * Math.cos(azimuth) * spread);
	    		position.z += (float) (Math.sin(incline) * Math.sin(azimuth) * spread);

				Quadtree cell = surface != null ? surface.getCell(position.toArray(), Quadtree.MAXIMUM_LOD) : null;
				position.y = (cell != null ? cell.getHeight(position.toArray()) : 0) - 0.5f;
				
				float yRotation = (float) (generator.nextFloat() * 2 * Math.PI);
				
				positions.put(position.toArray());
				positions.put(yRotation); // fill alpha channel with rotation data
			}
		}
		positions.position(0);
		
		gl.glTexSubImage2D(GL2.GL_TEXTURE_2D, 0, 0, 0, length, length, GL2.GL_RGBA, GL2.GL_FLOAT, positions);
		
		gl.glActiveTexture(GL2.GL_TEXTURE0);
	}
	
	public void render(GL2 gl)
	{
		if(update)
		{
			updateHeights(gl);
			update = false;
		}
		
		timer += 0.015f;
		
		gl.glEnableClientState(GL_VERTEX_ARRAY);
		
		gl.glVertexPointer(3, GL2.GL_FLOAT, 0, vBuffer);
		
		Shader shader = Shader.get("grass");
		if(shader == null) return; else shader.enable(gl);
		
		gl.glActiveTexture(GL2.GL_TEXTURE1); gl.glBindTexture(GL2.GL_TEXTURE_2D, positionMap);
		gl.glActiveTexture(GL2.GL_TEXTURE0); texture.bind(gl);
		
		shader.setSampler(gl, "texture", 0);
		shader.setSampler(gl, "positionMap", 1);
		
		shader.setUniform(gl, "length", length);
		shader.setUniform(gl,  "timer",  timer);
		
//		gl.glDrawArraysInstanced(GL2.GL_TRIANGLE_STRIP, 0, 6, length * length);
		gl.glDrawArraysInstanced(GL2.GL_QUADS, 0, 8, length * length);
		
		Shader.disable(gl);
		
		gl.glDisableClientState(GL_VERTEX_ARRAY);
	}
}
