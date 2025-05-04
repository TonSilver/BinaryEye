package de.markusfisch.android.binaryeye.net

import android.net.Uri

private const val SCHEME = "binaryeye"
private const val HOSTNAME = "markusfisch.de"
private const val ENCODE_PATH = "encode"

data class Deeplink(
	val content: String,
	val format: String?,
	val execute: Boolean,
) {
	
	companion object {

		fun createFromDataString(dataString: String): Deeplink {
			val uri = Uri.parse(dataString)
			return Deeplink(
				content = uri.getQueryParameter("content") ?: "",
				format = uri.getQueryParameter("format")?.uppercase(),
				execute = uri.getQueryParameter("execute")
					.let { it == "" || it.toBoolean() },
			)
		}
	}
}

fun isScanDeeplink(text: String) = listOf(
	"$SCHEME://scan",
	"http://$HOSTNAME/BinaryEye",
	"https://$HOSTNAME/BinaryEye"
).any { text.startsWith(it) }

fun isEncodeDeeplink(text: String) = listOf(
	"$SCHEME://$ENCODE_PATH",
	"http://$HOSTNAME/$ENCODE_PATH",
	"https://$HOSTNAME/$ENCODE_PATH"
).any { text.startsWith(it) }

fun createEncodeDeeplink(
	format: String,
	content: String,
	execute: Boolean,
	external: Boolean,
): String {
	val (scheme, hostname) = if (external) {
		"https" to HOSTNAME
	} else {
		SCHEME to ENCODE_PATH
	}
	val builder = Uri.Builder()
		.scheme(scheme)
		.authority(hostname)
		.appendPath(ENCODE_PATH)
		.appendQueryParameter("format", format)
		.appendQueryParameter("content", content)
	if (execute) {
		builder.appendQueryParameter("execute", "true")
	}
	return builder.build().toString()
}
