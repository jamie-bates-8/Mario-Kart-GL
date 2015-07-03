package bates.jamie.graphics.entity;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.Model;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.scene.SceneNode;
import bates.jamie.graphics.scene.SceneNode.RenderMode;
import bates.jamie.graphics.scene.process.ShadowCaster;
import bates.jamie.graphics.util.Matrix;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.Renderer;
import bates.jamie.graphics.util.TextureLoader;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.shader.Shader;
import bates.jamie.graphics.util.shader.Uniform;

import com.jogamp.opengl.util.texture.Texture;

public class CheckeredBlock
{
	public static Model bevelled_cube_model  = new Model("checker_block");
	public static Model bevelled_slope_model = new Model("sloped_block");
//	public static Model bevelled_cube_model  = OBJParser.parseTexturedTriangleMesh("checker_block");
//	public static Model bevelled_slope_model = OBJParser.parseTriangleMesh("sloped_block");
	public static Model block_bolts_model = new Model("block_bolts");
	public static Model wedge_block_model = new Model("wedge_block");
	
	private static Texture slope_pattern_mask;
	
	private static float[] block_colors;
	
	public static final float[] BLOCK_RED    = new float[] {0.800f, 0.134f, 0.155f};
	public static final float[] BLOCK_BLUE   = RGB.SKY_BLUE;
	public static final float[] BLOCK_GREEN  = new float[] {0.257f, 0.800f, 0.243f};
	public static final float[] BLOCK_YELLOW = new float[] {0.800f, 0.730f, 0.180f};
	
	public static final float[] BLOCK_CYAN    = new float[] {0.400f, 0.800f, 0.800f}; // alternative GREEN
	public static final float[] BLOCK_LILAC   = new float[] {0.800f, 0.600f, 1.000f}; // alternative BLUE
	public static final float[] BLOCK_ORANGE  = new float[] {1.000f, 0.600f, 0.400f}; // alternative YELLOW
	public static final float[] BLOCK_MAGENTA = new float[] {1.000f, 0.400f, 0.600f}; // alternative RED
	
	List<Uniform> uniforms;
	
	private static SceneNode block_bolt_node;
	private SceneNode block_node;
	
	
	
	public CheckeredBlock(GL2 gl, BlockType type, float[] color, Vec3 position, Vec3 rotation)
	{
		if(block_bolt_node == null)
		{
			block_bolt_node = new SceneNode(block_bolts_model);
			block_bolt_node.setRenderMode(RenderMode.COLOR);
			block_bolt_node.setColor(RGB.WHITE);
			
			slope_pattern_mask = TextureLoader.load(gl, "tex/slope_mask.jpg");
			
			setupInstances();
			
			block_colors = new float[]
			{
				BLOCK_RED[0], BLOCK_RED[1], BLOCK_RED[2], 
				BLOCK_BLUE[0], BLOCK_BLUE[1], BLOCK_BLUE[2], 
				BLOCK_GREEN[0], BLOCK_GREEN[1], BLOCK_GREEN[2], 
				BLOCK_YELLOW[0], BLOCK_YELLOW[1], BLOCK_YELLOW[2],
				
				BLOCK_CYAN[0], BLOCK_CYAN[1], BLOCK_CYAN[2],
				BLOCK_LILAC[0], BLOCK_LILAC[1], BLOCK_LILAC[2],
				BLOCK_ORANGE[0], BLOCK_ORANGE[1], BLOCK_ORANGE[2],
				BLOCK_MAGENTA[0], BLOCK_MAGENTA[1], BLOCK_MAGENTA[2] 
			};
		}
		
		uniforms = new ArrayList<Uniform>();
		
		Vec3 scale = new Vec3(3, 4, 3);
		Vec3 texScale = scale.add(new Vec3(1));
		
		uniforms.add(Uniform.getUniform("scaleVec", texScale.normalizeScale()));
		uniforms.add(Uniform.getUniform("minScale", texScale.min()));
		uniforms.add(Uniform.getSampler("patternMask", 0));
		uniforms.add(Uniform.getUniform("antiAlias", true));
		
		switch(type)
		{
			case BLOCK:
			{
				block_node = new SceneNode(bevelled_cube_model);
				block_node.setRenderMode(RenderMode.COLOR);
				block_node.setUniforms(uniforms);
				block_node.setShader(Shader.get("checker_block"));
				block_node.addChild(block_bolt_node);
				
				break;
			}
			case SLOPE:
			{
				block_node = new SceneNode(bevelled_slope_model);
				block_node.setRenderMode(RenderMode.COLOR);
				block_node.getMaterial().setDiffuseMap(slope_pattern_mask);
				block_node.setUniforms(uniforms);
				block_node.setShader(Shader.get("checker_shadow"));
				
				break;
			}
			case WEDGE:
			{
				block_node = new SceneNode(wedge_block_model);
				block_node.setRenderMode(RenderMode.COLOR);
				
				break;
			}
		}
		
		block_node.setColor(color);
		block_node.setScale(new Vec3(15));
		block_node.setTranslation(position);
		block_node.setRotation(rotation);
		
	}
	
