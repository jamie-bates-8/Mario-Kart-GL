package bates.jamie.graphics.entity;

import java.util.List;

import javax.media.opengl.GL2;

import bates.jamie.graphics.collision.BoundParser;
import bates.jamie.graphics.collision.OBB;
import bates.jamie.graphics.entity.CheckeredBlock.BlockType;
import bates.jamie.graphics.scene.Material;
import bates.jamie.graphics.scene.Model;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.scene.SceneNode;
import bates.jamie.graphics.scene.SceneNode.RenderMode;
import bates.jamie.graphics.util.OBJParser;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.TextureLoader;
import bates.jamie.graphics.util.Vec2;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.shader.Shader;

import com.jogamp.opengl.util.texture.Texture;


public class GreenFort
{
	private static Texture grass_top_diffuse;
	        static Texture grass_top_normal;
	        static Texture grass_top_alpha;
	
	private static Texture rock_wall_diffuse;
	        static Texture rock_wall_normal;
	        static Texture rock_wall_height;
	
//	private static Model grass_top_upper_model = OBJParser.parseIndexedArrays("grass_top_upper", false, false, new Vec2(1.5, 1).multiply(1.5f), new Vec2(8, 1));
//	private static Model grass_top_lower_model = OBJParser.parseIndexedArrays("grass_top_lower", false, true,  new Vec2(2.5, 2).multiply(1.5f), new Vec2(10, 1));
	static Model rock_wall_upper_model = OBJParser.parseTexturedTriangleMesh("rock_wall_upper", new Vec2(5, -1).multiply(1.5f));
	static Model rock_wall_lower_model = OBJParser.parseTexturedTriangleMesh("rock_wall_lower", new Vec2(8, -1).multiply(1.5f));
	
//	private static Model grass_top_upper_model = OBJParser.parseTexturedTriangleMesh("grass_top_upper_2", "grass_top_upper_3", new Vec2(1.5, 1).multiply(1.5f), new Vec2(8, 1));
//	private static Model grass_top_lower_model = OBJParser.parseTexturedTriangleMesh("grass_top_lower_3", "grass_top_lower_4", new Vec2(2.5, 1).multiply(1.5f), new Vec2(10, 1));
	
	static Model grass_top_upper_model = new Model("grass_top_upper");
	static Model grass_top_lower_model = new Model("grass_top_lower");
//	private static Model rock_wall_upper_model = new Model("rock_wall_upper");
//	private static Model rock_wall_lower_model = new Model("rock_wall_lower");
	
	private static Material rock_wall_mat;
	private static Material grass_top_mat;
	
	private SceneNode grass_top_upper_node;
	private SceneNode grass_top_lower_node;
	private SceneNode rock_wall_upper_node;
	private SceneNode rock_wall_lower_node;
	
	private WoodBridge large_bridge_lower;
	private WoodBridge large_bridge_upper;
	
	private WoodBridge small_bridge;
	
	private CheckeredBlock block_corner;
//	
//	private CheckeredBlock block_upper;
//	private CheckeredBlock slope_upper;
//	private CheckeredBlock wedge_upper;
//	
//	private CheckeredBlock block_x0;
//	private CheckeredBlock block_x1;
//	private CheckeredBlock block_x2;
//	private CheckeredBlock slope_x;
//	private CheckeredBlock wedge_x;
//	
//	private CheckeredBlock block_z0;
//	private CheckeredBlock block_z1;
//	private CheckeredBlock block_z2;
//	private CheckeredBlock slope_z;
//	private CheckeredBlock wedge_z;
	
	private List<OBB> bounds;
	
