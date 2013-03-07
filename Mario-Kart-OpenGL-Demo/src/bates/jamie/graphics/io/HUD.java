package bates.jamie.graphics.io;

import static java.lang.Math.abs;

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

import java.io.File;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.item.ItemRoulette;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.util.RGB;

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
	private Color textColor = Color.BLACK;
	
	private float yStretch = 1; //stretching factor for y-axis of FT graph
	
	private static final float STRETCH_INC = 0.25f;
	private static final float MIN_STRETCH = 0.25f;
	private static final float MAX_STRETCH = 8.00f;
	
	public GraphMode mode = GraphMode.FRAME_TIMES;
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
		
		Font font = new Font("Calibri", Font.BOLD, 16);
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
		
		if(visible)
		{
			gl.glDisable(GL_LIGHTING);
	
			renderSpeedometer(gl, car);
			
			ItemRoulette roulette = car.getRoulette();
			roulette.cursed = car.isCursed();
			if(roulette.isAlive()) roulette.render(gl);
			
			renderText(car);
			
			switch(mode)
			{
				case FRAME_TIMES: renderFrameTimes(gl); break;
				case FRAME_TIME_COMPONENTS: renderFrameTimeComponents(gl); break;
				case UPDATE_TIMES: renderCollisionTimes(gl); break;
			}
			
			gl.glEnable(GL_LIGHTING);
		}
		
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		
		ortho2DEnd(gl, glu);
		
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
		
		renderer.draw("FPS: " + scene.getFrameRate(), 40, 500);
		
		renderer.draw("Items: "    + scene.getItems().size(),     40, 440);
		renderer.draw("Particle: " + scene.getParticles().size(), 40, 410);
		renderer.draw("Foliage: "  + scene.foliage.size(),        40, 380);
		
		float[] p = car.getPosition();
		
		int x = scene.getWidth() - 200;
		
		renderer.draw("Colliding: "   + car.colliding, x, 430);
		renderer.draw("Falling: "     + car.falling,   x, 400);
		
		renderer.draw("Turn Rate: "   + String.format("%.2f", car.turnRate), x, 340);
		
		renderer.draw("x: " + String.format("%.2f", p[0]), x, 280);
		renderer.draw("y: " + String.format("%.2f", p[1]), x, 250);
		renderer.draw("z: " + String.format("%.2f", p[2]), x, 220);
		
		renderer.draw("Velocity: " + String.format("%.2f", car.velocity), x, 50);
		renderer.draw("Distance: " + (int) car.distance + " m", x, 20);
		
		for(int i = 0; i < BUFFER_SIZE; i++)
		{
			String message = messageBuffer[i] == null ? "" : messageBuffer[i];
			renderer.draw(message, 150, 620 - (i * 30));
		}
		
		renderer.draw("Graph Mode: " + mode + (mode == GraphMode.FRAME_TIME_COMPONENTS ?
			(" (" + Scene.COLUMN_HEADERS[emphasizedComponent] + ")") : ""), 50, 20);
		
		renderer.endRendering();
	}

	private void renderFrameTimes(GL2 gl)
	{
		int frame = scene.frameIndex;
		long[] times = scene.frameTimes;
		
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
		int frameIndex = scene.frameIndex;
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
	
	private void renderCollisionTimes(GL2 gl)
	{
		int frame = scene.frameIndex;
		long[] times = scene.updateTimes;
		
		float y = scene.getHeight() - 50;
		
		gl.glBegin(GL_LINES);
		{
			for(int i = 0; i < times.length; i++)
			{
				float time = (float) (times[i] / 1E6);
				
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
	private void ortho2DEnd(GL2 gl, GLU glu)
	{
		int width  = scene.getWidth();
		int height = scene.getHeight();
		
		float ratio = (float) width / (float) height;
		gl.glViewport(0, 0, width, height);
		
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(scene.fov, ratio, 1.0, 1000.0);
		
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glEnable(GL_DEPTH_TEST);
	}

	public enum GraphMode
	{
		FRAME_TIMES,
		FRAME_TIME_COMPONENTS,
		UPDATE_TIMES;
		
		public static GraphMode cycle(GraphMode mode)
		{
			return values()[(mode.ordinal() + 1) % values().length];
		}
		
		@Override
		public String toString()
		{
			switch(this)
			{
				case FRAME_TIMES:
				case FRAME_TIME_COMPONENTS: return "Frame Times";
				case UPDATE_TIMES: return "Update Times";
			}
			
			return name();
		}
	}
}
