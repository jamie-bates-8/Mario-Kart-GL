/** Utility imports **/
import static graphics.util.Renderer.*;

/** Native Java imports **/
import graphics.util.Face;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import java.io.File;
import java.util.List;
import static java.lang.Math.*;

/** OpenGL (JOGL) imports **/
import static javax.media.opengl.GL2.*;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;


/**
 * @author Jamie Bates
 * @version 1.0 (22/02/2012)
 * 
 * This class creates a 3D scene which displays a car that can be moved along a
 * track by the use of hotkeys. Users can also interact with the car model and
 * manipulation the scene in a number of ways.
 */
public class ScalextricScene extends Frame implements GLEventListener, KeyListener, MouseWheelListener
{
	private static final long serialVersionUID = 1L;
	private int canvasWidth = 840;
	private int canvasHeight = 680;
	private static final int FPS = 60;
	private final FPSAnimator animator;
	private GLU glu;
	
	private boolean enableAnimation = true;
	
	private int frames = 0;
	private int frameRate = 0;
	private long startTime = System.currentTimeMillis();
	
	private Texture speedometer;
	private TextRenderer renderer;
	
	
	/** Model Fields **/
	private boolean displayModels = true;
	
	private List<Face> trackFaces;
	private List<Face> environmentFaces;
	
	private int environmentList;
	private int trackList;
	
	private Scalextric car;
	
	
	/** Camera Fields **/
	private CameraMode camera = CameraMode.DYNAMIC_VIEW;
	
	private float xRotation_Camera = 0.0f;				
	private float yRotation_Camera = 0.0f;
	private float zRotation_Camera = 0.0f;
	
	private float zoom = -20.0f;
	
	
	/** Fog Fields **/
	private float fogDensity = 0.01f;
	private float[] fogColor = {1.0f, 1.0f, 1.0f, 1.0f};
	
	
	/** Lighting Fields **/
	private float[] global_specular = {1.0f, 1.0f, 1.0f};
	private float[] global_ambience = {0.8f, 0.8f, 0.8f, 1.0f};
	
	private float[] position = {0.0f, 20.0f, 0.0f, 1.0f};
	
    private float[] material_ambience  = {0.7f, 0.7f, 0.7f, 1.0f};
    private float[] material_shininess = {100.0f};
	
    
    /** Environment Fields **/
    private boolean enableSkybox = true;
    
	
	private BufferedImage motionLog;
	private static final int LOG_SCALE = 1;
	
