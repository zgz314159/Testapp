package com.example.testapp.uicommon.design

fun formatPracticeExamTimer(elapsedSeconds: Int): String =
    "%02d:%02d".format(elapsedSeconds / 60, elapsedSeconds % 60)
