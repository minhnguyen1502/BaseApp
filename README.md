# Base App (template)

Template Android tách từ M69-IPTV, dùng làm điểm xuất phát cho các app mới.
Package: `com.wavez.base` · Kotlin · Hilt · ViewBinding · minSdk 24.

## Có sẵn

| Phần | Vị trí |
|---|---|
| Base classes (Activity/Fragment/Dialog/BottomSheet/Adapter/ViewModel) | `base/` |
| AdMob: banner, interstitial, native, native collapse/full, open, reward + mediation (Meta, Mintegral, Pangle) | `com.wavez.admob`, `ads/` |
| Remote Config cho ads (on/off, gap, vị trí native) | `ads/AppRemoteConfig*.kt` |
| Google Play Billing + màn Premium | `com.wavez.billing`, `ui/premium/` |
| Firebase (Analytics, Crashlytics, Remote Config, Messaging), Adjust, Facebook SDK | `App.kt`, `firebase/` |
| Luồng: Splash → Onboarding Language → Onboarding → Home | `ui/splash`, `ui/onboard_language`, `ui/onboarding` |
| Home 2 tab (Home trống + Setting), Language, Rate, Feedback | `ui/home`, `ui/setting`, `ui/language` |
| Thông báo re-engagement + xin quyền notification | `notification/`, `ui/home/notification` |
| Extensions, network tracker, permission manager, SharedPref | `extension/`, `utils/`, `storage/` |

Màn Home là placeholder: viết nội dung app vào `ui/home/fragment/HomeFragment.kt` + `res/layout/fragment_home.xml`.

## Checklist khi tạo app mới

1. Copy thư mục này (bỏ `.git`, `build`, `.gradle`), đổi tên thư mục.
2. Đổi package `com.wavez.base` → package mới (Android Studio: Refactor ▸ Rename package), sửa `namespace` + `applicationId` trong `app/build.gradle.kts`, `rootProject.name` trong `settings.gradle.kts`.
3. Thay `app/google-services.json` bằng file của Firebase project mới (file hiện tại chỉ là placeholder để build được).
4. Thay các placeholder:
   - `AndroidManifest.xml`: `com.google.android.gms.ads.APPLICATION_ID` (đang là App ID test của Google).
   - `ads/Ads.kt`: các `ca-app-pub-REPLACE/REPLACE` (bản debug tự dùng ID test).
   - `utils/Constant.kt`: `TOKEN_ADJUST`, `PANGLE_APP_ID`, link privacy, email feedback.
   - `res/values/facebook.xml`: Facebook app id + client token.
   - `ui/premium/BillingConstant.kt`: product id gói premium.
5. Đổi `app_name` (`res/values/strings.xml`), icon (`mipmap-*`), theme màu (`res/values/colors.xml`, `themes.xml`).
6. Sửa nội dung onboarding (`storage/onboarding/OnboardingProvider.kt`, text trong strings) và text thông báo re-engagement cho đúng app.
7. Thêm keystore riêng vào `keys/` (đã gitignore) và cấu hình `signingConfigs` cho release.

## Ghi chú

- `strings.xml` (90+ ngôn ngữ) vẫn còn một số chuỗi của app IPTV cũ chưa dùng tới; lint `UnusedResources` sẽ chỉ ra để dọn dần.
- Room, OkHttp, WorkManager vẫn giữ trong dependencies vì app sau thường cần; bỏ nếu không dùng.
