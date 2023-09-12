package sc.denishik.ru.midwayApi.help;

import static sc.denishik.ru.midwayApi.help.other.joinToString$default;
import static sc.denishik.ru.midwayApi.help.other.joinToString$defaultC;
import static sc.denishik.ru.midwayApi.help.other.replace$default;
import static sc.denishik.ru.midwayApi.help.other.split$default;

import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.view.View;

import androidx.core.internal.view.SupportMenu;

import com.blankj.utilcode.util.LogUtils;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.IntRange;
import kotlin.text.CharsKt;
import kotlin.text.StringsKt;
import sc.denishik.ru.midwayApi.Config;

public class StringExtKT {
    @NotNull
    public static final String addFirst(@NotNull String str, @NotNull String s2) {
        Intrinsics.checkNotNullParameter(str, "<this>");
        Intrinsics.checkNotNullParameter(s2, "s");
        return s2 + str;
    }

    @NotNull
    public static final String addLast(@NotNull String str, @NotNull String s2) {
        Intrinsics.checkNotNullParameter(str, "<this>");
        Intrinsics.checkNotNullParameter(s2, "s");
        return str + s2;
    }

    @NotNull
    public static final String addSpaceEvery2Charts(@NotNull String str) {
        String replace$default;
        CharSequence trim;
        Intrinsics.checkNotNullParameter(str, "<this>");
        replace$default = replace$default(str, " ", "", false, 4, (Object) null);
        StringBuilder sb = new StringBuilder();
        int length = replace$default.length() / 2;
        for (int i2 = 0; i2 < length; i2++) {
            int i3 = i2 * 2;
            String substring = replace$default.substring(i3, i3 + 2);
            Intrinsics.checkNotNullExpressionValue(substring, "this as java.lang.String…ing(startIndex, endIndex)");
            sb.append(substring);
            sb.append(" ");
        }
        String sb2 = sb.toString();
        Intrinsics.checkNotNullExpressionValue(sb2, "sb.toString()");
        trim = StringsKt.trim((CharSequence) sb2);
        return trim.toString();
    }

    @NotNull
    public static final byte[] ascii2ByteArray(@NotNull String str, boolean z2) {
        Intrinsics.checkNotNullParameter(str, "<this>");
        if (!z2) {
            str = replace$default(str, " ", "", false, 4, (Object) null);
        }
        Charset forName = Charset.forName("US-ASCII");
        Intrinsics.checkNotNullExpressionValue(forName, "forName(charsetName)");
        byte[] bytes = str.getBytes(forName);
        Intrinsics.checkNotNullExpressionValue(bytes, "this as java.lang.String).getBytes(charset)");
        return bytes;
    }

