# 🔐 دليل إعداد GitHub Secrets لبناء APK موقع

هذا الدليل يشرح كيفية إعداد GitHub Secrets لبناء APK موقع بـ keystore رسمي.

## 📋 خطوات الإعداد

### الخطوة 1: إنشاء Keystore محلياً

#### على Windows:
```powershell
# افتح PowerShell في مجلد المشروع
cd c:\Users\Emad\soundwave\SOUNDWAVE

# شغل السكريبت
.\create_keystore.bat
```

سيتم إنشاء:
- **الملف**: `keystore/soundwave_release.jks`
- **Alias**: `soundwave_key`
- **Password**: `soundwave2026`

> ⚠️ **مهم جداً**: احتفظ بنسخة احتياطية من الـ keystore في مكان آمن! إذا فقدته، لن تتمكن من تحديث التطبيق على Google Play.

---

### الخطوة 2: تحويل Keystore إلى Base64

#### على Windows (PowerShell):
```powershell
# طريقة 1: حفظ في ملف
[Convert]::ToBase64String([IO.File]::ReadAllBytes("keystore\soundwave_release.jks")) > keystore_base64.txt

# طريقة 2: عرض مباشر (للنسخ)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("keystore\soundwave_release.jks"))
```

#### على Linux/macOS:
```bash
# حفظ في ملف
base64 -i keystore/soundwave_release.jks -o keystore_base64.txt

# عرض مباشر
base64 -i keystore/soundwave_release.jks
```

---

### الخطوة 3: إضافة Secrets على GitHub

1. **افتح مشروعك على GitHub**
   ```
   https://github.com/Emadhamy/SOUNDWAVE
   ```

2. **اذهب إلى Settings**
   - اضغط على **Settings** (في شريط القوائم العلوي)

3. **افتح Secrets and variables**
   - من القائمة الجانبية، اضغط **Secrets and variables**
   - ثم اضغط **Actions**

4. **أضف الـ Secrets الأربعة**
   
   لكل secret:
   - اضغط **New repository secret**
   - أدخل الاسم والقيمة
   - اضغط **Add secret**

   | **اسم Secret** | **القيمة** | **الوصف** |
   |---------------|-----------|-----------|
   | `KEYSTORE_BASE64` | محتوى `keystore_base64.txt` كامل | الـ keystore مشفر بـ Base64 |
   | `KEYSTORE_PASSWORD` | `soundwave2026` | كلمة مرور الـ keystore |
   | `KEY_ALIAS` | `soundwave_key` | اسم المفتاح داخل الـ keystore |
   | `KEY_PASSWORD` | `soundwave2026` | كلمة مرور المفتاح |

> 💡 **ملاحظة**: `KEYSTORE_BASE64` سيكون نص طويل جداً (آلاف الأحرف) - هذا طبيعي!

---

### الخطوة 4: التحقق من الإعداد

بعد إضافة جميع الـ Secrets، يجب أن ترى:

```
✓ KEYSTORE_BASE64 (Updated X minutes ago)
✓ KEYSTORE_PASSWORD (Updated X minutes ago)  
✓ KEY_ALIAS (Updated X minutes ago)
✓ KEY_PASSWORD (Updated X minutes ago)
```

---

## 🚀 استخدام الـ Workflow

### الطريقة 1: إنشاء Release باستخدام Tag

```bash
# إنشاء tag
git tag v1.0.0

# رفع tag إلى GitHub
git push origin v1.0.0

# سيبدأ workflow تلقائياً!
```

### الطريقة 2: تشغيل يدوياً

1. اذهب إلى **Actions**
2. اختر **🚀 Android Release**
3. اضغط **Run workflow**
4. أدخل رقم الإصدار (مثلاً: `1.0.0`)
5. اضغط **Run workflow**

---

## 📦 تحميل الملفات

بعد اكتمال البناء:

1. افتح صفحة **Releases** على GitHub
2. ستجد release جديد باسم `SoundWave v1.0.0`
3. حمّل الملفات:
   - **APK**: للتثبيت المباشر
   - **AAB**: للرفع على Google Play

---

## ❓ حل المشاكل الشائعة

### المشكلة: "Secret KEYSTORE_BASE64 not found"
**الحل**: تأكد من إضافة جميع الـ Secrets بالأسماء الصحيحة تماماً

### المشكلة: "Failed to decode keystore"
**الحل**: تأكد من نسخ محتوى `keystore_base64.txt` **كاملاً** بدون فراغات أو أسطر جديدة

### المشكلة: "Incorrect keystore password"
**الحل**: تأكد من أن `KEYSTORE_PASSWORD` = `soundwave2026` (نفس الكلمة المستخدمة في create_keystore.bat)

### المشكلة: "Key alias not found"
**الحل**: تأكد من أن `KEY_ALIAS` = `soundwave_key` (نفس الاسم المستخدم في create_keystore.bat)

---

## 🔒 نصائح الأمان

1. ✅ **لا تشارك** keystore أو passwords مع أحد
2. ✅ **احفظ نسخة احتياطية** من keystore في مكان آمن (خارج Git)
3. ✅ **لا ترفع** keystore إلى Git (موجود في `.gitignore`)
4. ✅ **استخدم Secrets** على GitHub - لا تضع passwords في الكود
5. ✅ **سجل** معلومات keystore في مكان آمن:
   ```
   Keystore: soundwave_release.jks
   Store Password: soundwave2026
   Key Alias: soundwave_key
   Key Password: soundwave2026
   ```

---

## ✅ Checklist

قبل إنشاء أول release:

- [ ] تم إنشاء keystore محلياً
- [ ] تم حفظ نسخة احتياطية من keystore
- [ ] تم تحويل keystore إلى Base64
- [ ] تم إضافة جميع الـ Secrets الأربعة على GitHub
- [ ] تم التحقق من أسماء الـ Secrets (حساسة لحالة الأحرف)
- [ ] تم تسجيل معلومات keystore في مكان آمن

---

## 📞 مزيد من المساعدة

- [التوثيق الرسمي لـ Android App Signing](https://developer.android.com/studio/publish/app-signing)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
