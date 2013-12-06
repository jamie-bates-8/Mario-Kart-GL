package bates.jamie.graphics.item.strategy;

import bates.jamie.graphics.entity.Car;

public class Mushroom implements IItem
{
	public void pressItem(Car car)
	{		
		car.setBoosting(true);
		car.getScene().focalBlur.enableRadial = true;
		car.setBoostDuration(60);
		car.velocity = 2 * Car.TOP_SPEED;
	}
}
