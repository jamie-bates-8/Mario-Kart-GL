package bates.jamie.graphics.scene;

import bates.jamie.graphics.util.TextureLoader;


import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;


public class TextureMaker {
	/** Texture Fields **/
	private Texture brick_front;
	private Texture brick_side;
	
	private Texture brick_front_normal;
	private Texture brick_side_normal;
	
	private Texture brick_front_height;
	private Texture brick_side_height;

	private Texture floor_normal;
	private Texture floor_colour;
	private Texture floor_height;
	
	private Texture rain_normal;
	private Texture pattern_mask;
	
	private Texture brickColour;
	private Texture brickNormal;
	private Texture brickHeight;
	
	
	public void makeTexture(GL2 gl) {
	
		
		try {
			this.brick_front = TextureLoader.load(gl, "tex/brick_front.png");
			this.brick_side  = TextureLoader.load(gl, "tex/brick_side.png");
		
			this.brick_front_normal = TextureLoader.load(gl, "tex/brick_front_normal.png");
			this.brick_side_normal  = TextureLoader.load(gl, "tex/brick_side_normal.png");
		
			this.brick_front_height = TextureLoader.load(gl, "tex/brick_front_height.png"); 
			this.brick_side_height  = TextureLoader.load(gl, "tex/brick_side_height.png");
		
			//   floor_normal = TextureLoader.load(gl, "tex/bump_maps/brick_parallax.png");
			//   floor_colour = TextureLoader.load(gl, "tex/brick_color.jpg");
			this.floor_normal = TextureLoader.load(gl, "tex/rock_mine_NRM.jpg");
			this.floor_colour = TextureLoader.load(gl, "tex/rock_mine.jpg");
			this.floor_height = TextureLoader.load(gl, "tex/rock_mine_DISP.jpg");
		
			//	 rain_normal = TextureLoader.load(gl, "tex/bump_maps/large_stone.jpg");
			this.rain_normal  = TextureLoader.load(gl, "tex/bump_maps/noise.jpg");
			this.pattern_mask = TextureLoader.load(gl, "tex/slope_mask.jpg");
		
			this.brickColour = TextureLoader.load(gl, "tex/brick_colour.png");
			this.brickNormal = TextureLoader.load(gl, "tex/brick_normal.png");
			this.brickHeight = TextureLoader.load(gl, "tex/brick_height.png");
		}
		catch (Exception e) { e.printStackTrace(); }

	    try {
			Scene.brick_front = this.brick_front;
			Scene.brick_side  = this.brick_side;
		
			Scene.brick_front_normal = this.brick_front_normal;
			Scene.brick_side_normal  = this.brick_side_normal;
		
			Scene.brick_front_height = this.brick_front_height; 
			Scene.brick_side_height  = this.brick_side_height;
		
			//	  floor_normal = TextureLoader.load(gl, "tex/bump_maps/brick_parallax.png");
			//	  floor_colour = TextureLoader.load(gl, "tex/brick_color.jpg");
			Scene.floor_normal = this.floor_normal;
			Scene.floor_colour = this.floor_colour;
			Scene.floor_height = this.floor_height;
		
			//	  rain_normal = TextureLoader.load(gl, "tex/bump_maps/large_stone.jpg");
			Scene.rain_normal  = this.rain_normal;
			Scene.pattern_mask = this.pattern_mask;
		
			Scene.brickColour = this.brickColour;
			Scene.brickNormal = this.brickNormal;
			Scene.brickHeight = this.brickHeight;
	
	}
	
	catch (Exception e) { e.printStackTrace(); }
	}
}
