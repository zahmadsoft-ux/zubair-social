# Zubair Social — Daily Build Guide

Yeh file roz ka plan hai: **ek din, ek feature**. Har din ke liye neeche ek ready-made **prompt** diya gaya hai. Bas us din ka prompt copy karo aur Claude Code mein paste kar do.

- Poora architecture aur data model: [PROJECT_PLAN.md](PROJECT_PLAN.md)
- Har feature ki detail aur flow diagrams: [docs/features/](docs/features/README.md)

---

## Roz ka session kaise chalega

1. **Goal**: aaj kya banana hai aur kyun isi order mein
2. **Concept pehle**: code se pehle samjhana ke andar kya hota hai
3. **Chhote steps**: har step ke baad app run karke dekhna
4. **Khud karna**: Android Studio aur Firebase console wale kaam tum khud karoge, Claude guide karega
5. **Recap**: din ke end pe kya seekha, aur neeche checklist mein tick lagao

> **Rule:** Agla din tab hi shuru karo jab pichle din ka "Done jab" wala hissa poora ho chuka ho. Har feature pichle feature pe khara hai (dekho PROJECT_PLAN.md section 6).

---

## Progress Checklist

- [ ] Day 1: Project setup + Firebase connect
- [ ] Day 2: Design tokens (Theme.kt)
- [ ] Day 3: Core components + Preview gallery
- [ ] Day 4: Navigation + BottomNavBar
- [ ] Day 5: Sign up
- [ ] Day 6: Login + auto-login + logout
- [ ] Day 7: Profile (read-only)
- [ ] Day 8: Edit Profile
- [ ] Day 9: Cloudflare Worker + R2 (image upload backend)
- [ ] Day 10: Create Post screen
- [ ] Day 11: Home Feed
- [ ] Day 12: Like button
- [ ] Day 13: Search
- [ ] Day 14: Follow / Unfollow
- [ ] Day 15: Notifications
- [ ] Day 16: Cleanup, Security Rules, aur review

---

## Har prompt ke sath kya jata hai

Har din ke prompt ke shuru mein yeh common hissa laga hua hai. Isay alag se paste karne ki zarurat nahi:

> Hum Zubair Social bana rahe hain (README.md aur PROJECT_PLAN.md dekho). Main seekh raha hoon, is liye har step pe samjhao ke kya ho raha hai aur kyun. Kaam chhote steps mein karo, har step ke baad mujhe run karke check karne do. Jo kaam main khud kar sakta hoon (Android Studio, Firebase console) wo mujh se karwao. End mein recap do aur README.md ki checklist update karo.

---

## Day 1: Project setup + Firebase connect

**Seekhoge:** Android project ka structure, Gradle kya karta hai, app Firebase se kaise judti hai.

```text
Hum Zubair Social bana rahe hain (README.md aur PROJECT_PLAN.md dekho). Main seekh raha hoon, is liye har step pe samjhao ke kya ho raha hai aur kyun. Kaam chhote steps mein karo, har step ke baad mujhe run karke check karne do. Jo kaam main khud kar sakta hoon (Android Studio, Firebase console) wo mujh se karwao. End mein recap do aur README.md ki checklist update karo.

Aaj Day 1 hai: Project setup + Firebase connect.
1. Mujhe Android Studio mein "Empty Activity" (Compose) project banwao, package name com.zubair.social, isi folder mein. Wizard ke har option ka matlab samjhao.
2. Project ka folder structure samjhao: app/, build.gradle.kts, libs.versions.toml, AndroidManifest.xml, MainActivity.kt.
3. Mujh se Firebase console mein project banwao, Android app register karwao, aur google-services.json sahi jagah rakhwao.
4. Gradle mein Firebase BoM, Auth aur Realtime Database dependencies add karo, aur samjhao ke BoM kyun use karte hain.
5. git init karo aur .gitignore check karo (google-services.json ke bare mein bhi batao).
Done jab: app emulator pe chal jaye aur Firebase connected ho.
```

**Done jab:** app emulator pe chale, Gradle sync bina error ke ho.

---

## Day 2: Design tokens (Theme.kt)

**Seekhoge:** Design tokens kya hote hain, `MaterialTheme` kaise kaam karta hai, hardcoded values kyun nahi likhte.

