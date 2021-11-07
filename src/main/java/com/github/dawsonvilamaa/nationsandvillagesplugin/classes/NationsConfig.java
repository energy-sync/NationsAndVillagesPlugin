package com.github.dawsonvilamaa.nationsandvillagesplugin.classes;

import com.github.dawsonvilamaa.nationsandvillagesplugin.NationsManager;
import org.json.simple.JSONObject;

public class NationsConfig {
    NationsPermission legatePermissions;
    NationsPermission memberPermissions;
    NationsPermission nonMemberPermissions;
    private boolean golemsAttackEnemies;

    public NationsConfig() {
        this.legatePermissions = new NationsPermission(NationsManager.Rank.LEGATE);
        this.memberPermissions = new NationsPermission(NationsManager.Rank.MEMBER);
        this.nonMemberPermissions = new NationsPermission(NationsManager.Rank.NONMEMBER);
        this.golemsAttackEnemies = true;
    }

    public NationsConfig(JSONObject jsonNation) {
        this.legatePermissions = new NationsPermission((JSONObject) jsonNation.get("legatePermissions"));
        this.memberPermissions = new NationsPermission((JSONObject) jsonNation.get("memberPermissions"));
        this.nonMemberPermissions = new NationsPermission((JSONObject) jsonNation.get("nonMemberPermissions"));
        this.golemsAttackEnemies = Boolean.parseBoolean(jsonNation.get("golemsAttackEnemies").toString());
    }

    /**
     * Returns the NationsPermission of a given rank in this nation
     * @param rank
     * @return NationsPermission
     */
    public NationsPermission getPermissionByRank(NationsManager.Rank rank) {
        switch (rank) {
            case LEADER:
                return new NationsPermission(NationsManager.Rank.LEADER);
            case LEGATE:
                return this.legatePermissions;
            case MEMBER:
                return this.memberPermissions;
            case NONMEMBER:
                return this.nonMemberPermissions;
            default:
                return null;
        }
    }

    /**
     * Sets the permissions of a given rank in this nation
     * @param rank
     * @param perms
     */
    public void setPermissionByRank(NationsManager.Rank rank, NationsPermission perms) {
        switch (rank) {
            case LEGATE:
                this.legatePermissions = perms;
                break;

            case MEMBER:
                this.memberPermissions = perms;
                break;

            case NONMEMBER:
                this.nonMemberPermissions = perms;
                break;
        }
    }

    /**
     * @return golemsAttackEnemies
     */
    public boolean getGolemsAttackEnemies() {
        return this.golemsAttackEnemies;
    }

    /**
     * @param golemsAttackEnemies
     */
    public void setGolemsAttackEnemies(boolean golemsAttackEnemies) {
        this.golemsAttackEnemies = golemsAttackEnemies;
    }
}
