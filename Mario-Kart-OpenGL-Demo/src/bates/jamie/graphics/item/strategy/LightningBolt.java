package bates.jamie.graphics.item.strategy;

import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.particle.LightningParticle;
import bates.jamie.graphics.util.Vec3;

public class LightningBolt implements IItem
{

	public void pressItem(Car car)
	{
			for(Car otherCar : car.getScene().getCars()) strikeWithLightning(otherCar);
				//if(!car.equals(this)) car.struckByLightning();
	}

	private void strikeWithLightning(Car otherCar) {
		if(!otherCar.hasStarPower() && !otherCar.isInvisible())
		{
			if(!otherCar.isMiniature())
			{
				otherCar.bound.e = otherCar.bound.e.multiply(0.5f);
				otherCar.setScale(otherCar.getScale()/2);
				otherCar.graph.getRoot().setScale(new Vec3(otherCar.getScale()));
			}
			
			otherCar.setMiniature(true);
			otherCar.setMiniatureDuration(400);
			otherCar.velocity = 0;
			
			if(otherCar.isSlipping()) otherCar.setSlipDuration(otherCar.getSlipDuration() + 24);
			else otherCar.spin();
		}
		
		Vec3 source = getLightningVector(otherCar); 
		otherCar.getScene().addParticle(new LightningParticle(source));
	}

	public Vec3 getLightningVector(Car otherCar)
	{
		return otherCar.bound.c.add(otherCar.bound.u.yAxis.multiply(20));
	}
}
