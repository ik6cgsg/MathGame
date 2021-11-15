package mathhelper.games.matify

import android.app.Application
import mathhelper.games.matify.common.ConnectionChecker
import mathhelper.games.matify.common.Storage
import mathhelper.games.matify.common.Request
import java.lang.ref.WeakReference

class Matify: Application() {
    override fun onCreate() {
        super.onCreate()
        Storage.shared.context = WeakReference(this)
        ConnectionChecker.shared.context = WeakReference(this)
        ConnectionChecker.shared.subscribe(Request.Companion)
    }
}