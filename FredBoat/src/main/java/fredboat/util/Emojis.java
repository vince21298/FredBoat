/*
 *
 * MIT License
 *
 * Copyright (c) 2017 Frederik Ar. Mikkelsen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package fredboat.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by napster on 09.06.17.
 */
public class Emojis {

    private static final Logger log = LoggerFactory.getLogger(Emojis.class);


    //if we run out of numbers, we can start using the alphabetic letters
    public final static String NUMBER_0 = "0⃣";
    public final static String NUMBER_1 = "1⃣";
    public final static String NUMBER_2 = "2⃣";
    public final static String NUMBER_3 = "3⃣";
    public final static String NUMBER_4 = "4⃣";
    public final static String NUMBER_5 = "5⃣";
    public final static String NUMBER_6 = "6⃣";
    public final static String NUMBER_7 = "7⃣";
    public final static String NUMBER_8 = "8⃣";
    public final static String NUMBER_9 = "9⃣";
    public final static String NUMBER_10 = "🔟";

    public final static String CHECK = "✅";
    public final static String CROSS = "❌";

    //to be called by eval for a quick'n'dirty test whether all emojis that are defined in this class are being
    //displayed in the Discord client as expected
    public static String test() {
        final StringBuilder result = new StringBuilder();
        Arrays.stream(Emojis.class.getFields()).filter(field -> field.getType().equals(String.class)).forEach(field -> {
            try {
                result.append(field.get(null));
            } catch (final IllegalAccessException e) {
                result.append("exception");
                log.error("something something unexpected error while using reflection", e);
            }
        });
        return result.toString();
    }
}
