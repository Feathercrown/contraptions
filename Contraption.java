package gmb.minecraft.contraptions;

import org.bukkit.Location;
import org.bukkit.World;

public class contraption {
	
	private String name = "Nonexistent Contraption";
	private Location loc = null;
	public contraption(String name, World world, int x, int y, int z) {
		this.name = name;
		this.loc = new Location(world, x, y, z);
	}

	public String getName() {
		return name;
	}

	public Location getLoc() {
		return loc;
	}
	
}