	public boolean displayModel = true;
	
	
	public GreenFort(GL2 gl)
	{
		loadTextures(gl);
		
		
		large_bridge_lower = new WoodBridge(gl, new Vec3(90,  7.5,  33.75), 0, true, false);
		large_bridge_upper = new WoodBridge(gl, new Vec3( 0, 37.5, 101.25), 0, true, false);
		
		small_bridge = new WoodBridge(gl, new Vec3(0, 30, 56.25), 90, false, false);
		
		
		block_corner = new CheckeredBlock(gl, BlockType.BLOCK, CheckeredBlock.BLOCK_GREEN, new Vec3(33.75, 0, 33.75), new Vec3());
//		
//		slope_upper = new CheckeredBlock(gl, BlockType.SLOPE, CheckeredBlock.BLOCK_BLUE,   new Vec3( 78.75, 30, 56.25), new Vec3());
//		wedge_upper = new CheckeredBlock(gl, BlockType.WEDGE, CheckeredBlock.BLOCK_RED,    new Vec3( 56.25, 30, 56.25), new Vec3());
//		block_upper = new CheckeredBlock(gl, BlockType.BLOCK, CheckeredBlock.BLOCK_YELLOW, new Vec3(101.25, 30, 56.25), new Vec3());
//		
//		block_x0 = new CheckeredBlock(gl, BlockType.BLOCK, CheckeredBlock.BLOCK_RED,    new Vec3(146.25, 0,  33.75), new Vec3());
//		block_x1 = new CheckeredBlock(gl, BlockType.BLOCK, CheckeredBlock.BLOCK_BLUE,   new Vec3(146.25, 0,  56.25), new Vec3());
//		block_x2 = new CheckeredBlock(gl, BlockType.BLOCK, CheckeredBlock.BLOCK_YELLOW, new Vec3(146.25, 0,  78.75), new Vec3());
//		slope_x  = new CheckeredBlock(gl, BlockType.SLOPE, CheckeredBlock.BLOCK_RED,    new Vec3(146.25, 0, 101.25), new Vec3(0, 90, 0));
//		wedge_x  = new CheckeredBlock(gl, BlockType.WEDGE, CheckeredBlock.BLOCK_BLUE,   new Vec3(146.25, 0, 123.75), new Vec3(0, 90, 0));
//		
//		block_z0 = new CheckeredBlock(gl, BlockType.BLOCK, CheckeredBlock.BLOCK_RED,    new Vec3( 33.75, 0, 146.25), new Vec3(0,  90, 0));
//		block_z1 = new CheckeredBlock(gl, BlockType.BLOCK, CheckeredBlock.BLOCK_YELLOW, new Vec3( 56.25, 0, 146.25), new Vec3(0,  90, 0));
//		block_z2 = new CheckeredBlock(gl, BlockType.BLOCK, CheckeredBlock.BLOCK_BLUE,   new Vec3( 78.75, 0, 146.25), new Vec3(0,  90, 0));
//		slope_z  = new CheckeredBlock(gl, BlockType.SLOPE, CheckeredBlock.BLOCK_RED,    new Vec3(101.25, 0, 146.25), new Vec3(0, 180, 0));
//		wedge_z  = new CheckeredBlock(gl, BlockType.WEDGE, CheckeredBlock.BLOCK_YELLOW, new Vec3(123.75, 0, 146.25), new Vec3(0, 180, 0));
		
		
		grass_top_mat = new Material();
		grass_top_mat.setDiffuseMap(grass_top_diffuse);
		grass_top_mat.setNormalMap(grass_top_normal);
		grass_top_mat.setAlphaMap(grass_top_alpha);
		grass_top_mat.setSpecular(RGB.DARK_GRAY);
		grass_top_mat.setShininess(32);
		
		
		grass_top_upper_model.calculateTangents();
		
		grass_top_upper_node = new SceneNode(grass_top_upper_model);
		grass_top_upper_node.useParallax(false);
		grass_top_upper_node.setScale(new Vec3(15));
		grass_top_upper_node.setTranslation(new Vec3(90, 0, 90));
		grass_top_upper_node.setMaterial(grass_top_mat);
		grass_top_upper_node.setRenderMode(RenderMode.BUMP_COLOR);
		
		grass_top_lower_model.calculateTangents();
		
		grass_top_lower_node = new SceneNode(grass_top_lower_model);
		grass_top_lower_node.useParallax(false);
		grass_top_lower_node.setScale(new Vec3(15));
		grass_top_lower_node.setTranslation(new Vec3(90, 0, 90));
		grass_top_lower_node.setMaterial(grass_top_mat);
		grass_top_lower_node.setRenderMode(RenderMode.BUMP_COLOR);
		
		rock_wall_mat = new Material();
		rock_wall_mat.setDiffuseMap(rock_wall_diffuse);
		rock_wall_mat.setNormalMap(rock_wall_normal);
		rock_wall_mat.setHeightMap(rock_wall_height);
		
		rock_wall_upper_model.calculateTangents();
		
		rock_wall_upper_node = new SceneNode(rock_wall_upper_model);
		rock_wall_upper_node.setRenderMode(RenderMode.BUMP_TEXTURE);
		rock_wall_upper_node.useParallax(true);
		rock_wall_upper_node.setScale(new Vec3(15));
		rock_wall_upper_node.setTranslation(new Vec3(90, 0, 90));
		rock_wall_upper_node.setMaterial(rock_wall_mat);
		
		rock_wall_lower_model.calculateTangents();
		
		rock_wall_lower_node = new SceneNode(rock_wall_lower_model);
		rock_wall_lower_node.setRenderMode(RenderMode.BUMP_TEXTURE);
		rock_wall_lower_node.useParallax(true);
		rock_wall_lower_node.setScale(new Vec3(15));
		rock_wall_lower_node.setTranslation(new Vec3(90, 0, 90));
		rock_wall_lower_node.setMaterial(rock_wall_mat);
	    
		
	    bounds = BoundParser.parseOBBs("bound/blockFort.bound");
	}
	
