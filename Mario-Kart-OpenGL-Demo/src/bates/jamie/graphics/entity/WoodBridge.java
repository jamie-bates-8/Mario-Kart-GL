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
import bates.jamie.graphics.util.Renderer;
import bates.jamie.graphics.util.TextureLoader;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.shader.Shader;

import com.jogamp.opengl.util.texture.Texture;

public class WoodBridge
{
	static Model large_bridge_model = new Model("large_wooden_bridge");
	static Model small_bridge_model = new Model("small_wooden_bridge");
	
	static Model plank_model   = new Model("plank_2");
	static Model post_model    = new Model("wooden_post");
	static Model support_model = new Model("bridge_support");
	
//	static Model large_bridge_model = OBJParser.parseTexturedTriangleMesh("large_wooden_bridge");
//	static Model small_bridge_model = OBJParser.parseTexturedTriangleMesh("small_wooden_bridge");
//	
//	static Model plank_model   = OBJParser.parseTexturedTriangleMesh("plank_2");
//	static Model post_model    = OBJParser.parseTexturedTriangleMesh("wooden_post");
//	static Model support_model = OBJParser.parseTexturedTriangleMesh("bridge_support");
	
	static Material polished_wood;
	static Material painted_wood;
	
	SceneNode large_bridge_node;
	SceneNode small_bridge_node;
	
	float rotation = 0;
	Vec3  position;
	
	boolean use_large_model;
	
	
	public WoodBridge(GL2 gl, Vec3 p, float r, boolean large, boolean painted)
	{
		use_large_model = large;
		
		position = p;
		rotation = r;
		
		if(polished_wood == null)
		{
			Texture diffuse  = TextureLoader.load(gl, "tex/plank_COLOR.jpg");
			Texture normal   = TextureLoader.load(gl, "tex/plank_NRM.jpg");
			Texture	height   = TextureLoader.load(gl, "tex/plank_DISP.jpg");
			Texture specular = TextureLoader.load(gl, "tex/plank_SPEC.jpg");
			
			Texture diffuse_alt = TextureLoader.load(gl, "tex/plank_alt_COLOR.jpg");
			
			if(!plank_model.hasTangentData()) plank_model.calculateTangents();
			if(!post_model.hasTangentData()) post_model.calculateTangents();
			if(!support_model.hasTangentData()) support_model.calculateTangents();
			
			polished_wood = new Material(diffuse, normal, height, specular);
			 painted_wood = new Material(diffuse_alt, normal, height, specular);
			
			setupInstances();
		}
		
		large_bridge_node = new SceneNode(large_bridge_model);
		large_bridge_node.setMaterial(painted ? painted_wood : polished_wood);
		large_bridge_node.setTranslation(p);
		large_bridge_node.setScale(new Vec3(15));
		large_bridge_node.setRotation(new Vec3(0, r, 0));
		large_bridge_node.setRenderMode(RenderMode.BUMP_TEXTURE);
		large_bridge_node.useParallax(true);
		
		small_bridge_node = new SceneNode(small_bridge_model);
		small_bridge_node.setMaterial(painted ? painted_wood : polished_wood);
		small_bridge_node.setTranslation(p);
		small_bridge_node.setScale(new Vec3(15));
		small_bridge_node.setRotation(new Vec3(0, r, 0));
		small_bridge_node.setRenderMode(RenderMode.BUMP_TEXTURE);
		small_bridge_node.useParallax(true);
	}
	
	private void setupInstances()
	{
		FloatBuffer plank_positions = setupPlankPositions();
		FloatBuffer post_positions = setupPostPositions();
		FloatBuffer support_positions = setupSupportPositions();
		
		FloatBuffer plank_matrices = setupPlankMatrices();
		FloatBuffer support_matrices = setupSupportMatrices();
		
		plank_model.setPositionData(plank_positions);
		plank_model.setMatrixData(plank_matrices);
		
		plank_model.matrixDivisor = 160;
		
		post_model.setPositionData(post_positions);
		post_model.setMatrixData(plank_matrices);
		
		post_model.matrixDivisor = 48;
		
		support_model.setPositionData(support_positions);
		support_model.setMatrixData(support_matrices);
		
		support_model.matrixDivisor = 8;
	}	
	
