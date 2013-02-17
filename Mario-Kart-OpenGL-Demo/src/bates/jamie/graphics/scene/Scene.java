package bates.jamie.graphics.scene;

import static bates.jamie.graphics.util.Renderer.displayTexturedCuboid;
import static bates.jamie.graphics.util.Renderer.displayTexturedObject;
import static javax.media.opengl.GL.GL_COLOR_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_TEST;
import static javax.media.opengl.GL.GL_LEQUAL;
import static javax.media.opengl.GL.GL_NICEST;
import static javax.media.opengl.GL.GL_REPEAT;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static javax.media.opengl.GL.GL_TEXTURE_MIN_FILTER;
import static javax.media.opengl.GL.GL_TEXTURE_WRAP_S;
import static javax.media.opengl.GL.GL_TEXTURE_WRAP_T;
import static javax.media.opengl.GL2.GL_ACCUM;
import static javax.media.opengl.GL2.GL_ACCUM_BUFFER_BIT;
import static javax.media.opengl.GL2.GL_LOAD;
import static javax.media.opengl.GL2.GL_MULT;
import static javax.media.opengl.GL2.GL_RETURN;
import static javax.media.opengl.GL2ES1.GL_EXP2;
import static javax.media.opengl.GL2ES1.GL_FOG;
import static javax.media.opengl.GL2ES1.GL_FOG_COLOR;
import static javax.media.opengl.GL2ES1.GL_FOG_DENSITY;
import static javax.media.opengl.GL2ES1.GL_FOG_END;
import static javax.media.opengl.GL2ES1.GL_FOG_HINT;
import static javax.media.opengl.GL2ES1.GL_FOG_MODE;
import static javax.media.opengl.GL2ES1.GL_FOG_START;
import static javax.media.opengl.GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUnurbs;
import javax.media.opengl.glu.GLUquadric;
import javax.media.opengl.glu.gl2.GLUgl2;
import javax.swing.JFrame;
import javax.swing.JTextField;

import bates.jamie.graphics.collision.Bound;
import bates.jamie.graphics.collision.BoundParser;
import bates.jamie.graphics.collision.OBB;
import bates.jamie.graphics.collision.Sphere;
import bates.jamie.graphics.entity.BillBoard;
import bates.jamie.graphics.entity.BlockFort;
import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.entity.Terrain;
import bates.jamie.graphics.entity.TerrainPatch;
import bates.jamie.graphics.io.Console;
import bates.jamie.graphics.io.GamePad;
import bates.jamie.graphics.item.Banana;
import bates.jamie.graphics.item.BlueShell;
import bates.jamie.graphics.item.FakeItemBox;
import bates.jamie.graphics.item.GreenShell;
import bates.jamie.graphics.item.Item;
import bates.jamie.graphics.item.ItemBox;
import bates.jamie.graphics.item.RedShell;
import bates.jamie.graphics.particle.BoostParticle;
import bates.jamie.graphics.particle.LightningParticle;
import bates.jamie.graphics.particle.Particle;
import bates.jamie.graphics.particle.ParticleGenerator;
import bates.jamie.graphics.particle.ParticleGenerator.GeneratorType;
import bates.jamie.graphics.particle.StarParticle;
import bates.jamie.graphics.sound.MP3;
import bates.jamie.graphics.util.Face;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.Renderer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

//TODO Seidel's polygon triangulation algorithm

/**
 * @author Jamie Bates
 * @version 1.0 (22/02/2012)
 * 
 * This class creates a 3D scene which displays a car that can be moved along a
 * track by the use of hotkeys. Users can also interact with the car model and 
 * manipulation the scene in a number of ways.
 */
public class Scene implements GLEventListener, KeyListener, MouseWheelListener, MouseListener, ActionListener
{
	private Console console;
	private JTextField consoleInput;
	
	private int canvasWidth = 840;
	private int canvasHeight = 680;
	
	public static final int FPS = 60;
	public static final int MIN_FPS = 30;
	
	private final FPSAnimator animator;
	
	public float fov = 100.0f;
	
	private GLU glu;
	private GLUT glut;
	
	public float[] background = {0.118f, 0.565f, 1.000f};
	
	private boolean enableAnimation = true;
	private boolean normalize = true;
	
	private Calendar execution;
	
	private int frames = 0; //frame counter for current second
	private int frameRate = FPS; //FPS of previous second
	private long startTime; //start time of current second
	private long renderTime; //time at which previous frame was rendered 
	
	public int frameIndex = 0; //time independent frame counter (for FT graph)
	public long[] frameTimes; //buffered frame times (for FT graph)
	public long[][] renderTimes; //buffered times for rendering each set of components
	public long[] collisionTimes; //buffered times for collision detection tests (for CT graph)
	
	private static final String[] COLUMN_HEADERS =
		{"Terrain", "Foliage", "Vehicles", "Items", "Particles", "Bounds", "HUD"};
	
	public boolean enableCulling = false;
	
	
	/** Texture Fields **/
	private Texture brickWall;
	private Texture brickWallTop;
	
	private Texture cobble;
	private Texture sky;

	
	/** Model Fields **/	
	private List<Face> environmentFaces;
	private List<Face> floorFaces;
	private int environmentList;
	private int floorList;
	
	private List<Car> cars = new ArrayList<Car>();
	
	public static final float[] ORIGIN = {0.0f, 0.0f, 0.0f};

	private List<ItemBox> itemBoxes = new ArrayList<ItemBox>();
	public boolean enableItemBoxes = false;
	
	
	/** Light Fields **/
	public Light light;
	
	public boolean enableLight = true;
	public boolean moveLight = false;
	public boolean enableHeadlight = false;
	
	
	/** Fog Fields **/
	public float fogDensity = 0.004f;
	public float[] fogColor = {1.0f, 1.0f, 1.0f, 1.0f};
	
	private float cloud_density = 1.0f;
    private static final float MIN_DENSITY = 0.5f;
    private static final float DENSITY_INC = 0.0125f;
	
    
    /** Environment Fields **/
    public boolean displaySkybox = true;
	
	
	/** Collision Detection Fields **/	
	private boolean enableOBBAxes          = false;
	private boolean enableOBBVertices      = false;
	private boolean enableOBBWireframes    = false;
	private boolean enableOBBSolids        = false;
	private boolean enableClosestPoints    = false;
	
	public boolean enableObstacles = false;
	
	private List<OBB> wallBounds;
	public BlockFort fort;
	
	
	/** Music Fields **/
	private boolean musicPlaying = false;
	private static final String MUTE_CITY =
			"file:///" + System.getProperty("user.dir") + "//music//Mute City.mp3";
	
	
	private List<Particle> particles = new ArrayList<Particle>();
	public List<ParticleGenerator> generators = new ArrayList<ParticleGenerator>();

	
	private Queue<Integer> itemQueue = new ArrayBlockingQueue<Integer>(100);
	private List<Item> itemList = new ArrayList<Item>();
	
	
	private int boostCounter = 0;
	public boolean enableMotionBlur = true;
	
	
	public boolean enableTerrain = true;
	
