# SC-Android

 Scooter App for midway i pro

-remote light and lock 
-max speed change on 35
-speed get

-led remote [SC-Led-Ardruino](https://github.com/denis-hik/SC-Led-Ardruino)

![plot](/screens/photo0.jpg)
![plot](/screens/photo1.jpg)
![plot](/screens/photo2.jpg)


## Kugoo F3 Pro Max: экспериментальное офлайн-подключение

Экран OfflineKeysActivity теперь выполняет root-импорт из установленного com.kugoo.cn,
сохраняет выбранную привязку в зашифрованном файле вне Android Backup и открывает
отдельную BLE-сессию через ThingHomeSdk. Сессия закрывается при уничтожении экрана.
При потере связи выполняются до трёх попыток восстановления. DP отображаются как
JSON без неподтверждённого сопоставления номеров со скоростью, зарядом и т. п.

1. В Kugoo Riding откройте ранее привязанный самокат, чтобы обновить локальный кеш.
2. Закройте Kugoo Riding и отключите другие BLE-подключения к самокату.
3. На экране офлайн-ключей нажмите «Получить данные Kugoo через root» и разрешите su.
4. Выберите самокат, затем «Подключить / восстановить». Предоставьте разрешения Bluetooth;
   на Android до 12 также нужна включённая геопозиция.
5. Если формат кеша не поддерживается, можно импортировать JSON DeviceRespBean либо
   объект с массивами устройств и продуктов. Обязательны devId, uuid, productId, localKey;
   сохраняйте secKey, communication, deviceTopo, dps и соответствующий ProductBean, если они есть.

Root-чтение использует отдельный app_process, UID владельца Kugoo и существующий
AndroidKeyStore AES/GCM-ключ. Оно не создаёт и не удаляет ключи официального приложения.
Поддерживаются его global_preference.xml и копии MMKV в files/thingmmkv (резервный путь files/mmkv), включая
preferences_global_key*, common_storage и ble_business_data. Форматы других версий,
compat-шифрование и дисковые/SQL-кеши этим импортёром не поддерживаются. SELinux либо
KeyStore конкретной прошивки могут запретить доступ даже при наличии root.

В other/archive-2026-10-08/KugoExportData подтверждены: SecuredStore.securityUserKey, MMKVManager,
CMHelper (bdbqpbd: type#address#flag), DeviceRespBean.localKey и запрос всех DP через
queryDps(devId, null). SDK API: https://developer.tuya.com/en/docs/app-development/android-bluetooth-ble?id=Karv7r2ju4c21

Окно Connect показывает сохранённые Kugoo. Выбор запускает KugooConnection в ServiceScooter:
сначала прямое BLE-подключение по сохранённому адресу, затем поиск и до трёх попыток восстановления.
Повторный root-импорт при обычном подключении не нужен. Все импортированные профили сохраняются
зашифрованно; старый формат с одним профилем также поддерживается. OfflineKeysActivity использует
тот же механизм соединения. В Info приходят текущие DP с кодами и масштабом из схемы продукта;
скорость передаётся основному экрану. В Remote доступно переключение фонаря по headlight_switch из схемы; остальные команды управления для этого пути отключены.
Успешная сборка и unit-тесты не подтверждают работу root/KeyStore и BLE на F3 Pro Max:
аппаратная проверка описана ниже.

Проверка: gradlew.bat :app:testDebugUnitTest :app:assembleDebug

### Проверено на телефоне

На Samsung SM-A065F (Android 14, Magisk) проверены root-импорт, выбор KUGOO F3PROMAX 2,
зашифрованное сохранение и загрузка профиля после перезапуска, BLE-подключение и получение DP.
После выключения Bluetooth на четыре секунды и повторного включения связь восстановилась
автоматически, затем пришли новые данные. Значения DP пока показываются без интерпретации единиц.

Для Magisk используется su --mount-master: каталог другого приложения скрыт в mount namespace
SC-Android. В app_process явно устанавливается AndroidKeyStoreProvider. Для MMKV с отдельным
rootPath SDK сначала создаёт собственную пару файлов; импорт заменяет только эту временную пару,
учитывая хеширование имён в версии SDK 5.17.2. Оригинальные хранилища Kugoo не открываются на запись.

Проверено подключение из основного окна Connect к KUGOO F3PROMAX 2: переход в HomeActivity,
данные в Info (speed, battery_percentage, level, ridetime_once и другие). После выключения
Bluetooth на четыре секунды сервис восстановил связь и продолжил получать обновления.
Проверки импорта и разбора схемы: 9 unit-тестов, сборка debug успешна.
