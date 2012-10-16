/** Utility imports **/
import static graphics.util.Renderer.displayTexturedCuboid;
import static graphics.util.Renderer.displayTexturedObject;
import static graphics.util.Renderer.displayWildcardObject;
import static java.lang.Math.abs;
import static javax.media.opengl.GL.GL_BLEND;
import static javax.media.opengl.GL.GL_COLOR_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_TEST;
import static javax.media.opengl.GL.GL_DONT_CARE;
import static javax.media.opengl.GL.GL_FRONT;
import static javax.media.opengl.GL.GL_LEQUAL;
import static javax.media.opengl.GL.GL_NEAREST;
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
import static javax.media.opengl.GL2.GL_QUADS;
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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;
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
public class Scene implements GLEventListener, KeyListener, MouseWheelListener
{
	private int canvasWidth = 840;
	private int canvasHeight = 680;
	private static final int FPS = 60;
	private final FPSAnimator animator;
	
	private static final float FOV = 100.0f;
	
	private GLU glu;
	private GLUT glut;
	
	private boolean enableAnimation = true;
	
	private int frames = 0;
	private int frameRate = 0;
	private long startTime = System.currentTimeMillis();
	
	private Texture speedometer;
	
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
	
	private TextRenderer renderer;

	
	/** Model Fields **/
	private boolean displayModels = true;
	
	private List<Face> environmentFaces;
	private List<Face> fortFaces;
	
	private int environmentList;
	private int fortList;
	
	private List<Car> cars = new ArrayList<Car>();
	
	public static final float[] ORIGIN = {0.0f, 0.0f, 0.0f};

	private List<ItemBox> itemBoxes = new ArrayList<ItemBox>();
	
	
	/** Fog Fields **/
	private float fogDensity = 0.005f;
	private float[] fogColor = {1.0f, 1.0f, 1.0f, 1.0f};
	
	
	/** Lighting Fields **/
	private float[] global_specular = {1.0f, 1.0f, 1.0f};
	private float[] global_ambience = {0.8f, 0.8f, 0.8f, 1.0f};
	
	private float[] position = {0.0f, 50.0f, 0.0f, 0.0f};
	
    private float[] material_ambience  = {0.7f, 0.7f, 0.7f, 1.0f};
    private float[] material_shininess = {100.0f};
    
    private float cloud_density = 1.0f;
	
    
    /** Environment Fields **/
    private boolean enableSkybox = true;
	
	
	/** Collision Detection Fields **/	
	private boolean enableBoundVisuals     = true;
	private boolean enableOBBAxes          = false;
	private boolean enableOBBVertices      = false;
	private boolean enableOBBWireframes    = false;
	private boolean enableOBBSolids        = false;
	private boolean enableSphereWireframes = false;
	private boolean enableSphereSolids     = false;
	private boolean enableClosestPoints    = false;
	
	private boolean enableObstacles = true;
	
	private List<OBB> wallBounds;
	
	
	/** Music Fields **/
	private boolean musicPlaying = false;
	private static final String MUTE_CITY =
			"file:///" + System.getProperty("user.dir") + "//music//Mute City.mp3";
	
	
	private List<Particle> particles = new ArrayList<Particle>();

	
	private Queue<Integer> itemQueue = new ArrayBlockingQueue<Integer>(100);
	private List<Item> itemList = new ArrayList<Item>();
	
	
	int boostCounter = 0;
	
	
	public Scene()
	{
		Frame frame = new Frame();
		
		GLCanvas canvas = new GLCanvas();
		canvas.setPreferredSize(new Dimension(canvasWidth, canvasHeight));
		canvas.addGLEventListener(this);
		canvas.addKeyListener(this);
		canvas.addMouseWheelListener(this);
		canvas.setFocusable(true);
		canvas.requestFocus();
		
		frame.add(canvas);

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

		frame.pack();
		frame.setTitle("Mario Kart OpenGL Demo");
		frame.setVisible(true);
		
		animator.start();
	}
	
