package com.codingwithmitch.openapi.ui

data class UIMessage(
    val message: String,
    val messageType: UIMessageType
)

sealed class UIMessageType{

    class Toast: UIMessageType()

    class Dialog: UIMessageType()

    class AreYouSureDialog(
        val callback: AreYouSureCallback
    ): UIMessageType()

    class None: UIMessageType()
}