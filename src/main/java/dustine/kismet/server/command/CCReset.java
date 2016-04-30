package dustine.kismet.server.command;

import dustine.kismet.Kismet;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;

public class CCReset implements ICommandComponent {
    private final String parentName;

    public CCReset(String parentName) {
        this.parentName = parentName;
    }

    @Override
    public String getComponentName() {
        return "reset";
    }

    @Override
    public String getParentName() {
        return parentName;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayerMP)) {
            CommandKismet.send(sender, "§cCommand can only be run in-game");
            return;
        }
        if (Kismet.databaseBuilder != null) {
            Kismet.databaseBuilder.generateStacks((EntityPlayerMP) sender);
        } else {
            CommandKismet.send(sender, "§cError, target database factory not found");
        }
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args,
                                                BlockPos pos) {
        return Collections.emptyList();
    }
}
