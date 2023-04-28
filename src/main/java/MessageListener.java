import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Queue;


public class MessageListener extends ListenerAdapter {
    private AudioPlayerManager audioPlayerManager;
    private AudioSourceManagers audioSourceManagers;
    private AudioPlayer audioPlayer;
    private AudioManager audioManager;
    private AudioSendHandler audioSender;

    public static void main(String[] args) {
        String token = "OTE2MzYzOTU3MDU5NjAwMzk0.GuW9Z-.0ufT9Ec5T0ThapDp1kxjYuUajcqShDMZrYYLf8";

        JDA builder = JDABuilder.createDefault(token).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();
        builder.addEventListener(new MessageListener());
    }

    public void sendPrivateMessage(User user, String content) {
        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessage(content))
                .queue();
    }

    public void onMessageReceived(MessageReceivedEvent event){
        if(event.isFromType(ChannelType.TEXT)){
            final Message message = event.getMessage();
            if(event == null){
                return;
            }
            if(message == null){
                return;
            }
            if(event.getMessage().getContentRaw().toLowerCase().contains("-?")){
                 
                final Member self = event.getGuild().getSelfMember();
                final GuildVoiceState selfVoiceState = self.getVoiceState();

                if(message.getMember().equals(event.getGuild().getSelfMember())){
                    return;
                }
                if(!selfVoiceState.inAudioChannel()){
                    message.reply("no can't do bitch").queue();
                    return;
                }

                final Member member = message.getMember();
                final GuildVoiceState memberVoiceState = member.getVoiceState();

                if(!memberVoiceState.inAudioChannel()){
                    message.reply("go into  a voice channel fucking dumbass").queue();
                    return;
                }
                if(!member.getVoiceState().inAudioChannel()){
                    return;
                }
                if(!memberVoiceState.getChannel().equals(selfVoiceState.getChannel())){
                    message.reply("I am at a different vibe rn").queue();
                    return;
                }
                final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(self.getGuild());
                final AudioPlayer audioPlayer = musicManager.audioPlayer;
                final AudioTrack track = audioPlayer.getPlayingTrack();
                if(track == null){
                    message.reply("Nothing is playing").queue();
                }

                final AudioTrackInfo info = track.getInfo();
                message.reply("Now playing: " + info.title + " from: " + info.author).queue();
                return;
            }

            if(event.getMessage().getContentRaw().toLowerCase().contains("-help")) {
                 
                final Member self = event.getGuild().getSelfMember();
                final GuildVoiceState selfVoiceState = self.getVoiceState();

                if(message.getAuthor().equals(self)){
                    return;
                }
                event.getMessage().reply("`-join : joins your current vc`\n" +
                        "`-play <song> / <link> : plays a song from youtube`\n" +
                        "`-stop : clears queue`\n" +
                        "`-dc : disconnects the bot`\n" +
                        "`-? : returns current song name`\n" +
                        "`-q/-queue : shows the current queue`\n" +
                        "`-tp : toggles pause/play`\n" +
                        "`-skip : skips the current song`").queue();
            }
            if(event.getMessage().getContentRaw().toLowerCase().contains("-q") || event.getMessage().getContentRaw().toLowerCase().contains("-queue")){
                Member self = event.getGuild().getSelfMember();
                Member member = event.getMember();

                final GuildVoiceState selfVoiceState = self.getVoiceState();

                if(message.getMember().equals(event.getGuild().getSelfMember())){
                    return;
                }
                if(!self.getVoiceState().inAudioChannel()){
                    message.reply("Nah bro").queue();
                    return;
                }
                Queue<AudioTrack> q = PlayerManager.getInstance().getQueue(message.getChannel().asTextChannel());
                if(q.isEmpty()){
                    message.reply("Queue is empty").queue();
                }
                else{
                    message.reply(qToString(q)).queue();
                }
            }
            if(event.getMessage().getContentRaw().toLowerCase().contains("-dc")){
                Member self = event.getGuild().getSelfMember();
                Member member = event.getMember();

                final GuildVoiceState selfVoiceState = self.getVoiceState();

                if(message.getMember().equals(event.getGuild().getSelfMember())){
                    return;
                }
                if(!self.getVoiceState().inAudioChannel()){
                    message.reply("Nah bro").queue();
                    return;
                }
                if(!member.getVoiceState().inAudioChannel()){
                    message.reply("Nah bro").queue();
                    return;
                }
                audioManager.closeAudioConnection();
                return;
            }
            if(event.getMessage().getContentRaw().toLowerCase().contains("-join")){
                Member self = event.getGuild().getSelfMember();
                Member member = event.getMember();
                if(message.getMember().equals(event.getGuild().getSelfMember())){
                    return;
                }
                if(self.getVoiceState().inAudioChannel()){
                    message.reply("Nah bro").queue();
                    return;
                }
                if(!member.getVoiceState().inAudioChannel()){
                    message.reply("Nah bro").queue();
                    return;
                }
                audioManager = event.getGuild().getAudioManager();
                if(!member.getVoiceState().inAudioChannel()){
                    return;
                }
                AudioChannel memberChannel = member.getVoiceState().getChannel();
                audioManager.openAudioConnection(memberChannel);
                message.reply("Connecting to [" + memberChannel.getName() + "]").queue();

                return;
            }
            if(event.getMessage().getContentRaw().toLowerCase().contains("-stop")){
                final Member self = event.getGuild().getSelfMember();
                final GuildVoiceState selfVoiceState = self.getVoiceState();

                if(message.getMember().equals(event.getGuild().getSelfMember())){
                    return;
                }
                if(!selfVoiceState.inAudioChannel()){
                    message.reply("no can't do bitch").queue();
                    return;
                }

                final Member member = message.getMember();
                final GuildVoiceState memberVoiceState = member.getVoiceState();

                if(!memberVoiceState.inAudioChannel()){
                    message.reply("go into  a voice channel fucking dumbass").queue();
                    return;
                }
                if(!member.getVoiceState().inAudioChannel()){
                    return;
                }
                if(!memberVoiceState.getChannel().equals(selfVoiceState.getChannel())){
                    message.reply("I am at a different vibe rn").queue();
                    return;
                }
                final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(self.getGuild());

                musicManager.scheduler.player.stopTrack();
                musicManager.scheduler.queue.clear();

                message.reply("queue is empty now <3").queue();

                return;
            }
            if(event.getMessage().getContentRaw().toLowerCase().contains("-skip")){
                 
                final Member self = event.getGuild().getSelfMember();
                GuildVoiceState selfVoiceState = self.getVoiceState();

                if(message.getMember().equals(event.getGuild().getSelfMember())){
                    return;
                }

                if(!selfVoiceState.inAudioChannel()){
                    message.reply("no can't do bitch").queue();
                    return;
                }

                final Member member = message.getMember();
                final GuildVoiceState memberVoiceState = member.getVoiceState();

                if(!memberVoiceState.inAudioChannel()){
                    message.reply("go into  a voice channel fucking dumbass").queue();
                    return;
                }
                if(!member.getVoiceState().inAudioChannel()){
                    return;
                }
                if(!memberVoiceState.getChannel().equals(selfVoiceState.getChannel())){
                    message.reply("I am at a different vibe rn").queue();
                    return;
                }
                final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(self.getGuild());
                final AudioPlayer audioPlayer = musicManager.audioPlayer;
                if(audioPlayer.getPlayingTrack() == null){
                    message.reply("Nothing to skip dumbass").queue();
                }

                musicManager.scheduler.nextTrack();
                final AudioTrack track = audioPlayer.getPlayingTrack();
                if(track == null){
                    message.reply("Nothing is playing").queue();
                }

                final AudioTrackInfo info = track.getInfo();
                message.reply("Now playing: " + info.title + " from: " + info.author).queue();
                return;

            }
            if(event.getMessage().getContentRaw().toLowerCase().contains("-tp")){
                Member self = event.getGuild().getSelfMember();
                GuildVoiceState selfVoiceState = self.getVoiceState();
                if(message.getMember().equals(event.getGuild().getSelfMember())){
                    return;
                }
                final Member member = message.getMember();
                final GuildVoiceState memberVoiceState = member.getVoiceState();

                if(!memberVoiceState.inAudioChannel()){
                    return;
                }
                if(!member.getVoiceState().inAudioChannel()){
                    return;
                }
                boolean state = PlayerManager.getInstance().pauseBot(message.getChannel().asTextChannel());
                if(state){
                    message.reply("Music is now paused").queue();
                }
                else{
                    message.reply("Music is now resumed").queue();
                }
                return;
            }
            if(event.getMessage().getContentRaw().toLowerCase().contains("-play") || event.getMessage().getContentRaw().toLowerCase().contains("-p")){
                 
                Member self = event.getGuild().getSelfMember();
                GuildVoiceState selfVoiceState = self.getVoiceState();
                if(message.getMember().equals(event.getGuild().getSelfMember())){
                    return;
                }
                final Member member = message.getMember();
                final GuildVoiceState memberVoiceState = member.getVoiceState();

                if(!memberVoiceState.inAudioChannel()){
                    return;
                }
                if(!member.getVoiceState().inAudioChannel()){
                    return;
                }
                if(!(memberVoiceState.getChannel().equals(selfVoiceState.getChannel()))){
                    if(!selfVoiceState.inAudioChannel()){
                        audioManager = event.getGuild().getAudioManager();
                        AudioChannel memberChannel = member.getVoiceState().getChannel();
                        audioManager.openAudioConnection(memberChannel);
                        message.reply("Connecting to [" + memberChannel.getName() + "]").queue();
                    }
                    else {
                        message.reply("I am at a different vibe rn").queue();
                        return;
                    }
                }
                String[] content = event.getMessage().getContentRaw().split(" ");
                if(content.length == 0){
                    message.reply("I need more info than that dumbass").queue();
                    return;
                }
                boolean wasPlaylist = true;
                String link = "";
                for (int i = 1; i < content.length; i++){
                    if(i == content.length - 1){
                        link += content[i] + "";
                        continue;
                    }
                    link += content[i] + " ";
                }
                if(!isUrl(link)){
                    link = "ytsearch:" + link;
                    wasPlaylist = false;
                }
                PlayerManager.getInstance()
                        .loadAndPlay(message.getChannel().asTextChannel(), link, wasPlaylist);
            }
        }
    }
    public static String qToString(Queue<AudioTrack> q){
       String ret = "";
       int i = 1;
       for(AudioTrack track : q){
            ret += "`" + i + " " + track.getInfo().title + " by " + track.getInfo().author + "`\n";
            i++;
       }
       return ret;
    }
    public static boolean isUrl(String url){
        try{
            new URL(url);
            return true;
        }catch (MalformedURLException e){
            return false;
        }
    }
}
