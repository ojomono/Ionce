package com.ojomono.ionce.utils

import com.google.android.gms.tasks.Task

object Utils {

    /**
     * If [task] is not null, run [continuation] as continuation to [task], else run it in new task.
     */
    fun <ResultT, TContinuationResult> continueWithTaskOrInNew(
        task: Task<ResultT>?,
        continuation: (Task<ResultT>?) -> Task<TContinuationResult>?
    ) = task?.continueWithTask { continuation.invoke(task) } ?: continuation.invoke(null)

}