package bates.jamie.graphics.item.strategy;

import bates.jamie.graphics.entity.Car;

public class PowerStar implements IItem
{

	public void pressItem(Car car)
	{
		car.usePowerStar();
		
	}

}
