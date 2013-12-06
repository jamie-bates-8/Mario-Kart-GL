package bates.jamie.graphics.item.strategy;

import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.item.IItem;

public class Boo implements IItem
{

	public void pressItem(Car car)
	{
		car.useBoo();
		
	}

}
