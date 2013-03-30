package ca.wacos;

import java.io.File;
import java.util.LinkedHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This is the main class for the NametagEdit server plugin.
 * 
 * @author Levi Webb
 *
 */
public class NametagEdit extends JavaPlugin {
	
	static LinkedHashMap<String, LinkedHashMap<String, String>> groups = null;
	static LinkedHashMap<String, LinkedHashMap<String, String>> config = null;
	
	static boolean tabListEnabled = false;
	static boolean deathMessageEnabled = false;
	
	static NametagEdit plugin = null;
	
	/**
	 * Called when the plugin is loaded, registering command executors and event handlers, intializes the {@link ScoreboardManager} class, and loads plugin information.
	 * @see #load()
	 */
	public void onEnable() {
		plugin = this;
		ScoreboardManager.load();
		this.getServer().getPluginManager().registerEvents(new NametagEventHandler(), this);
		getCommand("ne").setExecutor(new NametagCommand());
		load();
	}
	/**
	 * Loads groups, players, configurations, and refreshes information for in-game players.
	 */
	public void load() {
		PluginVersion v = Updater.getVersion();
		if (v.isSnapshot()) {
			NametagUtils.box(new String[] { "This is a development plugin build.",
					"", "Download the latest table version by",
					"running '/ne update stable' in the server.",
					"", "Current build: SNAPSHOT " + v.getBuild()  + " v" + v.getVersion()}, "NametagEdit INFO");
		}
		
		groups = GroupLoader.load(this);
		
		config = ConfigLoader.load(this);

		NametagEdit.tabListEnabled = ConfigLoader.parseBoolean("tab-list-mask", "enabled", config, false);
		NametagEdit.deathMessageEnabled = ConfigLoader.parseBoolean("death-message-mask", "enabled", config, false);
		
		LinkedHashMap<String, LinkedHashMap<String, String>> players = PlayerLoader.load(this);
		Player[] onlinePlayers = Bukkit.getOnlinePlayers();
		for (Player p : onlinePlayers) {
			
			ScoreboardManager.clear(p.getName());
			
			boolean setGroup = true;
			
			for (String key : players.keySet().toArray(new String[players.keySet().size()])) {
				if (p.getName().equals(key)) {

					String prefix = players.get(key).get("prefix");
					String suffix = players.get(key).get("suffix");
					if (prefix != null)
						prefix = NametagUtils.formatColors(prefix);
					if (suffix != null)
						suffix = NametagUtils.formatColors(suffix);
					ScoreboardManager.overlap(p.getName(), prefix, suffix);
					
					setGroup = false;
				}
			}
			if (setGroup) {
				for (String key : groups.keySet().toArray(new String[groups.keySet().size()])) {
					if (p.hasPermission(key)) {
						String prefix = groups.get(key).get("prefix");
						String suffix = groups.get(key).get("suffix");
						if (prefix != null)
							prefix = NametagUtils.formatColors(prefix);
						if (suffix != null)
							suffix = NametagUtils.formatColors(suffix);
						ScoreboardManager.overlap(p.getName(), prefix, suffix);
						
						break;
					}
				}
			}
			if (NametagEdit.tabListEnabled) {
				String str = "§f" + p.getName();
				String tab = "";
				for (int t = 0; t < str.length() && t < 16; t++)
					tab += str.charAt(t);
				p.setPlayerListName(tab);
			}
			else {
				p.setPlayerListName(p.getName());
			}
		}
	}
	File getPluginFile() {
		return getFile();
	}
	
}
