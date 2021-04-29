package de.contagio.core.usecase

import de.brendamour.jpasskit.signing.PKPassTemplateInMemory
import de.contagio.core.domain.entity.IssueStatus
import de.contagio.core.domain.entity.PassImage
import de.contagio.core.domain.entity.PassType
import net.coobird.thumbnailator.Thumbnails
import net.coobird.thumbnailator.filters.Colorize
import org.slf4j.LoggerFactory
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*
import javax.imageio.ImageIO


private val logger = LoggerFactory.getLogger(ContagioPassTemplate::class.java)

class ContagioPassTemplate(
    private val passImage: PassImage,
    private val passType: PassType,
    private val issueStatus: IssueStatus
) : PKPassTemplateInMemory() {

    fun build() {

        addFile(PK_ICON, getResourceStream("icon.png"))
        addFile(PK_ICON_RETINA, getResourceStream("icon@2x.png"))
        addFile(PK_LOGO, getResourceStream("logo.png"))
        addFile(PK_LOGO_RETINA, getResourceStream("logo@2x.png"))

        addFile("pass.strings", Locale.ENGLISH, getResourceStream("en.lproj/pass.strings"))
        addFile("pass.strings", Locale.GERMAN, getResourceStream("de.lproj/pass.strings"))

        val inputImage = ImageIO.read(ByteArrayInputStream(passImage.data))

        when (passType) {
            PassType.COUPON -> {
                createImages(inputImage, 375, 144, "strip")
            }
            PassType.EVENT -> {
                createImages(inputImage, 90, 90, "thumbnail")
                createImages(inputImage, 180, 220, "background")
            }
            else -> {
                createImages(inputImage, 90, 90, "thumbnail")
            }
        }
    }

    private fun createImages(inputImage: BufferedImage, width: Int, height: Int, name: String) {
        resizeImage(inputImage, width, height)?.let {
            addFile("$name.png", it)
        }

        resizeImage(inputImage, 2 * width, 2 * height)?.let {
            addFile("$name@2x.png", it)
        }
    }

    private fun getResourceStream(name: String): InputStream? {
        val path = "/passtemplate/$name"
        val result = ContagioPassTemplate::class.java.getResourceAsStream(path)

        logger.debug("getResourceStream() $path = $result")

        return result
    }

    @Throws(java.lang.Exception::class)
    private fun resizeImage(originalImage: BufferedImage, targetWidth: Int, targetHeight: Int): InputStream? {
        val outputStream = ByteArrayOutputStream()

        var thumbnailBuilder = Thumbnails.of(originalImage)
            .size(targetWidth, targetHeight)
            .outputFormat("PNG")
            .outputQuality(1.0)

        if( issueStatus==IssueStatus.EXPIRED ||issueStatus==IssueStatus.REVOKED)
            thumbnailBuilder = thumbnailBuilder.addFilter(Colorize(Color(255, 0, 0,128)))

        thumbnailBuilder.toOutputStream(outputStream)

        val result = outputStream.toByteArray()

        return if (result.isNotEmpty()) {
            logger.debug("resizeImage($targetWidth,$targetHeight): ${result.size}")

            ByteArrayInputStream(result)
        } else
            null
    }
}

