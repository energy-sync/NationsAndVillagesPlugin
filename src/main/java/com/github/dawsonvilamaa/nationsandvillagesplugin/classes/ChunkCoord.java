package com.github.dawsonvilamaa.nationsandvillagesplugin.classes;

import java.util.Objects;

public class ChunkCoord {
    private int x;
    private int z;
    private String world;
    private int hashCode;

    /**
     * @param x
     * @param z
     * @param world
     */
    public ChunkCoord(int x, int z, String world) {
        this.x = x;
        this.z = z;
        this.world = world;
        this.hashCode = Objects.hash(x, z, world);
    }

    /**
     * @return x
     */
    public int getX() {
        return x;
    }

    /**
     * @return z
     */
    public int getZ() {
        return z;
    }

    /**
     * @return world
     */
    public String getWorld() {
        return world;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChunkCoord) {
            ChunkCoord chunk = (ChunkCoord) obj;
            return chunk.x == x && chunk.z == z && chunk.world.equals(world);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
