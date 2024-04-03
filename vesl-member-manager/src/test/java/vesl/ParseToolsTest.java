package vesl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import jakarta.json.Json;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import vesl.tools.ParseTools;

public class ParseToolsTest 
{
    private static String CREDENTIALS_DIRECTORY_PATH = "/key.json";

    @Test
    public void testCountMatches()
    {
        //Testing countMatches()
        assertEquals(1, ParseTools.countMatches("pastry cheese man bobblehead", "pastry"));
        assertEquals(3, ParseTools.countMatches("pastry cheese man bobblehead", "a"));
        assertEquals(1, ParseTools.countMatches("aaaaaa", "aaaa"));
    }

    @Test
    public void testParseRoles() throws InterruptedException
    {
        //Initializing variable to test with
        final String TOKEN = Json.createReader(App.class.getResourceAsStream(CREDENTIALS_DIRECTORY_PATH)).readObject().getString("api_key");
        JDABuilder jdab = JDABuilder.createLight(TOKEN);
        jdab.enableIntents(GatewayIntent.GUILD_MEMBERS);
        JDA jda = jdab.build().awaitReady();
        Guild guild = jda.getGuildById(203282662608207872L);
        Member member = guild.retrieveMemberById(182967935000772608L).complete();
        Role role = guild.getRoleById(1205225542882951300L);

        //Testing Role Singular parsing
        HashSet<IPermissionHolder> expected = new HashSet<IPermissionHolder>(Arrays.asList(role));
        HashSet<IPermissionHolder> output = ParseTools.parseRoles(",channelroleset role: <@&1205225542882951300> channel: <#203282662608207872> allow: MESSAGE_SEND", guild);
        assertThat(output, is(expected));

        //Testing User Singular parsing
        expected = new HashSet<IPermissionHolder>(Arrays.asList(member));
        output = ParseTools.parseRoles(",channelroleset role: <@182967935000772608>  channel: <#203282662608207872> allow: MESSAGE_SEND", guild);
        assertThat(output, is(expected));

        //Testing Role/User multi parsing
        expected = new HashSet<IPermissionHolder>(Arrays.asList(member, role));
        output = ParseTools.parseRoles(",channelroleset role: {<@182967935000772608>, <@&1205225542882951300>} channel: <#203282662608207872> allow: MESSAGE_SEND", guild);
        assertThat(output, is(expected));

        //Testing Role ID parsing
        expected = new HashSet<IPermissionHolder>(Arrays.asList(role));
        output = ParseTools.parseRoles(",channelroleset role: 1205225542882951300 channel: <#203282662608207872> allow: MESSAGE_SEND", guild);
        assertThat(output, is(expected));

        //Testing User ID parsing
        expected = new HashSet<IPermissionHolder>(Arrays.asList(member));
        output = ParseTools.parseRoles(",channelroleset role: 182967935000772608 channel: <#203282662608207872> allow: MESSAGE_SEND", guild);
        assertThat(output, is(expected));

        //Testing Role/User Anti-dupe
        expected = new HashSet<IPermissionHolder>(Arrays.asList(member, role));
        output = ParseTools.parseRoles(",channelroleset role: {<@182967935000772608>, 182967935000772608, <@&1205225542882951300>, 1205225542882951300 } channel: <#203282662608207872> allow: MESSAGE_SEND", guild);
        assertThat(output, is(expected));

        //Testing error handling
        output = ParseTools.parseRoles(",channelroleset role: {<@182967935000772608>, 182967935000772608, <@&1205225542882951300>, 1205225542882951300 channel: <#203282662608207872> allow: MESSAGE_SEND", guild);
        assertNull(output);

        output = ParseTools.parseRoles(",channelroleset role: {<@182967935000772608, 12052255428951300, #general } channel: <#203282662608207872> allow: MESSAGE_SEND", guild);
        assertNull(output);

        jda.shutdownNow();
    }

