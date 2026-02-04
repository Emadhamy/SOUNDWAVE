# SOUNDWAVE
Professional Music Player for Android

# 🎵 SoundWave

![Build Status](https://github.com/Emadhamy/SOUNDWAVE/workflows/🔨%20Android%20Build/badge.svg)
![Release](https://img.shields.io/github/v/release/Emadhamy/SOUNDWAVE?include_prereleases)
![License](https://img.shields.io/github/license/Emadhamy/SOUNDWAVE)
![API](https://img.shields.io/badge/API-26%2B-brightgreen)

> مشغل موسيقى احترافي للأندرويد

## 📥 التحميل

[![Download APK](https://img.shields.io/badge/Download-APK-green?style=for-the-badge&logo=android)](https://github.com/Emadhamy/SOUNDWAVE/releases/latest)

## ✨ المميزات

- 🎵 تشغيل جميع صيغ الصوت
- 🎛️ معادل صوت احترافي 10 نطاقات
- 📝 كلمات الأغاني المتزامنة
- ⏰ مؤقت النوم
- 📊 عرض الطيف المرئي
- 🌙 الوضع الداكن
- 🔀 التشغيل العشوائي والتكرار

## 📱 متطلبات النظام

- Android 8.0 (API 26) أو أحدث

## 🛠️ البناء

### البناء المحلي (Local Build)

```bash
git clone https://github.com/Emadhamy/SOUNDWAVE.git
cd SOUNDWAVE
./gradlew assembleDebug
```

### البناء باستخدام GitHub Actions

#### 1️⃣ **بناء APK تلقائياً (موقع)**

الـ workflow `android-build-signed.yml` يعمل تلقائياً عند:
- Push إلى `main` أو `develop`
- فتح Pull Request

**أو يمكنك تشغيله يدوياً:**
1. افتح المشروع على GitHub
2. اذهب إلى **Actions** → **Build Signed APK (Auto)**
3. اضغط **Run workflow** → **Run workflow**
4. انتظر اكتمال البناء (حوالي 3-5 دقائق)
5. حمّل APK من **Artifacts**:
   - `soundwave-debug-signed` - للتجربة والاختبار
   - `soundwave-release-signed` - للإصدار

#### 2️⃣ **إنشاء Release رسمي (موقع بـ keystore ثابت)**

##### أولاً: إعداد Keystore

```bash
# قم بتشغيل السكريبت لإنشاء keystore
./create_keystore.bat

# سيتم إنشاء keystore في:
# keystore/soundwave_release.jks
```

##### ثانياً: رفع Secrets على GitHub

1. **تحويل keystore إلى Base64:**
   ```bash
   # على Windows (PowerShell):
   [Convert]::ToBase64String([IO.File]::ReadAllBytes("keystore\soundwave_release.jks")) > keystore_base64.txt
   
   # على Linux/Mac:
   base64 -i keystore/soundwave_release.jks -o keystore_base64.txt
   ```

2. **إضافة Secrets في GitHub:**
   - اذهب إلى Settings → Secrets and variables → Actions
   - اضغط **New repository secret**
   - أضف الـ Secrets التالية:
   
   | اسم Secret | القيمة |
   |-----------|--------|
   | `KEYSTORE_BASE64` | محتوى ملف `keystore_base64.txt` |
   | `KEYSTORE_PASSWORD` | `soundwave2026` |
   | `KEY_ALIAS` | `soundwave_key` |
   | `KEY_PASSWORD` | `soundwave2026` |

##### ثالثاً: إنشاء Release

**الطريقة 1: باستخدام Git Tag**
```bash
git tag v1.0.0
git push origin v1.0.0
```

**الطريقة 2: يدوياً من GitHub**
1. Actions → **Android Release**
2. Run workflow
3. أدخل رقم الإصدار (مثلاً: `1.0.0`)
4. Run workflow

سيتم إنشاء Release تلقائياً مع:
- ✅ APK موقع بـ keystore الرسمي
- ✅ AAB للرفع على Google Play
- ✅ Release notes



## 📄 الرخصة

MIT License
