package bates.jamie.graphics.scene;

import static bates.jamie.graphics.util.Renderer.displayTexturedCuboid;
import static bates.jamie.graphics.util.Renderer.displayTexturedObject;
import static bates.jamie.graphics.util.Vector.dot;
import static javax.media.opengl.GL.GL_COLOR_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_TEST;
import static javax.media.opengl.GL.GL_LEQUAL;
import static javax.media.opengl.GL.GL_NICEST;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import javax.media.opengl.glu.gl2.GLUgl2;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import bates.jamie.graphics.collision.Bound;
import bates.jamie.graphics.collision.BoundParser;
import bates.jamie.graphics.collision.OBB;
import bates.jamie.graphics.collision.Sphere;
import bates.jamie.graphics.entity.BillBoard;
import bates.jamie.graphics.entity.BlockFort;
import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.entity.Quadtree;
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
import bates.jamie.graphics.particle.Blizzard;
import bates.jamie.graphics.particle.Blizzard.StormType;
import bates.jamie.graphics.particle.BoostParticle;
import bates.jamie.graphics.particle.LightningParticle;
import bates.jamie.graphics.particle.Particle;
import bates.jamie.graphics.particle.ParticleGenerator;
import bates.jamie.graphics.particle.StarParticle;
import bates.jamie.graphics.sound.MP3;
import bates.jamie.graphics.util.Face;
import bates.jamie.graphics.util.Matrix;
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
public class Scene implements GLEventListener, KeyListener, MouseWheelListener, MouseListener, ActionListener, ItemListener
{
	public JFrame frame;
	public GLCanvas canvas;
	
	private Console console;
	private JTextField consoleInput;
	
	private JMenuBar menuBar;
	
	private JMenu menu_file;
	private JMenu menu_load;
	private JMenuItem menuItem_project;
	private JMenuItem menuItem_game;
	private JMenuItem menuItem_close;
	
	private JMenu menu_control;
	private JCheckBoxMenuItem menuItem_reverse;
	
	private JMenu menu_render;
	private JMenu menu_quality;
	private JCheckBoxMenuItem menuItem_multisample;
	private JCheckBoxMenuItem menuItem_anisotropic;
	private JMenu menu_effects;
	private JCheckBoxMenuItem menuItem_motionblur;
	private JCheckBoxMenuItem menuItem_fog;
	private JMenu menu_weather;
	private JRadioButtonMenuItem menuItem_none;
	private JRadioButtonMenuItem menuItem_rain;
	private JRadioButtonMenuItem menuItem_snow;
	
	private JMenu menu_light;
	private JCheckBoxMenuItem menuItem_normalize;
	private JCheckBoxMenuItem menuItem_smooth;
	private JCheckBoxMenuItem menuItem_secondary;
	
	private JMenu menu_terrain;
	private JCheckBoxMenuItem menuItem_water;
	
	private JMenu menu_quadtree;
	private JCheckBoxMenuItem menuItem_solid;
	private JCheckBoxMenuItem menuItem_frame;
	
	private int canvasWidth = 860;
	private int canvasHeight = 640;
	
	public static final int FPS = 60;
	public static final int MIN_FPS = 30;
	
	private final FPSAnimator animator;
	
	public float fov = 100.0f;
	
	private GLU glu;
	private GLUT glut;
	
	public float[] background = RGB.SKY_BLUE;
	
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
	public long[][] updateTimes; //buffered times for collision detection tests (for CT graph)
	
	public static final String[] RENDER_HEADERS =
		{"Terrain", "Foliage", "Vehicles", "Items", "Particles", "Bounds", "HUD"};
	
	public static final String[] UPDATE_HEADERS =
		{"Collision", "Deformation", "Buffers"};
	
	public boolean enableCulling = false;
	
	
	/** Texture Fields **/
	private Texture brickWall;
	private Texture brickWallTop;
	
	private Texture cobble;

	
	/** Model Fields **/	
	private List<Face> environmentFaces;
	private List<Face> floorFaces;
	private int environmentList;
	private int floorList;
	
	private List<Car> cars = new ArrayList<Car>();
	public boolean enableQuality = false;
	
	public static final float[] ORIGIN = {0.0f, 0.0f, 0.0f};
	public static final float GLOBAL_RADIUS = 300;

	private List<ItemBox> itemBoxes = new ArrayList<ItemBox>();
	public boolean enableItemBoxes = false;
	
	
	/** Light Fields **/
	public Light light;
	
	public boolean enableLight = true;
	public boolean moveLight = false;
	public boolean headlight = false;
	public boolean displayLight = true;
	
	
	/** Fog Fields **/
	public boolean enableFog = true;
	public float fogDensity = 0.004f;
	public float[] fogColor = {1.0f, 1.0f, 1.0f, 1.0f};
	
	private float cloudDensity = 1.0f;
    private static final float MIN_DENSITY = 0.5f;
    private static final float DENSITY_INC = 0.0125f;
	
    
    /** Environment Fields **/
    public boolean displaySkybox = true;
	
	
	/** Collision Detection Fields **/	
	private boolean enableOBBAxes       = false;
	private boolean enableOBBVertices   = false;
	private boolean enableOBBWireframes = false;
	private boolean enableOBBSolids     = false;
	private boolean enableClosestPoints = false;
	
	public boolean enableObstacles = false;
	
