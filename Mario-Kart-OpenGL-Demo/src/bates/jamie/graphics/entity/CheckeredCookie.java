package bates.jamie.graphics.entity;

import java.nio.FloatBuffer;
import java.util.ArrayList;
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
import bates.jamie.graphics.util.Vec2;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.shader.Shader;
import bates.jamie.graphics.util.shader.Uniform;

import com.jogamp.opengl.util.texture.Texture;

public class CheckeredCookie
{
	public static Model cookie_model = OBJParser.parseTexturedTriangleMesh("cookie_block", new Vec2(3));
	
	private SceneNode cookie_node;
	
	private static Texture cookie_normal_map;
	private static Texture cookie_specular_map;
	
	private static Material cookie_mat;
	private static boolean initialized = false;
	
	
	public CheckeredCookie(GL2 gl, Vec3 position, Vec3 rotation)
	{
		if(!initialized) initialize(gl);
		
		ArrayList<Uniform> uniforms = new ArrayList<Uniform>();
		
		uniforms.add(Uniform.getSampler("bumpmap", 1));
		uniforms.add(Uniform.getSampler("specular_map", 4));
		uniforms.add(Uniform.getUniform("enable_spec_map", true));
		uniforms.add(Uniform.getUniform("enableParallax", false));
		uniforms.add(Uniform.getUniform("primary_color", new Vec3(0.891f, 0.565f, 0.280f).correctGamma(2.7)));
		uniforms.add(Uniform.getUniform("secondary_color", new Vec3(0.064f, 0.018f, 0.006f).correctGamma(2.7)));
		
		cookie_node = new SceneNode(cookie_model);
		cookie_node.setTranslation(position);
		cookie_node.setRotation(rotation);
		cookie_node.setScale(new Vec3(4.5));
		cookie_node.setRenderMode(RenderMode.COLOR);
		cookie_node.setShader(Shader.get("checker_cookie"));
		cookie_node.setUniforms(uniforms);
		cookie_node.setMaterial(cookie_mat);
	}

	private static void initialize(GL2 gl)
	{
		cookie_normal_map   = TextureLoader.load(gl, "tex/cookie_NRM.png");
		cookie_specular_map = TextureLoader.load(gl, "tex/cookie_SPEC.tga");
		
		cookie_mat = new Material();
		cookie_mat.setSpecular(RGB.DARK_GRAY);
		cookie_mat.setNormalMap(cookie_normal_map);
		cookie_mat.setSpecularMap(cookie_specular_map);
		
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
		
		cookie_model.setPositionData(positions);
		cookie_model.setMatrixData(matrices);
		cookie_model.matrixDivisor = 2;
	}
	
	public void render(GL2 gl)
	{
		cookie_node.render(gl);
	}
	
	public static void renderSimplified(GL2 gl)
	{
		if(!initialized) initialize(gl);
		
		Renderer.instanced_mode = true;
		Renderer.instanced_matrix_mode = true;
		
		if(Scene.enable_culling && !Scene.shadowMode && !Scene.reflectMode) gl.glEnable(GL2.GL_CULL_FACE);
		
		Shader shader = Shader.get("cookie_instance");
		if(shader != null) shader.enable(gl);
		
		ArrayList<Uniform> uniforms = new ArrayList<Uniform>();
		
		uniforms.add(Uniform.getSampler("bumpmap", 1));
		uniforms.add(Uniform.getSampler("specular_map", 4));
		uniforms.add(Uniform.getUniform("enable_spec_map", true));
		uniforms.add(Uniform.getUniform("enableParallax", false));
		uniforms.add(Uniform.getUniform("primary_color", new Vec3(0.891f, 0.565f, 0.280f).correctGamma(2.7)));
		uniforms.add(Uniform.getUniform("secondary_color", new Vec3(0.064f, 0.018f, 0.006f).correctGamma(2.7)));
		
		for(Uniform uniform : uniforms) shader.setUniform(gl, uniform);
		
		if(Scene.enableShadow && shader != null)
		{
			shader.setSampler(gl, "shadowMap", ShadowCaster.SHADOW_MAP_TEXTURE_UNIT);

			shader.setUniform(gl, "enableShadow", true);
			shader.setUniform(gl, "sampleMode", ShadowCaster.sampleMode.ordinal());
			shader.setUniform(gl, "texScale", new float[] {1.0f / (Scene.canvasWidth * 12), 1.0f / (Scene.canvasHeight * 12)});
		}
		else if(shader != null) shader.setUniform(gl, "enableShadow", false);
		
		cookie_mat.load(gl);
		cookie_model.renderInstanced(gl, 14);
		Material.loadDefault(gl);
		
		Shader.disable(gl);
		
		gl.glDisable(GL2.GL_CULL_FACE);
		
		Renderer.instanced_mode = false;
		Renderer.instanced_matrix_mode = false;
	}
}
