package com.oqba26.barghkar.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.withTranslation
import com.oqba26.barghkar.R
import com.oqba26.barghkar.data.local.entity.MaterialEntity
import com.oqba26.barghkar.data.local.entity.ProjectEntity
import saman.zamani.persiandate.PersianDate
import saman.zamani.persiandate.PersianDateFormat
import java.io.File
import java.io.FileOutputStream

object InvoiceExporter {

    private fun getCurrentDate(): String {
        return PersianDateFormat("yyyy/MM/dd").format(PersianDate())
    }

    fun generateTextInvoice(project: ProjectEntity, materials: List<MaterialEntity>, useEnglish: Boolean = false): String {
        val sb = StringBuilder()
        sb.append("فاکتور پروژه: ${project.name}\n")
        sb.append("تاریخ: ${getCurrentDate()}\n")
        sb.append("توضیحات: ${project.description}\n")
        sb.append("---------------------------\n")
        var totalMaterial = 0L
        materials.forEach {
            val itemTotal = it.quantity.toLong() * it.pricePerUnit
            sb.append("${it.name}: ${NumberUtils.formatNumber(it.quantity, useEnglish)} ${it.unit} × ${NumberUtils.formatPrice(it.pricePerUnit, useEnglish)} = ${NumberUtils.formatPrice(itemTotal, useEnglish)} تومان\n")
            totalMaterial += itemTotal
        }
        sb.append("---------------------------\n")
        sb.append("جمع متریال: ${NumberUtils.formatPrice(totalMaterial, useEnglish)} تومان\n")
        sb.append("دستمزد: ${NumberUtils.formatPrice(project.totalWage, useEnglish)} تومان\n")
        sb.append("جمع کل: ${NumberUtils.formatPrice(totalMaterial + project.totalWage, useEnglish)} تومان\n")
        sb.append("\nصادر شده توسط برنامه برق‌کار")
        return sb.toString()
    }

    fun shareTextInvoice(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "ارسال فاکتور"))
    }

    fun exportPdfInvoice(context: Context, project: ProjectEntity, materials: List<MaterialEntity>, useEnglish: Boolean = false) {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        
        val textPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 14f
            typeface = ResourcesCompat.getFont(context, R.font.vazirmatn_regular)
        }

        var y = 50f
        val margin = 50f
        val contentWidth = pageWidth - (2 * margin).toInt()

        drawRtlText(canvas, "فاکتور پروژه: ${project.name}", margin, y, contentWidth, textPaint)
        y += 30f
        drawRtlText(canvas, "تاریخ: ${getCurrentDate()}", margin, y, contentWidth, textPaint)
        y += 40f
        
        drawRtlText(canvas, "توضیحات: ${project.description}", margin, y, contentWidth, textPaint)
        y += 40f

        textPaint.strokeWidth = 1f
        canvas.drawLine(margin, y, pageWidth - margin, y, textPaint)
        y += 20f

        var totalMaterial = 0L
        materials.forEach {
            val itemTotal = it.quantity.toLong() * it.pricePerUnit
            val line = "${it.name}: ${NumberUtils.formatNumber(it.quantity, useEnglish)} ${it.unit} × ${NumberUtils.formatPrice(it.pricePerUnit, useEnglish)} = ${NumberUtils.formatPrice(itemTotal, useEnglish)} تومان"
            drawRtlText(canvas, line, margin, y, contentWidth, textPaint)
            totalMaterial += itemTotal
            y += 30f
        }

        y += 20f
        canvas.drawLine(margin, y, pageWidth - margin, y, textPaint)
        y += 30f

        drawRtlText(canvas, "جمع متریال: ${NumberUtils.formatPrice(totalMaterial, useEnglish)} تومان", margin, y, contentWidth, textPaint)
        y += 30f
        drawRtlText(canvas, "دستمزد: ${NumberUtils.formatPrice(project.totalWage, useEnglish)} تومان", margin, y, contentWidth, textPaint)
        y += 40f
        
        textPaint.textSize = 18f
        textPaint.isFakeBoldText = true
        drawRtlText(canvas, "جمع کل: ${NumberUtils.formatPrice(totalMaterial + project.totalWage, useEnglish)} تومان", margin, y, contentWidth, textPaint)

        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "invoice_${project.id}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            shareFile(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        pdfDocument.close()
    }

    @Suppress("SameParameterValue")
    private fun drawRtlText(canvas: Canvas, text: String, x: Float, y: Float, width: Int, paint: TextPaint) {
        val builder = StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_OPPOSITE) // RTL alignment
            .setLineSpacing(0f, 1f)
            .setIncludePad(true)
        
        val layout = builder.build()
        canvas.withTranslation(x, y) {
            layout.draw(this)
        }
    }

    private fun shareFile(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "اشتراک فاکتور PDF"))
    }
}