	public static void main(String[] args) { new Scene(); }
	
	public void init(GLAutoDrawable drawable)
	{	
		Font font = new Font("Calibri", Font.PLAIN, 18);
		renderer = new TextRenderer(font);
		
		try
		{
			speedometer   = TextureIO.newTexture(new File("tex/speedometer.png"), true);
			
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
		}
		catch (Exception e) { e.printStackTrace(); }
		
		GL2 gl = drawable.getGL().getGL2();
		glu = new GLU();
		glut = new GLUT();
		
		// may provide extra speed depending on machine
//		gl.setSwapInterval(0); 

		gl.glShadeModel(GL_SMOOTH);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

		/** Texture Options **/
		gl.glEnable(GL2.GL_TEXTURE_2D);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
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
		
		long startTime = System.currentTimeMillis();
		
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
	    
	    itemBoxes.addAll(ItemBox.generateDiamond( 56.25f, 30f, particles));
	    itemBoxes.addAll(ItemBox.generateSquare (101.25f, 60f, particles));
	    itemBoxes.addAll(ItemBox.generateSquare (123.75f, 30f, particles));
	    itemBoxes.addAll(ItemBox.generateDiamond(   180f,  0f, particles));
	    
	    wallBounds = BoundParser.parseOBBs("bound/blockFort.bound");
	   
	    long endTime = System.currentTimeMillis();
	    System.out.println(endTime - startTime);
	}
	