	private Terrain heightMap;
	private TerrainPatch[] terrainPatches;
	private List<BillBoard> foliage;
	
	public String terrainCommand = "";
	public static final String DEFAULT_TERRAIN = "100 1000 25 6 18 0.125 1.0";
	
	public boolean enableReflection = false;
	public float opacity = 0.75f;
	
	private float ry = 0;
	private float bezier = 0;
	private boolean rising = true;
	
	public boolean smoothBound = false;
	public boolean multiSample = true;
	public boolean linearFilter = true;
	
	public boolean testMode = false;
	public boolean printVersion = true;
	
	private int selectX = -1;
	private int selectY = -1;
	private int selected = 0;
	private IntBuffer selectBuffer;
	private static final int BUFFER_SIZE = 512;
	
	
	public Scene()
	{
		JFrame frame = new JFrame();
		
		GLCapabilities capabilities = new GLCapabilities(GLProfile.getDefault());
		capabilities.setStencilBits(8);
		capabilities.setDoubleBuffered(true);
		capabilities.setSampleBuffers(true);
		
		GLCanvas canvas = new GLCanvas(capabilities);
		canvas.setPreferredSize(new Dimension(canvasWidth, canvasHeight));
		canvas.addGLEventListener(this);
		canvas.addKeyListener(this);
		canvas.addMouseWheelListener(this);
		canvas.addMouseListener(this);
		canvas.setFocusable(true);
		canvas.requestFocus();

		animator = new FPSAnimator(canvas, FPS, false);
		
		frame.addWindowListener(new WindowAdapter()
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
		
		console = new Console(this);
		
		consoleInput = new JTextField();
		consoleInput.addActionListener(this);
		
		consoleInput.setBackground(Color.DARK_GRAY);
		consoleInput.setForeground(Color.LIGHT_GRAY);
		consoleInput.setCaretColor(Color.WHITE);
		
		Container content = frame.getContentPane();
		
		content.setLayout(new BorderLayout());
		
		content.add(canvas, BorderLayout.CENTER);
		content.add(consoleInput, BorderLayout.SOUTH);

		frame.pack();
		frame.setTitle("OpenGL Project Demo");
		frame.setVisible(true);
		
		animator.start();
	}
	
	public static void main(String[] args) { new Scene(); }
	
	public int getFrameRate() { return frameRate; }
	
	public int getHeight() { return canvasHeight; }
	
	public int getWidth()  { return canvasWidth;  }
	
	private void printVersion(GL2 gl)
	{
		System.out.println("OpenGL Version: " + gl.glGetString(GL2.GL_VERSION));
	    System.out.println("OpenGL Vendor : " + gl.glGetString(GL2.GL_VENDOR) + "\n");
	    
	    System.out.println("GL Shading Language Version: " + gl.glGetString(GL2.GL_SHADING_LANGUAGE_VERSION) + "\n");
	    
	    String extensions = gl.glGetString(GL2.GL_EXTENSIONS);
	    
	    System.out.println("Multitexture Support: " + extensions.contains("GL_ARB_multitexture"));
	    
	    int[] textureUnits = new int[1];
	    gl.glGetIntegerv(GL2.GL_MAX_TEXTURE_UNITS, textureUnits, 0);
	    System.out.println("Number of Texture Units: " + textureUnits[0] + "\n");
	    
	    boolean anisotropic = gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic");
	    System.out.println("Anisotropic Filtering Support: " + anisotropic);
	    
	    if(anisotropic)
	    {
		    float[] anisotropy = new float[1];
		    gl.glGetFloatv(GL2.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, anisotropy, 0);
		    System.out.println("Maximum Anisotropy: " + anisotropy[0] + "\n");
	    } 
	    
	    float[] size = new float[3];
	    
	    gl.glGetFloatv(GL2.GL_POINT_SIZE_RANGE, size, 0);
	    gl.glGetFloatv(GL2.GL_POINT_SIZE_GRANULARITY, size, 2);
	    
	    System.out.println("Point Size Range: " + size[0] + " -> " + size[1]);
	    System.out.println("Point Size Granularity: " + size[2] + "\n");
	    
	    gl.glGetFloatv(GL2.GL_ALIASED_POINT_SIZE_RANGE, size, 0);
	    System.out.println("Aliased Point Size Range: " + size[0] + " -> " + size[1] + "\n");
	    
	    gl.glGetFloatv(GL2.GL_LINE_WIDTH_RANGE, size, 0);
	    gl.glGetFloatv(GL2.GL_LINE_WIDTH_GRANULARITY, size, 2);
	    
	    System.out.println("Line Width Range: " + size[0] + " -> " + size[1]);
	    System.out.println("Line Width Granularity: " + size[2] + "\n");
	}

	private void printErrors(GL2 gl)
	{
		int error = gl.glGetError();
		
		while(error != GL2.GL_NO_ERROR)
		{
			System.err.println("OpenGL Error: " + glu.gluErrorString(error));
			error = gl.glGetError();
		}
	}

	public void init(GLAutoDrawable drawable)
	{
		execution = Calendar.getInstance();
		
		try
		{			
			brickWall    = TextureIO.newTexture(new File("tex/longBrick.jpg"), false);
			brickWallTop = TextureIO.newTexture(new File("tex/longBrickTop.jpg"), false);
			
			cobble       = TextureIO.newTexture(new File("tex/cobbles.jpg"), true);
			sky          = TextureIO.newTexture(new File("tex/sky.jpg"), true);
		}
		catch (Exception e) { e.printStackTrace(); }
		
		GL2 gl = drawable.getGL().getGL2();
		glu = new GLUgl2();
		glut = new GLUT();
		
		// may provide extra speed depending on machine
		gl.setSwapInterval(0);
		
		gl.glClearStencil(0);
		gl.glEnable(GL2.GL_STENCIL_TEST);
		
		float[] quadratic = {0.0f, 0.0f, 0.005f};
		
		gl.glPointSize(3);
		gl.glPointParameterfv(GL2.GL_POINT_DISTANCE_ATTENUATION, quadratic, 0);
		
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

		/** Texture Options **/
		gl.glEnable(GL2.GL_TEXTURE_2D);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_BASE_LEVEL, 0);
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_MAX_LEVEL,  4);
		
