package gmb.minecraft.contraptions;


import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
 
public class Main extends JavaPlugin {
 
    @Override
    public void onEnable() {
    	 getServer().getPluginManager().registerEvents(new EventListener(getServer()), this);
    }
   
    @Override
    public void onDisable() {
       
    }
   
    @Override
    public boolean onCommand(CommandSender sender,
            Command command,
            String label,
            String[] args) {
        if (command.getName().equalsIgnoreCase("contraptions")) {
            sender.sendMessage("You ran /mycommand!");
            return true;
        }
        return false;
    }
}
