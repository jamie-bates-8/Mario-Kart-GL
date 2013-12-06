package bates.jamie.graphics.item.strategy;

import bates.jamie.graphics.entity.Car;

public class LightningBolt implements IItem
{

	public void pressItem(Car car)
	{
		car.useLightningBolt();
		
	}

}
