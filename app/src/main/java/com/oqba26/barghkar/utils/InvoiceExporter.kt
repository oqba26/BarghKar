package com.oqba26.barghkar.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.oqba26.barghkar.data.local.entity.MaterialEntity
import com.oqba26.barghkar.data.local.entity.ProjectEntity
import java.io.File
import java.io.FileOutputStream

object InvoiceExporter {
    fun generateTextInvoice(project: ProjectEntity, materials: List<MaterialEntity>): String {
        val sb = StringBuilder()
        sb.append("فاکتور پروژه: ${project.name}\n")
        sb.append("توضیحات: ${project.description}\n")
        sb.append("---------------------------\n")
        var totalMaterial = 0L
        materials.forEach {
            val itemTotal = it.quantity * it.pricePerUnit
            sb.append("${it.name}: ${it.quantity} ${it.unit} × ${it.pricePerUnit} = $itemTotal تومان\n")
            totalMaterial += itemTotal
        }
        sb.append("---------------------------\n")
        sb.append("جمع متریال: $totalMaterial تومان\n")
        sb.append("دستمزد: ${project.totalWage} تومان\n")
        sb.append("جمع کل: ${totalMaterial + project.totalWage} تومان\n")
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

    fun exportPdfInvoice(context: Context, project: ProjectEntity, materials: List<MaterialEntity>) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        paint.color = Color.BLACK
        paint.textSize = 16f
        
        var y = 40f
        canvas.drawText("فاکتور پروژه: ${project.name}", 50f, y, paint)
        y += 30f
        
        var totalMaterial = 0L
        materials.forEach {
            val itemTotal = it.quantity * it.pricePerUnit
            canvas.drawText("${it.name}: ${it.quantity} ${it.unit} × ${it.pricePerUnit} = $itemTotal تومان", 50f, y, paint)
            totalMaterial += itemTotal
            y += 25f
        }
        
        y += 20f
        canvas.drawText("جمع متریال: $totalMaterial تومان", 50f, y, paint)
        y += 25f
        canvas.drawText("دستمزد: ${project.totalWage} تومان", 50f, y, paint)
        y += 25f
        paint.isFakeBoldText = true
        canvas.drawText("جمع کل: ${totalMaterial + project.totalWage} تومان", 50f, y, paint)

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
