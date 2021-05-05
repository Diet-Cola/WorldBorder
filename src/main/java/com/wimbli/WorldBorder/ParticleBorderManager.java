package com.wimbli.WorldBorder;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class ParticleBorderManager {

    private BorderData border;
    private Location mapCenter;
    private Location first;
    private Location second;
    private int PARTICLE_RANGE = 4;
    private int PARTICLE_SIGHT_RANGE = 16;
    private double wbRange;
    private double fAngle;
    private double sAngle;
    private double arcLength;
    private double particleIncrement;


    public ParticleBorderManager(String worldName, BorderData border) {
        this.mapCenter = new Location(Bukkit.getWorld(worldName), border.getX(), 0, border.getZ());
        if (mapCenter == null) {
            throw new IllegalArgumentException("Failed to create mapCenter!");
        }
        this.first = mapCenter.clone().subtract(0, 0, border.getRadiusZ());
        this.second = mapCenter.clone().subtract(0.1, 0, border.getRadiusZ());

        double fRadius = getXZDistance(first);
        double sRadius = getXZDistance(second);
        this.wbRange = Math.min(fRadius, sRadius);

        this.fAngle = getAdjustedAngle(first);
        this.sAngle = getAdjustedAngle(second);

        this.arcLength = (fAngle == sAngle) ? 2 * Math.PI :
                (fAngle > sAngle) ? 2 * Math.PI - fAngle + sAngle :
                        sAngle - fAngle;
        //(circumference  in blocks) * (percentage of circumference the portal takes up)
        //= (wbRange * 2 * PI) * (arcLength / (PI * 2))
        //= wbRange * arcLength
        double blocksInPortal = arcLength * wbRange;
        this.particleIncrement = 1.0 / blocksInPortal;
    }

    private double getXZDistance(Location loc) {
        double x = loc.getX() - mapCenter.getX();
        double z = loc.getZ() - mapCenter.getZ();
        return Math.sqrt(x * x + z * z);
    }

    public double getArcPosition(Location loc) {
        double locAngle = getAdjustedAngle(loc);
        if (fAngle == sAngle) {
            return (Math.PI + locAngle) / arcLength;
        } else if ((fAngle > sAngle && (locAngle >= fAngle || locAngle <= sAngle)) ||
                (fAngle < sAngle && locAngle >= fAngle && locAngle <= sAngle)) {
            if (fAngle > sAngle && locAngle <= sAngle) {
                locAngle += 2.0*Math.PI;
            }
            return (locAngle - fAngle) / arcLength;
        }

        return -1.0;
    }

    public Location convertArcPositionToLocation(double arcPosition) {
        return convertArcPositionToLocation(arcPosition, 0);
    }

    private double getAdjustedAngle(Location loc) {
        double x = loc.getX() - mapCenter.getX();
        double z = loc.getZ() - mapCenter.getZ();
        return Math.atan2(z,x);
    }

    public Location convertArcPositionToLocation(double arcPosition, int y) {
        if (arcPosition > 1.0 || arcPosition < 0.0) return null;
        double theta = fAngle + arcLength * arcPosition;
        int x = (int) ((wbRange - 2.0) * Math.cos(theta));
        int z = (int) ((wbRange - 2.0) * Math.sin(theta));
        // TODO strengthen this
        return new Location(mapCenter.getWorld(), x, y, z);
    }

    public void showParticles(Player p) {
        Location loc = p.getLocation();
        if (getXZDistance(loc) >= wbRange - PARTICLE_SIGHT_RANGE && getArcPosition(loc) >= 0.0) {
            double angle = getArcPosition(loc) - (PARTICLE_RANGE * particleIncrement);
            for(int i = 0;i <= PARTICLE_RANGE * 2 + 1; i++) {
                if (angle < 0.0) {
                    angle += particleIncrement;
                    continue;
                }
                if (angle > 1.0) {
                    break;
                }
                for(int y = loc.getBlockY() - PARTICLE_RANGE; y <= loc.getBlockY() + PARTICLE_RANGE; y++) {
                    Location particeLoc = convertArcPositionToLocation(angle, y);
                    if (particeLoc == null) {
                        continue;
                    }
                    p.spawnParticle(Particle.ENCHANTMENT_TABLE, particeLoc, PARTICLE_RANGE);
                }
                angle += particleIncrement;
            }
        }
    }

    public void startRunnable() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(WorldBorder.plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                showParticles(player);
            }
        }, 4L, 4L);
    }
}
