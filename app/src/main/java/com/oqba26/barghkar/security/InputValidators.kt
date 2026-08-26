package com.oqba26.barghkar.security

object InputValidators {
    fun validateCustomer(name: String, phone: String, address: String): String? {
        val cleanName = name.trim()
        val cleanPhone = phone.trim()
        val cleanAddress = address.trim()

        if (cleanName.isBlank()) return "نام مشتری نمی‌تواند خالی باشد"
        if (cleanPhone.isBlank()) return "شماره تماس نمی‌تواند خالی باشد"
        if (cleanPhone.length < 8) return "شماره تماس باید حداقل 8 رقم باشد"
        if (cleanAddress.isBlank()) return "آدرس نمی‌تواند خالی باشد"
        if (cleanAddress.length < 3) return "آدرس باید حداقل 3 کاراکتر باشد"
        return null
    }

    fun validateInventory(name: String, quantity: Double, unit: String): String? {
        val cleanName = name.trim()
        val cleanUnit = unit.trim()

        if (cleanName.isBlank()) return "نام کالا نمی‌تواند خالی باشد"
        if (quantity <= 0.0) return "مقدار کالا باید بزرگ‌تر از صفر باشد"
        if (cleanUnit.isBlank()) return "واحد کالا نمی‌تواند خالی باشد"
        return null
    }

    fun validateProject(name: String, description: String, area: Double, priceFixture: Long, priceMeter: Long, p1: Long, p2: Long, p3: Long): String? {
        val cleanName = name.trim()
        val cleanDescription = description.trim()

        if (cleanName.isBlank()) return "نام پروژه نمی‌تواند خالی باشد"
        if (cleanDescription.isBlank()) return "توضیحات پروژه نمی‌تواند خالی باشد"
        if (area < 0.0) return "مساحت پروژه نمی‌تواند منفی باشد"
        if (priceFixture < 0) return "قیمت هر قطعه نمی‌تواند منفی باشد"
        if (priceMeter < 0) return "قیمت هر متر نمی‌تواند منفی باشد"
        if (p1 < 0 || p2 < 0 || p3 < 0) return "مقادیر پرداخت نمی‌توانند منفی باشند"
        return null
    }

    fun validateMaterial(name: String, quantity: Int, unit: String, pricePerUnit: Long): String? {
        val cleanName = name.trim()
        val cleanUnit = unit.trim()

        if (cleanName.isBlank()) return "نام کالا نمی‌تواند خالی باشد"
        if (quantity <= 0) return "تعداد کالا باید بزرگ‌تر از صفر باشد"
        if (cleanUnit.isBlank()) return "واحد کالا نمی‌تواند خالی باشد"
        if (pricePerUnit < 0) return "قیمت واحد نمی‌تواند منفی باشد"
        return null
    }

    fun validateInstallment(amount: Long): String? {
        if (amount <= 0L) return "مبلغ قسط باید بزرگ‌تر از صفر باشد"
        return null
    }
}