		gl.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
		gl.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
		gl.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_WRAP_S,     GL2.GL_CLAMP);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,     GL_REPEAT);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,     GL_REPEAT);
		

		/** Fog Setup **/
		gl.glEnable(GL_FOG);
		gl.glFogi  (GL_FOG_MODE, GL_EXP2);
		gl.glFogfv (GL_FOG_COLOR, fogColor, 0);
		gl.glFogf  (GL_FOG_DENSITY, fogDensity);
		gl.glFogf  (GL_FOG_START, 100.0f);
		gl.glFogf  (GL_FOG_END, 250.0f);
		gl.glHint  (GL_FOG_HINT, GL_NICEST);
		
		/** Lighting Setup **/	
	    light = new Light(gl);
	    
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		
		long setupStart = System.currentTimeMillis();
		
		/** Model Setup **/
		environmentFaces = OBJParser.parseTriangles("environment");
		floorFaces       = OBJParser.parseTriangles("floor");
		
		printErrors(gl);
	    
	    environmentList = gl.glGenLists(1);
	    gl.glNewList(environmentList, GL2.GL_COMPILE);
	    displayTexturedObject(gl, environmentFaces);
	    gl.glEndList();
	    
	    floorList = gl.glGenLists(1);
	    gl.glNewList(floorList, GL2.GL_COMPILE);
	    displayTexturedObject(gl, floorFaces);
	    gl.glEndList();
	    
	    new GreenShell (gl, this, null, 0, false); printErrors(gl);
	    new RedShell   (gl, this, null, 0, false); 
	    new BlueShell  (gl, this, null, 0);
	    new FakeItemBox(gl, this, null);
	    new Banana     (gl, this, null, 0);
	    
	    new BoostParticle(ORIGIN, null, 0, 0, 0, false, false);
	    new LightningParticle(ORIGIN);
	    new StarParticle(ORIGIN, null, 0, 0);
	    
	    generators.add(new ParticleGenerator(60, 600, GeneratorType.BLAST, new float[] {0, 50, -50}));
	    generators.add(new ParticleGenerator(60, 600, GeneratorType.BLAST, new float[] {0, 50, +50}));
	    
	    cars.add(new Car(gl, new float[] { 78.75f, 1.8f, 0}, 0,   0, 0, this));
	    
	    if(GamePad.numberOfGamepads() > 1)
	    	cars.add(new Car(gl, new float[] {-78.75f, 1.8f, 0}, 0, 180, 0, this));
	    
	    if(GamePad.numberOfGamepads() > 2)
	    	cars.add(new Car(gl, new float[] {0, 1.8f,  78.75f}, 0, 270, 0, this));
	    	
	    if(GamePad.numberOfGamepads() > 3)
		    cars.add(new Car(gl, new float[] {0, 1.8f, -78.75f}, 0,  90, 0, this));
		    
	    itemBoxes.addAll(ItemBox.generateDiamond( 56.25f, 30f, particles));
	    itemBoxes.addAll(ItemBox.generateSquare (101.25f, 60f, particles));
	    itemBoxes.addAll(ItemBox.generateSquare (123.75f, 30f, particles));
	    itemBoxes.addAll(ItemBox.generateDiamond(   180f,  0f, particles));
	    
	    fort = new BlockFort(gl);
	    
	    wallBounds = BoundParser.parseOBBs("bound/environment.bound");
	    
	    frameTimes     = new long[240];
	    renderTimes    = new long[240][COLUMN_HEADERS.length];
	    collisionTimes = new long[240];
	    
	    if(printVersion) printVersion(gl);
	    
	    console.parseCommand("profile project");
	    
	    generateTerrain(gl, DEFAULT_TERRAIN);
	    
	    long setupEnd = System.currentTimeMillis();
	    System.out.println("Setup Time: " + (setupEnd - setupStart) + " ms" + "\n");
	    
	    startTime = System.currentTimeMillis();
	    //records the time prior to the rendering of the first frame after initialization
	}

	public void display(GLAutoDrawable drawable)
	{	
		GL2 gl = drawable.getGL().getGL2();
		
		gl.glClearDepth(1.0f);
		gl.glClearColor(background[0], background[1], background[2], 1.0f);
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL2.GL_STENCIL_BUFFER_BIT);
		
		gl.glLoadIdentity();
		gl.glMatrixMode(GL_MODELVIEW);
		
		if(multiSample) gl.glEnable(GL2.GL_MULTISAMPLE);
		else gl.glDisable(GL2.GL_MULTISAMPLE);
		
		if(normalize) gl.glEnable(GL2.GL_NORMALIZE);
		else gl.glDisable(GL2.GL_NORMALIZE);
		
		setupFog(gl);
		
		registerItems(gl);
		
		if(enableAnimation) update();
		
		if(enableTerrain && !terrainCommand.equals(""))
		{	
			generateTerrain(gl, terrainCommand);	
			terrainCommand = "";
		}
		
		int[] order = new int[cars.size()];
		
		int i = orderRender(order);
		int _i = i; //temporary variable _i used to store the boost count this frame
		
		renderTime = System.currentTimeMillis();
		
		if(selectX != -1) selectModel(gl);

		for(int index : order)
		{
			Car car = cars.get(index);
			
			setupScene(gl, index);
			car.setupCamera(gl, glu);
			
			if(enableLight)
			{
				light.setup(gl, enableHeadlight);
				
				if(enableHeadlight)
				{
					float[][] vectors = car.getLightVectors();
					
					light.direction = vectors[2];
					light.setPosition(vectors[0]);
				}
			}
		
			if(enableReflection) displayReflection(gl, car);
			
			renderWorld(gl);
			render3DModels(gl, car);
			
			renderTimes[frameIndex][4] = renderParticles(gl, car);
			Particle.resetTexture();
			
			if(enableTerrain) renderTimes[frameIndex][1] = renderFoliage(gl, car);
			
			/*
			 * The condition (i == 1) means that the frames stored in the accumulation
			 * are displayed once the last boosting car has been rendered
			 * The condition (_i = boostCounter) means that the number of vehicles
			 * boosting is consistent with the previous frame; hence, the accumulation
			 * buffer is in a stable state (will not produce visual artifacts) 
			 */
			if(enableMotionBlur && car.isBoosting() && i == 1 && _i == boostCounter)
			{
				gl.glAccum(GL_MULT, 0.5f);
				gl.glAccum(GL_ACCUM, 0.5f);
		
				gl.glAccum(GL_RETURN, 1.0f);
			}
			else i--;
			
			renderTimes[frameIndex][5] = renderBounds(gl);
			renderTimes[frameIndex][6] = car.renderHUD(gl, glu);
		}
		
		/* 
		 * Loads the current frame into the accumulation buffer; this is so that if
		 * motion blur occurs in the next frame, any old frames stored in the buffer
		 * will be over-written to avoid displaying visual artifacts.
		 * */
		if(enableMotionBlur && _i != boostCounter) gl.glAccum(GL_LOAD, 1.0f);
		boostCounter = _i;
		
		gl.glFlush();
		
		printErrors(gl);
		
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
		glu.gluPerspective(fov, ratio, 1.0, 1000.0);
		
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity();
		
		gl.glClearAccum(0, 0, 0, 0);
		gl.glClear(GL_ACCUM_BUFFER_BIT);
	}

	public void dispose(GLAutoDrawable drawable) {}

	private void setupFog(GL2 gl)
	{
		if(cloud_density < 1) cloud_density += DENSITY_INC;
		
		float c = cloud_density;
		gl.glColor3f(c, c, c); //affect the color of the environment in addition to fog
		gl.glFogfv(GL_FOG_COLOR, new float[] {c, c, c, 1}, 0);
		
		gl.glFogf(GL_FOG_DENSITY, fogDensity);
	}

	private void registerItems(GL2 gl)
	{
		for(Car car : cars)
		{
			while(!car.getItemCommands().isEmpty())
			{
				int itemID = car.getItemCommands().poll();
				
				if(itemID == 10) cloud_density = MIN_DENSITY;
				else car.registerItem(gl, itemID);
			}
		}
	}

	private void update()
	{
		removeItems();
		
		Particle.removeParticles(particles);
		
		if(enableItemBoxes)
		{
			for(ItemBox box : itemBoxes) box.update(cars); 
		}
		
		updateItems();
		
		ItemBox.increaseRotation();
		FakeItemBox.increaseRotation();
		
		for(Particle p : particles) p.update();
		
		vehicleCollisions();
		for(Car car : cars) car.update();
		
		if(enableTerrain)
		{	
			List<BillBoard> toRemove = new ArrayList<BillBoard>();
			
			for(BillBoard b : foliage)
			{
				if(b.sphere.testOBB(cars.get(0).bound)) toRemove.add(b);
			}
			
			foliage.removeAll(toRemove);
				
			terrainCollisions();
		}
	}

	public void removeItems()
	{
		Item.removeItems(itemList);
		
		for(Car car : cars) car.removeItems();
	}

	private void updateItems()
	{
		for(Item item : itemList)
		{
			item.update();
			item.update(cars);
		}
		
		for(Car car : cars)
		{
			for(Item item : car.getItems())
			{
				item.hold();
				item.update(cars);
			}
		}
		
		collisionTimes[frameIndex] = itemCollisions();
	}

	private long itemCollisions()
	{
		List<Item> allItems = new ArrayList<Item>();
		
		allItems.addAll(itemList);
		
		for(Car car : cars)
			allItems.addAll(car.getItems());
		
		long start = System.nanoTime();
		
		for(int i = 0; i < allItems.size() - 1; i++)
		{
			for(int j = i + 1; j < allItems.size(); j++)
			{
				Item a = allItems.get(i);
				Item b = allItems.get(j);
				
				//TODO can be optimised further by using spatial partitioning
				if(a.canCollide(b) && a.getBound().testBound(b.getBound())) a.collide(b);
			}
		}
		
		return System.nanoTime() - start;
	}

	private void vehicleCollisions()
	{
		for(int i = 0; i < cars.size() - 1; i++)
		{
			for(int j = i + 1; j < cars.size(); j++)
			{
				Car a = cars.get(i);
				Car b = cars.get(j);
				
				if(a.getBound().testBound(b.getBound())) a.collide(b);
			}
		}
	}

	private void terrainCollisions()
	{
		//TODO Only considers collisions with player 1
		
		float[][] vertices = cars.get(0).bound.getVertices();
		float[] frictions = {1, 1, 1, 1};
		
		cars.get(0).patch = null;
			
		for(TerrainPatch patch : terrainPatches)
		{
			patch.colliding = false;
			
			for(int v = 0; v < 4; v++)
			{
				if(patch.isColliding(vertices[v]))
				{
					patch.colliding = true;
					if(frictions[v] > patch.friction) frictions[v] = patch.friction;
					cars.get(0).patch = patch;
				}
			}
		}
			
		float friction = (frictions[0] + frictions[1] + frictions[2] + frictions[3]) / 4;
		cars.get(0).friction = friction;
	}

	private int orderRender(int[] order)
	{
		int i = 0;
		
		for(int j = 0; j < cars.size(); j++)
		{
			order[j] = j; 
			
			/*
			 * The vehicles that are boosting must come first in the rendering
			 * order so that the accumulative buffer does not store the frames
			 * rendered by other vehicles.
			 */
			if(cars.get(j).isBoosting())
			{
				int t = order[i];
				order[i] = j;
				order[j] = t;
				i++;
			}
		}
		return i;
	}

	//TODO does not work in bird's eye view
	private void selectModel(GL2 gl)
	{
		startPicking(gl);
		
		gl.glPushName(1);
		
		gl.glPushMatrix();
		{		
			gl.glTranslatef(0, 30, -30);
			gl.glRotatef(ry, 0, 1, 0);
			
			glut.glutSolidTeapot(3);
		}
		gl.glPopMatrix();
		
		gl.glPopName();
		
		endPicking(gl);
	}
	
	private void startPicking(GL2 gl)
	{
		selectBuffer = Buffers.newDirectIntBuffer(BUFFER_SIZE);
		gl.glSelectBuffer(BUFFER_SIZE, selectBuffer);
		
		gl.glRenderMode(GL2.GL_SELECT);
		
		gl.glInitNames();
		
		gl.glMatrixMode(GL_PROJECTION);
		
		gl.glPushMatrix();
		{
			gl.glLoadIdentity();
			
			int[] viewport = new int[4];
			gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
			
			glu.gluPickMatrix(selectX, viewport[3] - selectY, 5, 5, viewport, 0);
			
			float aspect = (float) viewport[2] / (float) viewport[3];
			
			glu.gluPerspective(fov, aspect, 1.0, 1000.0);
			cars.get(0).setupCamera(gl, glu);
			
			gl.glMatrixMode(GL_MODELVIEW);
		}
	}

	private void endPicking(GL2 gl)
	{
		gl.glMatrixMode(GL_PROJECTION);
		gl.glPopMatrix();
		
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glFlush();
		
		int hits = gl.glRenderMode(GL2.GL_RENDER);
		
		selectX = selectY = -1;
		
		getSelection(gl, hits);
	}

	private void getSelection(GL2 gl, int hits)
	{
		selected++;
		
		if(hits == 0) cars.get(0).getHUD().broadcast("No hit: " + selected);
		else cars.get(0).getHUD().broadcast("Hit: " + selected);

	}
	
	/**
	 * This method minimises the viewport to create a split-screen effect.
	 * The size of the viewport is halved so that it occupies one fourth/corner
	 * of the screen; P1 is top-left, P2 is top-right, P3 is bottom-left and P4
	 * is bottom-right.
	 * 
	 * It uses the OpenGL method glViewport(x, y, w, h). (x, y) is the bottom-left
	 * corner of the viewport, while w and h are its width and height respectively. 
	 */
	private void setupScene(GL2 gl, int playerID)
	{
		int h = canvasHeight / 2;
		int w = canvasWidth  / 2;
		
		if(cars.size() < 2) gl.glViewport(0, 0, canvasWidth, canvasHeight); //full-screen
		else
		{
			     if(playerID == 0) gl.glViewport(0, h, w, h); //top-left
			else if(playerID == 1) gl.glViewport(w, h, w, h); //top-right
			else if(playerID == 2) gl.glViewport(0, 0, w, h); //bottom-left
			else if(playerID == 3) gl.glViewport(w, 0, w, h); //bottom-right
		}
	}

	private void displayReflection(GL2 gl, Car car)
	{
		float[] p = light.getPosition();
		light.setPosition(new float[] {p[0], -p[1], p[2]});
		
		gl.glPushMatrix();
		{
			gl.glScalef(1.0f, -1.0f, 1.0f);
			
			renderWorld(gl);
			render3DModels(gl, car);
			
			renderParticles(gl, car);
			Particle.resetTexture();
		}
		
		gl.glColor4f(0.75f, 0.75f, 0.75f, opacity);
	
		gl.glPopMatrix();
		
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_BLEND);
		
		gl.glPushMatrix();
		{
			gl.glTranslatef(0, 0, 0);
			gl.glScalef(40.0f, 40.0f, 40.0f);
	
			gl.glCallList(floorList);
		}	
		gl.glPopMatrix();
		
		gl.glDisable(GL2.GL_BLEND);
		gl.glEnable(GL2.GL_LIGHTING);
		
		light.setPosition(p);
		
		gl.glColor3f(1, 1, 1);
	}

	/**
	 * This method renders the world geometry that represents the artificial
	 * boundaries of the virtual environment. This consists of the skybox and
	 * the terrain itself.
	 */
	private long renderWorld(GL2 gl)
	{
		long start = System.nanoTime();
		
		if(displaySkybox) renderSkybox(gl);
		
		if(!enableReflection)
		{
			gl.glPushMatrix();
			{
				gl.glTranslatef(0, 0, 0);
				gl.glScalef(40.0f, 40.0f, 40.0f);
	
				gl.glCallList(floorList);
			}	
			gl.glPopMatrix();
		}
		
		if(enableTerrain) renderTimes[frameIndex][0] = renderTerrain(gl);
		
		renderObstacles(gl);
			
		if(displaySkybox)
		{
			Texture[] textures = {cobble, brickWallTop, brickWall};
		    	 
			gl.glPushMatrix();
			{
				displayTexturedCuboid(gl,        0, 45,  206.25f, 202.5f, 45,  3.75f,  0, textures);
				displayTexturedCuboid(gl,        0, 45, -206.25f, 202.5f, 45,  3.75f,  0, textures);
		    	displayTexturedCuboid(gl,  206.25f, 45,        0, 202.5f, 45,  3.75f, 90, textures);
		    	displayTexturedCuboid(gl, -206.25f, 45,        0, 202.5f, 45,  3.75f, 90, textures);
			}
			gl.glPopMatrix();
		}
		
		return System.nanoTime() - start;
	}

	private void renderSkybox(GL2 gl)
	{
		gl.glDisable(GL2.GL_LIGHTING);
		
		gl.glPushMatrix();
		{
			gl.glTranslatef(0, 0, 0);
			gl.glScalef(40.0f, 40.0f, 40.0f);
	
			gl.glCallList(environmentList);
		}	
		gl.glPopMatrix();
		
		gl.glEnable(GL2.GL_LIGHTING);
	}

	private long renderTerrain(GL2 gl)
	{
		long start = System.nanoTime();
		
		gl.glPushMatrix();
		{
			heightMap.render(gl, glut);
		}	
		gl.glPopMatrix();
			
		gl.glPushMatrix();
		{
			gl.glScalef(Terrain.sx, Terrain.sy, Terrain.sz);
			
			for(TerrainPatch shape : terrainPatches) shape.render(gl);
		}	
		gl.glPopMatrix();
		
		return System.nanoTime() - start;
	}

	/**
	 * This method renders the 3D models that represent obstacles within the
	 * world. Obstacle may be rendered using colored and/or textured geometry,
	 * or simply as wireframes if obstacle wireframes are enabled for debugging.
	 */
	private long renderObstacles(GL2 gl)
	{	
		long start = System.nanoTime();
		
		fort.render(gl);
		
		return System.nanoTime() - start;
	}

	/**
	 * This method renders all of the dynamic 3D models within the world from the
	 * perspective of the car passed as a parameter. The term dynamic refers to
	 * objects that change position, rotation or state such as other vehicles, item
	 * boxes and world items.
	 */
	private void render3DModels(GL2 gl, Car car)
	{
		renderTimes[frameIndex][2] = renderVehicles(gl, car);
	
		if(enableItemBoxes)
	    {
			for(ItemBox box : itemBoxes)
				if(!box.isDead()) box.render(gl, car.trajectory);
	    }
		
		renderTimes[frameIndex][3] = renderItems(gl, car);
	}

	private long renderVehicles(GL2 gl, Car car)
	{
		long start = System.nanoTime();
		
		car.render(gl);
	
		for(Car c : cars)
		{
			if(!c.equals(car))
			{
				boolean visibility = c.displayModel;
				
				c.displayModel = true;
				c.render(gl);
				c.displayModel = visibility;
			}
		}
		
		return System.nanoTime() - start;
	}

	private long renderItems(GL2 gl, Car car)
	{
		long start = System.nanoTime();
		
		if(enableCulling) gl.glEnable(GL2.GL_CULL_FACE);
	
		for(Item item : itemList)
			if(!item.isDead())
			{
				if(Item.renderMode == 1) item.renderWireframe(gl, car.trajectory);
				else item.render(gl, car.trajectory);
			}
		
		gl.glDisable(GL2.GL_CULL_FACE);
		
		return System.nanoTime() - start;
	}

	private long renderParticles(GL2 gl, Car car)
	{
		long start = System.nanoTime();
		
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
		
		for(Particle particle : particles)
		{
			if(car.isSlipping()) particle.render(gl, car.slipTrajectory);
			else particle.render(gl, car.trajectory);
		}
		
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		
		return System.nanoTime() - start;
	}

	private long renderFoliage(GL2 gl, Car car)
	{
		long start = System.nanoTime();
		
		gl.glPushMatrix();
		{	
			for(BillBoard b : foliage)
			{
				if(car.isSlipping()) b.render(gl, car.slipTrajectory);
				else b.render(gl, car.trajectory);
			}
		}	
		gl.glPopMatrix();
		
		return System.nanoTime() - start;
	}

	private long renderBounds(GL2 gl)
	{
		long start = System.nanoTime();
		
		gl.glDisable(GL_TEXTURE_2D);
		
		if(testMode) test(gl);
		
		List<Bound> bounds = getBounds();
		
		if(enableClosestPoints)
			for(Bound bound : bounds)
				bound.displayClosestPtToPt(gl, glut, cars.get(0).getPosition(), smoothBound);
		
		if(enableOBBSolids)
			for(Bound bound : bounds)
				bound.displaySolid(gl, glut, RGB.toRGBA(RGB.VIOLET, 0.1f));
		
		if(enableOBBVertices)
		{
			for(Car car : cars)
			{
				if(car.colliding)
					 car.bound.displayVertices(gl, glut, RGB.PURE_RED_3F, smoothBound);
				else car.bound.displayVertices(gl, glut, RGB.WHITE_3F, smoothBound);
			}
		
			for(OBB wall : wallBounds) wall.displayVertices(gl, glut, RGB.WHITE_3F, smoothBound);
			for(OBB wall : fort.getBounds()) wall.displayVertices(gl, glut, RGB.WHITE_3F, smoothBound);
		}
		
		if(enableOBBWireframes)
		{
			for(Car car : cars)
			{
				if(car.colliding)
					 car.bound.displayWireframe(gl, glut, RGB.PURE_RED_3F, smoothBound);
				else car.bound.displayWireframe(gl, glut, RGB.BLACK_3F, smoothBound);
			}
			
			for(Bound bound : bounds)
				for(Car car : cars)
				{
					if(car.collisions != null && car.collisions.contains(bound))
					{
						 bound.displayWireframe(gl, glut, RGB.PURE_RED_3F, smoothBound);
						 break;
					}
					else bound.displayWireframe(gl, glut, RGB.BLACK_3F, smoothBound);
				}
		}
		
		if(enableOBBAxes)
		{
			for(Car car : cars) car.bound.displayAxes(gl, 10);
			for(OBB wall : wallBounds) wall.displayAxes(gl, 20);
			for(OBB wall : fort.getBounds()) wall.displayAxes(gl, 20);
		}
		
		for(Car car : cars)
			for(Item item : car.getItems())
				item.renderBound(gl, glut);
		
		for(Item item : itemList) item.renderBound(gl, glut);
		
		gl.glEnable(GL_TEXTURE_2D);
		
		return System.nanoTime() - start;
	}

	private void test(GL2 gl)
	{
		gl.glPushMatrix();
		{
			gl.glColor3f(0.25f, 0.25f, 0.25f);
			gl.glTranslatef(0, 30, -30);
			gl.glRotatef(ry, 0, 1, 0);
			glut.glutSolidTeapot(3);
		}
		gl.glPopMatrix();
		
		gl.glColor3f(1, 1, 1);
		
		ry++;
		ry %= 360;
		
		List<Face> carFaces = Car.CAR_FACES;
		
		gl.glPushMatrix();
		{
			gl.glTranslatef(0, 30, -15);
			gl.glRotatef(ry, 0, 1, 0);
			gl.glScalef(1.5f, 1.5f, 1.5f);
			
			float[][] colors = Renderer.getCellShades(new float[] {1.0f, 0.4f, 0.4f}, 0.05f, 4);
			Renderer.cellShadeObject(gl, carFaces, light.getPosition(), colors);
		}
		gl.glPopMatrix();
		
		float[] plane = {0.25f, 0.5f, 0.75f, 0};
		
		gl.glEnable(GL2.GL_TEXTURE_GEN_S);
		gl.glEnable(GL2.GL_TEXTURE_GEN_T);
		
		gl.glTexGeni (GL2.GL_S, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_OBJECT_LINEAR);
		gl.glTexGeni (GL2.GL_T, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_OBJECT_LINEAR);
		gl.glTexGenfv(GL2.GL_S, GL2.GL_OBJECT_PLANE, plane, 0);
		gl.glTexGenfv(GL2.GL_T, GL2.GL_OBJECT_PLANE, plane, 0);
		
		gl.glEnable(GL2.GL_TEXTURE_2D);
		
		gl.glPushMatrix();
		{
			sky.bind(gl);
			sky.setTexParameterf(gl, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT, 16);
			
			gl.glTranslatef(0, 30, 0);
			gl.glRotatef(ry, 0, 1, 0);
			
			glut.glutSolidTeapot(3);
		}
		gl.glPopMatrix();
		
		gl.glTexGeni(GL2.GL_S, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_SPHERE_MAP);
		gl.glTexGeni(GL2.GL_T, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_SPHERE_MAP);
		
		gl.glPushMatrix();
		{
			sky.bind(gl);
			sky.setTexParameterf(gl, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT, 16);
			
			gl.glTranslatef(0, 30, 15);
			gl.glRotatef(ry, 0, 1, 0);
			
			glut.glutSolidTeapot(3);
		}
		gl.glPopMatrix();
		
		gl.glDisable(GL2.GL_TEXTURE_GEN_S);
		gl.glDisable(GL2.GL_TEXTURE_GEN_T);
		
		GLUquadric sphere = glu.gluNewQuadric();
		
		glu.gluQuadricDrawStyle(sphere, GLU.GLU_FILL);
		glu.gluQuadricNormals(sphere, GLU.GLU_SMOOTH);
		glu.gluQuadricTexture(sphere, true);
		
		gl.glPushMatrix();
		{	
			cobble.bind(gl);
			cobble.setTexParameterf(gl, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT, 16);
			
			gl.glTranslatef(0, 30, 30);
			gl.glRotatef(ry, 0, 1, 0);
			
			glu.gluSphere(sphere, 2, 24, 24);
		}
		gl.glPopMatrix();
		
		bezier += rising ? 10 : -10;
		     if(bezier == 360) rising = false;
		else if(bezier ==   0) rising = true;
		
		float h = (bezier - 180) / 180;
		
		float[][] control =
		{
			{-4.0f, 0.0f, 4.0f},
			{-2.0f, 4.0f, 4.0f},
			{ 4.0f, 0.0f, 4.0f},
		
			{-4.0f, 0.0f, 0.0f},
			{-2.0f, 4.0f * h, 0.0f},
			{ 4.0f, 0.0f, 0.0f},
		
			{-4.0f, 0.0f, -4.0f},
			{-2.0f, 4.0f, -4.0f},
			{ 4.0f, 0.0f, -4.0f}
		};
		
		gl.glEnable(GL2.GL_AUTO_NORMAL);
		
		gl.glPushMatrix();
		{	
			gl.glTranslatef(0, 15, -15);
			
			gl.glDisable(GL2.GL_TEXTURE_2D);
			
			Renderer.displayPoints(gl, glut, new float[][] {control[0], control[1], control[2]}, RGB.PURE_GREEN_3F, 10, true);
			Renderer.displayPoints(gl, glut, new float[][] {control[3], control[4], control[5]}, RGB.PURE_RED_3F,   10, true);
			Renderer.displayPoints(gl, glut, new float[][] {control[6], control[7], control[8]}, RGB.PURE_BLUE_3F,  10, true);
			
			gl.glColor3f(0.2f, 0.2f, 0.2f);
			
			ByteBuffer bb = ByteBuffer.allocateDirect(control.length * 3 * 3 * 4); 
			bb.order(ByteOrder.nativeOrder());
			FloatBuffer fb = bb.asFloatBuffer();
			
			for(int i = 0; i < control.length; i++) fb.put(control[i]);
			fb.position(0);  
			
			gl.glMap2f(GL2.GL_MAP2_VERTEX_3, // Type of data generated
					0.0f, // Lower u range
					10.0f, // Upper u range
					3, // Distance between points in the data
					3, // Dimension in u direction (order)
					0.0f, // Lower v range
					10.0f, // Upper v range
					9, // Distance between points in the data
					3, // Dimension in v direction (order)
					fb); // array of control points
	
			gl.glEnable(GL2.GL_MAP2_VERTEX_3);
			gl.glMapGrid2f(10, 0.0f, 10.0f, 10, 0.0f, 10.0f);
			gl.glEvalMesh2(GL2.GL_FILL, 0, 10, 0, 10);
		}
		gl.glPopMatrix();
		
		float[] control2 =
		{
			-6.0f, -6.0f, 0.0f,
			-6.0f, -2.0f, 0.0f,
			-6.0f,  2.0f, 0.0f, 
			-6.0f,  6.0f, 0.0f, 
			-2.0f, -6.0f, 0.0f, 
			-2.0f, -2.0f, 8.0f * h,
			-2.0f,  2.0f, 8.0f * h, 
			-2.0f,  6.0f, 0.0f, 
			 2.0f, -6.0f, 0.0f,
			 2.0f, -2.0f, 8.0f,
			 2.0f,  2.0f, 8.0f,
			 2.0f,  6.0f, 0.0f,
			 6.0f, -6.0f, 0.0f,
			 6.0f, -2.0f, 0.0f,
			 6.0f,  2.0f, 0.0f,
			 6.0f,  6.0f, 0.0f
		};
		
		float[] knots = {0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f};
		
		gl.glPushMatrix();
		{		
			gl.glTranslatef(0, 15, -30);
			gl.glRotatef(-90, 1, 0, 0);
			
	        GLUgl2 glugl2 = new GLUgl2(); 
	        GLUnurbs nurb = glugl2.gluNewNurbsRenderer();
	        
	        glugl2.gluBeginSurface(nurb);
	
	        glugl2.gluNurbsSurface(nurb, 8, knots, 8, knots, 4 * 3, 3, control2, 4, 4, GL2.GL_MAP2_VERTEX_3); 
	        
	        glugl2.gluEndSurface(nurb); 
		}
		gl.glPopMatrix();
		
		gl.glEnable(GL2.GL_TEXTURE_2D);
		
		gl.glColor3f(1, 1, 1);
	}

	/**
	 * This method calculates the frames per second (FPS) of the application by
	 * incrementing a frame counter every time the scene is displayed. Once the
	 * time elapsed is >= 1000 milliseconds (1 second), the current FPS is set
	 * equal to the frame counter which is then reset.
	 */
	private void calculateFPS()
	{
		frames++;
		frameIndex++;
		
		long currentTime = System.currentTimeMillis();
		long timeElapsed = currentTime - startTime;
		
		if(timeElapsed >= 1000)
		{
			frameRate = frames;
			
			frames = 0;
			startTime = currentTime + (timeElapsed % 1000);
		}
		
		if(frameIndex >= frameTimes.length) frameIndex = 0;
		
		frameTimes[frameIndex] = currentTime - renderTime;
	}

	public List<Car> getCars() { return cars; }

	public void sendItemCommand(int itemID) { itemQueue.add(itemID); }
	
	public void addItem(Item item) { itemList.add(item); }
	
	public void addItem(int itemID, float[] c, float trajectory)
	{
		switch(itemID)
		{
			case  0: itemList.add(new  GreenShell(this, c, trajectory)); break;
			case  2: itemList.add(new    RedShell(this, c, trajectory)); break;
			case  7: itemList.add(new FakeItemBox(this, c, trajectory)); break;
			case  8: itemList.add(new      Banana(this, c)); break;
			case 13: itemList.add(new   BlueShell(this, c)); break;
			
			default: break;
		}
	}

	public void spawnItemsInBound(int itemID, int quantity, Bound b)
	{
		Random random = new Random();
		
		for(int i = 0; i < quantity; i++)
		{
			addItem(itemID, b.randomPointInside(), random.nextInt(360));
		}
	}

	public void spawnItemsInSphere(int itemID, int quantity, float[] c, float r)
	{
		spawnItemsInBound(itemID, quantity, new Sphere(c, r));
	}

	public void spawnItemsInOBB(int itemID, int quantity, float[] c, float[] u, float[] e)
	{
		spawnItemsInBound(itemID, quantity, new OBB(c, u, e, null));
	}

	public void clearItems() { itemList.clear(); }

	public List<Item> getItems() { return itemList; }
	
	public void addItemBox(float x, float y, float z)
	{
		itemBoxes.add(new ItemBox(x, y, z, particles));
	}

	public void addParticle(Particle p) { particles.add(p); }
	
	public void addParticles(List<Particle> particles) { this.particles.addAll(particles); }
	
	public List<Particle> getParticles() { return particles; }
	
	public List<Bound> getBounds()
	{
		List<Bound> bounds = new ArrayList<Bound>();
		
		bounds.addAll(wallBounds);
		if(enableObstacles) bounds.addAll(fort.getBounds());
		
		return bounds;
	}

	public Terrain getTerrain() { return heightMap; }
	
	public void generateTerrain(GL2 gl, String command)
	{
		long start = System.currentTimeMillis();
		
		Random generator = new Random();
		
		String[] args = command.trim().split(" ");
		
		if(args.length > 3)
		{
			int length     = Integer.parseInt(args[0]);
			int iterations = Integer.parseInt(args[1]);
			int splashes   = Integer.parseInt(args[2]);
			
			int r0 = Integer.parseInt(args[3]);
			int r1 = Integer.parseInt(args[4]);
			
			float p = Float.parseFloat(args[5]);
			float h = Float.parseFloat(args[6]);
			
			heightMap = new Terrain(gl, length, iterations, r0, r1, p, h);
			terrainPatches = new TerrainPatch[splashes];
	    
			for (int i = 0; i < terrainPatches.length; i++)
				terrainPatches[i] = new TerrainPatch(null, heightMap.heights, generator.nextInt(15) + 5);
		}
		else
		{
			int length     = Integer.parseInt(args[0]);
			int iterations = Integer.parseInt(args[1]);
			int splashes   = Integer.parseInt(args[2]);
			
			heightMap = new Terrain(gl, length, iterations);
			terrainPatches = new TerrainPatch[splashes];
	    
			for (int i = 0; i < terrainPatches.length; i++)
				terrainPatches[i] = new TerrainPatch(null, heightMap.heights, generator.nextInt(15) + 5);
		}
			
		generateFoliage(60, 10, 30);
		
		System.out.printf("Terrain Generated: %d ms\n", (System.currentTimeMillis() - start));
	}

	public void generateFoliage(int patches, float spread, int patchSize)
	{
		foliage = new ArrayList<BillBoard>();
		Random generator = new Random();
	    
	    for(int i = 0; i < patches; i++)
	    {
	    	int t = (i < patches * 0.5) ? 3 : generator.nextInt(3);
	    	
	    	float[] p = new float[3];
	    	
	    	p[0] = generator.nextFloat() * 360 - 180;
	    	p[2] = generator.nextFloat() * 360 - 180;
	    	
	    	if(i > patches * 0.75)
	    	{
	    		p[1] = heightMap.getHeight(p);
	    		foliage.add(new BillBoard(p, t));
	    	}
	    	else
	    	{    	
		    	int k = generator.nextInt(patchSize - 1) + 1; 
		    	
		    	for(int j = 0; j < k; j++)
		    	{
		    		float[] p0 = {p[0], p[1], p[2]};
		    		
		    		p0[0] += (generator.nextFloat() * spread * 2) - spread;
		    		p0[2] += (generator.nextFloat() * spread * 2) - spread;
		    		p0[1] = heightMap.getHeight(p0);
		    		
		    		foliage.add(new BillBoard(p0, t));
		    	}
	    	}
	    }
	}

	public void printDataToFile(String file) //TODO Complete file format
	{
		try
		{
			Calendar now = Calendar.getInstance();
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
			
			String _execution = new SimpleDateFormat("yyyyMMddHHmmss").format(execution.getTime());
			
			FileWriter writer = new FileWriter("output/" + (file == null ? _execution : file) + ".txt");
			BufferedWriter out = new BufferedWriter(writer);
			
			out.write("Values recorded at: " + dateFormat.format(now.getTime()) + "\r\n\r\n ");
			
			for(int c = 0; c < COLUMN_HEADERS.length; c++)
				out.write(String.format("%11s", COLUMN_HEADERS[c]) + "\t");
			
			out.write("\r\n\r\n ");
			
			for(int i = 0; i < renderTimes.length; i++)
			{
				for(int j = 0; j < COLUMN_HEADERS.length; j++)
				{
					out.write(String.format("%11.3f", renderTimes[i][j] / 1E6) + "\t");
				}	
				
				out.write("\r\n ");
			}
	
			out.close();
		}
		
		catch(Exception e)
		{
			System.err.println(e.getMessage());
		}
	}

	public void playMusic()
	{
		if(!musicPlaying)
		{
			new MP3(this, MUTE_CITY).start();
			musicPlaying = true;
		}
	}

	public void stopMusic() { musicPlaying = false; }

	/**
	 * Invoked when a key has been pressed to make global alterations to the scene
	 * such as toggling the display of certain visualizations intended to aid the
	 * debugging process. By default, if one of the keys pressed is not a global
	 * control, the key press event is passed on to each vehicle within the scene.
	 */
	public void keyPressed(KeyEvent e)
	{	
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_ESCAPE: System.exit(0); break; //Close the application
	
			case KeyEvent.VK_H:  enableObstacles = !enableObstacles; break;
			
			case KeyEvent.VK_T:  enableTerrain = !enableTerrain; cars.get(0).friction = 1; break; //TODO
			case KeyEvent.VK_O:  enableItemBoxes = !enableItemBoxes; break;
			case KeyEvent.VK_I:  spawnItemsInSphere(8, 10, new float[] {0, 100, 0}, 50); break;
			case KeyEvent.VK_U:  spawnItemsInOBB(0, 10, new float[] {0, 100, 0}, ORIGIN, new float[] {150, 50, 150}); break;
			
			case KeyEvent.VK_PERIOD: Renderer.anisotropic = !Renderer.anisotropic; break;
			case KeyEvent.VK_COMMA: testMode = !testMode; break;
			
			case KeyEvent.VK_L:  printDataToFile(null); break;
			
			case KeyEvent.VK_DELETE: clearItems(); break;
	
			case KeyEvent.VK_P:	 playMusic(); break;
			
			case KeyEvent.VK_Z:  normalize = !normalize; break;
			case KeyEvent.VK_J:  enableLight = !enableLight; break;
			case KeyEvent.VK_K:  moveLight = !moveLight; break;
	 
			case KeyEvent.VK_F12: enableAnimation = !enableAnimation; break;
	
			case KeyEvent.VK_1:  enableOBBAxes       = !enableOBBAxes;       break;
			case KeyEvent.VK_2:  enableOBBVertices   = !enableOBBVertices;   break;
			case KeyEvent.VK_3:  enableOBBWireframes = !enableOBBWireframes; break;
			case KeyEvent.VK_4:	 enableOBBSolids     = !enableOBBSolids;     break;
			case KeyEvent.VK_5:	 enableClosestPoints = !enableClosestPoints; break;
			
			case KeyEvent.VK_F1: fort.renderMode++; fort.renderMode %= 4; break;
			case KeyEvent.VK_F3: Item.renderMode++; Item.renderMode %= 2; break;
			
			case KeyEvent.VK_6:  Item.toggleBoundSolids(); break;
			case KeyEvent.VK_7:  Item.toggleBoundFrames(); break;
			
			case KeyEvent.VK_8:  displaySkybox = !displaySkybox; break;
			case KeyEvent.VK_0:  fort.displayModel = !fort.displayModel; break;
			
			case KeyEvent.VK_Y:  enableMotionBlur = !enableMotionBlur; break;
			
			case KeyEvent.VK_SPACE: console.parseCommand(consoleInput.getText()); break;
	
			default: for(Car car : cars) car.keyPressed(e); break;
		}
	}
	
	public void keyReleased(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			default: for(Car car : cars) car.keyReleased(e); break;
		}
	}

	public void keyTyped(KeyEvent e)
	{
		switch (e.getKeyChar())
		{
			default: break;
		}
	}

	public void actionPerformed(ActionEvent event)
	{
		console.parseCommand(consoleInput.getText());
	}
	
	public KeyEvent pressKey(char c)
	{
		long when = System.nanoTime();
		int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
		
		return new KeyEvent(consoleInput, KeyEvent.KEY_PRESSED, when, 0, keyCode, c);
	}
	
	public void mouseWheelMoved(MouseWheelEvent e) {}

	public void mouseClicked(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited (MouseEvent e) {}

	public void mousePressed(MouseEvent e)
	{
		selectX = e.getX();
		selectY = e.getY();
	}

	public void mouseReleased(MouseEvent e) {}
}
