SUBMENU - Android projekt

Co aplikacia robi:
- Zobrazi 5 pozicii pre aplikacie.
- Predvolene hlada Chrome, CG Transit, Gmail, Hodiny a Kalendar.
- Tlacidlo UPRAVIT pri kazdej pozicii otvori zoznam aplikacii nainstalovanych v telefone.
- Vyber sa uklada do telefonu.
- Po spusteni vybranej aplikacie zavrie obrazovku Submenu cez finish().
- Nespusta sa automaticky po starte telefonu.

Dolezite:
- CG Transit, Hodiny a Kalendar mozu mat v telefone odlisny nazov alebo balicek.
  Ak sa automaticky nenajdu, pri prvej instalacii ich vyber cez UPRAVIT.
- Android spravuje procesy a RAM sam. finish() zatvori aktivitu Submenu, ale Android
  rozhoduje o okamzitom odstraneni procesu z pamate.
- QUERY_ALL_PACKAGES je potrebne na zobrazenie zoznamu aplikacii pri instalacii APK
  mimo Google Play. Pri publikovani na Google Play plati osobitne obmedzenie tejto permission.

Ako vytvorit APK:
1. Nainstaluj Android Studio na pocitac.
2. Rozbal ZIP.
3. V Android Studio zvol Open a vyber priecinok SubmenuAndroid.
4. Nechaj dokoncit Gradle Sync.
5. Zvol Build > Build Bundle(s) / APK(s) > Build APK(s).
6. APK najdes v app/build/outputs/apk/debug/app-debug.apk.
7. Prenes APK do Samsung Galaxy A52 a nainstaluj ho. Ak sa zobrazi upozornenie,
   povol instalaciu z daneho zdroja.

Minimalna verzia Androidu: 6.0. Cielova verzia: Android 15 (API 35).
