package fredboat.util.ratelimit;

import net.dv8tion.jda.core.entities.Member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by napster on 17.04.17.
 * <p>
 * This class uses an algorithm based on leaky bucket, but is optimized, mainly we work around having tons of threads for
 * each bucket filling/emptying it, instead saving timestamps. As a result this class works better for shorter time
 * periods, as the amount of timestamps to hold decreases.
 * some calculations can be found here: https://docs.google.com/spreadsheets/d/1Afdn25AsFD-v3WQGp56rfVwO1y2d105IQk3dtfTcKwA/edit#gid=0
 */
public class Ratelimit {

    public enum Scope {USER, GUILD}

    private final ConcurrentHashMap<String, Rate> limits;
    private final long maxRequests;
    private final long timeSpan;
    private Blacklist blacklist;

    //users that can never be limited
    private final Set<String> userWhiteList;

    //are we limiting the individual user or whole guilds?
    private final Scope scope;

    //class of commands this ratelimiter should be restricted to
    //creative use allows usage of other classes
    private final Class clazz;

    public Class getClazz() {
        return clazz;
    }

    /**
     * @param userWhiteList whitelist of user that should never be rate limited or blacklisted by this object
     * @param scope         on which scope this rate limiter shall operate
     * @param maxRequests   how many maxRequests shall be possible in the specified time
     * @param milliseconds  time in milliseconds, in which maxRequests shall be allowed
     * @param clazz         the optional (=can be null) clazz of commands to be ratelimited by this ratelimiter
     * @param blacklist     the optional (=can be null) blacklist, if none is set, no auto blacklist will be issued
     */
    public Ratelimit(Set<String> userWhiteList, Scope scope, long maxRequests, long milliseconds, Class clazz, Blacklist blacklist) {
        //TODO optimization: initialize the map with a sane high value, like the whole user base of fredboat (checkout where ;;mstats gets its numbers from)
        this.limits = new ConcurrentHashMap<>();

        this.userWhiteList = Collections.unmodifiableSet(userWhiteList);
        this.scope = scope;
        this.maxRequests = maxRequests;
        this.timeSpan = milliseconds;
        this.clazz = clazz;
        this.blacklist = blacklist;
    }


    /**
     * @return a RateResult object containing information whether the users request is rate limited or not and the reason for that
     * <p>
     * Caveat: This allows requests to overstep the ratelimit with single high weight requests.
     * The clearing of timestamps ensures it will take longer for them to get available again though.
     */
    public RateResult isAllowed(Member invoker, int weight) {
        //This gets called real often, right before every command execution. Keep it light, don't do any blocking stuff,
        //ensure whatever you do in here is threadsafe, but minimize usage of synchronized as it adds overhead

        //first of all, ppl that can never get limited or blacklisted, no matter what
        if (userWhiteList.contains(invoker.getUser().getId())) return new RateResult(true, "User is whitelisted");

        RateResult result = new RateResult(false, "Rate limit has not been calculated");

        //user or guild scope?
        String id = invoker.getUser().getId();
        if (scope == Scope.GUILD) id = invoker.getGuild().getId();

        Rate rate = limits.get(id);
        if (rate == null)
            rate = getOrCreateRate(id);

        //synchronize on the individual rate objects since we are about to change and save them
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (rate) {
            long now = System.currentTimeMillis();

            //clear outdated timestamps
            long maxTimeStampsToClear = (now - rate.lastUpdated) * maxRequests / timeSpan;
            long cleared = 0;
            while (rate.timeStamps.size() > 0 && rate.timeStamps.get(0) + timeSpan < now && cleared < maxTimeStampsToClear) {
                rate.timeStamps.remove(0);
                cleared++;
            }

            rate.lastUpdated = now;
            //ALLOWED?
            if (rate.timeStamps.size() < maxRequests) {
                for (int i = 0; i < weight; i++)
                    rate.timeStamps.add(now);
                result.allowed = true;
                result.reason = "You shall pass.";
                //everything is fine, get out of this method
                return result;
            }
        }

        //reaching this point in the code means a rate limit was hit
        //the following code has to handle that
        result.allowed = false;
        result.reason = "You shall not pass. Rate limit reached"; //TODO make sure the feedback the user receivers is more informative


        if (blacklist != null)
            result = blacklist.hitRateLimit(id, result);
        return result;
    }


    /**
     * synchronize the creation of new Rate objects
     */
    private synchronized Rate getOrCreateRate(String id) {
        //was one created on the meantime? use that
        Rate result = limits.get(id);
        if (result != null) return result;

        //create, save and return it
        result = new Rate(id);
        limits.put(id, result);
        return result;
    }

    /**
     * completely resets a limit for an id (user or guild for example)
     */
    public synchronized void liftLimit(String id) {
        limits.remove(id);
    }

    class Rate {
        //to whom this belongs
        String id;

        //last time this object was updated
        //useful for keeping track of how many timeStamps should be removed to ensure the limit is enforced
        long lastUpdated;

        //collects the requests
        ArrayList<Long> timeStamps;

        private Rate(String id) {
            this.id = id;
            this.lastUpdated = System.currentTimeMillis();
            this.timeStamps = new ArrayList<>();
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }
}
