package com.oqba26.barghkar.domain

data class ElectricalSupply(
    val name: String,
    val defaultUnit: String
)

object ElectricalSupplies {
    val commonItems = listOf(
        ElectricalSupply("سیم ۱.۵", "متر"),
        ElectricalSupply("سیم ۲.۵", "متر"),
        ElectricalSupply("سیم ۴", "متر"),
        ElectricalSupply("کابل ۲ در ۲.۵", "متر"),
        ElectricalSupply("کابل ۳ در ۱.۵", "متر"),
        ElectricalSupply("کلید تک‌پل", "عدد"),
        ElectricalSupply("کلید دوپل", "عدد"),
        ElectricalSupply("پریز برق", "عدد"),
        ElectricalSupply("پریز تلفن/شبکه", "عدد"),
        ElectricalSupply("فیوز مینیاتوری ۱۰ آمپر", "عدد"),
        ElectricalSupply("فیوز مینیاتوری ۱۶ آمپر", "عدد"),
        ElectricalSupply("فیوز مینیاتوری ۲۵ آمپر", "عدد"),
        ElectricalSupply("قوطی کلید", "عدد"),
        ElectricalSupply("لوله پلیکا ۲۰", "شاخه"),
        ElectricalSupply("لوله خرطومی", "متر"),
        ElectricalSupply("زانو برق", "عدد"),
        ElectricalSupply("جعبه تقسیم", "عدد"),
        ElectricalSupply("هالوژن", "عدد"),
        ElectricalSupply("لامپ LED", "عدد"),
        ElectricalSupply("پنل سقفی", "عدد")
    )
    
    val commonUnits = listOf("عدد", "متر", "شاخه", "حلقه", "بسته", "کیلوگرم")
}
