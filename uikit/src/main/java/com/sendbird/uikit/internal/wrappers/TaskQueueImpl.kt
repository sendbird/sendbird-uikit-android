package com.sendbird.uikit.internal.wrappers

import com.sendbird.uikit.internal.tasks.JobResultTask
import com.sendbird.uikit.internal.tasks.JobTask
import com.sendbird.uikit.internal.tasks.TaskQueue
import java.util.concurrent.Future

internal class TaskQueueImpl : TaskQueueWrapper {
    override fun <T> addTask(task: JobTask<T>): Future<T> {
        return TaskQueue.addTask(task)
    }

    override fun <T> addTask(task: JobResultTask<T>): Future<T> {
        return TaskQueue.addTask(task)
    }
}
