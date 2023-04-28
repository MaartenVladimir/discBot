import com.fasterxml.jackson.databind.ser.std.StdKeySerializers;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.PlayerResumeEvent;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class PlayerManager {
    private static PlayerManager INSTANCE;
    private final Map<Long, GuildMusicManager> musicManager;
    private final AudioPlayerManager audioPlayerManager;

    public PlayerManager(){
        this.musicManager = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
    }
    public GuildMusicManager getMusicManager(Guild guild){
        return this.musicManager.computeIfAbsent(guild.getIdLong(), (guildId) ->{
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);

            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
            return guildMusicManager;
        });
    }

    public Queue<AudioTrack> getQueue(TextChannel channel){
        final GuildMusicManager musicManager = this.getMusicManager(channel.getGuild());
        return musicManager.scheduler.queue;
    }


    public boolean pauseBot(TextChannel channel){
        final GuildMusicManager musicManager = this.getMusicManager(channel.getGuild());
        return musicManager.scheduler.pause();
    }

    public void loadAndPlay(TextChannel channel, String trackUrl, boolean wasPlaylist){
        final GuildMusicManager musicManager = this.getMusicManager(channel.getGuild());

        this.audioPlayerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.scheduler.queue(track);
                channel.sendMessage("Song added to queue cancer: `")
                        .addContent(track.getInfo().title)
                        .addContent("` by ")
                        .addContent(track.getInfo().author)
                        .queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                final List<AudioTrack> tracks = playlist.getTracks();
                if (!wasPlaylist) {
                    AudioTrack track = tracks.get(0);
                    musicManager.scheduler.queue(track);
                    channel.sendMessage("Song added to queue cancer: `")
                            .addContent(track.getInfo().title)
                            .addContent("` by ")
                            .addContent(track.getInfo().author)
                            .queue();
                }
                else{
                    channel.sendMessage("Song added to queue cancer: `")
                            .addContent(String.valueOf(tracks.size()))
                            .addContent("` tracks from playlist ")
                            .addContent(playlist.getName())
                            .queue();
                    for (final AudioTrack track : tracks){
                        musicManager.scheduler.queue(track);
                    }
                }
            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException exception) {

            }
        });
    }

    public static PlayerManager getInstance(){
        if(INSTANCE == null){
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }
}
