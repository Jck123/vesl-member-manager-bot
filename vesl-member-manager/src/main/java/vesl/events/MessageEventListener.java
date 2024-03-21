package vesl.events;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import net.dv8tion.jda.api.managers.channel.concrete.VoiceChannelManager;
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
                HashSet<IPermissionHolder> roles = ParseTools.parseRoles(messageText, event.getGuild());
                if (roles != null) {
                    for (IPermissionHolder r : roles) {
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
                /*HashSet<IMentionable> roles = ParseTools.parseRoles(messageText, event.getGuild());
                //HashSet<GuildChannel> parsedChannels = parseChannels(messageText, event.getGuild());
                HashSet<Permission> allow = ParseTools.parsePerms(messageText, 1);
                HashSet<Permission> deny = ParseTools.parsePerms(messageText, 2);

                HashSet<GuildChannel> channels = new HashSet<GuildChannel>();
                if (channelInverse) {
                    channels.addAll(event.getGuild().getChannels());
                    channels.removeAll(ParseTools.parseChannels(messageText, event.getGuild()));
                } else
                    channels = ParseTools.parseChannels(messageText, event.getGuild());*/



                Guild guild = event.getGuild();

                Role role = null;
                Member member = null;
                Member member2 = null;
                TextChannel textChannel = null;
                VoiceChannel voiceChannel = null;
                GuildChannel channel = null;
                IPermissionHolder permHolder = null;

                if (guild.getIdLong() == 203282662608207872L)   {       //Personal server roles/members
                    role = guild.getRoleById(1205225542882951300L);
                    member = guild.retrieveMemberById(1137409049269391490L).complete();
                    member2 = guild.retrieveMemberById(182967935000772608L).complete();
                    textChannel = guild.getTextChannelById(203282662608207872L);
                    voiceChannel = guild.getVoiceChannelById(203282662608207874L);
                    channel = guild.getTextChannelById(203282662608207872L);
                    permHolder = guild.retrieveMemberById(1137409049269391490L).complete();
                } else if (guild.getIdLong() == 1197698834118213662L) { //Dev VGC roles/members
                    role = guild.getRoleById(1197719933790990408L);
                    member = guild.retrieveMemberById(749073948473557043L).complete();
                    member2 = guild.retrieveMemberById(182967935000772608L).complete();
                    textChannel = guild.getTextChannelById(1197720436394434682L);
                    voiceChannel = guild.getVoiceChannelById(1197981925852315820L);
                    channel = guild.getTextChannelById(1197720436394434682L);
                    permHolder = guild.retrieveMemberById(182967935000772608L).complete();
                }
                TextChannelManager textChannelManager = textChannel.getManager();
                VoiceChannelManager voiceChannelManager = voiceChannel.getManager();
                Set<Permission> allow = new HashSet<Permission>(Arrays.asList(Permission.MANAGE_THREADS, Permission.VOICE_CONNECT));
                Set<Permission> deny = new HashSet<Permission>(Arrays.asList(Permission.CREATE_PUBLIC_THREADS, Permission.VOICE_SPEAK));

                //textChannelManager.putPermissionOverride(member, allow, deny).queue();
                //voiceChannelManager.putPermissionOverride(member, allow, deny).queue();

                //channel.getPermissionContainer().getManager().putPermissionOverride(member, allow, deny).complete();
                //try {Thread.sleep(10000);} catch (InterruptedException e){};
                //IPermissionContainer permContainer = channel.getPermissionContainer();
                //System.out.println(permContainer.getId());
                //PermissionOverride permOverride = permContainer.getPermissionOverride(permHolder);
                //System.out.println(permOverride.getId());

                //System.out.println(textChannel);
                //System.out.println(member);
                //PermissionOverride permOverride2 = textChannel.getPermissionOverride(member);
                //System.out.println(permOverride2);

                for (PermissionOverride po : textChannel.getMemberPermissionOverrides())
                    //if (po.isMemberOverride()) 
                        //System.out.println("MEMBER OVERRIDE");    
                    System.out.println(po);

                //System.out.println(textChannel.getPermissionOverrides());
                //System.out.println(guild.retrieveMemberById(1195901210545356881L).complete());
                //System.out.println(guild.retrieveMemberById(182967935000772608L).complete());

                /*permContainer.getManager().putPermissionOverride(permHolder, allow, deny).queue(
                    (success) -> System.out.println("Permission override created successfully."),
                    (error) -> System.out.println("Failed to create permission override: " + error.getMessage())
                );*/

                //try {Thread.sleep(10000);} catch (InterruptedException e){};

                /*if (channel == null) {
                    System.out.println("Channel is null.");
                } else {
                    permContainer = channel.getPermissionContainer();
                    if (permContainer == null) {
                        System.out.println("Permission container is null.");
                    } else {
                        System.out.println(permContainer.getId());
                        
                        if (permHolder == null) {
                            System.out.println("Permission holder is null.");
                        } else {
                            PermissionOverride permOverride = permContainer.getPermissionOverride(permHolder);
                            if (permOverride == null) {
                                System.out.println("Permission override is null.");
                            } else {
                                System.out.println(permOverride.getId());
                            }
                        }
                    }
                }*/


            } else if (messageText.startsWith(",channelcheck")) {

                Guild guild = event.getGuild();
                Role role = guild.getRoleById(1205225542882951300L);
                TextChannel textChannel = guild.getTextChannelById(203282662608207872L);
                VoiceChannel voiceChannel = guild.getVoiceChannelById(203282662608207874L);

                PermissionOverride textPerms = textChannel.getPermissionOverride(role);
                PermissionOverride voicePerms = voiceChannel.getPermissionOverride(role);

                System.out.println("--TEXT ALLOW--");
                for (Permission p : textPerms.getAllowed())
                    System.out.println(p);
                System.out.println("--TEXT DENY--");
                for (Permission p : textPerms.getDenied())
                    System.out.println(p);
                System.out.println("--VOICE ALLOW--");
                for (Permission p : voicePerms.getAllowed())
                    System.out.println(p);
                System.out.println("--VOICE DENY--");
                for (Permission p : voicePerms.getDenied())
                    System.out.println(p);

            } else if (messageText.startsWith(",channelroleadd")) {
                Guild guild = event.getGuild();
                Role role = guild.getRoleById(1205225542882951300L);
                TextChannel channel = guild.getTextChannelById(203282662608207872L);
                TextChannelManager channelManager = channel.getManager();
                HashSet<Permission> allow = new HashSet<Permission>(Arrays.asList(Permission.MANAGE_THREADS));
                HashSet<Permission> deny = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_ATTACH_FILES));

                PermissionOverride permOverride = channel.getPermissionOverride(role);
                
                System.out.println(permOverride.getAllowed());
                System.out.println(permOverride.getDenied());
                //permOverride.getAllowed();

            } else if (messageText.startsWith(",channelroleclear")) {
                Guild guild = event.getGuild();
                Role role = guild.getRoleById(1205225542882951300L);
                TextChannel channel = guild.getTextChannelById(203282662608207872L);
                TextChannelManager channelManager = channel.getManager();
                HashSet<Permission> permsToRemove = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_ATTACH_FILES));
                PermissionOverride permOverride = channel.getPermissionOverride(role);
                EnumSet<Permission> allowed = permOverride.getAllowed();
                EnumSet<Permission> denied = permOverride.getDenied();
                //EnumSet<Permission> currentPerms = EnumSet.copyOf(allowed);
                //currentPerms.addAll(denied);
                
                allowed.removeAll(permsToRemove);
                denied.removeAll(permsToRemove);

                //channelManager.removePermissionOverride(role).queue();
                if (allowed.isEmpty() && denied.isEmpty())
                    channelManager.removePermissionOverride(role).queue();
                else
                    channelManager.putPermissionOverride(role, allowed, denied).queue();

            } else if (messageText.startsWith(",devreset")) {

            } else if (messageText.startsWith(",undo")) {

            } else {
                event.getChannel().sendMessage("Unrecognized command!\nType in ',help' to view available commands").queue();
            }
        }
    }

}