	private List<OBB> wallBounds;
	public BlockFort fort;
	
	
	/** Music Fields **/
	private boolean musicPlaying = false;
	private static final String MUTE_CITY =
			"file:///" + System.getProperty("user.dir") + "//music//Mute City.mp3";
	
	
	private List<Particle> particles = new ArrayList<Particle>();
	public List<ParticleGenerator> generators = new ArrayList<ParticleGenerator>();

	
	private boolean enableItems = true;
	private Queue<Integer> itemQueue = new ArrayBlockingQueue<Integer>(100);
	private List<Item> itemList = new ArrayList<Item>();
	
	
	private int boostCounter = 0;
	public boolean enableMotionBlur = true;
	
	
	public boolean enableTerrain = true;
	
	private Terrain terrain;
	private TerrainPatch[] terrainPatches;
	public List<BillBoard> foliage;
	
	public String terrainCommand = "";
	public static final String DEFAULT_TERRAIN = "128 1000 0 6 18 0.125 1.0";
	
	public boolean enableReflection = false;
	public float opacity = 0.50f;
	
	private float ry = 0;
	
	public boolean smoothBound = false;
	public boolean multisample = true;
	
	public boolean testMode = false;
	public boolean printVersion = false;
	
	public int selectX = -1;
	public int selectY = -1;
	public int selected = 0;
	private IntBuffer selectBuffer;
	private static final int BUFFER_SIZE = 512;
	
	public boolean mousePressed  = false;
	public boolean enableRetical = true;
	public float retical = 50;
	
	public float[] quadratic;
	
