package vesl.tools;

import java.util.Set;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

public class PermAssignTool {
    public static int permsAdd(Guild guild, IPermissionHolder role, GuildChannel channel, Set<Permission> allow, Set<Permission> deny) {
        if (guild == null)
            return -1;
        if (role == null)
            return -2;
        if (channel == null)
            return -3;
        if ((allow == null || allow.isEmpty()) && (deny == null || deny.isEmpty()))
            return -4;
            
        PermissionOverride oldPerms = channel.getPermissionContainer().getPermissionOverride(role);

        if (oldPerms != null) {
            allow.addAll(oldPerms.getAllowed());
            deny.addAll(oldPerms.getDenied());
        }

        channel.getPermissionContainer().getManager().putPermissionOverride(role, allow, deny).complete();

        return 0;
    }

    public static int permsClear(Guild guild, IPermissionHolder role, GuildChannel channel, Set<Permission> perms) {
        return -1;
    }
    
    public static int permsSetAll(Guild guild, Set<IPermissionHolder> roles, Set<GuildChannel> channels, Set<Permission> allow, Set<Permission> deny) {
        return -1;
    }

    public static int permsAddAll(Guild guild, Set<IPermissionHolder> roles, Set<GuildChannel> channels, Set<Permission> perms, Set<Permission> deny) {
        return -1;
    }

    public static int permsClearAll(Guild guild, Set<IPermissionHolder> roles, Set<GuildChannel> channels, Set<Permission> perms) {
        return -1;
    }
}
