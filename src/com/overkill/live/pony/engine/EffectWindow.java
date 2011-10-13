package com.overkill.live.pony.engine;

import android.graphics.Canvas;
import android.graphics.Point;

public class EffectWindow{

	public long endTime;
	public boolean follows;
	public Pony.Directions direction;
	public Pony.Directions centering;
	public String effectName;
	public boolean Close_On_New_Behavior;
	public String behaviorName;
	public String ponyName;
	
	public Point position;
	
	private Sprite image;
	
	public long startTime;
	
	boolean ready = false;
	
	public void update(long globalTime){
		this.image.update(globalTime);
	}
	
	public void draw(Canvas canvas){
		this.image.draw(canvas, position);
	}

	public Sprite getImage() {
		return this.image;
	}
	
	public void setImage(Sprite image){
		this.image = image;
	}

	public void setLocation(Point effectLocation) {
		this.position = effectLocation;		
	}
	
	public void destroy() {
		if(this.image != null) this.image.destroy();
		this.image = null;
	}
	
}
