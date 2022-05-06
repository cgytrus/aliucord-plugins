package com.github.cgytrus;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;

public class Uwuifier {
    public static class Settings {
        public float periodToExclamationChance = 0.2f;
        public float stutterChance = 0.1f;
        public float presuffixChance = 0.1f;
        public float suffixChance = 0.3f;
        public float duplicateCharactersChance = 0.4f;
        public int duplicateCharactersAmount = 3;
    }

    private static Settings _settings = new Settings();

    public static Settings getSettings() {
        return _settings;
    }

    public static void resetSettings() {
        _settings = new Settings();
    }

    private static boolean getChance(float chance) {
        return Math.random() < chance;
    }

    private static final Pattern escapePattern = Pattern.compile("(?=[~_<>])");
    private static String escapeString(String string) {
        return escapePattern.matcher(string).replaceAll("\\\\");
    }

    private static boolean isCaps(String string) {
        return string.equals(string.toUpperCase());
    }

    private static boolean isNullOrWhitespace(String s) {
        if(s == null)
            return true;
        for(int i = 0; i < s.length(); i++)
            if(!Character.isWhitespace(s.charAt(i)))
                return false;
        return true;
    }

    private static final Map<Pattern, String> simpleReplacements = new HashMap<>() {{
        put(Pattern.compile("l"), "w");
        put(Pattern.compile("r"), "w");
        put(Pattern.compile("na"), "nya");
        put(Pattern.compile("ne"), "nye");
        put(Pattern.compile("ni"), "nyi");
        put(Pattern.compile("no"), "nyo");
        put(Pattern.compile("nu"), "nyu");
        put(Pattern.compile("pow"), "paw");
        put(Pattern.compile("(?<!w)ui"), "wi");
        put(Pattern.compile("(?<!w)ue"), "we");
        put(Pattern.compile("attempt"), "attwempt");
        put(Pattern.compile("config"), "cwonfig");
    }};

    private static final Map<String, String> wordReplacements = new HashMap<>() {{
        put("you", "uwu");
        put("no", "nu");
        put("oh", "ow");
        put("too", "two");
        put("attempt", "attwempt");
        put("config", "cwonfig");
    }};

    private static class SuffixChoice {
        private String _string = null;
        private SuffixChoice[] _choices = null;

        public SuffixChoice(String string) {
            _string = string;
        }

        public SuffixChoice(SuffixChoice[] choices) {
            _choices = choices;
        }

        public String choose() {
            if(_choices != null)
                return _choices[(int)Math.floor(Math.random() * _choices.length)].choose();
            else if(_string != null)
                return _string;
            return "";
        }
    }

    private static final SuffixChoice presuffixes = new SuffixChoice(new SuffixChoice[] {
        new SuffixChoice("~"),
        new SuffixChoice("~~"),
        new SuffixChoice(",")
    });

    private static final SuffixChoice suffixes = new SuffixChoice(new SuffixChoice[] {
        new SuffixChoice(":D"),
        new SuffixChoice(new SuffixChoice[] {
            new SuffixChoice("xD"),
            new SuffixChoice("XD")
        }),
        new SuffixChoice(":P"),
        new SuffixChoice(";3"),
        new SuffixChoice("<{^v^}>"),
        new SuffixChoice("^-^"),
        new SuffixChoice("x3"),
        new SuffixChoice(new SuffixChoice[] {
            new SuffixChoice("rawr"),
            new SuffixChoice("rawr~"),
            new SuffixChoice("rawr~~"),
            new SuffixChoice("rawr x3"),
            new SuffixChoice("rawr~ x3"),
            new SuffixChoice("rawr~~ x3")
        }),
        new SuffixChoice(new SuffixChoice[] {
            new SuffixChoice("owo"),
            new SuffixChoice("owo~"),
            new SuffixChoice("owo~~")
        }),
        new SuffixChoice(new SuffixChoice[] {
            new SuffixChoice("uwu"),
            new SuffixChoice("uwu~"),
            new SuffixChoice("uwu~~")
        }),
        new SuffixChoice("-.-"),
        new SuffixChoice(">w<"),
        new SuffixChoice(":3"),
        new SuffixChoice(new SuffixChoice[] {
            new SuffixChoice("nya"),
            new SuffixChoice("nya~"),
            new SuffixChoice("nya~~"),
            new SuffixChoice("nyaa"),
            new SuffixChoice("nyaa~"),
            new SuffixChoice("nyaa~~")
        }),
        new SuffixChoice(new SuffixChoice[] {
            new SuffixChoice(">_<"),
            new SuffixChoice(">-<")
        }),
        new SuffixChoice(":flushed:"),
        new SuffixChoice("\uD83D\uDC49\uD83D\uDC48"),
        new SuffixChoice(new SuffixChoice[] {
           new SuffixChoice("^^"),
           new SuffixChoice("^^;;")
        }),
        new SuffixChoice(new SuffixChoice[] {
            new SuffixChoice("w"),
            new SuffixChoice("ww")
        }),
        new SuffixChoice(",")
    });

