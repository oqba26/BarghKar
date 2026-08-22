# افزودن قابلیت مدیریت مشتریان، اقساط، انبارداری و صدور فاکتور

این طرح شامل گسترش پایگاه داده برای پشتیبانی از مدیریت مشتریان، ردیابی اقساط دستمزد، مدیریت موجودی انبار و محاسبه قیمت نهایی به همراه صدور فاکتور (متنی و PDF) است.

## User Review Required

> [!IMPORTANT]
> - **تغییر در ساختار پروژه**: موجودیت `Project` اکنون به یک `Customer` متصل خواهد شد. پروژه‌های فعلی ممکن است نیاز به تعیین مشتری داشته باشند.
> - **نسخه دیتابیس**: نسخه دیتابیس ارتقا می‌یابد و یک Migration ساده برای حفظ داده‌های فعلی اعمال می‌شود.
> - **کتابخانه PDF**: برای تولید PDF از ابزار داخلی اندروید (`PdfDocument`) استفاده می‌شود تا حجم برنامه افزایش نیابد.

## Proposed Changes

### پایگاه داده و مدل‌ها

#### [MODIFY] [ProjectEntity.kt](file:///E:/AndroidStudioProjects/BarghKar/app/src/main/java/com/oqba26/barghkar/data/local/entity/ProjectEntity.kt)
- افزودن `customerId: Long` به عنوان کلید خارجی.
- افزودن `totalWage: Long` برای ثبت کل مبلغ دستمزد پروژه.

#### [NEW] [CustomerEntity.kt](file:///E:/AndroidStudioProjects/BarghKar/app/src/main/java/com/oqba26/barghkar/data/local/entity/CustomerEntity.kt)
- شامل: `id`, `name`, `phoneNumber`, `address`, `createdAt`.

#### [NEW] [InstallmentEntity.kt](file:///E:/AndroidStudioProjects/BarghKar/app/src/main/java/com/oqba26/barghkar/data/local/entity/InstallmentEntity.kt)
- شامل: `id`, `projectId`, `amount`, `dueDate`, `isPaid`.

#### [NEW] [InventoryMaterialEntity.kt](file:///E:/AndroidStudioProjects/BarghKar/app/src/main/java/com/oqba26/barghkar/data/local/entity/InventoryMaterialEntity.kt)
- برای ثبت موجودی کلی برق‌کار (مثلاً ۲ توپ سیم).
- شامل: `id`, `name`, `quantity`, `unit`.

#### [MODIFY] [MaterialEntity.kt](file:///E:/AndroidStudioProjects/BarghKar/app/src/main/java/com/oqba26/barghkar/data/local/entity/MaterialEntity.kt)
- افزودن `pricePerUnit: Long` برای محاسبه قیمت در فاکتور.

#### [MODIFY] [AppDatabase.kt](file:///E:/AndroidStudioProjects/BarghKar/app/src/main/java/com/oqba26/barghkar/data/local/AppDatabase.kt)
- افزودن موجودیت‌های جدید و افزایش نسخه دیتابیس.

---

### لایه داده و رابط کاربری (UI)

#### [NEW] [CustomerDao.kt](file:///E:/AndroidStudioProjects/BarghKar/app/src/main/java/com/oqba26/barghkar/data/local/dao/CustomerDao.kt)
- متدهای CRUD برای مشتریان.

#### [NEW] [InventoryDao.kt](file:///E:/AndroidStudioProjects/BarghKar/app/src/main/java/com/oqba26/barghkar/data/local/dao/InventoryDao.kt)
- متدهای مدیریت انبار.

#### [NEW] [CustomersScreen.kt](file:///E:/AndroidStudioProjects/BarghKar/app/src/main/java/com/oqba26/barghkar/ui/screens/CustomersScreen.kt)
- لیست مشتریان با قابلیت جستجو و افزودن.

#### [NEW] [InventoryScreen.kt](file:///E:/AndroidStudioProjects/BarghKar/app/src/main/java/com/oqba26/barghkar/ui/screens/InventoryScreen.kt)
- نمایش موجودی انبار برق‌کار.

#### [MODIFY] [ProjectDetailsScreen.kt](file:///E:/AndroidStudioProjects/BarghKar/app/src/main/java/com/oqba26/barghkar/ui/screens/ProjectDetailsScreen.kt)
- نمایش متریال‌های مصرفی با قیمت.
- مدیریت اقساط دستمزد.
- نمایش "ماشین حساب" فاکتور (جمع کل متریال + دستمزد).
- دکمه‌های "اشتراک‌گذاری فاکتور" (SMS و PDF).

---

### ابزارهای کمکی (Utility)

#### [NEW] [InvoiceExporter.kt](file:///E:/AndroidStudioProjects/BarghKar/app/src/main/java/com/oqba26/barghkar/utils/InvoiceExporter.kt)
- تابعی برای تولید متن ساده فاکتور.
- تابعی برای تولید فایل PDF با استفاده از `PdfDocument`.

## Verification Plan

### Automated Tests
- تست‌های واحد برای `InvoiceExporter` جهت اطمینان از صحت محاسبات ریاضی.
- تست‌های Room Migration برای اطمینان از حذف نشدن پروژه‌های قبلی.

### Manual Verification
- افزودن یک مشتری و ثبت یک پروژه برای او.
- ثبت متریال با قیمت و چک کردن جمع نهایی.
- ثبت قساط و چک کردن تاریخ سررسید.
- خروجی گرفتن PDF و باز کردن آن در یک برنامه نمایشگر PDF.
- ارسال متن فاکتور به پیام‌رسان‌ها.
