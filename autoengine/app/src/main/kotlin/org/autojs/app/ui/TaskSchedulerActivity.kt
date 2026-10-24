package org.autojs.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.autojs.app.R

class TaskSchedulerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_scheduler)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "定时任务"
    }
}
