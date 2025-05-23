package de.markusfisch.android.binaryeye.database

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.text.format.DateFormat
import de.markusfisch.android.zxingcpp.ZxingCpp.BarcodeFormat
import de.markusfisch.android.zxingcpp.ZxingCpp.BitMatrix
import de.markusfisch.android.zxingcpp.ZxingCpp.ContentType
import de.markusfisch.android.zxingcpp.ZxingCpp.Result
import java.util.Locale

data class Scan(
	val text: String,
	val raw: ByteArray?,
	val format: BarcodeFormat,
	val errorCorrectionLevel: String? = null,
	val version: String? = null,
	val dataMask: Int = -1,
	val symbol: BitMatrix? = null,
	val sequenceSize: Int = -1,
	val sequenceIndex: Int = -1,
	val sequenceId: String = "",
	val country: String? = null,
	val addOn: String? = null,
	val price: String? = null,
	val issueNumber: String? = null,
	val dateTime: String = getDateTime(),
	var id: Long = 0L,
	val label: String? = null
) : Parcelable {
	// Needs to be overwritten manually, as ByteArray is an array and
	// this isn't handled well by Kotlin.
	override fun equals(other: Any?): Boolean {
		if (this === other) {
			return true
		}
		if (javaClass != other?.javaClass) {
			return false
		}

		other as Scan

		return id == other.id &&
				dateTime == other.dateTime &&
				text == other.text &&
				((raw == null && other.raw == null) ||
						(raw != null && other.raw != null && raw.contentEquals(other.raw))) &&
				format == other.format &&
				errorCorrectionLevel == other.errorCorrectionLevel &&
				version == other.version &&
				dataMask == other.dataMask &&
				sequenceSize == other.sequenceSize &&
				sequenceIndex == other.sequenceIndex &&
				sequenceId == other.sequenceId &&
				country == other.country &&
				addOn == other.addOn &&
				price == other.price &&
				issueNumber == other.issueNumber
	}

	// Needs to be overwritten manually, as ByteArray is an array and
	// this isn't handled well by Kotlin.
	override fun hashCode(): Int {
		var result = id.hashCode()
		result = 31 * result + dateTime.hashCode()
		result = 31 * result + text.hashCode()
		result = 31 * result + (raw?.contentHashCode() ?: 0)
		result = 31 * result + format.hashCode()
		result = 31 * result + (errorCorrectionLevel?.hashCode() ?: 0)
		result = 31 * result + version.hashCode()
		result = 31 * result + dataMask
		result = 31 * result + sequenceSize
		result = 31 * result + sequenceIndex
		result = 31 * result + sequenceId.hashCode()
		result = 31 * result + (country?.hashCode() ?: 0)
		result = 31 * result + (addOn?.hashCode() ?: 0)
		result = 31 * result + (price?.hashCode() ?: 0)
		result = 31 * result + (issueNumber?.hashCode() ?: 0)
		return result
	}

	private constructor(parcel: Parcel) : this(
		text = parcel.readString() ?: "",
		raw = parcel.readSizedByteArray(),
		format = BarcodeFormat.valueOf(parcel.readString() ?: ""),
		errorCorrectionLevel = parcel.readString(),
		version = parcel.readString(),
		dataMask = parcel.readInt(),
		symbol = parcel.readBitMatrix(),
		sequenceSize = parcel.readInt(),
		sequenceIndex = parcel.readInt(),
		sequenceId = parcel.readString() ?: "",
		country = parcel.readString(),
		addOn = parcel.readString(),
		price = parcel.readString(),
		issueNumber = parcel.readString(),
		dateTime = parcel.readString() ?: "",
		id = parcel.readLong(),
		label = parcel.readString()
	)

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.apply {
			writeString(text)
			writeSizedByteArray(raw)
			writeString(format.name)
			writeString(errorCorrectionLevel)
			writeString(version)
			writeInt(dataMask)
			writeBitMatrix(symbol)
			writeInt(sequenceSize)
			writeInt(sequenceIndex)
			writeString(sequenceId)
			writeString(country)
			writeString(addOn)
			writeString(price)
			writeString(issueNumber)
			writeString(dateTime)
			writeLong(id)
			writeString(label)
		}
	}

	override fun describeContents() = 0

	companion object {
		@JvmField
		val CREATOR = object : Parcelable.Creator<Scan> {
			override fun createFromParcel(parcel: Parcel) = Scan(parcel)
			override fun newArray(size: Int) = arrayOfNulls<Scan>(size)
		}
	}
}

fun Result.toScan(): Scan {
	return Scan(
		when (contentType) {
			ContentType.GS1,
			ContentType.ISO15434,
			ContentType.TEXT -> text

			else -> "" // Show as binary.
		},
		rawBytes,
		format,
		ecLevel,
		version,
		dataMask,
		symbol,
		sequenceSize,
		sequenceIndex,
		sequenceId,
		gtin?.country,
		gtin?.addOn,
		gtin?.price,
		gtin?.issueNumber
	)
}

private fun getDateTime(
	time: Long = System.currentTimeMillis()
) = DateFormat.format(
	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
		// Up to API level 17, 'k' was interpreted as 'H', which wasn't
		// implemented until API level 18.
		"yyyy-MM-dd kk:mm:ss"
	} else {
		"yyyy-MM-dd HH:mm:ss"
	},
	time
).toString() + String.format(
	Locale.getDefault(),
	":%03d",
	time % 1000
)

private fun Parcel.writeSizedByteArray(array: ByteArray?) {
	val size = array?.size ?: 0
	writeInt(size)
	if (size > 0) {
		writeByteArray(array)
	}
}

private fun Parcel.readSizedByteArray(): ByteArray? {
	val size = readInt()
	return if (size > 0) {
		val array = ByteArray(size)
		readByteArray(array)
		array
	} else {
		null
	}
}

private fun Parcel.writeBitMatrix(bm: BitMatrix?) {
	writeInt(bm?.width ?: 0)
	bm?.let {
		writeInt(bm.height)
		writeSizedByteArray(bm.data)
	}
}

private fun Parcel.readBitMatrix(): BitMatrix? {
	val width = readInt()
	if (width < 1) {
		return null
	}
	val height = readInt()
	val data = readSizedByteArray() ?: return null
	return BitMatrix(width, height, data)
}