	private FloatBuffer setupPlankPositions()
	{
		int num_of_planks = 192;
		
		FloatBuffer positions = FloatBuffer.allocate(num_of_planks * 4);
		
		Vec3 start = new Vec3(0, 37.5, 101.25).add(new Vec3(-47.8125, 21.9375, 0));
		Vec3 scale = new Vec3(-1, +1, -1);
		
		for(int i = 0; i < 16; i++)
		{
			start = start.add(new Vec3(5.625, 0, 0));
			positions.put(start.toArray()); positions.put(0);
			positions.put(start.multiply(scale).toArray()); positions.put(1);
			positions.put(start.add(new Vec3(0, -7.3125, 0)).toArray()); positions.put(0);
			positions.put(start.add(new Vec3(0, -7.3125, 0)).multiply(scale).toArray()); positions.put(1);
		}
		
		start = new Vec3(90, 7.5, 33.75).add(new Vec3(-47.8125, 21.9375, 0));
		
		for(int i = 0; i < 16; i++)
		{
			start = start.add(new Vec3(5.625, 0, 0));
			positions.put(start.toArray()); positions.put(0);
			positions.put(start.multiply(scale).toArray()); positions.put(1);
			positions.put(start.add(new Vec3(0, -7.3125, 0)).toArray()); positions.put(0);
			positions.put(start.add(new Vec3(0, -7.3125, 0)).multiply(scale).toArray()); positions.put(1);
		}
		
		start = new Vec3(0, 30, 56.25).add(new Vec3(-25.3125, -0.5625, 0));
		
		for(int i = 0; i < 8; i++)
		{
			start = start.add(new Vec3(5.625, 0, 0));
			positions.put(start.toArray()); positions.put(0);
			positions.put(start.multiply(scale).toArray()); positions.put(1);
			positions.put(start.add(new Vec3(0, -4.5, 0)).toArray()); positions.put(0);
			positions.put(start.add(new Vec3(0, -4.5, 0)).multiply(scale).toArray()); positions.put(1);
		}
		
		start = new Vec3(0, 37.5, 101.25).add(new Vec3(-33.75, 18.27375, 10.125));
		
		for(int i = 0; i < 4; i++)
		{
			positions.put(start.toArray()); positions.put(0);
			positions.put(start.multiply(scale).toArray()); positions.put(1);
			positions.put(start.add(new Vec3(0, 0, -20.25)).toArray()); positions.put(0);
			positions.put(start.add(new Vec3(0, 0, -20.25)).multiply(scale).toArray()); positions.put(1);
			start = start.add(new Vec3(22.5, 0, 0));
		}
		
		start = new Vec3(90, 7.5, 33.75).add(new Vec3(-33.75, 18.27375, 10.125));
		
		for(int i = 0; i < 4; i++)
		{
			positions.put(start.toArray()); positions.put(0);
			positions.put(start.multiply(scale).toArray()); positions.put(1);
			positions.put(start.add(new Vec3(0, 0, -20.25)).toArray()); positions.put(0);
			positions.put(start.add(new Vec3(0, 0, -20.25)).multiply(scale).toArray()); positions.put(1);
			start = start.add(new Vec3(22.5, 0, 0));
		}
		positions.position(0);
		
		return positions;
	}
	
