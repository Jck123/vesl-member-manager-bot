package vesl.events;

import java.util.HashSet;
import java.util.Set;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

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
                event.getChannel().sendMessage("You've entered ',help'\nThere is no help\n...Go home").queue();
            // } else if (messageText.startsWith(",debugparse")) {
            //     String output = "";
            //     output += "===== [ RESULTS  ] =====\n----- [  ROLES   ] -----\n";
            //     HashSet<IPermissionHolder> roles = ParseTools.parseRoles(messageText, event.getGuild());
            //     if (roles != null) {
            //         for (IPermissionHolder r : roles) {
            //             output += r.getClass().getSimpleName() + " | " + r + "\n";
            //         }
            //     }
            //     output += "----- [ CHANNELS ] -----\n";
            //     HashSet<GuildChannel> channels = ParseTools.parseChannels(messageText, event.getGuild());
            //     if (channels != null) {
            //         for (GuildChannel c : channels) {
            //             output += c.getClass().getSimpleName()  + " | " + c.getName() + "\n";
            //         }
            //     }
            //     output += "----- [  PERMS   ] -----\n";
            //     HashSet<Permission> perms = ParseTools.parsePerms(messageText, 0);
            //     if (perms != null) {
            //         for (Permission p : perms) {
            //             output += p.getName() + "\n";
            //         }
            //     }
            //     output += "----- [  ALLOW   ] -----\n";
            //     HashSet<Permission> allow = ParseTools.parsePerms(messageText, 1);
            //     if (allow != null) {
            //         for (Permission p : allow) {
            //             output += p.getName() + "\n";
            //         }
            //     }
            //     output += "----- [   DENY   ] -----\n";
            //     HashSet<Permission> deny = ParseTools.parsePerms(messageText, 2);
            //     if (deny != null) {
            //         for (Permission p : deny) {
            //             output += p.getName() + "\n";
            //         }
            //     }
            //     event.getChannel().sendMessage(output).queue();
            // // } else if (messageText.startsWith(",devtest")) {
            //     event.getChannel().sendMessage("You've entered ',devtest'").queue();
            //     Guild guild = event.getGuild();
            //     Role role = guild.getRoleById(1205225542882951300L);
            //     TextChannel channel = guild.getTextChannelById(203282662608207872L);
            //     TextChannelManager channelM = channel.getManager();

            //     //RoleManager manager = event.getGuild().getRoleById(1205225542882951300L).getManager();
            //     //manager.revokePermissions(Permission.VIEW_CHANNEL).queue();
            //     channelM.removePermissionOverride(1205225542882951300L).queue();
            //     //channelM.putRolePermissionOverride(role.getIdLong(), null, null);

            //     EnumSet<Permission> temp = channel.getPermissionContainer().getPermissionOverride(role).getDenied();
            //     //EnumSet<Permission> temp = event.getGuild().getRoleById(1205225542882951300L).getPermissionsExplicit(channelM.getChannel());
            //     for (Permission p : temp) {
            //         System.out.println(p.getName());
            //     }
            //     EnumSet<Permission> allow = EnumSet.of(Permission.valueOf("MANAGE_CHANNEL"));
            //     EnumSet<Permission> deny = EnumSet.of(Permission.valueOf("VOICE_CONNECT"));
            //     channelM.putRolePermissionOverride(1205225542882951300L, allow, deny).queue();
            //     //event.getGuild().getGuildChannelById(1205225542882951300L).getManager();
            //     //System.out.println(Permission.valueOf("ADMINISTRATOR"));
            //     temp = channel.getPermissionContainer().getPermissionOverride(role).getDenied();
            //     //temp = event.getGuild().getRoleById(1205225542882951300L).getPermissionsExplicit(channelM.getChannel());
            //     for (Permission p : temp) {
            //         System.out.println(p.getName());
            //     }


                
            } else if (messageText.startsWith(",channelroleset")) {
                
                //Variable setup
                Guild guild = event.getGuild();
                Set<GuildChannel> channels = ParseTools.parseChannels(messageText, guild);
                Set<IPermissionHolder> roles = ParseTools.parseRoles(messageText, guild);
                Set<Permission> allow = ParseTools.parsePerms(messageText, 1);
                Set<Permission> deny = ParseTools.parsePerms(messageText, 2);
                
                //Switches channels around 
                if(channelInverse) {
                    Set<GuildChannel> allChannels = new HashSet<GuildChannel>(guild.getChannels());
                    for(GuildChannel c : allChannels)           //Removes all channels that are already synced to the category perms
                        if(c.getType() == ChannelType.CATEGORY)
                            for(GuildChannel c2 : ((Category)c).getChannels())
                                if (((ICategorizableChannel)c2).isSynced())
                                    channels.add(c2);

                    allChannels.removeAll(channels);
                    channels = allChannels;
                    allChannels = null;
                }

                //Remove all threads(They do not have modifiable perms)
                channels.removeIf(c -> c.getType() == ChannelType.GUILD_NEWS_THREAD || c.getType() == ChannelType.GUILD_PRIVATE_THREAD || c.getType() == ChannelType.GUILD_PUBLIC_THREAD);

                //Print out parsed things and confirm if user wishes to proceed
                EmbedBuilder confirmEmbedBuilder = new EmbedBuilder();
                String channelsMentionList = "";
                String channelsTypeList = "";
                String channelsIdList = "";
                String rolesNameList = "";
                String rolesClassList = "";
                String rolesIdList = "";
                String allowList = "";
                String denyList = "";

                //TODO: Investigate using \t for separating channel and associated stats to fix misalignment issue
                for(GuildChannel c : channels) {
                    channelsMentionList += c.getAsMention() + '\n';
                    channelsTypeList += c.getType().name() + '\n';
                    channelsIdList += c.getId() + '\n';
                }
                for (IPermissionHolder r : roles) {
                    rolesNameList += ((IMentionable)r).getAsMention() + '\n';
                    rolesClassList += r.getClass().getSimpleName().replace("Impl", "") + '\n';
                    rolesIdList += r.getId() + '\n';
                }
                for (Permission p : allow)
                    allowList += p.getName() + '\n';
                for (Permission p : deny)
                    denyList += p.getName() + '\n';

                confirmEmbedBuilder.setTitle("Setting Permissions...");
                confirmEmbedBuilder.addField("Channels", channelsMentionList, true);
                confirmEmbedBuilder.addField("", channelsTypeList, true);
                confirmEmbedBuilder.addField("", channelsIdList, true);
                confirmEmbedBuilder.addField("Roles/Users", rolesNameList, true);
                confirmEmbedBuilder.addField("", rolesClassList, true);
                confirmEmbedBuilder.addField("", rolesIdList, true);
                confirmEmbedBuilder.addField("Allow", allowList, true);
                confirmEmbedBuilder.addField("Deny", denyList, true);
                confirmEmbedBuilder.setFooter("Please confirm you'd like to make these changes by clicking below" + Emoji.fromFormatted("⬇️").getFormatted());

                //Create buttons and add to reply
                Button confirmButton = Button.success("memberManagerConfirm", "Confirm").withEmoji(Emoji.fromFormatted("✔️"));
                Button denyButton = Button.danger("memberMangerDeny", "Deny").withEmoji(Emoji.fromFormatted("✖️"));

                //Compile message and reply to message
                event.getMessage().replyEmbeds(confirmEmbedBuilder.build()).addActionRow(confirmButton, denyButton).setSuppressedNotifications(true).queue();
                
                
                /*permContainer.getManager().putPermissionOverride(permHolder, allow, deny).queue(
                    (success) -> System.out.println("Permission override created successfully."),
                    (error) -> System.out.println("Failed to create permission override: " + error.getMessage())
                );*/

            // } else if (messageText.startsWith(",channelcheck")) {

            //     Guild guild = event.getGuild();
            //     Role role = guild.getRoleById(1205225542882951300L);
            //     TextChannel textChannel = guild.getTextChannelById(203282662608207872L);
            //     VoiceChannel voiceChannel = guild.getVoiceChannelById(203282662608207874L);

            //     PermissionOverride textPerms = textChannel.getPermissionOverride(role);
            //     PermissionOverride voicePerms = voiceChannel.getPermissionOverride(role);

            //     System.out.println("--TEXT ALLOW--");
            //     for (Permission p : textPerms.getAllowed())
            //         System.out.println(p);
            //     System.out.println("--TEXT DENY--");
            //     for (Permission p : textPerms.getDenied())
            //         System.out.println(p);
            //     System.out.println("--VOICE ALLOW--");
            //     for (Permission p : voicePerms.getAllowed())
            //         System.out.println(p);
            //     System.out.println("--VOICE DENY--");
            //     for (Permission p : voicePerms.getDenied())
            //         System.out.println(p);

            } else if (messageText.startsWith(",channelroleadd")) {

                //Variable setup
                Guild guild = event.getGuild();
                Set<GuildChannel> channels = ParseTools.parseChannels(messageText, guild);
                Set<IPermissionHolder> roles = ParseTools.parseRoles(messageText, guild);
                Set<Permission> allow = ParseTools.parsePerms(messageText, 1);
                Set<Permission> deny = ParseTools.parsePerms(messageText, 2);
                
                //Switches channels around 
                if(channelInverse) {
                    Set<GuildChannel> allChannels = new HashSet<GuildChannel>(guild.getChannels());
                    for(GuildChannel c : allChannels)           //Removes all channels that are already synced to the category perms
                        if(c.getType() == ChannelType.CATEGORY)
                            for(GuildChannel c2 : ((Category)c).getChannels())
                                if (((ICategorizableChannel)c2).isSynced())
                                    channels.add(c2);

                    allChannels.removeAll(channels);
                    channels = allChannels;
                    allChannels = null;
                }

                //Remove all threads(They do not have modifiable perms)
                channels.removeIf(c -> c.getType() == ChannelType.GUILD_NEWS_THREAD || c.getType() == ChannelType.GUILD_PRIVATE_THREAD || c.getType() == ChannelType.GUILD_PUBLIC_THREAD);

                //Print out parsed things and confirm if user wishes to proceed
                EmbedBuilder confirmEmbedBuilder = new EmbedBuilder();
                String channelsMentionList = "";
                String channelsTypeList = "";
                String channelsIdList = "";
                String rolesNameList = "";
                String rolesClassList = "";
                String rolesIdList = "";
                String allowList = "";
                String denyList = "";

                //TODO: Investigate using \t for separating channel and associated stats to fix misalignment issue
                for(GuildChannel c : channels) {
                    channelsMentionList += c.getAsMention() + '\n';
                    channelsTypeList += c.getType().name() + '\n';
                    channelsIdList += c.getId() + '\n';
                }
                for (IPermissionHolder r : roles) {
                    rolesNameList += ((IMentionable)r).getAsMention() + '\n';
                    rolesClassList += r.getClass().getSimpleName().replace("Impl", "") + '\n';
                    rolesIdList += r.getId() + '\n';
                }
                for (Permission p : allow)
                    allowList += p.getName() + '\n';
                for (Permission p : deny)
                    denyList += p.getName() + '\n';

                confirmEmbedBuilder.setTitle("Adding Permissions...");
                confirmEmbedBuilder.addField("Channels", channelsMentionList, true);
                confirmEmbedBuilder.addField("", channelsTypeList, true);
                confirmEmbedBuilder.addField("", channelsIdList, true);
                confirmEmbedBuilder.addField("Roles/Users", rolesNameList, true);
                confirmEmbedBuilder.addField("", rolesClassList, true);
                confirmEmbedBuilder.addField("", rolesIdList, true);
                confirmEmbedBuilder.addField("Allow", allowList, true);
                confirmEmbedBuilder.addField("Deny", denyList, true);
                confirmEmbedBuilder.setFooter("Please confirm you'd like to make these changes by clicking below" + Emoji.fromFormatted("⬇️").getFormatted());

                //Create buttons and add to reply
                Button confirmButton = Button.success("memberManagerConfirm", "Confirm").withEmoji(Emoji.fromFormatted("✔️"));
                Button denyButton = Button.danger("memberMangerDeny", "Deny").withEmoji(Emoji.fromFormatted("✖️"));

                //Compile message and reply to message
                event.getMessage().replyEmbeds(confirmEmbedBuilder.build()).addActionRow(confirmButton, denyButton).setSuppressedNotifications(true).queue();


                // Guild guild = event.getGuild();
                // Role role = guild.getRoleById(1205225542882951300L);
                // TextChannel channel = guild.getTextChannelById(203282662608207872L);
                // TextChannelManager channelManager = channel.getManager();
                // HashSet<Permission> allow = new HashSet<Permission>(Arrays.asList(Permission.MANAGE_THREADS));
                // HashSet<Permission> deny = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_ATTACH_FILES));

                // PermissionOverride permOverride = channel.getPermissionOverride(role);
                
                // System.out.println(permOverride.getAllowed());
                // System.out.println(permOverride.getDenied());
                // //permOverride.getAllowed();

            } else if (messageText.startsWith(",channelroleclear")) {

                //Variable setup
                Guild guild = event.getGuild();
                Set<GuildChannel> channels = ParseTools.parseChannels(messageText, guild);
                Set<IPermissionHolder> roles = ParseTools.parseRoles(messageText, guild);
                Set<Permission> perms = ParseTools.parsePerms(messageText, 0);
                
                //Switches channels around 
                if(channelInverse) {
                    Set<GuildChannel> allChannels = new HashSet<GuildChannel>(guild.getChannels());
                    for(GuildChannel c : allChannels)           //Removes all channels that are already synced to the category perms
                        if(c.getType() == ChannelType.CATEGORY)
                            for(GuildChannel c2 : ((Category)c).getChannels())
                                if (((ICategorizableChannel)c2).isSynced())
                                    channels.add(c2);

                    allChannels.removeAll(channels);
                    channels = allChannels;
                    allChannels = null;
                }

                //Remove all threads(They do not have modifiable perms)
                channels.removeIf(c -> c.getType() == ChannelType.GUILD_NEWS_THREAD || c.getType() == ChannelType.GUILD_PRIVATE_THREAD || c.getType() == ChannelType.GUILD_PUBLIC_THREAD);

                //Print out parsed things and confirm if user wishes to proceed
                EmbedBuilder confirmEmbedBuilder = new EmbedBuilder();
                String channelsMentionList = "";
                String channelsTypeList = "";
                String channelsIdList = "";
                String rolesNameList = "";
                String rolesClassList = "";
                String rolesIdList = "";
                String permList = "";

                //TODO: Investigate using \t for separating channel and associated stats to fix misalignment issue
                for(GuildChannel c : channels) {
                    channelsMentionList += c.getAsMention() + '\n';
                    channelsTypeList += c.getType().name() + '\n';
                    channelsIdList += c.getId() + '\n';
                }
                for (IPermissionHolder r : roles) {
                    rolesNameList += ((IMentionable)r).getAsMention() + '\n';
                    rolesClassList += r.getClass().getSimpleName().replace("Impl", "") + '\n';
                    rolesIdList += r.getId() + '\n';
                }
                for (Permission p : perms)
                    permList += p.getName() + '\n';

                confirmEmbedBuilder.setTitle("Clearing Permissions...");
                confirmEmbedBuilder.addField("Channels", channelsMentionList, true);
                confirmEmbedBuilder.addField("", channelsTypeList, true);
                confirmEmbedBuilder.addField("", channelsIdList, true);
                confirmEmbedBuilder.addField("Roles/Users", rolesNameList, true);
                confirmEmbedBuilder.addField("", rolesClassList, true);
                confirmEmbedBuilder.addField("", rolesIdList, true);
                confirmEmbedBuilder.addField("Permissions", permList, false);
                confirmEmbedBuilder.setFooter("Please confirm you'd like to make these changes by clicking below" + Emoji.fromFormatted("⬇️").getFormatted());

                //Create buttons and add to reply
                Button confirmButton = Button.success("memberManagerConfirm", "Confirm").withEmoji(Emoji.fromFormatted("✔️"));
                Button denyButton = Button.danger("memberMangerDeny", "Deny").withEmoji(Emoji.fromFormatted("✖️"));

                //Compile message and reply to message
                event.getMessage().replyEmbeds(confirmEmbedBuilder.build()).addActionRow(confirmButton, denyButton).setSuppressedNotifications(true).queue();

                // Guild guild = event.getGuild();
                // Role role = guild.getRoleById(1205225542882951300L);
                // TextChannel channel = guild.getTextChannelById(203282662608207872L);
                // TextChannelManager channelManager = channel.getManager();
                // HashSet<Permission> permsToRemove = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_ATTACH_FILES));
                // PermissionOverride permOverride = channel.getPermissionOverride(role);
                // EnumSet<Permission> allowed = permOverride.getAllowed();
                // EnumSet<Permission> denied = permOverride.getDenied();
                // //EnumSet<Permission> currentPerms = EnumSet.copyOf(allowed);
                // //currentPerms.addAll(denied);
                
                // allowed.removeAll(permsToRemove);
                // denied.removeAll(permsToRemove);

                // //channelManager.removePermissionOverride(role).queue();
                // if (allowed.isEmpty() && denied.isEmpty())
                //     channelManager.removePermissionOverride(role).queue();
                // else
                //     channelManager.putPermissionOverride(role, allowed, denied).queue();

            //} else if (messageText.startsWith(",devreset")) {

            } else if (messageText.startsWith(",undo")) {

            } else {
                event.getChannel().sendMessage("Unrecognized command!\nType in ',help' to view available commands").queue();
            }
        }
    }

}