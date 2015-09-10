package bates.jamie.graphics.entity;

import java.nio.FloatBuffer;
import java.util.Arrays;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.Material;
import bates.jamie.graphics.scene.Model;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.scene.SceneNode;
import bates.jamie.graphics.scene.SceneNode.RenderMode;
import bates.jamie.graphics.scene.process.ShadowCaster;
import bates.jamie.graphics.util.Matrix;
import bates.jamie.graphics.util.OBJParser;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.Renderer;
import bates.jamie.graphics.util.TextureLoader;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.shader.Shader;

public class GiftBox
{
	public static Model box_model = OBJParser.parseTexturedTriangleMesh("present_1");
	
	private SceneNode gift_box_node;
	
	private static Material pink_glitter_mat;
	private static Material blue_glitter_mat;
	private static Material green_stripe_mat;
	
	private static boolean initialized = false;
	
	
	public enum BoxType
	{
		BLUE_GLITTER,
		PINK_GLITTER,
		GREEN_STRIPE;
	}
	
	
	public GiftBox(GL2 gl, Vec3 position, Vec3 rotation, BoxType type)
	{
		if(!initialized) initialize(gl);
		
		gift_box_node = new SceneNode(box_model);
		gift_box_node.setTranslation(position);
		gift_box_node.setRotation(rotation);
		gift_box_node.setScale(new Vec3(7.5));
		gift_box_node.setRenderMode(RenderMode.TEXTURE_SPEC);
		
		switch(type)
		{
			case BLUE_GLITTER : gift_box_node.setMaterial(blue_glitter_mat); break;
			case PINK_GLITTER : gift_box_node.setMaterial(pink_glitter_mat); break;
			case GREEN_STRIPE : gift_box_node.setMaterial(green_stripe_mat); break;
		}
	}

	private static void initialize(GL2 gl)
	{	
		pink_glitter_mat = new Material();
		pink_glitter_mat.setSpecular(RGB.WHITE);
		pink_glitter_mat.setDiffuseMap(TextureLoader.load(gl, "tex/gift_wrap_1.png"));
		pink_glitter_mat.setSpecularMap(TextureLoader.load(gl, "tex/gift_wrap_1_SPEC.png"));
		
		blue_glitter_mat = new Material();
		blue_glitter_mat.setSpecular(RGB.WHITE);
		blue_glitter_mat.setDiffuseMap(TextureLoader.load(gl, "tex/gift_wrap_2.png"));
		blue_glitter_mat.setSpecularMap(TextureLoader.load(gl, "tex/gift_wrap_2_SPEC.png"));
		
		green_stripe_mat = new Material();
		green_stripe_mat.setSpecular(RGB.WHITE);
		green_stripe_mat.setDiffuseMap(TextureLoader.load(gl, "tex/gift_wrap_3.png"));
		green_stripe_mat.setSpecularMap(TextureLoader.load(gl, "tex/gift_wrap_3_SPEC.png"));
		
		setupInstances();
		
		initialized = true;
	}
	
	private static FloatBuffer setupPositions()
	{
		int num_of_cookies = 14;
		
		FloatBuffer positions = FloatBuffer.allocate(num_of_cookies * 4);
		
		for(int i = 0; i < 7; i++)
		{
			positions.put(new float[] {33.75f, 2.25f + i * 4.3f,  -33.75f, 0});
			positions.put(new float[] {33.75f, 2.25f + i * 4.3f, -146.25f, 0});
		}
		positions.position(0);
		
		return positions;
	}
	
	private static FloatBuffer setupMatrices()
	{
		FloatBuffer matrices = FloatBuffer.allocate(16 * 7);
		
		Vec3 scale = new Vec3(4.5);
		
		for(int i = 0; i < 7; i++)
		{
			float[] model = Arrays.copyOf(Matrix.IDENTITY_MATRIX_16, 16);
			Matrix.scale(model, scale.x, scale.y, scale.z);
			Matrix.rotate(model, new Vec3(180, 90 * i, 0));
			matrices.put(model);
		}
		matrices.position(0);
		
		return matrices;
	}
	
	private static void setupInstances()
	{
		FloatBuffer positions = setupPositions();
		FloatBuffer matrices  = setupMatrices();
		
		box_model.setPositionData(positions);
		box_model.setMatrixData(matrices);
		box_model.matrixDivisor = 2;
	}
	
	public void render(GL2 gl)
	{
		gift_box_node.render(gl);
	}
	
	public static void renderSimplified(GL2 gl)
	{
		if(!initialized) initialize(gl);
		
		Renderer.instanced_mode = true;
		Renderer.instanced_matrix_mode = true;
		
		if(Scene.enable_culling && !Scene.shadowMode && !Scene.reflectMode) gl.glEnable(GL2.GL_CULL_FACE);
		
		Shader shader = Shader.get("texture_specular");
		if(shader != null) shader.enable(gl);
		
		shader.setSampler(gl, "texture", 0);
		shader.setSampler(gl, "specular_map", 4);
		
		if(Scene.enableShadow && shader != null)
		{
			shader.setSampler(gl, "shadowMap", ShadowCaster.SHADOW_MAP_TEXTURE_UNIT);

			shader.setUniform(gl, "enableShadow", true);
			shader.setUniform(gl, "sampleMode", ShadowCaster.sampleMode.ordinal());
			shader.setUniform(gl, "texScale", new float[] {1.0f / (Scene.canvasWidth * 12), 1.0f / (Scene.canvasHeight * 12)});
		}
		else if(shader != null) shader.setUniform(gl, "enableShadow", false);
		
		pink_glitter_mat.load(gl);
		box_model.renderInstanced(gl, 14);
		Material.loadDefault(gl);
		
		Shader.disable(gl);
		
		gl.glDisable(GL2.GL_CULL_FACE);
		
		Renderer.instanced_mode = false;
		Renderer.instanced_matrix_mode = false;
	}
}