```text
Hum Zubair Social bana rahe hain (README.md aur PROJECT_PLAN.md dekho). Main seekh raha hoon, is liye har step pe samjhao ke kya ho raha hai aur kyun. Kaam chhote steps mein karo, har step ke baad mujhe run karke check karne do. Jo kaam main khud kar sakta hoon (Android Studio, Firebase console) wo mujh se karwao. End mein recap do aur README.md ki checklist update karo.

Aaj Day 2 hai: Design tokens.
1. Pehle samjhao ke design tokens kya hain (color, typography, spacing, shape/radius) aur design system mein inki kya jagah hai.
2. ui/theme/ mein Color.kt, Type.kt, Shape.kt aur Spacing.kt banao. Colors Stitch design ke purple/indigo accent se match karo. Light + dark dono color schemes.
3. Spacing ke liye custom tokens (xs, sm, md, lg, xl) CompositionLocal ke zariye banao, aur samjhao ke CompositionLocal kya hota hai.
4. Theme.kt mein ZubairSocialTheme wrap karo.
5. Ek "token gallery" @Preview screen banao jo sab colors, text styles aur spacing dikhaye, light aur dark dono mein.
Done jab: Preview mein poora token gallery nazar aaye.
```

**Done jab:** Preview mein sab colors, fonts aur spacing light/dark dono mein dikhein.

---

## Day 3: Core components + Preview gallery

**Seekhoge:** Reusable `@Composable` kaise banate hain, parameters/slots, state hoisting, `@Preview` se component catalog.

```text
Hum Zubair Social bana rahe hain (README.md aur PROJECT_PLAN.md dekho). Main seekh raha hoon, is liye har step pe samjhao ke kya ho raha hai aur kyun. Kaam chhote steps mein karo, har step ke baad mujhe run karke check karne do. Jo kaam main khud kar sakta hoon (Android Studio, Firebase console) wo mujh se karwao. End mein recap do aur README.md ki checklist update karo.

Aaj Day 3 hai: Core components.
ui/components/ mein yeh components banao, sirf Day 2 ke tokens use kar ke (koi hardcoded color/dp nahi):
- PrimaryButton (loading aur disabled states ke sath)
- AppTextField (label, error message, password mode)
- Avatar (small aur large size, image na ho to initials)
- FollowButton (filled "Follow" / outline "Follow back" / "Following")
- ListRow (avatar + title + subtitle + trailing slot)
- PostCard ka shell (abhi dummy data ke sath)
Har component ke liye @Preview banao (har state ka). Samjhao: stateless component kya hota hai, state hoisting kya hai, aur "trailing slot" (composable lambda parameter) kyun useful hai.
Done jab: har component ka Preview sab states ke sath dikhe.
```

**Done jab:** har component ka Preview sab states mein dikhe.

---

## Day 4: Navigation + BottomNavBar

**Seekhoge:** Navigation Compose, routes, `Scaffold`, aur screens ke beech kaise jaate hain.

```text
Hum Zubair Social bana rahe hain (README.md aur PROJECT_PLAN.md dekho). Main seekh raha hoon, is liye har step pe samjhao ke kya ho raha hai aur kyun. Kaam chhote steps mein karo, har step ke baad mujhe run karke check karne do. Jo kaam main khud kar sakta hoon (Android Studio, Firebase console) wo mujh se karwao. End mein recap do aur README.md ki checklist update karo.

Aaj Day 4 hai: Navigation + BottomNavBar.
1. Navigation Compose add karo aur samjhao NavHost, NavController aur routes kya hain.
2. BottomNavBar component banao (Home, Search, Create, Activity, Profile) tokens ke sath, aur Preview.
3. Har tab ke liye placeholder screen banao (sirf title).
4. Scaffold mein BottomNavBar lagao, tab switch kaam kare, back button sahi behave kare.
5. Auth wale screens (Login/Signup) ke liye alag nav graph ki jagah rakho, jahan bottom bar nahi dikhega.
Folder structure feature-wise rakho (ui/feed, ui/profile, ...) aur samjhao kyun.
Done jab: paanchon tabs pe click kar ke screens badlein.
```

**Done jab:** paanchon tabs ke beech switch ho sake.

---

## Day 5: Sign up

