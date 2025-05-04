package de.markusfisch.android.binaryeye.os

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import de.markusfisch.android.binaryeye.R
import de.markusfisch.android.binaryeye.activity.MainActivity
import de.markusfisch.android.binaryeye.net.createEncodeDeeplink
import de.markusfisch.android.binaryeye.widget.toast

fun addShortcutToShowEncode(context: Context, format: String, content: String) {
    val deeplink = createEncodeDeeplink(
        format = format,
        content = content,
        execute = true,
        external = false,
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val shortcutManager = context.getSystemService(ShortcutManager::class.java)

        val shortcut = ShortcutInfo.Builder(context, "encode_shortcut_${System.currentTimeMillis()}")
            .setShortLabel("BinEye Code")
            .setLongLabel("BinEye Code")
            .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse(deeplink)
            })
            .build()

        shortcutManager.requestPinShortcut(shortcut, null)
        context.toast(R.string.shortcut_added)
    } else {
        // Fallback для старых версий Android
        val shortcutIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(deeplink)
        }

        val addIntent = Intent().apply {
            putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
            putExtra(Intent.EXTRA_SHORTCUT_NAME, "BinEye Code")
            putExtra(
                Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(context, R.mipmap.ic_launcher))
            action = "com.android.launcher.action.INSTALL_SHORTCUT"
        }

        context.sendBroadcast(addIntent)
        context.toast(R.string.shortcut_added)
    }
}
