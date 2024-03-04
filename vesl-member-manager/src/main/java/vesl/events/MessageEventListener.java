package vesl.events;

import java.util.EnumSet;
import java.util.HashSet;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.channel.ChannelManager;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;

import vesl.tools.ParseTools;

public class MessageEventListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        super.onMessageReceived(event);

        if (event.getAuthor().isBot()) return;

        String messageText = event.getMessage().getContentRaw();
        if (messageText.startsWith(",")) {
            event.getMessage().addReaction(Emoji.fromUnicode("U+2611")).queue();

            Boolean channelInverse = messageText.contains("-ci") ? true : false;

            if (messageText.startsWith(",help")) {
                event.getChannel().sendMessage("You've entered ',help'").queue();
            } else if (messageText.startsWith(",debugparse")) {
                String output = "";
                output += "===== [ RESULTS  ] =====\n----- [  ROLES   ] -----\n";
                HashSet<IMentionable> roles = ParseTools.parseRoles(messageText, event.getGuild());
                if (roles != null) {
                    for (IMentionable r : roles) {
                        output += r.getClass().getSimpleName() + " | " + r + "\n";
                    }
                }
                output += "----- [ CHANNELS ] -----\n";
                HashSet<GuildChannel> channels = ParseTools.parseChannels(messageText, event.getGuild());
                if (channels != null) {
                    for (GuildChannel c : channels) {
                        output += c.getClass().getSimpleName()  + " | " + c.getName() + "\n";
                    }
                }
                output += "----- [  PERMS   ] -----\n";
                HashSet<Permission> perms = ParseTools.parsePerms(messageText, 0);
                if (perms != null) {
                    for (Permission p : perms) {
                        output += p.getName() + "\n";
                    }
                }
                output += "----- [  ALLOW   ] -----\n";
                HashSet<Permission> allow = ParseTools.parsePerms(messageText, 1);
                if (allow != null) {
                    for (Permission p : allow) {
                        output += p.getName() + "\n";
                    }
                }
                output += "----- [   DENY   ] -----\n";
                HashSet<Permission> deny = ParseTools.parsePerms(messageText, 2);
                if (deny != null) {
                    for (Permission p : deny) {
                        output += p.getName() + "\n";
                    }
                }
                event.getChannel().sendMessage(output).queue();
            } else if (messageText.startsWith(",devtest")) {
                event.getChannel().sendMessage("You've entered ',devtest'").queue();
                Guild guild = event.getGuild();
                Role role = guild.getRoleById(1205225542882951300L);
                TextChannel channel = guild.getTextChannelById(203282662608207872L);
                TextChannelManager channelM = channel.getManager();

                //RoleManager manager = event.getGuild().getRoleById(1205225542882951300L).getManager();
                //manager.revokePermissions(Permission.VIEW_CHANNEL).queue();
                channelM.removePermissionOverride(1205225542882951300L).queue();
                //channelM.putRolePermissionOverride(role.getIdLong(), null, null);

                EnumSet<Permission> temp = channel.getPermissionContainer().getPermissionOverride(role).getDenied();
                //EnumSet<Permission> temp = event.getGuild().getRoleById(1205225542882951300L).getPermissionsExplicit(channelM.getChannel());
                for (Permission p : temp) {
                    System.out.println(p.getName());
                }
                EnumSet<Permission> allow = EnumSet.of(Permission.valueOf("MANAGE_CHANNEL"));
                EnumSet<Permission> deny = EnumSet.of(Permission.valueOf("VOICE_CONNECT"));
                channelM.putRolePermissionOverride(1205225542882951300L, allow, deny).queue();
                //event.getGuild().getGuildChannelById(1205225542882951300L).getManager();
                //System.out.println(Permission.valueOf("ADMINISTRATOR"));
                temp = channel.getPermissionContainer().getPermissionOverride(role).getDenied();
                //temp = event.getGuild().getRoleById(1205225542882951300L).getPermissionsExplicit(channelM.getChannel());
                for (Permission p : temp) {
                    System.out.println(p.getName());
                }


                
            } else if (messageText.startsWith(",channelroleset")) {
                HashSet<IMentionable> roles = ParseTools.parseRoles(messageText, event.getGuild());
                //HashSet<GuildChannel> parsedChannels = parseChannels(messageText, event.getGuild());
                HashSet<Permission> allow = ParseTools.parsePerms(messageText, 1);
                HashSet<Permission> deny = ParseTools.parsePerms(messageText, 2);

                HashSet<GuildChannel> channels = new HashSet<GuildChannel>();
                if (channelInverse) {
                    channels.addAll(event.getGuild().getChannels());
                    channels.removeAll(ParseTools.parseChannels(messageText, event.getGuild()));
                } else
                    channels = ParseTools.parseChannels(messageText, event.getGuild());

                for (GuildChannel c : channels) {
                    ChannelManager cManager = c.getManager();

                    //cManager
                }

            } else if (messageText.startsWith(",channelroleadd")) {

            } else if (messageText.startsWith(",channelroleclear")) {

            } else if (messageText.startsWith(",undo")) {

            } else {
                event.getChannel().sendMessage("Unrecognized command!\nType in ',help' to view available commands").queue();
            }
        }
    }

}