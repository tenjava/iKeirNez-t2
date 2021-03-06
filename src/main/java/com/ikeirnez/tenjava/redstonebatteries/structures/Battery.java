package com.ikeirnez.tenjava.redstonebatteries.structures;

import com.ikeirnez.tenjava.redstonebatteries.configuration.Serialization;
import com.ikeirnez.tenjava.redstonebatteries.configuration.Setting;
import com.ikeirnez.tenjava.redstonebatteries.utilities.Cuboid;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.*;

import static com.ikeirnez.tenjava.redstonebatteries.utilities.Utils.getSubMap;

/**
 * Created by iKeirNez on 12/07/2014.
 */
public class Battery implements ConfigurationSerializable {

    private static final int BATTERY_SIZE_X = 3, BATTERY_SIZE_Z = 4, BATTERY_MIN_SIZE_Y = 5;
    private static final Material INPUT_BLOCK = Material.ENDER_STONE, OUTPUT_BLOCK = Material.LAPIS_BLOCK, CHARGED_NOTIFIER_BLOCK = Material.PISTON_STICKY_BASE;

    private final Cuboid cuboid;
    private final int size;
    private final int maxCharge;

    private int charge = 13;
    private int chargingCurrent = 0;

    private final List<Location> glassBlocksLocations = new ArrayList<>(), snowBlocksLocations = new ArrayList<>();
    private Location inputBlockLocation, outputBlockLocation, chargedNotifierBlockLocation;

    // 8 snow pile = 1 block

    public Battery(Cuboid cuboid, List<Location> glassBlocksLocations, List<Location> snowBlocksLocations, Location inputBlockLocation, Location outputBlockLocation, Location chargedNotifierBlockLocation){
        this.cuboid = cuboid;
        this.glassBlocksLocations.addAll(glassBlocksLocations);
        this.snowBlocksLocations.addAll(snowBlocksLocations);
        this.inputBlockLocation = inputBlockLocation;
        this.outputBlockLocation = outputBlockLocation;
        this.chargedNotifierBlockLocation = chargedNotifierBlockLocation;
        this.size = glassBlocksLocations.size();
        this.maxCharge = Setting.BATTERY__POWER_PER_LEVEL.intValue() * getSize();
        update();
    }

    public Battery(Map<String, Object> data){
        this.cuboid = new Cuboid(getSubMap(data, "cuboid"));

        Map<String, Object> locations = getSubMap(data, "locations");
        this.inputBlockLocation = Serialization.deserializeLocation(getSubMap(locations, "inputBlock"));
        this.outputBlockLocation = Serialization.deserializeLocation(getSubMap(locations, "outputBlock"));
        this.chargedNotifierBlockLocation = Serialization.deserializeLocation(getSubMap(locations, "chargedNotificationBlock"));

        Map<String, Object> glassBlocksMap = getSubMap(locations, "glassBlocks");
        for (String key : glassBlocksMap.keySet()){
            glassBlocksLocations.add(Serialization.deserializeLocation(getSubMap(glassBlocksMap, key)));
        }

        Map<String, Object> snowBlocksMap = getSubMap(locations, "snowBlocks");
        for (String key : snowBlocksMap.keySet()){
            snowBlocksLocations.add(Serialization.deserializeLocation(getSubMap(snowBlocksMap, key)));
        }

        this.charge = (int) data.get("charge");
        this.size = glassBlocksLocations.size();
        this.maxCharge = Setting.BATTERY__POWER_PER_LEVEL.intValue() * getSize();
        update();
    }

    public Cuboid getCuboid() {
        return cuboid;
    }

    public int getSize() {
        return size;
    }

    public int getMaxCharge() {
        return maxCharge;
    }

    public int getCharge() {
        return charge;
    }

    public void setCharge(int charge) {
        this.charge = charge;
    }

    public int getChargingCurrent() {
        return chargingCurrent;
    }

    public void setChargingCurrent(int chargingCurrent) {
        this.chargingCurrent = chargingCurrent;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("cuboid", getCuboid().serialize());
        data.put("charge", getCharge());

        Map<String, Object> locations = new HashMap<>();
        Map<String, Object> glassBlocks = new HashMap<>();
        Map<String, Object> snowBlocks = new HashMap<>();

        int i = 0;
        for (Location glassBlockLocation : glassBlocksLocations){
            glassBlocks.put(String.valueOf(i), Serialization.serializeLocation(glassBlockLocation));
        }

        i = 0;

        for (Location snowBlocksLocation : snowBlocksLocations){
            snowBlocks.put(String.valueOf(i), Serialization.serializeLocation(snowBlocksLocation));
        }

        locations.put("glassBlocks", glassBlocks);
        locations.put("snowBlocks", snowBlocks);
        locations.put("inputBlock", Serialization.serializeLocation(inputBlockLocation));
        locations.put("outputBlock", Serialization.serializeLocation(outputBlockLocation));
        locations.put("chargedNotificationBlock", Serialization.serializeLocation(chargedNotifierBlockLocation));
        data.put("locations", locations);
        return data;
    }

    private void update(){
        double blockPercentWorth = Math.round(100.0f / getSize());
        double percent = ((getCharge() * 100.0f) / getMaxCharge());

        int blocks = (int) Math.round(percent / blockPercentWorth);
        int snowLayers = (int) Math.round((percent % blockPercentWorth) / 8);


        for (int i = 0; i < blocks; i++){
            snowBlocksLocations.get(i).getBlock().setType(Material.SNOW_BLOCK);
        }

        if (blocks < snowBlocksLocations.size()){
            Block block = snowBlocksLocations.get(blocks).getBlock();
            block.setType(Material.SNOW_BLOCK);
            block.setData((byte) snowLayers);
        }
    }

    public static int getPercent(int value, int maxValue){
        return (int) ((value * 100.0f) / maxValue);
    }
}
