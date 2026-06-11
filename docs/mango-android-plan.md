# 🥭 Mango Disease Detector — Android App Plan
### For Illiterate Farmers | Full Bengali UI | University Project

---

## 📱 App Overview

| Item | Detail |
|------|--------|
| App Name | আমের রোগ নির্ণয় (Mango Disease Detector) |
| Language | Bengali (বাংলা) |
| Target User | Illiterate / low-literacy farmers |
| API | Hugging Face (already live) |
| Min Android | API 24 (Android 7.0) |
| Architecture | MVVM |

---

## 🎨 Design Philosophy

- **Big icons, minimal text** — farmers may not read well
- **Large buttons** — easy to tap with rough hands
- **Green & yellow color theme** — feels natural for farming
- **Voice feedback (optional)** — speak the disease name in Bengali
- **Simple 3-step flow** — Open → Take Photo → See Result

---

## 📂 Full Project Structure

```
app/
├── manifests/
│   └── AndroidManifest.xml
│
├── java/com/yourname/mangodetector/
│   │
│   ├── ui/
│   │   ├── onboarding/
│   │   │   ├── OnboardingActivity.kt         # Splash + onboarding screens
│   │   │   ├── OnboardingAdapter.kt          # ViewPager adapter
│   │   │   └── OnboardingFragment.kt         # Each onboarding page
│   │   │
│   │   ├── home/
│   │   │   ├── HomeActivity.kt               # Main screen
│   │   │   └── HomeFragment.kt               # Upload/camera button
│   │   │
│   │   ├── result/
│   │   │   ├── ResultActivity.kt             # Show disease result
│   │   │   └── ResultFragment.kt             # Disease name + confidence
│   │   │
│   │   └── splash/
│   │       └── SplashActivity.kt             # App loading screen
│   │
│   ├── network/
│   │   ├── ApiService.kt                     # API call definition
│   │   └── ApiClient.kt                      # OkHttp client setup
│   │
│   ├── model/
│   │   └── PredictionResponse.kt             # Data class for API response
│   │
│   ├── viewmodel/
│   │   └── DetectorViewModel.kt              # Handles API call logic
│   │
│   └── utils/
│       ├── ImageUtils.kt                     # Image resize/compress helper
│       └── PermissionUtils.kt                # Camera/storage permissions
│
└── res/
    ├── layout/
    │   ├── activity_splash.xml
    │   ├── activity_onboarding.xml
    │   ├── fragment_onboarding.xml
    │   ├── activity_home.xml
    │   ├── fragment_home.xml
    │   ├── activity_result.xml
    │   └── fragment_result.xml
    │
    ├── values/
    │   ├── strings.xml                       # All Bengali text here
    │   ├── colors.xml                        # Green/yellow theme
    │   └── themes.xml
    │
    ├── drawable/
    │   ├── ic_camera.xml                     # Camera icon
    │   ├── ic_gallery.xml                    # Gallery icon
    │   ├── bg_button_green.xml               # Big green button style
    │   ├── onboarding_1.png                  # Farmer image
    │   ├── onboarding_2.png                  # Leaf scanning image
    │   └── onboarding_3.png                  # Result image
    │
    └── anim/
        ├── fade_in.xml                       # Splash animation
        ├── slide_in.xml                      # Screen transitions
        └── result_bounce.xml                 # Result card animation
```

---

## 🖼️ Screens (Activities & Fragments)

### 1. SplashActivity
- App logo with mango tree animation
- Shows for 2 seconds
- Checks if onboarding was shown before
- Goes to → OnboardingActivity (first time) or HomeActivity

### 2. OnboardingActivity (3 pages, swipeable)

| Page | Image | Bengali Text |
|------|-------|-------------|
| Page 1 | Farmer with phone | আপনার আমের পাতার ছবি তুলুন |
| Page 2 | Leaf being scanned | আমাদের AI রোগ শনাক্ত করবে |
| Page 3 | Happy farmer | সঠিক চিকিৎসা জানুন |

- Big **NEXT** and **শুরু করুন (Start)** buttons
- Dot indicators at bottom
- Skip button at top right

### 3. HomeActivity (Main Screen)
- Big mango leaf icon at top
- Title: **আমের রোগ নির্ণয়**
- Two big buttons:
  - 📷 **ছবি তুলুন** (Take Photo) — opens camera
  - 🖼️ **গ্যালারি থেকে বেছে নিন** (Choose from Gallery)
- Simple, clean, no clutter

### 4. ResultActivity
- Shows the uploaded leaf image
- Big colored card with:
  - 🟢 Disease name in Bengali (large font)
  - Confidence percentage
  - Simple advice text
- **আবার চেষ্টা করুন** (Try Again) button

---

## 🌿 Disease Name Translations (Bengali)

| English | Bengali |
|---------|---------|
| Anthracnose | অ্যানথ্রাকনোজ |
| Bacterial Canker | ব্যাকটেরিয়াল ক্যাংকার |
| Cutting Weevil | কাটিং উইভিল |
| Die Back | ডাই ব্যাক |
| Gall Midge | গল মিজ |
| Healthy | সুস্থ ✅ |
| Powdery Mildew | পাউডারি মিলডিউ |
| Sooty Mould | সুটি মোল্ড |

---

## 🔗 API Integration

**Endpoint:**
```
POST https://YOUR_USERNAME-mango-disease-detector.hf.space/predict
```

**Response:**
```json
{
  "disease": "Anthracnose",
  "confidence": 94.5
}
```

**Files needed:**
- `ApiClient.kt` — sets up OkHttp
- `ApiService.kt` — defines the POST request
- `DetectorViewModel.kt` — calls API, updates UI
- `PredictionResponse.kt` — maps the JSON response

---

## 📦 Dependencies (build.gradle)

```gradle
dependencies {
    // UI
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'

    // ViewPager2 for onboarding
    implementation 'androidx.viewpager2:viewpager2:1.0.0'

    // Networking
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'

    // JSON parsing
    implementation 'com.google.code.gson:gson:2.10.1'

    // Image loading
    implementation 'com.github.bumptech.glide:glide:4.16.0'

    // ViewModel
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'

    // Lottie animations (for onboarding)
    implementation 'com.airbnb.android:lottie:6.4.0'
}
```

---

## 🚶 User Flow (Step by Step)

```
Open App
    ↓
Splash Screen (2 sec)
    ↓
First time? → Onboarding (3 pages) → Home
Not first time? → Home directly
    ↓
Home Screen
    ↓
Tap "ছবি তুলুন" or "গ্যালারি"
    ↓
Pick / Take leaf image
    ↓
Loading spinner (calling API)
    ↓
Result Screen
    ↓
Show disease name in Bengali + confidence
    ↓
Tap "আবার চেষ্টা করুন" → back to Home
```

---

## ✅ Build Order (What to code first)

1. Set up project + add dependencies
2. `strings.xml` — all Bengali text
3. `colors.xml` + `themes.xml` — green/yellow theme
4. `SplashActivity` — simple logo screen
5. `OnboardingActivity` — 3 page swipe intro
6. `HomeActivity` — two big buttons
7. `ApiClient` + `ApiService` + `PredictionResponse` — API setup
8. `DetectorViewModel` — connect API to UI
9. `ResultActivity` — show the result
10. Test full flow end to end

---

## 🗒️ Notes

- All text must be in `strings.xml` in Bengali — never hardcode text
- Test on a low-end Android phone (farmers won't have expensive phones)
- Keep APK size small — farmers may have limited storage
- Make sure app works on slow internet (add timeout handling)
- Add loading spinner so farmer knows app is working

---
