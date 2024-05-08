package com.example.pasyarapp.classes

import android.app.Dialog
import android.content.Context
import com.example.pasyarapp.R

object Commons {
    fun loadingDialog(context: Context): Dialog {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.loading_dialog)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        return dialog
    }
}