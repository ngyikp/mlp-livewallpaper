package com.overkill.live.pony;

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
    public Pony.Directions direction;
	public Pony.Directions centering;
	
	public Point position;
	
    public boolean follow = false;

    public long last_used = 0;
    public boolean already_played_for_currentbehavior = false;
	public boolean Close_On_New_Behavior;
	
	private boolean visible;
	public long endTime;
	
	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;		
	}
	
	public Sprite getImage(){
		return current_image;
	}

	public void setLocation(Point effectLocation) {
		position = effectLocation;		
	}
	
	public Sprite getRightImage(){
		if(right_image == null) right_image = new Sprite(right_image_path);
		return right_image;
	}
	
	public Sprite getLeftImage(){
		if(left_image == null) left_image = new Sprite(left_image_path);
		return left_image;
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