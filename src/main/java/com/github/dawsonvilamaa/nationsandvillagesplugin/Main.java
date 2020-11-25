package com.github.dawsonvilamaa.nationsandvillagesplugin;

import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.Nation;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsPlayer;
import com.github.dawsonvilamaa.nationsandvillagesplugin.listeners.NationsVillagerListener;
import com.github.dawsonvilamaa.nationsandvillagesplugin.listeners.PlayerListener;
import com.github.dawsonvilamaa.nationsandvillagesplugin.listeners.WorldListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

public class Main extends JavaPlugin {
    private PlayerListener playerListener = new PlayerListener(this);
    private NationsVillagerListener nationsVillagerListener = new NationsVillagerListener(this);
    private WorldListener worldListener = new WorldListener(this);

    public static NationsManager nationsManager;

    @Override
    public void onEnable() {
        nationsManager = new NationsManager();

        //register commands
        NAVCommandExecutor commandExecutor = new NAVCommandExecutor(this);
        getCommand("nation").setExecutor(commandExecutor);

        //register events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(playerListener, this);
        pm.registerEvents(nationsVillagerListener, this);
        pm.registerEvents(worldListener, this);

        //create data folder if it doesn't exist
        File dir = new File("plugins\\NationsAndVillages");
        if (!dir.exists()) dir.mkdir();

        //read data
        JSONParser parser = new JSONParser();
        try {
            //load players
            JSONArray jsonPlayers = (JSONArray) parser.parse(new FileReader("plugins\\NationsAndVillages\\players.json"));
            Iterator<JSONObject> iterator = jsonPlayers.iterator();
            while (iterator.hasNext()) {
                NationsPlayer player = new NationsPlayer(iterator.next());
                nationsManager.getPlayers().put(player.getUUID(), player);
            }
            //load nations
            JSONArray jsonNations = (JSONArray) parser.parse(new FileReader("plugins\\NationsAndVillages\\nations.json"));
            iterator = jsonNations.iterator();
            while (iterator.hasNext())
                nationsManager.getNations().add(new Nation(iterator.next()));
        } catch(IOException e) {
            getLogger().info(e.getMessage());
        } catch(ParseException ex) {
            getLogger().info(ex.getMessage());
        }
    }

    @Override
    public void onDisable() {
        //save player data
        JSONArray jsonPlayers = new JSONArray();
        for (NationsPlayer player : nationsManager.getPlayers().values())
            jsonPlayers.add(player.toJSON());

        FileWriter playerFile = null;
        try {
            playerFile = new FileWriter("plugins\\NationsAndVillages\\players.json");
            playerFile.write(jsonPlayers.toJSONString());
        } catch(IOException e) {
            getLogger().info(e.getMessage());
        } finally {
            try {
                playerFile.flush();
                playerFile.close();
            } catch (IOException ex) {
                getLogger().info(ex.getMessage());
            }
        }

        //save nations data
        JSONArray jsonNations = new JSONArray();
        for (Nation nation : nationsManager.getNations())
            jsonNations.add(nation.toJSON());

        FileWriter nationsFile = null;
        try {
            nationsFile = new FileWriter("plugins\\NationsAndVillages\\nations.json");
            nationsFile.write(jsonNations.toJSONString());
        } catch (IOException e) {
            getLogger().info(e.getMessage());
        } finally {
            try {
                nationsFile.flush();
                nationsFile.close();
            } catch(IOException e) {
                getLogger().info(e.getMessage());
            }
        }

    }
}