**Seekhoge:** Firebase Auth, ViewModel + UI state, coroutines, RTDB mein pehla write.

```text
Hum Zubair Social bana rahe hain (README.md aur PROJECT_PLAN.md dekho). Main seekh raha hoon, is liye har step pe samjhao ke kya ho raha hai aur kyun. Kaam chhote steps mein karo, har step ke baad mujhe run karke check karne do. Jo kaam main khud kar sakta hoon (Android Studio, Firebase console) wo mujh se karwao. End mein recap do aur README.md ki checklist update karo.

Aaj Day 5 hai: Sign up. docs/features/01-users-auth.md ka "Signup" hissa follow karo.
1. Mujh se Firebase console mein Email/Password provider aur Realtime Database enable karwao (test mode), aur test mode ka risk samjhao.
2. ViewModel, UiState (data class) aur StateFlow ka concept samjhao, phir SignupViewModel banao.
3. AuthRepository banao (Firebase calls yahan, UI mein nahi) aur samjhao ke repository layer kyun.
4. Client-side validation, username availability check (/usernames), createUserWithEmailAndPassword, phir /users/{uid} aur /usernames/{username} writes.
5. Doc ki failure table ke sab errors user-friendly message ke sath handle karo.
6. Signup screen Day 3 ke components se banao, success pe Home pe navigate.
Done jab: naya account bane aur Firebase console mein /users aur /usernames data nazar aaye.
```

**Done jab:** naya account bane aur Firebase console mein data dikhe.

---

## Day 6: Login + auto-login + logout

**Seekhoge:** Session kaise save hota hai, start destination ka decision, error codes handle karna.

```text
Hum Zubair Social bana rahe hain (README.md aur PROJECT_PLAN.md dekho). Main seekh raha hoon, is liye har step pe samjhao ke kya ho raha hai aur kyun. Kaam chhote steps mein karo, har step ke baad mujhe run karke check karne do. Jo kaam main khud kar sakta hoon (Android Studio, Firebase console) wo mujh se karwao. End mein recap do aur README.md ki checklist update karo.

Aaj Day 6 hai: Login. docs/features/01-users-auth.md ka "Login" aur "App Launch" hissa follow karo.
1. LoginViewModel + Login screen (Day 3 components), Sign up link ke sath.
2. signInWithEmailAndPassword aur doc ki failure table ke sab error cases.
3. App launch pe currentUser check: logged in ho to seedha Home, warna Login. Samjhao Firebase session khud kahan save karta hai.
4. Profile tab pe temporary Logout button, logout ke baad back stack clear ho (back dabane pe Home wapas na aaye).
Done jab: login, app band kar ke dobara kholne pe auto-login, aur logout teeno kaam karein.
```

**Done jab:** login, auto-login aur logout teeno kaam karein.

---

## Day 7: Profile (read-only)

**Seekhoge:** RTDB se read karna, one-time read vs real-time listener, `Flow` mein convert karna.

```text
Hum Zubair Social bana rahe hain (README.md aur PROJECT_PLAN.md dekho). Main seekh raha hoon, is liye har step pe samjhao ke kya ho raha hai aur kyun. Kaam chhote steps mein karo, har step ke baad mujhe run karke check karne do. Jo kaam main khud kar sakta hoon (Android Studio, Firebase console) wo mujh se karwao. End mein recap do aur README.md ki checklist update karo.

Aaj Day 7 hai: Profile (read-only). docs/features/02-profile.md follow karo.
1. Samjhao: RTDB mein get() (ek baar) aur addValueEventListener (real-time) mein kya farq hai, aur callbackFlow se listener ko Flow kaise banate hain.
2. UserRepository + ProfileViewModel banao, /users/{uid} read karo.
3. Profile header: large Avatar, username, full name, bio, stats (posts/followers/following, abhi 0 bhi theek hai).
4. Posts grid ki jagah (LazyVerticalGrid) ek empty state ke sath.
5. Loading aur error states dikhao.
Done jab: Profile tab pe apna asli data RTDB se dikhe.
```

**Done jab:** Profile tab pe apna asli data dikhe.

---

## Day 8: Edit Profile

**Seekhoge:** Form state, RTDB `updateChildren`, partial updates.

