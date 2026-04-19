# 🐕 Dog Breeds — AI-Powered Breed Identifier

An Android app that uses on-device machine learning (TensorFlow Lite) to identify dog breeds from photos taken with the camera or selected from the gallery.

---

## 📱 Video

https://github.com/user-attachments/assets/3b5e2c52-fcb2-4327-85a5-d83c03c7784e


---

## ✨ Features

- **Camera capture** — take a photo directly in-app with automatic EXIF rotation correction
- **Gallery import** — pick any photo from your device
- **On-device AI** — breed identification runs fully offline using a TFLite model
- **10 supported breeds** — Akita, Beagle, Boxer, Bulldog, Chihuahua, Doberman, Golden Retriever, Husky, Labrador, Yorkshire Terrier
- **Confidence score** — visual progress bar showing the model's confidence
- **Breed explorer** — browse all supported breeds and their personality traits
- **Share** — share the app with friends

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| ML | TensorFlow Lite 2.16.1 |
| Image handling | BitmapFactory + ExifInterface |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 (Android 15) |

---

## 🚀 Getting Started

### Prerequisites

- Android Studio (Hedgehog or newer)
- JDK 17+ (Android Studio's bundled JDK works)
- Android device or emulator running API 24+
