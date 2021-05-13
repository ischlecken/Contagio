package de.contagio.core.usecase

import de.brendamour.jpasskit.PKBarcode
import de.brendamour.jpasskit.PKLocation
import de.brendamour.jpasskit.PKPass
import de.brendamour.jpasskit.enums.PKBarcodeFormat
import java.nio.charset.Charset

@Suppress("unused")
fun PKPass.addLocation(latitude: Double, longitude: Double) {
    val location = PKLocation()
    location.latitude = latitude
    location.longitude = longitude

    this.locations = listOf(location)
}

fun PKPass.addBarcode(message: String) {
    val barcode = PKBarcode()
    barcode.format = PKBarcodeFormat.PKBarcodeFormatQR
    barcode.message = message
    barcode.messageEncoding = Charset.forName("iso-8859-1")

    this.barcodes = listOf(barcode)
}
