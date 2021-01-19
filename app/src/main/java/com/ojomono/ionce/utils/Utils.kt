package com.ojomono.ionce.utils

import android.view.View
import com.google.android.gms.tasks.Task
import com.google.android.material.color.MaterialColors
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
     * Get a default CircularProgressDrawable for [view].
     */
    fun getCircularProgressDrawable(view: View) =
        androidx.swiperefreshlayout.widget.CircularProgressDrawable(view.context).apply {
            setStyle(androidx.swiperefreshlayout.widget.CircularProgressDrawable.LARGE)
            setColorSchemeColors(MaterialColors.getColor(view, R.attr.colorSecondary))
            start()
        }
}