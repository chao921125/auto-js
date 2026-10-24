package org.autojs.app

import android.app.Application
import com.tencent.mmkv.MMKV

class AutoJsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
    }
}
