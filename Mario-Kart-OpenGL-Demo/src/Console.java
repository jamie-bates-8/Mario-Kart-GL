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

		     if(_cmd.equalsIgnoreCase(   "add")) parseAdd(cmd);
		else if(_cmd.equalsIgnoreCase("delete")) parseDelete(cmd);
		else if(_cmd.equalsIgnoreCase("player")) parsePlayer(cmd);
		else if(_cmd.equalsIgnoreCase( "scene")) parseScene(cmd);

		cmd.close();
	}
	
	private void parseScene(Scanner cmd)
	{
		String _cmd = cmd.next();
		
		if(_cmd.equalsIgnoreCase("fog")) parseFog(cmd);
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

	private void parseAdd(Scanner cmd)
	{
		String _cmd = cmd.next();
		
		if(_cmd.equalsIgnoreCase("item")) parseAddItem(cmd);
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
		     if(_cmd.equalsIgnoreCase(    "wireframe")) player.enableWireframe = cmd.nextBoolean();
		else if(_cmd.equalsIgnoreCase("invertReverse")) player.invertReverse = !player.invertReverse;
		else if(_cmd.equalsIgnoreCase(      "gravity")) player.gravity = cmd.nextFloat();
		else if(_cmd.equalsIgnoreCase( "acceleration")) player.acceleration = cmd.nextFloat();
		else if(_cmd.equalsIgnoreCase(          "HUD")) parseHUD(cmd, player);
	}
	
	private void parseHUD(Scanner cmd, Car player)
	{
		String _cmd = cmd.next();
		
	     if(_cmd.equalsIgnoreCase("graph")) parseFrameTime(cmd, player);
	}
	
	private void parseFrameTime(Scanner cmd, Car player)
	{
		String _cmd = cmd.next();
		
	         if(_cmd.equalsIgnoreCase(    "cycle")) player.getHUD().cycleGraphMode();
	    else if(_cmd.equalsIgnoreCase("emphasize")) player.getHUD().emphasizedComponent = cmd.nextInt();
	}
}
