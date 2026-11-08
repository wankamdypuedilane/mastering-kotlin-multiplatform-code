package hearthseed.platform.app

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import hearthseed.platform.AndroidAppDirectories
import hearthseed.platform.ExportPlanner
import hearthseed.platform.platformInfo

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The entry point owns the Context and builds the implementation.
        val directories = AndroidAppDirectories(applicationContext)
        val plan = ExportPlanner(platformInfo(), directories).plan("inventory")
        setContentView(TextView(this).apply { text = "${plan.header}\n${plan.path}" })
    }
}
