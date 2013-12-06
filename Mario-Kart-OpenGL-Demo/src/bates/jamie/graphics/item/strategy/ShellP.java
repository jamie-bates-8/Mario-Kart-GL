package bates.jamie.graphics.item.strategy;

import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.item.Shell;

public class ShellP implements IItem 
{
	public void pressItem(Car car) 
	{
		Shell shell = (Shell) car.getItems().remove();
		
		switch(car.getAiming())
		{
			case FORWARDS: shell.throwForwards(); break;
			case BACKWARDS: shell.throwBackwards(); break;
			default: break;
		}
		car.getScene().addItem(shell);	
	}
}
