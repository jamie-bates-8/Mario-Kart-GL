package bates.jamie.graphics.item;

import bates.jamie.graphics.entity.Car;

public class PowerStar implements IItem
{

	public void pressItem(Car car)
	{
		car.usePowerStar();
		
	}

}
