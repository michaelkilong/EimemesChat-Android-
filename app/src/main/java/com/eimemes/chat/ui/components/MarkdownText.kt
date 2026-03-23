package com.eimemes.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Syntax highlight token colors ──────────────────────────────────
private val SynKeyword  = Color(0xFFCC99FF)  // purple  — if, for, return, class...
private val SynString   = Color(0xFF98C379)  // green   — "strings"
private val SynNumber   = Color(0xFFD19A66)  // orange  — 42, 3.14
private val SynComment  = Color(0xFF5C6370)  // grey    — // comments
private val SynFunction = Color(0xFF61AFEF)  // blue    — function names
private val SynType     = Color(0xFFE5C07B)  // yellow  — Int, String, Bool

private val KEYWORDS = setOf(
    "if","else","for","while","do","return","fun","val","var","let","const",
    "class","object","interface","enum","struct","import","from","export",
    "new","this","super","null","nil","true","false","undefined","void",
    "public","private","protected","static","final","override","abstract",
    "try","catch","finally","throw","async","await","yield","in","of","is",
    "def","lambda","pass","with","as","and","or","not","elif","print",
    "function","switch","case","break","continue","default","typeof","instanceof"
)

private val TYPES = setOf(
    "Int","Long","Float","Double","Boolean","String","Char","Byte","Short",
    "List","Map","Set","Array","Any","Unit","Nothing","Number",
    "int","float","double","bool","str","list","dict","tuple","bytes",
    "char","void","auto","unsigned","signed"
)

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    baseColor: Color = MaterialTheme.colorScheme.onSurface,
    onCitationClick: ((Int) -> Unit)? = null
) {
    val codeBg    = MaterialTheme.colorScheme.surfaceVariant
    val codeColor = Color(0xFF4FA8FF)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val lines = text.lines()
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            when {
                // ── Fenced code block ───────────────────────────────
                line.trimStart().startsWith("```") -> {
                    val lang = line.trimStart().removePrefix("```").trim().lowercase()
                    val codeLines = mutableListOf<String>()
                    i++
                    while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                        codeLines.add(lines[i]); i++
                    }
                    CodeBlock(code = codeLines.joinToString("\n"), lang = lang, bg = codeBg)
                }

                // ── Headers ─────────────────────────────────────────
                line.startsWith("### ") -> Text(
                    parseInline(line.removePrefix("### "), baseColor, codeColor, onCitationClick),
                    fontSize = 15.sp, fontWeight = FontWeight.Bold, lineHeight = 22.sp, color = baseColor
                )
                line.startsWith("## ") -> Text(
                    parseInline(line.removePrefix("## "), baseColor, codeColor, onCitationClick),
                    fontSize = 17.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp, color = baseColor
                )
                line.startsWith("# ") -> Text(
                    parseInline(line.removePrefix("# "), baseColor, codeColor, onCitationClick),
                    fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 26.sp, color = baseColor
                )

                // ── Blockquote ──────────────────────────────────────
                line.startsWith("> ") -> Row(modifier = Modifier.fillMaxWidth()) {
                    Box(Modifier.width(3.dp).height(20.dp).clip(RoundedCornerShape(2.dp)).background(codeColor))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        parseInline(line.removePrefix("> "), baseColor.copy(alpha = 0.7f), codeColor, onCitationClick),
                        fontSize = 14.sp, fontStyle = FontStyle.Italic, lineHeight = 20.sp
                    )
                }

                // ── Unordered list ──────────────────────────────────
                line.matches(Regex("^[\\-\\*\\+] .+")) -> Row {
                    Text("•  ", color = codeColor, fontSize = 14.sp)
                    Text(
                        parseInline(line.drop(2), baseColor, codeColor, onCitationClick),
                        fontSize = 14.sp, lineHeight = 21.sp
                    )
                }

                // ── Ordered list ────────────────────────────────────
                line.matches(Regex("^\\d+\\. .+")) -> {
                    val num = line.substringBefore(". ")
                    Row {
                        Text("$num.  ", color = codeColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(
                            parseInline(line.substringAfter(". "), baseColor, codeColor, onCitationClick),
                            fontSize = 14.sp, lineHeight = 21.sp
                        )
                    }
                }

                // ── Horizontal rule ─────────────────────────────────
                line.matches(Regex("^[-_*]{3,}$")) ->
                    Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline))

                // ── Blank ───────────────────────────────────────────
                line.isBlank() -> Spacer(Modifier.height(4.dp))

                // ── Normal paragraph ────────────────────────────────
                else -> Text(
                    parseInline(line, baseColor, codeColor, onCitationClick),
                    fontSize = 15.sp, lineHeight = 23.sp
                )
            }
            i++
        }
    }
}

