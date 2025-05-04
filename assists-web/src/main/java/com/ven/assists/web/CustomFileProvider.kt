package com.ven.assists.web

import android.app.Application
import androidx.core.content.FileProvider
import com.ven.assists.utils.CoroutineWrapper
import kotlinx.coroutines.delay

class CustomFileProvider : FileProvider() {
    override fun onCreate(): Boolean {
        val applicationContext = context?.applicationContext
        if (applicationContext is Application) {
            CoroutineWrapper.launch {
                val clearIds = arrayListOf<String>()
                while (true) {
                    runCatching {
                        clearIds.clear()
                        NodeCacheManager.cache.forEach { item ->
                            item.value.let {
                                val node = it.get()
                                if (node == null || !node.refresh()) {
                                    clearIds.add(item.key)
                                }
                            }
                        }

                        clearIds.forEach { NodeCacheManager.cache.remove(it) }
                    }

                    delay(1000)
                }
            }
        }
        return super.onCreate()
    }
}