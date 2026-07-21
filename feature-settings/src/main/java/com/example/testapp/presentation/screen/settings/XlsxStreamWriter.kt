package com.example.testapp.presentation.screen.settings

import java.io.BufferedOutputStream
import java.io.BufferedWriter
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Writer
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * 最小 XLSX 流式写出器：ZipOutputStream + 手写 OOXML，全程流式、恒定内存。
 *
 * 为什么不用 POI 写出：
 * - `XSSFWorkbook` 整本工作簿驻留堆内，大题库导出触发 OOM（growth limit 368MB）；
 * - `SXSSFWorkbook` 创建 Sheet 时急切初始化 AutoSizeColumnTracker，其静态块依赖
 *   `java.awt.font.FontRenderContext`，Android 无 AWT 必抛 NoClassDefFoundError
 *   （POI bug 65260，5.2.4+ 仅收窄 catch，未修复 Android 场景）。
 *
 * 能力范围：字符串单元格（inline string）+ 整行红字高亮，正好覆盖导出需求。
 */
internal object XlsxStreamWriter {

    /** Excel 单 cell 文本上限。 */
    private const val MAX_CELL_TEXT_LENGTH = 32_767
    private const val MAIN_NS = "http://schemas.openxmlformats.org/spreadsheetml/2006/main"
    private const val REL_NS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"
    private const val XML_DECL = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
    private const val IO_BUFFER_SIZE = 64 * 1024

    /**
     * @param sheets sheet 名（须已按 Excel 规则消毒且互不重复）→ 行 × 列文本
     * @param highlightedRowsBySheet sheet 名 → 需整行红字的 0-based 行号集合
     */
    fun write(
        out: OutputStream,
        sheets: Map<String, List<List<String>>>,
        highlightedRowsBySheet: Map<String, Set<Int>> = emptyMap(),
    ) {
        val sheetNames = sheets.keys.toList()
        val zip = ZipOutputStream(BufferedOutputStream(out, IO_BUFFER_SIZE)).apply {
            // 题库文本压缩收益有限；优先导出速度，文件体积略增。
            setLevel(Deflater.BEST_SPEED)
        }
        val w = BufferedWriter(OutputStreamWriter(zip, Charsets.UTF_8), IO_BUFFER_SIZE)

        writeEntry(zip, w, "[Content_Types].xml") { contentTypesXml(sheetNames.size) }
        writeEntry(zip, w, "_rels/.rels") { relsXml() }
        writeEntry(zip, w, "xl/workbook.xml") { workbookXml(sheetNames) }
        writeEntry(zip, w, "xl/_rels/workbook.xml.rels") { workbookRelsXml(sheetNames.size) }
        writeEntry(zip, w, "xl/styles.xml") { stylesXml() }
        sheetNames.forEachIndexed { index, name ->
            writeEntry(zip, w, "xl/worksheets/sheet${index + 1}.xml") {
                sheetXml(sheets.getValue(name), highlightedRowsBySheet[name].orEmpty())
            }
        }

        w.flush()
        zip.finish()
        zip.flush()
    }

    private inline fun writeEntry(zip: ZipOutputStream, w: Writer, name: String, block: Writer.() -> Unit) {
        zip.putNextEntry(ZipEntry(name))
        w.block()
        w.flush()
        zip.closeEntry()
    }

