package com.overkill.live.pony;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import com.overkill.live.pony.Pony.AllowedMoves;

public class Behavior {
	public String name;
	public double chance;
	public double maxDuration;
	public double minDuration; 
	public double speed;
	
	public Sprite current_image;
	
	public String image_right_path;
	public Sprite image_right;
	
	public String image_left_path;
	public Sprite image_left;
	
	public AllowedMoves allowedMoves;
	
	public String linkedBehaviorName = null;
	public Behavior linkedBehavior;

	public long endTime;
	
	public AllowedMoves Allowed_Movement;
	public boolean Skip;
	public boolean right;
	public int destination_xcoord;
	public int destination_ycoord;
	public boolean horizontal;
	public boolean vertical;
	public boolean up;
	public int delay;

	/**
	 * Default Constructor
	 */
	public Behavior() {
	}

	/**
	 * Updates the currently used image
	 * @param globalTime The time
	 */
	public void update(long globalTime) {
		this.current_image.update(globalTime);
	}

	/**
	 * Draws the currently used image on the canvas at the position
	 * @param canvas The {@link Canvas} to draw on
	 * @param position The position on the canvas
	 */
	public void draw(Canvas canvas, PointF position) {
		this.current_image.draw(canvas, position);
	}	
	
	public PointF getDestination(int screenWidth, int screenHeight) {
	    // If we have a coordinate to go to
	    if (destination_xcoord != 0 && destination_ycoord != 0) {
	    	// Return its position on the screen
	    	Rect screenBounds = new Rect(0, 0, screenWidth, screenHeight);
	        return new PointF((int)(((destination_xcoord * screenBounds.right) / 100) + screenBounds.left),
	                         (int)(((destination_ycoord * screenBounds.bottom) / 100) + screenBounds.top));
	    }

	    // Otherwise return a blank Point
    	return new PointF();
    }
	
	public void bounce(Pony pony, PointF current_location, PointF new_location, int x_movement, int y_movement) {
    	if (x_movement == 0 && y_movement == 0)
    		return;
    	
    	// if we are moving in a simple direction (up/down, left/right) just reverse direction
    	if (x_movement == 0 && y_movement != 0) {
    		up = !up;
    		return;
    	}
    	if (x_movement != 0 && y_movement == 0) {
    		right = !right;
    		return;
    	}
    	
    	// if we were moving in a composite direction, we need to determine which component is bad
    	boolean x_bad = false;
    	boolean y_bad = false;
    	
    	PointF new_location_x = new PointF(new_location.x, current_location.y);
    	PointF new_location_y = new PointF(current_location.x, new_location.y);

        if (x_movement != 0 && y_movement != 0) {
	        if (!pony.isPonyOnWallpaper(new_location_x)) {
	        	x_bad = true;
	        }
	        if (!pony.isPonyOnWallpaper(new_location_y)) {
	        	y_bad = true;
	        }
        }
        
        if (!x_bad && !y_bad) {
        	up = !up;
        	right = !right;
        	return;
        }
        
        if (x_bad && y_bad) {
        	up = !up;
        	right = !right;
        	return;
        }
        
        if (x_bad) {
        	right = !right;
        	return;
        }
        
        if (y_bad) {
        	up = !up;
        	return;
        }
    }
	
	public boolean equals(Behavior b){
		return this.name.endsWith(b.name);
	}
	
	public void recycle(){
		this.current_image.recycle();
	}
}
