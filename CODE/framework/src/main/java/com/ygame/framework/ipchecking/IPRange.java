package com.ygame.framework.ipchecking;

/**
 *
 * @author ThanhNT
 */
public class IPRange {

    public long upper;
    public long lower;

    public IPRange(long lower, long upper) {
        this.upper = upper;
        this.lower = lower;
    }

    @Override
    public String toString() {
        return "lower: " + lower + ", upper: " + upper;
    }
}
