package bates.jamie.graphics.entity;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;

import bates.jamie.graphics.collision.BoundParser;
import bates.jamie.graphics.collision.OBB;
import bates.jamie.graphics.scene.Material;
import bates.jamie.graphics.scene.Model;
import bates.jamie.graphics.scene.SceneNode;
import bates.jamie.graphics.scene.SceneNode.RenderMode;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.TextureLoader;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.shader.Shader;
import bates.jamie.graphics.util.shader.Uniform;

import com.jogamp.opengl.util.texture.Texture;


public class NewBlockFort
{
	private static Texture grass_top_diffuse;
	private static Texture grass_top_normal;
	private static Texture grass_top_alpha;
	
	private static Texture rock_wall_diffuse;
	private static Texture rock_wall_normal;
	
	private static Texture slope_pattern_mask;
	
//	private static Model grass_top_upper_model = OBJParser.parseIndexedArrays("grass_top_upper", false, false, new Vec2(1.5, 1).multiply(1.5f), new Vec2(8, 1));
//	private static Model grass_top_lower_model = OBJParser.parseIndexedArrays("grass_top_lower", false, true,  new Vec2(2.5, 2).multiply(1.5f), new Vec2(10, 1));
//	private static Model rock_wall_upper_model = OBJParser.parseTexturedTriangleMesh("rock_wall_upper", new Vec2(4, -1).multiply(1.5f));
//	private static Model rock_wall_lower_model = OBJParser.parseTexturedTriangleMesh("rock_wall_lower", new Vec2(8, -1).multiply(1.5f));
	
	private static Model grass_top_upper_model = new Model("grass_top_upper");
	private static Model grass_top_lower_model = new Model("grass_top_lower");
	private static Model rock_wall_upper_model = new Model("rock_wall_upper");
	private static Model rock_wall_lower_model = new Model("rock_wall_lower");
	
	public static Model bevelled_cube_model  = new Model("checker_block");
	public static Model bevelled_slope_model = new Model("sloped_block");
	public static Model block_bolts_model = new Model("block_bolts");
	public static Model wedge_block_model = new Model("wedge_block");
	
	private static Material rock_wall_mat;
	private static Material grass_top_mat;
	
	private SceneNode grass_top_upper_node;
	private SceneNode grass_top_lower_node;
	private SceneNode rock_wall_upper_node;
	private SceneNode rock_wall_lower_node;
	
	private WoodBridge large_bridge_lower;
	private WoodBridge large_bridge_upper;
	
	private WoodBridge small_bridge;
	
	private SceneNode block_corner_node;
	private SceneNode block_bolt_node;
	
	private SceneNode block_upper_node;
	private SceneNode slope_upper_node;
	private SceneNode wedge_upper_node;
	
	private SceneNode block_x0_node;
	private SceneNode block_x1_node;
	private SceneNode block_x2_node;
	private SceneNode slope_x_node;
	private SceneNode wedge_x_node;
	
	private SceneNode block_z0_node;
	private SceneNode block_z1_node;
	private SceneNode block_z2_node;
	private SceneNode slope_z_node;
	private SceneNode wedge_z_node;
	
	private List<OBB> bounds;
	
	public boolean displayModel = true;
	
