package com.overkill.live.pony.engine;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import com.overkill.live.pony.MyLittleWallpaperService;
import com.overkill.live.pony.ToolSet;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.Log;

/**
 * This class handles the {@link Sprite} movement and frame-animation
 * @author ov3rk1ll
 *
 */
public class Pony{
		
	// Effect options
	public static final int EF_effect_name = 1;
    public static final int EF_behavior_name = 2;
    public static final int EF_right_image = 3;
    public static final int EF_left_image = 4;
    public static final int EF_duration = 5;
    public static final int EF_delay_before_next = 6;
    public static final int EF_location_right = 7;
    public static final int EF_center_right = 8;
    public static final int EF_location_left = 9;
    public static final int EF_center_left = 10;
    public static final int EF_follow = 11;
            
	// Behavior options
	public static final int BO_name = 1;
	public static final int BO_probability = 2;
	public static final int BO_max_duration = 3;
	public static final int BO_min_duration = 4;
	public static final int BO_speed = 5;
	public static final int BO_right_image_path = 6;
	public static final int BO_left_image_path = 7;
	public static final int BO_movement_type = 8;
	public static final int BO_linked_behavior = 9;
	public static final int BO_speaking_start = 10;
	public static final int BO_speaking_end = 11;
	public static final int BO_skip = 12;
	public static final int BO_xcoord = 13;
	public static final int BO_ycoord = 14;
	public static final int BO_object_to_follow = 15;
	
	public String name;
	
//	private Point position = new Point(0, 0);	
	private Point destination;
	
	public List<Behavior> behaviors;
	
	private Behavior currentBehavior = null;
	private Behavior nextBehavior = null;
	//private Behavior cachedBehavior = null;
	
//	private boolean ponyDirection;

	private long lastTimeMoved = 0;
	
	public boolean hasSpawned = false;

	public List<EffectWindow> activeEffects;
	public List<Interaction> interactions = new LinkedList<Interaction>();

//	public boolean shouldBeSleeping = false;
	
	public Interaction currentInteraction;
	public boolean isInteracting = false;
	public boolean interactionActive = false;
	private boolean isInteractionInitiator = false;
	private long interactionDelayUntil = 0;

	private boolean atDestination;

	private boolean sleeping;

	public PonyWindow window;
	
//	private boolean waitForEffectToLoad;

	protected boolean currentlyLoadingEffects;
	
	public enum AllowedMoves {
		None,
	    Horizontal_Only,
	    Vertical_Only,
	    Horizontal_Vertical,
	    Diagonal_Only,
	    Diagonal_Horizontal,
	    Diagonal_Vertical,
	    All,
	    MouseOver,
	    Sleep,
	    Dragged
	}

	public enum Direction{
	    top,
	    bottom,
	    left,
	    right,
	    bottom_right,
	    bottom_left,
	    top_right,
	    top_left,
	    center,
	    random,
	    random_not_center
	}

	public Pony(String name){
		this.name = name;
		this.behaviors = new LinkedList<Behavior>();
		this.activeEffects = new LinkedList<EffectWindow>();
		this.window = new PonyWindow(this.name);
	}

	@Override
	public boolean equals(Object p) {
		return this.name.equals(((Pony)p).name);
	}
	
	public void update(final long globalTime) {
		if (this.window.shouldBeSleeping) {
			if (sleeping){
				return;
			}else{
				sleep(globalTime);
			}
		} else {
			if (sleeping)
				wakeUp(globalTime);
		}
		
		if (currentBehavior == null) { // If we have no behavior, select a random one
			cancelInteraction(globalTime);
			selectBehavior(null, globalTime, true);
		} else if ((currentBehavior.endTime - globalTime) <= 0 && currentBehavior.keep == false) { // If the behavior has run its course, select a new one			
			if (currentBehavior.linkedBehavior != null) { // If we have a linked behavior, select that one next
				selectBehavior(currentBehavior.linkedBehavior, globalTime);
			} else { // Otherwise select a random one
				selectBehavior(null, globalTime);
			}					
		}   	        
	    // Move the Pony
		if(hasSpawned == false){
			this.teleport();
			hasSpawned = true;
		}
	    move(globalTime);	    
	}
	
	public void updateSprites(long globalTime){
//		currentBehavior.update(globalTime);
		this.window.update(globalTime, "updateSprites");
		
	    for (EffectWindow effect : this.activeEffects) {
			effect.update(globalTime);
		}
	}
	
	public void teleport() {
		Point teleport_location = new Point(0, 0);		
		for (int i = 0; i < 300; i++) {
			// Then select a random location to appear at		
			if(RenderEngine.screenBounds != null && RenderEngine.screenBounds.width() > 0 && RenderEngine.screenBounds.height() > 0)
				teleport_location = new Point(
						MyLittleWallpaperService.rand.nextInt((int)RenderEngine.screenBounds.width()) + (int)RenderEngine.screenBounds.left, 
						MyLittleWallpaperService.rand.nextInt((int)RenderEngine.screenBounds.height()) + (int)RenderEngine.screenBounds.top);
		
		    if(isPonyOnWallpaper(teleport_location) == true) break;
		}	
	    // Finally, go there
	    this.window.setLocation(teleport_location);
	}

