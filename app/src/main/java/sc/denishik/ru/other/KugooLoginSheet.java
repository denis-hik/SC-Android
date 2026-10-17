package sc.denishik.ru.other;

import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.json.*;
import java.util.*;
import sc.denishik.ru.R;
import sc.denishik.ru.kuggoApi.Login;
import sc.denishik.ru.kuggoApi.KugooOemCrypto;
import sc.denishik.ru.tuya.KugooProfileStore;

/** Credential entry stays in the dialog; only non-secret workflow state survives rotation. */
public class KugooLoginSheet extends BottomSheetDialogFragment {
    public interface Host { void onKugooLoginProfiles(List<JSONObject> profiles); }
    private State state;
    private LinearLayout content;
    private TextView status;
    private int rendered = -1;
    private TextInputEditText email, password;
    private final List<String> countryCodes = new ArrayList<>();
    private final List<String> countryLabels = new ArrayList<>();

    public static class State extends AndroidViewModel {
        final MutableLiveData<Integer> changes = new MutableLiveData<>();
        int step = 1;
        int message = R.string.ka_prepare;
        String error;
        List<JSONObject> profiles;
        boolean delivered;
        public State(Application app) { super(app); changes.setValue(0); }
        void notifyState() { changes.setValue(changes.getValue() + 1); }
        void start(String country, String email, String password) {
            if (step == 3) return;
            step = 3; error = null; message = R.string.ka_prepare; notifyState();
            Login.loginDetailed(getApplication(), country, email, password, stage -> {
                message = "TOKEN".equals(stage) ? R.string.ka_token : "LOGIN".equals(stage)
                        ? R.string.ka_login : "HOMES".equals(stage) ? R.string.ka_homes : R.string.ka_keys;
                notifyState();
            }, (result, code) -> {
                if (result == null || result.isEmpty()) {
                    error = result == null ? code : "NO_DEVICES"; step = 4; notifyState(); return;
                }
                profiles = result;
                if (result.size() != 1) { step = 4; notifyState(); return; }
                message = R.string.ka_saving; notifyState();
                new Thread(() -> {
                    String failure = null;
                    try { KugooProfileStore.saveAll(getApplication(), result, result.get(0).getString("devId")); }
                    catch (Exception ignored) { failure = "SAVE_ERROR"; }
                    final String saveError = failure;
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        error = saveError; step = 4; notifyState();
                    });
                }, "kugoo-save-login").start();
            });
        }
    }

    @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
        state = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(requireActivity().getApplication())).get(State.class);
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return dialog;
    }

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        ScrollView scroll = new ScrollView(requireContext());
        scroll.setFillViewport(true);
        content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(24), dp(20), dp(24), dp(28));
        scroll.setBackgroundResource(R.drawable.bg_connect_sheet);
        scroll.setClipToOutline(true);
        scroll.addView(content);
        return scroll;
    }

    @Override public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        state.changes.observe(getViewLifecycleOwner(), ignored -> render());
    }

    @Override public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) requireDialog();
        View sheet = dialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (sheet != null) sheet.setBackgroundResource(android.R.color.transparent);
        dialog.getBehavior().setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
        if (state.step == 1) { rendered = -1; render(); }
    }

    private void render() {
        if (content == null) return;
        if (rendered == state.step) {
            if (state.step == 3 && status != null) status.setText(state.message);
            return;
        }
        rendered = state.step;
        content.removeAllViews();
        email = null; password = null;
        text(getString(R.string.ka_steps, state.step), 13);
        text(getString(R.string.kugoo_auth_title), 24);
        content.setAlpha(0.4f); content.animate().alpha(1f).setDuration(180).start();
        if (state.step == 1) intro();
        else if (state.step == 2) form();
        else if (state.step == 3) {
            ProgressBar spinner = new ProgressBar(requireContext());
            LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(dp(88), dp(88));
            size.gravity = Gravity.CENTER; size.topMargin = dp(40); size.bottomMargin = dp(24);
            content.addView(spinner, size);
            status = text(getString(state.message), 18); status.setGravity(Gravity.CENTER);
            status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        } else result();
        setCancelable(state.step != 3);
        ((BottomSheetDialog) requireDialog()).getBehavior().setDraggable(state.step != 3);
    }

    private void intro() {
        text(getString(R.string.ka_intro), 16);
        boolean app = false;
        try { new KugooOemCrypto(requireContext()); app = true; } catch (Exception | LinkageError ignored) { }
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities net = cm == null ? null : cm.getNetworkCapabilities(cm.getActiveNetwork());
        boolean online = net != null && net.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        text(getString(R.string.ka_requirements, app ? "✓" : "✕", online ? "✓" : "✕"), 16);
        if (!app || !online) button(R.string.ka_check, () -> { rendered = -1; render(); });
        FloatingActionButton next = actionFab(R.string.ka_next,
                () -> { state.step = 2; state.notifyState(); });
        next.setEnabled(app && online);
        next.setAlpha(app && online ? 1f : 0.35f);

    }

    private void form() {
        text(getString(R.string.ka_region), 14);
        Spinner region = new Spinner(requireContext());
        countryLabels.clear(); countryCodes.clear();
        try (java.io.InputStream in = requireContext().getAssets().open("kugoo_countries.json")) {
            java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[4096]; int n;
            while ((n = in.read(buffer)) != -1) bytes.write(buffer, 0, n);
            JSONArray countries = new JSONArray(bytes.toString("UTF-8"));
            List<JSONObject> sorted = new ArrayList<>();
            for (int i = 0; i < countries.length(); i++) sorted.add(countries.getJSONObject(i));
            java.text.Collator collator = java.text.Collator.getInstance();
            sorted.sort((a, b) -> collator.compare(countryName(a), countryName(b)));
            int selected = 0;
            for (JSONObject country : sorted) {
                if (country.optString("iso").equals("RU")) selected = countryCodes.size();
                countryLabels.add(countryName(country) + " (+" + country.getString("code") + ")");
                countryCodes.add(country.getString("code"));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, countryLabels);
            region.setAdapter(adapter); region.setSelection(selected);
        } catch (Exception ignored) { text(getString(R.string.ka_app), 16); }
        content.addView(region, new LinearLayout.LayoutParams(-1, dp(56)));
        email = field(R.string.ka_email, false); password = field(R.string.ka_password, true);
        TextView validation = text("", 14);
        FloatingActionButton signIn = actionFab(R.string.ka_signin, () -> {
            String login = email.getText().toString().trim(), secret = password.getText().toString();
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(login).matches() || secret.length() <= 4 || countryCodes.isEmpty()) {
                validation.setText(R.string.ka_invalid); return;
            }
            ((android.view.inputmethod.InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(password.getWindowToken(), 0);
            password.setText("");
            state.start(countryCodes.get(region.getSelectedItemPosition()), login, secret);
        });
        final TextInputEditText emailInput = email, passwordInput = password;
        Runnable updateSignIn = () -> {
            boolean valid = android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput.getText().toString().trim()).matches()
                    && passwordInput.getText().length() > 4 && !countryCodes.isEmpty();
            signIn.setEnabled(valid);
            signIn.setAlpha(valid ? 1f : 0.35f);
        };
        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(android.text.Editable text) {
                validation.setText("");
                updateSignIn.run();
            }
        };
        emailInput.addTextChangedListener(watcher);
        passwordInput.addTextChangedListener(watcher);
        updateSignIn.run();
    }

    private String countryName(JSONObject country) {
        return new Locale("", country.optString("iso")).getDisplayCountry(Locale.getDefault());
    }

    private void result() {
        boolean success = state.error == null;
        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(success ? R.drawable.ic_auth_success : R.drawable.ic_auth_error);
        LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(dp(96), dp(96));
        size.gravity = Gravity.CENTER; size.topMargin = dp(24);
        content.addView(icon, size);
        text(getString(success ? R.string.ka_success : R.string.ka_failure), 22).setGravity(Gravity.CENTER);
        if (success) {
            text(getString(R.string.ka_saved), 16);
            button(R.string.ka_close, this::dismiss);
            if (!state.delivered && getActivity() instanceof Host) {
                state.delivered = true;
                if (state.profiles.size() > 1) dismiss();
                ((Host) requireActivity()).onKugooLoginProfiles(state.profiles);
            }
        } else {
            text(errorMessage(state.error), 16);
            button(R.string.ka_retry, () -> { state.step = 1; state.notifyState(); });
            button(R.string.ka_close, this::dismiss);
        }
    }

    private String errorMessage(String code) {
        if ("NO_DEVICES".equals(code)) return getString(R.string.ka_empty);
        if ("NETWORK_ERROR".equals(code)) return getString(R.string.ka_network);
        if (code != null && code.startsWith("KUGOO_")) return getString(R.string.ka_app);
        if ("SAVE_ERROR".equals(code)) return getString(R.string.ka_save_error);
        if (code != null && (code.contains("PASSWORD") || code.contains("USER_NOT") || code.contains("ACCOUNT")))
            return getString(R.string.ka_credentials);
        return getString(R.string.ka_other_error, code == null ? "CLIENT_ERROR" : code);
    }

    private TextInputEditText field(int hint, boolean secret) {
        TextInputLayout box = new TextInputLayout(requireContext());
        box.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE); box.setHint(getString(hint));
        TextInputEditText input = new TextInputEditText(box.getContext());
        input.setTextColor(requireContext().getColor(R.color.app_text));
        input.setSingleLine(); input.setSaveEnabled(false);
        input.setInputType(secret ? 129 : 33);
        if (android.os.Build.VERSION.SDK_INT >= 26) input.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        box.addView(input, new LinearLayout.LayoutParams(-1, -2));
        if (secret) box.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        content.addView(box, new LinearLayout.LayoutParams(-1, -2));
        return input;
    }
    private TextView text(String value, int sp) {
        TextView view = new TextView(requireContext()); view.setText(value); view.setTextSize(sp);
        view.setTextColor(requireContext().getColor(R.color.app_text));
        view.setPadding(0, dp(8), 0, dp(8)); content.addView(view, new LinearLayout.LayoutParams(-1, -2)); return view;
    }
    private FloatingActionButton actionFab(int title, Runnable action) {
        FloatingActionButton button = new FloatingActionButton(requireContext());
        button.setImageResource(R.drawable.ic_auth_next);
        button.setContentDescription(getString(title));
        androidx.appcompat.widget.TooltipCompat.setTooltipText(button, getString(title));
        LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(dp(56), dp(56));
        size.gravity = Gravity.CENTER; size.topMargin = dp(24);
        content.addView(button, size);
        button.setOnClickListener(v -> action.run());
        return button;
    }
    private void button(int title, Runnable action) {
        MaterialButton button = new MaterialButton(requireContext()); button.setText(title);
        content.addView(button, new LinearLayout.LayoutParams(-1, -2)); button.setOnClickListener(v -> action.run());
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    @Override public void onDestroyView() {
        if (password != null) password.setText("");
        content = null; status = null; email = null; password = null; rendered = -1;
        super.onDestroyView();
    }
}
