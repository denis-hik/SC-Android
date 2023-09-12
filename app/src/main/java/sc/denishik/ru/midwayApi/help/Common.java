package sc.denishik.ru.midwayApi.help;

import org.jetbrains.annotations.NotNull;

import com.blankj.utilcode.util.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.regex.Pattern;

import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Regex;

public class Common {
    @NotNull
    public static final Common INSTANCE = new Common();

    private Common() {
    }

    private final boolean isChinese(char c2) {
        Character.UnicodeBlock of = Character.UnicodeBlock.of(c2);
        return of == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || of == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS || of == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || of == Character.UnicodeBlock.GENERAL_PUNCTUATION || of == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || of == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }

    public static /* synthetic */ String now$default(Common common, String str, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            str = "yyyy-MM-dd HH:mm:ss:SSS";
        }
        return common.now(str);
    }

    @NotNull
    public final List<Stack<Integer>> distinct(@NotNull int[] arr) {
        Intrinsics.checkNotNullParameter(arr, "arr");
        ArrayList arrayList = new ArrayList();
        Stack stack = new Stack();
        arrayList.add(stack);
        for (int i2 : arr) {
            if (stack.isEmpty()) {
                stack.push(Integer.valueOf(i2));
            } else {
                int i3 = i2 - 1;
                Integer num = (Integer) ((Stack) arrayList.get(arrayList.size() - 1)).peek();
                if (num == null || i3 != num.intValue()) {
                    int i4 = i2 - 2;
                    Integer num2 = (Integer) ((Stack) arrayList.get(arrayList.size() - 1)).peek();
                    if (num2 == null || i4 != num2.intValue()) {
                        stack = new Stack();
                        stack.push(Integer.valueOf(i2));
                        arrayList.add(stack);
                    }
                }
                ((Stack) arrayList.get(arrayList.size() - 1)).push(Integer.valueOf(i2));
            }
        }
        return arrayList;
    }

    public final boolean isMessyCode(@NotNull String strName) {
        String after = Pattern.compile("\\s*|t*|r*|n*").matcher(strName).replaceAll("");
        Intrinsics.checkNotNullExpressionValue(after, "after");
        String replace = new Regex("\\p{P}").replace(after, "");
        int length = replace.length() - 1;
        int i2 = 0;
        boolean z2 = false;
        while (i2 <= length) {
            boolean z3 = Intrinsics.compare((int) replace.charAt(!z2 ? i2 : length), 32) <= 0;
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
        char[] charArray = replace.subSequence(i2, length + 1).toString().toCharArray();
        float length2 = charArray.length;
        float f2 = 0.0f;
        for (char c2 : charArray) {
            if (!Character.isLetterOrDigit(c2) && !isChinese(c2)) {
                f2++;
            }
        }
        return ((double) (f2 / length2)) > 0.4d;
    }

    @NotNull
    public final String now(@NotNull String pattern) {
        String nowString = TimeUtils.getNowString(new SimpleDateFormat(pattern, Locale.getDefault()));
        return nowString;
    }

}
