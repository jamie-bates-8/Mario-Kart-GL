package bates.jamie.graphics.io;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Rumbler;
import net.java.games.input.Version;


public class GamePad 
{
	private static final int DELAY = 40; //milliseconds
	private static final float DEAD_ZONE = 0.25f;
	
	private static int controllerID = 0;
	
	private Controller controller;
	private Component[] components;
	private int xAxis, yAxis, zAxis, xRotation, yRotation;
	private float z = 0.0f;
	private float rx = 0.0f;
	
	private static final int NUMBER_OF_BUTTONS = 10;
	private int[] buttons;
	private boolean[] buttonState;
	
	private Queue<Integer> pressEvents   = new ArrayBlockingQueue<Integer>(100);
	private Queue<Integer> releaseEvents = new ArrayBlockingQueue<Integer>(100);
	
	private boolean enabled = true;
	
	public static void main(String[] args)
	{
		System.out.println("JInput Version: " + Version.getVersion());
		
		ControllerEnvironment ce =
				ControllerEnvironment.getDefaultEnvironment();

		Controller[] cs = ce.getControllers();
		
		listControllers();
		
		printDetails(cs[2]);
		
		pollComponent(cs[2], cs[2].getComponents()[3]);
	}
	
	public GamePad()
	{
		ControllerEnvironment ce =
				ControllerEnvironment.getDefaultEnvironment();

		Controller[] controllers = ce.getControllers();
		
		if(controllers.length == 0)
		{
			System.out.println("No Controllers Found");
			controller = null;
		}
		else
		{
			controller = findGamePad(controllers);
			if(controller != null) findComponentIndices(controller);
		}
	}
	
	public static int numberOfGamepads()
	{
		ControllerEnvironment ce =
				ControllerEnvironment.getDefaultEnvironment();

		Controller[] controllers = ce.getControllers();
		
		int gamepads = 0;
		
		for(Controller controller : controllers)
		{
			Controller.Type type = controller.getType();
		
			if(type == Controller.Type.GAMEPAD ||
		       type == Controller.Type.STICK     ) gamepads++;
		}
		
		return gamepads;
	}
	
	public boolean isNull() { return controller == null; }
	
	public void update()
	{
		poll();
		
		updateButtons();
		updateZAxis();
		updateXRotation();
	}
	
	public void enable()  { enabled = true;  }
	
	public void disable() { enabled = false; }
	
	public Queue<Integer> getPressEvents() { return pressEvents; }
	
	public Queue<Integer> getReleaseEvents() { return releaseEvents; }
	
	public void updateButtons()
	{
		boolean value;
		
		for(int i = 0; i < buttonState.length; i++)
		{
			value = (components[buttons[i]].getPollData() != 0.0f);
			
			if(buttonState[i] != value)
			{
				if(value == false) releaseEvents.add(buttons[i]);
				else pressEvents.add(buttons[i]);
				
				enable();
			}
			
			buttonState[i] = value;
		}
	}
	
	public void updateZAxis()
	{
		float value = getZAxis();
		
		if     (value  > 0 && z == 0)   pressEvents.add( zAxis);
		else if(value  < 0 && z == 0)   pressEvents.add(-zAxis);
		else if(value == 0 && z != 0) releaseEvents.add( zAxis);
		
		z = value;
	}
	
	public void updateXRotation()
	{
		float value = getXRotation();
		
		     if(value > 0 && rx == 0) pressEvents.add( xRotation);
		else if(value < 0 && rx == 0) pressEvents.add(-xRotation);
		     
		rx = value;
	}
	
	public boolean isEnabled() { return controller != null && enabled; }
	
	public void poll()
	{
		if(!controller.poll())
		{
			System.out.println("Controller invalid");
			disable();
		}
	}
	
	public float getXAxis()
	{
		if(xAxis == -1) return 0;
		else
		{
			float x = components[xAxis].getPollData();

			return (Math.abs(x) > DEAD_ZONE) ? -x : 0; 
		}
	}
	
	public float getYAxis()
	{
		if(yAxis == -1) return 0;
		else
		{
			float y = components[yAxis].getPollData();

			return (Math.abs(y) > DEAD_ZONE) ? -y : 0; 
		}
	}
	
	public float getZAxis()
	{
		if(zAxis == -1) return 0;
		else
		{
			float z = components[zAxis].getPollData();
			
			return (Math.abs(z) > DEAD_ZONE) ? z : 0; 
		}
	}
	
	public float getXRotation()
	{
		if(xRotation == -1) return 0;
		else
		{
			float x = components[xRotation].getPollData();
			
			return (Math.abs(x) > DEAD_ZONE) ? x : 0; 
		}
	}
	
	public float getYRotation()
	{
		if(yRotation == -1) return 0;
		else
		{
			float y = components[yRotation].getPollData();
			
			return (Math.abs(y) > DEAD_ZONE) ? y : 0; 
		}
	}
	
