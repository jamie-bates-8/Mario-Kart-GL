package bates.jamie.graphics.entity;

import java.util.List;

import javax.media.opengl.GL2;

import bates.jamie.graphics.collision.BoundParser;
import bates.jamie.graphics.collision.OBB;
import bates.jamie.graphics.entity.CheckeredBlock.BlockType;
import bates.jamie.graphics.scene.Material;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.scene.SceneNode;
import bates.jamie.graphics.scene.SceneNode.RenderMode;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.TextureLoader;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.shader.Shader;

import com.jogamp.opengl.util.texture.Texture;


public class RedFort
{
	private static Texture grass_top_diffuse;
	private static Texture rock_wall_diffuse;
	
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
	
	private CheckeredBlock block_upper;
	private CheckeredBlock slope_upper;
	private CheckeredBlock wedge_upper;
	
	private CheckeredBlock block_x0;
	private CheckeredBlock block_x1;
	private CheckeredBlock block_x2;
	private CheckeredBlock slope_x;
	private CheckeredBlock wedge_x;
	
	private CheckeredBlock block_z0;
	private CheckeredBlock block_z1;
	private CheckeredBlock block_z2;
	private CheckeredBlock slope_z;
	private CheckeredBlock wedge_z;
	
	private List<OBB> bounds;
	
	public boolean displayModel = true;
	
	
	public RedFort(GL2 gl)
	{
		loadTextures(gl);
		
		
		large_bridge_lower = new WoodBridge(gl, new Vec3(-90,  7.5,  -33.75), 0, true, true);
		large_bridge_upper = new WoodBridge(gl, new Vec3(  0, 37.5, -101.25), 0, true, true);
		
		small_bridge = new WoodBridge(gl, new Vec3(0, 30, -56.25), -90, false, true);
		
		
		block_corner = new CheckeredBlock(gl, BlockType.BLOCK, CheckeredBlock.BLOCK_CYAN, new Vec3(-33.75, 0, -33.75), new Vec3(0, -90, 0));
		
		slope_upper = new CheckeredBlock(gl, BlockType.SLOPE, CheckeredBlock.BLOCK_LILAC,   new Vec3( -78.75, 30, -56.25), new Vec3(0, 180, 0));
		wedge_upper = new CheckeredBlock(gl, BlockType.WEDGE, CheckeredBlock.BLOCK_MAGENTA, new Vec3( -56.25, 30, -56.25), new Vec3(0, 180, 0));
		block_upper = new CheckeredBlock(gl, BlockType.BLOCK, CheckeredBlock.BLOCK_ORANGE,  new Vec3(-101.25, 30, -56.25), new Vec3());
		
		block_x0 = new CheckeredBlock(gl, BlockType.BLOCK, CheckeredBlock.BLOCK_MAGENTA, new Vec3(-146.25, 0,  -33.75), new Vec3());
		block_x1 = new CheckeredBlock(gl, BlockType.BLOCK, CheckeredBlock.BLOCK_LILAC,   new Vec3(-146.25, 0,  -56.25), new Vec3());
		block_x2 = new CheckeredBlock(gl, BlockType.BLOCK, CheckeredBlock.BLOCK_ORANGE,  new Vec3(-146.25, 0,  -78.75), new Vec3());
		slope_x  = new CheckeredBlock(gl, BlockType.SLOPE, CheckeredBlock.BLOCK_MAGENTA, new Vec3(-146.25, 0, -101.25), new Vec3(0, -90, 0));
		wedge_x  = new CheckeredBlock(gl, BlockType.WEDGE, CheckeredBlock.BLOCK_LILAC,   new Vec3(-146.25, 0, -123.75), new Vec3(0, -90, 0));
		
		block_z0 = new CheckeredBlock(gl, BlockType.BLOCK, CheckeredBlock.BLOCK_MAGENTA, new Vec3( -33.75, 0, -146.25), new Vec3(0,  90, 0));
		block_z1 = new CheckeredBlock(gl, BlockType.BLOCK, CheckeredBlock.BLOCK_ORANGE,  new Vec3( -56.25, 0, -146.25), new Vec3(0,  90, 0));
		block_z2 = new CheckeredBlock(gl, BlockType.BLOCK, CheckeredBlock.BLOCK_LILAC,   new Vec3( -78.75, 0, -146.25), new Vec3(0,  90, 0));
		slope_z  = new CheckeredBlock(gl, BlockType.SLOPE, CheckeredBlock.BLOCK_MAGENTA, new Vec3(-101.25, 0, -146.25), new Vec3());
		wedge_z  = new CheckeredBlock(gl, BlockType.WEDGE, CheckeredBlock.BLOCK_ORANGE,  new Vec3(-123.75, 0, -146.25), new Vec3());
		
		
		grass_top_mat = new Material();
		grass_top_mat.setDiffuseMap(grass_top_diffuse);
		grass_top_mat.setNormalMap(GreenFort.grass_top_normal);
		grass_top_mat.setAlphaMap(GreenFort.grass_top_alpha);
		grass_top_mat.setSpecular(RGB.BLACK);
		grass_top_mat.setShininess(32);
		
		grass_top_upper_node = new SceneNode(GreenFort.grass_top_upper_model);
		grass_top_upper_node.useParallax(false);
		grass_top_upper_node.setScale(new Vec3(15));
		grass_top_upper_node.setTranslation(new Vec3(-90, 0, -90));
		grass_top_upper_node.setRotation(new Vec3(0, 180, 0));
		grass_top_upper_node.setMaterial(grass_top_mat);
		grass_top_upper_node.enableCulling(false);
		
		grass_top_lower_node = new SceneNode(GreenFort.grass_top_lower_model);
		grass_top_lower_node.useParallax(false);
		grass_top_lower_node.setScale(new Vec3(15));
		grass_top_lower_node.setTranslation(new Vec3(-90, 0, -90));
		grass_top_lower_node.setRotation(new Vec3(0, 180, 0));
		grass_top_lower_node.setMaterial(grass_top_mat);
		grass_top_lower_node.enableCulling(false);
		
		rock_wall_mat = new Material();
		rock_wall_mat.setDiffuseMap(rock_wall_diffuse);
		rock_wall_mat.setNormalMap(GreenFort.rock_wall_normal);
		rock_wall_mat.setHeightMap(GreenFort.rock_wall_height);
		
		rock_wall_upper_node = new SceneNode(GreenFort.rock_wall_upper_model);
		rock_wall_upper_node.setRenderMode(RenderMode.BUMP_TEXTURE);
		rock_wall_upper_node.useParallax(true);
		rock_wall_upper_node.setScale(new Vec3(15));
		rock_wall_upper_node.setTranslation(new Vec3(-90, 0, -90));
		rock_wall_upper_node.setRotation(new Vec3(0, 180, 0));
		rock_wall_upper_node.setMaterial(rock_wall_mat);
		
		rock_wall_lower_node = new SceneNode(GreenFort.rock_wall_lower_model);
		rock_wall_lower_node.setRenderMode(RenderMode.BUMP_TEXTURE);
		rock_wall_lower_node.useParallax(true);
		rock_wall_lower_node.setScale(new Vec3(15));
		rock_wall_lower_node.setTranslation(new Vec3(-90, 0, -90));
		rock_wall_lower_node.setRotation(new Vec3(0, 180, 0));
		rock_wall_lower_node.setMaterial(rock_wall_mat);
	    
		
	    bounds = BoundParser.parseOBBs("bound/blockFort.bound");
	}
	
