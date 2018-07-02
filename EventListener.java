package gmb.minecraft.contraptions;

import java.awt.Event;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Dispenser;
import org.bukkit.material.Observer;
import org.bukkit.material.Redstone;
import org.bukkit.material.Directional;
import org.bukkit.material.DirectionalContainer;
import org.bukkit.material.Lever;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

public class EventListener implements Listener
{
	private Server server;
	
	public EventListener(Server server) {
		this.server = server;
	}
	
	ArrayList<contraption> contraptions = new ArrayList<contraption>();
	
	HashMap<Integer, HashMap<Integer, ItemStack>> facCreationMaterials = new HashMap<Integer, HashMap<Integer, ItemStack>>();
	HashMap<Integer, ItemStack> facMats1 = new HashMap<Integer, ItemStack>();
	HashMap<Integer, ItemStack> facMats2 = new HashMap<Integer, ItemStack>();
	{
		facMats1.put(0, new ItemStack(Material.DIAMOND, 1));
		facMats1.put(1, new ItemStack(Material.REDSTONE_LAMP_OFF, 1));
		facMats1.put(2, new ItemStack(Material.REDSTONE, 8));
	}
	{
		facMats2.put(0, new ItemStack(Material.IRON_INGOT, 16));
		facMats2.put(1, new ItemStack(Material.REDSTONE, 16));
	}
	{
		facCreationMaterials.put(0, facMats1);
		facCreationMaterials.put(1, facMats2);
	}
	HashMap<Integer, String> facNames = new HashMap<Integer, String>();
	{
		facNames.put(0, "Sensor");
		facNames.put(1, "Meticulous Interacter");
	}
	HashMap<Integer, Material> facBlocks = new HashMap<Integer, Material>();
	{
		facBlocks.put(0, Material.OBSERVER);
		facBlocks.put(1, Material.DAYLIGHT_DETECTOR);
	}

	Runnable run = new Runnable() {
        @SuppressWarnings("deprecation")
		public void run() {
        	//server.broadcastMessage("Sensors checked.");
        	for(contraption c : contraptions) {
        		if(!c.getName().equals("Sensor")) {
        			continue;
        		}
        		World wld = c.getLoc().getWorld();
        		Block observer = wld.getBlockAt(c.getLoc());
        		BlockFace face = ((Directional)observer.getState().getData()).getFacing().getOppositeFace();
        		//server.broadcastMessage(face.name());
        		int curSearchDist = 1;
        		boolean foundEntity = false;
        		while(!wld.getBlockAt(observer.getRelative(face, curSearchDist).getLocation()).getType().isOccluding() && curSearchDist <= 32) {
        			if(!wld.getNearbyEntities(observer.getRelative(face, curSearchDist).getLocation().add(0.5, 0.5, 0.5), 0.5, 0.5, 0.5).isEmpty()) {
        				foundEntity = true;
        				break;
        			}
        			curSearchDist++;
        		}
        		if(foundEntity) {
        			//server.broadcastMessage("Found an entity.");
        			BlockFace[] bf = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        			for(int i = 0;i<6;i++) {
        				Block toBePowered = wld.getBlockAt(observer.getRelative(bf[i], 1).getLocation());
        				if(toBePowered.getType().equals(Material.LEVER)) {
                			//server.broadcastMessage("Powering lever.");
        					//powerBlock(wld.getBlockAt(observer.getRelative(face.getOppositeFace(), 1).getLocation()), 5L);
        					byte meta = (byte) toBePowered.getState().getRawData();
        					toBePowered.setData((byte) (meta | 0x8));
        					//server.broadcastMessage(i+" "+meta);
        				}
        			}
        		} else {
        			BlockFace[] bf = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        			for(int i = 0;i<6;i++) {
        				Block toBePowered = wld.getBlockAt(observer.getRelative(bf[i], 1).getLocation());
        				if(toBePowered.getType().equals(Material.LEVER)) {
        					byte meta = (byte) toBePowered.getState().getRawData();
        					toBePowered.setData((byte)(meta & ~0x8));
        					//server.broadcastMessage(i+""+meta);
        				}
        			}
        		}
        		
        	}
        }
    };
    
