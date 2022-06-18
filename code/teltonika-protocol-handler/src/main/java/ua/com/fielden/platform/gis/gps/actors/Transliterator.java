package ua.com.fielden.platform.gis.gps.actors;

import java.util.HashMap;
import java.util.Map;

/**
 * <a href='https://www.ukrainianlanguage.org.uk/read/reference/translit.htm'>Ukrainian Language Transliteration</a>
 * 
 * @author TG Team
 *
 */
public class Transliterator {

    private static final Map<Character, String> charMap = new HashMap<>();

    static {
        charMap.put('А', "A");
        charMap.put('Б', "B");
        charMap.put('В', "V");
        charMap.put('Г', "H");
        charMap.put('Ґ', "G");
        charMap.put('Д', "D");
        charMap.put('Е', "E");
        charMap.put('Ё', "E");
        charMap.put('Є', "Ye");
        charMap.put('Ж', "Zh");
        charMap.put('З', "Z");
        charMap.put('И', "Y");
        charMap.put('І', "I");
        charMap.put('Ї', "Yi");
        charMap.put('Й', "Y");
        charMap.put('К', "K");
        charMap.put('Л', "L");
        charMap.put('М', "M");
        charMap.put('Н', "N");
        charMap.put('О', "O");
        charMap.put('П', "P");
        charMap.put('Р', "R");
        charMap.put('С', "S");
        charMap.put('Т', "T");
        charMap.put('У', "U");
        charMap.put('Ф', "F");
        charMap.put('Х', "Kh");
        charMap.put('Ц', "Ts");
        charMap.put('Ч', "Ch");
        charMap.put('Ш', "Sh");
        charMap.put('Щ', "Shch");
        charMap.put('Ъ', "'");
        charMap.put('Ы', "Y");
        charMap.put('Ь', "'");
        charMap.put('Э', "E");
        charMap.put('Ю', "Yu");
        charMap.put('Я', "Ya");
        charMap.put('а', "a");
        charMap.put('б', "b");
        charMap.put('в', "v");
        charMap.put('г', "h");
        charMap.put('ґ', "g");
        charMap.put('д', "d");
        charMap.put('е', "e");
        charMap.put('є', "ie");
        charMap.put('ё', "e");
        charMap.put('ж', "zh");
        charMap.put('з', "z");
        charMap.put('и', "y");
        charMap.put('і', "i");
        charMap.put('ї', "i");
        charMap.put('й', "i");
        charMap.put('к', "k");
        charMap.put('л', "l");
        charMap.put('м', "m");
        charMap.put('н', "n");
        charMap.put('о', "o");
        charMap.put('п', "p");
        charMap.put('р', "r");
        charMap.put('с', "s");
        charMap.put('т', "t");
        charMap.put('у', "u");
        charMap.put('ф', "f");
        charMap.put('х', "kh");
        charMap.put('ц', "ts");
        charMap.put('ч', "ch");
        charMap.put('ш', "sh");
        charMap.put('щ', "shch");
        charMap.put('ъ', "'");
        charMap.put('ы', "y");
        charMap.put('ь', "'");
        charMap.put('э', "e");
        charMap.put('ю', "iu");
        charMap.put('я', "ya");
    }

    public static String transliterate(final String string) {
        final StringBuilder transliteratedString = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            final Character ch = string.charAt(i);
            final String charFromMap = charMap.get(ch);
            if (charFromMap == null) {
                transliteratedString.append(ch);
            } else {
                transliteratedString.append(charFromMap);
            }
        }
        return transliteratedString.toString();
    }

}