	public void sleep(long globalTime) {
		Behavior sleep_behavior = getAppropriateBehavior(Pony.AllowedMoves.Sleep, false, null);
		if (sleep_behavior.Allowed_Movement != Pony.AllowedMoves.Sleep) {
			sleep_behavior = getAppropriateBehavior(Pony.AllowedMoves.MouseOver, false, null);
			if (sleep_behavior.Allowed_Movement != Pony.AllowedMoves.MouseOver) {
				sleep_behavior = getAppropriateBehavior(Pony.AllowedMoves.None, false, null);
			}
		}
		selectBehavior(sleep_behavior, globalTime);
		//current_behavior.endTime = globalTime;
		paint(globalTime);
		sleeping = true;
	}
	
	public void wakeUp(long globalTime) {
		Behavior wake_behavior = getAppropriateBehavior(Pony.AllowedMoves.MouseOver, false, null);
		if (wake_behavior.Allowed_Movement == Pony.AllowedMoves.MouseOver) {
			selectBehavior(wake_behavior, globalTime);
		}else{
			currentBehavior.endTime = globalTime;
		}
		sleeping = false;
	}
	
	public Point getLocation(){
		return this.window.getLocation();
	}
	
	public void move(long globalTime) {		
		if(globalTime - lastTimeMoved < RenderEngine.MOVEMENT_DELAY_MS) // we want to move again to quickly
			return;
		
		lastTimeMoved = globalTime;

		currentBehavior.blocked = false;
		
		double speed = currentBehavior.speed * RenderEngine.CONFIG_SCALE;
				
		// Get the destination for following (will be blank(0,0) if not following)
		destination = currentBehavior.getDestination(this);
	    
	    // Calculate the movement speed normally
		int x_movement = 0;
		int y_movement = 0;
		
		// If following something...
	    if (destination.x != 0 || destination.y != 0) {
	    	// Calculate the distance to this point
	    	double distance = Math.sqrt(Math.pow(this.getLocation().x - destination.x, 2) + Math.pow(this.getLocation().y - destination.y, 2));
	    	
	    	distance = distance == 0 ? 1 : distance;
	    	
	    	List<Pony.Direction> direction = getDestinationDirection(destination);
	    	
            try {
            	// Calculate the horizontal and vertical movement speeds
                if (direction.get(0) == Pony.Direction.left) {
                    x_movement = (int)((this.getLocation().x - destination.x) / (distance) * -speed);
                    currentBehavior.right = false;
                } else {
                    x_movement = (int)((destination.x - this.getLocation().x) / (distance) * speed);
                    currentBehavior.right = true;
                }
                
                y_movement = (int)((this.getLocation().y - destination.y) / (distance) * -speed);
            } catch(Exception ex) {
                //overflow due to distance being 0
                x_movement = 0;
                y_movement = 0;
            }
	    	
	    	// Determine if we are close enough to it
	        if (distance <= (this.window.getSpriteWidth() / 2)) {
	        	// If so, don't move anymore
	            x_movement = 0;
	            y_movement = 0;
	
	            atDestination = true;
	
	            //reached destination.
	            if (currentBehavior.linkedBehavior != null && currentBehavior.speed != 0) {
	                currentBehavior.endTime = globalTime;
	                destination = new Point();
	            }
	        } else { // Otherwise, we need to move towards it
	            if (atDestination == true) {
	                currentBehavior.delay = 10;
	            }
	
	            if (currentBehavior.delay > 0) {
	                atDestination = false;
	                currentBehavior.delay--;
	                return;
	            }
	
	            atDestination = false;
	        }
	    } else { // There is no destination, go whereever (If we aren't following anything)
	    	
	        if (!currentBehavior.right) {
	            speed = -speed;
	        }
	    
	        // Calculate the movement speed normally
			x_movement = (int) (currentBehavior.horizontal ? speed : 0);
			y_movement = (int) (currentBehavior.vertical ? speed : 0);
		
			if (currentBehavior.up) {
				if (y_movement > 0) {
					y_movement = -y_movement;
				}
			} else {
				if (y_movement < 0) {
					y_movement = -y_movement;
				}
			}
	    }

		// Point to determine where we would end up at this speed
		Point new_location = new Point(this.getLocation().x + x_movement, this.getLocation().y + y_movement);	
	    
	    if (isPonyOnWallpaper(new_location) && !isPonyInAvoidanceArea(new_location)) {
	        this.window.setLocation(new_location);
	        paint(globalTime);
	        
	        // Do we want interaction?
	        if(RenderEngine.CONFIG_INTERACT_PONY && !isInteracting){
		        Interaction Interact = findInteraction(globalTime);
		        	
		        if (Interact != null) {
		        	startInteraction(Interact, globalTime);
		        }
	        }
	        return;
	    }else{
	    	if(isPonyOnWallpaper(window.getLocation()) == false) {
	            //we are no where! Teleport!
	            teleport();
	            return;
	        }
	    }
	
	    //Nothing to worry about, we are on screen, but our current behavior would take us 
	    // off-screen in the next move.  Just do something else.
	    // if we are moving to a destination, our path is blocked, and we need to abort the behavior
	    // if we are just moving normally, just "bounce" off of the barrier.
	
	    if (destination.x == 0 && destination.y == 0) {
	    	currentBehavior.bounce(this, this.getLocation(), new_location, x_movement, y_movement);
	    } else {
	    	if (currentBehavior.follow_object == null) {
	    		currentBehavior = null;
	    	} else {
	    		//do nothing but stare longenly in the direction of the object we want to follow...
	    		currentBehavior.blocked = true;
		        paint(globalTime);
	    	}
	    }
	    
	    
	    
	}
		
