import graphics.util.Renderer;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/* TODO
 * 
 * Could possibly be improved by creating a data structure such as a parse tree
 * to parse the console commands more efficiently and reduce code by exploiting
 * the repetitious pattern
 */
public class Console
{
	Scene scene;

	public Console(Scene scene)
	{
		this.scene = scene;
	}

	public void parseCommand(String command)
	{
		//TODO deal with InputMismatchException and NoSuchElementException

		Scanner cmd = new Scanner(command);
		
		String _cmd = cmd.next();

		     if(_cmd.equalsIgnoreCase(      "add")) parseAdd(cmd);
		else if(_cmd.equalsIgnoreCase(   "delete")) parseDelete(cmd);
		else if(_cmd.equalsIgnoreCase(   "player")) parsePlayer(cmd);
		else if(_cmd.equalsIgnoreCase(    "scene")) parseScene(cmd);
		else if(_cmd.equalsIgnoreCase("collision")) parseCollision(cmd);
		else if(_cmd.equalsIgnoreCase(    "bound")) parseBound(cmd);
		else if(_cmd.equalsIgnoreCase(  "profile")) parseProfile(cmd);
		else if(_cmd.equalsIgnoreCase(    "press")) parsePress(cmd);
		else if(_cmd.equalsIgnoreCase(     "item")) parseItem(cmd);
		else if(_cmd.equalsIgnoreCase(  "texture")) parseTexture(cmd); 
		else if(_cmd.equalsIgnoreCase(  "terrain")) parseTerrain(cmd);     

		cmd.close();
	}
	
	private void parseTerrain(Scanner cmd)
	{
		String _cmd = cmd.next();
		Terrain map = scene.getHeightMap();
		
		     if(_cmd.equalsIgnoreCase( "generate")) parseGenerate(cmd);
		else if(_cmd.equalsIgnoreCase("wireframe")) map.enableWireframe = cmd.nextBoolean();
		else if(_cmd.equalsIgnoreCase("heightMap")) parseHeightMap(cmd);
		else if(_cmd.equalsIgnoreCase(  "foliage")) parseFoliage(cmd);
	}
	
	private void parseFoliage(Scanner cmd)
	{
		String _cmd = cmd.next();
		
		if(_cmd.equalsIgnoreCase("generate"))
		{
			int patches   = cmd.nextInt();
			float spread  = cmd.nextFloat();
			int patchSize = cmd.nextInt();
			
			scene.generateFoliage(patches, spread, patchSize);
		}
	}
	
	private void parseGenerate(Scanner cmd)
	{			
		scene.terrainCommand = cmd.nextLine();
	}
	
	private void parseTexture(Scanner cmd)
	{
		String _cmd = cmd.next();
		 
		     if(_cmd.equalsIgnoreCase(     "filter")) scene.linearFilter = cmd.nextBoolean();
		else if(_cmd.equalsIgnoreCase("anisotropic")) Renderer.anisotropic = cmd.nextBoolean(); //TODO seems to have no effect
	}
	
	private void parseBound(Scanner cmd)
	{
		String _cmd = cmd.next();
		
		if(_cmd.equalsIgnoreCase("smooth")) scene.smoothBound = cmd.nextBoolean();
	}
	
	private void parseItem(Scanner cmd)
	{
		String _cmd = cmd.next();
		
		if(_cmd.equalsIgnoreCase("smooth")) Item.smooth = cmd.nextBoolean();
	}
	
	private void parsePress(Scanner cmd)
	{
		scene.keyPressed(scene.pressKey(cmd.next().charAt(0)));
	}
	
	private void parseProfile(Scanner cmd)
	{
		String fileName = "profile/" + cmd.next() + ".txt";
		
		try
		{
			Scanner sc = new Scanner(new File(fileName));
			
			while(sc.hasNextLine()) parseCommand(sc.nextLine());
			
			sc.close();
		}
		catch (IOException e) { e.printStackTrace(); }
	}
	
	private void parseCollision(Scanner cmd)
	{
		String _cmd = cmd.next();
		
		if(_cmd.equalsIgnoreCase("obstacles")) scene.enableObstacles = cmd.nextBoolean();
	}
	