	public void display(GLAutoDrawable drawable)
	{		
		GL2 gl = drawable.getGL().getGL2();
		
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		gl.glLoadIdentity();
		gl.glMatrixMode(GL_MODELVIEW);
		
		if(cloud_density < 1) cloud_density += 0.0125f;
		
		float c = cloud_density;
		gl.glColor3f(c, c, c);
		gl.glFogfv (GL_FOG_COLOR, new float[] {c, c, c, 1}, 0);
		
		for(Car car : cars)
		{
			while(!car.getItemCommands().isEmpty())
			{
				int itemID = car.getItemCommands().poll();
				
				if(itemID == 10) cloud_density = 0.5f;
				else car.registerItem(gl, itemID);
			}
		}
		
		if(enableAnimation)
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
		
		int[] order = new int[cars.size()];
		int i = 0;
		
		for(int j = 0; j < cars.size(); j++)
		{
			order[j] = j; 
			
			if(cars.get(j).isBoosting())
			{
				int t = order[i];
				order[i] = j;
				order[j] = t;
				i++;
			}
		}
		
		int _i = i;
		
		for(int index : order)
		{
			Car car = cars.get(index);
			
			setupScene(gl, index);
			car.setupCamera(gl, glu);
			setupLights(gl);
			
			renderWorld(gl);
			if(displayModels) render3DModels(gl, car);
			
			long start = System.nanoTime();
			
			renderParticles(gl, car);
			Particle.resetTexture();
			
			long end = System.nanoTime();
			System.out.printf("Particles: %.3f ms" + "\n", (end - start) / 1E6);
			System.out.println("-------------------");
			
			if(car.isBoosting() && i == 1 && _i == boostCounter)
			{
				gl.glAccum(GL_MULT, 0.5f);
				gl.glAccum(GL_ACCUM, 0.5f);
		
				gl.glAccum(GL_RETURN, 1.0f);
			}
			else i--;
			
			renderBounds(gl);
			renderHUD(gl, car);
		}
		
		if(_i != boostCounter) gl.glAccum(GL_LOAD, 1.0f);
		boostCounter = _i;
		
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
	
	public void addParticle(Particle p) { particles.add(p); }
	
	public void addParticles(List<Particle> particles) { this.particles.addAll(particles); }
	
	private void updateItems()
	{
		List<Bound> bounds = getBounds();
		
		for(Item item : itemList)
		{
			item.update();
			item.update(cars);
		}
		
		for(Car car : cars)
			for(Item item : car.getItems())
			{
				item.hold();
				item.update(cars);
			}
		
		detectItemCollisions();
	}

	private void detectItemCollisions()
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
				
				if(a.getBound().testBound(b.getBound())) a.collide(b);
			}
		}
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
	
	public void renderParticles(GL2 gl, Car car)
	{
		for(Particle particle : particles)
			if(car.isSlipping()) particle.render(gl, car.slipTrajectory);
			else particle.render(gl, car.trajectory);
	}

	private void renderHUD(GL2 gl, Car car)
	{
		ortho2DBegin(gl);
		
		gl.glDisable(GL_LIGHTING);

		renderSpeedometer(gl, car);
		
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		
		ItemRoulette roulette = car.getRoulette();
		roulette.cursed = car.isCursed();
		if(roulette.isAlive()) roulette.render(gl);
		
		renderText(car);
		
		gl.glEnable(GL_LIGHTING);
		
		ortho2DEnd(gl);
	}

	private void renderSpeedometer(GL2 gl, Car car)
	{
		gl.glEnable(GL_TEXTURE_2D);
		gl.glEnable(GL_BLEND);
		
		gl.glColor3f(1, 1, 1);

		speedometer.bind(gl);
		
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
		
		gl.glDisable(GL_TEXTURE_2D);
		
		gl.glPushMatrix();
		{
			gl.glTranslatef(canvasWidth - 150, canvasHeight - 100, 0);
			gl.glRotatef(zRotation_Meter, 0.0f, 0.0f, 1.0f);

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
	}

	private void renderText(Car car)
	{
		renderer.beginRendering(canvasWidth, canvasHeight);
		renderer.setSmoothing(true);
		
		renderer.draw("Distance: " + (int) car.distance + " m", 40, 40);
		renderer.draw("FPS: " + frameRate, 40, 80);
		
		float[] p = car.getPosition();
		
		renderer.draw("x: " + String.format("%.2f", p[0]), 40, 200);
		renderer.draw("y: " + String.format("%.2f", p[1]), 40, 160);
		renderer.draw("z: " + String.format("%.2f", p[2]), 40, 120);
		
		renderer.draw("Colliding: " + car.colliding,    40, 240);
		renderer.draw("Falling: "   + car.falling,      40, 280);
		renderer.draw("Items: "     + itemList.size(),  40, 320);
		renderer.draw("Particle: "  + particles.size(), 40, 360);
		renderer.draw("Velocity: "  + String.format("%.2f", car.velocity), 40, 400);
		renderer.draw("Turn Rate: " + String.format("%.2f", car.turnRate), 40, 440);
		
		renderer.endRendering();
	}

	private void render3DModels(GL2 gl, Car car)
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
		
		long end = System.nanoTime();
		System.out.printf("Vehicle: %.3f ms" + "\n", (end - start) / 1E6);

		for(ItemBox box : itemBoxes)
			if(!box.isDead()) box.render(gl, car.trajectory);

		start = System.nanoTime();
		
		for(Item item : itemList)
			if(!item.isDead()) item.render(gl, car.trajectory);
		
		end = System.nanoTime();
		System.out.printf("World Items: %.3f ms" + "\n", (end - start) / 1E6);
	}

	private void renderWorld(GL2 gl)
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
		
		long end = System.nanoTime();
