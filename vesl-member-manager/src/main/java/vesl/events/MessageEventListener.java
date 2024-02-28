package vesl.events;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageEventListener extends ListenerAdapter {
    User interactingUser = null;

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
                HashSet<IMentionable> roles = parseRoles(messageText, event.getGuild());
                if (roles != null) {
                    for (IMentionable r : roles) {
                        output += r.getClass().getSimpleName() + " | " + r + "\n";
                    }
                }
                output += "----- [ CHANNELS ] -----\n";
                HashSet<GuildChannel> channels = parseChannels(messageText, event.getGuild());
                if (channels != null) {
                    for (GuildChannel c : channels) {
                        output += c.getClass().getSimpleName()  + " | " + c.getName() + "\n";
                    }
                }
                output += "----- [  PERMS   ] -----\n";
                HashSet<Permission> perms = parsePerms(messageText, 0);
                if (perms != null) {
                    for (Permission p : perms) {
                        output += p.getName() + "\n";
                    }
                }
                output += "----- [  ALLOW   ] -----\n";
                HashSet<Permission> allow = parsePerms(messageText, 1);
                if (allow != null) {
                    for (Permission p : allow) {
                        output += p.getName() + "\n";
                    }
                }
                output += "----- [   DENY   ] -----\n";
                HashSet<Permission> deny = parsePerms(messageText, 2);
                if (deny != null) {
                    for (Permission p : deny) {
                        output += p.getName() + "\n";
                    }
                }
                event.getChannel().sendMessage(output).queue();
            //} else if (messageText.startsWith(",channelroledeny")) {
                //event.getChannel().sendMessage("You've entered ',channelroledeny'").queue();
                //RoleManager manager = event.getGuild().getRoleById(1205225542882951300L).getManager();
                //manager.revokePermissions(Permission.VIEW_CHANNEL).queue();
                //TextChannelManager channelM = event.getGuild().getTextChannelById(203282662608207872L).getManager();
                //EnumSet<Permission> allow = EnumSet.of(Permission.valueOf("MANAGE_CHANNEL"));
                //EnumSet<Permission> deny = EnumSet.of(Permission.valueOf("MANAGE_THREADS"));
                //channelM.putRolePermissionOverride(1205225542882951300L, allow, deny).queue();
                //event.getGuild().getGuildChannelById(1205225542882951300L).getManager();
                //System.out.println(Permission.valueOf("ADMINISTRATOR"));
            } else if (messageText.startsWith(",channelroleset")) {
                //ArrayList<HashSet<Object>> details = parseOptions(messageText, event.getGuild());
                //ArrayList<IMentionable> roles;
                


                //if (channelInverse) {
                    
                //}
            } else if (messageText.startsWith(",channelroleadd")) {

            } else if (messageText.startsWith(",channelroleclear")) {

            } else if (messageText.startsWith(",undo")) {

            } else {
                event.getChannel().sendMessage("Unrecognized command!\nType in ',help' to view available commands").queue();
            }
        }
    }

    private HashSet<IMentionable> parseRoles(String strOptions, Guild guild) {
        if (countMatches(strOptions, "role:") != 1)
            return null;
        
        Pattern p = Pattern.compile("([^\\s]+)?(\\s)+");
        final Matcher matcher = p.matcher(strOptions);

        int start = strOptions.indexOf("role:") + 5;
            HashSet<IMentionable> parsedRoles = new HashSet<IMentionable>();
            if (strOptions.charAt(start) == '{' || strOptions.charAt(start + 1) == '{') {       //Parse multi entry(indicated by "{}")
                start = strOptions.indexOf('{', start) + 1;
                int end = strOptions.indexOf('}', start);
                List<String> tempList = Arrays.asList(strOptions.substring(start, end).split("\\s*,\\s*"));
                for(String s : tempList) {
                    s = s.trim();
                    IMentionable mention = null;
                    if (s.startsWith("<@")) {
                        if (s.charAt(2) == '&')     //Indicates Role
                            mention = guild.getRoleById(s.substring(3, s.length() - 1));
                        else                        //Indicates User
                            mention = guild.retrieveMemberById(s.substring(2, s.length() - 1)).complete();
                    } else {
                        try {
                            mention = guild.getRoleById(s);
                        } catch (NumberFormatException e) {}

                        if (mention == null) {
                            try {   //Try to parse member instead if can't get role
                                mention = guild.retrieveMemberById(s).complete();
                            } catch (Exception e1) {}       //Do nothing(don't parse) if can't parse long
                        }
                    }

                    if (mention != null)    //Make sure mention actually has a user, otherwise skip
                        parsedRoles.add(mention);
                }
            } else {                //Parse singular entry
                matcher.find(start);                            //Tries to find next non-whitespace after option indicator("role:")
                start = matcher.end();
                int end = strOptions.indexOf(' ', start);       //Tries to find the next space OR end of string
                if (end == -1) end = strOptions.length();
                String strRole = strOptions.substring(start, end).trim();
                IMentionable mention = null;
                if (strRole.startsWith("<@")) {         //Skip if not role or user(all of them start with this)
                    if (strRole.charAt(2) == '&')       //Indicates Role
                        mention = guild.getRoleById(strRole.substring(3, strRole.length() - 1));
                    else                                //Indicates User
                        mention = guild.retrieveMemberById(strRole.substring(2, strRole.length() - 1)).complete();
                } else {
                    try {       //Try to see if only number is present
                        mention = guild.getRoleById(strRole);
                    } catch (NumberFormatException e) {}

                    if (mention == null)
                        try {   //Try to parse member instead if can't get role
                            mention = guild.retrieveMemberById(strRole).complete();
                        } catch (Exception e1) {}    //Do nothing(don't parse) if can't parse long
                }

                if (mention != null)    //Make sure mention actually has a user or role(don't add null values)
                    parsedRoles.add(mention);
            }
            
            return parsedRoles;
    }

    private HashSet<GuildChannel> parseChannels(String strOptions, Guild guild) {
        if (countMatches(strOptions, "channel:") != 1)
            return null;

            Pattern p = Pattern.compile("([^\\s]+)?(\\s)+");
            final Matcher matcher = p.matcher(strOptions);

            HashSet<GuildChannel> parsedChannels = new HashSet<GuildChannel>();
            int start = strOptions.indexOf("channel:") + 8;       //Looks for where the channels begin
            
            if (strOptions.charAt(start) == '{' || strOptions.charAt(start + 1) == '{') {       //Parse multi entry(indicated by "{}")
                start = strOptions.indexOf('{', start) + 1;
                int end = strOptions.indexOf('}', start);
                List<String> tempList = Arrays.asList(strOptions.substring(start, end).split("\\s*,\\s*"));     //Splits up list, using commas as seperators
                for(String s : tempList) {                          //Parses each new seperate string
                    s = s.trim();                                   //Removes extra whitespaces
                    GuildChannel channel = null;

                    if (s.startsWith("<#"))                     //Verifies it's a channel, tries to parse number otherwise
                        channel = guild.getGuildChannelById(s.substring(2, s.length() - 1));  //Adds to list as a GuildChannel object
                    else
                        try {               //Try as if only guild channel ID were input
                            channel = guild.getGuildChannelById(s);
                        } catch (NumberFormatException e) {}    //Do nothing if can't parse number

                    if (channel != null)        //Verify channel is not null before adding
                        parsedChannels.add(channel);
                }
            
            } else {                //Parse singular entry
                matcher.find(start);    //Finds next non-whitespace character
                start = matcher.end();
                int end = strOptions.indexOf(' ', start);   //Finds next whitespace OR end of string
                if (end == -1) end = strOptions.length();
                String strChannel = strOptions.substring(start, end).trim();
                GuildChannel channel = null;
                if (strChannel.startsWith("<#"))        //Verifies it's a channel, skips otherwise
                    channel = guild.getGuildChannelById(strChannel.substring(2, strChannel.length() - 1));
                else
                    try {       //Try as if only guild channel ID were input
                        channel = guild.getGuildChannelById(strChannel);
                    } catch (NumberFormatException e) {}    //Do nothing if can't parse number
                
                if(channel != null)             //Verify channel is not null before adding
                    parsedChannels.add(channel);
            }
            return parsedChannels;              //Sends off the ArrayList in a package with a cute lil bow
    }

    private HashSet<Permission> parsePerms(String strOptions, int type) {
        String strSearch = "";
        
        if (type == 0)          //Search for "perms"
            strSearch = "perm:";
        else if (type == 1)     //Search for "allow"
            strSearch = "allow:";
        else if (type == 2)     //Search for "deny"
            strSearch = "deny:";
        else                    //Invalid type
            return null;

        if (countMatches(strOptions, strSearch) != 1)
            return null;

        Pattern p = Pattern.compile("([^\\s]+)?(\\s)+");
        final Matcher matcher = p.matcher(strOptions);
                
        int start = strOptions.indexOf(strSearch) + strSearch.length();
        HashSet<Permission> parsedPerms = new HashSet<Permission>();
    
        if (strOptions.charAt(start) == '{' || strOptions.charAt(start + 1) == '{') {       //Parse multi entry(indicated by "{}")
            start = strOptions.indexOf('{', start) + 1;
            int end = strOptions.indexOf('}', start);
            List<String> tempList = Arrays.asList(strOptions.substring(start, end).split("\\s*,\\s*"));     //Splits up list, using commas as seperators
            for(String s : tempList) {                          //Parses each new seperate string
                s = s.trim();                                   //Removes extra whitespaces
                try {
                    parsedPerms.add(Permission.valueOf(s.toUpperCase()));  //Adds to list as a Permission Enum object
                } catch (Exception e) {}        //We don't care about the broken ones for now
            }
        } else {                //Parse singular entry
            matcher.find(start);    //Finds next non-whitespace character
            start = matcher.end();
            int end = strOptions.indexOf(' ', start);   //Finds next whitespace OR end of string
            if (end == -1) end = strOptions.length();
            String strPerm = strOptions.substring(start, end).trim();
                    
            try {
                parsedPerms.add(Permission.valueOf(strPerm.toUpperCase()));  //Adds to list as a Permission Enum object
            } catch (Exception e) {}        //We don't care about the broken ones for now
        }
                       
        return parsedPerms;
    }

    private int countMatches(String input, String match) {      //Basic function to count number of matches in a string
        int lastIndex = 0;
        int counter = 0;

        while (lastIndex != -1) {
            lastIndex = input.indexOf(match, lastIndex);

            if (lastIndex != -1) {
                counter++;
                lastIndex++;
            }
        }
        return counter;
    }
}
