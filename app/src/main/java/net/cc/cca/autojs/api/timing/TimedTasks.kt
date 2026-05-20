package net.cc.cca.autojs.api.timing

import net.cc.stardust.execution.ExecutionConfig
import net.cc.cca.timing.TimedTask
import net.cc.cca.timing.TimedTaskManager
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime

object TimedTasks {

    fun daily(path: String, hour: Int, minute: Int) {
        TimedTaskManager.getInstance().addTask(TimedTask.dailyTask(LocalTime(hour, minute), path, ExecutionConfig()))
    }

    fun disposable(path: String, millis: Long) {
        TimedTaskManager.getInstance().addTask(TimedTask.disposableTask(LocalDateTime(millis), path, ExecutionConfig()))
    }

    fun weekly(path: String, millis: Long) {
        //TimedTaskManager.getInstance().addTask(TimedTask.weeklyTask(LocalDateTime(millis), path, ExecutionConfig()))
    }

}