package bates.jamie.graphics.scene;
import static bates.jamie.graphics.util.Vector.add;
import static bates.jamie.graphics.util.Vector.multiply;
import static bates.jamie.graphics.util.Vector.subtract;


import java.awt.event.KeyEvent;

import bates.jamie.graphics.io.GamePad;
import bates.jamie.graphics.util.Matrix;


public class AnchorPoint
{
	private float[] c;
	private static final float[] ORIGIN = {0, 10, 0};
	
	protected float[][] u;
	private static final float[][] DEFAULT_ROTATION = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
	private float rx, ry, rz;
	
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
		c = ORIGIN;
		u = DEFAULT_ROTATION;
		rx = ry = rz = 0;
	}
	
	public void moveLeft(float d)     { c = 	 add(c, multiply(u[2], d)); }
	public void moveRight(float d)    { c = subtract(c, multiply(u[2], d)); }
	public void moveForward(float d)  { c = subtract(c, multiply(u[0], d)); }
	public void moveBackward(float d) { c =      add(c, multiply(u[0], d)); }
	
	public void lookLeft(float theta)  { ry += theta * sensitivity; u = Matrix.getRotationMatrix(rx, ry, rz); }
	public void lookRight(float theta) { ry -= theta * sensitivity; u = Matrix.getRotationMatrix(rx, ry, rz); }
	
	public void lookUp(float theta)
	{
		if(rz > -MAX_PITCH) rz -= theta * sensitivity;
		u = Matrix.getRotationMatrix(rx, ry, rz);
	}
	
	public void lookDown(float theta)
	{
		if(rz <  MAX_PITCH) rz += theta * sensitivity;
		u = Matrix.getRotationMatrix(rx, ry, rz);
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
	
	public float[] getPosition() { return c; }
	
	public void setPosition(float[] c) { this.c = c; }
	
	public float[][] getRotationMatrix() { return u; }
	
	public void setRotation(float[] r)
	{
		rx = r[0];
		ry = r[1];
		rz = r[2];
		
		u = Matrix.getRotationMatrix(rx, ry, rz);
	}
	
	public float[] getRotation() { return new float[] {rx, ry, rz}; }
	
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
		float x  = controller.getXAxis();
		float y  = controller.getYAxis();
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
