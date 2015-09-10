package bates.jamie.graphics.entity;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;

import bates.jamie.graphics.collision.BoundParser;
import bates.jamie.graphics.collision.OBB;
import bates.jamie.graphics.entity.GiftBox.BoxType;
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


public class YellowFort
{
	private static Texture cake_top_diffuse;
	        static Texture cake_top_normal;
	        static Texture cake_top_alpha;
	        static Texture cake_top_specular;
	
	private static Texture sponge_wall_diffuse;
	        static Texture sponge_wall_normal;
	        static Texture sponge_wall_height;
	        static Texture sponge_wall_specular;
	        
	private static Texture choco_bar_normal;
	private static Texture choco_bar_height;
	
	private static Texture choco_wrap_diffuse;
	private static Texture choco_wrap_specular;
	private static Texture choco_wrap_alt;
	
//	private static Model grass_top_upper_model = OBJParser.parseIndexedArrays("grass_top_upper", false, false, new Vec2(1.5, 1).multiply(1.5f), new Vec2(8, 1));
//	private static Model grass_top_lower_model = OBJParser.parseIndexedArrays("grass_top_lower", false, true,  new Vec2(2.5, 2).multiply(1.5f), new Vec2(10, 1));
	static Model sponge_wall_upper_model = OBJParser.parseTexturedTriangleMesh("rock_wall_upper", new Vec2(5, -1).multiply(1.0f));
	static Model sponge_wall_lower_model = OBJParser.parseTexturedTriangleMesh("rock_wall_lower", new Vec2(8, -1).multiply(1.0f));
	
	static Model cake_top_upper_model = OBJParser.parseTexturedTriangleMesh("grass_top_upper_2", "grass_top_upper_3", new Vec2(1.5, 1).multiply(1.5f), new Vec2(8, 1));
	static Model cake_top_lower_model = OBJParser.parseTexturedTriangleMesh("grass_top_lower_3", "grass_top_lower_4", new Vec2(2.5, 2).multiply(1.5f), new Vec2(10, 1));
	
//	static Model grass_top_upper_model = new Model("grass_top_upper");
//	static Model grass_top_lower_model = new Model("grass_top_lower");
//	private static Model rock_wall_upper_model = new Model("rock_wall_upper");
//	private static Model rock_wall_lower_model = new Model("rock_wall_lower");
	
	private static Model choco_bar_model  = OBJParser.parseTexturedTriangleMesh("chocolate_bar_2");
	private static Model choco_wrap_model = OBJParser.parseTexturedTriangleMesh("chocolate_wrapper");
	
	private static Material sponge_wall_mat;
	public static Material cake_top_mat;
	
	private SceneNode cake_top_upper_node;
	private SceneNode cake_top_lower_node;
	private SceneNode sponge_wall_upper_node;
	private SceneNode sponge_wall_lower_node;
	
	private SceneNode milk_choco_bar_node;
	private SceneNode white_choco_bar_node;
	private SceneNode dark_choco_bar_node;
	
	private SceneNode milk_choco_wrap_node;
	private SceneNode mint_choco_wrap_node;
	
	private List<CheckeredCookie> cookies;
	private List<MushroomBlock> mush_blocks;
	
	private List<GiftBox> gift_boxes;
	
	private List<OBB> bounds;
	