	public void loadTextures(GL2 gl)
	{
		try
		{
			grass_top_diffuse = TextureLoader.load(gl, "tex/mario_grass.jpg");
			grass_top_normal  = TextureLoader.load(gl, "tex/grass_new_NRM.jpg");
			grass_top_alpha   = TextureLoader.load(gl, "tex/grass_ALPHA.png");
			
			rock_wall_diffuse = TextureLoader.load(gl, "tex/rock_wall_COLOR.jpg");
			rock_wall_normal  = TextureLoader.load(gl, "tex/rock_wall_NRM.jpg"  );
			rock_wall_height  = TextureLoader.load(gl, "tex/rock_wall_DISP.jpg" );
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
//			grass_top_upper_node.render(gl);
//			grass_top_upper_node.renderNormals(gl);
//			grass_top_upper_node.renderTangents(gl);
//			grass_top_lower_node.render(gl);
//			grass_top_lower_node.renderNormals(gl);
//			grass_top_lower_node.renderTangents(gl);
			rock_wall_upper_node.render(gl);
			rock_wall_lower_node.render(gl);
			
			if(!Scene.normalMode) WoodBridge.renderSimplified(gl);
			else
			{
				large_bridge_lower.render(gl);
				large_bridge_upper.render(gl);
				small_bridge.render(gl);
			}
			
//			Scene.renderQuery.getResult(gl);
//			Scene.renderQuery.begin(gl);
			
			CheckeredBlock.renderSimplified(gl);
			
//			if(!Scene.normalMode) CheckeredBlock.renderSimplified(gl);
//			else
//			{
//				block_corner.render(gl);
//				
//				block_upper.render(gl);
//				slope_upper.render(gl);
//				wedge_upper.render(gl);
//				
//				block_x0.render(gl);
//				block_x1.render(gl);
//				block_x2.render(gl);
//				slope_x.render(gl); 
//				wedge_x.render(gl);
//				
//				block_z0.render(gl);
//				block_z1.render(gl);
//				block_z2.render(gl);
//				slope_z.render(gl);
//				wedge_z.render(gl);
//			}
			
//			Scene.renderQuery.end(gl);

		}
	}
	
	public List<OBB> getBounds() { return bounds; }
}
