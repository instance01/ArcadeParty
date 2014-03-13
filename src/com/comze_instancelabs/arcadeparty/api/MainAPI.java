package com.comze_instancelabs.arcadeparty.api;

import java.util.ArrayList;

import com.comze_instancelabs.arcadeparty.Main;

public class MainAPI {

	private static Main m;
	
	public MainAPI(Main m){
		this.m = m;
	}
	
	public static String getPartyLeader(String p){
    	return m.getPartyLeader(p);
    }
    
    public static boolean isPartyMember(String p){
    	return m.isPartyMember(p);
    }
    
    public static boolean isPartyLeader(String p){
    	return m.isPartyLeader(p);
    }
	
    public static ArrayList<String> getPartyMembers(String p){
    	return m.getPartyMembers(p);
    }
    
    public static void setParty(String p, String leader){
    	m.setParty(p, leader);
    }
}
