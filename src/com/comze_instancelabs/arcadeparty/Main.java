package com.comze_instancelabs.arcadeparty;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import net.minecraft.server.v1_7_R1.EntityTypes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_7_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.comze_instancelabs.arcadeparty.api.MainAPI;

public class Main extends JavaPlugin implements Listener {

	// TODO
	// - scoreboard
	
	public static MainAPI api;
	public static String pos_prefix = ChatColor.GREEN + "[+] " + ChatColor.GOLD;
	public static String neg_prefix = ChatColor.RED + "[-] " + ChatColor.GOLD;
	public static String neutral_prefix = ChatColor.BLUE + "[~] " + ChatColor.GOLD;
	
	public static HashMap<String, ArrayList<String>> leadersp = new HashMap<String, ArrayList<String>>();
	public HashMap<String, IconMenu> p_iconm = new HashMap<String, IconMenu>();
	public HashMap<String, ArrayList<String>> gamesp = new HashMap<String, ArrayList<String>>();
	
	public void onEnable(){
		// enable API
		api = new MainAPI(this);
		
		// register listeners
		Bukkit.getPluginManager().registerEvents(this, this);
		
		// register npcs
		registerEntities();
		
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		
	}
	
	public MainAPI getAPI(){
		return this.api;
	}
	
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
    	if(cmd.getName().equalsIgnoreCase("party")){
    		if(args.length > 0){
    			if(!(sender instanceof Player)){
					sender.sendMessage(neg_prefix + "Please execute this command in a player context!");
					return true;
				}
    			Player p = (Player)sender;
    			if(args[0].equalsIgnoreCase("join")){
    				if(args.length > 1){
    					if(!isPartyMember(p.getName())){
    						joinParty(p.getName(), args[1]);
    					}else{
    						p.sendMessage(neg_prefix + "You are already in " + p.getName() + "'s party.");
    					}
    				}else{
    					sender.sendMessage(neg_prefix + "Usage: /party join <name>");
    				}
    				return true;
    			}else if(args[0].equalsIgnoreCase("list")){
    				if(isPartyLeader(p.getName())){
    					for(String p_ : getPartyMembers(p.getName())){
    						p.sendMessage(ChatColor.AQUA + p_);
    					}
    				}
    				return true;
    			}else if(args[0].equalsIgnoreCase("start")){
    				if(isPartyLeader(p.getName())){
    					startParty(this, p.getName());
    				}
    				return true;
    			}
    			
    			String player = args[0];
    			if(Bukkit.getPlayer(player) != null){
    				if(!player.equalsIgnoreCase(p.getName())){
    					if(isPartyLeader(p.getName())){
    						sendInvite(p.getName(), player);
    					}else{
    						createParty(p.getName());
    						sendInvite(p.getName(), player);
    					}
    				}else{
        				sender.sendMessage(neg_prefix + "You can't create a party with yourself.");
    				}
    			}else{
    				sender.sendMessage(neg_prefix + "This player is not online!");
    			}
    		}else{
    			sender.sendMessage(neutral_prefix + "/party [name], to create a new party with a friend.");
    			sender.sendMessage(neutral_prefix + "/party join [name], to join an invitation.");
    			sender.sendMessage(neutral_prefix + "/party list, to list all party members.");
    			sender.sendMessage(neutral_prefix + "/party start, to start a party.");
    			sender.sendMessage(neutral_prefix + "Don't forget to add some games to your party by rightclicking the sheep.");
    		}
    		return true;
    	}else if(cmd.getName().equalsIgnoreCase("arcade")){
    		if(args.length > 0){
    			if(!(sender instanceof Player)){
					sender.sendMessage(neg_prefix + "Please execute this command in a player context!");
					return true;
				}
    			Player p = (Player)sender;
    			if(args[0].equalsIgnoreCase("create")){
    				spawnSheep(p.getLocation());
    			}else if(args[0].equalsIgnoreCase("addserver")){
    				if(args.length > 2){
    					saveServer(args[1], args[2]);
    					if(args.length > 3){
    						setServerIcon(args[1], args[3]);
    					}
    				}else{
    					sender.sendMessage(neutral_prefix + "To add a new server, execute /arcade addserver [server] [description]. Optionally, you can also execute /arcade addserver [server] [description] [Material] to set a custom icon (the material).");
    				}
    			}else if(args[0].equalsIgnoreCase("removeserver")){
    				if(args.length > 1){
    					removeServer(args[1]);
    				}else{
    					sender.sendMessage(neutral_prefix + "To remove a server, execute /arcade removeserver [server].");
    				}
    			}
    		}else{
    			sender.sendMessage(neutral_prefix + "/arcade create, to create a new party sheep.");
    			sender.sendMessage(neutral_prefix + "/arcade addserver [server] [description], to add a new server to the sheep.");
    			sender.sendMessage(neutral_prefix + "/arcade addserver [server] [description] [material], to add a new server with a custom item (material) to the sheep.");
    			sender.sendMessage(neutral_prefix + "/arcade removeserver [server], to remove a server from the sheep.");
    		}
    		return true;
    	}
    	return false;
    }
    
    public static HashMap<String, String> getAllServers(Main m){
    	HashMap<String, String> ret = new HashMap<String, String>();
    	for(String k : m.getConfig().getKeys(false)){
    		if(!k.equalsIgnoreCase("config")){
    			ret.put(k, m.getConfig().getString(k + ".desc"));
    		}
    	}
    	return ret;
    }
    
    public void saveServer(String server, String name){
    	getConfig().set(server + ".desc", name);
    	this.saveConfig();
    }
    
    public void removeServer(String server){
    	getConfig().set(server, null);
    	this.saveConfig();
    }
    
    public void setServerIcon(String server, String material){
    	getConfig().set(server + ".icon", material);
    	this.saveConfig();
    }
    
    public static Material getServerIcon(Main m, String server){
    	return Material.getMaterial(m.getConfig().getString(server + ".icon"));
    }
    
    public static ArrayList<String> getPartyMembers(String p){
    	return leadersp.get(p);
    }
    
    public String getPartyLeader(String p){
    	for(String k : leadersp.keySet()){
    		if(leadersp.get(k).contains(p)){
    			return k;
    		}
    	}
    	return "";
    }
    
    public boolean isPartyMember(String p){
    	for(ArrayList<String> k : leadersp.values()){
    		if(k.contains(p)){
    			return true;
    		}
    	}
    	return false;
    }
    
    public boolean isPartyLeader(String p){
    	return leadersp.containsKey(p);
    }
    
    public void setParty(String p, String leader){
    	if(!isPartyLeader(leader)){
    		return;
    	}
    	ArrayList<String> members = leadersp.get(leader);
    	members.add(p);
    	leadersp.put(leader, members);
    }
    
    public void addPartyGame(String leader, String game){
    	if(!isPartyLeader(leader)){
    		return;
    	}
    	if(!gamesp.containsKey(leader)){
    		gamesp.put(leader, new ArrayList<String>(Arrays.asList(game)));
    		return;
    	}
    	ArrayList<String> games = gamesp.get(leader);
    	games.add(game);
    	gamesp.put(leader, games);
    }
    
    public static void startParty(final Main m, final String p){
    	if(!m.gamesp.containsKey(p)){
    		return;
    	}

    	Player p__ = Bukkit.getPlayer(p);

    	String games = "";
    	int count = 0;
		for(String g : m.gamesp.get(p)){
			games += g + ",";
			count++;
		}
		if(count < 1){
			p__.sendMessage(neutral_prefix + "You have zero games selected! Go to the Arcade Sheep to select some.");
		}
    	
    	ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);        
        try {
        	out.writeUTF("Forward"); 
            out.writeUTF("ALL");
        	out.writeUTF("info");
        	
        	String all = "";
			all += games.substring(0, games.length() - 1);
			
			String member = "";
			for(String me : m.getPartyMembers(p)){
				member += me + ",";
			}
			all += "#" + member.substring(0, member.length() - 1);
			all += "#0";
			
			m.getLogger().info(all);
			out.writeUTF(all);
		} catch (IOException e) {
			e.printStackTrace();
		}

        p__.sendPluginMessage(m, "BungeeCord", stream.toByteArray());  
        
        Bukkit.getScheduler().runTaskLater(m, new Runnable(){
        	public void run(){
        		for(String p_ : getPartyMembers(p)){
            		connectToServer(m, p_, m.gamesp.get(p).get(0));
                }
        	}
        }, 20L);
    }
    
    public static void connectToServer(Main m, String player, String server){
    	ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);
        try {
			out.writeUTF("Connect");
			out.writeUTF(server);
		} catch (IOException e) {
			e.printStackTrace();
		}
        Bukkit.getPlayer(player).sendPluginMessage(m, "BungeeCord", stream.toByteArray());
    }
    
    public void createParty(String leader){
    	Bukkit.getPlayer(leader).sendMessage(pos_prefix + "You just created a party!");
    	leadersp.put(leader, new ArrayList<String>(Arrays.asList(leader)));
    }
    
    public void joinParty(String p, String leader){
    	Player p_ = Bukkit.getPlayer(p);
    	if(isPartyLeader(leader)){
    		setParty(p, leader);
    		p_.sendMessage(pos_prefix + "You joined " + leader + "'s party!");
    		Bukkit.getPlayer(leader).sendMessage(pos_prefix + p + " joined your party!");
    	}else{
    		p_.sendMessage(neg_prefix + "The specified player did not create a party.");
    	}
    }
    
    public void disbandParty(){
    	//TODO disband party
    }
    
    public void sendInvite(String leader, String p){
    	Player p_ = Bukkit.getPlayer(p);
    	if(p_.isOnline()){
    		p_.sendMessage(pos_prefix + leader + " just invited you to join his party! Type /party join.");
    	}
    }
	
    
    public static boolean registerEntities(){
		try {
			Class entityTypeClass = EntityTypes.class;

			Field c = entityTypeClass.getDeclaredField("c");
			c.setAccessible(true);
			HashMap c_map = (HashMap) c.get(null);
			c_map.put("CSheep", CSheep.class);

			Field d = entityTypeClass.getDeclaredField("d");
			d.setAccessible(true);
			HashMap d_map = (HashMap) d.get(null);
			d_map.put(CSheep.class, "CSheep");

			Field e = entityTypeClass.getDeclaredField("e");
			e.setAccessible(true);
			HashMap e_map = (HashMap) e.get(null);
			e_map.put(Integer.valueOf(91), CSheep.class);

			Field f = entityTypeClass.getDeclaredField("f");
			f.setAccessible(true);
			HashMap f_map = (HashMap) f.get(null);
			f_map.put(CSheep.class, Integer.valueOf(91));

			Field g = entityTypeClass.getDeclaredField("g");
			g.setAccessible(true);
			HashMap g_map = (HashMap) g.get(null);
			g_map.put("CSheep", Integer.valueOf(91));

			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
    
    
    public CSheep spawnSheep( Location t) {
		Object w = ((CraftWorld) t.getWorld()).getHandle();
		CSheep t_ = new CSheep(t, (net.minecraft.server.v1_7_R1.World) ((CraftWorld) t.getWorld()).getHandle());
		((net.minecraft.server.v1_7_R1.World) w).addEntity(t_, CreatureSpawnEvent.SpawnReason.CUSTOM);
		t_.setCustomName("jeb_");
		t_.setCustomNameVisible(false);
		
		return t_;
	}
    
    public static void openMenu(final Main m, final String p) {
		IconMenu iconm = new IconMenu("-= Arcade =-", 36, new IconMenu.OptionClickEventHandler() {
			@Override
			public void onOptionClick(IconMenu.OptionClickEvent event) {
				String d = event.getName();
				if (d.equalsIgnoreCase("start")) {
					startParty(m, p);
				}else if (d.equalsIgnoreCase("clear")) {
					if(m.gamesp.containsKey(p)){
						m.gamesp.remove(p);
					}
				}else if (d.equalsIgnoreCase("close")) {
					//
				}else{
					m.addPartyGame(p, d);
					Bukkit.getPlayer(p).sendMessage(pos_prefix + "Added " + d + " to you party games.");
					Bukkit.getScheduler().runTaskLater(m, new Runnable(){
						public void run(){
							m.p_iconm.get(p).open(Bukkit.getPlayer(p));
						}
					}, 10L);					
				}
				event.setWillClose(true);
			}
		}, m)
		.setOption(0, new ItemStack(Material.SLIME_BALL, 1), "Clear", "Clear")
		.setOption(1, new ItemStack(Material.SLIME_BALL, 1), "Start", "Start")
		.setOption(8, new ItemStack(Material.MAGMA_CREAM, 1), "Close", "Close");
		//.setOption(9, new ItemStack(Material.DRAGON_EGG, 1), "test", "test")
		//.setOption(10, new ItemStack(Material.BOOKSHELF, 1), "MG2", "MG2");
		
		m.p_iconm.put(p, iconm);
		
		int count = 9;
		
		for(String k : getAllServers(m).keySet()){
			String[] f = new String[]{m.getConfig().getString(k + ".desc")};
			iconm.setOption(count, new ItemStack(getServerIcon(m, k)), k, f);
			count++;
		}

		iconm.open(Bukkit.getPlayerExact(p));
	}
    
	@EventHandler
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
		if(event.getRightClicked() instanceof Sheep){
			Sheep s = (Sheep)event.getRightClicked();
			if(s.getCustomName().equalsIgnoreCase("jeb_")){
				Player p = event.getPlayer();
				if(isPartyLeader(p.getName())){
					openMenu(this, p.getName());
				}else{
					p.sendMessage(neg_prefix + "Create a party first! /party <friend>");
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event){
		if(event.getEntity() instanceof Sheep){
			Sheep s = (Sheep)event.getEntity();
			s.setHealth(8D);
			event.setCancelled(true);
		}
	}
}
