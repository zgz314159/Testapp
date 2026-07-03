package com.example.testapp.uicommon.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

fun resolveSessionReadingSectionColors(
    tone: AnalysisSectionTone,
    darkTheme: Boolean
): AnalysisSectionColors {
    val t = SessionReadingSectionTokens
    return if (darkTheme) {
        when (tone) {
            AnalysisSectionTone.Explanation -> AnalysisSectionColors(
                t.explanationContainerDark,
                t.explanationContentDark
            )
            AnalysisSectionTone.Note -> AnalysisSectionColors(
                t.noteContainerDark,
                t.noteContentDark
            )
            AnalysisSectionTone.DeepSeek -> AnalysisSectionColors(
                t.deepSeekContainerDark,
                t.deepSeekContentDark
            )
            AnalysisSectionTone.Spark -> AnalysisSectionColors(
                t.sparkContainerDark,
                t.sparkContentDark
            )
            AnalysisSectionTone.Baidu -> AnalysisSectionColors(
                t.baiduContainerDark,
                t.baiduContentDark
            )
        }
    } else {
        when (tone) {
            AnalysisSectionTone.Explanation -> AnalysisSectionColors(
                t.explanationContainerLight,
                t.explanationContentLight
            )
            AnalysisSectionTone.Note -> AnalysisSectionColors(
                t.noteContainerLight,
                t.noteContentLight
            )
            AnalysisSectionTone.DeepSeek -> AnalysisSectionColors(
                t.deepSeekContainerLight,
                t.deepSeekContentLight
            )
            AnalysisSectionTone.Spark -> AnalysisSectionColors(
                t.sparkContainerLight,
                t.sparkContentLight
            )
            AnalysisSectionTone.Baidu -> AnalysisSectionColors(
                t.baiduContainerLight,
                t.baiduContentLight
            )
        }
    }
}

@Composable
fun sessionReadingSectionColors(tone: AnalysisSectionTone): AnalysisSectionColors =
    resolveSessionReadingSectionColors(tone, isSystemInDarkTheme())
