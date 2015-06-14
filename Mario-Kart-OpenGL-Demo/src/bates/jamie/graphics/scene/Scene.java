package bates.jamie.graphics.scene;

import static bates.jamie.graphics.util.Renderer.displayTexturedCuboid;
import static javax.media.opengl.GL.GL_COLOR_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_TEST;
import static javax.media.opengl.GL.GL_LEQUAL;
import static javax.media.opengl.GL.GL_NICEST;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL2.GL_ACCUM_BUFFER_BIT;
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
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
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
import javax.swing.DefaultListModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import bates.jamie.graphics.collision.Bound;
import bates.jamie.graphics.collision.BoundParser;
import bates.jamie.graphics.collision.OBB;
import bates.jamie.graphics.collision.Sphere;
import bates.jamie.graphics.entity.Balloon;
import bates.jamie.graphics.entity.BillBoard;
import bates.jamie.graphics.entity.BlockFort;
import bates.jamie.graphics.entity.BrickBlock;
import bates.jamie.graphics.entity.BrickWall;
import bates.jamie.graphics.entity.EnergyField;
import bates.jamie.graphics.entity.EnergyField.FieldType;
import bates.jamie.graphics.entity.GoldCoin;
import bates.jamie.graphics.entity.GoldCoin.CoinType;
import bates.jamie.graphics.entity.GrassPatch;
import bates.jamie.graphics.entity.LightningStrike;
import bates.jamie.graphics.entity.LightningStrike.BoltType;
import bates.jamie.graphics.entity.LightningStrike.RenderStyle;
import bates.jamie.graphics.entity.Mushroom;
import bates.jamie.graphics.entity.PlaneMesh;
import bates.jamie.graphics.entity.PowerStar;
import bates.jamie.graphics.entity.Quadtree;
import bates.jamie.graphics.entity.QuestionBlock;
import bates.jamie.graphics.entity.ShineSprite;
import bates.jamie.graphics.entity.SkyBox;
import bates.jamie.graphics.entity.Terrain;
import bates.jamie.graphics.entity.TerrainPatch;
import bates.jamie.graphics.entity.Vehicle;
import bates.jamie.graphics.entity.Volume;
import bates.jamie.graphics.entity.WarpPipe;
import bates.jamie.graphics.entity.Water;
import bates.jamie.graphics.io.Console;
import bates.jamie.graphics.io.GamePad;
import bates.jamie.graphics.io.ModelSelecter;
import bates.jamie.graphics.item.Banana;
import bates.jamie.graphics.item.BlueShell;
import bates.jamie.graphics.item.BobOmb;
import bates.jamie.graphics.item.FakeItemBox;
import bates.jamie.graphics.item.FireBall;
import bates.jamie.graphics.item.GreenShell;
import bates.jamie.graphics.item.Item;
import bates.jamie.graphics.item.ItemBox;
import bates.jamie.graphics.item.RedShell;
import bates.jamie.graphics.particle.Blizzard;
import bates.jamie.graphics.particle.Blizzard.StormType;
import bates.jamie.graphics.particle.BoostParticle;
import bates.jamie.graphics.particle.Particle;
import bates.jamie.graphics.particle.ParticleEngine;
import bates.jamie.graphics.particle.ParticleGenerator;
import bates.jamie.graphics.scene.process.BloomStrobe;
import bates.jamie.graphics.scene.process.FocalBlur;
import bates.jamie.graphics.scene.process.Mirror;
import bates.jamie.graphics.scene.process.RainScreen;
import bates.jamie.graphics.scene.process.ShadowCaster;
import bates.jamie.graphics.scene.process.ShadowCaster.ShadowQuality;
import bates.jamie.graphics.sound.MP3;
import bates.jamie.graphics.util.Matrix;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.Renderer;
import bates.jamie.graphics.util.TextureLoader;
import bates.jamie.graphics.util.TimeQuery;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.shader.Shader;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;

//TODO Seidel's polygon triangulation algorithm

/**
 * @author Jamie Bates
 * @version 1.0 (22/02/2012)
 * 
 * This class creates a 3D scene which displays a car that can be moved along a
 * track by the use of hotkeys. Users can also interact with the car model and 
 * manipulation the scene in a number of ways.
 */