	public boolean enableLightning = false;
	public Blizzard blizzard;
	public int flakeLimit = 10000;
	public boolean enableBlizzard = false;
	
	
	public Scene()
	{
		try
		{		
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
			{
		        if ("Nimbus".equals(info.getName()))
		        {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
			}
		}
		catch (Exception e) { e.printStackTrace(); }
		
		frame = new JFrame();
		
		GLCapabilities capabilities = new GLCapabilities(GLProfile.getDefault());
		capabilities.setStencilBits(8);
		capabilities.setDoubleBuffered(true);
		capabilities.setSampleBuffers(true);
		
		canvas = new GLCanvas(capabilities);
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
		
		// ensure that menu items are not displayed  the GL canvas
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
		
		setupMenu();
		
		frame.setJMenuBar(menuBar);
		
		console = new Console(this);
		
		consoleInput = new JTextField();
		consoleInput.addActionListener(this);
		consoleInput.setActionCommand("console");
		
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

	private void setupMenu()
	{
		menuBar = new JMenuBar();
		
		/** File Menu **/
		menu_file = new JMenu("File");
		menu_file.setMnemonic(KeyEvent.VK_F);

		menu_load = new JMenu("Open");
		menu_load.setMnemonic(KeyEvent.VK_O);
		
		menuItem_project = new JMenuItem("Project", KeyEvent.VK_P);
		menuItem_project.addActionListener(this);
		menuItem_project.setActionCommand("load_project");
		
		menuItem_game = new JMenuItem("Game", KeyEvent.VK_G);
		menuItem_game.addActionListener(this);
		menuItem_game.setActionCommand("load_game");
		
		menuItem_close = new JMenuItem("Close", KeyEvent.VK_C);
		menuItem_close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		menuItem_close.addActionListener(this);
		menuItem_close.setActionCommand("close");
		
		menu_load.add(menuItem_project);
		menu_load.add(menuItem_game);
		
		menu_file.add(menu_load);
		menu_file.addSeparator();
		menu_file.add(menuItem_close);
		
		menuBar.add(menu_file);
		/**----------------**/
		
		/** Controls Menu **/
		menu_control = new JMenu("Control");
		menu_control.setMnemonic(KeyEvent.VK_C);
		
		menuItem_reverse = new JCheckBoxMenuItem("Invert Reverse");
		menuItem_reverse.addItemListener(this);
		menuItem_reverse.setMnemonic(KeyEvent.VK_I);
		menuItem_reverse.setSelected(false);
		
		menu_control.add(menuItem_reverse);
		
		menuBar.add(menu_control);
		/**----------------**/
		
		/** Render Menu **/
		menu_render = new JMenu("Render");
		menu_render.setMnemonic(KeyEvent.VK_R);
		
		menu_quality = new JMenu("Quality");
		menu_quality.setMnemonic(KeyEvent.VK_Q);
		
		menuItem_multisample = new JCheckBoxMenuItem("Multisample");
		menuItem_multisample.addItemListener(this);
		menuItem_multisample.setMnemonic(KeyEvent.VK_M);
		menuItem_multisample.setSelected(multisample);
		
		menuItem_anisotropic = new JCheckBoxMenuItem("Anisotropic Filter");
		menuItem_anisotropic.addItemListener(this);
		menuItem_anisotropic.setMnemonic(KeyEvent.VK_A);
		menuItem_anisotropic.setSelected(Renderer.anisotropic);
		
		menu_quality.add(menuItem_multisample);
		menu_quality.add(menuItem_anisotropic);
		
		menu_render.add(menu_quality);
		
		menu_effects = new JMenu("Effects");
		menu_effects.setMnemonic(KeyEvent.VK_E);
		
		menuItem_motionblur = new JCheckBoxMenuItem("Motion Blur");
		menuItem_motionblur.addItemListener(this);
		menuItem_motionblur.setMnemonic(KeyEvent.VK_B);
		menuItem_motionblur.setSelected(enableMotionBlur);
		
		menuItem_fog = new JCheckBoxMenuItem("Fog");
		menuItem_fog.addItemListener(this);
		menuItem_fog.setMnemonic(KeyEvent.VK_F);
		menuItem_fog.setSelected(enableFog);
		
		menu_weather = new JMenu("Weather");
		menu_weather.setMnemonic(KeyEvent.VK_W);
		
		ButtonGroup group_weather = new ButtonGroup();
		
		menuItem_none = new JRadioButtonMenuItem("None");
		menuItem_none.addActionListener(this);
		menuItem_none.setSelected(true);
		menuItem_none.setMnemonic(KeyEvent.VK_N);
		menuItem_none.setActionCommand("no_weather");
		group_weather.add(menuItem_none);

		menuItem_snow = new JRadioButtonMenuItem("Snow");
		menuItem_snow.addActionListener(this);
		menuItem_snow.setSelected(false);
		menuItem_snow.setMnemonic(KeyEvent.VK_S);
		menuItem_snow.setActionCommand("snow");
		group_weather.add(menuItem_snow);
		
		menuItem_rain = new JRadioButtonMenuItem("Rain");
		menuItem_rain.addActionListener(this);
		menuItem_rain.setSelected(false);
		menuItem_rain.setMnemonic(KeyEvent.VK_R);
		menuItem_rain.setActionCommand("rain");
		group_weather.add(menuItem_rain);
		
		menu_weather.add(menuItem_none);
		menu_weather.add(menuItem_snow);
		menu_weather.add(menuItem_rain);
		
		menu_effects.add(menuItem_motionblur);
		menu_effects.add(menuItem_fog);
		menu_effects.add(menu_weather);
		
		menu_render.add(menu_effects);
		
		menuBar.add(menu_render);
		/**-------------------**/
		
		/** Light Menu **/
		menu_light = new JMenu("Light");
		menu_light.setMnemonic(KeyEvent.VK_L);
		
		menuItem_normalize = new JCheckBoxMenuItem("Normalize");
		menuItem_normalize.addItemListener(this);
		menuItem_normalize.setMnemonic(KeyEvent.VK_N);
		menuItem_normalize.setSelected(normalize);
		
		menuItem_smooth = new JCheckBoxMenuItem("Smooth");
		menuItem_smooth.addItemListener(this);
		menuItem_smooth.setMnemonic(KeyEvent.VK_S);
		
		menuItem_secondary = new JCheckBoxMenuItem("Specular Texture");
		menuItem_secondary.addItemListener(this);
		
		menu_light.add(menuItem_normalize);
		menu_light.add(menuItem_smooth);
		menu_light.add(menuItem_secondary);
		
		menuBar.add(menu_light);
		/**------------------**/
		
		/** Terrain Menu **/
		menu_terrain = new JMenu("Terrain");
		menu_terrain.setMnemonic(KeyEvent.VK_T);
		
		menuItem_water = new JCheckBoxMenuItem("Display Water");
		menuItem_water.addItemListener(this);
		menuItem_water.setMnemonic(KeyEvent.VK_W);
		menuItem_water.setSelected(false);

		menu_terrain.add(menuItem_water);
		
		menuBar.add(menu_terrain);
		/**------------------**/
		
		/** Quadtree Menu **/
		menu_quadtree = new JMenu("Quadtree");
		menu_quadtree.setMnemonic(KeyEvent.VK_Q);
		
		menuItem_solid = new JCheckBoxMenuItem("Fill Geometry");
		menuItem_solid.addItemListener(this);
		menuItem_solid.setMnemonic(KeyEvent.VK_F);
		menuItem_solid.setSelected(Quadtree.solid);
		
		menuItem_frame = new JCheckBoxMenuItem("Show Wireframe");
		menuItem_frame.addItemListener(this);
		menuItem_frame.setMnemonic(KeyEvent.VK_W);
		menuItem_frame.setSelected(Quadtree.frame);
		
		menu_quadtree.add(menuItem_solid);
		menu_quadtree.add(menuItem_frame);
		
		menuBar.add(menu_quadtree);
		/**------------------**/
	}
	
	public static void main(String[] args) { new Scene(); }
	
	public int getFrameRate() { return frameRate; }
	
	public int getHeight() { return canvasHeight; }
	
	public int getWidth()  { return canvasWidth;  }
	
	private void printVersion(GL2 gl)
	{
		System.out.println();
		
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
		
		GL2 gl = drawable.getGL().getGL2();
		glu = new GLUgl2();
		glut = new GLUT();
		
		loadTextures(gl);
		
		// may provide extra speed depending on machine
		gl.setSwapInterval(0);
		
		gl.glClearStencil(0);
		gl.glEnable(GL2.GL_STENCIL_TEST);
		
		quadratic = new float[] {0.0f, 0.0f, 0.005f};
		
		gl.glPointSize(3);
		gl.glPointParameterfv(GL2.GL_POINT_DISTANCE_ATTENUATION, quadratic, 0);
		
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

		/** Texture Options **/
		gl.glEnable(GL2.GL_TEXTURE_2D);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_BASE_LEVEL, 0);
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_MAX_LEVEL,  4);
		
		// For toon shading
		gl.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
		gl.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
		gl.glTexParameteri(GL2.GL_TEXTURE_1D, GL2.GL_TEXTURE_WRAP_S,     GL2.GL_CLAMP);

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
	    menuItem_smooth.setSelected(light.smooth);
	    menuItem_secondary.setSelected(light.secondary);
	    
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		
		long setupStart = System.currentTimeMillis();
		
		/** Model Setup **/
		environmentFaces = OBJParser.parseTriangles("environment");
		floorFaces       = OBJParser.parseTriangles("floor");
	    
	    environmentList = gl.glGenLists(1);
	    gl.glNewList(environmentList, GL2.GL_COMPILE);
	    displayTexturedObject(gl, environmentFaces);
	    gl.glEndList();
	    
	    floorList = gl.glGenLists(1);
	    gl.glNewList(floorList, GL2.GL_COMPILE);
	    displayTexturedObject(gl, floorFaces);
	    gl.glEndList();
	    
	    if(enableItems) loadItems(gl);
	    loadParticles();
	    
	    setupGenerators();
	    
	    loadPlayers(gl);
		    
	    itemBoxes.addAll(ItemBox.generateDiamond( 56.25f, 30f, particles));
	    itemBoxes.addAll(ItemBox.generateSquare (101.25f, 60f, particles));
	    itemBoxes.addAll(ItemBox.generateSquare (123.75f, 30f, particles));
	    itemBoxes.addAll(ItemBox.generateDiamond(   180f,  0f, particles));
	    
	    fort = new BlockFort(gl);
	    
//	    setupShadow(gl);
	    
	    wallBounds = BoundParser.parseOBBs("bound/environment.bound");
	    
	    frameTimes  = new long[240];
	    renderTimes = new long[240][RENDER_HEADERS.length];
	    updateTimes = new long[240][UPDATE_HEADERS.length];
	    
	    if(printVersion) printVersion(gl);
	    
	    console.parseCommand("profile project");
	    
	    generateTerrain(gl, DEFAULT_TERRAIN);
	    
	    long setupEnd = System.currentTimeMillis();
	    System.out.println("\nSetup Time: " + (setupEnd - setupStart) + " ms" + "\n");
	    
	    startTime = System.currentTimeMillis();
	    //records the time prior to the rendering of the first frame after initialization
	}

