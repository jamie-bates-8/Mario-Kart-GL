package bates.jamie.graphics.item.strategy;

import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.item.IItem;

public class Mushroom implements IItem
{
	public void pressItem(Car car)
	{		
		car.boost();
	}
}
