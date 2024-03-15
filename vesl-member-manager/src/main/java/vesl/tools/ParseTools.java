package vesl.tools;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

public class ParseTools {
    public static HashSet<IPermissionHolder> parseRoles(String strOptions, Guild guild) {
        if (countMatches(strOptions, "role:") != 1)
            return null;
        
        Pattern p = Pattern.compile("([^\\s]+)?(\\s)+");
        final Matcher matcher = p.matcher(strOptions);

        int start = strOptions.indexOf("role:") + 5;
            HashSet<IPermissionHolder> parsedRoles = new HashSet<IPermissionHolder>();
            if (strOptions.charAt(start) == '{' || strOptions.charAt(start + 1) == '{') {       //Parse multi entry(indicated by "{}")
                start = strOptions.indexOf('{', start) + 1;
                int end = strOptions.indexOf('}', start);
                int nextOpt = strOptions.indexOf(':', start);
                if (end == -1 || (nextOpt != -1 && end > nextOpt))
                    return null;        //If no closing bracket is found or it is found after a colon, return null(inicating error)

                List<String> tempList = Arrays.asList(strOptions.substring(start, end).split("\\s*,\\s*"));
                for(String s : tempList) {
                    s = s.trim();
                    IPermissionHolder mention = null;
                    if (s.startsWith("<@")) {
                        if (s.charAt(2) == '&')     //Indicates Role
                            try {
                                mention = guild.getRoleById(s.substring(3, s.length() - 1));
                            } catch (NumberFormatException e) {}
                        else                        //Indicates User
                            try {
                            mention = guild.retrieveMemberById(s.substring(2, s.length() - 1)).complete();
                            } catch (Exception e) {
                                if (!(e instanceof ErrorResponseException || e instanceof IllegalArgumentException || e instanceof NumberFormatException))
                                    throw e;
                            }
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
                IPermissionHolder mention = null;
                if (strRole.startsWith("<@")) {
                    if (strRole.charAt(2) == '&')     //Indicates Role
                        try {
                            mention = guild.getRoleById(strRole.substring(3, strRole.length() - 1));
                        } catch (NumberFormatException e) {}
                    else                        //Indicates User
                        try {
                        mention = guild.retrieveMemberById(strRole.substring(2, strRole.length() - 1)).complete();
                        } catch (Exception e) {
                            if (!(e instanceof ErrorResponseException || e instanceof IllegalArgumentException || e instanceof NumberFormatException))
                                throw e;
                        }
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
            
            if (parsedRoles.isEmpty())
                return null;

            return parsedRoles;
    }

    public static HashSet<GuildChannel> parseChannels(String strOptions, Guild guild) {
        if (countMatches(strOptions, "channel:") != 1)
            return null;

            Pattern p = Pattern.compile("([^\\s]+)?(\\s)+");
            final Matcher matcher = p.matcher(strOptions);

            HashSet<GuildChannel> parsedChannels = new HashSet<GuildChannel>();
            int start = strOptions.indexOf("channel:") + 8;       //Looks for where the channels begin
            
            if (strOptions.charAt(start) == '{' || strOptions.charAt(start + 1) == '{') {       //Parse multi entry(indicated by "{}")
                start = strOptions.indexOf('{', start) + 1;
                int end = strOptions.indexOf('}', start);
                int nextOpt = strOptions.indexOf(':', start);
                if (end == -1 || (nextOpt != -1 && end > nextOpt))
                    return null;        //If no closing bracket is found or it is found after a colon, return null(inicating error)
                
                List<String> tempList = Arrays.asList(strOptions.substring(start, end).split("\\s*,\\s*"));     //Splits up list, using commas as seperators
                for(String s : tempList) {                          //Parses each new seperate string
                    s = s.trim();                                   //Removes extra whitespaces
                    GuildChannel channel = null;

                    if (s.startsWith("<#"))                     //Verifies it's a channel, tries to parse number otherwise
                        try {
                            channel = guild.getGuildChannelById(s.substring(2, s.length() - 1));  //Adds to list as a GuildChannel object
                        } catch (NumberFormatException e) {}
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
                    try {
                        channel = guild.getGuildChannelById(strChannel.substring(2, strChannel.length() - 1));
                    } catch (NumberFormatException e) {}
                else
                    try {       //Try as if only guild channel ID were input
                        channel = guild.getGuildChannelById(strChannel);
                    } catch (NumberFormatException e) {}    //Do nothing if can't parse number
                
                if(channel != null)             //Verify channel is not null before adding
                    parsedChannels.add(channel);
            }

            if (parsedChannels.isEmpty())
                return null;
            return parsedChannels;              //Sends off the ArrayList in a package with a cute lil bow
    }

    public static HashSet<Permission> parsePerms(String strOptions, int type) {
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
            int nextOpt = strOptions.indexOf(':', start);
            if (end == -1 || (nextOpt != -1 && end > nextOpt))
                return null;        //If no closing bracket is found or it is found after a colon, return null(inicating error)
            
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
        
        if (parsedPerms.isEmpty())
            return null;
        return parsedPerms;
    }

    public static int countMatches(String input, String match) {      //Basic function to count number of matches in a string
        int lastIndex = 0;
        int counter = 0;

        while (lastIndex != -1) {
            lastIndex = input.indexOf(match, lastIndex);

            if (lastIndex != -1) {
                counter++;
                lastIndex += match.length();
            }
        }
        return counter;
    }
}
