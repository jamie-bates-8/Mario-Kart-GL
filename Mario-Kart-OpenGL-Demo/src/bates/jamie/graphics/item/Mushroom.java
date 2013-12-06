package bates.jamie.graphics.item;

import bates.jamie.graphics.entity.Car;

public class Mushroom implements IItem
{
	public void pressItem(Car car)
	{		
		car.boost();
	}
}