    public static /* synthetic */ byte[] ascii2ByteArray$default(String str, boolean z2, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            z2 = false;
        }
        return ascii2ByteArray(str, z2);
    }

    @NotNull
    public static final String filterRule(@NotNull String str) throws PatternSyntaxException {
        Intrinsics.checkNotNullParameter(str, "<this>");
        String replaceAll = Pattern.compile("[^a-zA-Z0-9_\\-. ]").matcher(str).replaceAll("");
        Intrinsics.checkNotNullExpressionValue(replaceAll, "m.replaceAll(\"\")");
        int length = replaceAll.length() - 1;
        int i2 = 0;
        boolean z2 = false;
        while (i2 <= length) {
            boolean z3 = Intrinsics.compare((int) replaceAll.charAt(!z2 ? i2 : length), 32) <= 0;
            if (z2) {
                if (!z3) {
                    break;
                }
                length--;
            } else if (z3) {
                i2++;
            } else {
                z2 = true;
            }
        }
        return replaceAll.subSequence(i2, length + 1).toString();
    }

    @NotNull
    public static final String formatString(@NotNull String str, @NotNull String format) {
        Intrinsics.checkNotNullParameter(str, "<this>");
        Intrinsics.checkNotNullParameter(format, "format");
        String format2 = String.format(Locale.getDefault(), format, Arrays.copyOf(new Object[]{str}, 1));
        Intrinsics.checkNotNullExpressionValue(format2, "format(locale, format, *args)");
        return format2;
    }

    @NotNull
    public static final byte[] hex2ByteArray(@NotNull String str) {
        String replace$default;
        int checkRadix;
        Intrinsics.checkNotNullParameter(str, "<this>");
        replace$default = replace$default(str, " ", "", false, 4, (Object) null);
        byte[] bArr = new byte[replace$default.length() / 2];
        int length = replace$default.length() / 2;
        for (int i2 = 0; i2 < length; i2++) {
            int i3 = i2 * 2;
            String substring = replace$default.substring(i3, i3 + 2);
            Intrinsics.checkNotNullExpressionValue(substring, "this as java.lang.String…ing(startIndex, endIndex)");
            checkRadix = CharsKt.checkRadix(16);
            bArr[i2] = (byte) Integer.parseInt(substring, checkRadix);
        }
        return bArr;
    }

    public static final boolean isAscii(@NotNull String str) throws PatternSyntaxException {
        Intrinsics.checkNotNullParameter(str, "<this>");
        return !Pattern.compile("[^a-zA-Z0-9_.-]").matcher(str).matches();
    }

    @NotNull
    public static final String keepDigital(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "<this>");
        StringBuilder sb = new StringBuilder();
        Matcher matcher = Pattern.compile("\\d").matcher(str);
        while (matcher.find()) {
            sb.append(matcher.group());
        }
        String sb2 = sb.toString();
        Intrinsics.checkNotNullExpressionValue(sb2, "newString.toString()");
        return sb2;
    }

    public static final void logA(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "<this>");
        if (Config.INSTANCE.isLogEnabled()) {
            String now$default = Common.now$default(Common.INSTANCE, null, 1, null);
        }
    }

    public static final void logD(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "<this>");
        if (Config.INSTANCE.isLogEnabled()) {
            String now$default = Common.now$default(Common.INSTANCE, null, 1, null);
        }
    }

    public static final void logE(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "<this>");
        if (Config.INSTANCE.isLogEnabled()) {
            String now$default = Common.now$default(Common.INSTANCE, null, 1, null);
        }
    }

    public static final void logI(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "<this>");
        if (Config.INSTANCE.isLogEnabled()) {
            String now$default = Common.now$default(Common.INSTANCE, null, 1, null);
            LogUtils.i(now$default + " " + str);
        }
    }

    public static final void logV(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "<this>");
        if (Config.INSTANCE.isLogEnabled()) {
            String now$default = Common.now$default(Common.INSTANCE, null, 1, null);
            LogUtils.v(now$default + " " + str);
        }
    }

    public static final void logW(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "<this>");
        if (Config.INSTANCE.isLogEnabled()) {
            String now$default = Common.now$default(Common.INSTANCE, null, 1, null);
            LogUtils.w(now$default + " " + str);
        }
    }

    @NotNull
    public static final String reversalEvery2Charts(@NotNull String str, boolean z2) {
        List split$default;
        List reversed;
        String joinToString$default;
        Intrinsics.checkNotNullParameter(str, "<this>");
        split$default = split$default((CharSequence) addSpaceEvery2Charts(str), new String[]{" "}, false, 0, 6, (Object) null);
        reversed = CollectionsKt.reversed(split$default);
        joinToString$default = joinToString$defaultC(reversed, z2 ? " " : "", null, null, 0, null, null, 62, null);
        return joinToString$default;
    }

    public static /* synthetic */ String reversalEvery2Charts$default(String str, boolean z2, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            z2 = false;
        }
        return reversalEvery2Charts(str, z2);
    }

    @NotNull
    public static final CharSequence toBackgroundColorSpan(@NotNull CharSequence charSequence, @NotNull IntRange range, int i2) {
        Intrinsics.checkNotNullParameter(charSequence, "<this>");
        Intrinsics.checkNotNullParameter(range, "range");
        SpannableString spannableString = new SpannableString(charSequence);
        spannableString.setSpan(new BackgroundColorSpan(i2), range.getFirst(), range.getLast(), 17);
        return spannableString;
    }

    public static /* synthetic */ CharSequence toBackgroundColorSpan$default(CharSequence charSequence, IntRange intRange, int i2, int i3, Object obj) {
        if ((i3 & 2) != 0) {
            i2 = SupportMenu.CATEGORY_MASK;
        }
        return toBackgroundColorSpan(charSequence, intRange, i2);
    }

    @NotNull
    public static final CharSequence toClickSpan(@NotNull CharSequence charSequence, @NotNull IntRange range, final int i2, final boolean z2, @NotNull final Function0<Unit> clickAction) {
        Intrinsics.checkNotNullParameter(charSequence, "<this>");
        Intrinsics.checkNotNullParameter(range, "range");
        Intrinsics.checkNotNullParameter(clickAction, "clickAction");
        SpannableString spannableString = new SpannableString(charSequence);
        spannableString.setSpan(new ClickableSpan() { // from class: com.zydtech.library.ext.StringExtKt$toClickSpan$1$clickableSpan$1
            @Override // android.text.style.ClickableSpan
            public void onClick(@NotNull View widget) {
                Intrinsics.checkNotNullParameter(widget, "widget");
                clickAction.invoke();
            }

            @Override // android.text.style.ClickableSpan, android.text.style.CharacterStyle
            public void updateDrawState(@NotNull TextPaint ds) {
                Intrinsics.checkNotNullParameter(ds, "ds");
                ds.setColor(i2);
                ds.setUnderlineText(z2);
            }
        }, range.getFirst(), range.getLast(), 17);
        return spannableString;
    }

    public static /* synthetic */ CharSequence toClickSpan$default(CharSequence charSequence, IntRange intRange, int i2, boolean z2, Function0 function0, int i3, Object obj) {
        if ((i3 & 2) != 0) {
            i2 = SupportMenu.CATEGORY_MASK;
        }
        if ((i3 & 4) != 0) {
            z2 = false;
        }
        return toClickSpan(charSequence, intRange, i2, z2, function0);
    }

    @NotNull
    public static final CharSequence toColorSpan(@NotNull CharSequence charSequence, @NotNull IntRange range, int i2) {
        Intrinsics.checkNotNullParameter(charSequence, "<this>");
        Intrinsics.checkNotNullParameter(range, "range");
        SpannableString spannableString = new SpannableString(charSequence);
        spannableString.setSpan(new ForegroundColorSpan(i2), range.getFirst(), range.getLast(), 17);
        return spannableString;
    }

    public static /* synthetic */ CharSequence toColorSpan$default(CharSequence charSequence, IntRange intRange, int i2, int i3, Object obj) {
        if ((i3 & 2) != 0) {
            i2 = SupportMenu.CATEGORY_MASK;
        }
        return toColorSpan(charSequence, intRange, i2);
    }

    @NotNull
    public static final CharSequence toSizeSpan(@NotNull CharSequence charSequence, @NotNull IntRange range, float f2) {
        Intrinsics.checkNotNullParameter(charSequence, "<this>");
        Intrinsics.checkNotNullParameter(range, "range");
        SpannableString spannableString = new SpannableString(charSequence);
        spannableString.setSpan(new RelativeSizeSpan(f2), range.getFirst(), range.getLast(), 17);
        return spannableString;
    }

    public static /* synthetic */ CharSequence toSizeSpan$default(CharSequence charSequence, IntRange intRange, float f2, int i2, Object obj) {
        if ((i2 & 2) != 0) {
            f2 = 1.5f;
        }
        return toSizeSpan(charSequence, intRange, f2);
    }

    @NotNull
    public static final CharSequence toStrikeThroughSpan(@NotNull CharSequence charSequence, @NotNull IntRange range) {
        Intrinsics.checkNotNullParameter(charSequence, "<this>");
        Intrinsics.checkNotNullParameter(range, "range");
        SpannableString spannableString = new SpannableString(charSequence);
        spannableString.setSpan(new StrikethroughSpan(), range.getFirst(), range.getLast(), 17);
        return spannableString;
    }

    @NotNull
    public static final CharSequence toStyleSpan(@NotNull CharSequence charSequence, int i2, @NotNull IntRange range) {
        Intrinsics.checkNotNullParameter(charSequence, "<this>");
        Intrinsics.checkNotNullParameter(range, "range");
        SpannableString spannableString = new SpannableString(charSequence);
        spannableString.setSpan(new StyleSpan(i2), range.getFirst(), range.getLast(), 17);
        return spannableString;
    }

    public static /* synthetic */ CharSequence toStyleSpan$default(CharSequence charSequence, int i2, IntRange intRange, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i2 = 1;
        }
        return toStyleSpan(charSequence, i2, intRange);
    }

    @NotNull
    public static final UUID toUUID(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "<this>");
        int length = str.length();
        if (length != 4) {
            if (length == 36) {
                UUID fromString = UUID.fromString(str);
                Intrinsics.checkNotNullExpressionValue(fromString, "fromString(this)");
                return fromString;
            }
            throw new IllegalArgumentException("The value of \"Length\" is out of range. It must be 4 or 36.");
        }
        Intrinsics.checkNotNullExpressionValue("0000", "this as java.lang.String…ing(startIndex, endIndex)");
        Intrinsics.checkNotNullExpressionValue("-0000-1000-8000-00805f9b34fb", "this as java.lang.String).substring(startIndex)");
        String upperCase = ("0000" + str + "-0000-1000-8000-00805f9b34fb").toUpperCase(Locale.ROOT);
        Intrinsics.checkNotNullExpressionValue(upperCase, "this as java.lang.String).toUpperCase(Locale.ROOT)");
        UUID fromString2 = UUID.fromString(upperCase);
        Intrinsics.checkNotNullExpressionValue(fromString2, "fromString((\"0000([0-9a-…bstring(38)).uppercase())");
        return fromString2;
    }

    @NotNull
    public static final String formatString(@NotNull String str, @NotNull String format, @NotNull String second) {
        Intrinsics.checkNotNullParameter(str, "<this>");
        Intrinsics.checkNotNullParameter(format, "format");
        Intrinsics.checkNotNullParameter(second, "second");
        String format2 = String.format(Locale.getDefault(), format, Arrays.copyOf(new Object[]{str, second}, 2));
        Intrinsics.checkNotNullExpressionValue(format2, "format(locale, format, *args)");
        return format2;
    }

    public static final void logA(@NotNull CharSequence charSequence) {
        Intrinsics.checkNotNullParameter(charSequence, "<this>");
        if (Config.INSTANCE.isLogEnabled()) {
            String now$default = Common.now$default(Common.INSTANCE, null, 1, null);
            LogUtils.a(now$default + " " + ((Object) charSequence));
        }
    }

    public static final void logD(@NotNull CharSequence charSequence) {
        Intrinsics.checkNotNullParameter(charSequence, "<this>");
        if (Config.INSTANCE.isLogEnabled()) {
            String now$default = Common.now$default(Common.INSTANCE, null, 1, null);
        }
    }

    public static final void logE(@NotNull CharSequence charSequence) {
        Intrinsics.checkNotNullParameter(charSequence, "<this>");
        if (Config.INSTANCE.isLogEnabled()) {
            String now$default = Common.now$default(Common.INSTANCE, null, 1, null);
        }
    }

    public static final void logI(@NotNull CharSequence charSequence) {
        Intrinsics.checkNotNullParameter(charSequence, "<this>");
        if (Config.INSTANCE.isLogEnabled()) {
            String now$default = Common.now$default(Common.INSTANCE, null, 1, null);
        }
    }

//    public static final void logV(@NotNull CharSequence charSequence) {
//        Intrinsics.checkNotNullParameter(charSequence, "<this>");
//        if (Config.INSTANCE.isLogEnabled()) {
//            String now$default = Common.now(Common.INSTANCE, null, 1, null);
//
//        }
//    }

    public static final void logW(@NotNull CharSequence charSequence) {
        Intrinsics.checkNotNullParameter(charSequence, "<this>");
        if (Config.INSTANCE.isLogEnabled()) {
            String now$default = Common.now$default(Common.INSTANCE, null, 1, null);
        }
    }

}
