package bates.jamie.graphics.entity;

import java.io.File;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.Light;
import bates.jamie.graphics.scene.Material;
import bates.jamie.graphics.scene.Model;
import bates.jamie.graphics.scene.Reflector;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.scene.SceneNode;
import bates.jamie.graphics.scene.SceneNode.RenderMode;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.shader.Shader;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class GoldCoin
{
	static Model      base_model = new Model("coin_base");
	static Model gold_coin_model = new Model("gold_coin");
	static Model  red_coin_model = new Model("red_coin");
	
	static Texture line_normal_map;
	static Texture star_normal_map;
	
	static Material gold_coin_mat;
	static Material red_coin_mat;
	
	public enum CoinType
	{
		GOLD_COIN(gold_coin_model, gold_coin_mat),
		 RED_COIN( red_coin_model,  red_coin_mat);
		
		public Model    model;
		public Material material;
		
		private CoinType(Model model, Material material)
		{
			this.model = model;
			this.material = material;
		}
	}
	
	private CoinType type = CoinType.GOLD_COIN;
	
	static
	{
		try
		{
			line_normal_map = TextureIO.newTexture(new File("tex/bump_maps/gold_coin_normal.png"), true);
			star_normal_map = TextureIO.newTexture(new File("tex/bump_maps/red_coin_normal.png"), true);
			
			gold_coin_mat = new Material(new float[] {1.0f, 1.0f, 0.2f}, RGB.WHITE, 16); 
			red_coin_mat  = new Material(new float[] {1.0f, 0.4f, 0.4f}, RGB.WHITE, 16); 
			gold_coin_mat.setNormalMap(line_normal_map);
			red_coin_mat .setNormalMap(star_normal_map);
			
			if(!base_model.hasTangentData()) base_model.calculateTangents();
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	SceneNode coinNode;
	
	public Reflector reflector;
	
	float rotation = 0;
	Vec3  position;
	
	boolean collected = false;
	boolean spin = true;
	
	public GoldCoin(Vec3 p, CoinType type, boolean spin)
	{
		position = p;
		
		this.type = type;
		this.spin = spin;
		
		reflector = new Reflector(1.0f);
		
		coinNode = new SceneNode(type.model);
		coinNode.setTranslation(p);
		coinNode.setScale(new Vec3(2.0));
		coinNode.setReflector(reflector);
		coinNode.setReflectivity(0.75f);
		coinNode.setRenderMode(RenderMode.BUMP_REFLECT);
		coinNode.setMaterial(type.material);
	}
	
	public void setPosition(Vec3 p)
	{
		position = p;
		
		coinNode.setTranslation(p);
	}
	
	public Vec3 getPosition() { return position; }
	
	public void render(GL2 gl)
	{	
		if(Scene.enableAnimation && spin) rotation += 2.0;
		
		float   rimPower = Light.rimPower;
		float[] rimColor = Light.rimColor;
		
		Light.rimPower = 1.0f;
		Light.rimColor = new float[] {.7f, .7f, .7f};
		
		Light.setepRimLighting(gl);
		
		if(spin) coinNode.setRotation(new Vec3(0, rotation, 0));
		else coinNode.setRotation(new Vec3(90, 0, 0));
		
		if(Scene.testMode)
		{
			coinNode.setModel(type.model);
			coinNode.setRenderMode(RenderMode.REFLECT);
		}
		else
		{
			coinNode.setModel(base_model);
			coinNode.setRenderMode(RenderMode.BUMP_REFLECT);
		}
		
		if(collected) coinNode.renderGhost(gl, 1, Shader.get("aberration"));
		else          coinNode.render(gl);
		
		Light.rimPower = rimPower;
		Light.rimColor = rimColor;
		
		Light.setepRimLighting(gl);
		
		Shader.disable(gl);
		
		gl.glColor3f(1, 1, 1);
	}

	public boolean isCollected() { return collected; }
	
	public void setCollected(boolean collected) { this.collected = collected; }
	
	
}
