// "Suppress 'UnusedImport' for file ${file}" "true"

import<caret> java.io.*

// K1_TOOL: org.jetbrains.kotlin.idea.inspections.KotlinUnusedImportInspection
// K2_TOOL: org.jetbrains.kotlin.idea.k2.codeinsight.inspections.KotlinUnusedImportInspection
// FUS_K2_QUICKFIX_NAME: com.intellij.codeInspection.SuppressIntentionActionFromFix
// FUS_QUICKFIX_NAME: com.intellij.codeInspection.SuppressIntentionActionFromFix