	private void setupGenerators()
	{
		
	}

	private void loadPlayers(GL2 gl)
	{
		cars.add(new Car(gl, new float[] { 78.75f, 1.8f, 0}, 0,   0, 0, this));
	    
	    if(GamePad.numberOfGamepads() > 1)
	    	cars.add(new Car(gl, new float[] {-78.75f, 1.8f, 0}, 0, 180, 0, this));
	    
	    if(GamePad.numberOfGamepads() > 2)
	    	cars.add(new Car(gl, new float[] {0, 1.8f,  78.75f}, 0, 270, 0, this));
	    	
	    if(GamePad.numberOfGamepads() > 3)
		    cars.add(new Car(gl, new float[] {0, 1.8f, -78.75f}, 0,  90, 0, this));
	}

	private void loadTextures(GL2 gl)
	{
		try
		{			
			brickWall = TextureIO.newTexture(new File("tex/longBrick.jpg"), true);
			brickWall.setTexParameterf(gl, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT, 16);
			brickWall.setTexParameterf(gl, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR);
			brickWall.setTexParameterf(gl, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
			
			brickWallTop = TextureIO.newTexture(new File("tex/longBrickTop.jpg"), false);
			
			cobble       = TextureIO.newTexture(new File("tex/cobbles.jpg"), true);
		}
		catch (Exception e) { e.printStackTrace(); }
	}

	private void loadParticles()
	{
		new BoostParticle(ORIGIN, null, 0, 0, 0, false, false);
	    new LightningParticle(ORIGIN);
	    new StarParticle(ORIGIN, null, 0, 0);
	}

	private void loadItems(GL2 gl)
	{
		new GreenShell (gl, this, null, 0, false);
	    new RedShell   (gl, this, null, 0, false); 
	    new BlueShell  (gl, this, null, 0);
	    new FakeItemBox(gl, this, null);
	    new Banana     (gl, this, null, 0);
	}
	
	private Texture shadowTexture;
	
	public void setupShadow(GL2 gl)
	{
		IntBuffer ib = Buffers.newDirectIntBuffer(1);
	    gl.glGenTextures(1, ib);
	    ib.position(0);
	    
	    shadowTexture = new Texture(ib.get());
	    
	    shadowTexture.setTexParameterf(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
	    shadowTexture.setTexParameterf(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
	    
	    shadowTexture.setTexParameterf(gl, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
	    shadowTexture.setTexParameterf(gl, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
	    
	    shadowTexture.setTexParameterf(gl, GL2.GL_DEPTH_TEXTURE_MODE, GL2.GL_INTENSITY);
	    
	    // ambient shadow supported 
	    shadowTexture.setTexParameterf(gl, GL2.GL_TEXTURE_COMPARE_FAIL_VALUE_ARB, 0.5f);
	    
	    generateShadowMap(gl);
	}

	public void display(GLAutoDrawable drawable)
	{	
		GL2 gl = drawable.getGL().getGL2();
		
		gl.glClearDepth(1.0f);
		gl.glClearColor(background[0], background[1], background[2], 1.0f);
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL2.GL_STENCIL_BUFFER_BIT);
		
		gl.glLoadIdentity();
		gl.glMatrixMode(GL_MODELVIEW);
		
		if(multisample) gl.glEnable(GL2.GL_MULTISAMPLE);
		else gl.glDisable(GL2.GL_MULTISAMPLE);
		
		if(normalize) gl.glEnable(GL2.GL_NORMALIZE);
		else gl.glDisable(GL2.GL_NORMALIZE);
		
		gl.glPointParameterfv(GL2.GL_POINT_DISTANCE_ATTENUATION, quadratic, 0);
		
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
			
			setupViewport(gl, index);
			car.setupCamera(gl, glu);
			
			light.setup(gl, headlight);
				
			if(headlight)
			{
				float[][] vectors = car.getLightVectors();
					
				light.direction = vectors[1];
				light.setPosition(vectors[0]);
			}
			
//			displayShadow(gl);
		
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
			
			if(displayLight) light.render(gl, glu);
			
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
		
//		gl.glDisable(GL2.GL_TEXTURE_GEN_S);
//      gl.glDisable(GL2.GL_TEXTURE_GEN_T);
//      gl.glDisable(GL2.GL_TEXTURE_GEN_R);
//      gl.glDisable(GL2.GL_TEXTURE_GEN_Q);
		
		calculateFPS();
	}

	public void displayShadow(GL2 gl)
	{
		gl.glTexGeni(GL2.GL_S, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_EYE_LINEAR);
		gl.glTexGeni(GL2.GL_T, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_EYE_LINEAR);
		gl.glTexGeni(GL2.GL_R, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_EYE_LINEAR);
		gl.glTexGeni(GL2.GL_Q, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_EYE_LINEAR);
		
		gl.glActiveTexture(GL2.GL_TEXTURE1);
		gl.glEnable(GL2.GL_TEXTURE_2D);
		shadowTexture.bind(gl);
		
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
		shadowTexture.setTexParameterf(gl, GL2.GL_TEXTURE_COMPARE_MODE, GL2.GL_COMPARE_R_TO_TEXTURE);

		// Set up the eye plane for projecting the shadow map on the scene
		gl.glEnable(GL2.GL_TEXTURE_GEN_S);
		gl.glEnable(GL2.GL_TEXTURE_GEN_T);
		gl.glEnable(GL2.GL_TEXTURE_GEN_R);
		gl.glEnable(GL2.GL_TEXTURE_GEN_Q);
		
		FloatBuffer shadow = Buffers.newDirectFloatBuffer(shadowMatrix);
		shadow.position(0);
		
		gl.glTexGenfv(GL2.GL_S, GL2.GL_EYE_PLANE, shadow); shadow.position( 4);
		gl.glTexGenfv(GL2.GL_T, GL2.GL_EYE_PLANE, shadow); shadow.position( 8);
		gl.glTexGenfv(GL2.GL_R, GL2.GL_EYE_PLANE, shadow); shadow.position(12);
		gl.glTexGenfv(GL2.GL_Q, GL2.GL_EYE_PLANE, shadow);
		
		gl.glActiveTexture(GL2.GL_TEXTURE0);
	}
	
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		GL2 gl = drawable.getGL().getGL2();
	
		if (height <= 0) height = 1;
		
		canvasHeight = height;
		canvasWidth = width;
		
		cars.get(0).camera.setDimensions(width, height);
		
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
		if(enableLightning)
		{
			if(cloudDensity < 1) cloudDensity += DENSITY_INC;
			
			float c = cloudDensity;
			gl.glColor3f(c, c, c); // affect the color of the environment in addition to fog
			gl.glFogfv(GL_FOG_COLOR, new float[] {c, c, c, 1}, 0);
		}
		else gl.glFogfv(GL_FOG_COLOR, fogColor, 0);
		
		if(enableFog) gl.glFogf(GL_FOG_DENSITY, fogDensity);
		else gl.glFogf(GL_FOG_DENSITY, 0);
	}

	private void registerItems(GL2 gl)
	{
		for(Car car : cars)
		{
			while(!car.getItemCommands().isEmpty())
			{
				int itemID = car.getItemCommands().poll();
				
				if(itemID == 10) cloudDensity = MIN_DENSITY;
				else car.registerItem(gl, itemID);
			}
		}
	}

	private long update()
	{
		long start = System.currentTimeMillis();
		
		if(mousePressed) modifyTerrain();
		
		removeItems();
		
		Particle.removeParticles(particles);
		
		if(enableItemBoxes)
		{
			for(ItemBox box : itemBoxes) box.update(cars); 
		}
		
		updateItems();
		
		ItemBox.increaseRotation();
		FakeItemBox.increaseRotation();
		
		for(ParticleGenerator generator : generators)
			if(generator.update()) particles.addAll(generator.generate());
		
		for(Particle p : particles) p.update();
		
		if(enableBlizzard) blizzard.update();
		
		vehicleCollisions();
		for(Car car : cars) car.update();
		
		if(enableTerrain)
		{			
			long _start = System.nanoTime();
			
			List<BillBoard> toRemove = new ArrayList<BillBoard>();
			
			for(BillBoard b : foliage)
			{
				if(b.sphere.testOBB(cars.get(0).bound)) toRemove.add(b);
			}
			
			foliage.removeAll(toRemove);
			
			updateTimes[frameIndex][0] = System.nanoTime() - _start;
				
			terrainCollisions();
		}
		
		return System.currentTimeMillis() - start;
	}

	private void modifyTerrain()
	{
		Camera camera = cars.get(0).camera;
		
		Point point = canvas.getMousePosition();
		int x = (int) point.getX();
		int y = (int) point.getY();
		
		if(camera.isAerial())
		{
			float[] p = camera.to3DPoint(x, y, canvasWidth, canvasHeight);
			float r = camera.getRadius(retical, canvasHeight);
			terrain.tree.createHill(p, r, 0.5f);
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
		
		itemCollisions();
	}

	private void itemCollisions()
	{
		List<Item> allItems = new ArrayList<Item>();
		
		allItems.addAll(itemList);
		
		for(Car car : cars)
			allItems.addAll(car.getItems());
		
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
			ry++; ry %= 360;
			
			gl.glTranslatef(0, 30, 0);
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
	private void setupViewport(GL2 gl, int playerID)
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
			
			renderParticles(gl, car); // TODO Weather effects do not work with reflection
			Particle.resetTexture();
		}
		gl.glPopMatrix();
		
		renderFloor(gl);
		
		light.setPosition(p);
		
		gl.glColor3f(1, 1, 1);
	}

	private void renderFloor(GL2 gl)
	{
		gl.glColor4f(0.75f, 0.75f, 0.75f, opacity);
		
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_BLEND);
		
		gl.glPushMatrix();
		{
//			gl.glTranslatef(0, 0, 0);
//			gl.glScalef(40.0f, 40.0f, 40.0f);
//	
//			gl.glCallList(floorList);
			
			terrain.water.render(gl);
			terrain.water.offsetHeights();
		}	
		gl.glPopMatrix();
		
		gl.glDisable(GL2.GL_BLEND);
		gl.glEnable(GL2.GL_LIGHTING);
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
		
		if(!enableReflection && !enableTerrain && !testMode)
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
			gl.glDisable(GL2.GL_LIGHTING);
			light.useSpecular(gl, false);
			
			terrain.render(gl, glut);
			
			gl.glEnable(GL2.GL_LIGHTING);
			light.useSpecular(gl, true);
		}	
		gl.glPopMatrix();
			
		if(!terrain.enableQuadtree)
		{
			gl.glPushMatrix();
			{
				gl.glScalef(Terrain.sx, Terrain.sy, Terrain.sz);
				
				for(TerrainPatch shape : terrainPatches) shape.render(gl);
			}	
			gl.glPopMatrix();
		}
		
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
		
		if(enableBlizzard) blizzard.render(gl);
		
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
		
		for(Particle particle : particles)
		{
			if(car.isSlipping()) particle.render(gl, car.slipTrajectory);
			else particle.render(gl, car.trajectory);
		}
		
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		
		return System.nanoTime() - start;
	}
	
	public int foliageMode = 0;

	private long renderFoliage(GL2 gl, Car car)
	{
		long start = System.nanoTime();
		
		switch(foliageMode)
		{
			case 0: 
			{
				gl.glPushMatrix();
				{	
					for(BillBoard b : foliage)
					{
						if(car.isSlipping()) b.render(gl, car.slipTrajectory);
						else b.render(gl, car.trajectory);
					}
				}	
				gl.glPopMatrix();
				
				break;
			}
			case 1: BillBoard.renderPoints(gl, foliage); break;
			case 2: BillBoard.renderQuads(gl, foliage, car.trajectory); break;
		}
		
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
				bound.displaySolid(gl, glut, RGB.toRGBAi(RGB.VIOLET, 0.1f));
		
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
		
	}
	
	private float[] shadowMatrix = new float[16];
	
	public void generateShadowMap(GL2 gl)
	{
	    float distance, near, fov;
	    
	    float[] modelview  = new float[16];
	    float[] projection = new float[16];
	    
	    float radius = 300.0f; // based on objects in scene

	    float[] p = light.getPosition();
	    
	    // Save the depth precision for where it's useful
	    distance = (float) Math.sqrt(p[0] * p[0] +  p[1] * p[1] + p[2] * p[2]);

	    near = distance - radius;
	    // Keep the scene filling the depth texture
	    fov = (float) Math.toDegrees(2.0f * Math.atan(radius / distance));

	    gl.glMatrixMode(GL_PROJECTION);
	    gl.glLoadIdentity();
	    glu.gluPerspective(fov, 1.0f, near, near + (2.0f * radius));
	    
	    gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, projection, 0);
	    
	    // Switch to light's point of view
	    gl.glMatrixMode(GL_MODELVIEW);
	    gl.glLoadIdentity();
	    glu.gluLookAt(p[0], p[1], p[2], 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
	    
	    gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, modelview, 0);
	    
	    gl.glViewport(0, 0, canvasWidth, canvasHeight);

	    // Clear the depth buffer only
	    gl.glClear(GL_DEPTH_BUFFER_BIT);

	    // All we care about here is resulting depth values
	    gl.glShadeModel(GL2.GL_FLAT);
	    gl.glDisable(GL2.GL_LIGHTING);
	    gl.glDisable(GL2.GL_COLOR_MATERIAL);
	    gl.glDisable(GL2.GL_NORMALIZE);
	    gl.glColorMask(false, false, false, false);

	    // Overcome imprecision
	    gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);

