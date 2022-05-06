package com.github.cgytrus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aliucord.Utils;
import com.aliucord.api.SettingsAPI;
import com.aliucord.utils.DimenUtils;
import com.aliucord.views.DangerButton;
import com.aliucord.views.Divider;
import com.aliucord.widgets.BottomSheet;
import com.discord.views.CheckedSetting;
import com.lytefast.flexinput.R;

import kotlin.jvm.functions.Function0;
import rx.functions.Action1;

public class UwuifierSettingsPage extends BottomSheet {
    private final SettingsAPI settings;

    public UwuifierSettingsPage(SettingsAPI settings) {
        this.settings = settings;
    }

    private String uwuify(String text) {
        if(!settings.getBool("enabled", true)) return text;
        return Uwuifier.uwuify(text);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);

        Context ctx = view.getContext();

        CheckedSetting enabledCheckbox = Utils.createCheckedSetting(ctx,
            CheckedSetting.ViewType.SWITCH, uwuify("Enabled"), null);
        enabledCheckbox.setChecked(settings.getBool("enabled", true));
        enabledCheckbox.setOnCheckedListener(check -> settings.setBool("enabled", check));
        addView(enabledCheckbox);

        addSliderView(ctx, "Period to exclamation chance",
            "Chance of a period being replaced with an exclamation mark",
            100, "%d%%", () -> (int)(Uwuifier.getSettings().periodToExclamationChance * 100f),
            (value) -> {
                settings.setFloat("periodToExclamationChance", value / 100f);
                Uwuifier.getSettings().periodToExclamationChance = value / 100f;
            });

        addSliderView(ctx, "Stutter chance", null,
            100, "%d%%", () -> (int)(Uwuifier.getSettings().stutterChance * 100f),
            (value) -> {
                settings.setFloat("stutterChance", value / 100f);
                Uwuifier.getSettings().stutterChance = value / 100f;
            });

        addSliderView(ctx, "Presuffix chance",
            "Chance of a tilde (~) appearing at the end of your message",
            100, "%d%%", () -> (int)(Uwuifier.getSettings().presuffixChance * 100f),
            (value) -> {
                settings.setFloat("presuffixChance", value / 100f);
                Uwuifier.getSettings().presuffixChance = value / 100f;
            });

        addSliderView(ctx, "Suffix chance",
            "Chance of a suffix (\"nya~\", \"^^;;\" etc.) appearing at the end of your message",
            100, "%d%%", () -> (int)(Uwuifier.getSettings().suffixChance * 100f),
            (value) -> {
                settings.setFloat("suffixChance", value / 100f);
                Uwuifier.getSettings().suffixChance = value / 100f;
            });

        addSliderView(ctx, "Duplicate characters chance",
            "Chance of a specific character (',' and '!') getting duplicated",
            100, "%d%%", () -> (int)(Uwuifier.getSettings().duplicateCharactersChance * 100f),
            (value) -> {
                settings.setFloat("duplicateCharactersChance", value / 100f);
                Uwuifier.getSettings().duplicateCharactersChance = value / 100f;
            });

        addSliderView(ctx, "Duplicate characters amount", null,
            12, null, () -> Uwuifier.getSettings().duplicateCharactersAmount,
            (value) -> {
                settings.setInt("duplicateCharactersAmount", value);
                Uwuifier.getSettings().duplicateCharactersAmount = value;
            });

        DangerButton resetButton = new DangerButton(ctx);
        resetButton.setText("Reset");
        resetButton.setOnClickListener(v -> {
            settings.resetSettings();
            Uwuifier.resetSettings();
            dismiss();
        });
        addView(resetButton);
    }

    @SuppressLint("DefaultLocale")
    private void addSliderView(Context ctx, String label, String subtext, int max, String format,
                               Function0<Integer> getValue, Action1<Integer> setValue) {
        TextView sliderLabel = new TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Label);
        sliderLabel.setText(uwuify(label));

        Function0<String> getText = () ->
            format == null ?
                getValue.invoke().toString() :
                String.format(format, getValue.invoke());

        TextView sliderText = new TextView(ctx, null, 0, R.i.UiKit_TextView);
        sliderText.setText(getText.invoke());
        sliderText.setWidth(DimenUtils.dpToPx(35));

        TextView sliderSubtext = null;
        if(subtext != null) {
            sliderSubtext = new TextView(ctx, null, 0, R.i.UiKit_Settings_Item_SubText);
            sliderSubtext.setText(uwuify(subtext));
            sliderSubtext.setPadding(DimenUtils.dpToPx(12), 0, DimenUtils.dpToPx(12), DimenUtils.dpToPx(12));
        }

        SeekBar slider = new SeekBar(ctx, null, 0, R.i.UiKit_SeekBar);
        slider.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        slider.setPadding(DimenUtils.dpToPx(12), 0, DimenUtils.dpToPx(12), 0);
        slider.setMax(max);
        slider.setProgress(getValue.invoke());
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setValue.call(progress);
                sliderText.setText(getText.invoke());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        LinearLayout layout = new LinearLayout(ctx, null, 0, R.i.UiKit_Settings_Item);
        layout.addView(sliderText);
        layout.addView(slider);

        addView(sliderLabel);
        addView(layout);
        if(subtext != null) addView(sliderSubtext);
    }
}
