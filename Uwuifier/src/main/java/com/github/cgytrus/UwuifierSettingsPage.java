package com.github.cgytrus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aliucord.Utils;
import com.aliucord.api.SettingsAPI;
import com.aliucord.utils.DimenUtils;
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
            () -> Uwuifier.Settings.periodToExclamationChance,
            (value) -> {
                settings.setFloat("periodToExclamationChance", value);
                Uwuifier.Settings.periodToExclamationChance = value;
            });

        addSliderView(ctx, "Stutter chance", null,
            () -> Uwuifier.Settings.stutterChance,
            (value) -> {
                settings.setFloat("stutterChance", value);
                Uwuifier.Settings.stutterChance = value;
            });

        addSliderView(ctx, "Presuffix chance",
            "Chance of a tilde (~) appearing at the end of your message",
            () -> Uwuifier.Settings.presuffixChance,
            (value) -> {
                settings.setFloat("presuffixChance", value);
                Uwuifier.Settings.presuffixChance = value;
            });

        addSliderView(ctx, "Suffix chance",
            "Chance of a suffix (\"nya~\", \"^^;;\" etc.) appearing at the end of your message",
            () -> Uwuifier.Settings.suffixChance,
            (value) -> {
                settings.setFloat("suffixChance", value);
                Uwuifier.Settings.suffixChance = value;
            });
    }

    @SuppressLint("DefaultLocale")
    private void addSliderView(Context ctx, String label, String subtext,
                               Function0<Float> getValue, Action1<Float> setValue) {
        TextView sliderLabel = new TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Label);
        sliderLabel.setText(uwuify(label));

        TextView sliderText = new TextView(ctx, null, 0, R.i.UiKit_TextView);
        sliderText.setText(String.format("%d%%", (int)(getValue.invoke() * 100f)));
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
        slider.setMax(100);
        slider.setProgress((int)(getValue.invoke() * 100f));
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setValue.call(progress / 100f);
                sliderText.setText(String.format("%d%%", (int)(getValue.invoke() * 100f)));
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
