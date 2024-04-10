package vesl.events;

import java.util.HashMap;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import vesl.PermAssignDataPack;
import vesl.tools.PermAssignTool;

public class ButtonInteractionListener extends ListenerAdapter {
    private HashMap<String, PermAssignDataPack> ProcessCache;

    public ButtonInteractionListener(HashMap<String, PermAssignDataPack> pc) {
        super();
        ProcessCache = pc;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        super.onButtonInteraction(event);
        
        //Prunes processes that are 30 or more seconds older
        ProcessCache.values().removeIf(v -> System.currentTimeMillis() - v.CREATED_AT > 30000);

        String buttonSelection = event.getComponentId();
        String pID = event.getGuild().getId() + '-' + event.getMessageId();
        Guild guild = event.getGuild();

        if (buttonSelection.equals("memberManagerConfirm")) {
            PermAssignDataPack data = ProcessCache.get(pID);
            if (data == null) {
                MessageEmbed currentEmbed = event.getMessage().getEmbeds().get(0);
                EmbedBuilder embed = new EmbedBuilder(currentEmbed).setFooter("This process has alread been denied or expired. Please generate a new one");
                event.editMessageEmbeds(embed.build()).setComponents().queue();
                return;
            }

            switch(data.TYPE) {
                case SET:
                    PermAssignTool.permsSetAll(guild, data.ROLES, data.CHANNELS, data.ALLOW, data.DENY);
                    break;
                case ADD:
                    PermAssignTool.permsAddAll(guild, data.ROLES, data.CHANNELS, data.ALLOW, data.DENY);
                    break;
                case CLEAR:
                    PermAssignTool.permsClearAll(guild, data.ROLES, data.CHANNELS, data.PERMS);
                    break;
            }
            ProcessCache.remove(pID);
            MessageEmbed currentEmbed = event.getMessage().getEmbeds().get(0);
            EmbedBuilder embed = new EmbedBuilder(currentEmbed).setFooter(Emoji.fromFormatted("✅").getFormatted() + " Successfully processed the command");
            event.editMessageEmbeds(embed.build()).setComponents().queue();
        } else if (buttonSelection.equals("memberMangerDeny")) {
            ProcessCache.remove(pID);
            MessageEmbed currentEmbed = event.getMessage().getEmbeds().get(0);
            EmbedBuilder embed = new EmbedBuilder(currentEmbed).setFooter("This process has already been denied or expired. Please generate a new one");
            event.editMessageEmbeds(embed.build()).setComponents().queue();
        }
    }
}
