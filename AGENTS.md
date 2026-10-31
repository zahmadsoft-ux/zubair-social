# AGENTS.md — Zubair Social

Android app (Kotlin + Jetpack Compose). Package: `com.zubair.social`, launcher activity: `.MainActivity`.

## Build + Install (jab user kahe "build install karo" / "update karo")

Hamesha **ADB over WiFi** se connected device par build karke install/update karna hai.

1. Device check karo:
   ```bash
   ~/Library/Android/sdk/platform-tools/adb devices -l
   ```
   - Agar device `device` state mein hai (wifi/`_adb-tls-connect._tcp`), to aage barho.
   - Agar list khali hai ya `offline`/`unauthorized` hai, to user ko batao ke phone par Wireless debugging on kare / pair kare (`adb pair <ip:port>` phir `adb connect <ip:port>`). Khud pairing code ya IP guess mat karo.
2. Build + install (existing app update ho jati hai, data nahi mitta):
   ```bash
   ./gradlew installDebug
   ```
3. App launch karo:
   ```bash
   ~/Library/Android/sdk/platform-tools/adb shell am start -n com.zubair.social/.MainActivity
   ```
4. Agar install fail ho, to pura error dikhao (`./gradlew installDebug` dobara chala kar) — sirf "failed" mat bolo. Ek se zyada devices hon to `ANDROID_SERIAL=<serial>` set karo.

## Rules
- Sirf debug build install karni hai jab tak user release na kahe.
- `adb uninstall` ya `adb shell pm clear` kabhi mat chalao bina user ki ijazat ke (app data delete hota hai).
- `local.properties` machine-specific hai (SDK path), commit nahi hota.
- Build verify karne ke liye: `./gradlew assembleDebug`.

## Compose Previews (zaroori)
Har naya composable (component ya screen) ke saath **usi file mein** `@Preview` hona chahiye. Bina preview ke kaam complete nahi.
- Preview `private fun XyzPreview()` ho, `@Preview(showBackground = true)` + `@Composable`, aur `ZubairSocialTheme { ... }` mein wrapped.
- Component: har important state ka preview — default, disabled, error, loading (jo lagu ho). Ek se zyada states ho to alag `@Preview` functions, ya ek preview mein Column.
- Screen: stateful `XyzScreen` (ViewModel/Firebase wala) ko preview nahi kar sakte, isliye UI ko stateless `XyzContent(state, callbacks)` mein alag rakho aur uska preview banao. Kam az kam default aur error/loading state.
- Dark mode check ke liye naye screens/components par `@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark")` bhi lagao.
- Preview mein Firebase, ViewModel ya network use mat karo; sample data hardcode karo (UI text English mein).
- Naya component/screen add karte waqt purani files bhi dekho: agar kisi mein preview missing ho to saath mein add karo.
- Verify: `./gradlew assembleDebug` pass hona chahiye.

## Language
- **App ka UI text (labels, buttons, error messages, placeholders) hamesha English mein.** Roman Urdu sirf docs/comments/chat mein.
- Docs ki error tables Roman Urdu mein hain, lekin code mein unka English version likho.
