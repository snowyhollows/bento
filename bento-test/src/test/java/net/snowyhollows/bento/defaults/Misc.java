package net.snowyhollows.bento.defaults;

import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.WithFactory;

public class Misc {
    public final  float floatValue;
    public final int intValue;
    public final String stringValue;
    public final boolean boolValue;
    public final Day day;
    public final Day dayWithDefault;
    public final float floatValueWithDefault;
    public final int intValueWithDefault;
    public final String stringValueWithDefault;
    public final boolean boolValueWithDefault;

    /*
floatValue
intValue
stringValue
boolValue
day
dayWithDefault
floatValueWithDefault
intValueWithDefault
stringValueWithDefault
boolValueWithDefault
     */
    @WithFactory
    public Misc(@ByName("floatValue") float xxx,
                int intValue,
                @ByName("stringValue") String sValue,
                @ByName("boolValue") boolean bsue,
                Day day,
                @ByName(fallbackValue = "SUNDAY") Day dayWithDefault,
                @ByName(fallbackValue = "11") float floatValueWithDefault,
                @ByName(value = "intValueWithDefault", fallbackValue = "12") int iiiiiii,
                @ByName(fallbackValue = "ssssss") String stringValueWithDefault,
                @ByName(fallbackValue = "true") boolean boolValueWithDefault) {
        this.floatValue = xxx;
        this.intValue = intValue;
        this.stringValue = sValue;
        this.boolValue = bsue;
        this.day = day;
        this.dayWithDefault = dayWithDefault;
        this.floatValueWithDefault = floatValueWithDefault;
        this.intValueWithDefault = iiiiiii;
        this.stringValueWithDefault = stringValueWithDefault;
        this.boolValueWithDefault = boolValueWithDefault;
    }
}