	int sensorTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Bukkit.getPluginManager().getPlugin("Contraptions"), run, 0L, 2L);
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e)
    {
    	Predicate<contraption> sameLocPred = ctr-> (e.getBlock().getState().getLocation().equals(ctr.getLoc()));
    	contraptions.removeIf(sameLocPred);
    	//TODO: Detect removed factories
        //e.getPlayer().sendMessage("You broke a " + c.getName());
    }
	
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        event.setJoinMessage("Welcome, " + event.getPlayer().getName() + "!");
    }
    
    @EventHandler
    public void onBlockDispense(BlockDispenseEvent e) {
    	boolean advancedDispenser = false;
    	Location dispenserLoc = null;
		for(int i=0;i<contraptions.size();i++) {
			contraption cont = contraptions.get(i);
			if(cont.getName().equals("Meticulous Interacter") && cont.getLoc().clone().add(0, -1, 0).equals(e.getBlock().getState().getLocation())) {
				advancedDispenser = true;
				dispenserLoc = cont.getLoc().clone().add(0, -1, 0);
			}
		}
		if(advancedDispenser) {
			//Advanced Dispenser handling
			server.broadcastMessage("Advanced Dispenser fired.");
			World dispenserWorld = dispenserLoc.getWorld();
			Block dispenser = dispenserWorld.getBlockAt(dispenserLoc);
			BlockState dispenserState = dispenser.getState();
			BlockFace dispenserFacing = ((DirectionalContainer)dispenserState.getData()).getFacing();
			Location dispenserFireLoc = dispenser.getRelative(dispenserFacing).getLocation();
			//Get all entities in the block the dispenser is facing
			Predicate<Entity> emptyChestCartPredicate = ent-> (ent.getEntityId() == 43 && ((InventoryHolder) ent).getInventory().firstEmpty() == -1);
			Collection<Entity> minecarts = dispenserWorld.getNearbyEntities(dispenserFireLoc.add(0.5, 0.5, 0.5), 0.5, 0.5, 0.5);
			minecarts.removeIf(emptyChestCartPredicate);
			if(!minecarts.isEmpty()) {
				//If a chest minecart is in front of the dispenser
				e.setCancelled(true);
				final Location dLoc = dispenserLoc;
				Bukkit.getScheduler().scheduleSyncDelayedTask(server.getPluginManager().getPlugin("Contraptions"), new Runnable() {
		            public void run() {
		            	Inventory dispInv = ((Dispenser)e.getBlock().getWorld().getBlockAt(dLoc).getState()).getInventory();
						server.broadcastMessage(""+dispInv.getStorageContents().toString());
						int firstItemSlot = 0;
						for(int i=0;i<9;i++) {
							if(dispInv.getStorageContents()[i] != null) {
								firstItemSlot = i;
								server.broadcastMessage(""+firstItemSlot);
								break;
							}
						}
						ItemStack movingItem = dispInv.getStorageContents()[firstItemSlot];
						server.broadcastMessage("Item being moved: "+dispInv.getStorageContents()[firstItemSlot].getType().toString());
						//dispInv.setItem(firstItemSlot, new ItemStack(Material.AIR));
						ItemStack[] dispInvContents = dispInv.getContents();
						dispInvContents[firstItemSlot] = new ItemStack(Material.AIR, 1);
						dispInv.setContents(dispInvContents);
						((InventoryHolder)minecarts.iterator().next()).getInventory().addItem(movingItem);
		            }
		        }, 1L); // L == ticks; thanks https://bukkit.org/threads/getting-the-instance-of-a-plugin.253856/
			} else {
				//server.broadcastMessage("No chest carts");
			}
		}
    }
    
    @EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
    	if(e.getAction().name() == "LEFT_CLICK_BLOCK" && e.getItem() != null && e.getItem().getType().equals(Material.STICK)) {
    		e.setCancelled(true);
    		boolean clickedFac = false;
    		contraption cFac = null; //Clicked factory/contraption
    		for(int i=0;i<contraptions.size();i++) {
    			if(contraptions.get(i).getLoc().equals(e.getClickedBlock().getLocation())) {
    				clickedFac = true;
    				cFac = contraptions.get(i);
    			}
    		}
    		if(clickedFac) {
    			e.getPlayer().sendMessage("You clicked a " + cFac.getName() + "!");
    		} else {
	    		Collection<HashMap<Integer, ItemStack>> facMatLists = facCreationMaterials.values();
	    	    Iterator<HashMap<Integer, ItemStack>> iterator = facMatLists.iterator();
	    	    int curFacNum = -1;
	    	    boolean canMakeCurFac = true;
	    	    while(iterator.hasNext()) {
	    	    	curFacNum++;
	    	    	if(e.getClickedBlock().getType() != facBlocks.get(curFacNum)) {
	    	    		iterator.next();
	    	    		continue;
	    	    	}
	    	    	//Each factory
	    	    	HashMap<Integer, ItemStack> curFacMatList = (HashMap<Integer, ItemStack>)iterator.next();
	    	    	Collection<ItemStack> curFacMatListCollection = curFacMatList.values();
	    	    	Iterator<ItemStack> iterator2 = curFacMatListCollection.iterator();
	    	    	canMakeCurFac = true;
	        	    while(iterator2.hasNext()) {
	        	    	//Each material of each factory
	        	    	ItemStack matAndAmount = (ItemStack)iterator2.next();
	        	    	if(!e.getPlayer().getInventory().contains(matAndAmount.getType(), matAndAmount.getAmount())) {
	        	    		//e.getPlayer().sendMessage("You don't have a material: " + matAndAmount.getType().name());
	        	    		canMakeCurFac = false;
	        	    	} else {
	        	    		//e.getPlayer().sendMessage("You have a material: " + matAndAmount.getType().name());
	        	    	}
	        	    }
	        	    if(canMakeCurFac) {
	        	    	e.getPlayer().sendMessage("Creating contraption: " + facNames.get(curFacNum));
	            	    HashMap<Integer, ItemStack> curFacMats = curFacMatList;
	            	    Collection<ItemStack> curFacMatListCollectionNew = curFacMats.values();
	        	    	Iterator<ItemStack> iterator3 = curFacMatListCollectionNew.iterator();
	            	    while(iterator3.hasNext()) {
	            	    	e.getPlayer().getInventory().removeItem((ItemStack)iterator3.next());
	            	    }
	            	    contraptions.add(new contraption(facNames.get(curFacNum), e.getPlayer().getWorld(), e.getClickedBlock().getX(), e.getClickedBlock().getY(), e.getClickedBlock().getZ()));
	            	    e.getPlayer().sendMessage(contraptions.get(0).getName());
	            	    //Save contraption to save file
	        	    	break;
	        	    } else {
	        	    	//e.getPlayer().sendMessage("You can't make the factory: " + facNames.get(curFacNameNum));
	        	    }
	        	    //Leftovers from https://beginnersbook.com/2013/12/hashmap-in-java-with-example/
	        	    //System.out.print("key is: "+ mentry.getKey() + " & Value is: ");
	    	    	//System.out.println(mentry.getValue());
	    	    }
	    		//HashMap<Integer, ItemStack> leftovers = e.getPlayer().getInventory().removeItem(new ItemStack(Material.STONE));
	    		//if(leftovers.isEmpty()) {
	    		//	e.getPlayer().sendMessage("X: "+String.valueOf(e.getClickedBlock().getX())+", Y: "+String.valueOf(e.getClickedBlock().getY())+", Z: "+String.valueOf(e.getClickedBlock().getZ()));
	    		//} else {
	    		//	e.getPlayer().sendMessage("Not enough materials!");
	    		//}
    		}
    	}
    	return;
	}
    
    //Gotten from itemexchange, thanks CivCraft!
    //https://github.com/CivClassic/ItemExchange/blob/master/src/main/java/com/untamedears/ItemExchange/utility/BlockUtility.javahttps://github.com/CivClassic/ItemExchange/blob/master/src/main/java/com/untamedears/ItemExchange/utility/BlockUtility.java
	/*
    public static void powerBlock(Block block, long time) {
		// Ensure that a timedelay exists
		if (time <= 0) {
			throw new IllegalArgumentException("You must enter an above zero time value!");
		}
		// Set the block to be powered
		BlockState b_onstate = block.getState();
		byte b_onmeta = block.getData();
		b_onstate.setRawData((byte)(b_onmeta | 0x8));
		b_onstate.update();
		// And set the block to unpower in due time
		Bukkit.getScheduler().scheduleSyncDelayedTask(server.getPluginManager().getPlugin("Contraptions"), new Runnable() {
			public void run() {
				BlockState b_offstate = block.getState();
				byte b_offmeta = block.getdata;
				b_offstate.setRawData((byte)(b_offmeta & ~0x8));
				b_offstate.update();
			}
		}, time);
	}
	*/
}
