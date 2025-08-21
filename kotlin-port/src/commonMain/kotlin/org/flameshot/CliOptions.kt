package org.flameshot

import org.flameshot.core.CaptureRequest

fun applyCommonOptions(request: CaptureRequest, parser: SimpleArgsParser): CaptureRequest {
    var result = request

    // Apply delay
    val delay = parser.getIntOptionValue("--delay") ?: parser.getIntOptionValue("-d")
    if (delay != null) {
        result = result.copy(delay = delay.toUInt())
    }

    // Apply save path
    val savePath = parser.getOptionValue("--path") ?: parser.getOptionValue("-p")
    if (savePath != null) {
        result = result.addSaveTask(savePath)
    }

    // Apply copy to clipboard
    if (parser.hasOption("--copy") || parser.hasOption("-c")) {
        result = result.addTask(CaptureRequest.ExportTask.COPY)
    }

    return result
}