	public void paint(final long globalTime){
		
		// Verify if we are following something
		if (destination.x != 0 || destination.y != 0) {
			// Calculate the horizontal and vertical distance 
	        double horizonal = Math.abs(destination.x - this.getLocation().x);
	        double vertical = Math.abs(destination.y - this.getLocation().y);
	        Behavior appropriate_behavior = null;
	        Pony.AllowedMoves allowed_movement = Pony.AllowedMoves.All;
	
	        // Calculate the real distance
	        double distance = Math.sqrt(Math.pow(this.getLocation().x - destination.x, 2) + Math.pow(this.getLocation().y - destination.y, 2));
	
	        // Determine if we want to move horizontally, diagonaly or vertically
	        if (distance >= this.window.getSpriteWidth() * 2) {
	            if (horizonal * 0.75 > vertical && allowed_movement == Pony.AllowedMoves.Horizontal_Only) {
	            	
	            } else {
	            	switch (allowed_movement) {
	            		case All:
	            		case Diagonal_Vertical:
	            		case Horizontal_Vertical:
	            		case Vertical_Only:
	    	                allowed_movement = Pony.AllowedMoves.Vertical_Only;
	    	                break;
	    	            default:
	    	            	allowed_movement = Pony.AllowedMoves.None;
	    	            	break;
	            	}
	            }
	        }
	     
	        // If we are already at destination or blocked
	        if (atDestination || currentBehavior.blocked || currentBehavior.speed == 0)
	            allowed_movement = Pony.AllowedMoves.None; // We are not allowed to move
	
	        // Find the animation best suited for where we are going
	        if (isInteracting)
	        	appropriate_behavior = getAppropriateBehavior(allowed_movement, true, currentBehavior);
	        else
	        	appropriate_behavior = getAppropriateBehavior(allowed_movement, true, null);
	
	        // Get this animation's left and right images
	        this.currentBehavior.image_left_path = appropriate_behavior.image_left_path;
	        this.currentBehavior.image_right_path = appropriate_behavior.image_right_path;
		}
			
		// Set the correct image to the behavior (left or right)
//	    this.currentBehavior.selectCurrentImage();
		this.window.ponyDirection = currentBehavior.right;
		// Change the pony animation if necessary
	    if (this.window.getBehaviorName() == null || this.window.getBehaviorName().equals(currentBehavior.name) == false) {
	    	this.window.setVisible(false);
	    	this.window.setBehaviorName(currentBehavior.name);
	    	this.window.setImages(new Sprite(currentBehavior.image_left_path), new Sprite(currentBehavior.image_right_path));    	
	    }
		
	    // Verify if we should create effects		
		if(RenderEngine.CONFIG_SHOW_EFFECTS){
	    	cleanUpEffects(globalTime); 
		}
	}
	
	public void cleanUpEffects(long globalTime){
		List<EffectWindow> effectsToRemove = new LinkedList<EffectWindow>();
		
        for (EffectWindow effect : this.activeEffects) {
        	if (effect.Close_On_New_Behavior) {
        		if (!currentBehavior.name.trim().equalsIgnoreCase(effect.behaviorName.trim())) {
        			effectsToRemove.add(effect);
        		}
        	}
        		
        	if(effect.endTime < globalTime)
        		effectsToRemove.add(effect);
        	
        	if (effect.follows)
        		effect.setLocation(ToolSet.getEffectLocation(effect, effect.direction, this.window, effect.centering));
        }
        
        for (EffectWindow effect : effectsToRemove) {
        	Log.i("Effect[" + effect.effectName +"]", "destroy");
            this.activeEffects.remove(effect);
        	effect.destroy();
        }
    }
	
	public Interaction findInteraction(long globalTime) {
		if (globalTime <= interactionDelayUntil) return null;
		
		for (Interaction interact : interactions) {
			for (Pony target : interact.interactsWith) {
				// Use Pythagorean theorem to get the distance
				double distance = 	Math.sqrt(
								Math.pow(this.getLocation().x + this.window.getSpriteWidth()  - target.window.getLocation().x + target.window.getSpriteWidth(),  2) + 
								Math.pow(this.getLocation().y + this.window.getSpriteHeight() - target.window.getLocation().y + target.window.getSpriteHeight(), 2)
									);
					
				if (distance <= interact.proximityActivationDistance) {
					double dice = MyLittleWallpaperService.rand.nextDouble();						
					if (dice <= interact.probability) {
						interact.trigger = target;
						return interact;
					}
				}
			}
		}		
		return null;
	}
	
	public void startInteraction(Interaction interaction, long globalTime) {
		isInteractionInitiator = true;
		currentInteraction = interaction;
		this.selectBehavior(interaction.behaviorList.get(MyLittleWallpaperService.rand.nextInt(interaction.behaviorList.size())), globalTime);
		for (Effect effect : currentBehavior.effects) {
			effect.already_played_for_currentbehavior = false;
		}
		
		if (interaction.selectAllTargets) {
			for (Pony pony : interaction.interactsWith) {
				pony.startInteractionAsTarget(currentBehavior.name, this, interaction, globalTime);
			}
		} else {
			interaction.trigger.startInteractionAsTarget(currentBehavior.name, this, interaction, globalTime);
		}
		
		isInteracting = true;
	}
	
	public void startInteractionAsTarget(String BehaviorName, Pony initiator, Interaction interaction, long globalTime) {
		for (Behavior behavior : behaviors) {
			if (BehaviorName.trim().equalsIgnoreCase(behavior.name.trim())) {
				this.selectBehavior(behavior, globalTime);
				for (Effect effect : currentBehavior.effects) {
					effect.already_played_for_currentbehavior = false;
				}
				break;
			}
		}		
		interaction.initiator = initiator;
		isInteractionInitiator = false;
		currentInteraction = interaction;
		isInteracting = true;
	}
	