	private void parseScene(Scanner cmd)
	{
		String _cmd = cmd.next();
		
		     if(_cmd.equalsIgnoreCase(        "fog")) parseFog(cmd);
		else if(_cmd.equalsIgnoreCase(      "light")) parseLight(cmd);
		else if(_cmd.equalsIgnoreCase(    "display")) parseDisplay(cmd);
		else if(_cmd.equalsIgnoreCase(    "culling")) scene.enableCulling = cmd.nextBoolean();
		else if(_cmd.equalsIgnoreCase("multisample")) scene.multiSample = cmd.nextBoolean();    
		else if(_cmd.equalsIgnoreCase(        "fov")) scene.fov = cmd.nextFloat(); 
		else if(_cmd.equalsIgnoreCase( "reflection")) scene.enableReflection = cmd.nextBoolean(); 
		else if(_cmd.equalsIgnoreCase(    "opacity")) scene.opacity = cmd.nextFloat();
		else if(_cmd.equalsIgnoreCase(    "terrain"))
		{
			scene.enableTerrain = cmd.nextBoolean();
			Car player = scene.getCars().get(0); //TODO
			player.friction = 1;
		}
		else if(_cmd.equalsIgnoreCase( "background"))
		{
			float r = cmd.nextFloat();
			float g = cmd.nextFloat();
			float b = cmd.nextFloat();
			
			scene.background = new float[] {r, g, b};
		}
	}
	
	private void parseLight(Scanner cmd)
	{
		String _cmd = cmd.next();
		
		if(_cmd.equalsIgnoreCase("ambience"))
		{
			float r = cmd.nextFloat();
			float g = cmd.nextFloat();
			float b = cmd.nextFloat();
			
			float a = cmd.hasNextFloat() ? cmd.nextFloat() : 1.0f;
			
			scene.light.setAmbience(new float[] {r, g, b, a});
		}
		else if(_cmd.equalsIgnoreCase(   "smooth")) scene.light.smooth = cmd.nextBoolean();
		else if(_cmd.equalsIgnoreCase( "parallel")) scene.light.parallel = cmd.nextBoolean();
		else if(_cmd.equalsIgnoreCase("secondary")) scene.light.secondary = cmd.nextBoolean();
	}
	
	private void parseDisplay(Scanner cmd)
	{
		String _cmd = cmd.next();
		
		     if(_cmd.equalsIgnoreCase(  "fort")) scene.fort.displayModel = cmd.nextBoolean();
		else if(_cmd.equalsIgnoreCase("skybox")) scene.displaySkybox = cmd.nextBoolean();
	}
	
	private void parseFog(Scanner cmd)
	{
		String _cmd = cmd.next();
		
		if(_cmd.equalsIgnoreCase("color"))
		{
			float r = cmd.nextFloat();
			float g = cmd.nextFloat();
			float b = cmd.nextFloat();
			float a = cmd.nextFloat();
			
			/*
			 * TODO
			 * 
			 * Currently has no effect due to the fog color being updated
			 * on a per frame basis to support lightning bolts visuals
			 */
			scene.fogColor = new float[] {r, g, b, a};
		}
		else if(_cmd.equalsIgnoreCase("density"))
		{
			scene.fogDensity = cmd.nextFloat();
		}
	}

	private void parseHeightMap(Scanner cmd)
	{
		String _cmd = cmd.next();
		Terrain map = scene.getHeightMap();
		     
		     if(_cmd.equalsIgnoreCase( "export")) map.export();
		else if(_cmd.equalsIgnoreCase("display")) map.displayMap();
	}
	
	private void parseAdd(Scanner cmd)
	{
		String _cmd = cmd.next();
		
		     if(_cmd.equalsIgnoreCase("item")) parseAddItem(cmd);
		else if(_cmd.equalsIgnoreCase("box"))  parseAddBox(cmd);
	}
	
	private void parseAddBox(Scanner cmd)
	{
		float x = cmd.nextFloat();
		float y = cmd.nextFloat();
		float z = cmd.nextFloat();
		
		scene.addItemBox(x, y, z);
	}

