package graphics.util;

import java.util.ArrayList;
import java.util.List;

import static graphics.util.Vector.*;

public class Gradient
{
	private List<Stop> stops = new ArrayList<Stop>();
	
	public Gradient(float[] color1, float[] color2)
	{
		stops.add(new Stop(  0, color1));
		stops.add(new Stop(100, color2));
	}
	
	public void addStop(int location, float[] color)
	{
		int index = 0;
		
		while(stops.get(index).location < location) index++;
		
		stops.add(index, new Stop(location, color));
	}
	
	public float[] getColor(int location)
	{
		int index = 0;
		
		while(stops.get(index + 1).location < location) index++;
		
		int lower = location - stops.get(index).location;
		int upper = stops.get(index + 1).location - location;
		
		if(lower < upper) return stops.get(index).color;
		else return stops.get(index + 1).color;
	}
	
	public float[] getColor(double location)
	{
		return getColor((int) (location * 100));
	}
	
	private class Stop
	{
		private int location;
		private float[] color;
		
		public Stop(int location, float[] color)
		{
			this.location = location;
			
			if(color[0] > 1 || color[1] > 1 || color[2] > 1)
				color = multiply(color, 1.0f / 255);
			
			this.color = color;	
		}
	}
}
