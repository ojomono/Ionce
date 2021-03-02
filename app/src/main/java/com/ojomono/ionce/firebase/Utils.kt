package com.ojomono.ionce.firebase

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks

object Utils {

    object NoSignedInUserException :
        IllegalStateException("This operation requires a signed-in user")

    /**
     * If [task] is not null, run [continuation] as continuation to [task], else run it in new task.
     * If [checkSuccess] is true, [continuation] will run only if [task] succeed.
     */
    fun <TResult, TContinuationResult> continueWithTaskOrInNew(
        task: Task<TResult>?,
        checkSuccess: Boolean = false,
        continuation: (Task<TResult>?) -> Task<TContinuationResult>?
    ) = task?.continueWithTask {
        if (checkSuccess and !task.isSuccessful) Tasks.forCanceled() else continuation.invoke(task)
    } ?: continuation.invoke(null)
}