	private void parseAddItem(Scanner cmd)
	{
		String _cmd = cmd.next();
		
		if(_cmd.equalsIgnoreCase("sphere"))
		{	
			int itemID = parseItemID(cmd.next());
			int quantity = cmd.nextInt();

			float x = cmd.nextFloat();
			float y = cmd.nextFloat();
			float z = cmd.nextFloat();

			float r = cmd.nextFloat();

			scene.spawnItemsInSphere(itemID, quantity, new float[] {x, y, z}, r);
		}
		else if(_cmd.equalsIgnoreCase("obb"))
		{
			int itemID = parseItemID(cmd.next());
			int quantity = cmd.nextInt();

			float c0 = cmd.nextFloat();
			float c1 = cmd.nextFloat();
			float c2 = cmd.nextFloat();

			float u0 = cmd.nextFloat();
			float u1 = cmd.nextFloat();
			float u2 = cmd.nextFloat();

			float e0 = cmd.nextFloat();
			float e1 = cmd.nextFloat();
			float e2 = cmd.nextFloat();

			float[] c = {c0, c1, c2};
			float[] u = {u0, u1, u2};
			float[] e = {e0, e1, e2};

			scene.spawnItemsInOBB(itemID, quantity, c, u, e);
		}
		else
		{
			int itemID = parseItemID(_cmd);
			
			float x = cmd.nextFloat();
			float y = cmd.nextFloat();
			float z = cmd.nextFloat();

			float t = cmd.nextFloat();
			
			scene.addItem(itemID, new float[] {x, y, z}, t);
		}
	}
	
	private int parseItemID(String itemID)
	{
		if(itemID.matches("\\d+")) return Integer.parseInt(itemID);
		
		else if(itemID.equalsIgnoreCase( "GreenShell")) return  0;
		else if(itemID.equalsIgnoreCase(   "RedShell")) return  2;
		else if(itemID.equalsIgnoreCase("FakeItemBox")) return  7;
		else if(itemID.equalsIgnoreCase(     "Banana")) return  8;
		else if(itemID.equalsIgnoreCase(  "BlueShell")) return 13;
		
		else return -1;	
	}
	
	private void parseDelete(Scanner cmd)
	{
		String _cmd = cmd.next();
		
		if(_cmd.equalsIgnoreCase("items")) scene.clearItems();
	}
	
	private void parsePlayer(Scanner cmd)
	{
		int playerID = cmd.nextInt();
		Car player = scene.getCars().get(playerID);
		
		String _cmd = cmd.next();
		
		//TODO nextBoolean() does not parse 1 and 0 as true and false respectively
		     if(_cmd.equalsIgnoreCase(   "renderMode")) player.renderMode = cmd.nextInt() % 3;
		else if(_cmd.equalsIgnoreCase("invertReverse")) player.invertReverse = !player.invertReverse;
		else if(_cmd.equalsIgnoreCase(      "gravity")) player.gravity = cmd.nextFloat();
		else if(_cmd.equalsIgnoreCase( "acceleration")) player.acceleration = cmd.nextFloat();
		else if(_cmd.equalsIgnoreCase(          "HUD")) parseHUD(cmd, player);
		else if(_cmd.equalsIgnoreCase(       "smooth")) player.smooth = cmd.nextBoolean();
		else if(_cmd.equalsIgnoreCase(     "position"))
		{
			float x = cmd.nextFloat();
			float y = cmd.nextFloat();
			float z = cmd.nextFloat();
			
			player.setPosition(new float[] {x, y, z});   
		}
	}
	
	private void parseHUD(Scanner cmd, Car player)
	{
		String _cmd = cmd.next();
		
	         if(_cmd.equalsIgnoreCase( "graph")) parseFrameTime(cmd, player);
	    else if(_cmd.equalsIgnoreCase("smooth")) player.getHUD().smooth = cmd.nextBoolean();
	}
	
	private void parseFrameTime(Scanner cmd, Car player)
	{
		String _cmd = cmd.next();
		
	         if(_cmd.equalsIgnoreCase(    "cycle")) player.getHUD().cycleGraphMode();
	    else if(_cmd.equalsIgnoreCase("emphasize")) player.getHUD().emphasizedComponent = cmd.nextInt();
	    else if(_cmd.equalsIgnoreCase(   "export"))
	    {
	    	if(cmd.hasNext()) scene.printDataToFile(cmd.next());
	    	else scene.printDataToFile(null);
	    }
	}
}