	private static final float[] BLOCK_RED    = new float[] {0.800f, 0.134f, 0.155f};
	private static final float[] BLOCK_BLUE   = RGB.SKY_BLUE;
	private static final float[] BLOCK_GREEN  = new float[] {0.257f, 0.800f, 0.243f};
	private static final float[] BLOCK_YELLOW = new float[] {0.800f, 0.730f, 0.180f};
	
	
	public NewBlockFort(GL2 gl)
	{
		loadTextures(gl);
		
		large_bridge_lower = new WoodBridge(gl, new Vec3(90,  7.5,  33.75), 0, true);
		large_bridge_lower.enable_simplify = true;
		large_bridge_upper = new WoodBridge(gl, new Vec3( 0, 37.5, 101.25), 0, true);
		
		small_bridge = new WoodBridge(gl, new Vec3(0, 30, 56.25), 90, false);
		
		List<Uniform> uniforms = new ArrayList<Uniform>();
		
		block_bolt_node = new SceneNode(block_bolts_model);
		block_bolt_node.setRenderMode(RenderMode.COLOR);
		block_bolt_node.setColor(RGB.WHITE);
		
		Vec3 scale = new Vec3(3, 4, 3);
		Vec3 texScale = scale.add(new Vec3(1));
		
		uniforms.add(Uniform.getUniform("scaleVec", texScale.normalizeScale()));
		uniforms.add(Uniform.getUniform("minScale", texScale.min()));
		uniforms.add(Uniform.getSampler("patternMask", 0));
		
		block_corner_node = new SceneNode(bevelled_cube_model);
		block_corner_node.setRenderMode(RenderMode.COLOR);
		block_corner_node.setColor(BLOCK_GREEN);
		block_corner_node.setUniforms(uniforms);
		block_corner_node.setShader(Shader.get("checker_block"));
		block_corner_node.setScale(new Vec3(15));
		block_corner_node.setTranslation(new Vec3(33.75, 0, 33.75));
		block_corner_node.addChild(block_bolt_node);
		
		slope_upper_node = new SceneNode(bevelled_slope_model);
		slope_upper_node.setRenderMode(RenderMode.COLOR);
		slope_upper_node.getMaterial().setDiffuseMap(slope_pattern_mask);
		slope_upper_node.setColor(BLOCK_BLUE);
		slope_upper_node.setUniforms(uniforms);
		slope_upper_node.setShader(Shader.get("checker_shadow"));
		slope_upper_node.setScale(new Vec3(15));
		slope_upper_node.setTranslation(new Vec3(78.75, 30, 56.25));
		
		wedge_upper_node = new SceneNode(wedge_block_model);
		wedge_upper_node.setRenderMode(RenderMode.COLOR);
		wedge_upper_node.setColor(BLOCK_RED);
		wedge_upper_node.setScale(new Vec3(15));
		wedge_upper_node.setTranslation(new Vec3(56.25, 30, 56.25));
		
		block_upper_node = new SceneNode(bevelled_cube_model);
		block_upper_node.setRenderMode(RenderMode.COLOR);
		block_upper_node.setColor(BLOCK_YELLOW);
		block_upper_node.setUniforms(uniforms);
		block_upper_node.setShader(Shader.get("checker_block"));
		block_upper_node.setScale(new Vec3(15));
		block_upper_node.setTranslation(new Vec3(101.25, 30, 56.25));
		block_upper_node.addChild(block_bolt_node);
		
		block_x0_node = new SceneNode(bevelled_cube_model);
		block_x0_node.setRenderMode(RenderMode.COLOR);
		block_x0_node.setColor(BLOCK_RED);
		block_x0_node.setUniforms(uniforms);
		block_x0_node.setShader(Shader.get("checker_block"));
		block_x0_node.setScale(new Vec3(15));
		block_x0_node.setTranslation(new Vec3(146.25, 0,  33.75));
		block_x0_node.addChild(block_bolt_node);
		
		block_x1_node = new SceneNode(bevelled_cube_model);
		block_x1_node.setRenderMode(RenderMode.COLOR);
		block_x1_node.setColor(BLOCK_BLUE);
		block_x1_node.setUniforms(uniforms);
		block_x1_node.setShader(Shader.get("checker_block"));
		block_x1_node.setScale(new Vec3(15));
		block_x1_node.setTranslation(new Vec3(146.25, 0, 56.25));
		block_x1_node.addChild(block_bolt_node);
		
		block_x2_node = new SceneNode(bevelled_cube_model);
		block_x2_node.setRenderMode(RenderMode.COLOR);
		block_x2_node.setColor(BLOCK_YELLOW);
		block_x2_node.setUniforms(uniforms);
		block_x2_node.setShader(Shader.get("checker_block"));
		block_x2_node.setScale(new Vec3(15));
		block_x2_node.setTranslation(new Vec3(146.25, 0, 78.75));
		block_x2_node.addChild(block_bolt_node);
		
		slope_x_node = new SceneNode(bevelled_slope_model);
		slope_x_node.setRenderMode(RenderMode.COLOR);
		slope_x_node.getMaterial().setDiffuseMap(slope_pattern_mask);
		slope_x_node.setColor(BLOCK_RED);
		slope_x_node.setUniforms(uniforms);
		slope_x_node.setShader(Shader.get("checker_shadow"));
		slope_x_node.setScale(new Vec3(15));
		slope_x_node.setTranslation(new Vec3(146.25, 0, 101.25));
		slope_x_node.setRotation(new Vec3(0, 90, 0));
		
		wedge_x_node = new SceneNode(wedge_block_model);
		wedge_x_node.setRenderMode(RenderMode.COLOR);
		wedge_x_node.setColor(BLOCK_BLUE);
		wedge_x_node.setScale(new Vec3(15));
		wedge_x_node.setTranslation(new Vec3(146.25, 0, 123.75));
		wedge_x_node.setRotation(new Vec3(0, 90, 0));
		
		block_z0_node = new SceneNode(bevelled_cube_model);
		block_z0_node.setRenderMode(RenderMode.COLOR);
		block_z0_node.setColor(BLOCK_RED);
		block_z0_node.setUniforms(uniforms);
		block_z0_node.setShader(Shader.get("checker_block"));
		block_z0_node.setScale(new Vec3(15));
		block_z0_node.setTranslation(new Vec3(33.75, 0, 146.25));
		block_z0_node.addChild(block_bolt_node);
		block_z0_node.setRotation(new Vec3(0, 90, 0));
		
		block_z1_node = new SceneNode(bevelled_cube_model);
		block_z1_node.setRenderMode(RenderMode.COLOR);
		block_z1_node.setColor(BLOCK_YELLOW);
		block_z1_node.setUniforms(uniforms);
		block_z1_node.setShader(Shader.get("checker_block"));
		block_z1_node.setScale(new Vec3(15));
		block_z1_node.setTranslation(new Vec3(56.25, 0, 146.25));
		block_z1_node.addChild(block_bolt_node);
		block_z1_node.setRotation(new Vec3(0, 90, 0));
		
		block_z2_node = new SceneNode(bevelled_cube_model);
		block_z2_node.setRenderMode(RenderMode.COLOR);
		block_z2_node.setColor(BLOCK_BLUE);
		block_z2_node.setUniforms(uniforms);
		block_z2_node.setShader(Shader.get("checker_block"));
		block_z2_node.setScale(new Vec3(15));
		block_z2_node.setTranslation(new Vec3(78.75, 0, 146.25));
		block_z2_node.addChild(block_bolt_node);
		block_z2_node.setRotation(new Vec3(0, 90, 0));
		
		slope_z_node = new SceneNode(bevelled_slope_model);
		slope_z_node.setRenderMode(RenderMode.COLOR);
		slope_z_node.getMaterial().setDiffuseMap(slope_pattern_mask);
		slope_z_node.setColor(BLOCK_RED);
		slope_z_node.setUniforms(uniforms);
		slope_z_node.setShader(Shader.get("checker_shadow"));
		slope_z_node.setScale(new Vec3(15));
		slope_z_node.setTranslation(new Vec3(101.25, 0, 146.25));
		slope_z_node.setRotation(new Vec3(0, 180, 0));
		
		wedge_z_node = new SceneNode(wedge_block_model);
		wedge_z_node.setRenderMode(RenderMode.COLOR);
		wedge_z_node.setColor(BLOCK_YELLOW);
		wedge_z_node.setScale(new Vec3(15));
		wedge_z_node.setTranslation(new Vec3(123.75, 0, 146.25));
		wedge_z_node.setRotation(new Vec3(0, 180, 0));
		
		grass_top_mat = new Material();
		grass_top_mat.setDiffuseMap(grass_top_diffuse);
		grass_top_mat.setNormalMap(grass_top_normal);
		grass_top_mat.setAlphaMap(grass_top_alpha);
		
		grass_top_upper_model.calculateTangents();
		
		grass_top_upper_node = new SceneNode(grass_top_upper_model);
		grass_top_upper_node.useParallax(false);
		grass_top_upper_node.setScale(new Vec3(15));
		grass_top_upper_node.setTranslation(new Vec3(90, 0, 90));
		grass_top_upper_node.setMaterial(grass_top_mat);
		
		grass_top_lower_model.calculateTangents();
		
		grass_top_lower_node = new SceneNode(grass_top_lower_model);
		grass_top_lower_node.useParallax(false);
		grass_top_lower_node.setScale(new Vec3(15));
		grass_top_lower_node.setTranslation(new Vec3(78.75, 30, 90));
		grass_top_lower_node.setMaterial(grass_top_mat);
		
		rock_wall_mat = new Material();
		rock_wall_mat.setDiffuseMap(rock_wall_diffuse);
		rock_wall_mat.setNormalMap(rock_wall_normal);
		
		rock_wall_upper_model.calculateTangents();
		
		rock_wall_upper_node = new SceneNode(rock_wall_upper_model);
		rock_wall_upper_node.setRenderMode(RenderMode.BUMP_TEXTURE);
		rock_wall_upper_node.useParallax(false);
		rock_wall_upper_node.setScale(new Vec3(15));
		rock_wall_upper_node.setTranslation(new Vec3(90, 0, 90));
		rock_wall_upper_node.setMaterial(rock_wall_mat);
		
		rock_wall_lower_model.calculateTangents();
		
		rock_wall_lower_node = new SceneNode(rock_wall_lower_model);
		rock_wall_lower_node.setRenderMode(RenderMode.BUMP_TEXTURE);
		rock_wall_lower_node.useParallax(false);
		rock_wall_lower_node.setScale(new Vec3(15));
		rock_wall_lower_node.setTranslation(new Vec3(90, 0, 90));
		rock_wall_lower_node.setMaterial(rock_wall_mat);
	    
	    bounds = BoundParser.parseOBBs("bound/blockFort.bound");
	}
	
