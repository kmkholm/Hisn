package app.hisn.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import app.hisn.MainActivity
import app.hisn.R
import app.hisn.data.Db

/**
 * ودجت الشاشة الرئيسية: الرقم الذي لا ينقص أبدًا، بلا فتح التطبيق.
 * يقرأ من قاعدة البيانات مباشرة ويتحدّث كل نصف ساعة ومع كل تغيير داخل التطبيق.
 */
class CounterWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { update(context, manager, it) }
    }

    companion object {
        fun refreshAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context) ?: return
            val ids = mgr.getAppWidgetIds(ComponentName(context, CounterWidget::class.java))
            ids.forEach { update(context, mgr, it) }
        }

        private fun update(context: Context, mgr: AppWidgetManager, id: Int) {
            val habit = runCatching { Db(context).habits().firstOrNull() }.getOrNull()
            val views = RemoteViews(context.packageName, R.layout.widget_counter)
            views.setTextViewText(R.id.w_days, "${habit?.totalCleanDays() ?: 0}")
            views.setTextViewText(
                R.id.w_label,
                if (habit == null) "افتح حِصن لتبدأ" else "يومًا نظيفًا · ${habit.displayName}"
            )
            views.setTextViewText(
                R.id.w_streak,
                if (habit == null) "" else
                    "السلسلة ${habit.currentStreakDays()} · القياسي ${habit.bestStreakDays()}"
            )
            val open = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.w_root, open)
            mgr.updateAppWidget(id, views)
        }
    }
}