//		System.out.printf("World: %.3f ms" + "\n", (end - start) / 1E6);
	}

	private void renderObstacles(GL2 gl)
	{
		gl.glPushMatrix();
		{
			gl.glTranslatef(90, 30, 90);
			gl.glScalef(30.0f, 30.0f, 30.0f);

			gl.glCallList(fortList);
		}	
		gl.glPopMatrix();

		gl.glPushMatrix();
		{
			gl.glTranslatef(-90, 30, 90);
			gl.glRotatef(-90, 0, 1, 0);
			gl.glScalef(30.0f, 30.0f, 30.0f);

			gl.glCallList(fortList + 1);

		}	
		gl.glPopMatrix();

		gl.glPushMatrix();
		{
			gl.glTranslatef(-90, 30, -90);
			gl.glRotatef(-180, 0, 1, 0);
			gl.glScalef(30.0f, 30.0f, 30.0f);

			gl.glCallList(fortList + 2);
		}	
		gl.glPopMatrix();

		gl.glPushMatrix();
		{
			gl.glTranslatef(90, 30, -90);
			gl.glRotatef(-270, 0, 1, 0);
			gl.glScalef(30.0f, 30.0f, 30.0f);

			gl.glCallList(fortList + 3);
		}	
		gl.glPopMatrix();
	}
	
	public List<Bound> getBounds()
	{
		List<Bound> bounds = new ArrayList<Bound>();
		
		if(enableObstacles) bounds.addAll(wallBounds);
		else bounds.addAll(wallBounds.subList(0, 5));
		
		return bounds;
	}

	private void renderBounds(GL2 gl)
	{
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
				item.displayBoundVisuals(gl, glut, new float[] {0, 1, 0, 1});
		
		for(Item item : itemList) item.displayBoundVisuals(gl, glut, new float[] {0, 1, 0, 1});
		
		gl.glEnable(GL_TEXTURE_2D);
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
		glu.gluPerspective(FOV, ratio, 2.0, 700.0);
		
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glEnable(GL_DEPTH_TEST);
	}
	
	private void setupScene(GL2 gl, int playerID)
	{
		float ratio = (float) canvasWidth / (float) canvasHeight;
		
		if(cars.size() < 2) gl.glViewport(0, 0, canvasWidth, canvasHeight);
		else
		{
			     if(playerID == 0) gl.glViewport(0, canvasHeight / 2, canvasWidth / 2, canvasHeight / 2);
			else if(playerID == 1) gl.glViewport(0, 0, canvasWidth / 2, canvasHeight / 2);
		}
	}
		
	public void keyPressed(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_ESCAPE: System.exit(0); break; //Close the application
	
			case KeyEvent.VK_H:     enableObstacles = !enableObstacles; break;
	
			case KeyEvent.VK_P:		 playMusic(); break;
	
			case KeyEvent.VK_F1:     enableAnimation = !enableAnimation; break;
	
			case KeyEvent.VK_F2:     Item.toggleBoundSolids();     break;
			case KeyEvent.VK_F3:     Item.toggleBoundWireframes(); break;
	
			case KeyEvent.VK_1:		 enableOBBAxes          = !enableOBBAxes;          break;
			case KeyEvent.VK_2:		 enableOBBVertices      = !enableOBBVertices;      break;
			case KeyEvent.VK_3:      enableOBBWireframes    = !enableOBBWireframes;    break;
			case KeyEvent.VK_4:		 enableOBBSolids        = !enableOBBSolids;        break;
			case KeyEvent.VK_5:		 enableSphereSolids     = !enableSphereSolids;     break;
			case KeyEvent.VK_6:      enableSphereWireframes = !enableSphereWireframes; break;
			case KeyEvent.VK_7:		 enableClosestPoints    = !enableClosestPoints;    break;
			case KeyEvent.VK_8:		 displayModels          = !displayModels;          break;
			
			case KeyEvent.VK_9:      for(Car car : cars) car.displayModel = false;     break;
			
			case KeyEvent.VK_0:		 enableBoundVisuals     = !enableBoundVisuals; toggleBoundVisuals(); break;
	
			default: for(Car car : cars) car.keyPressed(e); break; //TODO
		}
	}
	
	public void keyReleased(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			default: for(Car car : cars) car.keyReleased(e); break; //TODO
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
		enableSphereSolids 	   = enableBoundVisuals;
		enableSphereWireframes = enableBoundVisuals;
		enableClosestPoints    = enableBoundVisuals;
	}
}
