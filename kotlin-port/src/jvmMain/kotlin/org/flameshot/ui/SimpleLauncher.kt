// SPDX-License-Identifier: GPL-3.0-or-later

package org.flameshot.ui

import org.flameshot.core.CaptureRequest
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Image
import java.awt.Robot
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.io.Serializable
import javax.imageio.ImageIO
import javax.swing.*
import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import java.io.ByteArrayOutputStream

fun main() {
    SwingUtilities.invokeLater {
        createAndShowGui()
    }
}

private fun createAndShowGui() {
    val frame = JFrame("Flameshot - JVM Prototype")
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.layout = BorderLayout()

    val imageLabel = JLabel()
    imageLabel.horizontalAlignment = SwingConstants.CENTER
    val scroll = JScrollPane(imageLabel)
    scroll.preferredSize = Dimension(800, 600)

    val captureButton = JButton("Capture Full Screen")
    captureButton.addActionListener {
        val img = captureFullScreen()
        if (img != null) {
            lastCapture = img
            val preview = scaledPreview(img, 800, 600)
            val icon = ImageIcon(preview)
            imageLabel.icon = icon
            frame.revalidate()
        } else {
            JOptionPane.showMessageDialog(frame, "Capture failed")
        }
    }

    val saveButton = JButton("Save Last")
    saveButton.addActionListener {
        val image = lastCapture
        if (image != null) {
            val chooser = JFileChooser()
            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                var file = chooser.selectedFile
                if (!file.name.contains('.')) file = File(file.absolutePath + ".png")
                try {
                    ImageIO.write(image, "png", file)
                    JOptionPane.showMessageDialog(frame, "Saved to: ${file.absolutePath}")
                } catch (e: IOException) {
                    JOptionPane.showMessageDialog(frame, "Failed to save: ${e.message}")
                }
            }
        } else {
            JOptionPane.showMessageDialog(frame, "No capture available to save")
        }
    }

    val copyButton = JButton("Copy to Clipboard")
    copyButton.addActionListener {
        val image = lastCapture
        if (image != null) {
            try {
                val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                clipboard.setContents(ImageSelection(image), null)
                JOptionPane.showMessageDialog(frame, "Image copied to clipboard")
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(frame, "Failed to copy to clipboard: ${e.message}")
            }
        } else {
            JOptionPane.showMessageDialog(frame, "No capture available to copy")
        }
    }

    val buttons = JPanel()
    buttons.add(captureButton)
    buttons.add(saveButton)
    buttons.add(copyButton)

    frame.add(scroll, BorderLayout.CENTER)
    frame.add(buttons, BorderLayout.SOUTH)
    frame.pack()
    frame.setLocationRelativeTo(null)
    frame.isVisible = true
}

private var lastCapture: BufferedImage? = null

private fun captureFullScreen(): BufferedImage? {
    return try {
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val devices = ge.screenDevices
        var minX = Int.MAX_VALUE
        var minY = Int.MAX_VALUE
        var maxX = Int.MIN_VALUE
        var maxY = Int.MIN_VALUE
        for (d in devices) {
            val b = d.defaultConfiguration.bounds
            minX = minOf(minX, b.x)
            minY = minOf(minY, b.y)
            maxX = maxOf(maxX, b.x + b.width)
            maxY = maxOf(maxY, b.y + b.height)
        }
        if (minX == Int.MAX_VALUE) {
            val toolkit = Toolkit.getDefaultToolkit()
            val screenSize = toolkit.screenSize
            minX = 0; minY = 0; maxX = screenSize.width; maxY = screenSize.height
        }
        val captureRect = Rectangle(minX, minY, maxX - minX, maxY - minY)
        val robot = Robot()
        robot.createScreenCapture(captureRect)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun toBufferedImage(img: Image): BufferedImage {
    if (img is BufferedImage) return img
    val bimage = BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB)
    val g = bimage.createGraphics()
    g.drawImage(img, 0, 0, null)
    g.dispose()
    return bimage
}

private fun scaledPreview(img: BufferedImage, maxWidth: Int, maxHeight: Int): Image {
    val w = img.width
    val h = img.height
    if (w <= maxWidth && h <= maxHeight) return img
    val fw = maxWidth.toDouble() / w.toDouble()
    val fh = maxHeight.toDouble() / h.toDouble()
    val f = minOf(fw, fh)
    val nw = (w * f).toInt()
    val nh = (h * f).toInt()
    return img.getScaledInstance(nw, nh, Image.SCALE_SMOOTH)
}

private class ImageSelection(private val image: BufferedImage) : Transferable, Serializable {
    override fun getTransferData(flavor: DataFlavor): Any {
        if (flavor === DataFlavor.imageFlavor) return image
        throw UnsupportedFlavorException(flavor)
    }

    override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(DataFlavor.imageFlavor)

    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean = flavor === DataFlavor.imageFlavor
}
