package com.customcrystals.config;

public class CrystalConfig {
    public boolean coreTintEnabled = true;
    public int coreColor = 0xFFFFFF;
    public boolean frame1TintEnabled = true;
    public int frame1Color = 0xFFFFFF;
    public boolean frame2TintEnabled = true;
    public int frame2Color = 0xFFFFFF;
    public float scale = 1.0f;
    public float verticalOffset = 0.0f;
    public boolean beamEnabled = true;
    public float spinMultiplier = 1.0f;

    public static CrystalConfig defaults() {
        return new CrystalConfig();
    }
}
