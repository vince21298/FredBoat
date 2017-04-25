package fredboat.util.ratelimit;

import fredboat.Config;
import fredboat.FredBoat;
import fredboat.command.maintenance.ShardsCommand;
import fredboat.command.music.control.SkipCommand;
import fredboat.commandmeta.abs.Command;
import fredboat.util.DiscordUtil;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import org.eclipse.jetty.util.ConcurrentHashSet;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by napster on 17.04.17.
 * <p>
 * this object should be threadsafe by itself
 * <p>
 * http://i.imgur.com/ha0R3XZ.gif
 * <p>
 * TODO: exclude bot owner + admins?
 * TODO: i18n here in other classes in this package and subpackages
 * TODO: save blacklist (and maybe more) between restarts
 */
public class Ratelimiter {

    private static final int RATE_LIMIT_HITS_BEFORE_BLACKLIST = 5;


    //one ratelimiter for all running shards
    private static Ratelimiter ratelimiterSingleton;

    public static Ratelimiter getRatelimiter() {
        if (ratelimiterSingleton == null)
            ratelimiterSingleton = new Ratelimiter();

        return ratelimiterSingleton;
    }


    private final Set<Ratelimit> ratelimits;
    private final Blacklist blacklist;

    private Ratelimiter() {
        Set<String> whitelist = new ConcurrentHashSet<>();

        //it is ok to use the jda of any shard as long as we aren't using it for guild specific stuff
        JDA jda = FredBoat.getFirstJDA();
        whitelist.add(DiscordUtil.getOwnerId(jda));
        whitelist.add(jda.getSelfUser().getId());

        blacklist = new Blacklist(whitelist, RATE_LIMIT_HITS_BEFORE_BLACKLIST);

        //Create all the rate limiters we want
        ratelimits = new HashSet<>();

        Blacklist bl = null;
        if (Config.CONFIG.useAutoBlacklist())
            bl = blacklist;

        ratelimits.add(new Ratelimit(whitelist, Ratelimit.Scope.USER, 5, 10000, Command.class, bl));
        ratelimits.add(new Ratelimit(whitelist, Ratelimit.Scope.USER, 5, 20000, SkipCommand.class, bl));
        ratelimits.add(new Ratelimit(whitelist, Ratelimit.Scope.USER, 2, 30000, ShardsCommand.class, bl));

        //don't blacklist guilds
        ratelimits.add(new Ratelimit(whitelist, Ratelimit.Scope.GUILD, 10, 10000, Command.class, null));
        ratelimits.add(new Ratelimit(whitelist, Ratelimit.Scope.GUILD, 1000, 120000, SlowImportedPlaylistRateIdentifier.class, null));
    }

    public RateResult isAllowed(Member invoker, Object object) {
        return isAllowed(invoker, object, 1);
    }

    public RateResult isAllowed(Member invoker, Object object, int weight) {
        for (Ratelimit ratelimit : ratelimits) {
            if (ratelimit.getClazz().isInstance(object)) {
                RateResult result = ratelimit.isAllowed(invoker, weight);
                if (!result.allowed) return result;
            }
        }
        return new RateResult(true, "Command is not ratelimited");
    }

    /**
     * @param id Id of the object whose blacklist status is to be checked, for example a userId or a guildId
     * @return true if the id is blacklisted, false if it's not
     */
    public boolean isBlacklisted(String id) {
        return blacklist.isBlacklisted(id);
    }

    /**
     * Reset rate limits for the given id and removes it from the blacklist
     */
    public void liftLimitAndBlacklist(String id) {
        for (Ratelimit ratelimit : ratelimits) {
            ratelimit.liftLimit(id);
        }
        blacklist.liftBlacklist(id);
    }
}