// ── Syntax-highlighted code block ──────────────────────────────────
@Composable
private fun CodeBlock(code: String, lang: String, bg: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E1E2E))
            .border(1.dp, Color(0xFF2A2A3A), RoundedCornerShape(8.dp))
    ) {
        // Language label
        if (lang.isNotEmpty()) {
            Text(
                lang,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                fontSize  = 10.sp,
                color     = Color(0xFF5C6370),
                fontFamily = FontFamily.Monospace
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text       = buildSyntaxAnnotated(code, lang),
                fontFamily = FontFamily.Monospace,
                fontSize   = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

private fun buildSyntaxAnnotated(code: String, lang: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = code.lines()
        lines.forEachIndexed { idx, line ->
            tokenizeLine(line, lang)
            if (idx < lines.size - 1) append("\n")
        }
    }
}

private fun AnnotatedString.Builder.tokenizeLine(line: String, lang: String) {
    val trimmed = line.trimStart()

    // ── Whole-line comment ──────────────────────────────────────────
    if (trimmed.startsWith("//") || trimmed.startsWith("#") || trimmed.startsWith("--")) {
        withStyle(SpanStyle(color = SynComment)) { append(line) }
        return
    }

    var pos = 0
    while (pos < line.length) {
        val ch = line[pos]

        when {
            // Inline comment
            line.startsWith("//", pos) || (lang in setOf("python","py","shell","sh") && ch == '#') -> {
                withStyle(SpanStyle(color = SynComment)) { append(line.substring(pos)) }
                pos = line.length
            }

            // String literals " or '
            ch == '"' || ch == '\'' -> {
                val quote = ch
                val end = line.indexOf(quote, pos + 1)
                if (end != -1) {
                    withStyle(SpanStyle(color = SynString)) { append(line.substring(pos, end + 1)) }
                    pos = end + 1
                } else {
                    withStyle(SpanStyle(color = SynString)) { append(line.substring(pos)) }
                    pos = line.length
                }
            }

            // Numbers
            ch.isDigit() -> {
                val start = pos
                while (pos < line.length && (line[pos].isDigit() || line[pos] == '.' || line[pos] == 'L' || line[pos] == 'f')) pos++
                withStyle(SpanStyle(color = SynNumber)) { append(line.substring(start, pos)) }
            }

            // Words (keywords, types, identifiers, function calls)
            ch.isLetter() || ch == '_' -> {
                val start = pos
                while (pos < line.length && (line[pos].isLetterOrDigit() || line[pos] == '_')) pos++
                val word = line.substring(start, pos)
                val isFuncCall = pos < line.length && line[pos] == '('
                val color = when {
                    word in KEYWORDS -> SynKeyword
                    word in TYPES    -> SynType
                    isFuncCall       -> SynFunction
                    else             -> Color(0xFFABB2BF) // default identifier
                }
                withStyle(SpanStyle(color = color)) { append(word) }
            }

            else -> {
                withStyle(SpanStyle(color = Color(0xFF56B6C2))) { append(ch.toString()) }
                pos++
            }
        }
    }
}

// ── Inline parser: bold, italic, code, citations [n] ───────────────
private fun parseInline(
    text: String,
    textColor: Color,
    codeColor: Color,
    onCitationClick: ((Int) -> Unit)?
): AnnotatedString = buildAnnotatedString {
    var pos = 0
    while (pos < text.length) {
        when {
            // Bold+Italic ***
            text.startsWith("***", pos) -> {
                val end = text.indexOf("***", pos + 3)
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, color = textColor)) {
                        append(text.substring(pos + 3, end))
                    }
                    pos = end + 3
                } else { withStyle(SpanStyle(color = textColor)) { append(text[pos]) }; pos++ }
            }
            // Bold **
            text.startsWith("**", pos) -> {
                val end = text.indexOf("**", pos + 2)
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = textColor)) {
                        append(text.substring(pos + 2, end))
                    }
                    pos = end + 2
                } else { withStyle(SpanStyle(color = textColor)) { append(text[pos]) }; pos++ }
            }
            // Italic _
            text.startsWith("_", pos) && !text.startsWith("__", pos) -> {
                val end = text.indexOf("_", pos + 1)
                if (end != -1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = textColor)) {
                        append(text.substring(pos + 1, end))
                    }
                    pos = end + 1
                } else { withStyle(SpanStyle(color = textColor)) { append(text[pos]) }; pos++ }
            }
            // Italic *
            text[pos] == '*' -> {
                val end = text.indexOf('*', pos + 1)
                if (end != -1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = textColor)) {
                        append(text.substring(pos + 1, end))
                    }
                    pos = end + 1
                } else { withStyle(SpanStyle(color = textColor)) { append(text[pos]) }; pos++ }
            }
            // Inline code `
            text[pos] == '`' -> {
                val end = text.indexOf('`', pos + 1)
                if (end != -1) {
                    withStyle(SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        color      = codeColor,
                        background = codeColor.copy(alpha = 0.12f),
                        fontSize   = 13.sp
                    )) { append(text.substring(pos + 1, end)) }
                    pos = end + 1
                } else { withStyle(SpanStyle(color = textColor)) { append(text[pos]) }; pos++ }
            }
            // Strikethrough ~~
            text.startsWith("~~", pos) -> {
                val end = text.indexOf("~~", pos + 2)
                if (end != -1) {
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough, color = textColor.copy(alpha = 0.6f))) {
                        append(text.substring(pos + 2, end))
                    }
                    pos = end + 2
                } else { withStyle(SpanStyle(color = textColor)) { append(text[pos]) }; pos++ }
            }
            // Citation [n] — tappable bubble
            text[pos] == '[' -> {
                val close = text.indexOf(']', pos + 1)
                val inner = if (close != -1) text.substring(pos + 1, close) else null
                val num   = inner?.toIntOrNull()
                if (num != null) {
                    // Render as blue superscript-style tag
                    withStyle(SpanStyle(
                        color      = codeColor,
                        background = codeColor.copy(alpha = 0.15f),
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold
                    )) { append("[$num]") }
                    // Attach click annotation
                    addStringAnnotation("citation", num.toString(), pos, pos + (close - pos) + 1)
                    pos = close + 1
                } else { withStyle(SpanStyle(color = textColor)) { append(text[pos]) }; pos++ }
            }
            else -> { withStyle(SpanStyle(color = textColor)) { append(text[pos]) }; pos++ }
        }
    }
}