	private FloatBuffer setupPostPositions()
	{
		int num_of_posts = 48;
		
		FloatBuffer positions = FloatBuffer.allocate(num_of_posts * 4);
		
		Vec3 start = new Vec3(0, 37.5, 101.25).add(new Vec3(-33.75, 21.09375, 10.125));
		Vec3 scale = new Vec3(-1, +1, -1);
		
		for(int i = 0; i < 2; i++)
		{
			start = start.add(new Vec3(22.5, 0, 0));
			
			Vec3 p0 = new Vec3(start);
			Vec3 p1 = start.add(new Vec3(0, -5.625, 0));
			Vec3 p2 = start.add(new Vec3(0, -5.625, -20.25));
			Vec3 p3 = start.add(new Vec3(0, 0, -20.25));
			
			positions.put(p0.toArray()); positions.put(0);
			positions.put(p0.multiply(scale).toArray()); positions.put(1);
			positions.put(p1.toArray()); positions.put(0);
			positions.put(p1.multiply(scale).toArray()); positions.put(1);
			positions.put(p2.toArray()); positions.put(0);
			positions.put(p2.multiply(scale).toArray()); positions.put(1);
			positions.put(p3.toArray()); positions.put(0);
			positions.put(p3.multiply(scale).toArray()); positions.put(1);	
		}
		
		start = new Vec3(90, 7.5, 33.75).add(new Vec3(-33.75, 21.09375, 10.125));
		
		for(int i = 0; i < 2; i++)
		{
			start = start.add(new Vec3(22.5, 0, 0));
			
			Vec3 p0 = new Vec3(start);
			Vec3 p1 = start.add(new Vec3(0, -5.625, 0));
			Vec3 p2 = start.add(new Vec3(0, -5.625, -20.25));
			Vec3 p3 = start.add(new Vec3(0, 0, -20.25));
			
			positions.put(p0.toArray()); positions.put(0);
			positions.put(p0.multiply(scale).toArray()); positions.put(1);
			positions.put(p1.toArray()); positions.put(0);
			positions.put(p1.multiply(scale).toArray()); positions.put(1);
			positions.put(p2.toArray()); positions.put(0);
			positions.put(p2.multiply(scale).toArray()); positions.put(1);
			positions.put(p3.toArray()); positions.put(0);
			positions.put(p3.multiply(scale).toArray()); positions.put(1);	
		}
		
		start = new Vec3(0, 30, 56.25).add(new Vec3(-33.75, -1.40625, 10.125));
		
		for(int i = 0; i < 2; i++)
		{
			start = start.add(new Vec3(22.5, 0, 0));
			
			Vec3 p0 = new Vec3(start);
			Vec3 p1 = start.add(new Vec3(0, -2.8125, 0));
			Vec3 p2 = start.add(new Vec3(0, -2.8125, -20.25));
			Vec3 p3 = start.add(new Vec3(0, 0, -20.25));
			
			positions.put(p0.toArray()); positions.put(0);
			positions.put(p0.multiply(scale).toArray()); positions.put(1);
			positions.put(p1.toArray()); positions.put(0);
			positions.put(p1.multiply(scale).toArray()); positions.put(1);
			positions.put(p2.toArray()); positions.put(0);
			positions.put(p2.multiply(scale).toArray()); positions.put(1);
			positions.put(p3.toArray()); positions.put(0);
			positions.put(p3.multiply(scale).toArray()); positions.put(1);
		}
		positions.position(0);
		
		return positions;
	}
	
	private FloatBuffer setupSupportPositions()
	{
		int num_of_supports = 16;
		
		FloatBuffer positions = FloatBuffer.allocate(num_of_supports * 4);
		
		Vec3 scale = new Vec3(-1, +1, -1);
		
		Vec3 p0 = new Vec3(0, 37.5, 101.25).add(new Vec3(-33.75, 22.5, 10.125));
		Vec3 p1 = new Vec3(90, 7.5, 33.75).add(new Vec3(-33.75, 22.5, 10.125));
		
		positions.put(p0.toArray()); positions.put(0);
		positions.put(p0.add(new Vec3(0, 0, -20.25)).toArray()); positions.put(0);
		positions.put(p1.toArray()); positions.put(0);
		positions.put(p1.add(new Vec3(0, 0, -20.25)).toArray()); positions.put(0);
		
		positions.put(p0.add(new Vec3(67.5, 0, -20.25)).multiply(scale).toArray()); positions.put(1);
		positions.put(p0.add(new Vec3(67.5, 0, 0)).multiply(scale).toArray()); positions.put(1);
		positions.put(p1.add(new Vec3(67.5, 0, -20.25)).multiply(scale).toArray()); positions.put(1);
		positions.put(p1.add(new Vec3(67.5, 0, 0)).multiply(scale).toArray()); positions.put(1);
		
		positions.put(p0.add(new Vec3(67.5, 0, -20.25)).toArray()); positions.put(0);
		positions.put(p0.add(new Vec3(67.5, 0, 0)).toArray()); positions.put(0);
		positions.put(p1.add(new Vec3(67.5, 0, -20.25)).toArray()); positions.put(0);
		positions.put(p1.add(new Vec3(67.5, 0, 0)).toArray()); positions.put(0);
			
		positions.put(p0.multiply(scale).toArray()); positions.put(1);
		positions.put(p0.add(new Vec3(0, 0, -20.25)).multiply(scale).toArray()); positions.put(1);
		positions.put(p1.multiply(scale).toArray()); positions.put(1);
		positions.put(p1.add(new Vec3(0, 0, -20.25)).multiply(scale).toArray()); positions.put(1);
		
		
		
		positions.position(0);
		
		return positions;
	}
	
