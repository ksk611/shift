package com.example.cspi

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// 매일 자정에 실행 → 내일 알람 자동 설정
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,  // 폰 재시작 시
            "com.example.cspi.SET_DAILY_ALARM" -> {  // 자정 트리거
                AlarmScheduler.scheduleNextDayAlarm(context)
                // 다음날 자정에 다시 실행되도록 재등록
                DailyAlarmManager.scheduleMidnightTrigger(context)
            }
        }
    }
}

// 매일 자정 트리거 관리
object DailyAlarmManager {
    private const val REQUEST_CODE = 9999

    fun scheduleMidnightTrigger(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.cspi.SET_DAILY_ALARM"
        }
        val pi = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 오늘 자정 + 1분 (00:01)
        val cal = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.DAY_OF_YEAR, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 1)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                cal.timeInMillis,
                pi
            )
        } else {
            alarmManager.setExact(
                android.app.AlarmManager.RTC_WAKEUP,
                cal.timeInMillis,
                pi
            )
        }
    }

    fun cancelMidnightTrigger(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.cspi.SET_DAILY_ALARM"
        }
        val pi = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pi)
    }
}
