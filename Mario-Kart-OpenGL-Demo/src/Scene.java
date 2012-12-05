/** Utility imports **/
import static graphics.util.Renderer.displayTexturedCuboid;
import static graphics.util.Renderer.displayTexturedObject;
import static graphics.util.Renderer.displayWildcardObject;
import static graphics.util.Renderer.displayWireframeObject;
import static javax.media.opengl.GL.GL_COLOR_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_TEST;
import static javax.media.opengl.GL.GL_DONT_CARE;
import static javax.media.opengl.GL.GL_FRONT;
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
import static javax.media.opengl.GL2ES1.GL_LIGHT_MODEL_AMBIENT;
import static javax.media.opengl.GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_AMBIENT;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHT0;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_POSITION;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SHININESS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SMOOTH;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SPECULAR;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;
import graphics.util.Face;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;
import javax.swing.JTextField;

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
public class Scene implements GLEventListener, KeyListener, MouseWheelListener, ActionListener
{
	private Console console;
	private JTextField consoleInput;
	
	private int canvasWidth = 840;
	private int canvasHeight = 680;
	
	public static final int FPS = 60;
	public static final int MIN_FPS = 30;
	
	private final FPSAnimator animator;
	
	public static final float FOV = 100.0f;
	
	private GLU glu;
	private GLUT glut;
	
	private boolean enableAnimation = true;
	
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
		{"World", "Vehicles", "Items", "Particles", "Bounds", "HUD"};
	
	
	/** Texture Fields **/
	private Texture brickWall;
	private Texture brickWallTop;
	
	private Texture greenGranite;
	private Texture greenMetal;
	private Texture blueGranite;
	private Texture blueMetal;
	private Texture redGranite;
	private Texture redMetal;
	private Texture yellowGranite;
	private Texture yellowMetal;
	
	private Texture cobble;

	
	/** Model Fields **/
	private boolean displayModels = true;
	
	private List<Face> environmentFaces;
	private List<Face> fortFaces;
	
	private int environmentList;
	private int fortList;
	
	private List<Car> cars = new ArrayList<Car>();
	
	public static final float[] ORIGIN = {0.0f, 0.0f, 0.0f};

	private List<ItemBox> itemBoxes = new ArrayList<ItemBox>();
	private boolean enableItemBoxes = false;
	
	
	/** Fog Fields **/
	public float fogDensity = 0.004f;
	public float[] fogColor = {1.0f, 1.0f, 1.0f, 1.0f};
	
	
	/** Lighting Fields **/
	private float[] global_specular = {1.0f, 1.0f, 1.0f};
	private float[] global_ambience = {0.8f, 0.8f, 0.8f, 1.0f};
	
	private float[] position = {0.0f, 100.0f, 0.0f, 0.0f};
	
    private float[] material_ambience  = {0.7f, 0.7f, 0.7f, 1.0f};
    private float[] material_shininess = {100.0f};
    
    private float cloud_density = 1.0f;
    private static final float MIN_DENSITY = 0.5f;
    private static final float DENSITY_INC = 0.0125f;
	
    
    /** Environment Fields **/
    private boolean enableSkybox = true;
	
	
	/** Collision Detection Fields **/	
	private boolean enableBoundVisuals     = true;
	
	private boolean enableOBBAxes          = false;
	private boolean enableOBBVertices      = false;
	private boolean enableOBBWireframes    = false;
	private boolean enableOBBSolids        = false;
	private boolean enableClosestPoints    = false;
	
	private boolean enableObstacleWireframes = false;
	
	private boolean enableObstacles = false;
	
	private List<OBB> wallBounds;
	
	
	/** Music Fields **/
	private boolean musicPlaying = false;
	private static final String MUTE_CITY =
			"file:///" + System.getProperty("user.dir") + "//music//Mute City.mp3";
	
	
	private List<Particle> particles = new ArrayList<Particle>();

	
	private Queue<Integer> itemQueue = new ArrayBlockingQueue<Integer>(100);
	private List<Item> itemList = new ArrayList<Item>();
	
	
	int boostCounter = 0;
	boolean enableMotionBlur = true;
	
	
	HeightMap heightMap;
	SplashShape[] splashShapes;
	
	
	public Scene()
	{
		JFrame frame = new JFrame();
		
		GLCanvas canvas = new GLCanvas();
		canvas.setPreferredSize(new Dimension(canvasWidth, canvasHeight));
		canvas.addGLEventListener(this);
		canvas.addKeyListener(this);
		canvas.addMouseWheelListener(this);
		canvas.setFocusable(true);
		canvas.requestFocus();

		animator = new FPSAnimator(canvas, FPS, true);
		
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
		
		Container content = frame.getContentPane();
		
		content.setLayout(new BorderLayout());
		
		content.add(canvas, BorderLayout.CENTER);
		content.add(consoleInput, BorderLayout.SOUTH);

		frame.pack();
		frame.setTitle("Mario Kart OpenGL Demo");
		frame.setVisible(true);
		
		animator.start();
	}
	