	public boolean displayModel = true;
	
	
	public YellowFort(GL2 gl)
	{
		loadTextures(gl);
		
		cake_top_mat = new Material();
		cake_top_mat.setDiffuseMap(cake_top_diffuse);
		cake_top_mat.setNormalMap(cake_top_normal);
		cake_top_mat.setAlphaMap(cake_top_alpha);
		cake_top_mat.setSpecularMap(cake_top_specular);
		cake_top_mat.setSpecular(RGB.WHITE);
		cake_top_mat.setShininess(24);
		
		
		cake_top_upper_model.calculateTangents();
		
		cake_top_upper_node = new SceneNode(cake_top_upper_model);
		cake_top_upper_node.setScale(new Vec3(15));
		cake_top_upper_node.setTranslation(new Vec3(90, 0, -90));
		cake_top_upper_node.setRotation(new Vec3(0, 90, 0));
		cake_top_upper_node.setMaterial(cake_top_mat);
		cake_top_upper_node.setRenderMode(RenderMode.BUMP_COLOR);
		cake_top_upper_node.enableCulling(false);
		
		cake_top_lower_model.calculateTangents();
		
		cake_top_lower_node = new SceneNode(cake_top_lower_model);
		cake_top_lower_node.setScale(new Vec3(15));
		cake_top_lower_node.setTranslation(new Vec3(90, 0, -90));
		cake_top_lower_node.setRotation(new Vec3(0, 90, 0));
		cake_top_lower_node.setMaterial(cake_top_mat);
		cake_top_lower_node.setRenderMode(RenderMode.BUMP_COLOR);
		cake_top_lower_node.enableCulling(false);
		
		sponge_wall_mat = new Material();
		sponge_wall_mat.setDiffuseMap(sponge_wall_diffuse);
		sponge_wall_mat.setNormalMap(sponge_wall_normal);
		sponge_wall_mat.setHeightMap(sponge_wall_height);
		sponge_wall_mat.setSpecularMap(sponge_wall_specular);
		
		sponge_wall_upper_model.calculateTangents();
		
		sponge_wall_upper_node = new SceneNode(sponge_wall_upper_model);
		sponge_wall_upper_node.setRenderMode(RenderMode.BUMP_TEXTURE);
		sponge_wall_upper_node.useSpecularMap(true);
		sponge_wall_upper_node.setScale(new Vec3(15));
		sponge_wall_upper_node.setTranslation(new Vec3(90, 1, -90));
		sponge_wall_upper_node.setRotation(new Vec3(0, 90, 0));
		sponge_wall_upper_node.setMaterial(sponge_wall_mat);
		
		sponge_wall_lower_model.calculateTangents();
		
		sponge_wall_lower_node = new SceneNode(sponge_wall_lower_model);
		sponge_wall_lower_node.setRenderMode(RenderMode.BUMP_TEXTURE);
		sponge_wall_lower_node.setScale(new Vec3(15));
		sponge_wall_lower_node.useSpecularMap(false);
		sponge_wall_lower_node.setTranslation(new Vec3(90, 0, -90));
		sponge_wall_lower_node.setRotation(new Vec3(0, 90, 0));
		sponge_wall_lower_node.setMaterial(sponge_wall_mat);
		
		
		cookies = new ArrayList<CheckeredCookie>();
		
		for(int i = 0; i < 7; i++)
		{
			cookies.add(new CheckeredCookie(gl, new Vec3(33.75, 2.25 + i * 4.3,  -33.75), new Vec3(180, 90 * i, 0)));
			cookies.add(new CheckeredCookie(gl, new Vec3(33.75, 2.25 + i * 4.3, -146.25), new Vec3(180, 90 * i, 0)));
		}
		
		mush_blocks = new ArrayList<MushroomBlock>();
		
		for(int i = 0; i < 4; i++)
			mush_blocks.add(new MushroomBlock(gl, new Vec3(101.25, 57.75, -33.75 + i * 22.5), new Vec3()));
		
		choco_bar_model.calculateTangents();
		
		milk_choco_bar_node = new SceneNode(choco_bar_model);
		milk_choco_bar_node.setScale(new Vec3(4.5));
		milk_choco_bar_node.setTranslation(new Vec3(33.75, 25.5, -112.5));
		milk_choco_bar_node.setRotation(new Vec3(0, 0, 0));
		milk_choco_bar_node.setColor(new Vec3(0.184f, 0.042f, 0.013f).correctGamma(2.2).toArray());
		milk_choco_bar_node.setRenderMode(RenderMode.BUMP_COLOR);
		milk_choco_bar_node.getMaterial().setNormalMap(choco_bar_normal);
		milk_choco_bar_node.getMaterial().setHeightMap(choco_bar_height);
		milk_choco_bar_node.enableCulling(false);
		
		white_choco_bar_node = new SceneNode(choco_bar_model);
		white_choco_bar_node.setScale(new Vec3(4.5));
		white_choco_bar_node.setTranslation(new Vec3(146.25, 12.5, -108.5));
		white_choco_bar_node.setRotation(new Vec3(-33.69, 0, 0));
		white_choco_bar_node.setColor(new Vec3(0.999f, 0.838f, 0.559f).correctGamma(2.2).toArray());
		white_choco_bar_node.setRenderMode(RenderMode.BUMP_COLOR);
		white_choco_bar_node.getMaterial().setNormalMap(choco_bar_normal);
		white_choco_bar_node.getMaterial().setHeightMap(choco_bar_height);
		white_choco_bar_node.enableCulling(false);
		
		dark_choco_bar_node = new SceneNode(choco_bar_model);
		dark_choco_bar_node.setScale(new Vec3(4.5));
		dark_choco_bar_node.setTranslation(new Vec3(56.25, 42.5, -71.5));
		dark_choco_bar_node.setRotation(new Vec3(33.69, 0, 0));
		dark_choco_bar_node.setColor(new Vec3(0.070f, 0.026f, 0.011f).correctGamma(2.2).toArray());
		dark_choco_bar_node.setRenderMode(RenderMode.BUMP_COLOR);
		dark_choco_bar_node.getMaterial().setNormalMap(choco_bar_normal);
		dark_choco_bar_node.getMaterial().setHeightMap(choco_bar_height);
		dark_choco_bar_node.enableCulling(false);
		dark_choco_bar_node.useParallax(false);
		

		milk_choco_wrap_node = new SceneNode(choco_wrap_model);
		milk_choco_wrap_node.setScale(new Vec3(4.5));
		milk_choco_wrap_node.setTranslation(new Vec3(33.75, 27.75, -67.5));
		milk_choco_wrap_node.setRotation(new Vec3(0, 0, 0));
		milk_choco_wrap_node.setRenderMode(RenderMode.TEXTURE_SPEC);
		milk_choco_wrap_node.getMaterial().setDiffuseMap(choco_wrap_diffuse);
		milk_choco_wrap_node.getMaterial().setSpecularMap(choco_wrap_specular);
		
		mint_choco_wrap_node = new SceneNode(choco_wrap_model);
		mint_choco_wrap_node.setScale(new Vec3(4.5));
		mint_choco_wrap_node.setTranslation(new Vec3(109.75, 14.25, -146.25));
		mint_choco_wrap_node.setRotation(new Vec3(33.69, 90, 0));
		mint_choco_wrap_node.setRenderMode(RenderMode.TEXTURE_SPEC);
		mint_choco_wrap_node.getMaterial().setDiffuseMap(choco_wrap_alt);
		mint_choco_wrap_node.getMaterial().setSpecularMap(choco_wrap_specular);
		
		
		gift_boxes = new ArrayList<GiftBox>();
		gift_boxes.add(new GiftBox(gl, new Vec3(56.25, 45, -101.25), new Vec3(), BoxType.BLUE_GLITTER));
		gift_boxes.add(new GiftBox(gl, new Vec3(56.25, 30, -101.25), new Vec3(), BoxType.GREEN_STRIPE));
		
	    bounds = BoundParser.parseOBBs("bound/blockFort.bound");
	}
	
