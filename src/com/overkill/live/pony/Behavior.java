package com.overkill.live.pony;

import java.util.LinkedList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Point;

import com.overkill.live.pony.Pony.AllowedMoves;

public class Behavior {
	public String name;
	public String pony_name;
	public double chance_of_occurance;
	public double maxDuration;
	public double minDuration; 
	public double speed;
	
	private Sprite current_image = null;
	
	public String image_right_path;
	private Sprite image_right = null;
	
	public String image_left_path;
	private Sprite image_left = null;
	
	public AllowedMoves allowedMoves;
	
	public String linkedBehaviorName = null;
	public Behavior linkedBehavior;

	public long endTime;
	
	public AllowedMoves Allowed_Movement;
	public boolean Skip;
	public boolean right;
	private boolean currentRight = true;
	public int destination_xcoord;
	public int destination_ycoord;
	public boolean horizontal;
	public boolean vertical;
	public boolean up;
	public int delay;

	public String follow_object_name = "";
    public Pony follow_object;
	
	public List<Effect> effects = new LinkedList<Effect>();
	public boolean blocked;
	
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
		this.getCurrentImage().update(globalTime);
	}

	/**
	 * Draws the currently used image on the canvas at the position
	 * @param canvas The {@link Canvas} to draw on
	 * @param position The position on the canvas
	 */
	public void draw(Canvas canvas, Point position) {
		this.getCurrentImage().draw(canvas, position);
	}	
	
	/**
	 * Set the current image if it is null or we need to change direction
	 * @return
	 */
	public Sprite getCurrentImage(){	
		if(current_image == null){
			 this.selectCurrentImage();
		}
		if(currentRight != right){
			this.selectCurrentImage();
			currentRight = right;
		}
		return current_image;
	}
	
	/**
	 * Set Image depending on current direction
	 */
	private void selectCurrentImage(){
		current_image = null;
		if (this.right){
			if(image_right == null){
				image_right = new Sprite(image_right_path);
			}
	        current_image = image_right;
		}else{
	    	if(image_left == null)
				image_left = new Sprite(image_left_path);
	        current_image = image_left;
	    }
	}
		
//	public Point getDestination(int screenWidth, int screenHeight) {
//	    // If we have a coordinate to go to
//	    if (destination_xcoord != 0 && destination_ycoord != 0) {
//	    	// Return its position on the screen
//	    	Rect screenBounds = new Rect(0, 0, screenWidth, screenHeight);
//	        return new Point((int)(((destination_xcoord * screenBounds.right) / 100) + screenBounds.left),
//	                         (int)(((destination_ycoord * screenBounds.bottom) / 100) + screenBounds.top));
//	    }
//
//	    // Otherwise return a blank Point
//    	return new Point();
//    }
	
	public Point getDestination(Pony pony) {
    	// If we aren't following anything yet
    	if ((follow_object_name.length() > 0 && follow_object == null) || (follow_object != null && !follow_object.isVisible())) {
    		if (pony.isInteracting && follow_object_name.trim().equalsIgnoreCase(pony.currentInteraction.trigger.name.trim())) {
    			follow_object = pony.currentInteraction.trigger;
    			return new Point(follow_object.getLocation().x + destination_xcoord, follow_object.getLocation().y + destination_ycoord);
    		}
    		if (pony.isInteracting && pony.currentInteraction.initiator != null && follow_object_name.trim().equalsIgnoreCase(pony.currentInteraction.initiator.name.trim())) {
    			follow_object = pony.currentInteraction.initiator;
    			return new Point(follow_object.getLocation().x + destination_xcoord, follow_object.getLocation().y + destination_ycoord);
    		}
    		
    		List<Pony> ponies_to_follow = new LinkedList<Pony>();
    		// Look for a Pony to follow
            for (Pony ipony : RenderEngine.activePonies) {
                if (ipony.name.trim().equalsIgnoreCase(follow_object_name.trim()))
                    ponies_to_follow.add(ipony);
            }

            // If one or more ponies found
            if (ponies_to_follow.size() > 0) {
            	// Select one at random to follow
                int dice = MyLittleWallpaperService.rand.nextInt(ponies_to_follow.size());
                follow_object = ponies_to_follow.get(dice);
                return new Point(follow_object.getLocation().x + destination_xcoord, follow_object.getLocation().y + destination_ycoord);
            }

            // TODO follow a effect
//            List<EffectWindow> effects_to_follow = new LinkedList<EffectWindow>();
//            // Look for an object (effect) to follow
//            for(EffectWindow effect : pony.activeEffects) {
//                if (effect.effectName.trim().equalsIgnoreCase(follow_object_name.trim())) {
//                    effects_to_follow.add(effect);
//                }
//            }
//
//            // If one or more objects found
//            if (effects_to_follow.size() > 0) {
//            	// Select one at random to follow
//                int dice = MyLittleWallpaperService.rand.nextInt(effects_to_follow.size());
//                follow_object = effects_to_follow.get(dice);
//                return new Point(follow_object.getLocation().x + destination_xcoord, follow_object.getLocation().y + destination_ycoord);
//            }
            
            // Return blank point if nothing found
        	return new Point();
    	}
    	
    	// If we are following an object
	    if (follow_object != null)
	        return new Point(follow_object.getLocation().x + destination_xcoord, follow_object.getLocation().y + destination_ycoord); // Return its current screen position

	    // If we have a coordinate to go to
	    if (destination_xcoord != 0 && destination_ycoord != 0) {
	    	// Return its position on the screen
	        return new Point((int)(((destination_xcoord * RenderEngine.screenBounds.width()) / 100) + RenderEngine.screenBounds.left),
	                         (int)(((destination_ycoord * RenderEngine.screenBounds.height()) / 100) + RenderEngine.screenBounds.bottom));
	    }

	    // Otherwise return a blank Point
    	return new Point();
    }
	
	/**
	 * Bounce the Pony of the screen edge
	 * @param pony
	 * @param current_location
	 * @param new_location
	 * @param x_movement
	 * @param y_movement
	 */
	public void bounce(Pony pony, Point current_location, Point new_location, int x_movement, int y_movement) {
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
    	
    	Point new_location_x = new Point(new_location.x, current_location.y);
    	Point new_location_y = new Point(current_location.x, new_location.y);

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
		// Compare the names since they should be unique
		return this.name.endsWith(b.name);
	}
	
	public void destroy(){
		if(this.current_image != null) this.current_image.destroy();
		this.current_image = null;
		if(this.image_left != null) this.image_left.destroy();
		this.image_left = null;
		if(this.image_right != null) this.image_right.destroy();
		this.image_right = null;
	}

	public void addEffect(String effectname, String right_image, String left_image, double duration, double repeat_delay, Pony.Directions direction_right, Pony.Directions centering_right, Pony.Directions direction_left, Pony.Directions centering_left, boolean follow) {
		Effect new_effect = new Effect();
    	
    	new_effect.behavior_name = this.name;
    	new_effect.pony_Name = this.pony_name;
        new_effect.name = effectname;
        new_effect.right_image_path = right_image;
        new_effect.left_image_path = left_image;
        new_effect.duration = duration;
        new_effect.repeat_delay = repeat_delay;
        new_effect.placement_direction_right = direction_right;
        new_effect.centering_right = centering_right;
        new_effect.placement_direction_left = direction_left;
        new_effect.centering_left = centering_left;
        new_effect.follow = follow;

        effects.add(new_effect);
		
	}
}
