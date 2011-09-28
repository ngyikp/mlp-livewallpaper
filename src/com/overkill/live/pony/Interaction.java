package com.overkill.live.pony;

import java.util.List;
import java.util.LinkedList;

public class Interaction {
	public String name;
	public String ponyName;
	public double probability;
	public double proximityActivationDistance = 125;
	public List<String> targets = new LinkedList<String>();
	public String targetsString = "";
	
	public boolean selectAllTargets;
	public List<Behavior> behaviorList = new LinkedList<Behavior>();
	
	public List<Pony> interactsWith = new LinkedList<Pony>();
	public List<String> interactsWithNames = new LinkedList<String>();
	
	public Pony trigger = null;
	public Pony initiator = null;
	
	// seconds
	public int Reactivation_Delay = 60;
	
	public String getBehaviors() {
		if (behaviorList.size() > 0) {
			String behaviors_list = "";
			
			for (Behavior behavior : behaviorList) {
				behaviors_list = behaviors_list + behavior.name + ",";
			}
			
			return behaviors_list.substring(0, behaviors_list.length() - 2);
		}
		
		return "";
	}
}

