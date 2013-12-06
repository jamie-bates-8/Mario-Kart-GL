package bates.jamie.graphics.item.strategy;

import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.item.Banana;

public class BananaP implements IItem 
{
	public void pressItem(Car car)
	{
		Banana banana = (Banana) car.getItems().remove();
		switch(car.getAiming())
		{
			case FORWARDS: banana.throwUpwards(); break;
			case BACKWARDS: banana.throwBackwards(); break;
			default: break;
		}
		car.getScene().addItem(banana);	
	}
}