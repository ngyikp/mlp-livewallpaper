package com.overkill.live.pony;

import java.util.List;
import java.util.LinkedList;

public class Interaction {
	public String Name;
	public String PonyName;
	public double Probability;
	public double Proximity_Activation_Distance = 125;
	public List<String> Targets = new LinkedList<String>();
	public String Targets_String = "";
	
	public boolean Select_All_Targets;
	public List<Behavior> Behavior_List = new LinkedList<Behavior>();
	
	public List<Pony> Interacts_With = new LinkedList<Pony>();
	public List<String> Interacts_With_Names = new LinkedList<String>();
	
	public Pony Trigger = null;
	public Pony initiator = null;
	
	// seconds
	public int Reactivation_Delay = 60;
	
	public String getBehaviors() {
		if (Behavior_List.size() > 0) {
			String behaviors_list = "";
			
			for (Behavior behavior : Behavior_List) {
				behaviors_list = behaviors_list + behavior.name + ",";
			}
			
			return behaviors_list.substring(0, behaviors_list.length() - 2);
		}
		
		return "";
	}
}

