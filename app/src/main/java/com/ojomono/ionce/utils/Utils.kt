package com.ojomono.ionce.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.ojomono.ionce.R

object Utils {

    /**
     * If [task] is not null, run [continuation] as continuation to [task], else run it in new task.
     */
    fun <TResult, TContinuationResult> continueWithTaskOrInNew(
        task: Task<TResult>?,
        continuation: (Task<TResult>?) -> Task<TContinuationResult>?
    ) = task?.continueWithTask { continuation.invoke(task) } ?: continuation.invoke(null)

    /**
     * Get a default CircularProgressDrawable for [context].
     */
    fun getCircularProgressDrawable(context: Context) =
        androidx.swiperefreshlayout.widget.CircularProgressDrawable(context).apply {
            setStyle(androidx.swiperefreshlayout.widget.CircularProgressDrawable.LARGE)
            setColorSchemeColors(ContextCompat.getColor(context, R.color.colorAccent))
            start()
        }
}