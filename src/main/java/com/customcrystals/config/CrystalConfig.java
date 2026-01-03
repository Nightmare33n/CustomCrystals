package com.customcrystals.config;

public class CrystalConfig {
    // Core (inner cube)
    public boolean coreTintEnabled = false;
    public int coreColor = 0xFFFFFF;
    
    // Frames (both inner and outer rotating frames)
    public boolean framesTintEnabled = false;
    public int framesColor = 0xFFFFFF;
    
    // General settings
    public float scale = 1.0f;
    public float verticalOffset = 0.0f;
    public boolean beamEnabled = true;
    public float spinMultiplier = 1.0f;

    public static CrystalConfig defaults() {
        return new CrystalConfig();
    }
}
