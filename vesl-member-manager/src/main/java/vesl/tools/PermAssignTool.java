package vesl.tools;

import java.util.Set;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.managers.channel.attribute.IPermissionContainerManager;

public class PermAssignTool {
    @Deprecated
    public static int permsAdd(Guild guild, IPermissionHolder role, GuildChannel channel, Set<Permission> allow, Set<Permission> deny) {
        if (guild == null)
            return -1;
        if (role == null)
            return -2;
        if (channel == null)
            return -3;
        if ((allow == null || allow.isEmpty()) && (deny == null || deny.isEmpty()))
            return -4;
            
        // PermissionOverride oldPerms = channel.getPermissionContainer().getPermissionOverride(role);

        // if (oldPerms != null) {
        //     allow.addAll(oldPerms.getAllowed());
        //     deny.addAll(oldPerms.getDenied());
        // }

        // channel.getPermissionContainer().getManager().putPermissionOverride(role, allow, deny).complete();

        //return 0;

        channel.getPermissionContainer().upsertPermissionOverride(role).grant(allow).deny(deny).complete();
        //channel.getPermissionContainer().getPermissionOverride(role).getManager().grant(allow).deny(deny).complete();

        return 0;
    }

    @Deprecated
    public static int permsClear(Guild guild, IPermissionHolder role, GuildChannel channel, Set<Permission> perms) throws NullPointerException {
        if (guild == null)
            return -1;
        if (role == null)
            return -2;
        if (channel == null)
            return -3;
        if (perms == null || perms.isEmpty())
            return -4;

        PermissionOverride permOverride = channel.getPermissionContainer().getPermissionOverride(role);

        if (permOverride == null)
            return -5;

        // Set<Permission> oldAllow = oldPerms.getAllowed();
        // Set<Permission> oldDeny = oldPerms.getDenied();

        // oldAllow.removeAll(perms);
        // oldDeny.removeAll(perms);

        // channel.getPermissionContainer().getManager().putPermissionOverride(role, oldAllow, oldDeny).complete();

        //return 0;

        permOverride.getManager().clear(perms).complete();

        return 0;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static int permsSetAll(Guild guild, Set<IPermissionHolder> roles, Set<GuildChannel> channels, Set<Permission> allow, Set<Permission> deny) {
        if (guild == null)
            return -1;
        if (roles == null || roles.isEmpty())
            return -2;
        if (channels == null || channels.isEmpty())
            return -3;
        if ((allow == null || allow.isEmpty()) && (deny == null || deny.isEmpty()))
            return -4;

        for (GuildChannel c : channels) {   
            IPermissionContainerManager permContainerManager = c.getPermissionContainer().getManager();
            for (IPermissionHolder r : roles) {
                permContainerManager.putPermissionOverride(r, allow, deny);
                
            }
            permContainerManager.complete();
        }

        return 0;
    }

    public static int permsAddAll(Guild guild, Set<IPermissionHolder> roles, Set<GuildChannel> channels, Set<Permission> allow, Set<Permission> deny) {
        if (guild == null)
            return -1;
        if (roles == null || roles.isEmpty())
            return -2;
        if (channels == null || channels.isEmpty())
            return -3;
            if ((allow == null || allow.isEmpty()) && (deny == null || deny.isEmpty()))
            return -4;

        for(GuildChannel c : channels) {
            IPermissionContainer permContainer = c.getPermissionContainer();
            for (IPermissionHolder r : roles) {
                permContainer.upsertPermissionOverride(r).grant(allow).deny(deny).complete();
            }
        }

        return 0;
    }

    public static int permsClearAll(Guild guild, Set<IPermissionHolder> roles, Set<GuildChannel> channels, Set<Permission> perms) {
        return -1;
    }
}