	public static final float[] ORIGIN = {0.0f, 0.0f, 0.0f};
	
	
	public ScalextricScene()
	{
		GLCanvas canvas = new GLCanvas();
		canvas.setPreferredSize(new Dimension(canvasWidth, canvasHeight));
		this.add(canvas);
		canvas.addGLEventListener(this);
		canvas.addKeyListener(this);
		canvas.addMouseWheelListener(this);
		canvas.setFocusable(true);
		canvas.requestFocus();

		animator = new FPSAnimator(canvas, FPS, true);
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				new Thread()
				{
					public void run()
					{
						animator.stop();
						System.exit(0);
					}
				}.start();
			}
		});

		pack();
		setTitle("Computer Graphics Assignment");
		setVisible(true);
		animator.start();
	}
	
	public static void main(String[] args)
	{
		new ScalextricScene();
	}
	
	public void init(GLAutoDrawable drawable)
	{	
		Font font = new Font("Calibri", Font.PLAIN, 24);
		renderer = new TextRenderer(font);
		
		try
		{
			speedometer  = TextureIO.newTexture(new File("tex/speedometer.png"), true);
		}
		catch (Exception e) { e.printStackTrace(); }
		
		GL2 gl = drawable.getGL().getGL2();
		glu = new GLU();

		gl.glShadeModel(GL_SMOOTH);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		
		gl.glClearDepth(1.0f);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

		/** Texture Options **/
		gl.glEnable(GL2.GL_TEXTURE_2D);
		
		try
		{
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,     GL_REPEAT);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,     GL_REPEAT);
		}
		catch (GLException e) { e.printStackTrace(); }
		

		/** Fog Setup **/
		gl.glEnable(GL_FOG);
		gl.glFogi  (GL_FOG_MODE, GL_EXP2);
		gl.glFogfv (GL_FOG_COLOR, fogColor, 0);
		gl.glFogf  (GL_FOG_DENSITY, fogDensity);
		gl.glHint  (GL_FOG_HINT, GL_NICEST);
		
		
		/** Lighting Setup **/
		gl.glEnable(GL_LIGHTING);
	    gl.glEnable(GL_LIGHT0);
		
	    
		gl.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
		

		/** Model Setup **/
		trackFaces       = OBJParser.parseTriangles("obj/track.obj");
		environmentFaces = OBJParser.parseTriangles("obj/environment.obj");
	    
	    environmentList = gl.glGenLists(1);
	    gl.glNewList(environmentList, GL2.GL_COMPILE_AND_EXECUTE);
	    displayTexturedObject(gl, environmentFaces);
	    gl.glEndList();
	    
	    trackList = gl.glGenLists(1);
	    gl.glNewList(trackList, GL2.GL_COMPILE_AND_EXECUTE);
	    displayTexturedObject(gl, trackFaces);
	    gl.glEndList();
	    
	    car = new Scalextric(gl, new float[] {0, 0, 0}, 0, 0, 0);
	    
	    motionLog = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);	    
	}
	
	public void display(GLAutoDrawable drawable)
	{			
		GL2 gl = drawable.getGL().getGL2();

		gl.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		gl.glLoadIdentity();

		gl.glMatrixMode(GL_MODELVIEW);

		setupCamera(gl);
		
		setupLights(gl);
		
		if(enableAnimation)
			if(camera != CameraMode.MODEL_VIEW) car.drive();

		recordTracks();
		
		long t0 = System.currentTimeMillis();
		
		if(displayModels) render3DModels(gl);
		
		System.out.println("Total Model Rendering Time: " + (System.currentTimeMillis() - t0));
		t0 = System.currentTimeMillis();
		
		if(camera != CameraMode.MODEL_VIEW) renderHUD(drawable, gl);
		
		System.out.println("Total HUD Rendering Time: " + (System.currentTimeMillis() - t0));
		System.out.println();

		calculateFPS();
	}
	
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		GL2 gl = drawable.getGL().getGL2();
	
		if (height <= 0) height = 1;
		
		canvasHeight = height;
		canvasWidth = width;
		
		final float ratio = (float) width / (float) height;
		
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(100.0f, ratio, 2.0, 300.0);
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	public void dispose(GLAutoDrawable drawable) {}
	
	private void recordTracks()
	{
		int xCentre = (motionLog.getWidth()  / 2);
		int zCentre = (motionLog.getHeight() / 2);

		int x = (int) (xCentre - ((car.x / 120) * xCentre));
		int z = (int) (zCentre - ((car.z / 120) * zCentre));

		try { motionLog.setRGB(x / LOG_SCALE, z / LOG_SCALE, Color.BLUE.getRGB()); }
		catch(Exception e) {}
	}
	
	private void renderHUD(GLAutoDrawable drawable, GL2 gl)
	{
		ortho2DBegin(gl);

		gl.glEnable(GL_TEXTURE_2D);
		gl.glEnable(GL_BLEND);

		speedometer.bind(gl);
		
		gl.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
		
		gl.glBegin(GL_QUADS);
		{
			gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2f(canvasWidth - 250, canvasHeight - 200);
			gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2f(canvasWidth - 250, canvasHeight      );
			gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2f(canvasWidth -  50, canvasHeight      );
			gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2f(canvasWidth -  50, canvasHeight - 200);
		}
		gl.glEnd();
		
		gl.glDisable(GL_BLEND);
		
		double speedRatio = abs(car.velocity) / (2 * Car.TOP_SPEED);
		float zRotation_Meter = (float) ((speedRatio * 240) + 60);
		
		gl.glTranslatef(canvasWidth - 150, canvasHeight - 100, 0);
		gl.glRotatef(zRotation_Meter, 0.0f, 0.0f, 1.0f);
		
		gl.glDisable(GL_TEXTURE_2D);
		gl.glDisable(GL_LIGHTING);
		
		gl.glBegin(GL_QUADS);
		{
			gl.glColor3f(1.0f, 0.0f, 0.0f);
			
			gl.glVertex2f(0, -10);
			gl.glVertex2f(-10, 0);
			gl.glVertex2f(0, 100);
			gl.glVertex2f(10, 0);
		}
		gl.glEnd();
		
		gl.glEnable(GL_TEXTURE_2D);
		gl.glEnable(GL_LIGHTING);
		
		
		renderer.beginRendering(drawable.getWidth(), drawable.getHeight());
		renderer.setSmoothing(true);
		
		renderer.draw("Distance: " + (int) car.distance + " m", 40, 40);
		renderer.draw("FPS: " + frameRate, 40, 80);
		
		renderer.draw("x: " + String.format("%.2f", car.x), 40, 200);
		renderer.draw("y: " + String.format("%.2f", car.y), 40, 160);
		renderer.draw("z: " + String.format("%.2f", car.z), 40, 120);
		
		renderer.endRendering();
		
		gl.glViewport(canvasWidth - 100, canvasHeight - 100, 100, 100);
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(-50, 50, -50, 50, 1, 50);
		glu.gluLookAt(0, 40, 0,
			          0, 0, 0,
			          0, 0, 1);
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity();
		
		displayMap(gl);
		
		ortho2DEnd(gl);
	}

	private void setupCamera(GL2 gl)
	{
		switch(camera)
		{	
			//Setup the camera so that it can view a stationary model of the car from various angles
			case MODEL_VIEW:
			{
				car.displayModel = true;
				
				glu.gluLookAt(0, 5, zoom,
							  0, 0, 0,
						      0, 1, 0);
				
				gl.glRotatef(xRotation_Camera, 1.0f, 0.0f, 0.0f);
				gl.glRotatef(yRotation_Camera, 0.0f, 1.0f, 0.0f);
				gl.glRotatef(zRotation_Camera, 0.0f, 0.0f, 1.0f);
				
				break;
			}
			//Cause the camera to follow the car dynamically as it moves along the track 
			case DYNAMIC_VIEW:
			{
				float[] p = car.getPosition();
				
				glu.gluLookAt(p[0], p[1] + 10, p[2] - 20,
							  p[0], p[1], p[2],
						      0, 1, 0);
				break;
			}
			//Focus the camera on the centre of the track from a bird’s eye view
			case BIRDS_EYE_VIEW:
			{
				gl.glMatrixMode(GL_PROJECTION);
				gl.glLoadIdentity();
				gl.glOrtho(-50, 50, -50, 50, 1, 50);
				glu.gluLookAt(0, 40, 0,
					          0, 0, 0,
					          0, 0, 1);
				gl.glMatrixMode(GL_MODELVIEW);
				gl.glLoadIdentity();

				break;
			}
			//Setup the camera to view the scene from the driver's perspective
			case DRIVERS_VIEW:
			{
				car.displayModel = false;
				
				float[] p = car.getPosition();
				
				gl.glTranslatef(0, -1.5f, 0);
				
				gl.glRotatef(car.xr, 1.0f, 0.0f, 0.0f);
				gl.glRotatef(car.yr, 0.0f, 1.0f, 0.0f);
				gl.glRotatef(car.zr, 0.0f, 0.0f, 1.0f);
				
				glu.gluLookAt(p[0], p[1], p[2],
							  p[0] - 10, p[1], p[2],
					          0, 1, 0);
				
				break;
			}
			
			default: break;
		}
	}

	private void setupLights(GL2 gl)
	{
	    gl.glLightfv(GL_LIGHT0, GL_SPECULAR, global_specular, 0);
	    gl.glLightfv(GL_LIGHT0, GL_POSITION, position, 0);
	    gl.glLightModelfv(GL_LIGHT_MODEL_AMBIENT, global_ambience, 0);
	        
	    gl.glMaterialfv(GL_FRONT, GL_AMBIENT, material_ambience, 0);
	    gl.glMaterialfv(GL_FRONT, GL_SHININESS, material_shininess, 0);
	}

	private void calculateFPS()
	{
		frames++;
		
		long timeElapsed = System.currentTimeMillis() - startTime;
		
		if(timeElapsed > 1000)
		{
			frameRate = frames;
			
			frames = 0;
			startTime = System.currentTimeMillis();
		}
	}

	private void displayMap(GL2 gl)
	{
		
	}

	private void render3DModels(GL2 gl)
	{
		long t0 = System.currentTimeMillis();
		
		gl.glPushMatrix();
		{
			/*******************************
			 * Environment Transformations *
			 *******************************/
			gl.glTranslatef(0, -2.1f, 0);
			gl.glScalef(15.0f, 15.0f, 15.0f);
			
			if(enableSkybox)
				gl.glCallList(environmentList);
		}	
		gl.glPopMatrix();
		
		System.out.println("\t" + (System.currentTimeMillis() - t0));
		t0 = System.currentTimeMillis();
		
		gl.glPushMatrix();
		{
			/*************************
			 * Track Transformations *
			 *************************/
			gl.glTranslatef(0, -0.25f, 0);
			gl.glScalef(11.0f, 11.0f, 11.0f);
			
			//Disable the track in model view mode
			if(camera != CameraMode.MODEL_VIEW)
				gl.glCallList(trackList);
		}
		gl.glPopMatrix();
		
		System.out.println("\t" + (System.currentTimeMillis() - t0));
		t0 = System.currentTimeMillis();

		car.render(gl);
	}
	
	private void ortho2DBegin(GL2 gl)
	{
	    gl.glMatrixMode(GL_PROJECTION);
	    gl.glLoadIdentity();
	    glu.gluOrtho2D(0, canvasWidth, canvasHeight, 0);
	    
	    gl.glMatrixMode(GL_MODELVIEW);
	    gl.glLoadIdentity();
	    gl.glDisable(GL_DEPTH_TEST);
	}
	
	private void ortho2DEnd(GL2 gl)
	{
		float ratio = (float) canvasWidth / (float) canvasHeight;
		gl.glViewport(0, 0, canvasWidth, canvasHeight);
		
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(100.0f, ratio, 2.0, 300.0);
		
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glEnable(GL_DEPTH_TEST);
	}
		
	public void keyPressed(KeyEvent e)
	{
		if(camera == CameraMode.MODEL_VIEW)
		{
			switch (e.getKeyCode())
			{
				case KeyEvent.VK_ESCAPE: System.exit(0); break; //Close the application
				case KeyEvent.VK_EQUALS: if(zoom < -10) zoom++; break; //Zoom in the camera
				case KeyEvent.VK_MINUS:  if(zoom > -30) zoom--; break; //Zoom out the camera
				case KeyEvent.VK_W:      car.accelerating = true; break; //Cause the car to accelerate
				case KeyEvent.VK_DOWN:   if(xRotation_Camera <  40) xRotation_Camera++; break; //Rotate camera downwards
				case KeyEvent.VK_UP:     if(xRotation_Camera > -80) xRotation_Camera--; break; //Rotate camera upwards
				case KeyEvent.VK_RIGHT:  yRotation_Camera--; break; //Rotate camera rightwards
				case KeyEvent.VK_LEFT:   yRotation_Camera++; break; //Rotate camera leftwards
				case KeyEvent.VK_E:      enableSkybox = !enableSkybox; break; //Toggle skybox on/off
				case KeyEvent.VK_M:	     switchCamera(); break; //Cycle the camera mode
					
				default: break;
			}
		}
		else
		{
			switch (e.getKeyCode())
			{
				case KeyEvent.VK_ESCAPE: System.exit(0); break; //Close the application
				case KeyEvent.VK_W:      car.accelerating = true; break; //Cause the car to accelerate
				case KeyEvent.VK_M:	     switchCamera(); break; //Cycle the camera mode
				case KeyEvent.VK_L:		 displayMotionLog(); break;
					
				default: break;
			}
		}
	}
	
	public void keyReleased(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_UP:
			case KeyEvent.VK_W:
				car.accelerating = false; break; //Cause the car to decelerate
			
			default: break;
		}
	}
	
	public void keyTyped(KeyEvent e)
	{
		switch (e.getKeyChar()) {}
	}	
	
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		if (e.getWheelRotation() < 0) { if(zoom < -10) zoom++; } //Zoom in the camera
		else if(zoom > -30) zoom--; //Zoom out the camera
	}
		
	private void displayMotionLog()
	{
		JFrame record = new JFrame();
		
		int height = motionLog.getHeight() / LOG_SCALE;
		int width = motionLog.getWidth() / LOG_SCALE;
		
		ImageIcon image = new ImageIcon(motionLog);
		JLabel label = new JLabel(image);
		
		record.setPreferredSize(new Dimension(width, height));
		record.add(label);
		record.pack();
		record.setVisible(true);
	}
	
	/**
	 * Switches the camera mode cyclically as follows:
	 * Dynamic -> Bird's Eye -> Model -> Dynamic
	 */
	private void switchCamera()
	{
		if(camera == CameraMode.MODEL_VIEW)
		{	
			//Re-enable the skybox for the dynamic view 
			enableSkybox = true; 
			
			camera = CameraMode.DYNAMIC_VIEW;
		}
		else if(camera == CameraMode.DYNAMIC_VIEW) camera = CameraMode.BIRDS_EYE_VIEW;
		else if(camera == CameraMode.BIRDS_EYE_VIEW) camera = CameraMode.DRIVERS_VIEW;
		else camera =  CameraMode.MODEL_VIEW;
	}
	
	private enum CameraMode
	{
		MODEL_VIEW,
		DYNAMIC_VIEW,
		BIRDS_EYE_VIEW,
		DRIVERS_VIEW;
	}
}
