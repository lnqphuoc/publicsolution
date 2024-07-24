package com.ygame.framework.ipchecking;

/**
 *
 * @author ThanhNT
 */
public class RuleEnt {

    public String name;
    public String fileterKey;
    public String description;
    public String type;
    public boolean accepted;

    public RuleEnt(String name, String fileterKey, String description, String type, boolean accepted) {
        this.name = name;
        this.fileterKey = fileterKey;
        this.description = description;
        this.type = type;
        this.accepted = accepted;
    }

    public static class RuleType {
        public static final String BROWSER = "browser";
        public static final String REFERER = "referer";
    }
}
