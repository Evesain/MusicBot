package com.jagrosh.jmusicbot.commands.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import com.jagrosh.jdautilities.examples.doc.Author;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

@CommandInfo(name = { "About" }, description = "Gets information about the bot.")
@Author("John Grosh (jagrosh)")
public class AboutCommand extends Command {

    private boolean IS_AUTHOR;
    private String REPLACEMENT_ICON;
    private final Color color;
    private final String description;
    private final Permission[] perms;
    private String oauthLink;
    private final String[] features;

    public AboutCommand(final Color color, final String description, final String[] features, final Permission... perms) {
        this.IS_AUTHOR = true;
        this.REPLACEMENT_ICON = "+";
        this.color = color;
        this.description = description;
        this.features = features;
        this.name = "about";
        this.help = "shows info about the bot";
        this.guildOnly = false;
        this.perms = perms;
        this.botPermissions = new Permission[] { Permission.MESSAGE_EMBED_LINKS };
    }

    public void setIsAuthor(final boolean value) {
        this.IS_AUTHOR = value;
    }

    public void setReplacementCharacter(final String value) {
        this.REPLACEMENT_ICON = value;
    }

    @Override
    protected void execute(final CommandEvent event) {
        if (this.oauthLink == null) {
            try {
                final ApplicationInfo info = event.getJDA().retrieveApplicationInfo().complete();
                this.oauthLink = (info.isBotPublic() ? info.getInviteUrl(0L, this.perms) : "");
            }
            catch (Exception e) {
                final Logger log = LoggerFactory.getLogger("OAuth2");
                log.error("Could not generate invite link ", e);
                this.oauthLink = "";
            }
        }
        final EmbedBuilder builder = new EmbedBuilder();
        builder.setColor((event.getGuild() == null) ? this.color : event.getGuild().getSelfMember().getColor());
        builder.setAuthor("All about " + event.getSelfUser().getName() + "!", null, event.getSelfUser().getAvatarUrl());
        final boolean join = event.getClient().getServerInvite() != null && !event.getClient().getServerInvite().isEmpty();
        final boolean inv = !this.oauthLink.isEmpty();
        final String invline = "\n" + (join ? ("Join my server [`here`](" + event.getClient().getServerInvite() + ")") : (inv ? "Please " : "")) + (inv ? ((join ? ", or " : "") + "[`invite`](" + this.oauthLink + ") me to your server") : "") + "!";
        final String author = (event.getJDA().getUserById(event.getClient().getOwnerId()) == null) ? ("<@" + event.getClient().getOwnerId() + ">") : event.getJDA().getUserById(event.getClient().getOwnerId()).getName();
        final StringBuilder descr = new StringBuilder().append(this.description).append("\nType `").append(event.getClient().getTextualPrefix()).append(event.getClient().getHelpWord()).append("` to see my commands!").append((join || inv) ? invline : "").append("\n\nSome of my features include: ```css");
        for (final String feature : this.features) {
            descr.append("\n").append(event.getClient().getSuccess().startsWith("<") ? this.REPLACEMENT_ICON : event.getClient().getSuccess()).append(" ").append(feature);
        }
        descr.append(" ```");
        builder.setDescription(descr);
        if (event.getJDA().getShardInfo() == null) {
            builder.addField("Stats", event.getJDA().getGuilds().size() + " servers\n1 shard", true);
            builder.addField("Users", event.getJDA().getUsers().size() + " unique\n" + event.getJDA().getGuilds().stream().mapToInt(g -> g.getMembers().size()).sum() + " total", true);
            builder.addField("Channels", event.getJDA().getTextChannels().size() + " Text\n" + event.getJDA().getVoiceChannels().size() + " Voice", true);
        }
        else {
            builder.addField("Stats", event.getClient().getTotalGuilds() + " Servers\nShard " + (event.getJDA().getShardInfo().getShardId() + 1) + "/" + event.getJDA().getShardInfo().getShardTotal(), true);
            builder.addField("This shard", event.getJDA().getUsers().size() + " Users\n" + event.getJDA().getGuilds().size() + " Servers", true);
            builder.addField("", event.getJDA().getTextChannels().size() + " Text Channels\n" + event.getJDA().getVoiceChannels().size() + " Voice Channels", true);
        }
        builder.setFooter("Last restart", null);
        builder.setTimestamp(event.getClient().getStartTime());
        event.reply(builder.build());
    }
}
