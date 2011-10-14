package com.overkill.live.pony.engine;

import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;

public class Effect {
    public String name;
    public String behavior_name;
    public String pony_Name;
    
    private Sprite right_image;
    public String right_image_path;
    private Sprite left_image;
    public String left_image_path;
    public Sprite current_image;
    
    public double duration;
    public double repeat_delay;
    public Pony.Directions placement_direction_right;
    public Pony.Directions centering_right;
    public Pony.Directions placement_direction_left;
    public Pony.Directions centering_left;
	
	public Point position;
	
    public boolean follow = false;

    public long last_used = 0;
    public boolean already_played_for_currentbehavior = false;
		
	public Sprite getImage(){
		return current_image;
	}

	public void setLocation(Point effectLocation) {
		position = effectLocation;		
	}
	
	public Sprite getRightImage(boolean initializeGIF){
		if(initializeGIF) right_image = new Sprite(right_image_path, true);
		if(right_image == null) right_image = new Sprite(right_image_path, initializeGIF);
		return right_image;
	}
	
	public Sprite getLeftImage(boolean initializeGIF){
		if(initializeGIF) left_image = new Sprite(left_image_path, true);
		if(left_image == null) left_image = new Sprite(left_image_path, initializeGIF);
		return left_image;
	}
	
	public Sprite getRightImage(){
		return getRightImage(false);
	}
	
	public Sprite getLeftImage(){
		return getLeftImage(false);
	}
	
	public void preload(){
		if(right_image == null) right_image = new Sprite(right_image_path, true);
		if(left_image == null) left_image = new Sprite(left_image_path, true);	
	}
	
	public void destroy(){
		Log.i("Effect[" + name + "]", "destroy");
		this.already_played_for_currentbehavior = false;
		this.last_used = 0;
		//if(this.right_image != null) this.right_image.destroy();
		//this.right_image = null;
		//if(this.left_image != null) this.left_image.destroy();
		//this.left_image = null;
		//if(this.current_image != null) this.current_image.destroy();
		//this.current_image = null;
	}
	
	public void draw(Canvas canvas){
		//Log.i("Effect[" + name + "]", "draw");
		this.current_image.draw(canvas, position);		
	}

	public void update(long globalTime) {
		//Log.i("Effect[" + name + "]", "update");
		this.current_image.update(globalTime);		
	}
}