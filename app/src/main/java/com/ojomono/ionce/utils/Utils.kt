package com.ojomono.ionce.utils

import com.google.android.gms.tasks.Task

object Utils {

    /**
     * If [task] is not null, run [continuation] as continuation to [task], else run it in new task.
     */
    fun <TResult, TContinuationResult> continueWithTaskOrInNew(
        task: Task<TResult>?,
        continuation: (Task<TResult>?) -> Task<TContinuationResult>?
    ) = task?.continueWithTask { continuation.invoke(task) } ?: continuation.invoke(null)

}