	private FloatBuffer setupPlankMatrices()
	{
		FloatBuffer matrices = FloatBuffer.allocate(16 * 2);
		
		Vec3 scale = new Vec3(2.8125, 2.8125, 2.25);
		
		float[] model = Arrays.copyOf(Matrix.IDENTITY_MATRIX_16, 16);
		Matrix.scale(model, scale.x, scale.y, scale.z);
		Matrix.multiply(model, model, Matrix.getRotationMatrix(Matrix.getRotationMatrix(0, 90, 0)));
		matrices.put(model);
		
		model = Arrays.copyOf(Matrix.IDENTITY_MATRIX_16, 16);
		Matrix.scale(model, scale.x, scale.y, scale.z);
		Matrix.multiply(model, model, Matrix.getRotationMatrix(Matrix.getRotationMatrix(90, 90, 90)));
		matrices.put(model);
		
		matrices.position(0);
		
		return matrices;
	}
	
	private FloatBuffer setupSupportMatrices()
	{
		FloatBuffer matrices = FloatBuffer.allocate(16 * 2);
		
		Vec3 scale = new Vec3(2.8125, 2.8125, 2.25);
		
		float[] model = Arrays.copyOf(Matrix.IDENTITY_MATRIX_16, 16);
		Matrix.scale(model, scale.x, scale.y, scale.z);
		Matrix.multiply(model, model, Matrix.getRotationMatrix(Matrix.getRotationMatrix(0, 270, 0)));
		matrices.put(model);
		
		model = Arrays.copyOf(Matrix.IDENTITY_MATRIX_16, 16);
		Matrix.scale(model, scale.x, scale.y, scale.z);
		Matrix.multiply(model, model, Matrix.getRotationMatrix(Matrix.getRotationMatrix(0, 90, 0)));
		matrices.put(model);
		
		matrices.position(0);
		
		return matrices;
	}
	
	public Vec3 getPosition() { return position; }
	
	public void render(GL2 gl)
	{	
		if(use_large_model) large_bridge_node.render(gl);
		else small_bridge_node.render(gl);
	}
	
	public static void renderSimplified(GL2 gl)
	{
		polished_wood.load(gl);
		
		Renderer.instanced_mode = true;
		Renderer.instanced_matrix_mode = true;
		
		Shader shader = Shader.get("bump_mat_inst");
		if(shader != null) shader.enable(gl);
		
		shader.setSampler(gl, "texture"  , 0);
		shader.setSampler(gl, "bumpmap"  , 1);
		shader.setSampler(gl, "heightmap", 2);
		
		shader.setSampler(gl, "diffuse_alt", 4);
		
		gl.glActiveTexture(GL2.GL_TEXTURE4);
		painted_wood.getDiffuseMap().bind(gl);
		gl.glActiveTexture(GL2.GL_TEXTURE0);
		
		if(Scene.enableShadow && shader != null)
		{
			shader.setSampler(gl, "shadowMap", ShadowCaster.SHADOW_MAP_TEXTURE_UNIT);

			shader.setUniform(gl, "enableShadow", true);
			shader.setUniform(gl, "sampleMode", ShadowCaster.sampleMode.ordinal());
			shader.setUniform(gl, "texScale", new float[] {1.0f / (Scene.canvasWidth * 12), 1.0f / (Scene.canvasHeight * 12)});
		}
		else if(shader != null) shader.setUniform(gl, "enableShadow", false);
		
		if(Scene.enable_culling && !Scene.shadowMode && !Scene.reflectMode) gl.glEnable(GL2.GL_CULL_FACE);
		
		plank_model.renderInstanced(gl, 192);
		post_model.renderInstanced(gl, 48);
		support_model.renderInstanced(gl, 16);
		
		gl.glDisable(GL2.GL_CULL_FACE);
		
		Shader.disable(gl);
		
		Renderer.instanced_mode = false;
		Renderer.instanced_matrix_mode = false;
	}
}
