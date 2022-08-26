package dev.haziqkamel.barcodekotlin

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import dev.haziqkamel.barcodekotlin.databinding.ActivityMainBinding
import io.github.g0dkar.qrcode.ErrorCorrectionLevel
import io.github.g0dkar.qrcode.QRCode
import io.github.g0dkar.qrcode.internals.QRCodeRegion
import io.github.g0dkar.qrcode.internals.QRCodeSquareType.MARGIN
import io.github.g0dkar.qrcode.render.Colors
import io.github.g0dkar.qrcode.internals.QRCodeSquareType.POSITION_PROBE
import io.github.g0dkar.qrcode.render.QRCodeGraphics

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val topToBottom: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        generateQRCode(content = "https://tgt.wtf")
    }

    private fun generateQRCode(content: String) {

        val startColor = Colors.rgba(228, 34, 104, 1) // Light Blue
        val endColor = Colors.rgba(131, 146, 201, 1) // Dark Blue

        binding.generateQrButton.setOnClickListener {

            val qrCode = QRCode(
                content,
                errorCorrectionLevel = ErrorCorrectionLevel.Q
            )

            val qrCodeData = qrCode.encode()
            val qrCodeSize = qrCode.computeImageSize(rawData = qrCodeData)

            val (startR, startG, startB) = Colors.getRGBA(startColor)
            val (endR, endG, endB) = Colors.getRGBA(endColor)

            val qrCodeCanvas = qrCode.renderShaded(rawData = qrCodeData) { cellData, cellCanvas ->

                //Always paint it white to make sure there are no transparent pixels
                cellCanvas.fill(Colors.WHITE)

                // If want to make square to round
                when (cellData.squareInfo.type) {
                    POSITION_PROBE -> when (cellData.squareInfo.region) {
                        QRCodeRegion.TOP_LEFT_CORNER -> drawTopLeftCorner(cellCanvas)
                        QRCodeRegion.TOP_RIGHT_CORNER -> drawTopRightCorner(cellCanvas)
                        QRCodeRegion.BOTTOM_LEFT_CORNER -> drawBottomLeftCorner(cellCanvas)
                        QRCodeRegion.BOTTOM_RIGHT_CORNER -> drawBottomRightCorner(cellCanvas)
                        else -> cellCanvas.fill(Colors.BLACK)
                    }
                    MARGIN -> cellCanvas.fill(Colors.WHITE)
                    else -> cellCanvas.fillRoundRect(
                        0,
                        0,
                        cellCanvas.width,
                        cellCanvas.height,
                        15,
                        Colors.BLACK
                    )
                }

                // Will override the RoundQR
                // If want to use Gradient Color QR Generator
                if (cellData.dark) {
                    val x = cellData.absoluteX()
                    val y = cellData.absoluteY()

                    for (currY in 0 until cellCanvas.height) {
                        val topBottomPct = pct(x, y + currY, qrCodeSize, qrCodeSize)
                        val bottomTopPct = 1 - topBottomPct

                        val currColor = Colors.rgba(
                            (startR * bottomTopPct + endR * topBottomPct).toInt(),
                            (startG * bottomTopPct + endG * topBottomPct).toInt(),
                            (startB * bottomTopPct + endB * topBottomPct).toInt()
                        )

                        cellCanvas.drawLine(0, currY, cellCanvas.width, currY, currColor)
                    }

                } else {
                    cellCanvas.fill(Colors.WHITE)
                }
            }
            val bm = qrCodeCanvas.nativeImage() as Bitmap
            binding.imageView.setImageBitmap(bm)
            binding.generateQrButton.visibility = View.INVISIBLE
        }
    }

    private fun size(canvas: QRCodeGraphics) = canvas.width * 4
    private fun circleSize(canvas: QRCodeGraphics): Int = (canvas.width * 1.8).toInt()

    private fun drawTopLeftCorner(canvas: QRCodeGraphics) {
        val size = size(canvas)
        val circleSize = circleSize(canvas)
        canvas.fillRoundRect(0, 0, size, size, circleSize, Colors.BLACK)
    }

    private fun drawBottomRightCorner(canvas: QRCodeGraphics) {
        val size = size(canvas)
        val circleSize = circleSize(canvas)
        canvas.fillRoundRect(
            -size + canvas.width,
            -size + canvas.width,
            size,
            size,
            circleSize,
            Colors.BLACK
        )
    }

    private fun drawBottomLeftCorner(canvas: QRCodeGraphics) {
        val size = size(canvas)
        val circleSize = circleSize(canvas)
        canvas.fillRoundRect(0, -size + canvas.width, size, size, circleSize, Colors.BLACK)
    }

    private fun drawTopRightCorner(canvas: QRCodeGraphics) {
        val size = size(canvas)
        val circleSize = circleSize(canvas)
        canvas.fillRoundRect(-size + canvas.width, 0, size, size, circleSize, Colors.BLACK)
    }


    private fun pct(x: Int, y: Int, width: Int, height: Int): Double =
        if (topToBottom) {
            x.toDouble() / height.toDouble()
        } else {
            y.toDouble() / width.toDouble()
        }
}