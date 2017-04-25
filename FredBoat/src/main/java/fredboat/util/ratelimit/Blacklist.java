package fredboat.util.ratelimit;

import fredboat.util.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by napster on 17.04.17.
 * <p>
 * Provides a forgiving blacklist with progressively increasing blacklist lengths
 */
public class Blacklist {

    //this holds progressively increasing lengths of blacklisting in milliseconds
    private static final List<Long> blacklistLevels;

    static {
        List<Long> levels = new ArrayList<>();
        levels.add(1000L * 60);                     //one minute
        levels.add(1000L * 600);                    //ten minutes
        levels.add(1000L * 3600);                   //one hour
        levels.add(1000L * 3600 * 24);              //24 hours
        levels.add(1000L * 3600 * 24 * 7);          //a week

        blacklistLevels = Collections.unmodifiableList(levels);
    }

    private final long rateLimitHitsBeforeBlacklist;

    private final ConcurrentHashMap<String, BlacklistEntry> blacklist;

    //users that can never be blacklisted
    private final Set<String> userWhiteList;


    public Blacklist(Set<String> userWhiteList, long rateLimitHitsBeforeBlacklist) {
        //TODO optimization: initialize the map with a sane high value, like the whole user base of fredboat (checkout where ;;mstats gets its numbers from)
        this.blacklist = new ConcurrentHashMap<>();

        this.rateLimitHitsBeforeBlacklist = rateLimitHitsBeforeBlacklist;
        this.userWhiteList = Collections.unmodifiableSet(userWhiteList);
    }

    /**
     * @param id check whether this id is blacklisted
     * @return true if the id is blacklisted, false if not
     */
    //This will be called really fucking often, should be able to be accessed non-synchronized for performance
    // -> don't do any writes in here
    // -> don't call expensive methods
    public boolean isBlacklisted(String id) {

        //first of all, ppl that can never get blacklisted no matter what
        if (userWhiteList.contains(id)) return false;

        BlacklistEntry blEntry = blacklist.get(id);
        if (blEntry == null) return false;     //blacklist entry doesn't even exist
        if (blEntry.level < 0) return false;   //blacklist entry exists, but id hasn't actually been blacklisted yet

        //id was a blacklisted, but it has run out
        if (System.currentTimeMillis() > blEntry.blacklistedTimestamp + (getBlacklistTimeLength(blEntry.level)))
            return false;

        //looks like this id is blacklisted ¯\_(ツ)_/¯
        return true;
    }

    public RateResult hitRateLimit(String id, RateResult result) {
        //update blacklist entry of this id
        BlacklistEntry blEntry = blacklist.get(id);
        if (blEntry == null)
            blEntry = getOrCreateBlacklistEntry(id);

        //synchronize on the individual blacklist entries since we are about to change and save them
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (blEntry) {
            blEntry.rateLimitReached++;
            if (blEntry.rateLimitReached >= rateLimitHitsBeforeBlacklist) {
                //issue blacklist incident
                blEntry.level++;
                if (blEntry.level < 0) blEntry.level = 0;
                blEntry.blacklistedTimestamp = System.currentTimeMillis();
                blEntry.rateLimitReached = 0; //reset these for the next time

                long milliseconds = getBlacklistTimeLength(blEntry.level);
                String duration = TextUtils.formatTime(milliseconds);
                result.reason = ":hammer: _**BLACKLISTED**_ :hammer: for **" + duration + "**";
            }
            return result;
        }
    }


    /**
     * synchronize the creation of new blacklist entries
     */
    private synchronized BlacklistEntry getOrCreateBlacklistEntry(String id) {
        //was one created in the meantime? use that
        BlacklistEntry result = blacklist.get(id);
        if (result != null) return result;

        //create and return it
        result = new BlacklistEntry(id);
        blacklist.put(id, result);
        return result;
    }

    /**
     * completely resets a blacklist for an id
     */
    public synchronized void liftBlacklist(String id) {
        blacklist.remove(id);
    }

    /**
     * Return length of a blacklist incident in milliseconds depending on the blacklist level
     */
    private long getBlacklistTimeLength(int blacklistLevel) {
        if (blacklistLevel < 0) return 0;
        return blacklistLevel >= blacklistLevels.size() ? blacklistLevels.get(blacklistLevels.size() - 1) : blacklistLevels.get(blacklistLevel);
    }

    class BlacklistEntry {
        //id of the user or guild that this blacklist entry belongs to
        String id;

        //blacklist level that the user or guild is on
        //this should increase every time progressively
        int level;

        //keeps track of how many times a user or guild reached the rate limit on the current blacklist level
        int rateLimitReached;

        //time when the id was blacklisted
        long blacklistedTimestamp;

        public BlacklistEntry(String id) {
            this.id = id;
            this.level = -1;
            this.rateLimitReached = 0;
            this.blacklistedTimestamp = System.currentTimeMillis();
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }
}