```text
Hum Zubair Social bana rahe hain (README.md aur PROJECT_PLAN.md dekho). Main seekh raha hoon, is liye har step pe samjhao ke kya ho raha hai aur kyun. Kaam chhote steps mein karo, har step ke baad mujhe run karke check karne do. Jo kaam main khud kar sakta hoon (Android Studio, Firebase console) wo mujh se karwao. End mein recap do aur README.md ki checklist update karo.

Aaj Day 8 hai: Edit Profile. docs/features/02-profile.md follow karo.
1. Edit Profile screen: full name aur bio edit (avatar upload Day 10 ke baad karenge, abhi nahi).
2. setValue vs updateChildren ka farq samjhao, aur updateChildren se save karo.
3. Save button pe loading state, success pe wapas Profile, aur Profile real-time listener ki wajah se khud update ho.
4. Discard changes: bina save kiye back dabane pe kya hona chahiye, mujh se decide karwao.
Done jab: bio badlo, save karo, aur Profile pe turant naya bio dikhe.
```

**Done jab:** bio edit kar ke save karo aur Profile pe turant update dikhe.

---

## Day 9: Cloudflare Worker + R2 (image upload backend)

**Seekhoge:** Presigned URLs, serverless functions, secret keys app mein kyun nahi rakhte.

```text
Hum Zubair Social bana rahe hain (README.md aur PROJECT_PLAN.md dekho). Main seekh raha hoon, is liye har step pe samjhao ke kya ho raha hai aur kyun. Kaam chhote steps mein karo, har step ke baad mujhe run karke check karne do. Jo kaam main khud kar sakta hoon (Android Studio, Firebase console) wo mujh se karwao. End mein recap do aur README.md ki checklist update karo.

Aaj Day 9 hai: Cloudflare Worker + R2. docs/features/03-create-post.md ka upload flow follow karo.
1. Pehle presigned URL ka concept diagram ke sath samjhao: secret key Worker mein kyun, app mein kyun nahi.
2. Mujh se Cloudflare dashboard mein R2 bucket banwao aur public access (ya custom domain) set karwao.
3. worker/ folder mein Wrangler ke sath Worker banao: POST /upload-url jo presigned PUT URL aur final public URL return kare.
4. Worker sirf logged-in users ke liye ho: app Firebase ID token bheje, Worker usay verify kare. Samjhao ID token kya hai.
5. Secrets wrangler secret se set karwao, code mein nahi.
6. curl se test karo: URL lo, ek image PUT karo, browser mein public URL kholo.
Done jab: curl se upload ho aur image browser mein khule.
```

**Done jab:** curl se image upload ho kar browser mein khule.

---

## Day 10: Create Post screen

**Seekhoge:** Android Photo Picker, OkHttp/Ktor se upload, multi-path RTDB write.

```text
Hum Zubair Social bana rahe hain (README.md aur PROJECT_PLAN.md dekho). Main seekh raha hoon, is liye har step pe samjhao ke kya ho raha hai aur kyun. Kaam chhote steps mein karo, har step ke baad mujhe run karke check karne do. Jo kaam main khud kar sakta hoon (Android Studio, Firebase console) wo mujh se karwao. End mein recap do aur README.md ki checklist update karo.

Aaj Day 10 hai: Create Post. docs/features/03-create-post.md follow karo (location, tag people, advanced settings MVP se bahar hain).
1. Photo Picker (PickVisualMedia) se image chuno, preview dikhao, Coil se image load karna samjhao.
2. Upload se pehle image compress/resize karo aur samjhao kyun.
3. Day 9 wale Worker se presigned URL lo, image PUT karo, "Uploading" screen pe progress dikhao.
4. Ek hi multi-path updateChildren se /posts/{postId}, /userPosts/{uid}/{postId}, /feedPosts/{postId} likho, aur samjhao ke multi-path write atomic kyun hai.
5. Upload fail ho to retry ka option.
6. Ab Edit Profile mein avatar upload bhi isi flow se jodo.
Done jab: post bane aur Firebase console mein teeno paths mein data dikhe.
```

**Done jab:** post bane aur teeno RTDB paths mein data dikhe.

---

## Day 11: Home Feed

**Seekhoge:** `LazyColumn`, RTDB queries (`orderByChild`, `limitToLast`), data join karna client pe.

