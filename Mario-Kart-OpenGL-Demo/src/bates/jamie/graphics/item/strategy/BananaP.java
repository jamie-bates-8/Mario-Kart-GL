package bates.jamie.graphics.item.strategy;

import java.util.Queue;

import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.item.Banana;
import bates.jamie.graphics.item.Item;

public class BananaP implements IItem 
{
	public void pressItem(Car car)
	{
		Queue<Item> items = car.getItems();
		if(!items.isEmpty())
		{
			Banana banana = (Banana) items.remove();
			switch(car.getAiming())
			{
				case FORWARDS: banana.throwUpwards(); break;
				case BACKWARDS: banana.throwBackwards(); break;
				default: break;
			}
			car.getScene().addItem(banana);
		}
	}
}