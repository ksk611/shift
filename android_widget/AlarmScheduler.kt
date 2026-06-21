package com.example.cspi

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.AlarmClock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar

object AlarmScheduler {

    // 패턴별 기본 기상 시간 (SharedPreferences에서 읽어옴)
    fun getWakeTime(context: Context, shift: String): Pair<Int, Int>? {
        val prefs = context.getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE)
        return when (shift) {
            "D"  -> {
                val h = prefs.getInt("flutter.alarm_d_hour", 6)
                val m = prefs.getInt("flutter.alarm_d_min", 30)
                Pair(h, m)
            }
            "S"  -> {
                val h = prefs.getInt("flutter.alarm_s_hour", 10)
                val m = prefs.getInt("flutter.alarm_s_min", 0)
                Pair(h, m)
            }
            "G"  -> {
                val h = prefs.getInt("flutter.alarm_g_hour", 21)
                val m = prefs.getInt("flutter.alarm_g_min", 0)
                Pair(h, m)
            }
            "DS" -> {
                val h = prefs.getInt("flutter.alarm_ds_hour", 6)
                val m = prefs.getInt("flutter.alarm_ds_min", 30)
                Pair(h, m)
            }
            else -> null  // 휴무는 알람 없음
        }
    }

    // 폰 기본 알람 앱에 알람 등록
    fun setAlarm(context: Context, hour: Int, minute: Int, label: String) {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, label)
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)  // UI 없이 바로 등록
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    // 내일 근무 기준으로 알람 자동 설정
    fun scheduleNextDayAlarm(context: Context) {
        val prefs = context.getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE)
        val team = prefs.getString("flutter.my_team", "B") ?: "B"
        val alarmEnabled = prefs.getBoolean("flutter.alarm_enabled", true)
        if (!alarmEnabled) return

        val tomorrow = LocalDate.now().plusDays(1)
        val shift = ShiftWidgetHelper.getShift(team, tomorrow)
        val wakeTime = getWakeTime(context, shift) ?: return

        val label = "CSPI ${shift} - ${ShiftWidgetHelper.shiftName(shift)}"
        setAlarm(context, wakeTime.first, wakeTime.second, label)
    }
}