	public static void main(String[] args) { new Scene(); }
	
	public int getFrameRate() { return frameRate; }
	
	public int getHeight() { return canvasHeight; }
	
	public int getWidth()  { return canvasWidth;  }
	
	public void init(GLAutoDrawable drawable)
	{
		execution = Calendar.getInstance();
		
		try
		{			
			brickWall     = TextureIO.newTexture(new File("tex/longBrick.jpg"), false);
			brickWallTop  = TextureIO.newTexture(new File("tex/longBrickTop.jpg"), false);
			
			greenGranite  = TextureIO.newTexture(new File("tex/greenGranite.jpg"), true);
			greenMetal    = TextureIO.newTexture(new File("tex/greenMetal.jpg"), true);
			blueGranite   = TextureIO.newTexture(new File("tex/blueGranite.jpg"), true);
			blueMetal     = TextureIO.newTexture(new File("tex/blueMetal.jpg"), true);
			redGranite    = TextureIO.newTexture(new File("tex/redGranite.jpg"), true);
			redMetal      = TextureIO.newTexture(new File("tex/redMetal.jpg"), true);
			yellowGranite = TextureIO.newTexture(new File("tex/yellowGranite.jpg"), true);
			yellowMetal   = TextureIO.newTexture(new File("tex/yellowMetal.jpg"), true);
			
			cobble        = TextureIO.newTexture(new File("tex/cobbles.jpg"), true);
		}
		catch (Exception e) { e.printStackTrace(); }
		
		GL2 gl = drawable.getGL().getGL2();
		glu = new GLU();
		glut = new GLUT();
		
		// may provide extra speed depending on machine
		gl.setSwapInterval(0); 

		gl.glShadeModel(GL_SMOOTH);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

		/** Texture Options **/
		gl.glEnable(GL2.GL_TEXTURE_2D);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_BASE_LEVEL, 0);
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_MAX_LEVEL,  4);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR_MIPMAP_NEAREST);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_NEAREST);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,     GL_REPEAT);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,     GL_REPEAT);
		

		/** Fog Setup **/
		gl.glEnable(GL_FOG);
		gl.glFogi  (GL_FOG_MODE, GL_EXP2);
		gl.glFogfv (GL_FOG_COLOR, fogColor, 0);
		gl.glFogf  (GL_FOG_DENSITY, fogDensity);
		gl.glFogf  (GL_FOG_START, 1.0f);
		gl.glFogf  (GL_FOG_END, 5.0f);
		gl.glHint  (GL_FOG_HINT, GL_DONT_CARE);
		
		
		/** Lighting Setup **/
		gl.glEnable(GL_LIGHTING);
	    gl.glEnable(GL_LIGHT0);
		
	    gl.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);
	    gl.glEnable(GL2.GL_COLOR_MATERIAL);
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);
		
		long setupStart = System.currentTimeMillis();
		
		/** Model Setup **/
		environmentFaces = OBJParser.parseTriangles("obj/environment.obj");
		fortFaces		 = OBJParser.parseTriangles("obj/blockFort.obj");
	    
	    environmentList = gl.glGenLists(1);
	    gl.glNewList(environmentList, GL2.GL_COMPILE);
	    displayTexturedObject(gl, environmentFaces);
	    gl.glEndList();
	    
	    fortList = gl.glGenLists(4);

	    gl.glNewList(fortList, GL2.GL_COMPILE);
	    displayWildcardObject(gl, fortFaces, new Texture[] {greenMetal, greenGranite});
	    gl.glEndList();
	    
	    gl.glNewList(fortList + 1, GL2.GL_COMPILE);
		displayWildcardObject(gl, fortFaces, new Texture[] {blueMetal, blueGranite});
	    gl.glEndList();
	    
	    gl.glNewList(fortList + 2, GL2.GL_COMPILE);
		displayWildcardObject(gl, fortFaces, new Texture[] {redMetal, redGranite});
	    gl.glEndList();
	    
	    gl.glNewList(fortList + 3, GL2.GL_COMPILE);
		displayWildcardObject(gl, fortFaces, new Texture[] {yellowMetal, yellowGranite});
	    gl.glEndList();
	    
	    new GreenShell (gl, this, null, 0, false);
	    new RedShell   (gl, this, null, 0, false);
	    new BlueShell  (gl, this, null, 0);
	    new FakeItemBox(gl, this, null);
	    new Banana     (gl, this, null, 0);
	    
	    new BoostParticle(ORIGIN, null, 0, 0, 0, false, false);
	    new LightningParticle(ORIGIN);
	    new StarParticle(ORIGIN, null, 0, 0);
	    
	    cars.add(new Car(gl, new float[] { 78.75f, 1.8f, 0}, 0,   0, 0, this));
	    
	    if(GamePad.numberOfGamepads() > 1)
	    	cars.add(new Car(gl, new float[] {-78.75f, 1.8f, 0}, 0, 180, 0, this));
	    
	    if(GamePad.numberOfGamepads() > 2)
	    	cars.add(new Car(gl, new float[] {0, 1.8f,  78.75f}, 0, 270, 0, this));
	    	
	    if(GamePad.numberOfGamepads() > 3)
		    cars.add(new Car(gl, new float[] {0, 1.8f, -78.75f}, 0,  90, 0, this));
		    
	    if(enableItemBoxes)
	    {
	    	itemBoxes.addAll(ItemBox.generateDiamond( 56.25f, 30f, particles));
	    	itemBoxes.addAll(ItemBox.generateSquare (101.25f, 60f, particles));
	    	itemBoxes.addAll(ItemBox.generateSquare (123.75f, 30f, particles));
	    	itemBoxes.addAll(ItemBox.generateDiamond(   180f,  0f, particles));
	    }
	    
	    wallBounds = BoundParser.parseOBBs("bound/blockFort.bound");
	   
	    long setupEnd = System.currentTimeMillis();
	    System.out.println("Setup Time: " + (setupEnd - setupStart) + " ms");
	    
	    startTime = System.currentTimeMillis();
	    //records the time prior to the rendering of the first frame after initialization
	    
	    frameTimes     = new long[240];
	    renderTimes    = new long[240][6];
	    collisionTimes = new long[240];
	    
	    heightMap = new HeightMap(gl, 100);
	    splashShapes = new SplashShape[10];
	    
	    Random generator = new Random();
	    
	    for (int i = 0; i < 10; i++)
	    	splashShapes[i] = new SplashShape(cobble, heightMap.heights, generator.nextInt(10) + 1);
	}
	
	public void display(GLAutoDrawable drawable)
	{	
		GL2 gl = drawable.getGL().getGL2();
		
		gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		gl.glClearDepth(1.0f);
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		gl.glLoadIdentity();
		gl.glMatrixMode(GL_MODELVIEW);
		
		setupFog(gl);
		
		registerItems(gl);
		
		if(enableAnimation) update();
		
		int[] order = new int[cars.size()];
		
		int i = getRenderOrder(order);
		int _i = i; //temporary variable _i used to store the boost count this frame
		
		renderTime = System.currentTimeMillis();
		
		for(int index : order)
		{
			Car car = cars.get(index);
			
			setupScene(gl, index);
			car.setupCamera(gl, glu);
			setupLights(gl);
			
			if(displayModels)
			{
				renderTimes[frameIndex][0] = renderWorld(gl);
				render3DModels(gl, car);
			}
			
			renderTimes[frameIndex][3] = renderParticles(gl, car);
			Particle.resetTexture();
			
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
			
			renderTimes[frameIndex][4] = renderBounds(gl);
			renderTimes[frameIndex][5] = car.renderHUD(gl, glu);
		}
		
		/* 
		 * Loads the current frame into the accumulation buffer; this is so that if
		 * motion blur occurs in the next frame, any old frames stored in the buffer
		 * will be over-written to avoid displaying visual artifacts.
		 * */
		if(enableMotionBlur && _i != boostCounter) gl.glAccum(GL_LOAD, 1.0f);
		boostCounter = _i;
		
		calculateFPS();
	}

	private int getRenderOrder(int[] order)
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

	private void update()
	{
		removeItems();
		
		removeParticles();
		
		for(ItemBox box : itemBoxes) box.update(cars); 
		
		updateItems();
		
		ItemBox.increaseRotation();
		FakeItemBox.increaseRotation();
		
		for(Particle p : particles) p.update();
		
		detectCarCollisions();
		for(Car car : cars) car.update();
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

	private void setupFog(GL2 gl)
	{
		if(cloud_density < 1) cloud_density += DENSITY_INC;
		
		float c = cloud_density;
		gl.glColor3f(c, c, c); //affect the color of the environment in addition to fog
		gl.glFogfv (GL_FOG_COLOR, new float[] {c, c, c, 1}, 0);
		
		gl.glFogf  (GL_FOG_DENSITY, fogDensity);
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
		glu.gluPerspective(FOV, ratio, 2.0, 700.0);
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity();
		
		gl.glClearAccum(0, 0, 0, 0);
		gl.glClear(GL_ACCUM_BUFFER_BIT);
	}

	public void dispose(GLAutoDrawable drawable) {}
	
	public List<Car> getCars() { return cars; }

	public void sendItemCommand(int itemID) { itemQueue.add(itemID); }
	
	public void addItem(Item item) { itemList.add(item); }
	
	public List<Item> getItems() { return itemList; }
	
	public void clearItems() { itemList.clear(); }
	
	public void addParticle(Particle p) { particles.add(p); }
	
	public void addParticles(List<Particle> particles) { this.particles.addAll(particles); }
	
	public List<Particle> getParticles() { return particles; }
	
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
		
		collisionTimes[frameIndex] = detectItemCollisions();
	}

	private long detectItemCollisions()
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
	
	private void detectCarCollisions()
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
	
	public void removeParticles()
	{
		List<Particle> toRemove = new ArrayList<Particle>();
		
		for(Particle particle : particles)
			if(particle.isDead()) toRemove.add(particle);
		
		particles.removeAll(toRemove);
	}
	
	public void removeItems()
	{
		List<Item> toRemove = new ArrayList<Item>();
		
		for(Item item : itemList)
			if(item.isDead()) toRemove.add((Item) item);
		
		itemList.removeAll(toRemove);
		
		for(Car car : cars) car.removeItems();
	}
	
	public long renderParticles(GL2 gl, Car car)
	{
		long start = System.nanoTime();
		
		for(Particle particle : particles)
		{
			if(car.isSlipping()) particle.render(gl, car.slipTrajectory);
			else particle.render(gl, car.trajectory);
		}
		
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
		renderTimes[frameIndex][1] = renderVehicles(gl, car);

		for(ItemBox box : itemBoxes)
			if(!box.isDead()) box.render(gl, car.trajectory);
		
		renderTimes[frameIndex][2] = renderItems(gl, car);
	}

	private long renderItems(GL2 gl, Car car)
	{
		long start = System.nanoTime();

		for(Item item : itemList)
			if(!item.isDead()) item.render(gl, car.trajectory);
		
		return System.nanoTime() - start;
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

	/**
	 * This method renders the world geometry that represents the artificial
	 * boundaries of the virtual environment. This consists of the skybox and
	 * the terrain itself.
	 */
	private long renderWorld(GL2 gl)
	{
		long start = System.nanoTime();
		
		gl.glPushMatrix();
		{
			gl.glTranslatef(0, 0, 0);
			gl.glScalef(40.0f, 40.0f, 40.0f);

			if(enableSkybox)
				gl.glCallList(environmentList);
		}	
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		{
			heightMap.render(gl, glut);
//			for(SplashShape shape : splashShapes) shape.render(gl);
		}	
		gl.glPopMatrix();
		
		if(enableObstacles) renderObstacles(gl);
			
		Texture[] textures = {brickWall, brickWallTop, brickWall};
	    	 
		gl.glPushMatrix();
		{
			displayTexturedCuboid(gl,       0, 45,  206.25, 202.5, 45,  3.75,  0, textures);
			displayTexturedCuboid(gl,       0, 45, -206.25, 202.5, 45,  3.75,  0, textures);
	    	displayTexturedCuboid(gl,  206.25, 45,       0, 202.5, 45,  3.75, 90, textures);
	    	displayTexturedCuboid(gl, -206.25, 45,       0, 202.5, 45,  3.75, 90, textures);
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
		
		gl.glPushMatrix();
		{
			gl.glTranslatef(90, 30, 90);
			gl.glScalef(30.0f, 30.0f, 30.0f);

			if(enableObstacleWireframes)
				 displayWireframeObject(gl, fortFaces, new float[] {0, 0, 0});
			else gl.glCallList(fortList);
		}	
		gl.glPopMatrix();

		gl.glPushMatrix();
		{
			gl.glTranslatef(-90, 30, 90);
			gl.glRotatef(-90, 0, 1, 0);
			gl.glScalef(30.0f, 30.0f, 30.0f);

			if(enableObstacleWireframes)
				 displayWireframeObject(gl, fortFaces, new float[] {0, 0, 0});
			else gl.glCallList(fortList + 1);

		}	
		gl.glPopMatrix();

		gl.glPushMatrix();
		{
			gl.glTranslatef(-90, 30, -90);
			gl.glRotatef(-180, 0, 1, 0);
			gl.glScalef(30.0f, 30.0f, 30.0f);

			if(enableObstacleWireframes)
				 displayWireframeObject(gl, fortFaces, new float[] {0, 0, 0});
			else gl.glCallList(fortList + 2);
		}	
		gl.glPopMatrix();

		gl.glPushMatrix();
		{
			gl.glTranslatef(90, 30, -90);
			gl.glRotatef(-270, 0, 1, 0);
			gl.glScalef(30.0f, 30.0f, 30.0f);

			if(enableObstacleWireframes)
				 displayWireframeObject(gl, fortFaces, new float[] {0, 0, 0});
			else gl.glCallList(fortList + 3);
		}	
		gl.glPopMatrix();
		
		return System.nanoTime() - start;
	}
	
	public List<Bound> getBounds()
	{
		List<Bound> bounds = new ArrayList<Bound>();
		
		if(enableObstacles) bounds.addAll(wallBounds);
		else bounds.addAll(wallBounds.subList(0, 5));
		
		return bounds;
	}

	private long renderBounds(GL2 gl)
	{
		long start = System.nanoTime();
		
		gl.glDisable(GL_TEXTURE_2D);
		
		List<Bound> bounds = getBounds();
		
		if(enableClosestPoints)
			for(Bound bound : bounds)
				bound.displayClosestPtToPt(gl, glut, cars.get(0).getPosition());
		
		if(enableOBBSolids)
			for(OBB wall : wallBounds)
				wall.displaySolid(gl, glut, new float[] {0, 0.67f, 0.94f, 0.5f});
		
		
		if(enableOBBVertices)
		{
			for(Car car : cars)
			{
				if(car.colliding)
					 car.bound.displayVertices(gl, glut, new float[] {1, 0, 0, 1});
				else car.bound.displayVertices(gl, glut, new float[] {1, 1, 1, 1});
			}
		
			for(OBB wall : wallBounds)
				wall.displayVertices(gl, glut, new float[] {1, 1, 1, 1});
		}
		
		if(enableOBBWireframes)
		{
			for(Car car : cars)
			{
				if(car.colliding)
					 car.bound.displayWireframe(gl, glut, new float[] {1, 0, 0, 1});
				else car.bound.displayWireframe(gl, glut, new float[] {0, 0, 0, 1});
			}
			
			for(OBB wall : wallBounds)
				for(Car car : cars)
				{
					if(car.collisions != null && car.collisions.contains(wall))
					{
						 wall.displayWireframe(gl, glut, new float[] {1, 0, 0, 1});
						 break;
					}
					else wall.displayWireframe(gl, glut, new float[] {0, 0, 0, 1});
				}
		}
		
		if(enableOBBAxes)
		{
			for(Car car : cars) car.bound.displayAxes(gl, 10);
			
			for(OBB wall : wallBounds)
				wall.displayAxes(gl, 20);
		}
		
		for(Car car : cars)
			for(Item item : car.getItems())
				item.displayBoundVisuals(gl, glut, new float[] {0, 1, 0, 0.5f});
		
		for(Item item : itemList) item.displayBoundVisuals(gl, glut, new float[] {0, 1, 0, 0.5f});
		
		gl.glEnable(GL_TEXTURE_2D);
		
		return System.nanoTime() - start;
	}
	
	public HeightMap getHeightMap() { return heightMap; }

	private void setupLights(GL2 gl)
	{
	    gl.glLightfv(GL_LIGHT0, GL_SPECULAR, global_specular, 0);
	    gl.glLightfv(GL_LIGHT0, GL_POSITION, position, 0);
	    gl.glLightModelfv(GL_LIGHT_MODEL_AMBIENT, global_ambience, 0);
	        
	    gl.glMaterialfv(GL_FRONT, GL_AMBIENT, material_ambience, 0);
	    gl.glMaterialfv(GL_FRONT, GL_SHININESS, material_shininess, 0);
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
	
	public void printDataToFile() //TODO Complete file format
	{
		try
		{
			Calendar now = Calendar.getInstance();
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
			
			String _execution = new SimpleDateFormat("yyyyMMddHHmmss").format(execution.getTime());
			
			FileWriter writer = new FileWriter("output/" + _execution + ".txt");
			BufferedWriter out = new BufferedWriter(writer);
			
			out.write("Values recorded at: " + dateFormat.format(now.getTime()) + "\r\n\r\n ");
			
			for(int c = 0; c < renderTimes[0].length; c++)
				out.write(String.format("%11s", COLUMN_HEADERS[c]) + "\t");
			
			out.write("\r\n\r\n ");
			
			for(int i = 0; i < renderTimes.length; i++)
			{
				for(int j = 0; j < renderTimes[0].length; j++)
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
	
	public void spawnItemsInSphere(int itemID, int quantity, float[] c, float r)
	{
		spawnItemsInBound(itemID, quantity, new Sphere(c, r));
	}
	
	public void spawnItemsInOBB(int itemID, int quantity, float[] c, float[] u, float[] e)
	{
		spawnItemsInBound(itemID, quantity, new OBB(c, u, e, null));
	}
	
	public void spawnItemsInBound(int itemID, int quantity, Bound b)
	{
		Random random = new Random();
		
		for(int i = 0; i < quantity; i++)
		{
			switch(itemID)
			{
				case  0: itemList.add(new  GreenShell(this, b.randomPointInside(), random.nextInt(360))); break;
				case  2: itemList.add(new    RedShell(this, b.randomPointInside(), random.nextInt(360))); break;
				case  7: itemList.add(new FakeItemBox(this, b.randomPointInside(), random.nextInt(360))); break;
				case  8: itemList.add(new      Banana(this, b.randomPointInside())); break;
				case 13: itemList.add(new   BlueShell(this, b.randomPointInside())); break;
				
				default: break;
			}
		}
	}	
		
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
			case KeyEvent.VK_I:  spawnItemsInSphere(0, 10, new float[] {0, 100, 0}, 50); break;
			case KeyEvent.VK_U:  spawnItemsInOBB(8, 10, new float[] {0, 100, 0}, ORIGIN, new float[] {150, 50, 150}); break;
			
			case KeyEvent.VK_L: printDataToFile(); break;
			
			case KeyEvent.VK_DELETE: clearItems(); break;
	
			case KeyEvent.VK_P:	 playMusic(); break;
	 
			case KeyEvent.VK_F1: enableAnimation = !enableAnimation; break;
	
			case KeyEvent.VK_1:  enableOBBAxes       = !enableOBBAxes;       break;
			case KeyEvent.VK_2:  enableOBBVertices   = !enableOBBVertices;   break;
			case KeyEvent.VK_3:  enableOBBWireframes = !enableOBBWireframes; break;
			case KeyEvent.VK_4:	 enableOBBSolids     = !enableOBBSolids;     break;
			case KeyEvent.VK_5:	 enableClosestPoints = !enableClosestPoints; break;
			case KeyEvent.VK_F2: enableObstacleWireframes = !enableObstacleWireframes; break;
			
			case KeyEvent.VK_6:  Item.toggleBoundSolids();     break;
			case KeyEvent.VK_7:  Item.toggleBoundWireframes(); break;
			
			case KeyEvent.VK_8:  displayModels = !displayModels; break;
			
			case KeyEvent.VK_9:  enableBoundVisuals = !enableBoundVisuals; toggleBoundVisuals(); break;
			
			case KeyEvent.VK_0:  enableMotionBlur = !enableMotionBlur; break;
			
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

	public void mouseWheelMoved(MouseWheelEvent e) {}

	public void playMusic()
	{
		if(!musicPlaying)
		{
			new MP3(this, MUTE_CITY).start();
			musicPlaying = true;
		}
	}
	
	public void stopMusic() { musicPlaying = false; }
	
	public void toggleBoundVisuals()
	{
		enableOBBAxes          = enableBoundVisuals;
		enableOBBVertices      = enableBoundVisuals;
		enableOBBWireframes    = enableBoundVisuals;
		enableOBBSolids    	   = enableBoundVisuals;
		enableClosestPoints    = enableBoundVisuals;
		
		displayModels = !enableBoundVisuals;
	}

	public void actionPerformed(ActionEvent event)
	{
		console.parseCommand(consoleInput.getText());
	}
}
