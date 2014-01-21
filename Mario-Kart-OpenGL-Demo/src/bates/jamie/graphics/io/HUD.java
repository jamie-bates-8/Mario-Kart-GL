package bates.jamie.graphics.io;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;
import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_DEPTH_TEST;
import static javax.media.opengl.GL.GL_LINES;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL2.GL_QUADS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.io.File;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.entity.Terrain;
import bates.jamie.graphics.item.ItemRoulette;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.TimeQuery;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.Vector;

import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class HUD
{
	private Scene scene;
	private Car car;
	
	private boolean visible = true;
	public boolean smooth = false;
	
	private static final int BUFFER_SIZE = 3;
	private String[] messageBuffer = new String[BUFFER_SIZE];
	private int messageIndex = 0;
	
	private static Texture speedometer;
	
	private TextRenderer renderer;
	private Color textColor = Color.WHITE;
	
	private float yStretch = 1; //stretching factor for y-axis of FT graph
	
	private static final float STRETCH_INC = 0.25f;
	private static final float MIN_STRETCH = 0.25f;
	private static final float MAX_STRETCH = 8.00f;
	
	private static final int LINE_SPACING = 20;
	private static final int LEFT_MARGIN  = 20;
	
	public GraphMode mode = GraphMode.RENDER_TIMES;
	public int emphasizedComponent = 0; 
	
	static
	{
		try { speedometer = TextureIO.newTexture(new File("tex/speedometer.png"), true); }
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public HUD(Scene scene, Car car)
	{
		this.scene = scene;
		this.car = car;
		
		Font font = new Font("Calibri", Font.PLAIN, 15);
		renderer = new TextRenderer(font, true, false);
	}
	
	public void broadcast(String message)
	{
		messageBuffer[messageIndex] = message;
		
		messageIndex++;
		messageIndex %= BUFFER_SIZE;
	}
	
	public void increaseStretch() { if(yStretch < MAX_STRETCH) yStretch += STRETCH_INC; }
	public void decreaseStretch() { if(yStretch > MIN_STRETCH) yStretch -= STRETCH_INC; }
	
	public void cycleGraphMode() { mode = GraphMode.cycle(mode); }
	
	public boolean getVisibility() { return visible; }
	public void setVisibility(boolean visible) { this.visible = visible; }
	
	public long render(GL2 gl, GLU glu)
	{
		long start = System.nanoTime();
		
		if(smooth) gl.glEnable(GL2.GL_LINE_SMOOTH);
		
		ortho2DBegin(gl, glu);
		
		gl.glDisable(GL_LIGHTING);
	
//		renderSpeedometer(gl, car);
			
		ItemRoulette roulette = car.getRoulette();
		roulette.cursed = car.isCursed();
		if(roulette.isAlive()) roulette.render(gl);
			
		if(scene.enableRetical && car.camera.isAerial()) renderRetical(gl);
			
		if(visible)	
		{
			renderText(car);
			
			switch(mode)
			{
				case RENDER_TIMES: renderFrameTimes(gl); break;
				case RENDER_TIME_COMPONENTS: renderFrameTimeComponents(gl); break;
				case UPDATE_TIMES: renderUpdateTimes(gl); break;
				case UPDATE_TIME_COMPONENTS: renderUpdateTimeComponents(gl); break;
			}
		}
		
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		
		ortho2DEnd(gl);
		
		gl.glEnable (GL_LIGHTING);
		gl.glDisable(GL2.GL_LINE_SMOOTH);
		
		return System.nanoTime() - start;
	}

	private void renderSpeedometer(GL2 gl, Car car)
	{
		gl.glEnable(GL_TEXTURE_2D);
		gl.glEnable(GL_BLEND);
		
		gl.glColor3f(1, 1, 1);

		speedometer.bind(gl);
		
		int width  = scene.getWidth();
		int height = scene.getHeight();
		
		gl.glBegin(GL_QUADS);
		{
			gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2f(width - 250, height - 200);
			gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2f(width - 250, height      );
			gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2f(width -  50, height      );
			gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2f(width -  50, height - 200);
		}
		gl.glEnd();
		
		gl.glDisable(GL_BLEND);
		
		double speedRatio = abs(car.velocity) / (2 * Car.TOP_SPEED);
		float dialRotation = (float) ((speedRatio * 240) + 60);
		
		gl.glDisable(GL_TEXTURE_2D);
		
		gl.glPushMatrix();
		{
			gl.glTranslatef(width - 150, height - 100, 0);
			gl.glRotatef(dialRotation, 0.0f, 0.0f, 1.0f);

			gl.glBegin(GL_QUADS);
			{
				gl.glColor3f(1, 0, 0);

				gl.glVertex2f(  0, -10);
				gl.glVertex2f(-10,   0);
				gl.glVertex2f(  0, 100);
				gl.glVertex2f( 10,   0);
			}
			gl.glEnd();
		}
		gl.glPopMatrix();
		
		gl.glEnable(GL_TEXTURE_2D);
		gl.glColor3f(1, 1, 1);
	}

	private void renderRetical(GL2 gl)
	{
		Point point = scene.canvas.getMousePosition();
		
		if(point == null) return;
		
		double x = point.getX();
		double y = point.getY();
		
		gl.glDisable(GL_TEXTURE_2D);
		
		float r = scene.retical; 
		int v = (int) r;
		
		float[][] vertices = new float[v][3];
		
		for(int i = 0; i < v; i ++)
		{
			double theta = toRadians(i * (360.0 / v));
			vertices[i] = new float[] {(float) (r * cos( theta)), 0, (float) (r * sin( theta))};
			vertices[i] = Vector.add(vertices[i], new float[] {(float) x, 0, (float) y});
		}
		
		if(scene.mousePressed)
		{
			float[] c = (scene.rightClick) ? RGB.BLUE : RGB.ORANGE;
			c = Vector.multiply(c, 1.0f / 255);
			gl.glColor3f(c[0], c[1], c[2]);
		}
		else gl.glColor3f(0, 0, 0);
		
		gl.glPointParameterfv(GL2.GL_POINT_DISTANCE_ATTENUATION, new float[] {1, 0, 0}, 0);
		gl.glPointSize(3);
		
		gl.glPushMatrix();
		{		
			gl.glBegin(GL2.GL_POINTS);
			{
				for(int i = 0; i < v; i ++)
					gl.glVertex2f(vertices[i][0], vertices[i][2]);
			}
			gl.glEnd();
		}
		gl.glPopMatrix();
		
		gl.glEnable(GL_TEXTURE_2D);
		gl.glColor3f(1, 1, 1);
	}
	
	/**
	 * This method renders text on the screen to describe the state of the scene
	 * (FPS, number of world items, number of particles in the scene...) in
	 * addition to the state of the car passed as a parameter (such as the position,
	 * velocity, turn rate...)
	 */
	private void renderText(Car car)
	{
		renderer.beginRendering(scene.getWidth(), scene.getHeight());
		renderer.setSmoothing(true);
		renderer.setColor(textColor);
		
		int y = scene.getHeight();
		
		renderer.draw("FPS: " + scene.getFrameRate(), LEFT_MARGIN, y - 160);
		
		Terrain terrain = scene.getTerrain();
		
		renderer.draw("Items: "    + scene.getItems().size(),     LEFT_MARGIN, y - 200);
		renderer.draw("Particle: " + scene.getParticles().size(), LEFT_MARGIN, y - 200 - LINE_SPACING * 1);
		
		if(scene.enableTerrain)
		{
			renderer.draw("Foliage: "  + scene.foliage.size(),        LEFT_MARGIN, y - 200 - LINE_SPACING * 2);
			renderer.draw("LOD: "      + terrain.tree.detail,         LEFT_MARGIN, y - 200 - LINE_SPACING * 3);
			renderer.draw("Cells: "    + terrain.tree.cellCount(),    LEFT_MARGIN, y - 200 - LINE_SPACING * 4);
			renderer.draw("Vertices: " + terrain.tree.vertexCount(),  LEFT_MARGIN, y - 200 - LINE_SPACING * 5);
		}
		
		String weather = scene.enableBlizzard ? scene.blizzard.type.toString() : "Off";
		if(scene.enableBlizzard)
			weather += String.format(" (%d%%)", (scene.blizzard.flakes.size() * 100) / scene.blizzard.flakeLimit);
		
		renderer.draw("Weather: " + weather, LEFT_MARGIN, y - 200 - LINE_SPACING * 6);
		
		renderer.draw("Bloom Mode: " + scene.bloom.getDisplayMode(), LEFT_MARGIN, y - 200 - LINE_SPACING * 7);
		
		renderer.draw("Quadtree Render: " + scene.getTerrain().tree.timeQuery.getPrevious(), LEFT_MARGIN, y - 200 - LINE_SPACING * 8);
//		renderer.draw("Banana Render: " + TimeQuery.getCache()[Scene.frameIndex][TimeQuery.ITEM_ID], 40, y - 490);
		renderer.draw("Brick Render: " + scene.brickWall.timeQuery.getPrevious(), LEFT_MARGIN, y - 200 - LINE_SPACING * 9);
		renderer.draw("Occlusion: " + (Scene.enableOcclusion ? "Enabled" : "Disabled"), LEFT_MARGIN, y - 200 - LINE_SPACING * 10);
		renderer.draw("Light: " + scene.lightID, LEFT_MARGIN, y - 200 - LINE_SPACING * 11);
		
		renderer.draw("Plane Render: " + scene.planeMesh.timeQuery.getPrevious(), LEFT_MARGIN, y - 200 - LINE_SPACING * 12);
		
		Vec3 p = car.getPosition();
		
		int x = scene.getWidth() - 200;
		
		renderer.draw("Bananas: " + Scene.bananasRendered, x, 610);
		
		renderer.draw("Parallax: " + Scene.enableParallax, x, 580);
		
		renderer.draw("Zoom: " + String.format("%.2f", car.camera.zoom), x, 550);
		renderer.draw("Azimuth: " + String.format("%.1f", Math.toDegrees(car.camera.azimuth)), x, 520);
		renderer.draw("Incline: " + String.format("%.1f", Math.toDegrees(car.camera.incline)), x, 490);
		
		renderer.draw("Vehicle Render: " + TimeQuery.getCache()[Scene.frameIndex][TimeQuery.VEHICLE_ID], x, 460);
		
		renderer.draw("Colliding: "   + car.colliding, x, 430);
		renderer.draw("Falling: "     + car.falling,   x, 400);
		
		renderer.draw("Turn Rate: "   + String.format("%.2f", car.turnRate), x, 340);
		
		renderer.draw("x: " + String.format("%.2f", p.x), x, 280);
		renderer.draw("y: " + String.format("%.2f", p.y), x, 250);
		renderer.draw("z: " + String.format("%.2f", p.z), x, 220);
		
		renderer.draw("Velocity: " + String.format("%.2f", car.velocity), x, 50);
		renderer.draw("Distance: " + (int) car.distance + " m", x, 20);
		
		for(int i = 0; i < BUFFER_SIZE; i++)
		{
			String message = messageBuffer[i] == null ? "" : messageBuffer[i];
			renderer.draw(message, 150, y - ((i + 1) * 30));
		}
		
		renderer.draw("Graph Mode: " + mode + (mode == GraphMode.RENDER_TIME_COMPONENTS ?
			(" (" + Scene.RENDER_HEADERS[emphasizedComponent] + ")") : ""), 50, 20);
		
		renderer.endRendering();
	}

	private void renderFrameTimes(GL2 gl)
	{
		int frame = Scene.frameIndex;
		long[] times = scene.frameTimes;
		
		gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glDisable(GL2.GL_LIGHTING);
		
		gl.glBegin(GL_LINES);
		{
			for(int i = 0; i < times.length; i++)
			{
				float[] color1, color2;
				
				     if(i == frame    ) color1 = color2 = RGB.VIOLET;
				else if(i == frame - 1) color1 = color2 = RGB.INDIGO;
				else if(i == frame - 2) color1 = color2 = RGB.BLUE;
				
				else if(times[i] < (1000.0 / Scene.FPS))
				{
					color1 = RGB.GREEN; color2 = RGB.LIME_GREEN;
				}
				else if(times[i] < (1000.0 / Scene.MIN_FPS))
				{
					color1 = RGB.ORANGE; color2 = RGB.YELLOW;
				}
				else
				{
					color1 = RGB.DARK_RED; color2 = RGB.RED;
				}
				     
				gl.glColor3f(color1[0]/255, color1[1]/255, color1[2]/255);
				gl.glVertex2f(50 + (i * 2), scene.getHeight() - 50);
				
				gl.glColor3f(color2[0]/255, color2[1]/255, color2[2]/255);
				gl.glVertex2f(50 + (i * 2), scene.getHeight() - 50 - (times[i] * yStretch));
			}
		}
		gl.glEnd();
	}
	
	private void renderFrameTimeComponents(GL2 gl)
	{
		gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glDisable(GL2.GL_LIGHTING);
		
		int frameIndex = Scene.frameIndex;
		long[][] renderTimes = scene.renderTimes;
		
		int height = scene.getHeight();
		
		float[][] colors = {RGB.RED, RGB.ORANGE, RGB.YELLOW, RGB.GREEN, RGB.BLUE, RGB.INDIGO, RGB.PLUM};
		float[] color = {};
		
		gl.glBegin(GL_LINES);
		{
			for(int i = 0; i < renderTimes.length; i++)
			{
				float y = height - 50;
				
				for(int j = 0; j < renderTimes[0].length; j++)
				{
				         if(i == frameIndex    ) color = RGB.BLACK;
				    else if(i == frameIndex - 1) color = RGB.DARK_GRAY;
					else if(i == frameIndex - 2) color = RGB.GRAY;
					
					else if(j == emphasizedComponent) color = RGB.WHITE;
					
					else color = colors[j];
					
					gl.glColor3f(color[0]/255, color[1]/255, color[2]/255);
				
					gl.glVertex2f(50 + (i * 2), y);
					gl.glVertex2f(50 + (i * 2), y -= (renderTimes[i][j] / 1E6 * yStretch));
				}	
			}
		}
		gl.glEnd();
	}
	
	private void renderUpdateTimeComponents(GL2 gl)
	{
		int frameIndex = Scene.frameIndex;
		long[][] times = scene.updateTimes;
		
		int height = scene.getHeight();
		
		float[][] colors = {RGB.GREEN, RGB.BLUE, RGB.INDIGO, RGB.VIOLET};
		float[] color = {};
		
		gl.glBegin(GL_LINES);
		{
			for(int i = 0; i < times.length; i++)
			{
				float y = height - 50;
				
				for(int j = 0; j < times[0].length; j++)
				{
				         if(i == frameIndex    ) color = RGB.RED;
				    else if(i == frameIndex - 1) color = RGB.ORANGE;
					else if(i == frameIndex - 2) color = RGB.YELLOW;
					
					else color = colors[j];
					
					gl.glColor3f(color[0]/255, color[1]/255, color[2]/255);
				
					gl.glVertex2f(50 + (i * 2), y);
					gl.glVertex2f(50 + (i * 2), y -= (times[i][j] / 1E6 * yStretch));
				}	
			}
		}
		gl.glEnd();
	}
	
	private void renderUpdateTimes(GL2 gl)
	{
		int frame = Scene.frameIndex;
		long[][] times = scene.updateTimes;
		
		int y = scene.getHeight() - 50;
		
		gl.glBegin(GL_LINES);
		{
			for(int i = 0; i < times.length; i++)
			{
				float time = (float) (Vector.sum(times[i]) / 1E6);
				
				float[] color1, color2;
				
				     if(i == frame    ) color1 = color2 = RGB.VIOLET;
				else if(i == frame - 1) color1 = color2 = RGB.INDIGO;
				else if(i == frame - 2) color1 = color2 = RGB.BLUE;
				
				else if(time < (200.0 / Scene.FPS))
				{
					color1 = RGB.GREEN; color2 = RGB.LIME_GREEN;
				}
				else if(time < (200.0 / Scene.MIN_FPS))
				{
					color1 = RGB.ORANGE; color2 = RGB.YELLOW;
				}
				else
				{
					color1 = RGB.DARK_RED; color2 = RGB.RED;
				}
				     
				gl.glColor3f(color1[0]/255, color1[1]/255, color1[2]/255);
				gl.glVertex2f(50 + (i * 2), y);
				
				gl.glColor3f(color2[0]/255, color2[1]/255, color2[2]/255);
				gl.glVertex2f(50 + (i * 2), y - time * yStretch);
			}
		}
		gl.glEnd();
	}
	
	public void setTextColor(Color color) { textColor = color; }
	
	public void nextComponent()
	{
		emphasizedComponent++;
		emphasizedComponent %= scene.renderTimes[0].length;
	}
	
	/**
	 * Switches the matrix mode from Model View, normally used to render 3D models
	 * without the virtual environment, to Projection, which allows 2D graphics to
	 * be drawn as an overlay on the screen. In particular, it can be used to render
	 * a HUD (heads-up display) 
	 */
	private void ortho2DBegin(GL2 gl, GLU glu)
	{	
	    gl.glMatrixMode(GL_PROJECTION);
	    gl.glLoadIdentity();
	    glu.gluOrtho2D(0, scene.getWidth(), scene.getHeight(), 0);
	    
	    gl.glMatrixMode(GL_MODELVIEW);
	    gl.glLoadIdentity();
	    gl.glDisable(GL_DEPTH_TEST);
	}
	
	/**
	 * Switches the matrix mode from Projection to Model View, allowing 3D models
	 * to be rendered normally in the virtual environment.
	 */
	private void ortho2DEnd(GL2 gl)
	{
		scene.resetView(gl);
		
		gl.glEnable(GL_DEPTH_TEST);
	}

	public enum GraphMode
	{
		RENDER_TIMES,
		RENDER_TIME_COMPONENTS,
		UPDATE_TIMES,
		UPDATE_TIME_COMPONENTS;
		
		public static GraphMode cycle(GraphMode mode)
		{
			return values()[(mode.ordinal() + 1) % values().length];
		}
		
		@Override
		public String toString()
		{
			switch(this)
			{
				case RENDER_TIMES:
				case RENDER_TIME_COMPONENTS: return "Render Times";
				case UPDATE_TIMES:           return "Update Times";
				case UPDATE_TIME_COMPONENTS: return "Update Times (Components)";
			}
			
			return name();
		}
	}
}
