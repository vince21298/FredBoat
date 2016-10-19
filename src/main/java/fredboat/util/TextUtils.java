package fredboat.util;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import fredboat.FredBoat;
import fredboat.commandmeta.MessagingException;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

public class TextUtils {

    private TextUtils() {
    }

    public static Message prefaceWithMention(User user, String msg) {
        MessageBuilder builder = new MessageBuilder().appendMention(user).appendString(msg);
        return builder.build();
    }

    public static Message replyWithMention(TextChannel channel, User user, String msg) {
        MessageBuilder builder = new MessageBuilder().appendMention(user).appendString(msg);
        Message mes = builder.build();
        channel.sendMessage(mes);
        return mes;
    }

    public static void handleException(Throwable e, MessageChannel channel) {
        handleException(e, channel, null);
    }

    public static void handleException(Throwable e, MessageChannel channel, User invoker) {
        if (e instanceof MessagingException) {
            channel.sendMessage(invoker.getUsername() + ": " + e.getMessage());
            return;
        }
        MessageBuilder builder = new MessageBuilder();

        if (invoker != null) {
            builder.appendMention(invoker);
            
            String filtered = " an error occured :anger: ```java\n" + e.toString() + "\n";
            
            for(String str : FredBoat.getGoogleKeys()){
                filtered = filtered.replace(str, "GOOGLE_SERVER_KEY");
            }
            
            builder.appendString(filtered);
        } else {
            String filtered = "An error occured :anger: ```java\n" + e.toString() + "\n";
            
            for(String str : FredBoat.getGoogleKeys()){
                filtered = filtered.replace(str, "GOOGLE_SERVER_KEY");
            }
            
            builder.appendString(filtered);
        }

        //builder.appendString("```java\n");
        for (StackTraceElement ste : e.getStackTrace()) {
            builder.appendString("\t" + ste.toString() + "\n");
            if ("prefixCalled".equals(ste.getMethodName())) {
                break;
            }
        }
        builder.appendString("\t...```");

        try {
            channel.sendMessage(builder.build());
        } catch (UnsupportedOperationException tooLongEx) {
            channel.sendMessage("An error occured :anger: Error was too long to display.");
            e.printStackTrace();
        }
    }

    public static String postToHastebin(String body) throws UnirestException {
        return Unirest.post("http://hastebin.com/documents").body(body).asJson().getBody().getObject().getString("key");
    }

    public static String postToHastebin(String body, boolean asURL) throws UnirestException {
        if (asURL) {
            return "http://hastebin.com/" + postToHastebin(body);
        } else {
            return postToHastebin(body);
        }
    }

    public static String formatTime(long millis) {
        long t = millis / 1000L;
        int sec = (int) (t % 60L);
        int min = (int) ((t % 3600L) / 60L);
        int hrs = (int) (t / 3600L);

        String timestamp;

        if (hrs != 0) {
            timestamp = forceTwoDigits(hrs) + ":" + forceTwoDigits(min) + ":" + forceTwoDigits(sec);
        } else {
            timestamp = forceTwoDigits(min) + ":" + forceTwoDigits(sec);
        }

        return timestamp;
    }

    private static String forceTwoDigits(int i) {
        return i < 10 ? "0" + i : Integer.toString(i);
    }
}