```text
Hum Zubair Social bana rahe hain (README.md aur PROJECT_PLAN.md dekho). Main seekh raha hoon, is liye har step pe samjhao ke kya ho raha hai aur kyun. Kaam chhote steps mein karo, har step ke baad mujhe run karke check karne do. Jo kaam main khud kar sakta hoon (Android Studio, Firebase console) wo mujh se karwao. End mein recap do aur README.md ki checklist update karo.

Aaj Day 11 hai: Home Feed. docs/features/04-feed.md follow karo.
1. /feedPosts ko createdAt ke hisab se newest pehle read karo (orderByChild + limitToLast) aur samjhao RTDB order kaise karta hai.
2. Har post ke liye /posts/{postId} aur author ke liye /users/{uid} lo. Samjhao RTDB mein joins kyun nahi, aur author data ko cache kaise karte hain taake har post pe dobara na mangna pade.
3. Day 3 wala PostCard ab asli data ke sath, LazyColumn mein, key ke sath (samjhao key kyun).
4. Pull-to-refresh, empty state, aur loading state.
5. Profile ka posts grid bhi ab /userPosts se bhar do.
Done jab: Feed mein sab posts newest pehle dikhein aur Profile grid bhi bhar jaye.
```

**Done jab:** Feed aur Profile grid dono mein asli posts dikhein.

---

## Day 12: Like button

**Seekhoge:** RTDB transactions, optimistic UI update.

```text
Hum Zubair Social bana rahe hain (README.md aur PROJECT_PLAN.md dekho). Main seekh raha hoon, is liye har step pe samjhao ke kya ho raha hai aur kyun. Kaam chhote steps mein karo, har step ke baad mujhe run karke check karne do. Jo kaam main khud kar sakta hoon (Android Studio, Firebase console) wo mujh se karwao. End mein recap do aur README.md ki checklist update karo.

Aaj Day 12 hai: Like. docs/features/04-feed.md ka like hissa follow karo.
1. Likes ka data model decide karo (jaise /postLikes/{postId}/{uid}: true) aur PROJECT_PLAN.md mein add karo.
2. likeCount ko runTransaction se badhao/ghatao. Samjhao do log ek sath like karein to transaction ke baghair kya galat hota.
3. Optimistic UI: tap karte hi dil bhar jaye, fail ho to wapas. Samjhao yeh pattern.
4. Double-tap on image se like (chhota animation, optional).
Done jab: like/unlike kaam kare aur count sahi rahe, do devices pe bhi.
```

**Done jab:** like/unlike sahi count ke sath kaam kare.

---

## Day 13: Search

**Seekhoge:** Prefix query, debounce, `Flow` operators (`debounce`, `flatMapLatest`).

```text
Hum Zubair Social bana rahe hain (README.md aur PROJECT_PLAN.md dekho). Main seekh raha hoon, is liye har step pe samjhao ke kya ho raha hai aur kyun. Kaam chhote steps mein karo, har step ke baad mujhe run karke check karne do. Jo kaam main khud kar sakta hoon (Android Studio, Firebase console) wo mujh se karwao. End mein recap do aur README.md ki checklist update karo.

Aaj Day 13 hai: Search. docs/features/05-search.md follow karo.
1. /usernames pe orderByKey().startAt(q).endAt(q + "") prefix search, aur samjhao "" kyun lagate hain.
2. Search bar mein typing pe debounce (300ms) aur flatMapLatest, samjhao purani query cancel kyun honi chahiye.
3. Results Day 3 wale ListRow + Avatar se dikhao.
4. Result pe tap karne se us user ka Profile khule (Profile screen ko ab kisi bhi uid ke liye reusable banao).
5. Empty query, no results, aur lowercase handling.
Done jab: username ka pehla hissa likhne se sahi users milein aur unka profile khule.
```

**Done jab:** search se user mile aur uska profile khule.

---

## Day 14: Follow / Unfollow

**Seekhoge:** Do-tarfa (bidirectional) data, multi-path writes, derived counts.