    private fun Writer.contentTypesXml(sheetCount: Int) {
        write(XML_DECL)
        write("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">")
        write("<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>")
        write("<Default Extension=\"xml\" ContentType=\"application/xml\"/>")
        write("<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>")
        for (i in 1..sheetCount) {
            write("<Override PartName=\"/xl/worksheets/sheet$i.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>")
        }
        write("<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>")
        write("</Types>")
    }

    private fun Writer.relsXml() {
        write(XML_DECL)
        write("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">")
        write("<Relationship Id=\"rId1\" Type=\"$REL_NS/officeDocument\" Target=\"xl/workbook.xml\"/>")
        write("</Relationships>")
    }

    private fun Writer.workbookXml(sheetNames: List<String>) {
        write(XML_DECL)
        write("<workbook xmlns=\"$MAIN_NS\" xmlns:r=\"$REL_NS\"><sheets>")
        sheetNames.forEachIndexed { index, name ->
            write("<sheet name=\"${escapeXml(name)}\" sheetId=\"${index + 1}\" r:id=\"rId${index + 1}\"/>")
        }
        write("</sheets></workbook>")
    }

    private fun Writer.workbookRelsXml(sheetCount: Int) {
        write(XML_DECL)
        write("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">")
        for (i in 1..sheetCount) {
            write("<Relationship Id=\"rId$i\" Type=\"$REL_NS/worksheet\" Target=\"worksheets/sheet$i.xml\"/>")
        }
        write("<Relationship Id=\"rId${sheetCount + 1}\" Type=\"$REL_NS/styles\" Target=\"styles.xml\"/>")
        write("</Relationships>")
    }

    /** 样式表：fontId 0 = 默认，fontId 1 = 红字；cellXfs 1 即"已编辑行"高亮样式。 */
    private fun Writer.stylesXml() {
        write(XML_DECL)
        write("<styleSheet xmlns=\"$MAIN_NS\">")
        write("<fonts count=\"2\">")
        write("<font><sz val=\"11\"/><name val=\"Calibri\"/></font>")
        write("<font><sz val=\"11\"/><color rgb=\"FFFF0000\"/><name val=\"Calibri\"/></font>")
        write("</fonts>")
        write("<fills count=\"2\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill></fills>")
        write("<borders count=\"1\"><border/></borders>")
        write("<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\"/></cellStyleXfs>")
        write("<cellXfs count=\"2\">")
        write("<xf numFmtId=\"0\" fontId=\"0\" xfId=\"0\"/>")
        write("<xf numFmtId=\"0\" fontId=\"1\" xfId=\"0\" applyFont=\"1\"/>")
        write("</cellXfs>")
        write("</styleSheet>")
    }

    private fun Writer.sheetXml(rows: List<List<String>>, highlightedRows: Set<Int>) {
        val columnRefs = List(rows.maxOfOrNull { it.size } ?: 0, ::columnRef)
        write(XML_DECL)
        write("<worksheet xmlns=\"$MAIN_NS\"><sheetData>")
        rows.forEachIndexed { rowIndex, columns ->
            val styleAttr = if (rowIndex in highlightedRows) " s=\"1\"" else ""
            write("<row r=\"${rowIndex + 1}\">")
            columns.forEachIndexed { columnIndex, cellValue ->
                // 稀疏行：空 cell 不写节点（r 属性定位列）。题库模板 165 列大多为空，
                // 全量写出会让 sheet XML 膨胀到 ~77MB，导入端 POI DOM 解析直接 OOM。
                val text = sanitizeCellText(cellValue)
                if (text.isEmpty()) return@forEachIndexed
                write("<c r=\"${columnRefs[columnIndex]}${rowIndex + 1}\" t=\"inlineStr\"$styleAttr>")
                write("<is><t xml:space=\"preserve\">")
                write(escapeXml(text))
                write("</t></is></c>")
            }
            write("</row>")
        }
        write("</sheetData></worksheet>")
    }

    /** 0-based 列号 → Excel 列字母（0→A, 25→Z, 26→AA…）。 */
    private fun columnRef(index: Int): String {
        var i = index
        val sb = StringBuilder()
        while (i >= 0) {
            sb.append('A' + i % 26)
            i = i / 26 - 1
        }
        return sb.reverse().toString()
    }

    /** 剔除 XML 1.0 非法控制字符并截断到 Excel 单 cell 上限。 */
    private fun sanitizeCellText(value: String): String {
        val limit = value.length.coerceAtMost(MAX_CELL_TEXT_LENGTH)
        var requiresCopy = value.length > limit
        if (!requiresCopy) {
            for (index in 0 until limit) {
                val ch = value[index]
                if (ch < ' ' && ch != '\t' && ch != '\n' && ch != '\r') {
                    requiresCopy = true
                    break
                }
            }
        }
        if (!requiresCopy) return value

        return buildString(limit) {
            for (index in 0 until limit) {
                val ch = value[index]
                if (ch >= ' ' || ch == '\t' || ch == '\n' || ch == '\r') append(ch)
            }
        }
    }

    private fun escapeXml(value: String): String {
        if (value.none { it == '&' || it == '<' || it == '>' || it == '"' || it == '\'' }) return value
        val sb = StringBuilder(value.length + 16)
        for (ch in value) {
            when (ch) {
                '&' -> sb.append("&amp;")
                '<' -> sb.append("&lt;")
                '>' -> sb.append("&gt;")
                '"' -> sb.append("&quot;")
                '\'' -> sb.append("&apos;")
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }
}
