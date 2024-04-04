package vesl;

import java.util.HashMap;

import jakarta.json.Json;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import vesl.events.ButtonInteractionListener;
import vesl.events.MessageEventListener;

public class App {
    private static String CREDENTIALS_DIRECTORY_PATH = "/key.json";
    private static HashMap<String, PermAssignDataPack> ProcessCache = null;
    public static void main(String[] args) {
        final String TOKEN = Json.createReader(App.class.getResourceAsStream(CREDENTIALS_DIRECTORY_PATH)).readObject().getString("api_key");
        ProcessCache = new HashMap<String, PermAssignDataPack>();
        JDABuilder jdab = JDABuilder.createLight(TOKEN);
        jdab.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT);
        jdab.enableCache(CacheFlag.MEMBER_OVERRIDES);
        jdab.addEventListeners(new MessageEventListener(ProcessCache), new ButtonInteractionListener(ProcessCache));
        
        jdab.build();
    }
}
