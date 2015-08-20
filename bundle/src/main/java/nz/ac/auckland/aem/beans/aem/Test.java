package nz.ac.auckland.aem.beans.aem;

import nz.ac.auckland.lmzwidget.configuration.model.WidgetItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by gregkw on 15/12/14.
 */
public class Test {

    public static String camelCaseString(String text) {
        return StringUtils.uncapitalize(StringUtils.remove(WordUtils.capitalizeFully(StringUtils.stripToEmpty(text)), " "));
    }

    public static void main(String[] args) {
        String text = "This is    tHe Value";

        System.out.println(camelCaseString(text));

        System.out.println(StringUtils.isNotBlank("as") && StringUtils.isNotBlank("ss") && StringUtils.isNotBlank("ss"));

        String[] VALID_TYPES = new String[] {"bool",         "dropdown",     "number",       "string",       "textarea"};
        String[] X_TYPES = new String[]     {"selection",    "selection",    "numberfield",  "textfield",    "textarea"};
        String[] TYPES = new String[]       {"checkbox",     "select",       null,           null,           null};

        int type4 = Arrays.binarySearch(VALID_TYPES, "bool");
        int type3 = Arrays.binarySearch(VALID_TYPES, "dropdown");
        int type = Arrays.binarySearch(VALID_TYPES, "number");
        int type1 = Arrays.binarySearch(VALID_TYPES, "string");
        int type2 = Arrays.binarySearch(VALID_TYPES, "textarea");

        System.out.println(type4 + "" + type3 + "" + type + "" + type1 + "" + type2);

        String xx = "http://www.youtube.com/oembed?url=http://youtube.com/watch/v=VIDEOID&format=FORMAT";
        System.out.println("xx: " + xx.replace("VIDEOID", "zK4fugELDTI" ));

        String requestUrl = "http://www.youtube.com/oembed?url=http%3A//youtube.com/watch%3Fv%3DVIDEOID&format=FORMAT";
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("videoID", "zK4fugELDTI");
        properties.put("format", "json");

        String [] escCharList = "[VIDEOID];videoID-[FORMAT];format".split("-");
        if (requestUrl != null) {
            for (String escpair : escCharList) {
                String[] kvPairs = escpair.split(";");
                String key = kvPairs[0];
                String value = kvPairs[1];
                if (key.startsWith("[")) {
                    key = key.substring(1, key.length() - 1);
                    value = properties.get(value);
                    System.out.println(key + " : " + value);
                }
                requestUrl = requestUrl.replace(key, value);
            }
        }
        System.out.println("new URL"+ requestUrl);

        System.out.println("EMAIL PATTERN MATCH: " + Pattern.matches(WidgetItem.PATTERNS.get("email"), "greg.kw@mmm.com"));

    }
}
