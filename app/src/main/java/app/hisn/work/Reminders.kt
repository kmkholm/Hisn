package app.hisn.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.hisn.MainActivity
import app.hisn.R
import app.hisn.data.Prefs
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * التذكير اليومي.
 *
 * العمود الفقري للتطبيق هو العودة اليومية — وبلا إشعار يعتمد كل شيء على
 * ذاكرة المستخدم وحدها. نستخدم طلب عمل واحد بتأخير محسوب حتى ساعة التذكير،
 * وكل تنفيذ يجدول التالي — بدل عمل دوري لا يضمن التوقيت.
 */
object Reminders {

    private const val WORK_NAME = "hisn-daily-reminder"
    const val CHANNEL_ID = "daily"

    fun schedule(context: Context, hour: Int) {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(LocalTime.of(hour.coerceIn(0, 23), 0))
        if (!next.isAfter(now)) next = next.plusDays(1)
        val delay = Duration.between(now, next)

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.daily_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = context.getString(R.string.daily_channel_desc) }
        )
    }
}

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val ctx = applicationContext
        val prefs = Prefs(ctx)

        if (prefs.remindersEnabled) {
            postNotification(ctx)
            Reminders.schedule(ctx, prefs.reminderHour)
        }
        return Result.success()
    }

    private fun postNotification(ctx: Context) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ctx.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return

        Reminders.ensureChannel(ctx)
        val open = PendingIntent.getActivity(
            ctx, 0,
            Intent(ctx, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val n = NotificationCompat.Builder(ctx, Reminders.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("جلستك اليوم في انتظارك")
            .setContentText("دقائق قليلة تحفظ تقدّمك — يوم المسار وتمرين اليوم جاهزان.")
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(ctx).notify(1001, n)
    }
}