	public void loadTextures(GL2 gl)
	{
		try
		{
			cake_top_diffuse  = TextureLoader.load(gl, "tex/cake_top.png");
			cake_top_normal   = TextureLoader.load(gl, "tex/cake_top_NRM.tga");
			cake_top_alpha    = TextureLoader.load(gl, "tex/cake_ALPHA.png");
			cake_top_specular = TextureLoader.load(gl, "tex/chocolate_top_SPEC.tga");
			
			sponge_wall_diffuse  = TextureLoader.load(gl, "tex/cake_COLOR.tga");
			sponge_wall_normal   = TextureLoader.load(gl, "tex/cake_NRM.tga"  );
			sponge_wall_height   = TextureLoader.load(gl, "tex/chocolate_cake_DISP.jpg" );
			sponge_wall_specular = TextureLoader.load(gl, "tex/cake_SPEC.tga"  );
			
			choco_bar_normal = TextureLoader.load(gl, "tex/chocolate_bevel.png");
			choco_bar_height = TextureLoader.load(gl, "tex/chocolate_bevel_DISP.png");
			
			choco_wrap_diffuse  = TextureLoader.load(gl, "tex/chocolate_wrapper.png");
			choco_wrap_specular = TextureLoader.load(gl, "tex/chocolate_wrapper_SPEC.png");
			choco_wrap_alt      = TextureLoader.load(gl, "tex/chocolate_wrapper_ALT.png");
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
		shader.setSampler(gl, "specular_map", 4);
		
		shader.setUniform(gl, "enableParallax", false);
		shader.setUniform(gl, "enable_spec_map", Scene.enableParallax);
		
		Shader.disable(gl);
		
		if(displayModel)
		{
			cake_top_upper_node.render(gl, shader, null); 
			cake_top_lower_node.render(gl, shader, null);

			sponge_wall_upper_node.render(gl);
			sponge_wall_lower_node.render(gl);
			
			milk_choco_bar_node.render(gl);
			white_choco_bar_node.render(gl);
			dark_choco_bar_node.render(gl);
			
			milk_choco_wrap_node.render(gl);
			mint_choco_wrap_node.render(gl);

			if(Scene.enableInstanced) CheckeredCookie.renderSimplified(gl);
			else for(CheckeredCookie cookie : cookies) cookie.render(gl);
			for(MushroomBlock block : mush_blocks) block.render(gl);
			
			for(GiftBox box : gift_boxes) box.render(gl);
			
			DonutBlock.renderSimplified(gl);
		}
	}
	
	public List<OBB> getBounds() { return bounds; }
}