```text
Hum Zubair Social bana rahe hain (README.md aur PROJECT_PLAN.md dekho). Main seekh raha hoon, is liye har step pe samjhao ke kya ho raha hai aur kyun. Kaam chhote steps mein karo, har step ke baad mujhe run karke check karne do. Jo kaam main khud kar sakta hoon (Android Studio, Firebase console) wo mujh se karwao. End mein recap do aur README.md ki checklist update karo.

Aaj Day 14 hai: Follow / Unfollow. docs/features/05-search.md aur 02-profile.md ka follow hissa follow karo.
1. Follow pe ek multi-path write: /follows/{me}/following/{other} aur /follows/{other}/followers/{me}. Unfollow pe dono null.
2. FollowButton ki teeno states (Follow / Following / Follow back) asli data se.
3. Search results aur doosre users ke Profile dono pe FollowButton.
4. Profile stats mein followers/following count asli data se.
5. Apne aap ko follow na kar sako.
Done jab: follow karo to dono users ke counts sahi badlein.
```

**Done jab:** follow/unfollow pe dono users ke counts sahi badlein.

---

## Day 15: Notifications

**Seekhoge:** Real-time listeners, events se data banana, unread state.

```text
Hum Zubair Social bana rahe hain (README.md aur PROJECT_PLAN.md dekho). Main seekh raha hoon, is liye har step pe samjhao ke kya ho raha hai aur kyun. Kaam chhote steps mein karo, har step ke baad mujhe run karke check karne do. Jo kaam main khud kar sakta hoon (Android Studio, Firebase console) wo mujh se karwao. End mein recap do aur README.md ki checklist update karo.

Aaj Day 15 hai: Notifications. docs/features/06-notifications.md follow karo (sirf in-app list, push/FCM nahi).
1. Like (Day 12) aur Follow (Day 14) ke waqt /notifications/{targetUid}/{notifId} bhi likho, usi multi-path write mein. Apne post pe khud like karne pe notification na bane.
2. Activity tab pe /notifications/{uid} ka real-time listener, newest pehle, ListRow se.
3. Follow notification pe "Follow back" button (FollowButton reuse).
4. Unread notifications ke liye BottomNavBar ke Activity icon pe badge, aur screen kholne pe read: true.
Done jab: doosre account se like/follow karo aur pehle account pe turant notification aaye.
```

**Done jab:** doosre account ke like/follow pe turant notification aaye.

---

## Day 16: Cleanup, Security Rules, aur review

**Seekhoge:** Firebase Security Rules, code review, apni design system ka audit.

```text
Hum Zubair Social bana rahe hain (README.md aur PROJECT_PLAN.md dekho). Main seekh raha hoon, is liye har step pe samjhao ke kya ho raha hai aur kyun. Kaam chhote steps mein karo, har step ke baad mujhe run karke check karne do. Jo kaam main khud kar sakta hoon (Android Studio, Firebase console) wo mujh se karwao. End mein recap do aur README.md ki checklist update karo.

Aaj Day 16 hai: Cleanup aur Security.
1. Test mode band karo aur RTDB Security Rules likho: har user sirf apna /users data badle, username sirf ek baar reserve ho (signup race condition yahan fix karo), posts sirf owner delete kare, waghaira. Rules ko Firebase Rules Playground mein test karwao.
2. Design system audit: poore code mein hardcoded colors/dp/text sizes dhoondo aur tokens se replace karo. Jo UI do jagah copy hua hai usay component banao.
3. Preview gallery update karo taake har component wahan ho.
4. Mera code review karo: kahan galti hai, kahan behtar ho sakta hai, aur mujhe samjhao.
5. Ek recap do ke 16 dinon mein kya kya seekha, aur v2 ke liye agla kya (PROJECT_PLAN.md ki v2 list).
Done jab: app test mode ke baghair bhi poori kaam kare.
```

**Done jab:** test mode band ho aur app phir bhi poori chale.

---

## Agar koi din atak jaye

Yeh prompt use karo:

```text
Day __ pe atak gaya hoon. Masla yeh hai: ____ (error ya jo ho raha hai wo paste karo). Seedha fix mat do. Pehle samjhao ke yeh error kyun aa raha hai, phir mujhe khud fix karne ka hint do. Agar main na kar sakoon to phir fix dikhao.
```

Agar ek din ka kaam ek session mein khatam na ho, to agle din yeh likho:

```text
Day __ adhoora reh gaya tha. Code dekho, batao kahan tak hua hai, aur wahi se continue karo.
```