	public void loadTextures(GL2 gl)
	{
		try
		{
			grass_top_diffuse = TextureLoader.load(gl, "tex/mario_pink_grass.jpg");
			rock_wall_diffuse = TextureLoader.load(gl, "tex/rock_wall_6.jpg");
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public void render(GL2 gl)
	{
		Shader shader = (Scene.depthMode) ? Shader.getDirect("simple_alpha") : Shader.get("bump_alpha");
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
			
			if(!Scene.normalMode && Scene.enableInstanced) {}
			else
			{
				large_bridge_lower.render(gl);
				large_bridge_upper.render(gl);
				small_bridge.render(gl);
			}
			
//			Scene.renderQuery.getResult(gl);
//			Scene.renderQuery.begin(gl);

			if(!Scene.enableInstanced)
			{
				block_corner.render(gl);
				
				block_upper.render(gl);
				slope_upper.render(gl);
				wedge_upper.render(gl);
				
				block_x0.render(gl);
				block_x1.render(gl);
				block_x2.render(gl);
				slope_x.render(gl); 
				wedge_x.render(gl);
				
				block_z0.render(gl);
				block_z1.render(gl);
				block_z2.render(gl);
				slope_z.render(gl);
				wedge_z.render(gl);
			}
			
//			Scene.renderQuery.end(gl);

		}
	}
	
	public List<OBB> getBounds() { return bounds; }
}
