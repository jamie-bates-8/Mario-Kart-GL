package bates.jamie.graphics.item.strategy;

import java.util.Queue;

import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.item.Item;
import bates.jamie.graphics.item.Shell;

public class ShellP implements IItem 
{
	public void pressItem(Car car) 
	{
		Queue<Item> items = car.getItems();
		if(!items.isEmpty())
		{
			Shell shell = (Shell) items.remove();
			switch(car.getAiming())
			{
				case FORWARDS: shell.throwForwards(); break;
				case BACKWARDS: shell.throwBackwards(); break;
				default: break;
			}
			car.getScene().addItem(shell);	
		}
	}
}
