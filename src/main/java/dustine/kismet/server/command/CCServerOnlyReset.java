package dustine.kismet.server.command;

import dustine.kismet.Kismet;
import dustine.kismet.target.TargetLibraryBuilder;
import dustine.kismet.world.savedata.WSDTargetDatabase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;

public class CCServerOnlyReset extends CommandComponent {
    public CCServerOnlyReset(String parent) {
        super(parent);
    }

    @Override public String getCommandName() {
        return "serverOnlyReset";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (Kismet.databaseBuilder != null) {
            CommandKismet.send(sender, "Starting §cserver only§r database reset...");
            Kismet.databaseBuilder.buildServerSide((EntityPlayerMP) sender);
            CommandKismet.send(sender, "Finished! Refreshing target library now...");
            TargetLibraryBuilder.build(WSDTargetDatabase.get(server.getEntityWorld()));
            CommandKismet.send(sender, "Done! Database reset finished.");
        } else {
            CommandKismet.error("Target database factory not found");
        }
    }

    @Override public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args,
                                                          BlockPos pos) {
        return Collections.emptyList();
    }
}
