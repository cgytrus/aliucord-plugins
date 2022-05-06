package com.github.cgytrus;

import android.content.Context;

import com.aliucord.Utils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.api.CommandsAPI;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.*;
import com.aliucord.utils.ReflectUtils;
import com.discord.api.commands.ApplicationCommandType;
import com.discord.widgets.chat.MessageContent;
import com.discord.widgets.chat.MessageManager;
import com.discord.widgets.chat.input.ChatInputViewModel;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.jvm.functions.Function1;

@AliucordPlugin()
public class UwuifierPlugin extends Plugin {
    private final Pattern[] ignoreIn = {
        Pattern.compile("^<#(?<id>\\d{17,19})>$"), // channel
        Pattern.compile("<a?:\\w{2,32}:\\d{17,18}>"), // emote
        Pattern.compile("^<@&(?<id>\\d{17,19})>$"), // role
        Pattern.compile("^<@!?(?<id>\\d{17,19})>$"), // user
        Pattern.compile("(http(s)?://.)?(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&/=]*)"), // link
        Pattern.compile("^(@everyone|@here)$"), // global ping
        Pattern.compile("```.*```", Pattern.DOTALL), // multi-line code
        Pattern.compile("`.*`", Pattern.DOTALL), // single-line code
    };

    private boolean _ignoreNext = false;

    public UwuifierPlugin() {
        settingsTab = new SettingsTab(UwuifierSettingsPage.class, SettingsTab.Type.BOTTOM_SHEET)
            .withArgs(settings);
    }

    @Override
    public void start(Context context) throws Throwable {
        Uwuifier.getSettings().periodToExclamationChance =
            settings.getFloat("periodToExclamationChance", Uwuifier.getSettings().periodToExclamationChance);
        Uwuifier.getSettings().stutterChance =
            settings.getFloat("stutterChance", Uwuifier.getSettings().stutterChance);
        Uwuifier.getSettings().presuffixChance =
            settings.getFloat("presuffixChance", Uwuifier.getSettings().presuffixChance);
        Uwuifier.getSettings().suffixChance =
            settings.getFloat("suffixChance", Uwuifier.getSettings().suffixChance);
        Uwuifier.getSettings().duplicateCharactersChance =
            settings.getFloat("duplicateCharactersChance", Uwuifier.getSettings().duplicateCharactersChance);
        Uwuifier.getSettings().duplicateCharactersAmount =
            settings.getInt("duplicateCharactersAmount", Uwuifier.getSettings().duplicateCharactersAmount);

        patcher.patch(ChatInputViewModel.class.getDeclaredMethod("sendMessage", Context.class,
            MessageManager.class, MessageContent.class, List.class, boolean.class,
            Function1.class), new PreHook(cf -> {
            MessageContent content = (MessageContent)cf.args[2];
            try {
                if(!settings.getBool("enabled", true) || _ignoreNext) {
                    _ignoreNext = false;
                    return;
                }
                ReflectUtils.setField(content, "textContent", uwuifyMessage(content.component1()));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }));

        commands.registerCommand("uwu", Uwuifier.uwuify("Toggle uwuifying for this message."),
            Utils.createCommandOption(ApplicationCommandType.STRING, "message",
                null, null, true, true), ctx -> {
                    if(settings.getBool("enabled", true)) {
                        _ignoreNext = true;
                        return new CommandsAPI.CommandResult(ctx.getRequiredString("message"));
                    }
                    return new CommandsAPI.CommandResult(uwuifyMessage(ctx.getRequiredString("message")));
                });
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
        commands.unregisterAll();
    }

    private String uwuifyMessage(String text) {
        return Uwuifier.uwuify(text, true, (offset, string) -> {
            for(Pattern pattern : ignoreIn) {
                Matcher matcher = pattern.matcher(string);
                while(matcher.find()) {
                    if(matcher.start() <= offset && offset <= matcher.end())
                        return true;
                }
            }
            return false;
        });
    }
}