	public void loadTextures(GL2 gl)
	{
		try
		{
			slope_pattern_mask = TextureLoader.load(gl, "tex/slope_mask.jpg");
			
			grass_top_diffuse = TextureLoader.load(gl, "tex/grass_COLOR.jpg");
			grass_top_normal  = TextureLoader.load(gl, "tex/grass_NRM.jpg"  );
			grass_top_alpha   = TextureLoader.load(gl, "tex/grass_ALPHA.png");
			
			rock_wall_diffuse = TextureLoader.load(gl, "tex/rock_wall_COLOR.jpg");
			rock_wall_normal  = TextureLoader.load(gl, "tex/rock_wall_NRM.jpg"  );
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public void render(GL2 gl)
	{
		Shader shader = Shader.get("bump_alpha");
		shader.enable(gl);
		
		shader.setSampler(gl, "texture"  , 0);
		shader.setSampler(gl, "bumpmap"  , 1);
		shader.setSampler(gl, "heightmap", 2);
		shader.setSampler(gl, "alphaMask", 3);
		
		shader.setUniform(gl, "enableParallax", false);
		
		Shader.disable(gl);
		
		if(displayModel)
		{
			grass_top_upper_node.render(gl, shader, null); 
			grass_top_lower_node.render(gl, shader, null);
			rock_wall_upper_node.render(gl);
			rock_wall_lower_node.render(gl);
			
			large_bridge_lower.render(gl);
//			large_bridge_upper.render(gl);
//			small_bridge.render(gl);
			
			block_corner_node.render(gl);
			
			block_upper_node.render(gl);
			slope_upper_node.render(gl);
			wedge_upper_node.render(gl);
			
			block_x0_node.render(gl);
			block_x1_node.render(gl);
			block_x2_node.render(gl);
			slope_x_node.render(gl); 
			wedge_x_node.render(gl);
			
			block_z0_node.render(gl);
			block_z1_node.render(gl);
			block_z2_node.render(gl);
			slope_z_node.render(gl);
			wedge_z_node.render(gl);
		}
	}
	
	public List<OBB> getBounds() { return bounds; }
}
