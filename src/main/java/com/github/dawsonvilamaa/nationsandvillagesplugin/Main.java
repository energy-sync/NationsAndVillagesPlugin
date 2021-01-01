package com.github.dawsonvilamaa.nationsandvillagesplugin;

import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.Nation;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsChunk;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsPlayer;
import com.github.dawsonvilamaa.nationsandvillagesplugin.classes.NationsVillager;
import com.github.dawsonvilamaa.nationsandvillagesplugin.listeners.NationsVillagerListener;
import com.github.dawsonvilamaa.nationsandvillagesplugin.listeners.PlayerListener;
import com.github.dawsonvilamaa.nationsandvillagesplugin.listeners.WorldListener;
import org.bukkit.Bukkit;
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
        getCommand("autoclaim").setExecutor(commandExecutor);
        getCommand("autounclaim").setExecutor(commandExecutor);
        getCommand("balance").setExecutor(commandExecutor);
        getCommand("claim").setExecutor(commandExecutor);
        getCommand("nation").setExecutor(commandExecutor);
        getCommand("unclaim").setExecutor(commandExecutor);

        //register events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(playerListener, this);
        pm.registerEvents(nationsVillagerListener, this);
        pm.registerEvents(worldListener, this);

        //create data folder if it doesn't exist
        File dir = new File("plugins\\NationsAndVillages");
        if (!dir.exists()) dir.mkdir();

        //load data
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
                nationsManager.addNation(new Nation(iterator.next()));

            //load villagers
            JSONArray jsonVillagers = (JSONArray) parser.parse(new FileReader("plugins\\NationsAndVillages\\villagers.json"));
            iterator = jsonVillagers.iterator();
            while (iterator.hasNext()) {
                NationsVillager nationsVillager = new NationsVillager(iterator.next());
                nationsManager.addVillager(nationsVillager.getEntity().getUniqueID(), nationsVillager);
            }

            //load chunks
            JSONArray jsonChunks = (JSONArray) parser.parse(new FileReader("plugins\\NationsAndVillages\\chunks.json"));
            iterator = jsonChunks.iterator();
            while (iterator.hasNext())
                nationsManager.getChunks().add(new NationsChunk(iterator.next()));

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
        for (Nation nation : nationsManager.getNations().values())
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

        //save villager data
        JSONArray jsonVillagers = new JSONArray();
        for (NationsVillager villager : nationsManager.getVillagers().values())
            jsonVillagers.add(villager.toJSON());

        FileWriter villagersFile = null;
        try {
            villagersFile = new FileWriter("plugins\\NationsAndVillages\\villagers.json");
            villagersFile.write(jsonVillagers.toJSONString());
        } catch (IOException e) {
            getLogger().info(e.getMessage());
        } finally {
            try {
                villagersFile.flush();
                villagersFile.close();
            } catch (IOException e) {
                getLogger().info(e.getMessage());
            }
        }

        //save chunk data
        JSONArray jsonChunks = new JSONArray();
        for (NationsChunk chunk : nationsManager.getChunks())
            jsonChunks.add(chunk.toJSON());

        FileWriter chunksFile = null;
        try {
            chunksFile = new FileWriter("plugins\\NationsAndVillages\\chunks.json");
            chunksFile.write(jsonChunks.toJSONString());
        } catch (IOException e) {
            getLogger().info(e.getMessage());
        } finally {
            try {
                chunksFile.flush();
                chunksFile.close();
            } catch(IOException e) {
                getLogger().info(e.getMessage());
            }
        }
    }
}
