package bates.jamie.graphics.item.strategy;

import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.item.FakeItemBox;

public class FakeItemBoxP implements IItem {

	public void pressItem(Car car)
	{
		FakeItemBox fakeBox = (FakeItemBox) car.getItems().remove();
		
		switch(car.getAiming())
		{
			case FORWARDS: fakeBox.throwUpwards(); break;
			case BACKWARDS: fakeBox.throwBackwards(); break;
			default: break;
		}
		car.getScene().addItem(fakeBox);
	}
}
