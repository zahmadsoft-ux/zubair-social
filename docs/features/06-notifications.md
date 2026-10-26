# Feature: Notifications

Real-time list — kisi ne like/comment/follow kiya to yahan dikhega. MVP mein **sirf in-app list**, push notification (FCM) nahi.

## Screen
- **Notifications** — list of events: "X liked your post", "X commented: ...", "X started following you" (with inline "Follow back")

## Flow Diagram — Receiving & Displaying Notifications

```mermaid
sequenceDiagram
    participant Other as Doosra User
    participant DB as Realtime Database
    participant App as Meri App (listener attached)
    participant Me as Main

    Other->>DB: Like/Comment/Follow action karta hai
    DB->>DB: write /notifications/{myUid}/{notifId}
    Note over DB,App: ValueEventListener already attached hai /notifications/{myUid} pe
    DB-->>App: onDataChange fire hota hai (real-time push)
    App->>Me: List turant update ho jati hai, naya item sabse upar
```

## Flow Diagram — Notification Screen Logic

```mermaid
flowchart TD
    A[Notifications screen open] --> B["/notifications/myUid pe ValueEventListener lagao"]
    B --> C[Har notification ka 'type' check karo]
    C -->|like| D[Icon: heart, Text: 'X liked your post', thumbnail: post image]
    C -->|comment| E[Icon: comment, Text: 'X commented: ...', thumbnail: post image]
    C -->|follow| F[Icon: person, Text: 'X started following you', 'Follow back' button]
    D --> G[Tap karne par us post pe navigate karo]
    E --> G
    F --> H[Follow back tap -> /follows write, dekho 05-search.md]
```

## RTDB Data Touched

| Path | Operation | Kab |
|---|---|---|
| `/notifications/{myUid}` | real-time read (listener) | Screen open rehte waqt hamesha |
| `/notifications/{myUid}/{notifId}/read` | update | Notification tap karne par (read=true) |
| `/follows/...` | write | "Follow back" tap (dekho [05-search.md](05-search.md)) |

## Design System Components
- `ListRow` (Search se shared shape — avatar + text + action)
- `FollowButton` (outline "Follow back")
- `NotificationIcon` (type ke hisab se heart/comment/person icon)
- `BottomNavBar` (Activity tab active state)

## Build Checklist
- [ ] `ValueEventListener` ya Kotlin Flow wrapper (`callbackFlow`) se `/notifications/{uid}` real-time observe karo
- [ ] Listener ko screen leave karte waqt detach karna mat bhoolna (memory leak se bacho)
- [ ] Notification type ke hisab se alag UI render karo (sealed class `NotificationType` banana acha idea hai)
- [ ] Unread count badge bottom nav ke "Activity" icon pe (optional MVP+ polish)

## Edge Cases
- Post delete ho jaye lekin uski purani "liked your post" notification reh jaye → tap karne par graceful handle karo (post na mile to error state)
- Bohat zyada purani notifications → `limitToLast(50)` jaisi cap lagao, infinite list na ho
- Already followed user ko dobara "Follow back" dikhna nahi chahiye → state check karo

---

**Push Notifications (v2, abhi scope mein nahi):** Agar baad mein lock-screen push chahiye ho, to Firebase Cloud Messaging (FCM) + ek Cloud Function chahiye hoga jo `/notifications` mein naya write hote hi FCM message trigger kare. Yeh extra infrastructure hai, is liye MVP se bahar rakha gaya hai.
