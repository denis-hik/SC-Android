package sc.denishik.ru.midwayApi.help;

import java.util.List;

import kotlin.collections.ArraysKt;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.functions.Function1;
import kotlin.text.StringsKt;

public class other {
    public static String joinToString$default(int[] bArr, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, int i2, CharSequence charSequence4, Function1 function1, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            charSequence = ", ";
        }
        String str = (i3 & 2) != 0 ? "" : (String) charSequence2;
        String str2 = (i3 & 4) == 0 ? (String) charSequence3 : "";
        if ((i3 & 8) != 0) {
            i2 = -1;
        }
        int i4 = i2;
        if ((i3 & 16) != 0) {
            charSequence4 = "...";
        }
        CharSequence charSequence5 = charSequence4;
        if ((i3 & 32) != 0) {
            function1 = null;
        }
        return ArraysKt.joinToString(bArr, charSequence, str, str2, i4, charSequence5, (Function1) function1);
    }
    public static String joinToString$default(byte[] bArr, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, int i2, CharSequence charSequence4, Function1 function1, int i3) {
        if ((i3 & 1) != 0) {
            charSequence = ", ";
        }
        String str = (i3 & 2) != 0 ? "" : (String) charSequence2;
        String str2 = (i3 & 4) == 0 ? (String) charSequence3 : "";
        if ((i3 & 8) != 0) {
            i2 = -1;
        }
        int i4 = i2;
        if ((i3 & 16) != 0) {
            charSequence4 = "...";
        }
        CharSequence charSequence5 = charSequence4;
        if ((i3 & 32) != 0) {
            function1 = null;
        }
        return ArraysKt.joinToString(bArr, charSequence, str, str2, i4, charSequence5, (Function1) function1);
    }

    public static /* synthetic */ List split$default(CharSequence charSequence, String[] strArr, boolean z2, int i2, int i3, Object obj) {
        if ((i3 & 2) != 0) {
            z2 = false;
        }
        if ((i3 & 4) != 0) {
            i2 = 0;
        }
        return StringsKt.split(charSequence, strArr, z2, i2);
    }

    public static /* synthetic */ String joinToString$defaultC(Iterable iterable, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, int i2, CharSequence charSequence4, Function1 function1, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            charSequence = ", ";
        }
        String str = (i3 & 2) != 0 ? "" : (String) charSequence2;
        String str2 = (i3 & 4) == 0 ? (String) charSequence3 : "";
        if ((i3 & 8) != 0) {
            i2 = -1;
        }
        int i4 = i2;
        if ((i3 & 16) != 0) {
            charSequence4 = "...";
        }
        CharSequence charSequence5 = charSequence4;
        if ((i3 & 32) != 0) {
            function1 = null;
        }
        return CollectionsKt.joinToString(iterable, charSequence, str, str2, i4, charSequence5, function1);
    }

    public static /* synthetic */ List split$default(CharSequence charSequence, char[] cArr, boolean z2, int i2, int i3, Object obj) {
        if ((i3 & 2) != 0) {
            z2 = false;
        }
        if ((i3 & 4) != 0) {
            i2 = 0;
        }
        return StringsKt.split(charSequence, cArr, z2, i2);
    }



    public static /* synthetic */ String replace$default(String str, String str2, String str3, boolean z2, int i2, Object obj) {
        String replace;
        if ((i2 & 4) != 0) {
            z2 = false;
        }
        replace = StringsKt.replace(str, str2, str3, z2);
        return replace;
    }





}