	    renderObstacles(gl);

	    // Copy depth values into depth texture
	    gl.glCopyTexImage2D(GL_TEXTURE_2D, 0, GL2.GL_DEPTH_COMPONENT, 0, 0, canvasWidth, canvasHeight, 0);

	    // Restore normal drawing state
	    gl.glShadeModel(GL2.GL_SMOOTH);
	    gl.glEnable(GL2.GL_LIGHTING);
	    gl.glEnable(GL2.GL_COLOR_MATERIAL);
	    gl.glEnable(GL2.GL_NORMALIZE);
	    gl.glColorMask(true, true, true, true);
	    
	    gl.glDisable(GL2.GL_POLYGON_OFFSET_FILL);

	    // Set up texture matrix for shadow map projection,
	    // which will be rolled into the eye linear
	    // texture coordinate generation plane equations
	    float[][] temp = Matrix.IDENTITY_MATRIX_44;

	    Matrix.translateMatrix44(temp, 0.5f, 0.5f, 0.5f);
	    Matrix.scale(temp, 0.5f);
	    shadowMatrix = Matrix.toVector(Matrix.multiply(temp, Matrix.toMatrix(projection)));
	    temp = Matrix.multiply(Matrix.toMatrix(shadowMatrix), Matrix.toMatrix(modelview));
	    // transpose to get the s, t, r, and q rows for plane equations
	    shadowMatrix = Matrix.toVector(Matrix.transpose(temp));
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
	
