package bates.jamie.graphics.entity;

import java.nio.FloatBuffer;
import java.util.Arrays;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.Material;
import bates.jamie.graphics.scene.Model;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.scene.SceneNode;
import bates.jamie.graphics.scene.SceneNode.RenderMode;
import bates.jamie.graphics.util.Matrix;
import bates.jamie.graphics.util.OBJParser;
import bates.jamie.graphics.util.PLYParser;
import bates.jamie.graphics.util.Renderer;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.shader.Shader;

public class DonutBlock
{
//	public static Model donut_end_model = new Model("");
//	public static Model donut_mid_model = new Model("");
	
	public static Model donut_end_model = PLYParser.parseColoredMesh("donut_block_end");
	public static Model donut_mid_model = OBJParser.parseTriangleMesh("donut_block");
	
	private static Material mid_material;
	
	public static final float[] DONUT_YELLOW = {1.0f, 0.8f, 0.3f};
	
	private SceneNode donut_end_left_node;
	private SceneNode donut_end_right_node;
	private SceneNode donut_mid_node;
	
	private static boolean initialized = false; 
	
	
	public DonutBlock(GL2 gl, Vec3 position, Vec3 rotation)
	{
		if(!initialized) initialize();
		
		donut_end_left_node = new SceneNode(donut_end_model);
		donut_end_left_node.setTranslation(position);
		donut_end_left_node.setRotation(rotation);
		donut_end_left_node.setRenderMode(RenderMode.COLOR);
		
		Vec3 r = new Vec3(rotation.x, rotation.y + 180, rotation.z);
		
		donut_end_right_node = new SceneNode(donut_end_model);
		donut_end_right_node.setTranslation(position);
		donut_end_right_node.setRotation(r);
		donut_end_right_node.setRenderMode(RenderMode.COLOR);
		
		donut_mid_node = new SceneNode(donut_mid_model);
		donut_mid_node.setTranslation(position);
		donut_mid_node.setRotation(rotation);
		donut_mid_node.setRenderMode(RenderMode.COLOR);
		donut_mid_node.setColor(DONUT_YELLOW);
	}

	private static void initialize()
	{
		mid_material = new Material();
		mid_material.setDiffuse(DONUT_YELLOW);
		
		setupInstances();
		
		initialized = true;
	}
	
	private static void setupInstances()
	{
		FloatBuffer matrices = setupMatrices();
		
		donut_end_model.setPositionData(setupEndPositions());
		donut_end_model.setMatrixData(matrices);
		donut_end_model.matrixDivisor = 5;
		
		donut_mid_model.setPositionData(setupMidPositions());
		donut_mid_model.setMatrixData(matrices);
		donut_mid_model.matrixDivisor = 5;
	}
	
	private static FloatBuffer setupMidPositions()
	{
		int num_of_blocks = 5;
		
		FloatBuffer positions = FloatBuffer.allocate(num_of_blocks * 4);
		
		Vec3 start = new Vec3(56.25, 25.5, -18);
		
		for(int i = 0; i < num_of_blocks; i++)
		{
			positions.put(start.toArray());  positions.put(1);
			start = start.add(new Vec3(0, 0, 9));
		}
		positions.position(0);
		
		return positions;
	}
	
	private static FloatBuffer setupEndPositions()
	{
		int num_of_blocks = 5;
		
		FloatBuffer positions = FloatBuffer.allocate(num_of_blocks * 2 * 4);
		
		Vec3 start = new Vec3(56.25, 25.5, -18);
		
		for(int i = 0; i < num_of_blocks; i++)
		{
			positions.put(start.toArray());  positions.put(1);
			start = start.add(new Vec3(0, 0, 9));
		}
		
		start = new Vec3(56.25, 25.5, -18);
		
		for(int i = 0; i < num_of_blocks; i++)
		{
			positions.put(start.toArray());  positions.put(1);
			start = start.add(new Vec3(0, 0, 9));
		}
		positions.position(0);
		
		return positions;
	}
	
	private static FloatBuffer setupMatrices()
	{
		FloatBuffer matrices = FloatBuffer.allocate(16 * 2);
		
		float[] model = Arrays.copyOf(Matrix.IDENTITY_MATRIX_16, 16);
		Matrix.scale(model, 4.5f, 4.5f, 4.5f);
		matrices.put(model);
		
		Matrix.multiply(model, model, Matrix.getRotationMatrix(Matrix.getRotationMatrix(0, 180, 0)));
		matrices.put(model);
		
		matrices.position(0);
		
		return matrices;
	}
	
	public void render(GL2 gl)
	{
		donut_end_left_node.render(gl);
		donut_end_right_node.render(gl);
		donut_mid_node.render(gl);
	}
	
	public static void renderSimplified(GL2 gl)
	{
		if(!initialized) initialize();
		
		Renderer.instanced_mode = true;
		Renderer.instanced_matrix_mode = true;
		
		if(Scene.enable_culling && !Scene.shadowMode && !Scene.reflectMode) gl.glEnable(GL2.GL_CULL_FACE);
		
		Shader shader = Shader.get("phong_mat_inst");
		if(shader != null) shader.enable(gl);
		
		donut_end_model.renderInstanced(gl, 10);
		
		mid_material.load(gl);
		donut_mid_model.renderInstanced(gl,  5);
		
		Material.loadDefault(gl);
		
		gl.glDisable(GL2.GL_CULL_FACE);
		
		Renderer.instanced_mode = false;
		Renderer.instanced_matrix_mode = false;
	}
}
