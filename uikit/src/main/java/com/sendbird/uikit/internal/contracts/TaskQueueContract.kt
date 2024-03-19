package com.sendbird.uikit.internal.contracts

import com.sendbird.uikit.internal.tasks.JobResultTask
import com.sendbird.uikit.internal.tasks.JobTask
import java.util.concurrent.Future

internal interface TaskQueueContract {
    fun <T> addTask(task: JobTask<T>): Future<T>
    fun <T> addTask(task: JobResultTask<T>): Future<T>
}
