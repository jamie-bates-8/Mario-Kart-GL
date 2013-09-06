package bates.jamie.graphics.scene;

import java.awt.event.KeyEvent;

import bates.jamie.graphics.io.GamePad;
import bates.jamie.graphics.util.RotationMatrix;
import bates.jamie.graphics.util.Vec3;


public class AnchorPoint
{
	protected Vec3 c;
	protected RotationMatrix u;
	protected float rx, ry, rz;
	
	private float speed = 5.0f;
	
	private float sensitivity = 2.0f;
	private static final float MIN_SENSITIVITY = 0.25f;
	private static final float MAX_SENSITIVITY = 4.0f;
	private static final float SENSITIVITY_INC = 0.25f;
	private static final float MAX_PITCH = 60.0f;
	
	public AnchorPoint()
	{
		reset();
	}
	
	public void reset()
	{
		c = new Vec3(); c.y = 50;
		u = new RotationMatrix();
		rx = ry = rz = 0;
	}
	
	public void moveLeft(float d)     { c = c.subtract(u.xAxis.multiply(d)); }
	public void moveRight(float d)    { c = c.     add(u.xAxis.multiply(d)); }
	public void moveForward(float d)  { c = c.subtract(u.zAxis.multiply(d)); }
	public void moveBackward(float d) { c = c.     add(u.zAxis.multiply(d)); }
	
	public void lookLeft (float theta) { ry -= theta * sensitivity; u = new RotationMatrix(rx, ry, rz); }
	public void lookRight(float theta) { ry += theta * sensitivity; u = new RotationMatrix(rx, ry, rz); }
	
	public void lookUp(float theta)
	{
		if(rx > -MAX_PITCH) rx -= theta * sensitivity;
		u = new RotationMatrix(rx, ry, rz);
	}
	
	public void lookDown(float theta)
	{
		if(rx <  MAX_PITCH) rx += theta * sensitivity;
		u = new RotationMatrix(rx, ry, rz);
	}
	
	public void increaseSensitivity()
	{
		if(sensitivity < MAX_SENSITIVITY) sensitivity += SENSITIVITY_INC;
	}
	
	public void decreaseSensitivity()
	{
		if(sensitivity > MIN_SENSITIVITY) sensitivity -= SENSITIVITY_INC;
	}
	
	public float getSensitivity() { return sensitivity; }
	
	public Vec3 getPosition() { return c; }
	
	public void setPosition(Vec3 c) { this.c = c; }
	
	public RotationMatrix getOrientation() { return u; }
	
	public void setOrientation(RotationMatrix u) { this.u = u; }
	
	public void setRotation(Vec3 r)
	{
		rx = r.x;
		ry = r.y;
		rz = r.z;
		
		u = new RotationMatrix(rx, ry, rz);
	}
	
	public Vec3 getRotation() { return new Vec3(rx, ry, rz); }
	
	public void setSpeed(float speed) { this.speed = speed; }
	
	public void keyPressed(KeyEvent e)
	{
		switch(e.getKeyCode())
		{
			case KeyEvent.VK_W: moveForward(speed);  break;
			case KeyEvent.VK_S: moveBackward(speed); break;
			case KeyEvent.VK_A: moveLeft(speed);     break;
			case KeyEvent.VK_D: moveRight(speed);    break;
			
			case KeyEvent.VK_UP:    lookUp(5);    break;
			case KeyEvent.VK_DOWN:  lookDown(5);  break;
			case KeyEvent.VK_LEFT:  lookLeft(5);  break;
			case KeyEvent.VK_RIGHT: lookRight(5); break;
			
			case KeyEvent.VK_MINUS:  decreaseSensitivity(); break;
			case KeyEvent.VK_EQUALS: increaseSensitivity(); break;
		}
	}
	
	public void update(GamePad controller)
	{
		float x  = controller.getXAxis() * speed;
		float y  = controller.getYAxis() * speed;
		float rx = controller.getXRotation();
		float ry = controller.getYRotation();
		
		if     (x > 0) moveLeft(x);
		else if(x < 0) moveRight(-x);
		
		if     (y > 0) moveForward(y);
		else if(y < 0) moveBackward(-y);
		
		if     (rx > 0) lookRight(rx);
		else if(rx < 0) lookLeft(-rx);
		
		if     (ry > 0) lookDown(ry);
		else if(ry < 0) lookUp(-ry);
	}
}
