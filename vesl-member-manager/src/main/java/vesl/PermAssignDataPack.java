package vesl;

import java.util.Set;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

public class PermAssignDataPack {
    public PermAssignDataType TYPE;
    public Set<IPermissionHolder> ROLES;
    public Set<GuildChannel> CHANNELS;
    public Set<Permission> PERMS;
    public Set<Permission> ALLOW;
    public Set<Permission> DENY;
    public Long CREATED_AT;

    public PermAssignDataPack(PermAssignDataType t, Set<IPermissionHolder> r, Set<GuildChannel> c, Set<Permission> a, Set<Permission> d) {
        TYPE = t;
        ROLES = r;
        CHANNELS = c;
        ALLOW = a;
        DENY = d;
        CREATED_AT = System.currentTimeMillis();
    }

    public PermAssignDataPack(Set<IPermissionHolder> r, Set<GuildChannel> c, Set<Permission> p) {
        TYPE = PermAssignDataType.CLEAR;
        ROLES = r;
        CHANNELS = c;
        PERMS = p;
        CREATED_AT = System.currentTimeMillis();
    }
}
