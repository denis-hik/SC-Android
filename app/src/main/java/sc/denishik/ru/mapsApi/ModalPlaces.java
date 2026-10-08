package sc.denishik.ru.mapsApi;

import android.app.Activity;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import sc.denishik.ru.yandexApi.YandexMapController;

/** Saved destinations are committed only after the user chooses a search result. */
public final class ModalPlaces {
    private final Activity activity;
    private final PlacesStore store;
    private final YandexMapController map;
    private final Runnable changed;
    private AlertDialog dialog;
    public ModalPlaces(Activity activity, PlacesStore store, YandexMapController map, Runnable changed) {
        this.activity = activity; this.store = store; this.map = map; this.changed = changed;
    }
    public void show() {
        String[] slots = {"home", "garden"};
        String[] labels = new String[2];
        for (int i = 0; i < slots.length; i++) {
            SavedPlace place = store.get(slots[i]);
            labels[i] = place == null ? defaultTitle(slots[i]) + "\nДобавить адрес" : place.title + "\n" + place.address;
        }
        dialog = new AlertDialog.Builder(activity).setTitle("Мои места")
                .setItems(labels, (d, which) -> edit(slots[which], null)).setNegativeButton("Закрыть", null).show();
    }
    public static String defaultTitle(String slot) { return "home".equals(slot) ? "Дом" : "Дача"; }
    public void edit(String slot, Runnable saved) {
        SavedPlace current = store.get(slot);
        LinearLayout content = new LinearLayout(activity); content.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (24 * activity.getResources().getDisplayMetrics().density);
        content.setPadding(pad, pad / 2, pad, pad / 2);
        EditText title = new EditText(activity); title.setHint("Название места"); title.setSingleLine(true);
        title.setText(current == null ? defaultTitle(slot) : current.title);
        EditText address = new EditText(activity); address.setHint("Город, улица, дом");
        address.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
        if (current != null) address.setText(current.address);
        TextView status = new TextView(activity); status.setText("Найдите адрес и выберите точку из результатов.");
        status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        LinearLayout results = new LinearLayout(activity); results.setOrientation(LinearLayout.VERTICAL);
        content.addView(title); content.addView(address); content.addView(status); content.addView(results);
        ScrollView scroll = new ScrollView(activity); scroll.addView(content);
        AlertDialog edit = new AlertDialog.Builder(activity).setTitle("Изменить место").setView(scroll)
                .setPositiveButton(current == null ? "Найти адрес" : "Сохранить", null).setNegativeButton("Отмена", null)
                .setNeutralButton(current == null ? null : "Удалить", (d, w) -> { store.remove(slot); changed.run(); }).create();
        dialog = edit;
        edit.setOnDismissListener(d -> map.cancelSearch());
        edit.setOnShowListener(d -> edit.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String query = address.getText().toString().trim();
            if (title.getText().toString().trim().isEmpty()) { title.setError("Введите название"); return; }
            if (current != null && current.address.equals(query)) {
                store.save(slot, new SavedPlace(title.getText().toString().trim(), current.address, current.latitude, current.longitude));
                edit.dismiss(); changed.run(); if (saved != null) saved.run();
                return;
            }
            if (query.length() < 3) { address.setError("Введите полный адрес"); return; }
            results.removeAllViews(); status.setText("Ищем адрес…");
            edit.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            map.search(query, (places, error) -> {
                if (!edit.isShowing()) return;
                edit.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                status.setText(error == null ? "Нажмите на нужный адрес, чтобы сохранить:" : error);
                for (SavedPlace place : places) {
                    Button choice = new Button(activity); choice.setAllCaps(false); choice.setText(place.address);
                    choice.setMinHeight((int) (56 * activity.getResources().getDisplayMetrics().density));
                    choice.setOnClickListener(button -> {
                        String name = title.getText().toString().trim();
                        if (name.isEmpty()) { title.setError("Введите название"); return; }
                        store.save(slot, new SavedPlace(name, place.address, place.latitude, place.longitude));
                        edit.dismiss(); changed.run(); if (saved != null) saved.run();
                    });
                    results.addView(choice);
                }
            });
        }));
        edit.show();
    }
    public interface RouteSelection { void select(SavedPlace place); }

    /** A one-off destination; never writes to PlacesStore. */
    public void searchRoute(RouteSelection selected) {
        dismiss();
        LinearLayout content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (24 * activity.getResources().getDisplayMetrics().density);
        content.setPadding(pad, pad / 2, pad, pad / 2);
        EditText address = new EditText(activity);
        address.setHint("Город, улица, дом или название места");
        address.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS);
        address.setSingleLine(true);
        address.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH);
        TextView status = new TextView(activity);
        status.setText("Выберите адрес для маршрута. Он не будет сохранён в «Мои места».");
        status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        LinearLayout results = new LinearLayout(activity);
        results.setOrientation(LinearLayout.VERTICAL);
        content.addView(address); content.addView(status); content.addView(results);
        ScrollView scroll = new ScrollView(activity); scroll.addView(content);
        AlertDialog search = new AlertDialog.Builder(activity).setTitle("Куда поедем?").setView(scroll)
                .setPositiveButton("Найти", null).setNegativeButton("Отмена", null).create();
        dialog = search;
        search.setOnDismissListener(d -> map.cancelSearch());
        Runnable find = () -> {
            if (!search.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled()) return;
            String query = address.getText().toString().trim();
            if (query.length() < 3) { address.setError("Введите адрес или название места"); return; }
            results.removeAllViews(); status.setText("Ищем адрес…");
            search.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            map.search(query, (found, error) -> {
                if (!search.isShowing()) return;
                search.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                status.setText(error != null ? error : found.isEmpty() ? "Ничего не найдено. Уточните адрес."
                        : "Выберите адрес, чтобы построить маршрут:");
                for (SavedPlace place : found) {
                    Button choice = new Button(activity);
                    choice.setAllCaps(false); choice.setText(place.address);
                    choice.setMinHeight((int) (56 * activity.getResources().getDisplayMetrics().density));
                    choice.setOnClickListener(v -> {
                        android.view.inputmethod.InputMethodManager keyboard = (android.view.inputmethod.InputMethodManager)
                                activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                        if (keyboard != null) keyboard.hideSoftInputFromWindow(address.getWindowToken(), 0);
                        search.dismiss(); selected.select(place);
                    });
                    results.addView(choice);
                }
            });
        };
        search.setOnShowListener(d -> search.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> find.run()));
        address.setOnEditorActionListener((v, action, event) -> {
            if (action != android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) return false;
            find.run(); return true;
        });
        search.show();
        address.requestFocus();
        if (search.getWindow() != null) search.getWindow().setSoftInputMode(
                android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
                        | android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }
    public void dismiss() { if (dialog != null) dialog.dismiss(); }
}