	private void setupInstances()
	{
		FloatBuffer block_positions = setupBlockPositions();
		
		FloatBuffer matrices = setupMatrices();
		
		bevelled_cube_model.setPositionData(block_positions);
		bevelled_cube_model.setMatrixData(matrices);
		bevelled_cube_model.matrixDivisor = 8;
		
		block_bolts_model.setPositionData(block_positions);
		block_bolts_model.setMatrixData(matrices);
		block_bolts_model.matrixDivisor = 8;
		
		bevelled_slope_model.setPositionData(setupSlopePositions());
		bevelled_slope_model.setMatrixData(matrices);
		bevelled_slope_model.matrixDivisor = 1;
		
		wedge_block_model.setPositionData(setupWedgePositions());
		wedge_block_model.setMatrixData(matrices);
		wedge_block_model.matrixDivisor = 1;
	}
	
	private FloatBuffer setupBlockPositions()
	{
		int num_of_blocks = 16;
		
		FloatBuffer positions = FloatBuffer.allocate(num_of_blocks * 4);
		
		positions.put(new float[] { 101.25f, 30,  56.25f, 3});
		positions.put(new float[] {-101.25f, 30, -56.25f, 6});
		
		positions.put(new float[] { 146.25f, 0,  33.75f, 0});
		positions.put(new float[] { 146.25f, 0,  56.25f, 1});
		positions.put(new float[] { 146.25f, 0,  78.75f, 3});
		
		positions.put(new float[] {-146.25f, 0, -33.75f, 7});
		positions.put(new float[] {-146.25f, 0, -56.25f, 5});
		positions.put(new float[] {-146.25f, 0, -78.75f, 6});
		
		
		positions.put(new float[] { 33.75f, 0,  33.75f, 2});
		positions.put(new float[] {-33.75f, 0, -33.75f, 4});
		
		positions.put(new float[] { 33.75f, 0,  146.25f, 0});
		positions.put(new float[] { 56.25f, 0,  146.25f, 3});
		positions.put(new float[] { 78.75f, 0,  146.25f, 1});
		
		positions.put(new float[] {-33.75f, 0, -146.25f, 7});
		positions.put(new float[] {-56.25f, 0, -146.25f, 6});
		positions.put(new float[] {-78.75f, 0, -146.25f, 5});
		
		positions.position(0);
		
		return positions;
	}
	
	private FloatBuffer setupSlopePositions()
	{
		int num_of_slopes = 6;
		
		FloatBuffer positions = FloatBuffer.allocate(num_of_slopes * 4);
		
		positions.put(new float[] { 78.75f, 30, 56.25f, 1});
		positions.put(new float[] {146.25f, 0, 101.25f, 0});
		positions.put(new float[] {101.25f, 0, 146.25f, 0});
		
		positions.put(new float[] { -78.75f, 30, -56.25f, 5});
		positions.put(new float[] {-146.25f, 0, -101.25f, 7});
		positions.put(new float[] {-101.25f, 0, -146.25f, 7});
		
		positions.position(0);
		
		return positions;
	}
	
	private FloatBuffer setupWedgePositions()
	{
		int num_of_wedges = 6;
		
		FloatBuffer positions = FloatBuffer.allocate(num_of_wedges * 4);
		
		positions.put(new float[] { 56.25f, 30, 56.25f, 0});
		positions.put(new float[] {146.25f, 0, 123.75f, 1});
		positions.put(new float[] {123.75f, 0, 146.25f, 3});
		
		positions.put(new float[] { -56.25f, 30, -56.25f, 7});
		positions.put(new float[] {-146.25f, 0, -123.75f, 5});
		positions.put(new float[] {-123.75f, 0, -146.25f, 6});
		
		positions.position(0);
		
		return positions;
	}
	