    private static class Replacement {
        private final Pattern _pattern;
        private final Function2<Boolean, Function2<Integer, String, Boolean>,
            Function3<String, Integer, String, String>> _replacement;

        public Replacement(Pattern pattern, Function2<Boolean, Function2<Integer, String, Boolean>,
            Function3<String, Integer, String, String>> replacement) {
            this._pattern = pattern;
            this._replacement = replacement;
        }

        public Pattern getPattern() {
            return _pattern;
        }

        public Function3<String, Integer, String, String> getReplacement(boolean escape,
            Function2<Integer, String, Boolean> isIgnoredAt) {
            return _replacement.invoke(escape, isIgnoredAt);
        }
    }

    private static final Replacement[] replacements = {
        // . to !
        // match a . with a space or string end after it
        new Replacement(Pattern.compile("\\.(?= |$)"), (escape, isIgnoredAt) ->
            (match, offset, string) -> {
                if(isIgnoredAt.invoke(offset, string)) return match;
                if(!getChance(_settings.periodToExclamationChance))
                    return match;
                return "!";
            }),
        // duplicate characters
        new Replacement(Pattern.compile("[,!]"), (escape, isIgnoredAt) ->
            (match, offset, string) -> {
                if(isIgnoredAt.invoke(offset, string)) return match;
                if(getChance(_settings.duplicateCharactersChance)) {
                    int amount =
                        (int)Math.floor((Math.random() + 1) * (_settings.duplicateCharactersAmount - 1));

                    StringBuilder matchBuilder = new StringBuilder(match);
                    for(int i = 0; i < amount; i++)
                        matchBuilder.append(",");
                    match = matchBuilder.toString();
                }
                return match;
            }),
        // simple and word replacements
        // match a word
        new Replacement(Pattern.compile("(?<=\\b)[a-zA-Z']+(?=\\b)"), (escape, isIgnoredAt) ->
            (match, offset, string) -> {
                if(isIgnoredAt.invoke(offset, string)) return match;
                boolean caps = isCaps(match);
                match = match.toLowerCase();
                if(wordReplacements.containsKey(match))
                    match = wordReplacements.get(match); // only replace whole words
                if(match == null) return "";
                for(Map.Entry<Pattern, String> replacement : simpleReplacements.entrySet()) {
                    Matcher matcher = replacement.getKey().matcher(match);
                    // don't replace whole words
                    boolean wholeWord = false;
                    while(matcher.find()) {
                        if(matcher.group().equals(match)) {
                            wholeWord = true;
                            break;
                        }
                    }
                    if(wholeWord) continue;
                    matcher.reset();
                    match = matcher.replaceAll(replacement.getValue());
                }
                return caps ? match.toUpperCase() : match;
            }),
        // stutter
        // match beginning of a word
        new Replacement(Pattern.compile("(?<= |^)[a-zA-Z]"), (escape, isIgnoredAt) ->
            (match, offset, string) -> {
                if(isIgnoredAt.invoke(offset, string)) return match;
                if(!getChance(_settings.stutterChance))
                    return match;
                return String.format("%s-%s", match, match);
            }),
        // suffixes
        new Replacement(Pattern.compile("(?<=[.!?,;\\-])(?= )|(?=$)"), (escape, isIgnoredAt) ->
            (match, offset, string) -> {
                if(isIgnoredAt.invoke(offset, string)) return match;
                String presuffix = "";
                String suffix = "";
                if(getChance(_settings.presuffixChance))
                    presuffix = presuffixes.choose();
                if(getChance(_settings.suffixChance))
                    suffix = suffixes.choose();
                String finalSuffix = String.format("%s %s", presuffix, suffix);
                if(escape) finalSuffix = escapeString(finalSuffix);
                return finalSuffix;
            }),
    };

    public static String uwuify(String text, boolean escape,
        Function2<Integer, String, Boolean> isIgnoredAt) {
        if(isNullOrWhitespace(text)) return text;

        for(Replacement replacement : replacements) {
            Matcher matcher = replacement.getPattern().matcher(text);
            Function3<String, Integer, String, String> func = replacement.getReplacement(escape, isIgnoredAt);

            boolean result = matcher.find();
            if(result) {
                //noinspection StringBufferMayBeStringBuilder
                StringBuffer sb = new StringBuffer();
                do {
                    matcher.appendReplacement(sb, func.invoke(matcher.group(), matcher.start(), text));
                    result = matcher.find();
                } while(result);
                matcher.appendTail(sb);
                text = sb.toString();
            }
        }

        return text;
    }

    public static String uwuify(String text) {
        return uwuify(text, false, (offset, string) -> false);
    }
}
