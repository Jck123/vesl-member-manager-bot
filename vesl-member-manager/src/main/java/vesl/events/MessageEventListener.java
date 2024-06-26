package vesl.events;

import java.util.HashMap;
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
import vesl.PermAssignDataPack;
import vesl.PermAssignDataType;
import vesl.tools.ParseTools;

public class MessageEventListener extends ListenerAdapter {
    private HashMap<String, PermAssignDataPack> ProcessCache;

    public MessageEventListener(HashMap<String, PermAssignDataPack> pc) {
        super();
        ProcessCache = pc;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        super.onMessageReceived(event);

        //Prunes processes that are 30 or more seconds older
        ProcessCache.values().removeIf(v -> System.currentTimeMillis() - v.CREATED_AT > 30000);

        if (event.getAuthor().isBot()) return;

        String messageText = event.getMessage().getContentRaw();
        if (messageText.startsWith(",")) {
            event.getMessage().addReaction(Emoji.fromUnicode("U+2611")).queue();

            Boolean channelInverse = messageText.contains("-ci") ? true : false;

            if (messageText.startsWith(",help")) {
                event.getChannel().sendMessage("You've entered ',help'\nThere is no help\n...Go home").queue();

                for (int i = 0; i < 11; i++) {
                    try {Thread.sleep(1000);} catch (InterruptedException e) {}
                    System.out.println(i);
                }

            } else if (messageText.startsWith(",debugCachePrint")) {
                ProcessCache.forEach((k, v) -> {
                    System.out.println(k);
                    System.out.println("TYPE: " + v.TYPE);
                    System.out.println("\tROLES:");
                    for (IPermissionHolder r : v.ROLES)
                        System.out.println("\t\t" + r.toString());
                    System.out.println("\tCHANNELS:");
                    for (GuildChannel c : v.CHANNELS)
                        System.out.println("\t\t" + c.getName() + "\t|\t" + c.getId());
                    if (v.TYPE != PermAssignDataType.CLEAR) {
                        System.out.println("\tALLOW:");
                        for (Permission p : v.ALLOW)
                            System.out.println("\t\t" + p);
                        System.out.println("\tDENY:");
                        for (Permission p : v.DENY)
                            System.out.println("\t\t" + p);
                    } else {
                        System.out.println("\tPERMS:");
                        for (Permission p : v.PERMS)
                            System.out.println("\t\t" + p);
                    }
                    System.out.println("\tCREATED_AT:" + v.CREATED_AT);
                });


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
                    for(GuildChannel c : allChannels)          //Removes all channels that are already synced to the category perms
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
                if (allow != null)
                    for (Permission p : allow)
                        allowList += p.getName() + '\n';
                if (deny != null)
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

                //Need to do this for some reason???
                final Set<GuildChannel> chan = new HashSet<GuildChannel>(channels);

                //Compile message and reply to message
                event.getMessage().replyEmbeds(confirmEmbedBuilder.build()).addActionRow(confirmButton, denyButton).setSuppressedNotifications(true).queue(
                    (message) -> { 
                        ProcessCache.put(guild.getId() + '-' + message.getId(), new PermAssignDataPack(PermAssignDataType.SET, roles, chan, allow, deny));
                    }
                );
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

                //Need to do this for some reason???
                final Set<GuildChannel> chan = new HashSet<GuildChannel>(channels);

                //Compile message and reply to message
                event.getMessage().replyEmbeds(confirmEmbedBuilder.build()).addActionRow(confirmButton, denyButton).setSuppressedNotifications(true).queue(
                    (message) -> { 
                        ProcessCache.put(guild.getId() + '-' + message.getId(), new PermAssignDataPack(PermAssignDataType.ADD, roles, chan, allow, deny));
                    }
                );
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

                //Need to do this for some reason???
                final Set<GuildChannel> chan = new HashSet<GuildChannel>(channels);

                //Compile message and reply to message
                event.getMessage().replyEmbeds(confirmEmbedBuilder.build()).addActionRow(confirmButton, denyButton).setSuppressedNotifications(true).queue(
                    (message) -> { 
                        ProcessCache.put(guild.getId() + '-' + message.getId(), new PermAssignDataPack(roles, chan, perms));
                    }
                );


            } else if (messageText.startsWith(",channelrolefullclear")) {

                //Variable setup
                Guild guild = event.getGuild();
                Set<GuildChannel> channels = ParseTools.parseChannels(messageText, guild);
                Set<IPermissionHolder> roles = ParseTools.parseRoles(messageText, guild);

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

                confirmEmbedBuilder.setTitle("Clearing All Permissions...");
                confirmEmbedBuilder.addField("Channels", channelsMentionList, true);
                confirmEmbedBuilder.addField("", channelsTypeList, true);
                confirmEmbedBuilder.addField("", channelsIdList, true);
                confirmEmbedBuilder.addField("Roles/Users", rolesNameList, true);
                confirmEmbedBuilder.addField("", rolesClassList, true);
                confirmEmbedBuilder.addField("", rolesIdList, true);
                confirmEmbedBuilder.setFooter("Please confirm you'd like to make these changes by clicking below" + Emoji.fromFormatted("⬇️").getFormatted());

                //Create buttons and add to reply
                Button confirmButton = Button.success("memberManagerConfirm", "Confirm").withEmoji(Emoji.fromFormatted("✔️"));
                Button denyButton = Button.danger("memberMangerDeny", "Deny").withEmoji(Emoji.fromFormatted("✖️"));

                //Need to do this for some reason???
                final Set<GuildChannel> chan = new HashSet<GuildChannel>(channels);

                //Compile message and reply to message
                event.getMessage().replyEmbeds(confirmEmbedBuilder.build()).addActionRow(confirmButton, denyButton).setSuppressedNotifications(true).queue(
                    (message) -> { 
                        ProcessCache.put(guild.getId() + '-' + message.getId(), new PermAssignDataPack(roles, chan));
                    }
                );

            //} else if (messageText.startsWith(",undo")) {
                //TODO: Add undo functionality OR add undo button
            } else {
                event.getChannel().sendMessage("Unrecognized command!\nType in ',help' to view available commands").queue();
            }
        }
    }
}