package vesl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import jakarta.json.Json;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IPermissionHolder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import vesl.tools.PermAssignTool;

public class PermAssignToolTest {
    private static String CREDENTIALS_DIRECTORY_PATH = "/key.json";

    @Test
    @SuppressWarnings("deprecation")
    public void testPermsAdd() throws InterruptedException {
        
        //Client setup
        final String TOKEN = Json.createReader(App.class.getResourceAsStream(CREDENTIALS_DIRECTORY_PATH)).readObject().getString("api_key");
        JDABuilder jdab = JDABuilder.createLight(TOKEN);
        jdab.enableIntents(GatewayIntent.GUILD_MEMBERS);
        jdab.enableCache(CacheFlag.MEMBER_OVERRIDES);
        JDA jda = jdab.build().awaitReady();
        
        //Object setup
        Guild guild = jda.getGuildById(203282662608207872L);
        Role role = guild.getRoleById(1205225542882951300L);
        TextChannel channel = guild.getTextChannelById(1230586192848752762L);
        TextChannelManager channelManager = channel.getManager();
        Set<Permission> expectedAllow = null;
        Set<Permission> expectedDeny = null;
        Set<Permission> actualAllow = null;
        Set<Permission> actualDeny = null;
        Set<Permission> inputAllow = null;
        Set<Permission> inputDeny = null;

        //Cleaning up any potential leftover changes(Clean the slate)
        channelManager.removePermissionOverride(role).complete();

        //Testing perm addition
        expectedAllow = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_SEND, Permission.MESSAGE_ADD_REACTION));
        expectedDeny = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EXT_EMOJI));
        inputAllow = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_SEND, Permission.MESSAGE_ADD_REACTION));
        inputDeny = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EXT_EMOJI));

        assertEquals(0, PermAssignTool.permsAdd(guild, role, channel, inputAllow, inputDeny));

        Thread.sleep(3000);

        actualAllow = channel.getPermissionOverride(role).getAllowed();
        actualDeny = channel.getPermissionOverride(role).getDenied();

        assertThat(actualAllow, is(expectedAllow));
        assertThat(actualDeny, is(expectedDeny));

        //Testing perm adds on top of previous perms
        expectedAllow = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_SEND, Permission.MESSAGE_ADD_REACTION, Permission.MANAGE_THREADS, Permission.MESSAGE_EMBED_LINKS));
        expectedDeny = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EXT_EMOJI, Permission.MESSAGE_ATTACH_VOICE_MESSAGE, Permission.MESSAGE_SEND_IN_THREADS));
        inputAllow = new HashSet<Permission>(Arrays.asList(Permission.MANAGE_THREADS, Permission.MESSAGE_EMBED_LINKS));
        inputDeny = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_ATTACH_VOICE_MESSAGE, Permission.MESSAGE_SEND_IN_THREADS));

        assertEquals(0, PermAssignTool.permsAdd(guild, role, channel, inputAllow, inputDeny));
        
        Thread.sleep(3000);

        actualAllow = channel.getPermissionOverride(role).getAllowed();
        actualDeny = channel.getPermissionOverride(role).getDenied();

        assertThat(actualAllow, is(expectedAllow));
        assertThat(actualDeny, is(expectedDeny));


        //Error handling
        expectedAllow = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_SEND, Permission.MESSAGE_ADD_REACTION, Permission.MANAGE_THREADS, Permission.MESSAGE_EMBED_LINKS));
        expectedDeny = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EXT_EMOJI, Permission.MESSAGE_ATTACH_VOICE_MESSAGE, Permission.MESSAGE_SEND_IN_THREADS));
        inputAllow = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_TTS));
        inputDeny = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_EXT_STICKER));

        //Null Guild
        assertEquals(-1, PermAssignTool.permsAdd(null, null, null, null, null));

        Thread.sleep(3000);

        actualAllow = channel.getPermissionOverride(role).getAllowed();
        actualDeny = channel.getPermissionOverride(role).getDenied();

        assertThat(actualAllow, is(expectedAllow));
        assertThat(actualDeny, is(expectedDeny));

        //Null Role
        assertEquals(-2, PermAssignTool.permsAdd(guild, null, null, null, null));

        Thread.sleep(3000);

        actualAllow = channel.getPermissionOverride(role).getAllowed();
        actualDeny = channel.getPermissionOverride(role).getDenied();

        assertThat(actualAllow, is(expectedAllow));
        assertThat(actualDeny, is(expectedDeny));

        //Null GuildChannel
        assertEquals(-3, PermAssignTool.permsAdd(guild, role, null, null, null));

        Thread.sleep(3000);

        actualAllow = channel.getPermissionOverride(role).getAllowed();
        actualDeny = channel.getPermissionOverride(role).getDenied();

        assertThat(actualAllow, is(expectedAllow));
        assertThat(actualDeny, is(expectedDeny));

        //Null Allow AND Deny List
        assertEquals(-4, PermAssignTool.permsAdd(guild, role, channel, null, null));

        Thread.sleep(3000);

        actualAllow = channel.getPermissionOverride(role).getAllowed();
        actualDeny = channel.getPermissionOverride(role).getDenied();

        assertThat(actualAllow, is(expectedAllow));
        assertThat(actualDeny, is(expectedDeny));

        //Clean up and shut down
        channelManager.removePermissionOverride(role).complete();
        Thread.sleep(3000);
        jda.shutdownNow();
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testPermsClear() throws InterruptedException {
        
        //Client setup
        final String TOKEN = Json.createReader(App.class.getResourceAsStream(CREDENTIALS_DIRECTORY_PATH)).readObject().getString("api_key");
        JDABuilder jdab = JDABuilder.createLight(TOKEN);
        jdab.enableIntents(GatewayIntent.GUILD_MEMBERS);
        jdab.enableCache(CacheFlag.MEMBER_OVERRIDES);
        JDA jda = jdab.build().awaitReady();
        
        //Object setup
        Guild guild = jda.getGuildById(203282662608207872L);
        Role role = guild.getRoleById(1205225542882951300L);
        TextChannel channel = guild.getTextChannelById(1230586192848752762L);
        TextChannelManager channelManager = channel.getManager();
        Set<Permission> expectedAllow = null;
        Set<Permission> expectedDeny = null;
        Set<Permission> actualAllow = null;
        Set<Permission> actualDeny = null;
        Set<Permission> inputPerms = null;

        //Cleaning up any potential leftover changes(Clean the slate)
        channelManager.removePermissionOverride(role).complete();
        channelManager.putPermissionOverride(role, new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_SEND, Permission.MESSAGE_ADD_REACTION, Permission.MANAGE_THREADS, Permission.MESSAGE_EMBED_LINKS)), new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EXT_EMOJI, Permission.MESSAGE_ATTACH_VOICE_MESSAGE, Permission.MESSAGE_SEND_IN_THREADS))).complete();

        Thread.sleep(3000);

        //Testing perm clearing
        expectedAllow = new HashSet<Permission>(Arrays.asList(Permission.MANAGE_THREADS, Permission.MESSAGE_EMBED_LINKS));
        expectedDeny = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_ATTACH_VOICE_MESSAGE, Permission.MESSAGE_SEND_IN_THREADS));
        inputPerms = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_SEND, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_EXT_EMOJI));

        assertEquals(0, PermAssignTool.permsClear(guild, role, channel, inputPerms));

        Thread.sleep(3000);

        actualAllow = channel.getPermissionOverride(role).getAllowed();
        actualDeny = channel.getPermissionOverride(role).getDenied();

        assertThat(actualAllow, is(expectedAllow));
        assertThat(actualDeny, is(expectedDeny));


        //Error handling
        expectedAllow = new HashSet<Permission>(Arrays.asList(Permission.MANAGE_THREADS, Permission.MESSAGE_EMBED_LINKS));
        expectedDeny = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_ATTACH_VOICE_MESSAGE, Permission.MESSAGE_SEND_IN_THREADS));
        inputPerms = new HashSet<Permission>(Arrays.asList(Permission.MANAGE_THREADS, Permission.MESSAGE_ATTACH_VOICE_MESSAGE));

        //Null Guild
        assertEquals(-1, PermAssignTool.permsClear(null, null, null, null));

        Thread.sleep(3000);

        actualAllow = channel.getPermissionOverride(role).getAllowed();
        actualDeny = channel.getPermissionOverride(role).getDenied();

        assertThat(actualAllow, is(expectedAllow));
        assertThat(actualDeny, is(expectedDeny));

        //Null Role
        assertEquals(-2, PermAssignTool.permsClear(guild, null, null, null));

        Thread.sleep(3000);

        actualAllow = channel.getPermissionOverride(role).getAllowed();
        actualDeny = channel.getPermissionOverride(role).getDenied();

        assertThat(actualAllow, is(expectedAllow));
        assertThat(actualDeny, is(expectedDeny));

        //Null GuildChannel
        assertEquals(-3, PermAssignTool.permsClear(guild, role, null, null));

        Thread.sleep(3000);

        actualAllow = channel.getPermissionOverride(role).getAllowed();
        actualDeny = channel.getPermissionOverride(role).getDenied();

        assertThat(actualAllow, is(expectedAllow));
        assertThat(actualDeny, is(expectedDeny));

        //Null Permission list
        assertEquals(-4, PermAssignTool.permsClear(guild, role, channel, null));

        Thread.sleep(3000);

        actualAllow = channel.getPermissionOverride(role).getAllowed();
        actualDeny = channel.getPermissionOverride(role).getDenied();

        assertThat(actualAllow, is(expectedAllow));
        assertThat(actualDeny, is(expectedDeny));

        
        //Clean up
        channelManager.removePermissionOverride(role).complete();
        
        Thread.sleep(3000);

        //Test no perms to remove error
        assertEquals(-5, PermAssignTool.permsClear(guild, role, channel, inputPerms));
        
        Thread.sleep(3000);

        jda.shutdownNow();
    }
    
    @Test
    public void testPermsSetAll() throws InterruptedException {
        
        //Client setup
        final String TOKEN = Json.createReader(App.class.getResourceAsStream(CREDENTIALS_DIRECTORY_PATH)).readObject().getString("api_key");
        JDABuilder jdab = JDABuilder.createLight(TOKEN);
        jdab.enableIntents(GatewayIntent.GUILD_MEMBERS);
        jdab.enableCache(CacheFlag.MEMBER_OVERRIDES);
        JDA jda = jdab.build().awaitReady();
        
        //Object setup
        Guild guild = jda.getGuildById(203282662608207872L);
        
        Role role1 = guild.getRoleById(1205225542882951300L);
        Role role2 = guild.getRoleById(1217608439832776795L);
        Member member = guild.retrieveMemberById(1137409049269391490L).complete();

        TextChannel textChannel1 = guild.getTextChannelById(1230586192848752762L);
        TextChannel textChannel2 = guild.getTextChannelById(217766003449397248L);
        VoiceChannel voiceChannel = guild.getVoiceChannelById(1203035987802988585L);

        Set<IPermissionHolder> roles = new HashSet<IPermissionHolder>(Arrays.asList(role1, role2, member));
        Set<GuildChannel> channels = new HashSet<GuildChannel>(Arrays.asList(textChannel1, textChannel2, voiceChannel));
        Set<Permission> expectedAllow = null;
        Set<Permission> expectedDeny = null;
        Set<Permission> actualAllow = null;
        Set<Permission> actualDeny = null;
        Set<Permission> inputAllow = null;
        Set<Permission> inputDeny = null;

        //Clearing current perms to set things up
        for (GuildChannel c : channels)
            for (IPermissionHolder r : roles) {
                c.getPermissionContainer().getManager().removePermissionOverride(r).complete();
            }

        //Testing perm setting
        expectedAllow = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_HISTORY, Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT));
        expectedDeny = new HashSet<Permission>(Arrays.asList(Permission.MANAGE_THREADS, Permission.MESSAGE_EXT_EMOJI, Permission.VOICE_SPEAK));
        inputAllow = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_HISTORY, Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT));
        inputDeny = new HashSet<Permission>(Arrays.asList(Permission.MANAGE_THREADS, Permission.MESSAGE_EXT_EMOJI, Permission.VOICE_SPEAK));

        assertEquals(0, PermAssignTool.permsSetAll(guild, roles, channels, inputAllow, inputDeny));

        Thread.sleep(3000);

        for (GuildChannel c : channels) {
            for (IPermissionHolder r : roles) {
                PermissionOverride permOverride = c.getPermissionContainer().getPermissionOverride(r);

                assertNotNull(permOverride);

                actualAllow = permOverride.getAllowed();
                actualDeny = permOverride.getDenied();

                assertThat(actualAllow, is(expectedAllow));
                assertThat(actualDeny, is(expectedDeny));
            }
        }


        //Testing ONLY new perms being set
        expectedAllow = new HashSet<Permission>(Arrays.asList(Permission.CREATE_PRIVATE_THREADS, Permission.MESSAGE_SEND, Permission.VOICE_STREAM));
        expectedDeny = new HashSet<Permission>(Arrays.asList(Permission.CREATE_PUBLIC_THREADS, Permission.MESSAGE_ADD_REACTION, Permission.VOICE_USE_SOUNDBOARD));
        inputAllow = new HashSet<Permission>(Arrays.asList(Permission.CREATE_PRIVATE_THREADS, Permission.MESSAGE_SEND, Permission.VOICE_STREAM));
        inputDeny = new HashSet<Permission>(Arrays.asList(Permission.CREATE_PUBLIC_THREADS, Permission.MESSAGE_ADD_REACTION, Permission.VOICE_USE_SOUNDBOARD));

        assertEquals(0, PermAssignTool.permsSetAll(guild, roles, channels, inputAllow, inputDeny));

        Thread.sleep(3000);

        for (GuildChannel c : channels) {
            for (IPermissionHolder r : roles) {
                PermissionOverride permOverride = c.getPermissionContainer().getPermissionOverride(r);
                
                assertNotNull(permOverride);

                actualAllow = permOverride.getAllowed();
                actualDeny = permOverride.getDenied();

                assertThat(actualAllow, is(expectedAllow));
                assertThat(actualDeny, is(expectedDeny));
            }
        }


        //Testing only specified channels are changing
        Set<GuildChannel> channels1 = new HashSet<GuildChannel>(Arrays.asList(textChannel1, voiceChannel));
        Set<GuildChannel> channels2 = new HashSet<GuildChannel>(channels);
        channels2.removeAll(channels1);
        Set<Permission> expectedAllow2 = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_MENTION_EVERYONE));
        Set<Permission> expectedDeny2 = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_SEND_IN_THREADS, Permission.VOICE_SET_STATUS));
        Set<Permission> inputAllow2 = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_MENTION_EVERYONE));
        Set<Permission> inputDeny2 = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_SEND_IN_THREADS, Permission.VOICE_SET_STATUS));

        assertEquals(0, PermAssignTool.permsSetAll(guild, roles, channels1, inputAllow2, inputDeny2));
        
        Thread.sleep(3000);

        //Verify select channels were affected
        for(GuildChannel c : channels1) {
            for (IPermissionHolder r : roles) {
                actualAllow = c.getPermissionContainer().getPermissionOverride(r).getAllowed();
                actualDeny = c.getPermissionContainer().getPermissionOverride(r).getDenied();

                assertThat(actualAllow, is(expectedAllow2));
                assertThat(actualDeny, is(expectedDeny2));
            }
        }

        //Verify previous channel perms remained the same(only select channels were affected)
        for (GuildChannel c : channels2) {
            for (IPermissionHolder r : roles) {
                actualAllow = c.getPermissionContainer().getPermissionOverride(r).getAllowed();
                actualDeny = c.getPermissionContainer().getPermissionOverride(r).getDenied();

                assertThat(actualAllow, is(expectedAllow));
                assertThat(actualDeny, is(expectedDeny));
            }
        }        
        
        //Resyncing perms
        PermAssignTool.permsSetAll(guild, roles, channels, inputAllow, inputDeny);

        //Error handling
        //Null Guild
        assertEquals(-1, PermAssignTool.permsSetAll(null, null, null, null, null));

        Thread.sleep(3000);

        //Verify channels remained unchanged
        for (GuildChannel c : channels) {
            for (IPermissionHolder r : roles) {
                actualAllow = c.getPermissionContainer().getPermissionOverride(r).getAllowed();
                actualDeny = c.getPermissionContainer().getPermissionOverride(r).getDenied();

                assertThat(actualAllow, is(expectedAllow));
                assertThat(actualDeny, is(expectedDeny));
            }
        }

        //Null Role List
        assertEquals(-2, PermAssignTool.permsSetAll(guild, null, null, null, null));

        Thread.sleep(3000);

        //Verify channels remained unchanged
        for (GuildChannel c : channels) {
            for (IPermissionHolder r : roles) {
                actualAllow = c.getPermissionContainer().getPermissionOverride(r).getAllowed();
                actualDeny = c.getPermissionContainer().getPermissionOverride(r).getDenied();

                assertThat(actualAllow, is(expectedAllow));
                assertThat(actualDeny, is(expectedDeny));
            }
        }

        //Null Channel List
        assertEquals(-3, PermAssignTool.permsSetAll(guild, roles, null, null, null));

        Thread.sleep(3000);

        //Verify channels remained unchanged
        for (GuildChannel c : channels) {
            for (IPermissionHolder r : roles) {
                actualAllow = c.getPermissionContainer().getPermissionOverride(r).getAllowed();
                actualDeny = c.getPermissionContainer().getPermissionOverride(r).getDenied();

                assertThat(actualAllow, is(expectedAllow));
                assertThat(actualDeny, is(expectedDeny));
            }
        }


        //Null Allow AND Deny List
        assertEquals(-4, PermAssignTool.permsSetAll(guild, roles, channels, null, null));

        Thread.sleep(3000);

        //Verify channels remained unchanged
        for (GuildChannel c : channels) {
            for (IPermissionHolder r : roles) {
                actualAllow = c.getPermissionContainer().getPermissionOverride(r).getAllowed();
                actualDeny = c.getPermissionContainer().getPermissionOverride(r).getDenied();

                assertThat(actualAllow, is(expectedAllow));
                assertThat(actualDeny, is(expectedDeny));
            }
        }


        //Clean up and shut down
        for (GuildChannel c : channels)
            for (IPermissionHolder r : roles)
                c.getPermissionContainer().getManager().removePermissionOverride(r).complete();
        
        Thread.sleep(3000);
        jda.shutdownNow();
    }

    @Test
    public void testPermsAddAll() throws InterruptedException {

        //Client setup
        final String TOKEN = Json.createReader(App.class.getResourceAsStream(CREDENTIALS_DIRECTORY_PATH)).readObject().getString("api_key");
        JDABuilder jdab = JDABuilder.createLight(TOKEN);
        jdab.enableIntents(GatewayIntent.GUILD_MEMBERS);
        jdab.enableCache(CacheFlag.MEMBER_OVERRIDES);
        JDA jda = jdab.build().awaitReady();
        
        //Object setup
        Guild guild = jda.getGuildById(203282662608207872L);
        
        Role role1 = guild.getRoleById(1205225542882951300L);
        Role role2 = guild.getRoleById(1217608439832776795L);
        Member member = guild.retrieveMemberById(1137409049269391490L).complete();

        TextChannel textChannel1 = guild.getTextChannelById(1230586192848752762L);
        TextChannel textChannel2 = guild.getTextChannelById(217766003449397248L);
        VoiceChannel voiceChannel = guild.getVoiceChannelById(1203035987802988585L);

        Set<IPermissionHolder> roles = new HashSet<IPermissionHolder>(Arrays.asList(role1, role2, member));
        Set<GuildChannel> channels = new HashSet<GuildChannel>(Arrays.asList(textChannel1, textChannel2, voiceChannel));
        Set<Permission> expectedAllow = null;
        Set<Permission> expectedDeny = null;
        Set<Permission> actualAllow = null;
        Set<Permission> actualDeny = null;
        Set<Permission> inputAllow = null;
        Set<Permission> inputDeny = null;

        //Clearing current perms to set things up
        for (GuildChannel c : channels)
            for (IPermissionHolder r : roles) {
                c.getPermissionContainer().getManager().removePermissionOverride(r).complete();
            }

        //Testing perm adding
        expectedAllow = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_HISTORY, Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT));
        expectedDeny = new HashSet<Permission>(Arrays.asList(Permission.MANAGE_THREADS, Permission.MESSAGE_EXT_EMOJI, Permission.VOICE_SPEAK));
        inputAllow = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_HISTORY, Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT));
        inputDeny = new HashSet<Permission>(Arrays.asList(Permission.MANAGE_THREADS, Permission.MESSAGE_EXT_EMOJI, Permission.VOICE_SPEAK));

        assertEquals(0, PermAssignTool.permsAddAll(guild, roles, channels, inputAllow, inputDeny));

        Thread.sleep(3000);

        for (GuildChannel c : channels) {
            for (IPermissionHolder r : roles) {
                PermissionOverride permOverride = c.getPermissionContainer().getPermissionOverride(r);

                assertNotNull(permOverride);

                actualAllow = permOverride.getAllowed();
                actualDeny = permOverride.getDenied();

                assertThat(actualAllow, is(expectedAllow));
                assertThat(actualDeny, is(expectedDeny));
            }
        }


        //Testing new perms are being added on top
        expectedAllow = new HashSet<Permission>(Arrays.asList(Permission.CREATE_PRIVATE_THREADS, Permission.MESSAGE_SEND, Permission.VOICE_STREAM, Permission.MESSAGE_HISTORY, Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT));
        expectedDeny = new HashSet<Permission>(Arrays.asList(Permission.CREATE_PUBLIC_THREADS, Permission.MESSAGE_ADD_REACTION, Permission.VOICE_USE_SOUNDBOARD, Permission.MANAGE_THREADS, Permission.MESSAGE_EXT_EMOJI, Permission.VOICE_SPEAK));
        inputAllow = new HashSet<Permission>(Arrays.asList(Permission.CREATE_PRIVATE_THREADS, Permission.MESSAGE_SEND, Permission.VOICE_STREAM));
        inputDeny = new HashSet<Permission>(Arrays.asList(Permission.CREATE_PUBLIC_THREADS, Permission.MESSAGE_ADD_REACTION, Permission.VOICE_USE_SOUNDBOARD));

        assertEquals(0, PermAssignTool.permsAddAll(guild, roles, channels, inputAllow, inputDeny));

        Thread.sleep(3000);

        for (GuildChannel c : channels) {
            for (IPermissionHolder r : roles) {
                PermissionOverride permOverride = c.getPermissionContainer().getPermissionOverride(r);
                
                assertNotNull(permOverride);

                actualAllow = permOverride.getAllowed();
                actualDeny = permOverride.getDenied();

                assertThat(actualAllow, is(expectedAllow));
                assertThat(actualDeny, is(expectedDeny));
            }
        }


        //Testing only specified channels are changing
        Set<GuildChannel> channels1 = new HashSet<GuildChannel>(Arrays.asList(textChannel1, voiceChannel));
        Set<GuildChannel> channels2 = new HashSet<GuildChannel>(channels);
        channels2.removeAll(channels1);
        Set<Permission> expectedAllow2 = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_MENTION_EVERYONE, Permission.CREATE_PRIVATE_THREADS, Permission.MESSAGE_SEND, Permission.VOICE_STREAM, Permission.MESSAGE_HISTORY, Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT));
        Set<Permission> expectedDeny2 = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_SEND_IN_THREADS, Permission.VOICE_SET_STATUS, Permission.CREATE_PUBLIC_THREADS, Permission.MESSAGE_ADD_REACTION, Permission.VOICE_USE_SOUNDBOARD, Permission.MANAGE_THREADS, Permission.MESSAGE_EXT_EMOJI, Permission.VOICE_SPEAK));
        Set<Permission> inputAllow2 = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_MENTION_EVERYONE));
        Set<Permission> inputDeny2 = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_SEND_IN_THREADS, Permission.VOICE_SET_STATUS));

        assertEquals(0, PermAssignTool.permsAddAll(guild, roles, channels1, inputAllow2, inputDeny2));
        
        Thread.sleep(3000);

        //Verify select channels were affected
        for(GuildChannel c : channels1) {
            for (IPermissionHolder r : roles) {
                actualAllow = c.getPermissionContainer().getPermissionOverride(r).getAllowed();
                actualDeny = c.getPermissionContainer().getPermissionOverride(r).getDenied();

                assertThat(actualAllow, is(expectedAllow2));
                assertThat(actualDeny, is(expectedDeny2));
            }
        }

        //Verify previous channel perms remained the same(only select channels were affected)
        for (GuildChannel c : channels2) {
            for (IPermissionHolder r : roles) {
                actualAllow = c.getPermissionContainer().getPermissionOverride(r).getAllowed();
                actualDeny = c.getPermissionContainer().getPermissionOverride(r).getDenied();

                assertThat(actualAllow, is(expectedAllow));
                assertThat(actualDeny, is(expectedDeny));
            }
        }
        
        //Resyncing perms
        PermAssignTool.permsAddAll(guild, roles, channels2, inputAllow2, inputDeny2);
        expectedAllow = expectedAllow2;
        expectedDeny = expectedDeny2;

        //Error handling
        //Null Guild
        assertEquals(-1, PermAssignTool.permsAddAll(null, null, null, null, null));

        Thread.sleep(3000);

        //Verify channels remained unchanged
        for (GuildChannel c : channels) {
            for (IPermissionHolder r : roles) {
                actualAllow = c.getPermissionContainer().getPermissionOverride(r).getAllowed();
                actualDeny = c.getPermissionContainer().getPermissionOverride(r).getDenied();

                assertThat(actualAllow, is(expectedAllow));
                assertThat(actualDeny, is(expectedDeny));
            }
        }

        //Null Role List
        assertEquals(-2, PermAssignTool.permsAddAll(guild, null, null, null, null));

        Thread.sleep(3000);

        //Verify channels remained unchanged
        for (GuildChannel c : channels) {
            for (IPermissionHolder r : roles) {
                actualAllow = c.getPermissionContainer().getPermissionOverride(r).getAllowed();
                actualDeny = c.getPermissionContainer().getPermissionOverride(r).getDenied();

                assertThat(actualAllow, is(expectedAllow));
                assertThat(actualDeny, is(expectedDeny));
            }
        }

        //Null Channel List
        assertEquals(-3, PermAssignTool.permsAddAll(guild, roles, null, null, null));

        Thread.sleep(3000);

        //Verify channels remained unchanged
        for (GuildChannel c : channels) {
            for (IPermissionHolder r : roles) {
                actualAllow = c.getPermissionContainer().getPermissionOverride(r).getAllowed();
                actualDeny = c.getPermissionContainer().getPermissionOverride(r).getDenied();

                assertThat(actualAllow, is(expectedAllow));
                assertThat(actualDeny, is(expectedDeny));
            }
        }


        //Null Allow AND Deny List
        assertEquals(-4, PermAssignTool.permsAddAll(guild, roles, channels, null, null));

        Thread.sleep(3000);

        //Verify channels remained unchanged
        for (GuildChannel c : channels) {
            for (IPermissionHolder r : roles) {
                actualAllow = c.getPermissionContainer().getPermissionOverride(r).getAllowed();
                actualDeny = c.getPermissionContainer().getPermissionOverride(r).getDenied();

                assertThat(actualAllow, is(expectedAllow));
                assertThat(actualDeny, is(expectedDeny));
            }
        }


        //Clean up and shut down
        for (GuildChannel c : channels)
            for (IPermissionHolder r : roles)
                c.getPermissionContainer().getManager().removePermissionOverride(r).complete();
        
        Thread.sleep(3000);
        jda.shutdownNow();
    }

    @Test
    public void testPermsClearAll() throws InterruptedException {
    
        //Client setup
        final String TOKEN = Json.createReader(App.class.getResourceAsStream(CREDENTIALS_DIRECTORY_PATH)).readObject().getString("api_key");
        JDABuilder jdab = JDABuilder.createLight(TOKEN);
        jdab.enableIntents(GatewayIntent.GUILD_MEMBERS);
        jdab.enableCache(CacheFlag.MEMBER_OVERRIDES);
        JDA jda = jdab.build().awaitReady();
        
        //Object setup
        Guild guild = jda.getGuildById(203282662608207872L);
        
        Role role1 = guild.getRoleById(1205225542882951300L);
        Role role2 = guild.getRoleById(1217608439832776795L);
        Member member = guild.retrieveMemberById(1137409049269391490L).complete();

        TextChannel textChannel1 = guild.getTextChannelById(1230586192848752762L);
        TextChannel textChannel2 = guild.getTextChannelById(217766003449397248L);
        VoiceChannel voiceChannel = guild.getVoiceChannelById(1203035987802988585L);

        Set<IPermissionHolder> roles = new HashSet<IPermissionHolder>(Arrays.asList(role1, role2, member));
        Set<GuildChannel> channels = new HashSet<GuildChannel>(Arrays.asList(textChannel1, textChannel2, voiceChannel));
        Set<Permission> expectedAllow = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_MENTION_EVERYONE, Permission.CREATE_PRIVATE_THREADS, Permission.MESSAGE_SEND, Permission.VOICE_STREAM, Permission.MESSAGE_HISTORY, Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT));;
        Set<Permission> expectedDeny = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_SEND_IN_THREADS, Permission.VOICE_SET_STATUS, Permission.CREATE_PUBLIC_THREADS, Permission.MESSAGE_ADD_REACTION, Permission.VOICE_USE_SOUNDBOARD, Permission.MANAGE_THREADS, Permission.MESSAGE_EXT_EMOJI, Permission.VOICE_SPEAK));
        Set<Permission> actualAllow = null;
        Set<Permission> actualDeny = null;
        Set<Permission> inputPerms = null;

        //Setting perms to set things up
        for (GuildChannel c : channels)
            for (IPermissionHolder r : roles) {
                c.getPermissionContainer().getManager().putPermissionOverride(r, expectedAllow, expectedDeny).complete();
            }

        Thread.sleep(7000);

        //Testing perm clearing
        expectedAllow = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_MENTION_EVERYONE, Permission.CREATE_PRIVATE_THREADS, Permission.MESSAGE_SEND, Permission.VOICE_STREAM));
        expectedDeny = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_SEND_IN_THREADS, Permission.VOICE_SET_STATUS, Permission.CREATE_PUBLIC_THREADS, Permission.MESSAGE_ADD_REACTION, Permission.VOICE_USE_SOUNDBOARD));
        inputPerms = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_HISTORY, Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT, Permission.MANAGE_THREADS, Permission.MESSAGE_EXT_EMOJI, Permission.VOICE_SPEAK));

        assertEquals(0, PermAssignTool.permsClearAll(guild, roles, channels, inputPerms));

        Thread.sleep(10000);

        for (GuildChannel c : channels) {
            for (IPermissionHolder r : roles) {
                PermissionOverride permOverride = c.getPermissionContainer().getPermissionOverride(r);

                assertNotNull(permOverride);

                actualAllow = permOverride.getAllowed();
                actualDeny = permOverride.getDenied();

                assertThat(actualAllow, is(expectedAllow));
                assertThat(actualDeny, is(expectedDeny));
            }
        }


        //Testing new perms are being cleared on top
        expectedAllow = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_MENTION_EVERYONE));
        expectedDeny = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_SEND_IN_THREADS, Permission.VOICE_SET_STATUS));
        inputPerms = new HashSet<Permission>(Arrays.asList(Permission.CREATE_PRIVATE_THREADS, Permission.MESSAGE_SEND, Permission.VOICE_STREAM, Permission.CREATE_PUBLIC_THREADS, Permission.MESSAGE_ADD_REACTION, Permission.VOICE_USE_SOUNDBOARD));

        assertEquals(0, PermAssignTool.permsClearAll(guild, roles, channels, inputPerms));

        Thread.sleep(3000);

        for (GuildChannel c : channels) {
            for (IPermissionHolder r : roles) {
                PermissionOverride permOverride = c.getPermissionContainer().getPermissionOverride(r);
                
                assertNotNull(permOverride);

                actualAllow = permOverride.getAllowed();
                actualDeny = permOverride.getDenied();

                assertThat(actualAllow, is(expectedAllow));
                assertThat(actualDeny, is(expectedDeny));
            }
        }


        //Testing only specified channels are changing
        Set<GuildChannel> channels1 = new HashSet<GuildChannel>(Arrays.asList(textChannel1, voiceChannel));
        Set<GuildChannel> channels2 = new HashSet<GuildChannel>(channels);
        channels2.removeAll(channels1);
        Set<Permission> inputPerms2 = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_MENTION_EVERYONE, Permission.MESSAGE_SEND_IN_THREADS, Permission.VOICE_SET_STATUS));

        assertEquals(0, PermAssignTool.permsClearAll(guild, roles, channels1, inputPerms2));
        
        Thread.sleep(5000);

        //Verify select channels were affected
        for(GuildChannel c : channels1) {
            for (IPermissionHolder r : roles) {
                assertNull(c.getPermissionContainer().getPermissionOverride(r));
            }
        }

        //Verify previous channel perms remained the same(only select channels were affected)
        for (GuildChannel c : channels2) {
            for (IPermissionHolder r : roles) {
                actualAllow = c.getPermissionContainer().getPermissionOverride(r).getAllowed();
                actualDeny = c.getPermissionContainer().getPermissionOverride(r).getDenied();

                assertThat(actualAllow, is(expectedAllow));
                assertThat(actualDeny, is(expectedDeny));
            }
        }
        
        //Resyncing perms
        PermAssignTool.permsClearAll(guild, roles, channels2, inputPerms2);

        Thread.sleep(3000);

        //Error handling
        //Null Guild
        assertEquals(-1, PermAssignTool.permsClearAll(null, null, null, null));

        Thread.sleep(3000);

        //Verify channels remained unchanged
        for (GuildChannel c : channels) {
            for (IPermissionHolder r : roles) {
                assertNull(c.getPermissionContainer().getPermissionOverride(r));
            }
        }

        //Null Role List
        assertEquals(-2, PermAssignTool.permsClearAll(guild, null, null, null));

        Thread.sleep(3000);

        //Verify channels remained unchanged
        for (GuildChannel c : channels) {
            for (IPermissionHolder r : roles) {
                assertNull(c.getPermissionContainer().getPermissionOverride(r));
            }
        }

        //Null Channel List
        assertEquals(-3, PermAssignTool.permsClearAll(guild, roles, null, null));

        Thread.sleep(3000);

        //Verify channels remained unchanged
        for (GuildChannel c : channels) {
            for (IPermissionHolder r : roles) {
                assertNull(c.getPermissionContainer().getPermissionOverride(r));
            }
        }


        //Null Perms List
        assertEquals(-4, PermAssignTool.permsClearAll(guild, roles, channels, null));

        Thread.sleep(3000);
        
        //Verify channels remained unchanged
        for (GuildChannel c : channels) {
            for (IPermissionHolder r : roles) {
                assertNull(c.getPermissionContainer().getPermissionOverride(r));
            }
        }


        //Clean up and shut down
        for (GuildChannel c : channels)
            for (IPermissionHolder r : roles)
                c.getPermissionContainer().getManager().removePermissionOverride(r).complete();
        
        Thread.sleep(3000);
        jda.shutdownNow();
    }

    @Test
    public void testPermsAllClearAll() throws InterruptedException {
    
        //Client setup
        final String TOKEN = Json.createReader(App.class.getResourceAsStream(CREDENTIALS_DIRECTORY_PATH)).readObject().getString("api_key");
        JDABuilder jdab = JDABuilder.createLight(TOKEN);
        jdab.enableIntents(GatewayIntent.GUILD_MEMBERS);
        jdab.enableCache(CacheFlag.MEMBER_OVERRIDES);
        JDA jda = jdab.build().awaitReady();
        
        //Object setup
        Guild guild = jda.getGuildById(203282662608207872L);
        
        Role role1 = guild.getRoleById(1205225542882951300L);
        Role role2 = guild.getRoleById(1217608439832776795L);
        Member member = guild.retrieveMemberById(1137409049269391490L).complete();

        TextChannel textChannel1 = guild.getTextChannelById(1230586192848752762L);
        TextChannel textChannel2 = guild.getTextChannelById(217766003449397248L);
        VoiceChannel voiceChannel = guild.getVoiceChannelById(1203035987802988585L);

        Set<IPermissionHolder> roles = new HashSet<IPermissionHolder>(Arrays.asList(role1, role2, member));
        Set<GuildChannel> channels = new HashSet<GuildChannel>(Arrays.asList(textChannel1, textChannel2, voiceChannel));
        Set<Permission> expectedAllow = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_MENTION_EVERYONE, Permission.CREATE_PRIVATE_THREADS, Permission.MESSAGE_SEND, Permission.VOICE_STREAM, Permission.MESSAGE_HISTORY, Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT));;
        Set<Permission> expectedDeny = new HashSet<Permission>(Arrays.asList(Permission.MESSAGE_SEND_IN_THREADS, Permission.VOICE_SET_STATUS, Permission.CREATE_PUBLIC_THREADS, Permission.MESSAGE_ADD_REACTION, Permission.VOICE_USE_SOUNDBOARD, Permission.MANAGE_THREADS, Permission.MESSAGE_EXT_EMOJI, Permission.VOICE_SPEAK));
        Set<Permission> actualAllow = null;
        Set<Permission> actualDeny = null;

        //Setting perms to set things up
        for (GuildChannel c : channels)
            for (IPermissionHolder r : roles) {
                c.getPermissionContainer().getManager().putPermissionOverride(r, expectedAllow, expectedDeny).complete();
            }

        Thread.sleep(10000);

        assertEquals(0, PermAssignTool.permsAllClearAll(guild, roles, channels));

        Thread.sleep(15000);

        for (GuildChannel c : channels) {
            for (IPermissionHolder r : roles) {
                assertNull(c.getPermissionContainer().getPermissionOverride(r));
            }
        }

        //Resetting perms
        for (GuildChannel c : channels)
            for (IPermissionHolder r : roles) {
                c.getPermissionContainer().getManager().putPermissionOverride(r, expectedAllow, expectedDeny).complete();
            }

        Thread.sleep(7000);

        //Testing only specified channels are changing
        Set<GuildChannel> channels1 = new HashSet<GuildChannel>(Arrays.asList(textChannel1, voiceChannel));
        Set<GuildChannel> channels2 = new HashSet<GuildChannel>(channels);
        channels2.removeAll(channels1);

        assertEquals(0, PermAssignTool.permsAllClearAll(guild, roles, channels1));
        
        Thread.sleep(5000);

        //Verify select channels were affected
        for(GuildChannel c : channels1) {
            for (IPermissionHolder r : roles) {
                assertNull(c.getPermissionContainer().getPermissionOverride(r));
            }
        }

        //Verify previous channel perms remained the same(only select channels were affected)
        for (GuildChannel c : channels2) {
            for (IPermissionHolder r : roles) {
                actualAllow = c.getPermissionContainer().getPermissionOverride(r).getAllowed();
                actualDeny = c.getPermissionContainer().getPermissionOverride(r).getDenied();

                assertThat(actualAllow, is(expectedAllow));
                assertThat(actualDeny, is(expectedDeny));
            }
        }
        
        //Resyncing perms
        PermAssignTool.permsAllClearAll(guild, roles, channels2);

        Thread.sleep(5000);

        //Error handling
        //Null Guild
        assertEquals(-1, PermAssignTool.permsAllClearAll(null, null, null));

        Thread.sleep(5000);

        //Verify channels remained unchanged
        for (GuildChannel c : channels) {
            for (IPermissionHolder r : roles) {
                assertNull(c.getPermissionContainer().getPermissionOverride(r));
            }
        }

        //Null Role List
        assertEquals(-2, PermAssignTool.permsAllClearAll(guild, null, null));

        Thread.sleep(3000);

        //Verify channels remained unchanged
        for (GuildChannel c : channels) {
            for (IPermissionHolder r : roles) {
                assertNull(c.getPermissionContainer().getPermissionOverride(r));
            }
        }

        //Null Channel List
        assertEquals(-3, PermAssignTool.permsAllClearAll(guild, roles, null));

        Thread.sleep(3000);

        //Verify channels remained unchanged
        for (GuildChannel c : channels) {
            for (IPermissionHolder r : roles) {
                assertNull(c.getPermissionContainer().getPermissionOverride(r));
            }
        }

        //Clean up and shut down
        for (GuildChannel c : channels)
            for (IPermissionHolder r : roles)
                c.getPermissionContainer().getManager().removePermissionOverride(r).complete();
        
        Thread.sleep(3000);
        jda.shutdownNow();
    }
}
