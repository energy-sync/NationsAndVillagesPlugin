package com.github.dawsonvilamaa.nationsandvillagesplugin.classes;

import com.github.dawsonvilamaa.nationsandvillagesplugin.NationsManager;
import org.json.simple.JSONObject;

public class NationsPermission {
    private boolean modifyBlocks;
    private boolean openContainers;
    private boolean attackEntities;
    private boolean claimLand;
    private boolean manageMembers;
    private boolean declareEnemies;

    /**
     * @param rank
     */
    public NationsPermission(NationsManager.Rank rank) {
        switch (rank) {
            case LEADER:
            case LEGATE:
                modifyBlocks = true;
                openContainers = true;
                attackEntities = true;
                claimLand = true;
                manageMembers = true;
                declareEnemies = true;
            break;

            case MEMBER:
                modifyBlocks = true;
                openContainers = true;
                attackEntities = true;
                claimLand = false;
                manageMembers = false;
                declareEnemies = false;
            break;

            case NONMEMBER:
                modifyBlocks = false;
                openContainers = false;
                attackEntities = false;
                claimLand = false;
                manageMembers = false;
                declareEnemies = false;
            break;
        }
    }

    /**
     * @param jsonPermission
     */
    public NationsPermission(JSONObject jsonPermission) {
        this.modifyBlocks = Boolean.valueOf(jsonPermission.get("modifyBlocks").toString());
        this.openContainers = Boolean.valueOf(jsonPermission.get("openContainers").toString());
        this.attackEntities = Boolean.valueOf(jsonPermission.get("attackEntities").toString());
        this.claimLand = Boolean.valueOf(jsonPermission.get("claimLand").toString());
        this.manageMembers = Boolean.valueOf(jsonPermission.get("manageMembers").toString());
        this.declareEnemies = Boolean.valueOf(jsonPermission.get("declareEnemies").toString());
    }

    /**
     * @return canModifyBlocks
     */
    public boolean canModifyBlocks() {
        return this.modifyBlocks;
    }

    /**
     * @param modifyBlocks
     */
    public void setModifyBlocks(boolean modifyBlocks) {
        this.modifyBlocks = modifyBlocks;
    }

    /**
     * @return canOpenContainers
     */
    public boolean canOpenContainers() {
        return this.openContainers;
    }

    /**
     * @param openContainers
     */
    public void setOpenContainers(boolean openContainers) {
        this.openContainers = openContainers;
    }

    /**
     * @return canAttackEntities
     */
    public boolean canAttackEntities() {
        return this.attackEntities;
    }

    /**
     * @param attackEntities
     */
    public void setAttackEntities(boolean attackEntities) {
        this.attackEntities = attackEntities;
    }

    /**
     * @return canClaimLand
     */
    public boolean canClaimLand() {
        return this.claimLand;
    }

    /**
     * @param claimLand
     */
    public void setClaimLand(boolean claimLand) {
        this.claimLand = claimLand;
    }

    /**
     * @return canManageMembers
     */
    public boolean canManageMembers() {
        return this.manageMembers;
    }

    /**
     * @param manageMembers
     */
    public void setManageMembers(boolean manageMembers) {
        this.manageMembers = manageMembers;
    }

    /**
     * @return canDeclareEnemies
     */
    public boolean canDeclareEnemies() {
        return this.declareEnemies;
    }

    /**
     * @param declareEnemies
     */
    public void setDeclareEnemies(boolean declareEnemies) {
        this.declareEnemies = declareEnemies;
    }

    public JSONObject toJSON() {
        JSONObject jsonPermission = new JSONObject();
        jsonPermission.put("modifyBlocks", this.modifyBlocks);
        jsonPermission.put("openContainers", this.openContainers);
        jsonPermission.put("attackEntities", this.attackEntities);
        jsonPermission.put("claimLand", this.claimLand);
        jsonPermission.put("manageMembers", this.manageMembers);
        jsonPermission.put("declareEnemies", this.declareEnemies);
        return jsonPermission;
    }
}