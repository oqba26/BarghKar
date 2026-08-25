# پیاده‌سازی Supabase با معماری Offline-First و قابلیت‌های امنیتی

این طرح شامل یکپارچه‌سازی کامل Supabase در پروژه "برق‌کار" است، به گونه‌ای که برنامه ابتدا با داده‌های محلی (Room) کار کند و سپس در پس‌زمینه با سرور همگام‌سازی شود. همچنین راهکاری برای حذف خودکار فایل‌های نصب (APK) جهت بهبود امنیت ارائه شده است.

## User Review Required

> [!IMPORTANT]
> برای راه‌اندازی نهایی، شما باید **URL** و **API Key** پروژه Supabase خود را در فایل تنظیمات (که ایجاد خواهد شد) قرار دهید.
> همچنین برای قابلیت Realtime، باید تنظیمات جداول را در پنل Supabase روی حالت `Replica` قرار دهید.

> [!WARNING]
> با اضافه شدن قابلیت همگام‌سازی، نسخه دیتابیس Room به نسخه ۳ ارتقا می‌یابد. اگر دیتای بسیار حساسی در نسخه فعلی دارید، حتماً یک بک‌آپ تهیه کنید، هرچند ما Migration را به درستی پیاده خواهیم کرد.

## Proposed Changes

### ۱. زیرساخت و وابستگی‌ها (Dependencies)

#### [MODIFY] [libs.versions.toml](file:///E:/AndroidStudioProjects/BarghKar/gradle/libs.versions.toml)
*   اضافه کردن نسخه‌ها و کتابخانه‌های Supabase شامل:
    *   `postgrest-kt` (دیتابیس)
    *   `gotrue-kt` (احراز هویت)
    *   `storage-kt` (ذخیره فایل)
    *   `realtime-kt` (آپدیت لحظه‌ای)
*   اضافه کردن `androidx.work-runtime-ktx` برای مدیریت وظایف پس‌زمینه.

#### [MODIFY] [build.gradle.kts (app)](file:///E:/AndroidStudioProjects/BarghKar/app/build.gradle.kts)
*   اعمال وابستگی‌های جدید در بخش `dependencies`.

---

### ۲. تنظیمات Supabase و احراز هویت

#### [NEW] [SupabaseClient.kt](file:///E:/AndroidStudioProjects/BarghKar/app/src/main/java/com/oqba26/barghkar/data/remote/SupabaseClient.kt)
*   تعریف کلاینت اصلی Supabase با استفاده از Singleton.
*   پیکربندی ماژول‌های Auth، Database و Storage.

#### [NEW] [AuthRepository.kt](file:///E:/AndroidStudioProjects/BarghKar/app/src/main/java/com/oqba26/barghkar/data/remote/AuthRepository.kt)
*   پیاده‌سازی متدهای ثبت‌نام (Sign Up)، ورود (Login) و مدیریت نشست کاربر (Session).

---

### ۳. مدل‌سازی داده و همگام‌سازی (Offline-First)

#### [MODIFY] [Entities](file:///E:/AndroidStudioProjects/BarghKar/app/src/main/java/com/oqba26/barghkar/data/local/entity/)
*   افزودن فیلدهای `remoteId: String?` و `isSynced: Boolean` به تمام Entityهای مهم (Project, Customer, Material).
*   استفاده از `@Serializable` برای تبدیل مدل‌های Room به مدل‌های قابل ارسال به Supabase.

#### [MODIFY] [AppDatabase.kt](file:///E:/AndroidStudioProjects/BarghKar/app/src/main/java/com/oqba26/barghkar/data/local/AppDatabase.kt)
*   ارتقای نسخه دیتابیس به ۳ و نوشتن `MIGRATION_2_3` برای اضافه کردن ستون‌های جدید.

#### [NEW] [SyncWorker.kt](file:///E:/AndroidStudioProjects/BarghKar/app/src/main/java/com/oqba26/barghkar/data/sync/SyncWorker.kt)
*   پیاده‌سازی منطق همگام‌سازی:
    1.  خواندن رکوردهایی که `isSynced = false` هستند.
    2.  ارسال آن‌ها به Supabase.
    3.  بروزرسانی وضعیت در دیتابیس محلی.

---

### ۴. امنیت و مدیریت فایل APK

#### [MODIFY] [UpdateManager.kt](file:///E:/AndroidStudioProjects/BarghKar/app/src/main/java/com/oqba26/barghkar/utils/UpdateManager.kt)
*   اضافه کردن متد `cleanupOldApks()` برای جستجو و حذف فایل‌های APK دانلود شده در پوشه `Downloads` برنامه.

#### [MODIFY] [MainActivity.kt](file:///E:/AndroidStudioProjects/BarghKar/app/src/main/java/com/oqba26/barghkar/MainActivity.kt)
*   فراخوانی متد حذف APK در `onCreate` تا بلافاصله پس از باز شدن نسخه جدید، فایل نصب قبلی حذف شود.

## Verification Plan

### Automated Tests
*   اجرای تست‌های واحد برای `SyncWorker` جهت اطمینان از صحت منطق ارسال داده.
*   تست Migration دیتابیس برای جلوگیری از کرش در هنگام ارتقا.

### Manual Verification
1.  برنامه را باز کرده و در حالت هواپیما (Offline) چند پروژه ثبت کنید.
2.  اینترنت را وصل کنید و بررسی کنید آیا داده‌ها در پنل Supabase ظاهر می‌شوند یا خیر.
3.  یک آپدیت تستی انجام دهید و مطمئن شوید فایل APK پس از اتمام فرآیند از حافظه گوشی حذف شده است.
