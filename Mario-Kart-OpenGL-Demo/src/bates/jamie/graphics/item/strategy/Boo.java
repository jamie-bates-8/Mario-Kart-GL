package bates.jamie.graphics.item.strategy;

import bates.jamie.graphics.entity.Car;

public class Boo implements IItem
{

	public void pressItem(Car car)
	{
		car.setInvisible(true);
		car.setBooDuration(400);
	}

}
