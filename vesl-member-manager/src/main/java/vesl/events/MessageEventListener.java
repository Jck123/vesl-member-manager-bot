package vesl.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.entities.Guild;
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
        //event.getChannel().sendMessage(messageText).queue();
        if (messageText.startsWith(",")) {
            event.getMessage().addReaction(Emoji.fromUnicode("U+2611")).queue();
            if (messageText.startsWith(",help")) {
                event.getChannel().sendMessage("You've entered ',help'").queue();
            } else if (messageText.startsWith(",channelroleallow")) {
                event.getChannel().sendMessage("You've entered ',channelroleallow'").queue();
                ArrayList<ArrayList<Object>> results = parseOptions(messageText, event.getGuild());
                System.out.println("===== [ RESULTS  ] =====");
                System.out.println("----- [  ROLES   ] -----");
                if (results.get(0) != null) {
                    for (Object o : results.get(0)) {
                        System.out.println(o.getClass().getSimpleName() + " | " + o);
                    }
                }
                System.out.println("----- [ CHANNELS ] -----");
                if (results.get(2) != null) {
                    for (Object o : results.get(2)) {
                        if (o.getClass().equals(String.class))
                            System.out.println(o.toString());
                        else
                            System.out.println(o.getClass().getSimpleName()  + " | " + ((GuildChannel)o).getName());
                    }
                }
            } else if (messageText.startsWith(",channelroledeny")) {
                event.getChannel().sendMessage("You've entered ',channelroledeny'").queue();
            } else if (messageText.startsWith(",channelroleclear")) {
                event.getChannel().sendMessage("You've entered ',channelroleclear'").queue();
            } else if (messageText.startsWith(",undo")) {
                event.getChannel().sendMessage("You've entered ',undo'").queue();
            } else {
                event.getChannel().sendMessage("Unrecognized command!\nType in ',help' to view available commands").queue();
            }
        }
    }

    private ArrayList<ArrayList<Object>> parseOptions(String strOptions, Guild guild) {
        ArrayList<ArrayList<Object>> options = new ArrayList<ArrayList<Object>>(Arrays.asList(null, null, null, null));
        Pattern p = Pattern.compile("([^\\s]+)?(\\s)+");
        final Matcher matcher = p.matcher(strOptions);

        if (countMatches(strOptions, "role:") == 1) {   //Parses roles and users from message
            int start = strOptions.indexOf("role:") + 5;
            ArrayList<Object> parsedRoles = new ArrayList<Object>();
            if (strOptions.charAt(start) == '{' || strOptions.charAt(start + 1) == '{') {       //Parse multi entry(indicated by "{}")
                start = strOptions.indexOf('{', start) + 1;
                int end = strOptions.indexOf('}', start);
                List<String> tempList = Arrays.asList(strOptions.substring(start, end).split("\\s*,\\s*"));
                for(String s : tempList) {
                    s = s.trim();
                    if (!s.startsWith("<@"))            //Skip if not role or user(all of them start with this)
                        continue;
                    if (s.charAt(2) == '&')     //Indicates Role
                        parsedRoles.add(guild.getRoleById(s.substring(3, s.length() - 1)));
                    else                        //Indicates User
                        parsedRoles.add(guild.retrieveMemberById(s.substring(2, s.length() - 1)).complete());
                }
            
            } else {                //Parse singular entry
                matcher.find(start);                            //Tries to find next non-whitespace after option indicator("role:")
                start = matcher.end();
                int end = strOptions.indexOf(' ', start);       //Tries to find the next space OR end of string
                if (end == -1) end = strOptions.length();
                String strRole = strOptions.substring(start, end).trim();
                if (strRole.startsWith("<@")) {         //Skip if not role or user(all of them start with this)
                    if (strRole.charAt(2) == '&')       //Indicates Role
                        parsedRoles.add(guild.getRoleById(strRole.substring(3, strRole.length() - 1)));
                    else                                //Indicates User
                        parsedRoles.add(guild.retrieveMemberById(strRole.substring(2, strRole.length() - 1)).complete());
                }
            }
            options.set(0, parsedRoles);                //Sends off the ArrayList in a package with a cute lil bow
        }

        if (countMatches(strOptions, "perm:") == 1) {

        }

        //Parses any channels if either include xor exclude are found
        if (countMatches(strOptions, "include:") == 1 ^ countMatches(strOptions, "exclude:") == 1) {
            ArrayList<Object> parsedChannels = new ArrayList<Object>();
            int start = strOptions.indexOf("clude:") + 6;       //Looks for where the channels begin(both conveiniently end in "clude:")
            parsedChannels.add(strOptions.substring(start - 8, start - 1));     //First object in this ArrList indicates if this is exclusionary or inclusionary

            if (strOptions.charAt(start) == '{' || strOptions.charAt(start + 1) == '{') {       //Parse multi entry(indicated by "{}")
                start = strOptions.indexOf('{', start) + 1;
                int end = strOptions.indexOf('}', start);
                List<String> tempList = Arrays.asList(strOptions.substring(start, end).split("\\s*,\\s*"));     //Splits up list, using commas as seperators
                for(String s : tempList) {                          //Parses each new seperate string
                    s = s.trim();                                   //Removes extra whitespaces
                    if (!s.startsWith("<#")) continue;              //Verifies it's a channel, skips otherwise

                    parsedChannels.add(guild.getGuildChannelById(s.substring(2, s.length() - 1)));  //Adds to list as a GuildChannel object
                }
            
            } else {                //Parse singular entry
                matcher.find(start);    //Finds next non-whitespace character
                start = matcher.end();
                int end = strOptions.indexOf(' ', start);   //Finds next whitespace OR end of string
                if (end == -1) end = strOptions.length();
                String strChannel = strOptions.substring(start, end).trim();
                if (strChannel.startsWith("<#"))        //Verifies it's a channel, skips otherwise
                    parsedChannels.add(guild.getGuildChannelById(strChannel.substring(2, strChannel.length() - 1)));
            }
            options.set(2, parsedChannels);         //Sends off the ArrayList in a package with a cute lil bow
        }

        if (countMatches(strOptions, "conditional:") == 1) {
            //System.out.println("Conditionals found!");
            //To be implmented later
        }
        
        return options;
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