	public static boolean outOfBounds(float[] p)
	{
		return dot(p, p) > GLOBAL_RADIUS * GLOBAL_RADIUS;
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

	public Terrain getTerrain() { return terrain; }
	
	public void generateTerrain(GL2 gl, String command)
	{
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
			
			long start = System.currentTimeMillis();
			
			terrain = new Terrain(gl, length, iterations, r0, r1, p, h);
			
			long end = System.currentTimeMillis();
			
			System.out.printf("Terrain Generated: (%d) %d ms\n", terrain.length * terrain.length, (end - start));
			
			terrainPatches = new TerrainPatch[splashes];
	    
			for (int i = 0; i < terrainPatches.length; i++)
				terrainPatches[i] = new TerrainPatch(null, terrain.heights, generator.nextInt(15) + 5);
			
			System.out.printf("Patches Generated: %d ms\n", (System.currentTimeMillis() - end));
		}
		else
		{
			int length     = Integer.parseInt(args[0]);
			int iterations = Integer.parseInt(args[1]);
			int splashes   = Integer.parseInt(args[2]);
			
			long start = System.currentTimeMillis();
			
			terrain = new Terrain(gl, length, iterations);
			
			long end = System.currentTimeMillis();
			
			System.out.printf("Terrain Generated: (%d) %d ms\n", terrain.length * terrain.length, (end - start));
			
			terrainPatches = new TerrainPatch[splashes];
	    
			for (int i = 0; i < terrainPatches.length; i++)
				terrainPatches[i] = new TerrainPatch(null, terrain.heights, generator.nextInt(15) + 5);
			
			System.out.printf("Patches Generated: %d ms\n", (System.currentTimeMillis() - end));
		}
		
		long start = System.currentTimeMillis();
			
		generateFoliage(60, 10, 30);
		
		System.out.printf("Foliage Generated: (%d) %d ms\n", foliage.size(), (System.currentTimeMillis() - start));
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
	    		p[1] = (terrain.enableQuadtree) ? terrain.tree.getCell(p, terrain.tree.detail).getHeight(p) : terrain.getHeight(p);
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
		    		
		    		if(Math.abs(p0[0]) < 200 && Math.abs(p0[2]) < 200)
		    		{
		    			p0[1] = (terrain.enableQuadtree) ? terrain.tree.getCell(p0, terrain.tree.detail).getHeight(p0) : terrain.getHeight(p0);
		    			foliage.add(new BillBoard(p0, t));
		    		}
		    	}
	    	}
	    }
	}
	
	public void updateFoliage()
	{
		for(BillBoard board : foliage)
		{
			float[] p = board.sphere.c;
			board.sphere.c[1] = (terrain.enableQuadtree) ? terrain.tree.getCell(p, terrain.tree.detail).getHeight(p) : terrain.getHeight(p);
		}
	}

	public void printDataToFile(String file, String[] headers, long[][] data) //TODO Complete file format
	{
		try
		{
			Calendar now = Calendar.getInstance();
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
			
			String _execution = new SimpleDateFormat("yyyyMMddHHmmss").format(execution.getTime());
			
			FileWriter writer = new FileWriter("output/" + (file == null ? _execution : file) + ".txt");
			BufferedWriter out = new BufferedWriter(writer);
			
			out.write("Values recorded at: " + dateFormat.format(now.getTime()) + "\r\n\r\n ");
			
			for(int c = 0; c < headers.length; c++)
				out.write(String.format("%11s", headers[c]) + "\t");
			
			out.write("\r\n\r\n ");
			
			for(int i = 0; i < data.length; i++)
			{
				for(int j = 0; j < headers.length; j++)
				{
					out.write(String.format("%11.3f", data[i][j] / 1E6) + "\t");
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
			case KeyEvent.VK_H:  enableObstacles = !enableObstacles; break;
			
			case KeyEvent.VK_T:  enableTerrain = !enableTerrain; cars.get(0).friction = 1; break; //TODO Only includes player 1
			case KeyEvent.VK_O:  enableItemBoxes = !enableItemBoxes; break;
			case KeyEvent.VK_I:  spawnItemsInSphere(8, 10, new float[] {0, 100, 0}, 50); break;
			case KeyEvent.VK_U:  spawnItemsInOBB(0, 10, new float[] {0, 100, 0}, ORIGIN, new float[] {150, 50, 150}); break;
			
			case KeyEvent.VK_SLASH: testMode = !testMode; break;
			
			case KeyEvent.VK_L        : printDataToFile(null, RENDER_HEADERS, renderTimes); break;
			case KeyEvent.VK_SEMICOLON: printDataToFile(null, UPDATE_HEADERS, updateTimes); break;
			
			case KeyEvent.VK_DELETE: clearItems(); break;
			
			case KeyEvent.VK_X:  moveLight = !moveLight; break;
	 
			case KeyEvent.VK_F12: enableAnimation = !enableAnimation; break;
	
			case KeyEvent.VK_1:  enableOBBAxes       = !enableOBBAxes;       break;
			case KeyEvent.VK_2:  enableOBBVertices   = !enableOBBVertices;   break;
			case KeyEvent.VK_3:  enableOBBWireframes = !enableOBBWireframes; break;
			case KeyEvent.VK_4:	 enableOBBSolids     = !enableOBBSolids;     break;
			case KeyEvent.VK_5:	 enableClosestPoints = !enableClosestPoints; break;
			
			case KeyEvent.VK_F1 : fort.renderMode++; fort.renderMode %= 4; break;
			case KeyEvent.VK_F3 : Item.renderMode++; Item.renderMode %= 2; break;
			case KeyEvent.VK_Z  : foliageMode++; foliageMode %= 3; break;
			
			case KeyEvent.VK_6:  Item.toggleBoundSolids(); break;
			case KeyEvent.VK_7:  Item.toggleBoundFrames(); break;
			
			case KeyEvent.VK_8:  fort.displayModel = !fort.displayModel; break;
			case KeyEvent.VK_0:  displaySkybox = !displaySkybox; break;
			
			case KeyEvent.VK_J:
			{
				terrain.keyPressed(e);
				background = RGB.BLACK;
				fogDensity = 0.01f;
				fogColor = new float[] {0, 0, 0, 0};
				displaySkybox = false;
				break;
			}
			case KeyEvent.VK_K:
			{
				terrain.keyPressed(e);
				background = RGB.SKY_BLUE;
				fogDensity = 0.004f;
				fogColor = new float[] {1, 1, 1, 1};
				displaySkybox = true;
				break;
			}	
			case KeyEvent.VK_EQUALS       :
			case KeyEvent.VK_MINUS        : terrain.keyPressed(e); updateFoliage(); break;
			case KeyEvent.VK_OPEN_BRACKET :
			case KeyEvent.VK_CLOSE_BRACKET:
			case KeyEvent.VK_QUOTE        :
			case KeyEvent.VK_NUMBER_SIGN  :
			case KeyEvent.VK_PERIOD       :
			case KeyEvent.VK_P            : terrain.keyPressed(e); break; 
			case KeyEvent.VK_COMMA        : terrain.keyPressed(e); generateFoliage(60, 10, 30); break;
			
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
		     if(event.getActionCommand().equals("console"     )) console.parseCommand(consoleInput.getText());
		else if(event.getActionCommand().equals("load_project")) console.parseCommand("profile project");
		else if(event.getActionCommand().equals("load_game"   )) console.parseCommand("profile game");
		else if(event.getActionCommand().equals("no_weather"  )) enableBlizzard = false;
		else if(event.getActionCommand().equals("snow"        )) { enableBlizzard = true; blizzard = new Blizzard(this, flakeLimit, new float[] {0.2f, -1.5f, 0.1f}, StormType.SNOW); }
		else if(event.getActionCommand().equals("rain"        )) { enableBlizzard = true; blizzard = new Blizzard(this, flakeLimit, new float[] {0.0f, -4.0f, 0.0f}, StormType.RAIN); }
		else if(event.getActionCommand().equals("close"       )) System.exit(0);
	}
	
	public KeyEvent pressKey(char c)
	{
		long when = System.nanoTime();
		int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
		
		return new KeyEvent(consoleInput, KeyEvent.KEY_PRESSED, when, 0, keyCode, c);
	}
	
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		Camera camera = cars.get(0).camera;
		
		if(camera.isAerial())
		{
			retical += e.getWheelRotation() * 3;
			if(retical < 1) retical = 1;
		}
		else if(camera.isDynamic()) camera.zoom(e.getWheelRotation());
	}

	public void mouseClicked(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited (MouseEvent e) {}

	public void mousePressed(MouseEvent e)
	{
		selectX = e.getX();
		selectY = e.getY();
		
		mousePressed = true;
	}

	public void mouseReleased(MouseEvent e) { mousePressed = false; }

	public void itemStateChanged(ItemEvent ie)
	{
		Object source = ie.getItemSelectable();
		boolean selected = (ie.getStateChange() == ItemEvent.SELECTED);
		
		     if(source.equals(menuItem_multisample)) multisample               = selected;
		else if(source.equals(menuItem_anisotropic)) Renderer.anisotropic      = selected; 
		else if(source.equals(menuItem_motionblur )) enableMotionBlur          = selected;
		else if(source.equals(menuItem_fog        )) enableFog                 = selected;    
		else if(source.equals(menuItem_normalize  )) normalize                 = selected;
		else if(source.equals(menuItem_smooth     )) light.smooth              = selected;
		else if(source.equals(menuItem_secondary  )) light.secondary           = selected;
		else if(source.equals(menuItem_water      )) terrain.enableWater       = selected;    
		else if(source.equals(menuItem_solid      )) Quadtree.solid            = selected;
		else if(source.equals(menuItem_frame      )) Quadtree.frame            = selected;
		else if(source.equals(menuItem_reverse    )) cars.get(0).invertReverse = selected;
	}
}
