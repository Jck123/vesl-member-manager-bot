package vesl;

import jakarta.json.Json;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import vesl.events.MessageEventListener;

public class App {
    private static String CREDENTIALS_DIRECTORY_PATH = "/key.json";
    public static void main( String[] args ) {
        final String TOKEN = Json.createReader(App.class.getResourceAsStream(CREDENTIALS_DIRECTORY_PATH)).readObject().getString("api_key");
        JDABuilder jdab = JDABuilder.createLight(TOKEN);
        jdab.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT);
        jdab.addEventListeners(new MessageEventListener());

        jdab.build();
    }
}