	private FloatBuffer setupMatrices()
	{
		FloatBuffer matrices = FloatBuffer.allocate(16 * 6);
		
		float[] model = Arrays.copyOf(Matrix.IDENTITY_MATRIX_16, 16);
		Matrix.scale(model, 15, 15, 15);
		matrices.put(model);
		
		Matrix.multiply(model, model, Matrix.getRotationMatrix(Matrix.getRotationMatrix(0, 90, 0)));
		matrices.put(model);
		
		model = Arrays.copyOf(Matrix.IDENTITY_MATRIX_16, 16);
		Matrix.scale(model, 15, 15, 15);
		Matrix.multiply(model, model, Matrix.getRotationMatrix(Matrix.getRotationMatrix(0, 180, 0)));
		matrices.put(model);
		
		model = Arrays.copyOf(Matrix.IDENTITY_MATRIX_16, 16);
		Matrix.scale(model, 15, 15, 15);
		Matrix.multiply(model, model, Matrix.getRotationMatrix(Matrix.getRotationMatrix(0, 180, 0)));
		matrices.put(model);
		
		model = Arrays.copyOf(Matrix.IDENTITY_MATRIX_16, 16);
		Matrix.scale(model, 15, 15, 15);
		Matrix.multiply(model, model, Matrix.getRotationMatrix(Matrix.getRotationMatrix(0, 270, 0)));
		matrices.put(model);
		
		model = Arrays.copyOf(Matrix.IDENTITY_MATRIX_16, 16);
		Matrix.scale(model, 15, 15, 15);
		matrices.put(model);
		
		matrices.position(0);
		
		return matrices;
	}
	
	public void render(GL2 gl)
	{
		block_node.render(gl);
	}
	
	public static void renderSimplified(GL2 gl)
	{
		Renderer.instanced_mode = true;
		Renderer.instanced_matrix_mode = true;
		
		Shader shader = Shader.get("checker_instance");
		if(shader != null) shader.enable(gl);
		setupShader(gl, shader);
		
		bevelled_cube_model.renderInstanced(gl, 16);
		
		shader = Shader.get("phong_mat_inst");
		if(shader != null) shader.enable(gl);
		
		block_bolts_model.renderInstanced(gl, 16);
		
		shader = Shader.get("slope_instance");
		if(shader != null) shader.enable(gl);
		setupShader(gl, shader);
		
		slope_pattern_mask.bind(gl);
		
		bevelled_slope_model.renderInstanced(gl, 6);
		wedge_block_model.renderInstanced(gl, 6);
		
		Shader.disable(gl);
		
		Renderer.instanced_mode = false;
		Renderer.instanced_matrix_mode = false;
	}

	private static void setupShader(GL2 gl, Shader shader)
	{
		Vec3 scale = new Vec3(3, 4, 3);
		Vec3 texScale = scale.add(new Vec3(1));
		
		shader.setUniform(gl, "scaleVec", texScale.normalizeScale());
		shader.setUniform(gl, "minScale", texScale.min());
		shader.setSampler(gl, "patternMask", 0);
		
		int offsetsLoc = -1;
		
		offsetsLoc = gl.glGetUniformLocation(shader.shaderID, "block_colors");
		gl.glUniform3fv(offsetsLoc, block_colors.length, block_colors, 0);
		
		if(Scene.enableShadow && shader != null)
		{
			shader.setSampler(gl, "shadowMap", ShadowCaster.SHADOW_MAP_TEXTURE_UNIT);

			shader.setUniform(gl, "enableShadow", true);
			shader.setUniform(gl, "sampleMode", ShadowCaster.sampleMode.ordinal());
			shader.setUniform(gl, "texScale", new float[] {1.0f / (Scene.canvasWidth * 12), 1.0f / (Scene.canvasHeight * 12)});
		}
		else if(shader != null) shader.setUniform(gl, "enableShadow", false);
		
		shader.setUniform(gl, "antiAlias", Scene.normalMode);
	}
	
	public enum BlockType
	{
		BLOCK,
		SLOPE,
		WEDGE;
	}
}