	public void touch(long globalTime) {
		this.window.shouldBeSleeping = !this.window.shouldBeSleeping;
		Log.i("Pony[" + name + "].touch()", "are we sleeping now? " + this.window.shouldBeSleeping);
	}
	
	public void draw(Canvas canvas) {
		for (EffectWindow effect : this.activeEffects) {
			effect.draw(canvas);
		}
		if(isPonyOnScreen(this.window.getLocation())){
//			this.currentBehavior.draw(canvas, position);
			this.window.draw(canvas);
		}		
		if(MyLittleWallpaperService.INTERACTIONLINES){
			if(destination.x != 0 && destination.y != 0){
				canvas.drawLine(this.getLocation().x + RenderEngine.OFFSET, this.getLocation().y, destination.x + RenderEngine.OFFSET, destination.y, Sprite.debugPaint);
			}
		}
	}
	
	public boolean isPonyAtLocation(int x, int y){
		x = x - RenderEngine.OFFSET;
		Rect ponyBox = new Rect(this.getLocation().x, 
								this.getLocation().y, 
								this.getLocation().x + this.window.getSpriteWidth(), 
								this.getLocation().y + this.window.getSpriteHeight());
		return ponyBox.contains(x, y);
	}
	
	/**
	 * Checks if location is on the wallpaper
	 * @param location
	 * @return
	 */
	public boolean isPonyOnWallpaper(Point location) {
		List<Point> points = new LinkedList<Point>();
		points.add(location);
		points.add(new Point(location.x + this.window.getSpriteWidth(), location.y + this.window.getSpriteHeight()));
		points.add(new Point(location.x + this.window.getSpriteWidth(), location.y));
		points.add(new Point(location.x, location.y + this.window.getSpriteHeight()));

		for (Point point : points) {				
			if (RenderEngine.screenBounds.contains(point.x, point.y) == false) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks if the Pony is currently visible on the screen
	 * @param location Location to check
	 * @return True if visible
	 */
	public boolean isPonyOnScreen(Point location){
		try{
			List<Point> points = new LinkedList<Point>();
			points.add(new Point(location.x + RenderEngine.OFFSET, location.y));
			points.add(new Point(location.x + this.window.getSpriteWidth() + RenderEngine.OFFSET, location.y + this.window.getSpriteHeight()));
			points.add(new Point(location.x + this.window.getSpriteWidth() + RenderEngine.OFFSET, location.y));
			points.add(new Point(location.x + RenderEngine.OFFSET, location.y + this.window.getSpriteHeight()));
	
			for (Point point : points) {
				if (RenderEngine.visibleScreenArea.contains(point.x, point.y) == true) {
					return true;
				}
			}
			return false;
		}catch(Exception e){
			return true;
		}
	}
	
	public boolean isPonyInAvoidanceArea(Point location){
		return false;
	}
	
	public void addBehavior(String name, double chance_of_occurance, double max_duration,  double min_duration,  double speed,
            String right_image_path, String left_image_path, AllowedMoves Allowed_Moves,
            String _Linked_Behavior, boolean _skip,
            int _xcoord, int _ycoord, String _object_to_follow) throws IOException {
		
		// Create a new behavior structure
		Behavior new_behavior = new Behavior();
		
		// Set its values
       new_behavior.name = name.trim();
       new_behavior.pony_name = this.name;
       new_behavior.chance_of_occurance = chance_of_occurance;
       new_behavior.maxDuration = max_duration;
       new_behavior.minDuration = min_duration;
       new_behavior.speed = speed;
       new_behavior.Allowed_Movement = Allowed_Moves;

       new_behavior.Skip = _skip;
       
       new_behavior.destination_xcoord = _xcoord;
       new_behavior.destination_ycoord = _ycoord; 
       new_behavior.follow_object_name = _object_to_follow;
       
       if (_Linked_Behavior != null && _Linked_Behavior.length() > 0) {
    	   new_behavior.linkedBehaviorName = _Linked_Behavior;
       }
		
       new_behavior.image_right_path = right_image_path;
       new_behavior.image_left_path = left_image_path;
			
       // Add this new behavior to the list
       behaviors.add(new_behavior);			
		       
	}
	
	public Behavior getAppropriateBehavior(AllowedMoves movement, boolean speed, Behavior specified_behavior) {
		int selected_behavior_speed = 0;
		Behavior selected_behavior = currentBehavior;

		//does the current behavior work?  /use current behavior images when following
		if ((movement == AllowedMoves.None && (currentBehavior.Allowed_Movement == AllowedMoves.None || currentBehavior.Allowed_Movement == AllowedMoves.MouseOver)) ||
				(movement == AllowedMoves.Diagonal_Only && (currentBehavior.Allowed_Movement == AllowedMoves.Diagonal_Horizontal || currentBehavior.Allowed_Movement == AllowedMoves.Diagonal_Only || currentBehavior.Allowed_Movement == AllowedMoves.Diagonal_Vertical)) ||
				(movement == AllowedMoves.Vertical_Only && (currentBehavior.Allowed_Movement == AllowedMoves.Diagonal_Vertical || currentBehavior.Allowed_Movement == AllowedMoves.Horizontal_Vertical || currentBehavior.Allowed_Movement == AllowedMoves.Vertical_Only)) ||
				(movement == AllowedMoves.Horizontal_Only && (currentBehavior.Allowed_Movement == AllowedMoves.Diagonal_Horizontal || currentBehavior.Allowed_Movement == AllowedMoves.Horizontal_Vertical || currentBehavior.Allowed_Movement == AllowedMoves.Horizontal_Only)) ||
				(movement == currentBehavior.Allowed_Movement) ||
				(movement == AllowedMoves.All)
				) {
			if (currentBehavior.speed == 0 && movement == AllowedMoves.None)
				return currentBehavior;
			
			if (currentBehavior.speed != 0 && movement == AllowedMoves.All)
				return currentBehavior;
		}
		
		// Loop through the list of behaviors
		for (Behavior behavior : behaviors) {
			if ((movement == AllowedMoves.None && (behavior.Allowed_Movement == AllowedMoves.None || behavior.Allowed_Movement == AllowedMoves.MouseOver)) ||
				(movement == AllowedMoves.Diagonal_Only && (behavior.Allowed_Movement == AllowedMoves.Diagonal_Horizontal || behavior.Allowed_Movement == AllowedMoves.Diagonal_Only || behavior.Allowed_Movement == AllowedMoves.Diagonal_Vertical)) ||
				(movement == AllowedMoves.Vertical_Only && (behavior.Allowed_Movement == AllowedMoves.Diagonal_Vertical || behavior.Allowed_Movement == AllowedMoves.Horizontal_Vertical || behavior.Allowed_Movement == AllowedMoves.Vertical_Only)) ||
				(movement == AllowedMoves.Horizontal_Only && (behavior.Allowed_Movement == AllowedMoves.Diagonal_Horizontal || behavior.Allowed_Movement == AllowedMoves.Horizontal_Vertical || behavior.Allowed_Movement == AllowedMoves.Horizontal_Only)) ||
				(movement == behavior.Allowed_Movement) ||
				(movement == AllowedMoves.All)
				) {
				if (behavior.Skip == false) {
					if (behavior.speed == 0 && movement != AllowedMoves.All) {
						if (this.window.ponyDirection == true)
							behavior.right = true;
						else
							behavior.right = false;
						
						return behavior;
					} else {
						// see if the specified behavior works. If not, we'll find another.
						if (specified_behavior != null) {
							if ((movement == AllowedMoves.None && (specified_behavior.Allowed_Movement == AllowedMoves.None || specified_behavior.Allowed_Movement == AllowedMoves.MouseOver)) ||
								(movement == AllowedMoves.Diagonal_Only && (specified_behavior.Allowed_Movement == AllowedMoves.Diagonal_Horizontal || specified_behavior.Allowed_Movement == AllowedMoves.Diagonal_Only || specified_behavior.Allowed_Movement == AllowedMoves.Diagonal_Vertical)) ||
								(movement == AllowedMoves.Vertical_Only && (specified_behavior.Allowed_Movement == AllowedMoves.Diagonal_Vertical || specified_behavior.Allowed_Movement == AllowedMoves.Horizontal_Vertical || specified_behavior.Allowed_Movement == AllowedMoves.Vertical_Only)) ||
								(movement == AllowedMoves.Horizontal_Only && (specified_behavior.Allowed_Movement == AllowedMoves.Diagonal_Horizontal || specified_behavior.Allowed_Movement == AllowedMoves.Horizontal_Vertical || specified_behavior.Allowed_Movement == AllowedMoves.Horizontal_Only)) ||
								(movement == specified_behavior.Allowed_Movement) ||
								(movement == AllowedMoves.All)
								) {
								if (destination.x == 0 && destination.y == 0) {
									if (this.window.ponyDirection == true)
										specified_behavior.right = true;
									else
										specified_behavior.right = false;
								} else {
									if (getDestinationDirection(destination).get(0) == Direction.right)
										specified_behavior.right = true;
									else
										specified_behavior.right = false;
								}
								
								return specified_behavior;
							}
						}
						
						if (speed) {
							// Find the behavior with the greatest speed
							if (Math.abs(behavior.speed) > selected_behavior_speed) {
								selected_behavior = behavior;
								selected_behavior_speed = (int) Math.abs(behavior.speed);
							}
						} else {
							// Otherwise find the behavior with the smallest speed
							if (Math.abs(behavior.speed) < selected_behavior_speed || selected_behavior_speed == 0) {
								selected_behavior = behavior;
								selected_behavior_speed = (int) Math.abs(behavior.speed);
							}
						}
					}
				}
			}
		}
		
		// Return the behavior we found
		return selected_behavior;
	}
	
	private List<Direction> getDestinationDirection(Point destination) {
		List<Direction> direction = new LinkedList<Direction>();
		
		// Do we need to go left or right?
		if (destination.x - this.getLocation().x <= 0)
			direction.add(Direction.left);
		else
			direction.add(Direction.right);
		
		// Do we need to go up or down?
		if (destination.y - this.getLocation().y <= 0)
			direction.add(Direction.top);
		else
			direction.add(Direction.bottom);
		
		return direction;
	}
	
//	public Behavior getCurrentBehavior(){
//		if(cachedBehavior != null) return cachedBehavior;
//		return currentBehavior;
//	}
	
	public void selectBehavior(Behavior specifiedBehavior, long globalTime) {
		selectBehavior(specifiedBehavior, globalTime, false);
	}
	
	public void selectBehavior(Behavior specifiedBehavior, final long globalTime, boolean preventPreload) {		
		if (isInteracting && specifiedBehavior == null) cancelInteraction(globalTime);
		long startTime = SystemClock.elapsedRealtime();
		Behavior newBehavior = null;		
		
		double dice;
		
		int selection = 0;
		
		// Are we being forced into a specific behavior or not?
		if (specifiedBehavior == null) {
			int loop_total = 0;
			
			// Randomly select a non-skip behavior
			while(loop_total <= 200) {
				dice = MyLittleWallpaperService.rand.nextDouble();
				
				selection = MyLittleWallpaperService.rand.nextInt(behaviors.size());
				if (dice <= behaviors.get(selection).chance_of_occurance && behaviors.get(selection).Skip == false) {
					behaviors.get(selection).follow_object = null;
		
	                destination = behaviors.get(selection).getDestination(this);
	
	                if (!(destination.x == 0 && destination.y == 0 && behaviors.get(selection).follow_object_name.length() > 0)) {
	                	newBehavior = behaviors.get(selection);
						break;
	                }
				}
				loop_total++;
			}
			
			if (loop_total > 200) {
				// If the Random number generator is being goofy, select the default behavior (usually standing)
				newBehavior = behaviors.get(0);
			}				
		} else { // Set the forced behavior that was specified
			destination = specifiedBehavior.getDestination(this);

			if (destination.x == 0 && destination.y == 0 && specifiedBehavior.follow_object_name.length() > 0) {
				selectBehavior(null, globalTime);
				return;
			}
			newBehavior = specifiedBehavior;
			newBehavior.follow_object = null;
		}
		
		
		for (Effect effect : newBehavior.effects) {
			effect.already_played_for_currentbehavior = false;
		}
		
		// Set the time this behavior will last
		newBehavior.endTime = (globalTime + Math.round(MyLittleWallpaperService.rand.nextDouble() * (newBehavior.maxDuration - newBehavior.minDuration) * 1000 + (newBehavior.minDuration * 1000)));
	    
		// Select facing (left or right)
		dice = MyLittleWallpaperService.rand.nextDouble();
		if (dice >= 0.5)
			newBehavior.right = true;
		else
			newBehavior.right = false;
			
	    // If we aren't moving anywhere, stop here
	    if (newBehavior.Allowed_Movement == AllowedMoves.None || 
	    		newBehavior.Allowed_Movement == AllowedMoves.MouseOver ||
	    		newBehavior.Allowed_Movement == AllowedMoves.Sleep) {
	    	newBehavior.horizontal = false;
	    	newBehavior.vertical = false;
	    } else { // Otherwise, randomly select the movement direction based on where we're allowed to move    
		    List<AllowedMoves> modes = new LinkedList<AllowedMoves>();
		    if (newBehavior.Allowed_Movement == AllowedMoves.All ||
		    		newBehavior.Allowed_Movement == AllowedMoves.Diagonal_Vertical ||
		    		newBehavior.Allowed_Movement == AllowedMoves.Horizontal_Vertical ||
		    		newBehavior.Allowed_Movement == AllowedMoves.Vertical_Only) {
		    	modes.add(AllowedMoves.Vertical_Only);
		    }
		    if (newBehavior.Allowed_Movement == AllowedMoves.All ||
		    		newBehavior.Allowed_Movement == AllowedMoves.Diagonal_Vertical ||
		    		newBehavior.Allowed_Movement == AllowedMoves.Diagonal_Horizontal ||
		    		newBehavior.Allowed_Movement == AllowedMoves.Diagonal_Only) {
		    	modes.add(AllowedMoves.Diagonal_Only);
		    }
		    if (newBehavior.Allowed_Movement == AllowedMoves.All ||
		    		newBehavior.Allowed_Movement == AllowedMoves.Diagonal_Horizontal ||
		    		newBehavior.Allowed_Movement == AllowedMoves.Horizontal_Only ||
		    		newBehavior.Allowed_Movement == AllowedMoves.Horizontal_Vertical) {
		    	modes.add(AllowedMoves.Horizontal_Only);
		    }
		    
		    if (modes.size() == 0) {
		    	System.out.println("Unhandled movement type in SelectBehavior()");
		    	return;
		    }
		    
		    selection = MyLittleWallpaperService.rand.nextInt(modes.size());
		    AllowedMoves selected_mode = modes.get(selection);
		    
		    switch(selected_mode) {
		    	case Vertical_Only:
		    		newBehavior.horizontal = false;
		    		newBehavior.vertical = true;
		    		break;
		    	case Diagonal_Only:
		    		newBehavior.horizontal = true;
		    		newBehavior.vertical = true;
		    		break;
		    	case Horizontal_Only:
		    		newBehavior.horizontal = true;
		    		newBehavior.vertical = false;
		    		break;
		    }
		    
		    dice = MyLittleWallpaperService.rand.nextDouble();
		    
		    if (dice >= 0.5)
		    	newBehavior.up = true;
		    else
		    	newBehavior.up = false;
	    } // End if moving

		newBehavior.keep = false;
		
	    long timeNeeded = SystemClock.elapsedRealtime() - startTime;
	    
		if(RenderEngine.CONFIG_SHOW_EFFECTS && preventPreload == false && newBehavior.willPlayEffect(globalTime)){
			// The next behavior needs an effect so we will preload the effect
			nextBehavior = newBehavior;
			if(currentBehavior != null) currentBehavior.keep = true;
			if(currentlyLoadingEffects == false){
	    		Thread t = new Thread(new Runnable() {					
					@Override
					public void run() {
						currentlyLoadingEffects = true;
						long effectLoadStartTime = SystemClock.elapsedRealtime();
						try{
					        nextBehavior.preloadImages();
					    	List<EffectWindow> newEffects = nextBehavior.getPreloadedEffects(globalTime, Pony.this);
							for(EffectWindow newEffect : newEffects){
						        newEffect.setLocation(ToolSet.getEffectLocation(newEffect, newEffect.direction, window, newEffect.centering));
						        newEffect.endTime += SystemClock.elapsedRealtime() - effectLoadStartTime;
						        activeEffects.add(newEffect);	
							}
						}catch(Exception e){
							e.printStackTrace();
						}finally{
							nextBehavior.endTime += SystemClock.elapsedRealtime() - effectLoadStartTime;
					        currentBehavior.destroy();
							currentBehavior = nextBehavior;
					        currentBehavior.keep = false;
					        //nextBehavior.destroy();
					        nextBehavior = null;
							currentlyLoadingEffects = false;
						}
					}
				});
	    		t.start();
	    	}
		}else{
			currentBehavior = newBehavior;	
		}
//			Log.i("Pony[" + name + "]", "Found next (effect) Behavior after " + timeNeeded + " ms. Will use \"" + nextBehavior.name + "\" for " + Math.round((nextBehavior.endTime - SystemClock.elapsedRealtime()) / 1000) + " sec");
//		}else{ // No Effects to load
//			// Load Behavior for the first time
//			if(preventPreload || currentBehavior == null){
//				if(currentBehavior != null && newBehavior != null && (newBehavior.equals(currentBehavior) == false)){
//					if(MyLittleWallpaperService.DEBUG) Log.i("Pony[" + name + "]", "swaping from " + currentBehavior.name + " to " + newBehavior.name);
//					currentBehavior.destroy();
//					currentBehavior = null;
//				}
//				long loadingStartTime = SystemClock.elapsedRealtime();
//				newBehavior.preloadImages();
//				newBehavior.endTime += SystemClock.elapsedRealtime() - loadingStartTime;
//				currentBehavior = newBehavior;		
//			}else{
//				if(currentBehavior != null) currentBehavior.keep = true;
//				nextBehavior = newBehavior;
//				Thread t = new Thread(new Runnable() {					
//					@Override
//					public void run() {
//						long loadingStartTime = SystemClock.elapsedRealtime();
//						nextBehavior.preloadImages();
//						nextBehavior.endTime += SystemClock.elapsedRealtime() - loadingStartTime;
//						// Clean up the old Behavior
//						if(currentBehavior != null && nextBehavior != null && (nextBehavior.equals(currentBehavior) == false)){
//							currentBehavior.destroy();
//							currentBehavior = null;
//						}
//						currentBehavior = nextBehavior;
//						currentBehavior.keep = false;
//						nextBehavior = null;
//					}
//				});
//				t.start();
//			}
//		}
	    
		if(MyLittleWallpaperService.DEBUG){
			if(nextBehavior == null)
				Log.i("Pony[" + name + "]", "Found new Behavior after " + timeNeeded + " ms. Using \"" + currentBehavior.name + "\" for " + Math.round((currentBehavior.endTime - SystemClock.elapsedRealtime()) / 1000) + " sec");
			else
				Log.i("Pony[" + name + "]", "Found next Behavior after " + timeNeeded + " ms. Will use \"" + nextBehavior.name + "\" for " + Math.round((nextBehavior.endTime - SystemClock.elapsedRealtime()) / 1000) + " sec");
		}
	}

	
	public void setDestination(int x, int y){
		this.destination = new Point(x, y);
		getAppropriateBehavior(AllowedMoves.All, true, null);
	}
	
	public void cancelInteraction(long globalTime) {
		isInteracting = false;
		
		if (currentInteraction != null) {
			if (this.isInteractionInitiator) {
				for(Pony pony : currentInteraction.interactsWith) {
					pony.cancelInteraction(globalTime);
				}
			}
		
			interactionDelayUntil = globalTime + (currentInteraction.Reactivation_Delay * 1000);
			
			currentInteraction = null;
			isInteractionInitiator = false;
		}
	}
		
	public void linkBehaviors(){
		for(Behavior b : behaviors){
			if(b.linkedBehaviorName == null)
				continue;
			
			for(Behavior links : behaviors){
				if(links.name.equals(b.linkedBehaviorName)){
					b.linkedBehavior = links;
					break;
				}
			}
		}
	}	
	
	public void cleanUp(){
		for(Behavior b : behaviors)
			b.destroy();
	}

	public static Pony fromFile(File localFolder, boolean onlyName){
		int effectCount = 0;
    	Pony newPony = new Pony(null);
    	try{
		    String line = "";
		    File iniFile = new File(localFolder, "pony.ini");
		    if(iniFile.exists() == false)
			    iniFile = new File(localFolder, "Pony.ini");
		    BufferedReader br = null;
		    InputStreamReader is = new InputStreamReader(new FileInputStream(iniFile), "UTF-8");
		    if(is.read() == 0x0fffd){		    	
		    	br = new BufferedReader(new InputStreamReader(new FileInputStream(iniFile), "UTF-16"));
		    	Log.i("Pony", "opening " + iniFile.getPath() + " with UTF-16");
		    } else {
		    	br = new BufferedReader(new InputStreamReader(new FileInputStream(iniFile), "UTF-8"));
		    	Log.i("Pony", "opening " + iniFile.getPath() + " with UTF-8");
		    }
		    is.close();
		    while ((line = br.readLine()) != null) {	
			           if(line.startsWith("'")) continue; //skip comments
			           if(line.toLowerCase().startsWith("name,")){ newPony = new Pony(line.substring("name,".length())); if(onlyName) { return newPony; } continue;}
			           if(line.toLowerCase().startsWith("behavior")){
				           	String[] columns = ToolSet.splitWithQualifiers(line, ",", "\"");
				           	
				           	AllowedMoves movement = AllowedMoves.None;
				           	String linked_behavior = "";
							int xcoord = 0;
							int ycoord = 0;
							String follow = "";
							boolean skip = false;							
				           	
							if (columns[BO_movement_type].trim().equalsIgnoreCase("none")) {
								movement = AllowedMoves.None;
							} else if (columns[BO_movement_type].trim().equalsIgnoreCase("horizontal_only")) {
								movement = AllowedMoves.Horizontal_Only;
							} else if (columns[BO_movement_type].trim().equalsIgnoreCase("vertical_only")) {
								movement = AllowedMoves.Vertical_Only;
							} else if (columns[BO_movement_type].trim().equalsIgnoreCase("horizontal_vertical")) {
								movement = AllowedMoves.Horizontal_Vertical;
							} else if (columns[BO_movement_type].trim().equalsIgnoreCase("diagonal_only")) {
								movement = AllowedMoves.Diagonal_Only;
							} else if (columns[BO_movement_type].trim().equalsIgnoreCase("diagonal_horizontal")) {
								movement = AllowedMoves.Diagonal_Horizontal;
							} else if (columns[BO_movement_type].trim().equalsIgnoreCase("diagonal_vertical")) {
								movement = AllowedMoves.Diagonal_Vertical;
							} else if (columns[BO_movement_type].trim().equalsIgnoreCase("all")) {
								movement = AllowedMoves.All;
							} else if (columns[BO_movement_type].trim().equalsIgnoreCase("mouseover")) {
								movement = AllowedMoves.MouseOver;
							} else if (columns[BO_movement_type].trim().equalsIgnoreCase("sleep")) {
								movement = AllowedMoves.Sleep;
							}
														
							if (columns.length > BO_linked_behavior) {
								linked_behavior = columns[BO_linked_behavior].trim();
								skip = Boolean.parseBoolean(columns[BO_skip].trim());
								xcoord = Integer.parseInt(columns[BO_xcoord].trim());
								ycoord = Integer.parseInt(columns[BO_ycoord].trim());
								follow = columns[BO_object_to_follow].trim();
							}
							
				            newPony.addBehavior(
				            		columns[BO_name], 
				            		Double.parseDouble(columns[BO_probability]), 
				            		Double.parseDouble(columns[BO_max_duration]), 
				            		Double.parseDouble(columns[BO_min_duration]),
				            		Double.parseDouble(columns[BO_speed]),
				            		localFolder.getPath() + "/" + columns[BO_right_image_path].trim(), 
				            		localFolder.getPath() + "/" + columns[BO_left_image_path].trim(), 
				            		movement, 
				            		linked_behavior, 
				            		skip, 
				            		xcoord, 
				            		ycoord,
				            		follow);
				            newPony.linkBehaviors();
				            continue;
			           } // Behavior
			           if(line.toLowerCase().startsWith("effect")){
							String[] columns = ToolSet.splitWithQualifiers(line, ",", "\"");							
							boolean found_behavior = false;
							
							// Try to find the behavior to associate with
							for (Behavior behavior : newPony.behaviors) {
								if (behavior.name.equalsIgnoreCase(columns[EF_behavior_name].replace('"', ' ').trim())) {
									Direction direction_right = Direction.center;
									Direction centering_right = Direction.center;
									Direction direction_left = Direction.center;
									Direction centering_left = Direction.center;
									
									try {
										direction_right = ToolSet.getDirection(columns[EF_location_right]);
										centering_right = ToolSet.getDirection(columns[EF_center_right]);
										direction_left = ToolSet.getDirection(columns[EF_location_left]);
										centering_left = ToolSet.getDirection(columns[EF_center_left]);
									} catch (Exception ex) {
										// Debug output
										System.out.println("Invalid placement direction or centering for effect " + columns[EF_effect_name] + " for pony " + newPony.name + ":\n" + line);
									}
																		
							        // This is where we load the animation image
									String rightimage = localFolder.getPath() + "/" + columns[EF_right_image].trim();
									String leftimage = localFolder.getPath() + "/" + columns[EF_left_image].trim();									
									// Add the effect to the behavior if the image loaded correctly
									behavior.addEffect(columns[EF_effect_name].replace('"', ' ').trim(), rightimage, leftimage, Double.parseDouble(columns[EF_duration].trim()), Double.parseDouble(columns[EF_delay_before_next].trim()), direction_right, centering_right, direction_left, centering_left, Boolean.parseBoolean(columns[EF_follow].trim()));
									effectCount++;
									found_behavior = true;
									break;
								}
							}
							if (!found_behavior) {
								// Debug output
								System.out.println("Could not find behavior for effect " + columns[1] + " for pony " + newPony.name + ":\n" + line);
							}
			           } // Effect
		    		
		    	}
	        br.close();
	        Log.i("Pony[" + newPony.name + "]", "Loaded with " + newPony.behaviors.size() + " Behavior(s) and " + effectCount + " Effect(s)");
		  	
    	}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return newPony;
    }
	
    public static Pony fromFile(File localFolder){
    	return Pony.fromFile(localFolder, false);
    }
}