public class Scene implements GLEventListener, KeyListener, MouseWheelListener, MouseListener,
                              MouseMotionListener, ActionListener, ItemListener, ListSelectionListener,
                              ChangeListener, TreeSelectionListener
{
	private static final boolean SMOOTH_FPS = true;
	
	public static Scene singleton;
	
	private static final boolean USE_NIMBUS_STYLE = true;
	
	public JFrame frame;
	public GLCanvas canvas;
	
	private Console console;
	private JTextField command;
	
	private JSlider sliderEta;
	private JToggleButton shaderToggle;
	private JSpinner shaderSpinner;
	
	private JMenuBar menuBar;
	
	private JMenu menu_file;
	private JMenu menu_load;
	private JMenuItem menuItem_project;
	private JMenuItem menuItem_game;
	private JMenuItem menuItem_brimstone;
	private JMenuItem menuItem_simple;
	private JMenuItem menuItem_close;
	
	private JMenu menu_camera;
	private JCheckBoxMenuItem menuItem_shaking;
	private JCheckBoxMenuItem menuItem_trackball;
	private JCheckBoxMenuItem menuItem_focal_blur;
	private JCheckBoxMenuItem menuItem_mirage;
	
	private JMenu menu_vehicle;
	private JCheckBoxMenuItem menuItem_reverse;
	private JCheckBoxMenuItem menuItem_tag;
	private JMenu menu_car_material;
	private JMenuItem menuItem_car_color;
	private JCheckBoxMenuItem menuItem_cubemap;
	
	private JMenu menu_render;
	private JCheckBoxMenuItem menuItem_shaders;
	private JMenu menu_quality;
	private JCheckBoxMenuItem menuItem_multisample;
	private JCheckBoxMenuItem menuItem_anisotropic;
	private JMenu menu_effects;
	private JCheckBoxMenuItem menuItem_motionblur;
	private JCheckBoxMenuItem menuItem_aberration;
	private JCheckBoxMenuItem menuItem_bloom;
	private JMenu menu_shadows;
	private JCheckBoxMenuItem menuItem_shadows;
	private JMenu menu_shadow_quality;
	private JRadioButtonMenuItem menuItem_shadow_poor;
	private JRadioButtonMenuItem menuItem_shadow_norm;
	private JRadioButtonMenuItem menuItem_shadow_high;
	private JRadioButtonMenuItem menuItem_shadow_best;
	
	private JMenu menu_environment;
	private JMenuItem menuItem_background;
	private JCheckBoxMenuItem menuItem_fog;
	private JMenuItem menuItem_fogcolor;
	private JMenuItem menuItem_skycolor;
	private JMenuItem menuItem_horizon;
	private JMenu menu_weather;
	private JRadioButtonMenuItem menuItem_none;
	private JRadioButtonMenuItem menuItem_rain;
	private JRadioButtonMenuItem menuItem_snow;
	private JCheckBoxMenuItem menuItem_settle;
	private JCheckBoxMenuItem menuItem_splash;
	
	private JMenu menu_light;
	private JCheckBoxMenuItem menuItem_synch_light;
	private JMenuItem menuItem_ambience;
	private JMenuItem menuItem_emission;
	private JMenuItem menuItem_specular;
	private JMenuItem menuItem_diffuse;
	
	private JCheckBoxMenuItem menuItem_normalize;
	private JCheckBoxMenuItem menuItem_smooth;
	private JCheckBoxMenuItem menuItem_attenuate;
	private JCheckBoxMenuItem menuItem_local;
	private JCheckBoxMenuItem menuItem_secondary;
	
	private JMenu menu_terrain;
	private JCheckBoxMenuItem menuItem_water;
	
	private JMenu menu_quadtree;
	private JMenu menu_geometry;
	private JCheckBoxMenuItem menuItem_solid;
	private JCheckBoxMenuItem menuItem_frame;
	private JMenu menu_material;
	private JCheckBoxMenuItem menuItem_texturing;
	private JCheckBoxMenuItem menuItem_malleable;
	private JCheckBoxMenuItem menuItem_bumpmaps;
	private JCheckBoxMenuItem menuItem_caustics;
	private JMenuItem menuItem_shininess;
	private JMenu menu_coloring;
	private JCheckBoxMenuItem menuItem_vcoloring;
	private JMenu menu_normals;
	private JCheckBoxMenuItem menuItem_shading;
	private JMenuItem menuItem_tangent;
	private JMenuItem menuItem_recalculate;
	private JCheckBoxMenuItem menuItem_vnormals;
	private JCheckBoxMenuItem menuItem_vtangents;
	private JMenu menu_heights;
	private JCheckBoxMenuItem menuItem_elevation;
	private JMenuItem menuItem_height;
	private JMenuItem menuItem_reset;
	
	public static int canvasWidth  = 860;
	public static int canvasHeight = 640;
	
	public float far = 2000.0f;
	
	public static final int FPS     = 60;
	public static final int MIN_FPS = 30;
	
	private final FPSAnimator animator;
	
	public float fov = 90.0f;
	
	private GLU glu;
	private GLUT glut;
	
	public float[] background = RGB.BLACK;
	
	public static boolean enableAnimation = true;
	private boolean normalize = true;
	
	private Calendar execution;
	
	private int frames = 0;      // frame counter for current second
	private int frameRate = FPS; // FPS of previous second
	private long startTime;      // start time of current second
	private long renderTime;     // time at which previous frame was rendered 
	
	public static int frameIndex = 0;   //time independent frame counter (for FT graph)
	public long[] frameTimes;    //buffered frame times (for FT graph)
	public long[][] renderTimes; //buffered times for rendering each set of components
	public long[][] updateTimes; //buffered times for collision detection tests (for CT graph)
	
	public static final String[] RENDER_HEADERS =
		{"Terrain", "Foliage", "Vehicles", "Items", "Particles", "Bounds", "HUD"};
	
	public static final String[] UPDATE_HEADERS =
		{"Collision", "Weather", "Deformation", "Buffers"};
	
	public boolean enableCulling = false;
	
	
	/** Texture Fields **/
	private Texture brick_front;
	private Texture brick_side;
	
	private Texture brick_front_normal;
	private Texture brick_side_normal;
	
	private Texture brick_front_height;
	private Texture brick_side_height;
	
	private Texture floorBump;
	private Texture floorTex;
	
	public Texture rain_normal;
	public Texture pattern_mask;
	
	public Texture brickColour;
	public Texture brickNormal;
	public Texture brickHeight;
	
	
	
	/** Model Fields **/
	private EnergyField energyField;
	
	private List<Vehicle> cars = new ArrayList<Vehicle>();
	public boolean enableQuality = true;
	
	public static final float[] ORIGIN = {0.0f, 0.0f, 0.0f};
	public static final float GLOBAL_RADIUS = 300;

	private List<ItemBox> itemBoxes = new ArrayList<ItemBox>();
	public boolean enableItemBoxes = false;
	
	
	/** Light Fields **/
	public Light[] lights = new Light[1];
	public Light light;
	public int lightID = 0;
	
	public boolean singleLight  = false;
	public boolean moveLight    = false;
	public boolean displayLight = false;
	public boolean synchLights  = true;
	public boolean rimLighting  = false;
	
	
	/** Shadow Fields **/
	public ShadowCaster caster;
	public static boolean enableShadow = true;
	public boolean shadowMap   = false;
	public boolean resetShadow = false;
	
	/** Fog Fields **/
	public boolean enableFog = true;
	public float fogDensity = 0.004f;
	public float[] fogColor = {1.0f, 1.0f, 1.0f, 1.0f};
	
	private float cloudDensity = 1.0f;
    private static final float MIN_DENSITY = 0.5f;
    private static final float DENSITY_INC = 0.0125f;
	
    
    /** Environment Fields **/
    public SkyBox skybox;
    public boolean displaySkybox = true;
	public boolean sphereMap = false;
	
	/** Collision Detection Fields **/	
	private boolean enableOBBAxes       = false;
	private boolean enableOBBVertices   = false;
	private boolean enableOBBWireframes = false;
	private boolean enableOBBSolids     = false;
	private boolean enableClosestPoints = false;
	
	public boolean enableObstacles = false;
	
	private List<OBB> wallBounds;
	private OBB floorBound;
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
	
	
	public boolean enableBlur   = false;
	public boolean enableRadial = true;
	
	
	public boolean enableTerrain = true;
	
	private Terrain terrain;
	private TerrainPatch[] terrainPatches;
	public List<BillBoard> foliage;
	public GrassPatch[] grassPatches;
	
	public PlaneMesh planeMesh = new PlaneMesh(new Vec3(0, 20, 0), 420, 420, 128, 128, true);
	
	public String terrainCommand = "";
	public static final String DEFAULT_TERRAIN = "128 1000 0 6 18 0.125 1.0";
	
	public boolean enableReflection = false;
	public float opacity = 0.50f;
	
	public boolean smoothBound = false;
	public boolean multisample = true;
	
	public static boolean testMode = false;
	public boolean printVersion = true;
	public boolean printErrors  = false;
	
	public ModelSelecter selecter;
	
	public boolean mousePressed  = false;
	public boolean enableRetical = true;
	public float retical = 50;
	
	public float[] quadratic;
	
	public boolean enableLightning = false;
	public Blizzard blizzard;
	public int flakeLimit = 10000;
	public boolean enableBlizzard = false;
	
	public JList<String> quadList;
	public DefaultListModel<String> listModel;

	public Water water;
	public BloomStrobe bloom;
	public FocalBlur focalBlur;
	
	public boolean enableBloom = true;
	public static boolean enableParallax = true;
	public static boolean enableFocalBlur = true;
	
	DefaultMutableTreeNode shaderRoot;
	JTree shaderTree;
	
	DefaultMutableTreeNode selectedShader;
	DefaultMutableTreeNode selectedUniform;
	
	Volume volume;
	
	List<BrickBlock> brickBlocks = new ArrayList<BrickBlock>();
	public BrickWall brickWall;
	float brickScale = 3.75f;
	
	List<GoldCoin> coins = new ArrayList<GoldCoin>();
	
	
	
	
	public Scene()
	{
		try
		{	
			if(USE_NIMBUS_STYLE)
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
			else UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
		canvas.addMouseMotionListener(this);
		canvas.setFocusable(true);
		canvas.requestFocus();

		animator = new FPSAnimator(canvas, FPS, !SMOOTH_FPS);
		
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
		
		// ensure that menu items are not displayed above the GL canvas
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
		
		setupMenu();
		
		frame.setJMenuBar(menuBar);
		
		console = new Console(this);
		
		command = new JTextField();
		command.addActionListener(this);
		command.setActionCommand("console");
		
		command.setBackground(Color.DARK_GRAY);
		command.setForeground(Color.LIGHT_GRAY);
		command.setCaretColor(Color.WHITE);
		
		listModel = new DefaultListModel<String>();
		
		quadList = new JList<String>(listModel);
		quadList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		quadList.addListSelectionListener(this);
		
		shaderRoot = new DefaultMutableTreeNode("Shaders");
		shaderTree = new JTree(shaderRoot);
		shaderTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		shaderTree.addTreeSelectionListener(this);
		
		sliderEta = new JSlider();
		sliderEta.addChangeListener(this);
		
		shaderToggle = new JToggleButton();
		shaderToggle.addItemListener(this);
		
		SpinnerModel spinnerModel =
			new SpinnerNumberModel(0.0, 0.0, 100.0, 0.1);
		shaderSpinner = new JSpinner(spinnerModel);
		shaderSpinner.addChangeListener(this);
		
		JScrollPane pane = new JScrollPane(shaderTree);
		
		JPanel shaderPanel = new JPanel(new BorderLayout());
		shaderPanel.add(pane         , BorderLayout.NORTH);
		shaderPanel.add(shaderSpinner, BorderLayout.SOUTH);
		
		Container content = frame.getContentPane();
		
		content.setLayout(new BorderLayout());
		
		content.add(sliderEta, BorderLayout.EAST  );
		content.add(canvas,      BorderLayout.CENTER);
		content.add(command,     BorderLayout.SOUTH );

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
		
		menuItem_brimstone = new JMenuItem("Brimstone", KeyEvent.VK_B);
		menuItem_brimstone.addActionListener(this);
		menuItem_brimstone.setActionCommand("load_brimstone");
		
		menuItem_simple = new JMenuItem("Simple", KeyEvent.VK_S);
		menuItem_simple.addActionListener(this);
		menuItem_simple.setActionCommand("load_simple");
		
		menuItem_close = new JMenuItem("Close", KeyEvent.VK_C);
		menuItem_close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		menuItem_close.addActionListener(this);
		menuItem_close.setActionCommand("close");
		
		menu_load.add(menuItem_project);
		menu_load.add(menuItem_game);
		menu_load.add(menuItem_brimstone);
		menu_load.add(menuItem_simple);
		
		menu_file.add(menu_load);
		menu_file.addSeparator();
		menu_file.add(menuItem_close);
		
		menuBar.add(menu_file);
		/**----------------**/
		
		/** Camera Menu **/
		menu_camera = new JMenu("Camera");
		menu_camera.setMnemonic(KeyEvent.VK_C);
		
		menuItem_shaking = new JCheckBoxMenuItem("Camera Shake");
		menuItem_shaking.addItemListener(this);
		menuItem_shaking.setMnemonic(KeyEvent.VK_S);
		
		menuItem_trackball = new JCheckBoxMenuItem("Enable Trackball");
		menuItem_trackball.addItemListener(this);
		menuItem_trackball.setMnemonic(KeyEvent.VK_T);
		
		menuItem_focal_blur = new JCheckBoxMenuItem("Focal Blur");
		menuItem_focal_blur.addItemListener(this);
		menuItem_focal_blur.setMnemonic(KeyEvent.VK_B);
		
		menuItem_mirage = new JCheckBoxMenuItem("Heat Haze");
		menuItem_mirage.addItemListener(this);
		menuItem_mirage.setMnemonic(KeyEvent.VK_H);
		
		menu_camera.add(menuItem_shaking);
		menu_camera.add(menuItem_trackball);
		menu_camera.add(menuItem_focal_blur);
		menu_camera.add(menuItem_mirage);
		
		menuBar.add(menu_camera);
		/**----------------**/
		
		/** Controls Menu **/
		menu_vehicle = new JMenu("Vehicle");
		menu_vehicle.setMnemonic(KeyEvent.VK_V);
		
		menuItem_reverse = new JCheckBoxMenuItem("Invert Reverse");
		menuItem_reverse.addItemListener(this);
		menuItem_reverse.setMnemonic(KeyEvent.VK_I);
		menuItem_reverse.setSelected(false);
		
		menu_vehicle.add(menuItem_reverse);
		menu_vehicle.addSeparator();
		
		menuItem_tag = new JCheckBoxMenuItem("Show Label");
		menuItem_tag.addItemListener(this);
		menuItem_tag.setMnemonic(KeyEvent.VK_L);
		menuItem_tag.setSelected(false);
		
		menu_vehicle.add(menuItem_tag);
		
		menu_car_material = new JMenu("Material");
		menu_car_material.setMnemonic(KeyEvent.VK_M); 
		
		menu_vehicle.add(menu_car_material);
		
		menuItem_car_color = new JMenuItem("Body Color", KeyEvent.VK_B);
		menuItem_car_color.addActionListener(this);
		menuItem_car_color.setActionCommand("car_color");
		
		menuItem_cubemap = new JCheckBoxMenuItem("Enable Chrome");
		menuItem_cubemap.addItemListener(this);
		menuItem_cubemap.setMnemonic(KeyEvent.VK_C);
		
		menu_car_material.add(menuItem_car_color);
		menu_car_material.add(menuItem_cubemap);
		
		menuBar.add(menu_vehicle);
		/**----------------**/
		
		/** Render Menu **/
		menu_render = new JMenu("Render");
		menu_render.setMnemonic(KeyEvent.VK_R);
		
		menuItem_shaders = new JCheckBoxMenuItem("Enable Shaders");
		menuItem_shaders.addItemListener(this);
		menuItem_shaders.setMnemonic(KeyEvent.VK_S);
		menuItem_shaders.setSelected(true);
		
		menu_render.add(menuItem_shaders);
		
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
		menuItem_motionblur.setMnemonic(KeyEvent.VK_M);
		menuItem_motionblur.setSelected(enableBlur);
		
		menuItem_aberration = new JCheckBoxMenuItem("Chromatic Aberration");
		menuItem_aberration.addItemListener(this);
		menuItem_aberration.setMnemonic(KeyEvent.VK_A);
		
		menuItem_bloom = new JCheckBoxMenuItem("HDR Bloom");
		menuItem_bloom.addItemListener(this);
		menuItem_bloom.setMnemonic(KeyEvent.VK_B);
		menuItem_bloom.setSelected(enableBloom);
		
		menu_effects.add(menuItem_motionblur);
		menu_effects.add(menuItem_aberration);
		menu_effects.add(menuItem_bloom);
		
		menu_render.add(menu_effects);
		
		menu_render.addSeparator();
		
		menu_shadows = new JMenu("Shadows");
		
		menuItem_shadows = new JCheckBoxMenuItem("Enable Shadows");
		menuItem_shadows.addItemListener(this);
		menuItem_shadows.setMnemonic(KeyEvent.VK_S);
		menuItem_shadows.setSelected(enableShadow);
		
		menu_shadows.add(menuItem_shadows);
		menu_shadows.addSeparator();
		
		menu_shadow_quality = new JMenu("Quality");
		menu_shadow_quality.setMnemonic(KeyEvent.VK_Q);
		
		ButtonGroup group_shadow = new ButtonGroup();
		
		menuItem_shadow_poor = new JRadioButtonMenuItem("Low");
		menuItem_shadow_poor.addActionListener(this);
		menuItem_shadow_poor.setSelected(false);
		menuItem_shadow_poor.setMnemonic(KeyEvent.VK_L);
		menuItem_shadow_poor.setActionCommand("shadow_low");

		menuItem_shadow_norm = new JRadioButtonMenuItem("Medium");
		menuItem_shadow_norm.addActionListener(this);
		menuItem_shadow_norm.setSelected(false);
		menuItem_shadow_norm.setMnemonic(KeyEvent.VK_M);
		menuItem_shadow_norm.setActionCommand("shadow_medium");
		
		menuItem_shadow_high = new JRadioButtonMenuItem("High");
		menuItem_shadow_high.addActionListener(this);
		menuItem_shadow_high.setSelected(false);
		menuItem_shadow_high.setMnemonic(KeyEvent.VK_H);
		menuItem_shadow_high.setActionCommand("shadow_high");
		
		menuItem_shadow_best = new JRadioButtonMenuItem("Best");
		menuItem_shadow_best.addActionListener(this);
		menuItem_shadow_best.setSelected(true);
		menuItem_shadow_best.setMnemonic(KeyEvent.VK_B);
		menuItem_shadow_best.setActionCommand("shadow_best");
		
		group_shadow.add(menuItem_shadow_poor);
		group_shadow.add(menuItem_shadow_norm);
		group_shadow.add(menuItem_shadow_high);
		group_shadow.add(menuItem_shadow_best);
		
		menu_shadow_quality.add(menuItem_shadow_poor);
		menu_shadow_quality.add(menuItem_shadow_norm);
		menu_shadow_quality.add(menuItem_shadow_high);
		menu_shadow_quality.add(menuItem_shadow_best);
		
		menu_shadows.add(menu_shadow_quality);
		
		menu_render.add(menu_shadows);
		
		menuBar.add(menu_render);
		/**-------------------**/
		
		/** Environment Menu **/
		menu_environment = new JMenu("Environment");
		menu_environment.setMnemonic(KeyEvent.VK_E);
		
		menuItem_background = new JMenuItem("Background Color", KeyEvent.VK_B);
		menuItem_background.addActionListener(this);
		menuItem_background.setActionCommand("bg_color");
		
		menuItem_fog = new JCheckBoxMenuItem("Fog");
		menuItem_fog.addItemListener(this);
		menuItem_fog.setMnemonic(KeyEvent.VK_F);
		menuItem_fog.setSelected(enableFog);
		
		menuItem_fogcolor = new JMenuItem("Fog Color", KeyEvent.VK_C);
		menuItem_fogcolor.addActionListener(this);
		menuItem_fogcolor.setActionCommand("fog_color");
		
		menuItem_skycolor = new JMenuItem("Sky Color", KeyEvent.VK_S);
		menuItem_skycolor.addActionListener(this);
		menuItem_skycolor.setActionCommand("sky_color");
		
		menuItem_horizon = new JMenuItem("Horizon Color", KeyEvent.VK_H);
		menuItem_horizon.addActionListener(this);
		menuItem_horizon.setActionCommand("horizon");
		
		menu_weather = new JMenu("Weather");
		menu_weather.setMnemonic(KeyEvent.VK_W);
		
		ButtonGroup group_weather = new ButtonGroup();
		
		menuItem_none = new JRadioButtonMenuItem("None");
		menuItem_none.addActionListener(this);
		menuItem_none.setSelected(true);
		menuItem_none.setMnemonic(KeyEvent.VK_N);
		menuItem_none.setActionCommand("no_weather");

		menuItem_snow = new JRadioButtonMenuItem("Snow");
		menuItem_snow.addActionListener(this);
		menuItem_snow.setSelected(false);
		menuItem_snow.setMnemonic(KeyEvent.VK_S);
		menuItem_snow.setActionCommand("snow");
		
		menuItem_rain = new JRadioButtonMenuItem("Rain");
		menuItem_rain.addActionListener(this);
		menuItem_rain.setSelected(false);
		menuItem_rain.setMnemonic(KeyEvent.VK_R);
		menuItem_rain.setActionCommand("rain");
		
		group_weather.add(menuItem_none);
		group_weather.add(menuItem_snow);
		group_weather.add(menuItem_rain);
		
		menuItem_settle = new JCheckBoxMenuItem("Snow Settles");
		menuItem_settle.addItemListener(this);
		
		menuItem_splash = new JCheckBoxMenuItem("Rain Splashes");
		menuItem_splash.addItemListener(this);
		
		menu_weather.add(menuItem_none);
		menu_weather.add(menuItem_snow);
		menu_weather.add(menuItem_rain);
		menu_weather.addSeparator();
		menu_weather.add(menuItem_settle);
		menu_weather.add(menuItem_splash);
		
		menu_environment.add(menuItem_background);
		menu_environment.addSeparator();
		menu_environment.add(menuItem_fog);
		menu_environment.add(menuItem_fogcolor);
		menu_environment.add(menuItem_skycolor);
		menu_environment.add(menuItem_horizon);
		menu_environment.addSeparator();
		menu_environment.add(menu_weather);
		
		menuBar.add(menu_environment);
		/**-------------------**/
		
		/** Light Menu **/
		menu_light = new JMenu("Light");
		menu_light.setMnemonic(KeyEvent.VK_L);
		
		menuItem_synch_light = new JCheckBoxMenuItem("Edit Lights Together");
		menuItem_synch_light.addItemListener(this);
		menuItem_synch_light.setMnemonic(KeyEvent.VK_T);
		menuItem_synch_light.setSelected(synchLights);
		
		menuItem_ambience = new JMenuItem("Set Ambience", KeyEvent.VK_A);
		menuItem_ambience.addActionListener(this);
		menuItem_ambience.setActionCommand("set_ambience");
		
		menuItem_emission = new JMenuItem("Set Emission", KeyEvent.VK_E);
		menuItem_emission.addActionListener(this);
		menuItem_emission.setActionCommand("set_emission");
		
		menuItem_specular = new JMenuItem("Set Specular", KeyEvent.VK_C);
		menuItem_specular.addActionListener(this);
		menuItem_specular.setActionCommand("set_specular");
		
		menuItem_diffuse = new JMenuItem("Set Diffuse", KeyEvent.VK_D);
		menuItem_diffuse.addActionListener(this);
		menuItem_diffuse.setActionCommand("set_diffuse");
		
		menuItem_normalize = new JCheckBoxMenuItem("Normalize");
		menuItem_normalize.addItemListener(this);
		menuItem_normalize.setMnemonic(KeyEvent.VK_N);
		menuItem_normalize.setSelected(normalize);
		
		menuItem_smooth = new JCheckBoxMenuItem("Smooth");
		menuItem_smooth.addItemListener(this);
		menuItem_smooth.setMnemonic(KeyEvent.VK_S);
		
		menuItem_attenuate = new JCheckBoxMenuItem("Enable Attenuation");
		menuItem_attenuate.addItemListener(this);
		menuItem_attenuate.setMnemonic(KeyEvent.VK_A);
		
		menuItem_local = new JCheckBoxMenuItem("Local Specularity");
		menuItem_local.addItemListener(this);
		menuItem_local.setMnemonic(KeyEvent.VK_L);
		
		menuItem_secondary = new JCheckBoxMenuItem("Specular Texture");
		menuItem_secondary.addItemListener(this);
		
		menu_light.add(menuItem_synch_light);
		menu_light.addSeparator();
		menu_light.add(menuItem_ambience);
		menu_light.add(menuItem_emission);
		menu_light.add(menuItem_specular);
		menu_light.add(menuItem_diffuse);
		menu_light.addSeparator();
		menu_light.add(menuItem_normalize);
		menu_light.add(menuItem_smooth);
		menu_light.add(menuItem_attenuate);
		menu_light.add(menuItem_local);
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
		
		menu_geometry = new JMenu("Geometry");
		menu_geometry.setMnemonic(KeyEvent.VK_G);	
		
		menuItem_solid = new JCheckBoxMenuItem("Show Geometry");
		menuItem_solid.addItemListener(this);
		menuItem_solid.setMnemonic(KeyEvent.VK_G);
		
		menuItem_frame = new JCheckBoxMenuItem("Show Wireframe");
		menuItem_frame.addItemListener(this);
		menuItem_frame.setMnemonic(KeyEvent.VK_W);
		
		menu_geometry.add(menuItem_solid);
		menu_geometry.add(menuItem_frame);
		
		menu_material = new JMenu("Material");
		menu_material.setMnemonic(KeyEvent.VK_M);
		
		menuItem_texturing = new JCheckBoxMenuItem("Enable Texture");
		menuItem_texturing.addItemListener(this);
		menuItem_texturing.setMnemonic(KeyEvent.VK_T);
		
		menuItem_bumpmaps = new JCheckBoxMenuItem("Bump Mapping");
		menuItem_bumpmaps.addItemListener(this);
		menuItem_bumpmaps.setMnemonic(KeyEvent.VK_B);
		
		menuItem_caustics = new JCheckBoxMenuItem("Water Caustics");
		menuItem_caustics.addItemListener(this);
		menuItem_caustics.setMnemonic(KeyEvent.VK_C);
		
		menuItem_malleable = new JCheckBoxMenuItem("Malleable");
		menuItem_malleable.addItemListener(this);
		menuItem_malleable.setMnemonic(KeyEvent.VK_M);
		
		menuItem_shininess = new JMenuItem("Set Specular", KeyEvent.VK_S);
		menuItem_shininess.addActionListener(this);
		menuItem_shininess.setActionCommand("material_specular");
		
		menu_material.add(menuItem_texturing);
		menu_material.add(menuItem_bumpmaps );
		menu_material.add(menuItem_caustics );
		menu_material.add(menuItem_malleable);
		menu_material.add(menuItem_shininess);
		
		menu_normals = new JMenu("Normals");
		menu_normals.setMnemonic(KeyEvent.VK_N);
		
		menuItem_shading = new JCheckBoxMenuItem("Enable Shading");
		menuItem_shading.addItemListener(this);
		menuItem_shading.setMnemonic(KeyEvent.VK_S);
		
		menuItem_vnormals = new JCheckBoxMenuItem("Show Vertex Normals");
		menuItem_vnormals.addItemListener(this);
		menuItem_vnormals.setMnemonic(KeyEvent.VK_V);
		
		menuItem_vtangents = new JCheckBoxMenuItem("Show Vertex Tangents");
		menuItem_vtangents.addItemListener(this);
		
		menuItem_recalculate = new JMenuItem("Recalculate Normals", KeyEvent.VK_R);
		menuItem_recalculate.addActionListener(this);
		menuItem_recalculate.setActionCommand("recalc_norms");
		
		menuItem_tangent = new JMenuItem("Recalculate Tangents", KeyEvent.VK_T);
		menuItem_tangent.addActionListener(this);
		menuItem_tangent.setActionCommand("recalc_tangs");
		
		menu_normals.add(menuItem_vnormals );
		menu_normals.add(menuItem_vtangents);
		menu_normals.addSeparator();
		menu_normals.add(menuItem_shading);
		menu_normals.addSeparator();
		menu_normals.add(menuItem_recalculate);
		menu_normals.add(menuItem_tangent);
		
		menu_coloring = new JMenu("Coloring");
		menu_coloring.setMnemonic(KeyEvent.VK_C);
		
		menuItem_vcoloring = new JCheckBoxMenuItem("Color Vertices");
		menuItem_vcoloring.addItemListener(this);
		menuItem_vcoloring.setMnemonic(KeyEvent.VK_C);
		
		menu_coloring.add(menuItem_vcoloring);
		
		menu_heights = new JMenu("Heights");
		menu_heights.setMnemonic(KeyEvent.VK_H);
		
		menuItem_elevation = new JCheckBoxMenuItem("Show Elevation");
		menuItem_elevation.addItemListener(this);
		menuItem_elevation.setMnemonic(KeyEvent.VK_E);
		
		menuItem_height = new JMenuItem("Save Heights", KeyEvent.VK_H);
		menuItem_height.addActionListener(this);
		menuItem_height.setActionCommand("save_heights");
		
		menuItem_reset = new JMenuItem("Reset Heights", KeyEvent.VK_R);
		menuItem_reset.addActionListener(this);
		menuItem_reset.setActionCommand("reset_heights");
		
		menu_heights.add(menuItem_elevation);
		menu_heights.addSeparator();
		menu_heights.add(menuItem_height);
		menu_heights.add(menuItem_reset);
		
		menu_quadtree.add(menu_geometry);
		menu_quadtree.add(menu_material);
		menu_quadtree.add(menu_normals);
		menu_quadtree.add(menu_coloring);
		menu_quadtree.add(menu_heights);
		
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
	    
	    int[] textureUnits = new int[3];
	    gl.glGetIntegerv(GL2.GL_MAX_TEXTURE_UNITS, textureUnits, 0);
	    gl.glGetIntegerv(GL2.GL_MAX_TEXTURE_IMAGE_UNITS, textureUnits, 1);
	    gl.glGetIntegerv(GL2.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS, textureUnits, 2);
	    System.out.println("Number of Texture Units: " + textureUnits[0]);
	    System.out.println("Number of Texture Image Units: " + textureUnits[1]);
	    System.out.println("Number of Vertex Texture Image Units: " + textureUnits[2] + "\n");
	    
	    boolean anisotropic = gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic");
	    System.out.println("Anisotropic Filtering Support: " + anisotropic);
	    
	    if(anisotropic)
	    {
		    float[] anisotropy = new float[1];
		    gl.glGetFloatv(GL2.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, anisotropy, 0);
		    System.out.println("Maximum Anisotropy: " + anisotropy[0] + "\n");
	    } 
	    
	    float[] data = new float[3];
	    
	    gl.glGetFloatv(GL2.GL_POINT_SIZE_RANGE, data, 0);
	    gl.glGetFloatv(GL2.GL_POINT_SIZE_GRANULARITY, data, 2);
	    
	    System.out.println("Point Size Range: " + data[0] + " -> " + data[1]);
	    System.out.println("Point Size Granularity: " + data[2] + "\n");
	    
	    gl.glGetFloatv(GL2.GL_ALIASED_POINT_SIZE_RANGE, data, 0);
	    System.out.println("Aliased Point Size Range: " + data[0] + " -> " + data[1] + "\n");
	    
	    gl.glGetFloatv(GL2.GL_LINE_WIDTH_RANGE, data, 0);
	    gl.glGetFloatv(GL2.GL_LINE_WIDTH_GRANULARITY, data, 2);
	    
	    System.out.println("Line Width Range: " + data[0] + " -> " + data[1]);
	    System.out.println("Line Width Granularity: " + data[2] + "\n");
	    
	    gl.glGetFloatv(GL2.GL_MAX_EVAL_ORDER, data, 0);
	    System.out.println("Number of Control Points: " + data[0] + "\n");
	    
	    gl.glGetFloatv(GL2.GL_MAX_VERTEX_UNIFORM_COMPONENTS, data, 0);
	    System.out.println("Vertex Uniform Components: " + data[0] + "\n");
	}

	public void printErrors(GL2 gl)
	{
		int error = gl.glGetError();
		
		while(error != GL2.GL_NO_ERROR)
		{
			System.out.println("OpenGL Error: " + glu.gluErrorString(error));
			error = gl.glGetError();
		}
	}
	
	public SceneNode cubeNode;
	public SceneNode testNode;
	public SceneNode blueNode;
	public SceneNode wedgeNode;
	public Reflector cubeReflector;
	
	public QuestionBlock questionBlock;
	
	public Mirror mirror;

	public void init(GLAutoDrawable drawable)
	{
		Scene.singleton = this;
		
		execution = Calendar.getInstance();
		
		GL2 gl = drawable.getGL().getGL2();
		
		glu = new GLUgl2();
		glut = new GLUT();
		
		loadTextures(gl);
		Shader.loadShaders(gl);
		
		setupShaderTree();
		
		// may provide extra speed depending on machine
//		gl.setSwapInterval(0);
		
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
	    createLights(gl);
	    
	    caster = new ShadowCaster(this, light);
	    
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		
		long setupStart = System.currentTimeMillis();
		
		/** Model Setup **/
		skybox = new SkyBox(gl);
		shine = new ShineSprite(new Vec3(10, 40, 0));
		star = new PowerStar(new Vec3(-10, 40, 0));
		mushroom = new Mushroom(gl, new Vec3(40, 1.5, 0));
		energyField = new EnergyField(new Vec3(), FieldType.LIGHT);
		pipe = new WarpPipe(new Vec3(0, 2, 20));
		
		setupCoinStacks();
	
		setupBrickBlocks();
		brickWall = new BrickWall(gl, brickBlocks, brickScale);
		
		questionBlock = new QuestionBlock(new Vec3(0, 40, 0), 2);
	    
	    if(enableItems) loadItems(gl);
	    loadParticles();
	    
	    setupGenerators();
	    
	    loadPlayers(gl);
		    
	    itemBoxes.addAll(ItemBox.generateDiamond( 56.25f, 30f, particles));
	    itemBoxes.addAll(ItemBox.generateSquare (101.25f, 60f, particles));
	    itemBoxes.addAll(ItemBox.generateSquare (123.75f, 30f, particles));
	    itemBoxes.addAll(ItemBox.generateDiamond(   180f,  0f, particles));
	    
	    fort = new BlockFort(gl);
	    
	    if(enableTerrain)
	    {
	    	generateTerrain(gl, DEFAULT_TERRAIN);
	    	setCheckBoxes();
	    	
	    	Set<String> trees = terrain.trees.keySet();
		    
		    for(String tree : trees)
				listModel.addElement(tree);
		    
		    generateFoliage(gl, 20);
	    }
	    
	    if(enableShadow) caster.setup(gl);
	    
	    floorBound = new OBB(0, -15, 0, 0, 0, 0, 240, 15, 240);
	    wallBounds = BoundParser.parseOBBs("bound/environment.bound");
	    
//	    volume = new Volume(gl, 512, 512, 512);
	    
	    selecter = new ModelSelecter(this, glu); 
	    
	    bloom = new BloomStrobe(gl, this);
	    
	    focalBlur = new FocalBlur(this);
	    
	    water = new Water(this);
	    water.createTextures(gl);
	    
	    cubeReflector = new Reflector(1.0f);
	        
	    bolt = (new LightningStrike(new Vec3(0, 34, 0), new Vec3(0, 2, 0), 2, true, true, RenderStyle.CONTINUOUS));
	    if(bolt.getChildren().isEmpty()) bolt.addChild(bolt.generateBolt(RenderStyle.CONTINUOUS, BoltType.SELF_ARCH, 1.5f, new Vec3(), 5, 32));
	    
	    frameTimes  = new long[240];
	    renderTimes = new long[240][RENDER_HEADERS.length];
	    updateTimes = new long[240][UPDATE_HEADERS.length];
	    
	    focalBlur.setup(gl);
	    
	    mirror = new Mirror(new Vec3(0, 100, 0), new Vec3(0, -1, 0));
	    mirror.setup(gl);
	    
	    rainScreen = new RainScreen();
	    
	    if(printVersion) printVersion(gl);
	    
	    console.parseCommand("profile game");
	    
	    long setupEnd = System.currentTimeMillis();
	    System.out.println("\nSetup Time: " + (setupEnd - setupStart) + " ms" + "\n");
	    
	    startTime = System.currentTimeMillis();
	    //records the time prior to the rendering of the first frame after initialization
	}
	
	LightningStrike bolt;

	private void setupCoinStacks()
	{
		Random r = new Random();
		
		double theta = 2 * Math.PI / 8;
		
		for(int i = 0; i < 8; i++)
		{
			coins.add(new GoldCoin(new Vec3(Math.sin(theta * i) * 10, 3, Math.cos(theta * i) * 10), CoinType.RED_COIN, true));
		}
		for(int i = 0; i < 30; i++)	coins.add(new GoldCoin(new Vec3(     r.nextFloat() * .1, .2 + .4 * i,     r.nextFloat() * .1), CoinType.GOLD_COIN, false));
		for(int i = 0; i < 40; i++)	coins.add(new GoldCoin(new Vec3(+3 + r.nextFloat() * .1, .2 + .4 * i, 2 + r.nextFloat() * .1), CoinType.GOLD_COIN, false));
		for(int i = 0; i < 20; i++)	coins.add(new GoldCoin(new Vec3(-2 + r.nextFloat() * .1, .2 + .4 * i, 3 + r.nextFloat() * .1), CoinType.GOLD_COIN, false));
	}
	
	private static final float EPSILON = 1E-5f;
	
	private void setupBrickBlocks()
	{	
		Vec3 p = new Vec3(-206.25 + brickScale * 2, brickScale, 206.25);
		
		Random generator = new Random();
		
		for(int i = 0; i < 54; i++)
		{
			for(int j = 0; j < 8; j++)
			{
				if(generator.nextFloat() > -0.15) brickBlocks.add(new BrickBlock(new Vec3(p.x, p.y, -p.z), brickScale));
				if(generator.nextFloat() > -0.15) brickBlocks.add(new BrickBlock(new Vec3(p), brickScale));
				p.y += 2 * brickScale + EPSILON;
			}
			p.y  = brickScale;
			p.x += 2 * brickScale + EPSILON;
		}
		
		p = new Vec3(-206.25, brickScale, 206.25 - brickScale * 2);
		
		for(int i = 0; i < 54; i++)
		{
			for(int j = 0; j < 8; j++)
			{
				if(generator.nextFloat() > -0.15) brickBlocks.add(new BrickBlock(new Vec3(-p.x, p.y, -p.z), brickScale));
				if(generator.nextFloat() > -0.15) brickBlocks.add(new BrickBlock(new Vec3( p), brickScale));
				p.y += 2 * brickScale + EPSILON;
			}
			p.y  = brickScale;
			p.z -= 2 * brickScale + EPSILON;
		}
	}
	
	private void setupShaderTree()
	{
		DefaultMutableTreeNode shaderNode  = null;
		DefaultMutableTreeNode uniformNode = null;
		
		for(Map.Entry<String, Shader> shader : Shader.shaders.entrySet())
		{
			shaderNode = new DefaultMutableTreeNode(shader.getKey());
			
//			for(String uniform : shader.getValue().uniforms)
//			{
//				uniformNode = new DefaultMutableTreeNode(uniform);
//				shaderNode.add(uniformNode);
//			}
//			shaderRoot.add(shaderNode);
		}
	}
	
	public void setCheckBoxes()
	{
		if(terrain == null) return;
		
		menuItem_solid.setSelected(terrain.tree.solid);
		menuItem_frame.setSelected(terrain.tree.frame);
		
		menuItem_elevation.setSelected(terrain.tree.reliefMap);
		menuItem_vnormals .setSelected(terrain.tree.vNormals );
		menuItem_vtangents.setSelected(terrain.tree.vTangents);
		
		menuItem_shading.setSelected(terrain.tree.enableShading);
		
		menuItem_vcoloring.setSelected(terrain.tree.enableColoring);
		menuItem_texturing.setSelected(terrain.tree.enableTexture );
		menuItem_bumpmaps .setSelected(terrain.tree.enableBumpmap );
		menuItem_caustics .setSelected(terrain.tree.enableCaustic );
		menuItem_malleable.setSelected(terrain.tree.malleable     );
		
		menuItem_bloom.setSelected(enableBloom);
		menuItem_focal_blur.setSelected(enableFocalBlur);
		if(focalBlur != null) menuItem_mirage.setSelected(focalBlur.enableMirage);
		
		Vehicle car = cars.get(0);
		
		menuItem_cubemap.setSelected(car.enableChrome);
		menuItem_aberration.setSelected(car.enableAberration);
		
		menuItem_water.setSelected(terrain.enableWater);
		
		menuItem_smooth.setSelected(Light.smoothShading);
		menuItem_attenuate.setSelected(light.enableAttenuation);
	    menuItem_secondary.setSelected(Light.seperateSpecular);
	}

	private void setupGenerators()
	{
		
	}

	private void loadPlayers(GL2 gl)
	{
		cars.add(new Vehicle(gl, new Vec3(0, 1.8f, 78.75f), 0,   0, 0, this));
	    
	    if(GamePad.numberOfGamepads() > 1)
	    	cars.add(new Vehicle(gl, new Vec3( -78.75f, 1.8f, 0), 0, 180, 0, this));
	    
	    if(GamePad.numberOfGamepads() > 2)
	    	cars.add(new Vehicle(gl, new Vec3( 0, 1.8f,  78.75f), 0, 270, 0, this));
	    	
	    if(GamePad.numberOfGamepads() > 3)
		    cars.add(new Vehicle(gl, new Vec3( 0, 1.8f, -78.75f), 0,  90, 0, this));
	}

	private void loadTextures(GL2 gl)
	{
		try
		{			
			brick_front = TextureLoader.load(gl, "tex/brick_front.png");
			brick_side  = TextureLoader.load(gl, "tex/brick_side.png");
			
			brick_front_normal = TextureLoader.load(gl, "tex/brick_front_normal.png");
			brick_side_normal  = TextureLoader.load(gl, "tex/brick_side_normal.png");
			
			brick_front_height = TextureLoader.load(gl, "tex/brick_front_height.png"); 
			brick_side_height  = TextureLoader.load(gl, "tex/brick_side_height.png");
			
			floorBump = TextureLoader.load(gl, "tex/bump_maps/brick_parallax.png");
			floorTex  = TextureLoader.load(gl, "tex/brick_color.jpg");
			
//			rain_normal = TextureLoader.load(gl, "tex/bump_maps/large_stone.jpg");
			rain_normal  = TextureLoader.load(gl, "tex/bump_maps/noise.jpg");
			pattern_mask = TextureLoader.load(gl, "tex/slope_mask.jpg");
			
			brickColour = TextureLoader.load(gl, "tex/brick_colour.png");
			brickNormal = TextureLoader.load(gl, "tex/brick_normal.png");
			brickHeight = TextureLoader.load(gl, "tex/brick_height.png");
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	private void createLights(GL2 gl)
	{
		lights[0] = new Light(gl);
//		lights[1] = new Light(gl, new Vec3(-60, 20, -60), new float[] {0.25f, 0.10f, 0.10f}, new float[] {0.75f, 0.50f, 0.50f}, new float[] {1.00f, 0.75f, 0.75f});
//		lights[2] = new Light(gl, new Vec3(-60, 20, +60), new float[] {0.10f, 0.25f, 0.10f}, new float[] {0.50f, 0.75f, 0.50f}, new float[] {0.75f, 1.00f, 0.75f});
//		lights[3] = new Light(gl, new Vec3(+60, 20, +60), new float[] {0.10f, 0.10f, 0.25f}, new float[] {0.50f, 0.50f, 0.75f}, new float[] {0.75f, 0.75f, 1.00f});
//		lights[4] = new Light(gl, new Vec3(+60, 20, -60), new float[] {0.25f, 0.25f, 0.10f}, new float[] {0.75f, 0.75f, 0.50f}, new float[] {1.00f, 1.00f, 0.75f});
		
		light = lights[0];
	}

	private void loadParticles()
	{
		new BoostParticle(new Vec3(), new Vec3(), 0, 0, 0, false, false);
	}

	private void loadItems(GL2 gl)
	{
		new GreenShell (gl, this, null, 0, false);
	    new RedShell   (gl, this, null, 0, false); 
	    new BlueShell  (gl, this, null, 0);
	    new FakeItemBox(gl, this, null);
	    new Banana     (gl, this, null, 0);
	    new BobOmb     (null, null, false);
	}

	public void display(GLAutoDrawable drawable)
	{
		if(Scene.sceneTimer % Scene.log_timer == 0)
		{
			for(int i = 0; i < 4; i++) System.out.println();
			System.out.println("|| --------------- ||");
			System.out.println("||    NEW FRAME    ||");
			System.out.println("|| --------------- ||");
		}
		
		GL2 gl = drawable.getGL().getGL2();
		
		gl.glClearDepth(1.0f);
		gl.glClearColor(background[0], background[1], background[2], 1.0f);
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL2.GL_STENCIL_BUFFER_BIT);
		
		resetView(gl);
		
		if(multisample) gl.glEnable(GL2.GL_MULTISAMPLE);
		else gl.glDisable(GL2.GL_MULTISAMPLE);
		
		if(normalize) gl.glEnable(GL2.GL_NORMALIZE);
		else gl.glDisable(GL2.GL_NORMALIZE);
		
		gl.glPointParameterfv(GL2.GL_POINT_DISTANCE_ATTENUATION, quadratic, 0);
		
		setupFog(gl);
		
		registerItems(gl);
		
		if(enableAnimation) update(gl);
		else cars.get(0).updateController();
		
		if(enableTerrain && !terrainCommand.equals(""))
		{	
			generateTerrain(gl, terrainCommand);	
			terrainCommand = "";
		}
		
		renderTime = System.currentTimeMillis();
		
//		renderQuery.getResult(gl);
//		renderQuery.begin(gl);
		
		Vehicle car = cars.get(0);
		updateReflectors(gl, car);
		
//		rainScreen.render(gl);
//		if(testMode) displayMap(gl, caster.getTexture(), 0, 0, canvasWidth, canvasHeight);
//		else render(gl);
		
		render(gl);
		
//		renderQuery.end(gl);

		gl.glFlush();
		
		if(printErrors) printErrors(gl);
		
		shadowQuery.getResult(gl);
		shadowQuery.begin(gl);

		if(enableShadow) caster.update(gl);
		
		shadowQuery.end(gl);
		
//		if(testMode) displayMap(gl, caster.getTexture(), 0, 0, 1, 1);
		if(displayDepth) displayMap(gl, focalBlur.getDepthTexture(), 0, 0, 1, 1);
//		if(testMode) displayMap(gl, bloom.getTexture(7), -0, -0, 1, 1);
		
		calculateFPS();
	}
	
	public static TimeQuery renderQuery = new TimeQuery(64);
	public static TimeQuery shadowQuery = new TimeQuery(65);
	
	public RainScreen rainScreen;

	private void updateReflectors(GL2 gl, Vehicle car)
	{
		beginRenderLog("REFLECT MODE");
		
		if(car.enableChrome || car.isInvisible() || car.hasStarPower()) car.reflector.update(gl, cars.get(0).getPosition().add(new Vec3(0, 2, 0)));
		if(shine != null) shine.reflector.update(gl, shine.getPosition());
		if(star  != null)  star.reflector.update(gl,  star.getPosition());
		if(pipe  != null)  pipe.reflector.update(gl,  pipe.getPosition());
		
		for(Balloon balloon : car.balloons) balloon.reflector.update(gl, cars.get(0).getPosition().add(new Vec3(0, 4, 0)));
		
		questionBlock.updateReflection(gl);
		
		cubeReflector.update(gl, questionBlock.getPosition());
		
		for(ItemBox box : itemBoxes) box.reflector.update(gl, box.getPosition());
		
		for(GoldCoin coin : coins) coin.reflector.update(gl, coin.getPosition());
		
		for(Item item : itemList)
		{
			if(item instanceof BobOmb)
			{
				BobOmb bomb = (BobOmb) item;
				bomb.reflector.update(gl, bomb.getPosition());
			}
			else if(item instanceof FakeItemBox)
			{
				FakeItemBox box = (FakeItemBox) item;
				box.reflector.update(gl, box.getPosition());
			}
		}
		
		endRenderLog();
	}
	
	public void displayMap(GL2 gl, int texture, float x, float y, float w, float h)
    {
        gl.glMatrixMode(GL_PROJECTION); gl.glLoadIdentity();
        gl.glMatrixMode(GL_MODELVIEW ); gl.glLoadIdentity();
        
//        gl.glEnable(GL2.GL_BLEND);
        gl.glDisable(GL2.GL_DEPTH_TEST);
        
        gl.glMatrixMode(GL2.GL_TEXTURE); gl.glLoadIdentity();
        gl.glMatrixMode(GL_MODELVIEW );
        gl.glPushMatrix();
        {
	        gl.glLoadIdentity();
	           
	        gl.glEnable(GL_TEXTURE_2D);
	        gl.glBindTexture(GL_TEXTURE_2D, texture);
	        gl.glDisable(GL2.GL_LIGHTING);
	        
	        gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
	        gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_COMPARE_MODE, GL2.GL_NONE);
	        
	        // Show the shadow map at its actual size relative to window
	        gl.glBegin(GL2.GL_QUADS);
	        {
	            gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex2f(x + 0, y + 0);
	            gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex2f(x + w, y + 0);
	            gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex2f(x + w, y + h);
	            gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex2f(x + 0, y + h);
	        }
	        gl.glEnd();
	        
	        gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
	        gl.glEnable(GL2.GL_LIGHTING);
        }
        gl.glPopMatrix();
        
        gl.glDisable(GL2.GL_BLEND);
        gl.glEnable(GL2.GL_DEPTH_TEST);
		
        resetView(gl);
    }
	
	public static boolean enableOcclusion = true;
	
	public static int tabs_in_log = 0;
	public static int log_timer = 300;
	
	public static void beginRenderLog(String log_header)
	{
		if(Scene.sceneTimer % Scene.log_timer == 0)
		{
			String indent = "";
			for(int i = 0; i < Scene.tabs_in_log; i++) indent += "\t";
			System.out.println(indent + log_header + "\n" + indent + "{");
			Scene.tabs_in_log++;
		}
	}
	
	public static void printToRenderLog(String log)
	{
		if(Scene.sceneTimer % Scene.log_timer == 0)
		{
			String indent = "";
			for(int i = 0; i < Scene.tabs_in_log; i++) indent += "\t";
			System.out.println(indent + log);
		}
	}
	
	public static void endRenderLog()
	{
		if(sceneTimer % log_timer == 0)
		{
	    	tabs_in_log--;
			String indent = "";
			for(int i = 0; i < tabs_in_log; i++) indent += "\t";
			System.out.println(indent + "}");
		}
	}

	private void render(GL2 gl)
	{		
		TimeQuery.resetCache();
		
		if(mousePressed) selecter.selectModel(gl);

		for(int index = 0; index < cars.size(); index++)
		{
			Vehicle car = cars.get(index);
			
			setupViewport(gl, index);
			car.setupCamera(gl);
			
			setupLights(gl, car);
			
			mirror.update(gl);
			
			setupViewport(gl, index);
			car.setupCamera(gl);
			
			setupLights(gl, car);
			
			if(enableShadow)
			{
				caster.displayShadow(gl);
				
				if(resetShadow) // required if shadow quality is altered
				{
					caster.setup(gl);
					focalBlur.setup(gl);
					resetShadow = false;
				}
			}
			
			if(enableFocalBlur) focalBlur.update(gl);
			
			bananasRendered = 0;
			
			if(enableBloom)
			{
				BloomStrobe.begin(gl);
				 
				bloom.render(gl);
			}
			else
			{
				BloomStrobe.end(gl);
				
				if(enableReflection) displayReflection(gl, car);
				
				beginRenderLog("NORMAL MODE");

				if(terrain != null && terrain.enableWater) renderWater(gl, car);

				renderWorld(gl);
				render3DModels(gl, car);

//				if(enableTerrain) renderTimes[frameIndex][1] = renderFoliage(gl, car);
//				else renderTimes[frameIndex][1] = 0;

				if(terrain != null && terrain.enableWater) 
				{
					water.setRefraction(gl);
					water.render(gl, car.camera.getPosition());
				}
				
			    renderTimes[frameIndex][4] = renderParticles(gl, car);
				Particle.resetTexture();
				
				if(displayLight && !moveLight)
					for(Light l : lights) l.render(gl);
				
				endRenderLog();
			}
			
			renderQuery.getResult(gl);
			renderQuery.begin(gl);
			
			if(enableFocalBlur) focalBlur.guassianPass(gl);
			
			renderQuery.end(gl);
			
			gl.glDisable(GL2.GL_CLIP_PLANE2);
			
			renderTimes[frameIndex][5] = renderBounds(gl);
			renderTimes[frameIndex][6] = car.renderHUD(gl, glu);
		}
		
		if(shadowMap) displayMap(gl, mirror.getTexture(), 0.0f, 0.0f, 1.0f, 1.0f);
	}

	private void setupLights(GL2 gl, Vehicle car)
	{
		for(Light l : lights) l.setup(gl);
		car.starLight.setup(gl);
		for(Light l : car.driftLights) l.setup(gl);
		BlueShell.blastLight.setup(gl);
		
		Light.setepRimLighting(gl);
	}
	
	public static boolean shadowMode = false;
	public static boolean reflectMode = false;
	public static boolean depthMode = false;
	public static boolean environmentMode = false;
	
	/**
	 * This method renders the world as reflected by the surface of the water.
	 * A new clipping plane is defined so that any primitives drawn above water
	 * level are clipped.
	 */
	public void renderWater(GL2 gl, Vehicle car)
	{
		int aboveWater = car.camera.getPosition().y >= 0 ? -1 : 1;  
		
		gl.glEnable(GL2.GL_CLIP_PLANE1);
		double equation[] = {0, aboveWater, 0, 0}; // primitives above water are clipped
		gl.glClipPlane(GL2.GL_CLIP_PLANE1, equation, 0);
		
		reflectMode = true;
		
		beginRenderLog("WATER MODE");

		gl.glPushMatrix();
		{
			gl.glScalef(1.0f, -1.0f, 1.0f); // render environment upside down

			renderWorld(gl);
			render3DModels(gl, car);
		}
		gl.glPopMatrix();
		
		endRenderLog();
		
		reflectMode = false;

		gl.glDisable(GL2.GL_CLIP_PLANE1);
		water.setReflection(gl);
		
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		// removed reflected geometry from final render
	}
	
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		GL2 gl = drawable.getGL().getGL2();
	
		if (height <= 0) height = 1;
		
		canvasHeight = height;
		canvasWidth  = width;
		
		cars.get(0).camera.setDimensions(width, height);
		bloom.changeSize(gl);
		rainScreen.changeSize();
		resetShadow = true;
		
		final float ratio = (float) width / (float) height;
		
		gl.glViewport(0, 0, width, height);
		
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(fov, ratio, 1.0, far);
		
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
		for(Vehicle car : cars)
		{
			while(!car.getItemCommands().isEmpty())
			{
				int itemID = car.getItemCommands().poll();
				
				if(itemID == 10) cloudDensity = MIN_DENSITY;
				else car.registerItem(gl, itemID);
			}
		}
	}

	private long update(GL2 gl)
	{	
		long start = System.currentTimeMillis();
		
		focalBlur.enableRadial = false;
		
		if(mousePressed) modifyTerrain(gl);
		
//		while(!uniformCommands.isEmpty())
//		{
//			UniformCommand command = uniformCommands.poll();
//			
//			Shader shader = Shader.get(command.shader);
//			shader.enable(gl);
//			shader.setUniform(gl, command.uniform, command.value);
//			Shader.disable(gl);
//		}
		
		removeItems();
		
		Particle.removeParticles(particles);
		
		if(enableItemBoxes)
			{ for(ItemBox box : itemBoxes) box.update(cars); } 
		
		updateItems();
		
		    ItemBox.increaseRotation();
		FakeItemBox.increaseRotation();
		
		for(ParticleGenerator generator : generators)
			if(generator.update()) particles.addAll(generator.generate());
		
		for(Particle p : particles) p.update();
		
		updateTimes[frameIndex][1] = (enableBlizzard) ? blizzard.update() : 0;
			
		vehicleCollisions();
		for(Vehicle car : cars)
		{
			car.update();
			if(outOfBounds(car.getPosition())) car.reset();
		}
		
		if(enableTerrain)
		{			
			long _start = System.nanoTime();
			
//			List<BillBoard> toRemove = new ArrayList<BillBoard>();
//			
//			for(BillBoard b : foliage)
//			{
//				if(b.sphere.testOBB(cars.get(0).bound)) toRemove.add(b);
//			}
//			
//			foliage.removeAll(toRemove);
			
			updateTimes[frameIndex][0] = System.nanoTime() - _start;
				
			for(Vehicle car : cars) terrainCollisions(car);
		}
		
		return System.currentTimeMillis() - start;
	}

	private void modifyTerrain(GL2 gl)
	{
		Camera camera = cars.get(0).camera;
		
		if(camera.isAerial())	
		{
			Point point = canvas.getMousePosition();
			if(point == null) return;
		
			int x = (int) point.getX();
			int y = (int) point.getY();
			
			float[] p = camera.to3DPoint(x, y, canvasWidth, canvasHeight);
			float   r = camera.getRadius(retical, canvasHeight);
			float   h = (rightClick ? -0.5f : 0.5f);
			
			terrain.tree.deform(p, r, h);
			for(GrassPatch patch : grassPatches) patch.updateHeights(gl);
		}
	}

	public void removeItems()
	{	
		Item.removeItems(itemList);
		
		for(Vehicle car : cars) car.removeItems();
	}

	private void updateItems()
	{		
		for(Item item : itemList)
		{
			item.update();
			item.update(cars);
		}
		
		for(Vehicle car : cars)
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
		
		for(Vehicle car : cars)
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
				Vehicle a = cars.get(i);
				Vehicle b = cars.get(j);
				
				if(a.getBound().testBound(b.getBound())) a.collide(b);
			}
		}
	}

	private void terrainCollisions(Vehicle car)
	{
		Vec3[] vertices = car.bound.getVertices();
		float[] frictions = {1, 1, 1, 1};
		
		cars.get(0).patch = null;
			
		for(TerrainPatch patch : terrainPatches)
		{
			patch.colliding = false;
			
			for(int v = 0; v < 4; v++)
			{
				if(patch.isColliding(vertices[v].toArray()))
				{
					patch.colliding = true;
					if(frictions[v] > patch.friction) frictions[v] = patch.friction;
					car.patch = patch;
				}
			}
		}
			
		float friction = (frictions[0] + frictions[1] + frictions[2] + frictions[3]) / 4;
		car.friction = friction;
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

	private void displayReflection(GL2 gl, Vehicle car)
	{
		// TODO Weather effects may not be working correctly with reflection
		
		Vec3 p = light.getPosition(); 
		light.setPosition(new Vec3(p.x, -p.y, p.z));
		
		if(enableTerrain)
		{
			gl.glEnable(GL2.GL_CLIP_PLANE1);
			double equation[] = {0, -1, 0, 10}; // primitives above water are clipped
			gl.glClipPlane(GL2.GL_CLIP_PLANE1 , equation, 0);
		}
		
		gl.glPushMatrix();
		{
			if(enableTerrain) gl.glTranslatef(0.0f, 20, 0.0f);
			gl.glScalef(1.0f, -1.0f, 1.0f); // render environment upside down
			
			renderWorld(gl);
			render3DModels(gl, car);
			
			renderParticles(gl, car);
			Particle.resetTexture();
		}
		gl.glPopMatrix();
		
		gl.glDisable(GL2.GL_CLIP_PLANE1);
		
		if(!enableTerrain) renderFloor(gl);
		
		light.setPosition(p);
		
		gl.glColor3f(1, 1, 1);
		
		if(enableTerrain)
		{
			/*
			 * For the plane equation, the first three components represent a vector/axis
			 * and the fourth component is a translation along this vector.
			 */
			gl.glEnable(GL2.GL_CLIP_PLANE2);
			double[] equation = {0, 1, 0, -10};
			gl.glClipPlane(GL2.GL_CLIP_PLANE2 , equation, 0);
		}
	}
	
	public boolean enableBump = true;

	public void renderFloor(GL2 gl)
	{	
		gl.glPushMatrix();
		{
			boolean bloom = BloomStrobe.begin(gl);
			
			Shader shader = enableBump ? (singleLight ? Shader.get("bump") : Shader.get("bump_lights")) :
				Shader.getLightModel("shadow");

			shader.enable(gl);
			shader.setSampler(gl, "texture", 0);
			shader.setSampler(gl, "bumpmap", 1);
//		    shader.setSampler(gl, "reflectSampler", 2);

			shader.setUniform(gl, "enableParallax", Scene.enableParallax);

			if(enableShadow && shader != null)
			{
				float[] model = Arrays.copyOf(Matrix.IDENTITY_MATRIX_16, 16);

				shader.loadModelMatrix(gl, model);

				shader.setSampler(gl, "shadowMap", ShadowCaster.SHADOW_MAP_TEXTURE_UNIT);

				shader.setUniform(gl, "enableShadow", 1);
				shader.setUniform(gl, "sampleMode", ShadowCaster.sampleMode.ordinal());
				shader.setUniform(gl, "texScale", new float[] {1.0f / (Scene.canvasWidth * 12), 1.0f / (Scene.canvasHeight * 12)});
			}

//			gl.glActiveTexture(GL2.GL_TEXTURE3); rockHeight.bind(gl);
//			gl.glActiveTexture(GL2.GL_TEXTURE2); gl.glBindTexture(GL2.GL_TEXTURE_2D, water.reflectTexture);
			gl.glActiveTexture(GL2.GL_TEXTURE1); floorBump.bind(gl);
			gl.glActiveTexture(GL2.GL_TEXTURE0); floorTex.bind(gl);
			
			gl.glColor3f(1, 1, 1);

			gl.glBegin(GL2.GL_QUADS);
			{
				gl.glVertexAttrib3f(1, 1, 0, 0);
				gl.glNormal3f(0, 1, 0);

				float size = 30.0f;

				gl.glTexCoord2f(size,    0); gl.glVertex3f(+250, -0.01f, +250);
				gl.glTexCoord2f(size, size); gl.glVertex3f(+250, -0.01f, -250);
				gl.glTexCoord2f(   0, size); gl.glVertex3f(-250, -0.01f, -250);
				gl.glTexCoord2f(   0,    0); gl.glVertex3f(-250, -0.01f, +250);
			}
			gl.glEnd();
			
			if(!bloom) BloomStrobe.end(gl);
		}	
		gl.glPopMatrix();
	}

	/**
	 * This method renders the world geometry that represents the artificial
	 * boundaries of the virtual environment. This consists of the skybox and
	 * the terrain itself.
	 */
	public long renderWorld(GL2 gl)
	{
		long start = System.nanoTime();
		
		// prevents shadow texture unit from being active
		if(displaySkybox) renderSkybox(gl);
		
		Shader shader = null;
		{
			if(enableShadow)
			{
				shader = Shader.get("phong_shadow");
				
				if(shader != null)
				{
					caster.enable(gl);
					shader.enable(gl);
					shader.setSampler(gl, "texture"  , 0);
					shader.setSampler(gl, "shadowMap", ShadowCaster.SHADOW_MAP_TEXTURE_UNIT);
				}
			}
			else
			{
				shader = Shader.get("phong_texture");
				
				if(shader != null)
				{
					shader.enable(gl);
					shader.setSampler(gl, "texture", 0);
				}
			}
		}
		
		if(enableTerrain && !environmentMode) renderTimes[frameIndex][0] = renderTerrain(gl); 
		else if(!enableReflection)
		{
			renderFloor(gl);
			renderTimes[frameIndex][0] = 0;
		}

		renderTimes[frameIndex][1] = renderObstacles(gl);
		
		Shader.disable(gl);
			
		if(testMode) renderWalls(gl);
		else brickWall.render(gl);
		
//		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
//		planeMesh.render(gl);
//		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
		
		return System.nanoTime() - start;
	}

	public void renderWalls(GL2 gl)
	{
		if(!enableParallax)
		{
			Shader shader = Shader.get("texture_lights");
			if(shader != null) shader.enable(gl);

			Texture[] textures = {brick_side, brick_front, brick_front};

			gl.glPushMatrix();
			{
				displayTexturedCuboid(gl, new Vec3(0, 45,  206.25f), new Vec3(202.5f, 45, 3.75f), 0, textures, 3.75f);
				displayTexturedCuboid(gl, new Vec3(0, 45, -206.25f), new Vec3(202.5f, 45, 3.75f), 0, textures, 3.75f);
				displayTexturedCuboid(gl, new Vec3( 206.25f, 45, 0), new Vec3(3.75f, 45, 202.5f), 0, textures, 3.75f);
				displayTexturedCuboid(gl, new Vec3(-206.25f, 45, 0), new Vec3(3.75f, 45, 202.5f), 0, textures, 3.75f);
			}
			gl.glPopMatrix();
		}
		else
		{
			Shader shader = Shader.get("parallax_lights");
			if(shader != null) shader.enable(gl);
			
			shader.setSampler(gl, "texture", 0);
			shader.setSampler(gl, "bumpmap", 1);
			shader.setSampler(gl, "heightmap", 2);
			
			float[] model = Arrays.copyOf(Matrix.IDENTITY_MATRIX_16, 16);

//			shader.loadModelMatrix(gl, model);

//			shader.setSampler(gl, "shadowMap", ShadowCaster.SHADOW_MAP_TEXTURE_UNIT);

//			shader.setUniform(gl, "enableShadow", 1);
//			shader.setUniform(gl, "sampleMode", ShadowCaster.sampleMode.ordinal());
//			shader.setUniform(gl, "texScale", new float[] {1.0f / (Scene.canvasWidth * 12), 1.0f / (Scene.canvasHeight * 12)});
			
			Texture[] colourMaps = {brick_side, brick_front, brick_front};
			Texture[] normalMaps = {brick_side_normal, brick_front_normal, brick_front_normal};
			Texture[] heightMaps = {brick_side_height, brick_front_height, brick_front_height};

			gl.glPushMatrix();
			{
				Renderer.displayBumpMappedCuboid(gl, new Vec3(0, 45,  206.25f), new Vec3(202.5f, 45, 3.75f), 0, colourMaps, normalMaps, heightMaps, 3.75f);
				Renderer.displayBumpMappedCuboid(gl, new Vec3(0, 45, -206.25f), new Vec3(202.5f, 45, 3.75f), 0, colourMaps, normalMaps, heightMaps, 3.75f);
				Renderer.displayBumpMappedCuboid(gl, new Vec3( 206.25f, 45, 0), new Vec3(3.75f, 45, 202.5f), 0, colourMaps, normalMaps, heightMaps, 3.75f);
				Renderer.displayBumpMappedCuboid(gl, new Vec3(-206.25f, 45, 0), new Vec3(3.75f, 45, 202.5f), 0, colourMaps, normalMaps, heightMaps, 3.75f);
			}
			gl.glPopMatrix();
		}
		
		Shader.disable(gl);
	}

	public void renderSkybox(GL2 gl)
	{
		skybox.render(gl);
	}

	public long renderTerrain(GL2 gl)
	{
		long start = System.nanoTime();

		gl.glPushMatrix();
		{	
			terrain.render(gl, glut);
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
	public long renderObstacles(GL2 gl)
	{	
		long start = System.nanoTime();
		
		Scene.printToRenderLog("Rendering Obstacles");

		fort.render(gl);
		
		gl.glColor3f(1, 1, 1);
		
		Shader.disable(gl);
		
		if(!environmentMode)
		{	
			shine.render(gl); 
			 star.render(gl);
    questionBlock.render(gl);
//    		 pipe.render(gl);
            
//    		for(WoodPlank plank : planks) plank.render(gl);
			 
//			for(GoldCoin coin : coins) coin.render(gl);
		}
		
		mushroom.render(gl);

		return System.nanoTime() - start;
	}
	
	private ShineSprite shine;
	private PowerStar star;
	private Mushroom mushroom;
	private WarpPipe pipe;

	/**
	 * This method renders all of the dynamic 3D models within the world from the
	 * perspective of the car passed as a parameter. The term dynamic refers to
	 * objects that change position, rotation or state such as other vehicles, item
	 * boxes and world items.
	 */
	public void render3DModels(GL2 gl, Vehicle car)
	{	
		renderTimes[frameIndex][2] = renderVehicles(gl, car, false);
	
		if(enableItemBoxes)
	    {
			for(ItemBox box : itemBoxes)
				if(!box.isDead()) box.render(gl, car.trajectory);
	    }
		
		renderTimes[frameIndex][3] = renderItems(gl, car);
	}

	public long renderVehicles(GL2 gl, Vehicle car, boolean shadow)
	{
		long start = System.nanoTime();
		
		if(!shadow || !car.isInvisible())
		{
			boolean tag = car.displayTag;
			if(shadow) car.displayTag = false;
			car.render(gl);
			car.displayTag = tag;
		}
	
		for(Vehicle c : cars)
		{
			if(!c.equals(car))
			{
				boolean visibility = c.displayModel;
				
				c.displayModel = true;
				
				if(!shadow || !car.isInvisible())
				{
					boolean tag = car.displayTag;
					if(shadow || car.isInvisible()) car.displayTag = false;
					car.render(gl);
					car.displayTag = tag;
				}
				c.displayModel = visibility;
			}
		}
		
		return System.nanoTime() - start;
	}
	
	public static int bananasRendered = 0;

	public long renderItems(GL2 gl, Vehicle car)
	{
		long start = System.nanoTime();
		
		gl.glColor3f(1, 1, 1);
		
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

	public long renderParticles(GL2 gl, Vehicle car)
	{
		long start = System.nanoTime();
		
		Scene.printToRenderLog("Rendering Particles");
		
//		renderQuery.getResult(gl);
//		renderQuery.begin(gl);
		
		if(enableBlizzard) blizzard.render(gl);
		
		bolt.render(gl, car.camera.u.zAxis);
		
//		energyField.render(gl);
		
		shine.renderFlare(gl, car.camera.isFree() ? car.camera.ry : car.trajectory);
		 star.renderFlare(gl, car.camera.isFree() ? car.camera.ry : car.trajectory);
		 
		for(Particle particle : particles)
		{
			if(car.isSlipping()) particle.render(gl, car.slipTrajectory);
			else particle.render(gl, car.camera.isFree() ? car.camera.ry : car.trajectory);
		}
		
//		volume.render(gl);
		
//		smokeCloud.render(gl);
		
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		
//		smokeCloud.render(gl);
		
//		renderQuery.end(gl);
		
		return System.nanoTime() - start;
	}
	
	public boolean enableFoliage = true;
	public int foliageMode = 0;

	public long renderFoliage(GL2 gl, Vehicle car)
	{
		if(!enableFoliage) return 0;
		
		long start = System.nanoTime();
		
//		Shader shader = Shader.enabled ? Shader.get("phong_alpha") : null;
//		if(shader != null)
//		{
//			shader.enable(gl);
//			shader.setUniform(gl, "alphaTest", 0.25f);
//		}
//		
//		switch(foliageMode)
//		{
//			case 0: 
//			{
//				gl.glPushMatrix();
//				{	
//					for(BillBoard b : foliage)
//					{
//						if(car.isSlipping()) b.render(gl, car.slipTrajectory);
//						else b.render(gl, car.trajectory);
//					}
//				}	
//				gl.glPopMatrix();
//				
//				break;
//			}
//			case 1: BillBoard.renderPoints(gl, foliage); break;
//			case 2: BillBoard.renderQuads(gl, foliage, car.trajectory); break;
//		}
//		
//		Shader.disable(gl);
		
		for(GrassPatch patch : grassPatches) patch.render(gl);
		
		return System.nanoTime() - start;
	}

	private long renderBounds(GL2 gl)
	{
		long start = System.nanoTime();
		
		gl.glDisable(GL_TEXTURE_2D);
//		test(gl);
		gl.glDisable(GL2.GL_LIGHTING);
		
		List<Bound> bounds = getBounds();
		
		if(enableClosestPoints)
			for(Bound bound : bounds)
				bound.displayClosestPtToPt(gl, glut, cars.get(0).getPosition(), smoothBound);
		
		if(enableOBBVertices)
		{
//			for(Car car : cars)
//			{
//				if(car.colliding)
//					 car.bound.displayVertices(gl, glut, RGB.PURE_RED_3F, smoothBound);
//				else car.bound.displayVertices(gl, glut, RGB.WHITE_3F   , smoothBound);
//			}
//		
//			for(Bound bound : bounds)
//			{
//				OBB obb = (OBB) bound;
//				obb.displayVertices(gl, glut, RGB.WHITE_3F, smoothBound);
//			}
			
			for(Bound bound : bounds)
			{
				OBB obb = (OBB) bound;
				
				Vehicle car = cars.get(0);
				
				Vec3[] vertices = car.bound.getVertices();
				
				if(car.collisions.contains(obb))
				{
					obb.displayPerimeterPtToPt(gl, glut, vertices[0].toArray(), RGB.WHITE);
					obb.displayPerimeterPtToPt(gl, glut, vertices[1].toArray(), RGB.PURE_RED);
					obb.displayPerimeterPtToPt(gl, glut, vertices[2].toArray(), RGB.PURE_GREEN);
					obb.displayPerimeterPtToPt(gl, glut, vertices[3].toArray(), RGB.PURE_BLUE);
				}
			}
		}
		
		if(enableOBBWireframes)
		{
			for(Vehicle car : cars)
			{
				if(car.colliding)
					 car.bound.renderWireframe(gl, RGB.PURE_RED, smoothBound);
				else car.bound.renderWireframe(gl, RGB.WHITE   , smoothBound);
			}
			
			for(Bound bound : bounds)
				for(Vehicle car : cars)
				{
					OBB obb = (OBB) bound;
					
					Vec3 p0 = car.bound.getPosition();
					Vec3 p1 = p0.subtract(car.bound.u.zAxis.multiply(20));
										
					if(obb.testSegment(p0, p1))
					{
						obb.renderWireframe(gl, RGB.PURE_GREEN, smoothBound);
						
						Vec3 p_ = obb.testRay(p0, car.bound.u.zAxis.negate());
						
						Renderer.displayPoints(gl, glut, new float[][] {p_.toArray()}, RGB.ORANGE, 2, true);
					}
					else if(car.collisions != null && car.collisions.contains(bound))
					{
						 bound.renderWireframe(gl, RGB.PURE_RED, smoothBound);
						 break;
					}
					else bound.renderWireframe(gl, RGB.WHITE   , smoothBound);		
				}
		}
		
		if(enableOBBAxes)
		{
//			for(Car car : cars) car.bound.displayAxes(gl, 10);
//			
//			for(Bound bound : bounds)
//			{
//				OBB obb = (OBB) bound;
//				obb.displayAxes(gl, 20);
//			}
			
			Vehicle car = cars.get(0);
			
//			Vec3 p0 = car.bound.getPosition();
//			Vec3 p1 = p0.subtract(car.bound.u.zAxis.multiply(20));
//			
//			Renderer.displaySegment(gl, p0, p1, RGB.toRGBi(RGB.BRIGHT_YELLOW));
//			
			Vec3[] vertices = car.bound.getVertices();
			
			for(int i = 0; i < 4; i++)
			{
				Vec3 p0 = vertices[i];
				Vec3 p1 = p0.add(Vec3.NEGATIVE_Y_AXIS.multiply(5));
				
				Renderer.displaySegment(gl, p0, p1, RGB.BRIGHT_YELLOW);
			}
		}
		
		if(enableOBBSolids)
			for(Bound bound : bounds)
				bound.renderSolid(gl, RGB.toRGBA(RGB.VIOLET, 0.1f));
		
		for(Vehicle car : cars)
			for(Item item : car.getItems())
				item.renderBound(gl);
		
		for(Item item : itemList) item.renderBound(gl);
		
		gl.glEnable(GL_TEXTURE_2D);
		gl.glEnable(GL2.GL_LIGHTING);
		
		return System.nanoTime() - start;
	}

	public void test(GL2 gl)
	{		
		
	}

	public void resetView(GL2 gl)
	{
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(this.fov, (float) canvasWidth / (float) canvasHeight, 1.0, far);
		
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	    gl.glViewport(0, 0, canvasWidth, canvasHeight);
	}
	
	public static float sceneTimer = 0;
	public static long startTimeCPU;
	public static long   endTimeCPU;

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
		sceneTimer++;
		
		long currentTime = System.currentTimeMillis();
		long timeElapsed = currentTime - startTime;
		
		if(timeElapsed >= 1000)
		{
			frameRate = frames;
			
			frames = 0;
			startTime = currentTime + (timeElapsed % 1000);
			// new start time = the current time + the time elapsed over a second
		}
		
		if(frameIndex >= frameTimes.length) frameIndex = 0;
		
		frameTimes[frameIndex] = currentTime - renderTime;
	}
	
	public static boolean outOfBounds(Vec3 p)
	{
		return p.dot() > GLOBAL_RADIUS * GLOBAL_RADIUS;
	}

	public List<Vehicle> getCars() { return cars; }

	public void sendItemCommand(int itemID) { itemQueue.add(itemID); }
	
	public void addItem(Item item) { itemList.add(item); }
	
	public void addItem(int item, Vec3 c, float trajectory)
	{
		switch(item)
		{
			case GreenShell.ID  : itemList.add(new  GreenShell(this, c, trajectory, true)); break;
			case RedShell.ID    : itemList.add(new    RedShell(this, c, trajectory, true)); break;
			case FakeItemBox.ID : itemList.add(new FakeItemBox(this, c, trajectory)); break;
			case Banana.ID      : itemList.add(new      Banana(this, c)); break;
			case BlueShell.ID   : itemList.add(new   BlueShell(this, c)); break;
			case BobOmb.ID      : itemList.add(new BobOmb(c, null, true)); break;
			case FireBall.ID    : itemList.add(new FireBall(this, c, trajectory)); break;
			
			default: break;
		}
	}

	public void spawnItemsInBound(int item, int quantity, Bound b)
	{
		Random random = new Random();
		
		for(int i = 0; i < quantity; i++)
		{
			addItem(item, b.randomPointInside(), random.nextInt(360));
		}
	}

	public void spawnItemsInSphere(int item, int quantity, Vec3 c, float r)
	{
		spawnItemsInBound(item, quantity, new Sphere(c, r));
	}

	public void spawnItemsInOBB(int item, int quantity, float[] c, float[] u, float[] e)
	{
		spawnItemsInBound(item, quantity, new OBB(c, u, e, null));
	}

	public void clearItems()
	{
		for(Item item : itemList) item.destroy();
		
		itemList.clear();
	}

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
		
		if(!enableTerrain ) bounds.add(floorBound);              
		if(enableObstacles) bounds.addAll(fort.getBounds());
							bounds.addAll(wallBounds);
		
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
	
	public void generateFoliage(GL2 gl, int patches)
	{
		long start = System.currentTimeMillis();
		
		grassPatches = new GrassPatch[patches];
		
		Random generator = new Random();
		
		for(int i = 0; i < patches; i++)
		{
			Vec3 centre = new Vec3();
	    	
			centre.x = generator.nextFloat() * 360 - 180;
			centre.z = generator.nextFloat() * 360 - 180;
			
			grassPatches[i] = new GrassPatch(gl, terrain.tree, centre.toArray(), 64, 10);
		}	
		
		System.out.printf("Foliage Generated: %d ms\n", (System.currentTimeMillis() - start));
	}

	public void generateFoliage(int patches, float spread, int patchSize)
	{
		foliage = new ArrayList<BillBoard>();
		Random generator = new Random(); 
	    
//	    for(int i = 0; i < patches; i++)
//	    {
//	    	int t = (i < patches * 0.5) ? 3 : generator.nextInt(3);
//	    	
//	    	Vec3 p = new Vec3();
//	    	
//	    	p.x = generator.nextFloat() * 360 - 180;
//	    	p.z = generator.nextFloat() * 360 - 180;
//	    	
//	    	if(i > patches * 0.75)
//	    	{
//	    		p.y = (terrain.enableQuadtree) ? terrain.tree.getCell(p.toArray(), terrain.tree.detail).getHeight(p.toArray()) : terrain.getHeight(p.toArray());
//	    		foliage.add(new BillBoard(p, 3, t));
//	    	}
//	    	else
//	    	{    	
//		    	int k = generator.nextInt(patchSize - 1) + 1; 
//		    	
//		    	for(int j = 0; j < k; j++)
//		    	{
//		    		Vec3 p0 = new Vec3(p);
//		    		
//		    		double incline = Math.toRadians(generator.nextInt(360));
//		    		double azimuth = Math.toRadians(generator.nextInt(360));
//		    		
//		    		p0.x += (float) (Math.sin(incline) * Math.cos(azimuth) * spread);
//		    		p0.z += (float) (Math.sin(incline) * Math.sin(azimuth) * spread);
//		    		
//		    		if(Math.abs(p0.x) < 200 && Math.abs(p0.z) < 200)
//		    		{
//		    			p0.y = (terrain.enableQuadtree) ? terrain.tree.getCell(p0.toArray(), terrain.tree.detail).getHeight(p0.toArray()) : terrain.getHeight(p0.toArray());
//		    			foliage.add(new BillBoard(p0, 2, t));
//		    		}
//		    	}
//	    	}
//	    }
	    
//	    for(int i = 0; i < 30; i++)
//	    {
//	    	int t = 4 + generator.nextInt(4);
//	    	
//	    	Vec3 p = new Vec3();
//	    	
//	    	p.x = generator.nextFloat() * 360 - 180;
//	    	p.z = generator.nextFloat() * 360 - 180;
//	    	
//	    	p.y = (terrain.enableQuadtree) ? terrain.tree.getCell(p.toArray(), terrain.tree.detail).getHeight(p.toArray()) : terrain.getHeight(p.toArray());
//	    	
//	    	foliage.add(new BillBoard(p, 30, t));
//	    }
//	    
//	    for(int i = 0; i < 30; i++)
//	    {
//	    	int t = 8 + generator.nextInt(2);
//	    	
//	    	Vec3 p = new Vec3();
//	    	
//	    	p.x = generator.nextFloat() * 360 - 180;
//	    	p.z = generator.nextFloat() * 360 - 180;
//	    	
//	    	p.y = (terrain.enableQuadtree) ? terrain.tree.getCell(p.toArray(), terrain.tree.detail).getHeight(p.toArray()) : terrain.getHeight(p.toArray());
//
//	    	foliage.add(new BillBoard(p, 4, t));
//	    }
	}
	
	public void updateFoliage()
	{
		for(BillBoard board : foliage)
		{
			Vec3 p = board.sphere.c;
			board.sphere.c.y = (terrain.enableQuadtree) ?
					terrain.tree.getCell(p.toArray(), terrain.tree.detail).getHeight(p.toArray()) :
					terrain.getHeight(p.toArray());
		}
		
		for(GrassPatch patch : grassPatches) patch.update = true;
	}

	public void printDataToFile(String file, String[] headers, long[][] data)
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
			
			for(int j = 0; j < headers.length; j++)
			{
				long sum = 0;
				
				for(int i = 0; i < data.length; i++) sum += data[i][j];
				
				sum /= data.length;
				
				out.write(String.format("%11.3f", sum / 1E6) + "\t");
			}	
			
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
		if(e.isShiftDown()) { shiftEvent(e); return; }
		
		switch(e.getKeyCode())
		{
			case KeyEvent.VK_ESCAPE: System.exit(0); break;
			
			case KeyEvent.VK_H:  enableObstacles = !enableObstacles; break;
			
			case KeyEvent.VK_T:   enableTerrain = !enableTerrain; for(Vehicle car : cars) car.friction = 1; break;
			case KeyEvent.VK_F10: enableItemBoxes = !enableItemBoxes; break;
			
			case KeyEvent.VK_BACK_SLASH: shadowMap = !shadowMap; break;
			case KeyEvent.VK_F9: sphereMap = !sphereMap; break;
			case KeyEvent.VK_P: bloom.cycleMode(); break;
			
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
			
//			case KeyEvent.VK_F1 : fort.renderMode++; fort.renderMode %= 4; break;
			case KeyEvent.VK_F3 : Item.renderMode++; Item.renderMode %= 2; break;
			case KeyEvent.VK_Z  : foliageMode++; foliageMode %= 3; break;
			
			case KeyEvent.VK_6:  Item.toggleBoundSolids(); break;
			case KeyEvent.VK_7:  Item.toggleBoundFrames(); break;
			
			case KeyEvent.VK_8:  fort.displayModel = !fort.displayModel; break;
			case KeyEvent.VK_0:  displaySkybox = !displaySkybox; break;

			case KeyEvent.VK_PERIOD       :
			case KeyEvent.VK_EQUALS       :
			case KeyEvent.VK_MINUS        : terrain.keyPressed(e); updateFoliage(); break;
			case KeyEvent.VK_J            :
			case KeyEvent.VK_K            :
			case KeyEvent.VK_I            :
			case KeyEvent.VK_U            : // TODO temporary for demonstration
			case KeyEvent.VK_O            :
			case KeyEvent.VK_F11          :
			case KeyEvent.VK_OPEN_BRACKET :
			case KeyEvent.VK_CLOSE_BRACKET:
			case KeyEvent.VK_QUOTE        :
			case KeyEvent.VK_NUMBER_SIGN  :
			case KeyEvent.VK_SLASH        : terrain.keyPressed(e); break; 
			case KeyEvent.VK_COMMA        : terrain.keyPressed(e); generateFoliage(60, 10, 30); break;
			
			case KeyEvent.VK_SPACE: console.parseCommand(command.getText()); break;
	
			default: for(Vehicle car : cars) car.keyPressed(e); break;
		}
	}
	
	public static boolean occludeSphere = false;
	
	public void shiftEvent(KeyEvent e)
	{
		switch(e.getKeyCode())
		{
			case KeyEvent.VK_1: spawnItemsInSphere(GreenShell.ID, 10, new Vec3(0, 100, 0), 50); break;
			case KeyEvent.VK_2: spawnItemsInSphere(RedShell.ID, 10, new Vec3(0, 100, 0), 50); break;
			case KeyEvent.VK_3: spawnItemsInOBB(FakeItemBox.ID, 10, new float[] {0, 100, 0}, ORIGIN, new float[] {150, 50, 150}); break;
			case KeyEvent.VK_4: spawnItemsInSphere(Banana.ID, 10, new Vec3(0, 100, 0), 50); break;
			case KeyEvent.VK_5: spawnItemsInOBB(BlueShell.ID, 10, new float[] {0, 100, 0}, ORIGIN, new float[] {150, 50, 150}); break;
			case KeyEvent.VK_6: spawnItemsInOBB(BobOmb.ID, 10, new float[] {0, 100, 0}, ORIGIN, new float[] {150, 50, 150}); break;
			case KeyEvent.VK_7: addItem(FireBall.ID, new Vec3(0, 10, 0), 30);
			
			case KeyEvent.VK_D: cars.get(0).enableDeform = !cars.get(0).enableDeform; break;
			case KeyEvent.VK_P: enableParallax = !enableParallax; break;
			case KeyEvent.VK_I: occludeSphere = !occludeSphere; break;
			case KeyEvent.VK_W: water.frozen = !water.frozen; break;
			case KeyEvent.VK_M: water.magma = !water.magma; break;
			case KeyEvent.VK_F: enableFoliage = !enableFoliage; break;
			
			case KeyEvent.VK_O: enableOcclusion = !enableOcclusion; break;
			
			case KeyEvent.VK_B: enableBump = !enableBump; break;
			
			case KeyEvent.VK_S: ShadowCaster.cycle(); break;
			case KeyEvent.VK_Z: enableShadow = !enableShadow; break;
			case KeyEvent.VK_L: lightID++; lightID %= lights.length; light = lights[lightID]; break;
			case KeyEvent.VK_K: singleLight = !singleLight; break;
			case KeyEvent.VK_J: displayLight = !displayLight; break;
			case KeyEvent.VK_R: rimLighting = !rimLighting; break;
			
			case KeyEvent.VK_V: Model.enableVBO = !Model.enableVBO; break;
			
			case KeyEvent.VK_0: brickWall.cycleMode(); break;
			
//			case KeyEvent.VK_G: smokeCloud = new ParticleEngine(10000); break;
			case KeyEvent.VK_G: Water.cycle(); break;
			
			case KeyEvent.VK_T:
			{
				star.setCollected(!star.isCollected());
				shine.setCollected(!shine.isCollected());
				pipe.setClear(!pipe.isClear());
				break;
			}
			case KeyEvent.VK_E: energyField.cycle(); break;
			
			case KeyEvent.VK_F1: testMode = !testMode; break;
			case KeyEvent.VK_F2: LightningStrike.debugMode = !LightningStrike.debugMode; break;
			case KeyEvent.VK_F3:
			{
				Set<Map.Entry<String, Model>> models = Model.model_set.entrySet();
				for(Map.Entry<String, Model> model : models) model.getValue().export(model.getKey());
				break;
			}
			case KeyEvent.VK_F4: displayDepth = !displayDepth; break;
			
			default: break;
		}
	}
	
	public static boolean displayDepth = false;
	
	public void keyReleased(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			default: for(Vehicle car : cars) car.keyReleased(e); break;
		}
	}

	public void keyTyped(KeyEvent e)
	{
		switch (e.getKeyChar())
		{
			default: break;
		}
	}

	public KeyEvent pressKey(char c, int modifier)
	{
		long when = System.nanoTime();
		int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
		
		return new KeyEvent(command, KeyEvent.KEY_PRESSED, when, modifier, keyCode, c);
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

	public boolean rightClick;
	
	public void mousePressed(MouseEvent e)
	{
		selecter.setSelection(e.getX(), e.getY());
		
		rightClick = SwingUtilities.isRightMouseButton(e);
		mousePressed = true;
	}

	public void mouseReleased(MouseEvent e)
	{
		mousePressed = false;
		terrain.tree.setHeights();
	}

	public void actionPerformed(ActionEvent event)
	{
		     if(event.getActionCommand().equals("console"       )) console.parseCommand(command.getText());
		else if(event.getActionCommand().equals("load_project"  )) console.parseCommand("profile project");
		else if(event.getActionCommand().equals("load_game"     )) console.parseCommand("profile game");
		else if(event.getActionCommand().equals("load_brimstone")) console.parseCommand("profile brimstone");
		else if(event.getActionCommand().equals("load_simple"   )) console.parseCommand("profile simple");
		     
		else if(event.getActionCommand().equals("no_weather"   )) enableBlizzard = false;
		     
		else if(event.getActionCommand().equals("snow"         )) { enableBlizzard = true; blizzard = new Blizzard(this, flakeLimit, new Vec3(0.2f, -1.5f, 0.1f), StormType.SNOW); }
		else if(event.getActionCommand().equals("rain"         )) { enableBlizzard = true; blizzard = new Blizzard(this, flakeLimit, new Vec3(0.0f, -4.0f, 0.0f), StormType.RAIN); }
		     
		else if(event.getActionCommand().equals("shadow_low"   )) { caster.setQuality(ShadowQuality.LOW);  resetShadow = true; }
		else if(event.getActionCommand().equals("shadow_medium")) { caster.setQuality(ShadowQuality.MED);  resetShadow = true; }
		else if(event.getActionCommand().equals("shadow_high"  )) { caster.setQuality(ShadowQuality.HIGH); resetShadow = true; }
		else if(event.getActionCommand().equals("shadow_best"  )) { caster.setQuality(ShadowQuality.BEST); resetShadow = true; }
		     
		else if(event.getActionCommand().equals("recalc_norms" )) terrain.tree.resetNormals();
		else if(event.getActionCommand().equals("recalc_tangs" )) terrain.tree.resetTangent();    
		else if(event.getActionCommand().equals("save_heights" ))
		{
			Quadtree tree = terrain.tree;
			tree.setHeights();
			tree.setGradient(tree.gradient);
		}
		else if(event.getActionCommand().equals("reset_heights")) terrain.tree.resetHeights();
		     
		else if(event.getActionCommand().equals("set_ambience"))
		{
			float[] color = chooseColor(light.getAmbience(), "Ambient Lighting Color");
			
			if(synchLights) for(Light l : lights) l.setAmbience(color);
			else light.setAmbience(color);
		}
		else if(event.getActionCommand().equals("set_specular"))
		{
			float[] color = chooseColor(light.getSpecular(), "Specular Lighting Color");
			
			if(synchLights) for(Light l : lights) l.setSpecular(color);
			else light.setSpecular(color);
		}
		else if(event.getActionCommand().equals("set_diffuse"))
		{
			float[] color = chooseColor(light.getDiffuse(), "Diffuse Lighting Color");
			
			if(synchLights) for(Light l : lights) l.setDiffuse(color);
			else light.setDiffuse(color);    
		}
		else if(event.getActionCommand().equals("set_emission" )) light.setEmission(chooseColor(light.getEmission(), "Emissive Material Color"));
		     
		else if(event.getActionCommand().equals("material_specular")) terrain.tree.specular = chooseColor(terrain.tree.specular, "Specular Reflectivity");
		
		else if(event.getActionCommand().equals("fog_color")) fogColor   = chooseColor(fogColor, "Fog Color");
		else if(event.getActionCommand().equals("bg_color" )) background = chooseColor(background, "Background Color");
		else if(event.getActionCommand().equals("sky_color")) skybox.setSkyColor(chooseColor(skybox.getSkyColor(), "Sky Color"));
		else if(event.getActionCommand().equals("horizon"  )) skybox.setHorizonColor(chooseColor(skybox.getHorizonColor(), "Horizon Color"));
		     
		else if(event.getActionCommand().equals("car_color")) cars.get(0).setColor(chooseColor(cars.get(0).getColor(), "Body Color"));
		     
		else if(event.getActionCommand().equals("close")) System.exit(0);
	}
	
	public float[] chooseColor(float[] color, String title)
	{
		Color oldColor = color.length > 3 ? new Color(color[0], color[1], color[2], color[3]) :
											new Color(color[0], color[1], color[2]);
		Color newColor = JColorChooser.showDialog(frame, title, oldColor);
		
		if(newColor == null) return color;
		
		if(color.length > 3) color = RGB.toRGBA(newColor);
		else color = RGB.toRGB(newColor);
		
		return color;
	}

	public void itemStateChanged(ItemEvent ie)
	{
		Object source = ie.getItemSelectable();
		boolean selected = (ie.getStateChange() == ItemEvent.SELECTED);
		
		     if(source.equals(menuItem_multisample)) multisample                  = selected;
		else if(source.equals(menuItem_anisotropic)) Renderer.anisotropic         = selected; 
		else if(source.equals(menuItem_bloom      )) enableBloom                  = selected;  
		else if(source.equals(menuItem_motionblur )) enableBlur                   = selected;
		else if(source.equals(menuItem_fog        )) enableFog                    = selected; 
		else if(source.equals(menuItem_synch_light)) synchLights                  = selected;      
		else if(source.equals(menuItem_normalize  )) normalize                    = selected;
		else if(source.equals(menuItem_smooth     )) Light.smoothShading          = selected;
		else if(source.equals(menuItem_attenuate  ))
		{
			if(synchLights) for(Light l : lights) l.enableAttenuation = selected;
			else light.enableAttenuation = selected;
		}
		else if(source.equals(menuItem_secondary  )) Light.seperateSpecular       = selected;
		else if(source.equals(menuItem_local      )) Light.localViewer            = selected;    
		else if(source.equals(menuItem_water      )) terrain.enableWater          = selected;   
		else if(source.equals(menuItem_solid      )) terrain.tree.solid           = selected;
		else if(source.equals(menuItem_elevation  )) terrain.tree.reliefMap       = selected;  
		else if(source.equals(menuItem_frame      )) terrain.tree.frame           = selected;
		else if(source.equals(menuItem_texturing  )) terrain.tree.enableTexture   = selected;
		else if(source.equals(menuItem_bumpmaps   )) terrain.tree.enableBumpmap   = selected;
		else if(source.equals(menuItem_caustics   )) terrain.tree.enableCaustic   = selected;   
		else if(source.equals(menuItem_malleable  )) terrain.tree.malleable       = selected;     
		else if(source.equals(menuItem_vnormals   )) terrain.tree.vNormals        = selected; 
		else if(source.equals(menuItem_vtangents  )) terrain.tree.vTangents       = selected;     
		else if(source.equals(menuItem_shading    )) terrain.tree.enableShading   = selected;
		else if(source.equals(menuItem_vcoloring  )) terrain.tree.enableColoring  = selected;
		else if(source.equals(menuItem_reverse    )) cars.get(0).invertReverse    = selected;
		else if(source.equals(menuItem_shaking    )) cars.get(0).camera.shaking   = selected;
		else if(source.equals(menuItem_trackball  )) cars.get(0).camera.trackball = selected;
		else if(source.equals(menuItem_focal_blur )) enableFocalBlur              = selected;
		else if(source.equals(menuItem_mirage     )) focalBlur.enableMirage       = selected;    
		else if(source.equals(menuItem_tag        )) cars.get(0).displayTag       = selected;    
		else if(source.equals(menuItem_settle     )) blizzard.enableSettling      = selected;
		else if(source.equals(menuItem_splash     )) blizzard.enableSplashing     = selected;
		else if(source.equals(menuItem_shaders    )) ;  
		else if(source.equals(menuItem_shadows    )) enableShadow                 = selected;   
		else if(source.equals(menuItem_aberration )) cars.get(0).enableAberration = selected;     
		else if(source.equals(menuItem_cubemap    ))
		{
			cars.get(0).enableChrome = selected;
			cars.get(0).resetGraph();
		}
//		else if(source.equals(shaderToggle))
//		{
//			if(selectedShader != null && selectedUniform != null)
//				uniformCommands.add(new UniformCommand(selectedShader.toString(), selectedUniform.toString(), selected));
//		}
	}

	public void valueChanged(ListSelectionEvent e)
	{
		int index = quadList.getSelectedIndex();
		terrain.selectQuadtree(listModel.elementAt(index));
		
		setCheckBoxes();
	}

	public void mouseMoved(MouseEvent e)
	{
		if(cars != null && !cars.isEmpty()) cars.get(0).camera.mouseMoved(e);
	}
	
	public void mouseDragged(MouseEvent e)
	{
		if(cars != null && !cars.isEmpty()) cars.get(0).camera.mouseDragged(e);
	}
	
	Queue<UniformCommand> uniformCommands = new ArrayBlockingQueue<UniformCommand>(100);

	public void stateChanged(ChangeEvent e)
	{
		Object source = e.getSource();
		
		if(source.equals(sliderEta))
		{
			JSlider slider = (JSlider) source;
			
			float value = (float) slider.getValue() / 100.0f;
			
			Light.setShininess((int) (value * 128));
		}
		else if(source.equals(shaderSpinner))
		{
			SpinnerModel model = shaderSpinner.getModel();
			double value = (Double) ((SpinnerNumberModel) model).getValue();
			
			if(selectedShader != null && selectedUniform != null)
				uniformCommands.add(new UniformCommand(selectedShader.toString(), selectedUniform.toString(), (float) value));
		}	
	}
	
	class UniformCommand
	{
		String shader, uniform;
		float value;
		
		public UniformCommand(String shader, String uniform, float value)
		{
			this.shader  = shader;
			this.uniform = uniform;
			this.value   = value;
		}
	}

	public void valueChanged(TreeSelectionEvent e)
	{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) shaderTree.getLastSelectedPathComponent();

		if (node == null) return;

		if (node.isLeaf())
		{
			selectedUniform = node;
			selectedShader  = (DefaultMutableTreeNode) node.getParent();
			
			cars.get(0).getHUD().broadcast("Shader: " + selectedShader + ", Uniform: " + selectedUniform);
		}
	}
}