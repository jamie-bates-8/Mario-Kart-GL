package bates.jamie.graphics.io;


import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.entity.GrassPatch;
import bates.jamie.graphics.entity.Quadtree;
import bates.jamie.graphics.entity.Terrain;
import bates.jamie.graphics.item.Banana;
import bates.jamie.graphics.item.BlueShell;
import bates.jamie.graphics.item.FakeItemBox;
import bates.jamie.graphics.item.GreenShell;
import bates.jamie.graphics.item.Item;
import bates.jamie.graphics.item.RedShell;
import bates.jamie.graphics.particle.Blizzard;
import bates.jamie.graphics.particle.Blizzard.StormType;
import bates.jamie.graphics.particle.ParticleGenerator;
import bates.jamie.graphics.scene.Light;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.util.Vec3;

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
		
		try
		{
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
			else if(_cmd.equalsIgnoreCase( "particle")) parseParticle(cmd);
			else if(_cmd.equalsIgnoreCase( "quadtree")) parseQuadtree(cmd);
			else if(_cmd.equalsIgnoreCase(   "export")) parseExport(cmd);
			else if(_cmd.equalsIgnoreCase(  "weather")) parseWeather(cmd);
			else if(_cmd.equalsIgnoreCase(   "shadow")) parseShadow(cmd);
			else if(_cmd.equalsIgnoreCase(    "grass")) parseGrass(cmd); 
			else if(_cmd.equalsIgnoreCase(   "effect")) parseEffect(cmd);
			else if(_cmd.equalsIgnoreCase(    "water")) parseWater(cmd); 
		}
		catch(Exception e)
		{
			scene.getCars().get(0).getHUD().broadcast("Invalid Command");
		}

		cmd.close();
	}
	
	private void parseWater(Scanner cmd)
	{
		String _cmd = cmd.next();
		
	         if(_cmd.equalsIgnoreCase("magma")) scene.water.magma = cmd.nextBoolean();
	}
	
	private void parseEffect(Scanner cmd)
	{
		String _cmd = cmd.next();
		
	         if(_cmd.equalsIgnoreCase("bloom")) scene.enableBloom = cmd.nextBoolean();
	    else if(_cmd.equalsIgnoreCase("focalblur")) Scene.enableFocalBlur = cmd.nextBoolean();
	    else if(_cmd.equalsIgnoreCase("radialblur")) parseRadial(cmd);    
	    else if(_cmd.equalsIgnoreCase("mirage"))
	    {
	    	boolean enabled = cmd.nextBoolean();
	    	if(enabled) Scene.enableFocalBlur = true;
	    	scene.focalBlur.enableMirage = enabled;
	    }
	}
	
	private void parseRadial(Scanner cmd)
	{
		String _cmd = cmd.next();
		
	         if(_cmd.equalsIgnoreCase(   "decay")) scene.focalBlur.decay    = cmd.nextFloat();
	    else if(_cmd.equalsIgnoreCase("exposure")) scene.focalBlur.exposure = cmd.nextFloat();
	    else if(_cmd.equalsIgnoreCase( "density")) scene.focalBlur.density  = cmd.nextFloat();
	    else if(_cmd.equalsIgnoreCase(  "weight")) scene.focalBlur.weight   = cmd.nextFloat();
	         
	    else if(_cmd.equalsIgnoreCase( "samples")) scene.focalBlur.samples = cmd.nextInt();
	}
	
	private void parseGrass(Scanner cmd)
	{
		String _cmd = cmd.next();
		GrassPatch patch = scene.grassPatches[0];
		
		if(_cmd.equalsIgnoreCase("spread"))
		{
			patch.setSpread(cmd.nextFloat());
			patch.update = true;
		}
		else if(_cmd.equalsIgnoreCase("origin"))
		{
			float x = cmd.nextFloat();
			float y = cmd.nextFloat();
			float z = cmd.nextFloat();
			
			patch.setOrigin(new float[] {x, y, z});
			patch.update = true;
		}
	}
	
	private void parseExport(Scanner cmd)
	{
		String _cmd = cmd.next();
		
		     if(_cmd.equalsIgnoreCase("update"))
		     {
		    	 if(cmd.hasNext()) scene.printDataToFile(cmd.next(), Scene.UPDATE_HEADERS, scene.updateTimes);
		    	 else  scene.printDataToFile(cmd.next(), Scene.UPDATE_HEADERS, scene.updateTimes);
		     }
		else if(_cmd.equalsIgnoreCase("render"))
		     {
		    	 if(cmd.hasNext()) scene.printDataToFile(cmd.next(), Scene.RENDER_HEADERS, scene.renderTimes);
		    	 else  scene.printDataToFile(cmd.next(), Scene.RENDER_HEADERS, scene.renderTimes);
		     }
	}
	
	private void parseQuadtree(Scanner cmd)
	{
		String _cmd = cmd.next();
		Quadtree tree = scene.getTerrain().tree;
		
		     if(_cmd.equalsIgnoreCase( "wireframe")) parseWireframe(cmd);
		else if(_cmd.equalsIgnoreCase( "subdivide")) tree.subdivideAll();
		else if(_cmd.equalsIgnoreCase(  "decimate")) tree.decimateAll(); 
		else if(_cmd.equalsIgnoreCase(       "lod")) tree.detail = cmd.nextInt();
		else if(_cmd.equalsIgnoreCase(   "texture")) tree.scaleTexture(cmd.nextFloat());
		else if(_cmd.equalsIgnoreCase("elasticity")) tree.elasticity = cmd.nextFloat();
		else if(_cmd.equalsIgnoreCase(  "caustics")) tree.enableCaustic = cmd.nextBoolean();    
		else if(_cmd.equalsIgnoreCase( "translate"))
		{
			float x = cmd.nextFloat();
			float y = cmd.nextFloat();
			float z = cmd.nextFloat();
			
			tree.translate(new float[] {x, y, z});
		}
	}
	
	private void parseWeather(Scanner cmd)
	{
		String _cmd = cmd.next();
		
		if(_cmd.equalsIgnoreCase("limit"))
		{
			int limit = cmd.nextInt();
			scene.flakeLimit = limit;
			if(scene.blizzard != null) scene.blizzard.setLimit(limit);
		}
		else if(_cmd.equalsIgnoreCase("none")) scene.enableBlizzard = false;
		else if(_cmd.equalsIgnoreCase("snow"))
		{	
			scene.enableBlizzard = true;
			scene.blizzard = new Blizzard(scene, scene.flakeLimit, new Vec3(0.2f, -1.5f, 0.1f), StormType.SNOW);
		}
		else if(_cmd.equalsIgnoreCase("rain"))
		{	
			scene.enableBlizzard = true;
			scene.blizzard = new Blizzard(scene, scene.flakeLimit, new Vec3(0.0f, -4.0f, 0.0f), StormType.RAIN);
		}
	}
	
	private void parseShadow(Scanner cmd)
	{
		String _cmd = cmd.next();
		
		if(_cmd.equalsIgnoreCase("offset")) scene.caster.shadowOffset = cmd.nextFloat();
		else if(_cmd.equalsIgnoreCase("enabled")) Scene.enableShadow = cmd.nextBoolean();
	}
	
	private void parseWireframe(Scanner cmd)
	{
		String _cmd = cmd.next();
		Quadtree tree = scene.getTerrain().tree;
		
		if(_cmd.equalsIgnoreCase("color"))
		{
			float r = cmd.nextFloat();
			float g = cmd.nextFloat();
			float b = cmd.nextFloat();
			
			tree.lineColor = new float[] {r, g, b};
		}
	}
	
	private void parseParticle(Scanner cmd)
	{
		String _cmd = cmd.next();
		
		     if(_cmd.equalsIgnoreCase("generator")) parseGenerator(cmd);
		else if(_cmd.equalsIgnoreCase("quadratic")) scene.quadratic = new float[] {cmd.nextFloat(), cmd.nextFloat(), cmd.nextFloat()};
	}
	
	private void parseGenerator(Scanner cmd)
	{
		ParticleGenerator generator = scene.generators.get(cmd.nextInt());
		
		String _cmd = cmd.next();
		
		     if(_cmd.equalsIgnoreCase("quantity")) generator.setQuantity(cmd.nextInt());
		else if(_cmd.equalsIgnoreCase(   "pulse")) generator.setPulse(cmd.nextInt());
	}
	
	private void parseTerrain(Scanner cmd)
	{
		String _cmd = cmd.next();
		Terrain map = scene.getTerrain();
		
		     if(_cmd.equalsIgnoreCase( "generate")) parseGenerate(cmd);
		else if(_cmd.equalsIgnoreCase(   "render")) map.renderMode = cmd.nextInt();
		else if(_cmd.equalsIgnoreCase("heightMap")) parseHeightMap(cmd);
		else if(_cmd.equalsIgnoreCase(  "foliage")) parseFoliage(cmd);
		else if(_cmd.equalsIgnoreCase(  "texture")) map.scaleTexCoords(cmd.nextInt());
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
		else if(_cmd.equalsIgnoreCase( "boxes")) scene.enableItemBoxes = cmd.nextBoolean();
	}
	
	private void parsePress(Scanner cmd)
	{
		String _cmd = cmd.next();
		
		int modifier = 0;
		
		if(_cmd.equalsIgnoreCase("shift")) modifier = KeyEvent.SHIFT_DOWN_MASK;
		
		scene.keyPressed(scene.pressKey(_cmd.charAt(0), modifier));
	}
	
	private void parseProfile(Scanner cmd)
	{
		String fileName = "profile/" + cmd.next() + ".txt";
		
		try
		{
			Scanner sc = new Scanner(new File(fileName));
			
			while(sc.hasNextLine()) parseCommand(sc.nextLine());
			
			sc.close();
			
			scene.setCheckBoxes();
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
		else if(_cmd.equalsIgnoreCase(     "lights")) parseLights(cmd);
		else if(_cmd.equalsIgnoreCase(    "display")) parseDisplay(cmd);
		else if(_cmd.equalsIgnoreCase(    "culling")) scene.enableCulling = cmd.nextBoolean();   
		else if(_cmd.equalsIgnoreCase(        "fov")) scene.fov = cmd.nextFloat();
		else if(_cmd.equalsIgnoreCase(    "opacity")) scene.opacity = cmd.nextFloat();
		else if(_cmd.equalsIgnoreCase(  "reflector")) parseReflector(cmd);    
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
		else if(_cmd.equalsIgnoreCase("sky"))
		{
			float r = cmd.nextFloat();
			float g = cmd.nextFloat();
			float b = cmd.nextFloat();
			
			scene.skybox.setSkyColor(new float[] {r, g, b});
		}
		else if(_cmd.equalsIgnoreCase("horizon"))
		{
			float r = cmd.nextFloat();
			float g = cmd.nextFloat();
			float b = cmd.nextFloat();
			
			scene.skybox.setHorizonColor(new float[] {r, g, b});
		} 
	}
	
	private void parseReflector(Scanner cmd)
	{
		String _cmd = cmd.next();
		
		     if(_cmd.equalsIgnoreCase("reflectivity")) scene.reflector.reflectivity = cmd.nextFloat();
		else if(_cmd.equalsIgnoreCase(         "eta")) scene.reflector.setRefractionIndex(cmd.nextFloat());
		else if(_cmd.equalsIgnoreCase(  "resolution")) scene.reflector.updateSize(cmd.nextInt());
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
		else if(_cmd.equalsIgnoreCase("emission"))
		{
			float r = cmd.nextFloat();
			float g = cmd.nextFloat();
			float b = cmd.nextFloat();
			
			float a = cmd.hasNextFloat() ? cmd.nextFloat() : 1.0f;
			
			scene.light.setEmission(new float[] {r, g, b, a});
		}
		else if(_cmd.equalsIgnoreCase("rim_color"))
		{
			float r = cmd.nextFloat();
			float g = cmd.nextFloat();
			float b = cmd.nextFloat();
			
			Light.rimColor = new float[] {r, g, b};
		}
		else if(_cmd.equalsIgnoreCase("rim_power")) Light.rimPower = cmd.nextFloat();
		
		else if(_cmd.equalsIgnoreCase("shininess")) scene.light.setShininess(cmd.nextInt());
		else if(_cmd.equalsIgnoreCase( "parallel")) scene.light.parallel = cmd.nextBoolean();
		
		else if(_cmd.equalsIgnoreCase( "constant")) scene.light.setConstantAttenuation (cmd.nextFloat());
		else if(_cmd.equalsIgnoreCase(   "linear")) scene.light.setLinearAttenuation   (cmd.nextFloat());
		else if(_cmd.equalsIgnoreCase("quadratic")) scene.light.setQuadraticAttenuation(cmd.nextFloat());
		
	}
	
	private void parseLights(Scanner cmd)
	{
		String _cmd = cmd.next();
		
		if(_cmd.equalsIgnoreCase("ambience"))
		{
			float r = cmd.nextFloat();
			float g = cmd.nextFloat();
			float b = cmd.nextFloat();
			
			float a = cmd.hasNextFloat() ? cmd.nextFloat() : 1.0f;
			
			for(Light l : scene.lights) l.setAmbience(new float[] {r, g, b, a});
		}
		else if(_cmd.equalsIgnoreCase("diffuse"))
		{
			float r = cmd.nextFloat();
			float g = cmd.nextFloat();
			float b = cmd.nextFloat();
			
			float a = cmd.hasNextFloat() ? cmd.nextFloat() : 1.0f;
			
			for(Light l : scene.lights) l.setDiffuse(new float[] {r, g, b, a});
		}
		else if(_cmd.equalsIgnoreCase("specular"))
		{
			float r = cmd.nextFloat();
			float g = cmd.nextFloat();
			float b = cmd.nextFloat();
			
			float a = cmd.hasNextFloat() ? cmd.nextFloat() : 1.0f;
			
			for(Light l : scene.lights) l.setSpecular(new float[] {r, g, b, a});
		}
		
		else if(_cmd.equalsIgnoreCase( "constant")) { float c = cmd.nextFloat(); for(Light light : scene.lights) light.setConstantAttenuation (c); }
		else if(_cmd.equalsIgnoreCase(   "linear")) { float l = cmd.nextFloat(); for(Light light : scene.lights) light.setLinearAttenuation   (l); }
		else if(_cmd.equalsIgnoreCase("quadratic")) { float q = cmd.nextFloat(); for(Light light : scene.lights) light.setQuadraticAttenuation(q); }
		else if(_cmd.equalsIgnoreCase("attenuate")) { boolean attenuate = cmd.nextBoolean(); for(Light light : scene.lights) light.enableAttenuation = attenuate; }
	}
	
	private void parseDisplay(Scanner cmd)
	{
		String _cmd = cmd.next();
		
		     if(_cmd.equalsIgnoreCase(   "fort")) scene.fort.displayModel = cmd.nextBoolean();
		else if(_cmd.equalsIgnoreCase( "skybox")) scene.displaySkybox = cmd.nextBoolean();
		else if(_cmd.equalsIgnoreCase("foliage")) scene.enableFoliage = cmd.nextBoolean(); 
		else if(_cmd.equalsIgnoreCase(  "water")) scene.getTerrain().enableWater = cmd.nextBoolean();     
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
		Terrain map = scene.getTerrain();
		     
		     if(_cmd.equalsIgnoreCase( "export")) map.export();
		else if(_cmd.equalsIgnoreCase("display")) map.displayMap();
	}
	
	private void parseAdd(Scanner cmd)
	{
		String _cmd = cmd.next();
		
		     if(_cmd.equalsIgnoreCase("item")) parseAddItem(cmd);
		else if(_cmd.equalsIgnoreCase( "box")) parseAddBox (cmd);
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

			scene.spawnItemsInSphere(itemID, quantity, new Vec3(x, y, z), r);
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
			
			scene.addItem(itemID, new Vec3(x, y, z), t);
		}
	}
	
	private int parseItemID(String itemID)
	{
		if(itemID.matches("\\d+")) return Integer.parseInt(itemID);
		
		else if(itemID.equalsIgnoreCase( "GreenShell")) return GreenShell.ID;
		else if(itemID.equalsIgnoreCase(   "RedShell")) return RedShell.ID;
		else if(itemID.equalsIgnoreCase("FakeItemBox")) return FakeItemBox.ID;
		else if(itemID.equalsIgnoreCase(     "Banana")) return Banana.ID;
		else if(itemID.equalsIgnoreCase(  "BlueShell")) return BlueShell.ID;
		
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
		     if(_cmd.equalsIgnoreCase(       "render")) player.renderMode = cmd.nextInt() % 3;
		else if(_cmd.equalsIgnoreCase("invertReverse")) player.invertReverse = !player.invertReverse;
		else if(_cmd.equalsIgnoreCase(      "gravity")) player.gravity = cmd.nextFloat();
		else if(_cmd.equalsIgnoreCase( "acceleration")) player.acceleration = cmd.nextFloat();
		else if(_cmd.equalsIgnoreCase(          "HUD")) parseHUD(cmd, player);
		else if(_cmd.equalsIgnoreCase(       "smooth")) player.smooth = cmd.nextBoolean();
		else if(_cmd.equalsIgnoreCase(      "opacity")) player.opacity = cmd.nextFloat();    
		else if(_cmd.equalsIgnoreCase(     "position"))
		{
			float x = cmd.nextFloat();
			float y = cmd.nextFloat();
			float z = cmd.nextFloat();
			
			player.setPosition(new Vec3(x, y, z));   
		}
	}
	
	private void parseHUD(Scanner cmd, Car player)
	{
		String _cmd = cmd.next();
		
	         if(_cmd.equalsIgnoreCase( "graph")) parseFrameTime(cmd, player);
	    else if(_cmd.equalsIgnoreCase("smooth")) player.getHUD().smooth = cmd.nextBoolean();
	    else if(_cmd.equalsIgnoreCase( "color"))
	    {
	    	int r = cmd.nextInt();
	    	int g = cmd.nextInt();
	    	int b = cmd.nextInt();
	    	
	    	player.getHUD().setTextColor(new Color(r, g, b));
	    }
	}
	
	private void parseFrameTime(Scanner cmd, Car player)
	{
		String _cmd = cmd.next();
		
	         if(_cmd.equalsIgnoreCase(    "cycle")) player.getHUD().cycleGraphMode();
	    else if(_cmd.equalsIgnoreCase("emphasize")) player.getHUD().emphasizedComponent = cmd.nextInt();
	}
}