	private void findComponentIndices(Controller controller)
	{
		components = controller.getComponents();
		
		if(components.length == 0) System.exit(0);
		else
		{
			xAxis = findComponentIndex(components, Component.Identifier.Axis.X);
			yAxis = findComponentIndex(components, Component.Identifier.Axis.Y);
			zAxis = findComponentIndex(components, Component.Identifier.Axis.Z);
			
			xRotation = findComponentIndex(components, Component.Identifier.Axis.RX);
			yRotation = findComponentIndex(components, Component.Identifier.Axis.RY);
			
			findButtons(components);
		}
	}
	
	private void findButtons(Component[] components)
	{
		buttons = new int[NUMBER_OF_BUTTONS];
		int buttonCount = 0;
		Component c;
		
		Arrays.fill(buttons, -1);
		
		for (int i = 0; i < components.length; i++)
		{
			c = components[i];
			
			if(isButton(c))
			{
				if(buttonCount != NUMBER_OF_BUTTONS)
				{
					buttons[buttonCount] = i;
					buttonCount++;
				}
			}
		}
		
		buttonState = new boolean[buttonCount];
	}
	
	private boolean isButton(Component c)
	{
		if(!c.isAnalog() && !c.isRelative())
		{
			String className = c.getIdentifier().getClass().getName();
			if(className.endsWith("Button")) return true;
		}
		return false;
	}
	
	private int findComponentIndex(Component[] components, Component.Identifier id)
	{
		Component c;
		
		for (int i = 0; i < components.length; i++)
		{
			c = components[i];
			
			if(c.getIdentifier() == id && !c.isRelative()) return i;
		}
		
		return -1;
	}
	
	private Controller findGamePad(Controller[] controllers)
	{
		Controller.Type type;
		int index = 0;
		int counter = controllerID;
		
		while(index < controllers.length)
		{
			type = controllers[index].getType();
			
			if(type == Controller.Type.GAMEPAD ||
			   type == Controller.Type.STICK     )
			{
				if(counter == 0) break;
				else counter--;
			}
			
			index++;
		}
		
		if(index == controllers.length)
		{
			System.out.println("No Game Pad Found");
			return null;
		}
		else controllerID++;
		
		return controllers[index];
	}
	
	private static void pollComponent(Controller ctrl, Component comp)
	{
		float previous = 0.0f;
		float current;
		
		int i = 1;
		
		while(true)
		{
			try { Thread.sleep(DELAY); }
			catch (InterruptedException e) { e.printStackTrace(); }
			
			ctrl.poll();
			current = comp.getPollData();
			
			if(current != previous)
			{
				if(Math.abs(current) > DEAD_ZONE)
				{
					System.out.print(current + "; ");
					i++;
				}
				previous = current;
			}
			
			if(i % 10 == 0)
			{
				System.out.println();
				i = 1;
			}
		}
	}
	
	public static void listControllers()
	{
		ControllerEnvironment ce =
				ControllerEnvironment.getDefaultEnvironment();

		Controller[] cs = ce.getControllers();
		
		for(int i = 0; i < cs.length; i++)
			System.out.println(i + ". " + cs[i].getName() + ", " + cs[i].getType());
	}
	
	private static void printDetails(Controller c)
	{
		System.out.println("Details for: " + c.getName() + ", " +
				c.getType() + ", " + c.getPortType());
		
		printComponents(c.getComponents());
		printRumblers(c.getRumblers());
		
		Controller[] subCtrls = c.getControllers();
		
		if(subCtrls.length == 0) System.out.println("No subcontrollers");
		else
		{
			System.out.println("Number of subcontrollers: " + subCtrls.length);
			
			for(int i = 0; i < subCtrls.length; i++)
			{
				System.out.println("----------------------------------------");
				System.out.println("Subcontroller: " + i);
				printDetails(subCtrls[i]);
			}
		}
	}
	
	private static void printComponents(Component[] comps)
	{
		if(comps.length == 0) System.out.println("No components");
		else
		{
			System.out.println("Number of components: " + comps.length);
			
			for (int i = 0; i < comps.length; i++)
			{
				System.out.println(i + ". " +
					 comps[i].getName() + ", " +
					 getIdentifierName(comps[i]) + ", " +
					(comps[i].isRelative() ? "relative" : "absolute") + ", " +
					(comps[i].isAnalog() ? "analog" : "digital") + ", " +
					 comps[i].getDeadZone());
			}
		}
	}
	
	private static void printRumblers(Rumbler[] rumblers)
	{
		if(rumblers.length == 0) System.out.println("No rumblers");
		else
		{
			System.out.println("Number of rumblers: " + rumblers.length);
			
			Component.Identifier id;
			
			for (int i = 0; i < rumblers.length; i++)
			{
				id = rumblers[i].getAxisIdentifier();
				
				System.out.print(i + ". " + rumblers[i].getAxisName() + " on axis; ");
				
				if(id == null) System.out.println("no name");
				else System.out.println("name: " + id.getName());
			}
		}
	}
	
	private static String getIdentifierName(Component c)
	{
		Component.Identifier id = c.getIdentifier();
		
		if(id == Component.Identifier.Button.UNKNOWN) return "button";
		else if(id == Component.Identifier.Key.UNKNOWN) return "key";
		else return id.getName();
	}
}
