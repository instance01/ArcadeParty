package com.comze_instancelabs.arcadeparty;

import net.minecraft.server.v1_7_R1.DamageSource;
import net.minecraft.server.v1_7_R1.Entity;
import net.minecraft.server.v1_7_R1.EntityComplexPart;
import net.minecraft.server.v1_7_R1.EntityHuman;
import net.minecraft.server.v1_7_R1.EntitySheep;
import net.minecraft.server.v1_7_R1.World;

import org.bukkit.Location;

public class CSheep extends EntitySheep {

	private boolean onGround = false;

	public CSheep(Location loc, World world) {
		super(world);
		setPosition(loc.getX(), loc.getY(), loc.getZ());
		yaw = loc.getYaw() + 180;
		while (yaw > 360) {
			yaw -= 360;
		}
		while (yaw < 0) {
			yaw += 360;
		}
		if (yaw < 45 || yaw > 315) {
			yaw = 0F;
		} else if (yaw < 135) {
			yaw = 90F;
		} else if (yaw < 225) {
			yaw = 180F;
		} else {
			yaw = 270F;
		}
	}

	@Override
	public void e() {
		return;
	}

	public boolean damageEntity(DamageSource damagesource, int i) {
		return false;
	}

	@Override
	public int getExpReward() {
		return 0;
	}

	public boolean a(EntityComplexPart entitycomplexpart, DamageSource damagesource, int i) {
		return false;
	}

	@Override
	public boolean a(EntityHuman entityhuman) {
		return false;
	}

	@Override
	protected void a(Entity entity, float f) {
		return;
	}

	@Override
	protected void a(double d0, boolean flag) {
		return;
	}

	@Override
	protected void b(float f) {
		return;
	}

	@Override
	public void collide(Entity entity) {
		return;
	}

	@Override
	public void g(double x, double y, double z) {
		super.g(0D, 0D, 0D);
	}

	@Override
	protected void w() {
		return;
	}
}