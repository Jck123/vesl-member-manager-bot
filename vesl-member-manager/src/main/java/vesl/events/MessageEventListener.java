package vesl.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
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
                System.out.println("===== [ RESULTS ] =====");
                for (Object o : results.get(0))
                    System.out.println(o);
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

        if (countMatches(strOptions, "role:") == 1) {
            int start = strOptions.indexOf("role:") + 5;
            ArrayList<Object> parsedRoles = new ArrayList<Object>();
            if (strOptions.charAt(start) == '{' || strOptions.charAt(start + 1) == '{') {       //Parse multi entry(indicated by "{}")
                start = strOptions.indexOf('{', start) + 1;
                int end = strOptions.indexOf('}', start);
                List<String> tempList = Arrays.asList(strOptions.substring(start, end).split("\\s*,\\s*"));
                for(String s : tempList) {
                    s = s.trim();
                    if (!s.startsWith("<@"))
                        continue;
                    if (s.charAt(2) == '&')     //Indicates Role
                        parsedRoles.add(guild.getRoleById(s.substring(3, s.length() - 1)));
                    else                        //Indicates User
                        parsedRoles.add(guild.retrieveMemberById(s.substring(2, s.length() - 1)).complete());
                }
            
            } else {                //Parse singular entry
                matcher.find(start);
                start = matcher.end();
                int end = strOptions.indexOf(' ', start);
                if (end == -1) end = strOptions.length();
                String strRole = strOptions.substring(start, end).trim();
                if (strRole.startsWith("<@")) {
                    if (strRole.charAt(2) == '&')
                        parsedRoles.add(guild.getRoleById(strRole.substring(3, strRole.length() - 1)));
                    else
                        parsedRoles.add(guild.retrieveMemberById(strRole.substring(2, strRole.length() - 1)).complete());
                }
            }
            options.set(0, parsedRoles);
        }

        if (countMatches(strOptions, "perm:") == 1) {
            System.out.println("Perms found!");
        }

        if (countMatches(strOptions, "include:") == 1 ^ countMatches(strOptions, "exclude:") == 1) {
            System.out.println("Channels found!");
        }

        if (countMatches(strOptions, "conditional:") == 1) {
            System.out.println("Conditionals found!");
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