    @Test
    public void testParseChannels() throws InterruptedException
    {
        //Initializing variables to test with
        final String TOKEN = Json.createReader(App.class.getResourceAsStream(CREDENTIALS_DIRECTORY_PATH)).readObject().getString("api_key");
        JDABuilder jdab = JDABuilder.createLight(TOKEN);
        jdab.enableIntents(GatewayIntent.GUILD_MEMBERS);
        JDA jda = jdab.build().awaitReady();
        Guild guild = jda.getGuildById(203282662608207872L);
        TextChannel textChannel = guild.getTextChannelById(203282662608207872L);
        VoiceChannel voiceChannel = guild.getVoiceChannelById(1203035987802988585L);
        StageChannel stageChannel = guild.getStageChannelById(1225114961823666217L);
        NewsChannel newsChannel = guild.getNewsChannelById(1225114793308983487L);
        ForumChannel forumChannel = guild.getForumChannelById(1225114742868410429L);
        ThreadChannel threadChannel = guild.getThreadChannelById(1222690971125547140L);

        //Testing Channel Singular parsing
        HashSet<GuildChannel> expected = new HashSet<GuildChannel>(Arrays.asList(textChannel));
        HashSet<GuildChannel> output = ParseTools.parseChannels(",channelroleset role: <@&1205225542882951300> channel: <#203282662608207872> allow: MESSAGE_SEND", guild);
        assertThat(output, is(expected));

        //Testing Channel multi parse
        expected = new HashSet<GuildChannel>(Arrays.asList(textChannel, voiceChannel, stageChannel, newsChannel, forumChannel, threadChannel));
        output = ParseTools.parseChannels(",channelroleset role: <@&1205225542882951300> channel: {<#203282662608207872>, <#1203035987802988585>, <#1225114961823666217>, <#1225114793308983487>, <#1225114742868410429>, <#1222690971125547140>} allow: MESSAGE_SEND", guild);
        assertThat(output, is(expected));

        //Testing Channel ID parse
        expected = new HashSet<GuildChannel>(Arrays.asList(textChannel));
        output = ParseTools.parseChannels(",channelroleset role: <@&1205225542882951300> channel: 203282662608207872 allow: MESSAGE_SEND", guild);
        assertThat(output, is(expected));

        //Testing Channel Anti-dupe
        expected = new HashSet<GuildChannel>(Arrays.asList(textChannel, voiceChannel));
        output = ParseTools.parseChannels(",channelroleset role: <@&1205225542882951300> channel: {<#203282662608207872>, 203282662608207872, <#1203035987802988585>, 1203035987802988585} allow: MESSAGE_SEND", guild);
        assertThat(output, is(expected));

        //Testing error handling
        output = ParseTools.parseChannels(",channelroleset role: <@&1205225542882951300> channel: {<#203282662608207872>, 203282662608207872, <#1203035987802988585>, 1203035987802988585 allow: MESSAGE_SEND", guild);
        assertNull(output);

        output = ParseTools.parseChannels(",channelroleset role: <@&1205225542882951300> channel: {<#203282662608207872, 1203035987802, @AAAAA } allow: MESSAGE_SEND", guild);
        assertNull(output);

        jda.shutdownNow();
    }

    @Test
    public void testParsePerms()
    {
        //Testing Permission singular parse
        HashSet<Permission> expected = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_SEND));
        HashSet<Permission> output = ParseTools.parsePerms(",channelroleset role: <@&1205225542882951300> channel: <#203282662608207872> allow: MESSAGE_SEND", 1);
        assertThat(output, is(expected));

        //Testing Permission multi parse
        expected = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_SEND, Permission.KICK_MEMBERS));
        output = ParseTools.parsePerms(",channelroleset role: <@&1205225542882951300> channel: <#203282662608207872> allow: {MESSAGE_SEND, KICK_MEMBERS}", 1);
        assertThat(output, is(expected));

        //Testing Permission Anti-dupe
        expected = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_SEND, Permission.KICK_MEMBERS));
        output = ParseTools.parsePerms(",channelroleset role: <@&1205225542882951300> channel: <#203282662608207872> allow: {MESSAGE_SEND, MESSAGE_SEND, KICK_MEMBERS, KICK_MEMBERS}", 1);
        assertThat(output, is(expected));

        //Testing "deny:" detection
        expected = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_SEND));
        output = ParseTools.parsePerms(",channelroleset role: <@&1205225542882951300> channel: <#203282662608207872> deny: MESSAGE_SEND", 2);
        assertThat(output, is(expected));

        //Testing "perm:" detection
        expected = new HashSet<Permission>(Arrays.asList(Permission.KICK_MEMBERS));
        output = ParseTools.parsePerms(",channelroleclear role: <@&1205225542882951300> channel: <#203282662608207872> perm: KICK_MEMBERS", 0);
        assertThat(output, is(expected));

        //Testing error handling
        output = ParseTools.parsePerms(",channelroleset role: <@&1205225542882951300> channel: <#203282662608207872> allow: {MESSAGE_SEND", 1);
        assertNull(output);

        output = ParseTools.parsePerms(",channelroleset role: <@&1205225542882951300> channel: <#203282662608207872> allow: MESGE_SEND", 1);
        assertNull(output);
    }
}
