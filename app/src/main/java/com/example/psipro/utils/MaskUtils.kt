package com.example.psipro.utils

import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputEditText

object MaskUtils {
    const val CPF_MASK = "###.###.###-##"
    const val PHONE_MASK = "(##) #####-####"
    const val DATE_MASK = "##/##/####"

    fun TextInputEditText.addCpfMask() {
        this.addTextChangedListener(object : TextWatcher {
            var isUpdating = false
            var old = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val str = s.toString().replace(Regex("[^0-9]"), "")
                var mask = ""
                
                if (isUpdating) {
                    old = str
                    isUpdating = false
                    return
                }

                var i = 0
                for (m in CPF_MASK.toCharArray()) {
                    if (m != '#' && str.length > old.length) {
                        mask += m
                        continue
                    }
                    try {
                        mask += str[i]
                    } catch (e: Exception) {
                        break
                    }
                    i++
                }

                isUpdating = true
                this@addCpfMask.setText(mask)
                this@addCpfMask.setSelection(mask.length)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun TextInputEditText.addPhoneMask() {
        this.addTextChangedListener(object : TextWatcher {
            var isUpdating = false
            var old = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val str = s.toString().replace(Regex("[^0-9]"), "")
                var mask = ""
                
                if (isUpdating) {
                    old = str
                    isUpdating = false
                    return
                }

                var i = 0
                for (m in PHONE_MASK.toCharArray()) {
                    if (m != '#' && str.length > old.length) {
                        mask += m
                        continue
                    }
                    try {
                        mask += str[i]
                    } catch (e: Exception) {
                        break
                    }
                    i++
                }

                isUpdating = true
                this@addPhoneMask.setText(mask)
                this@addPhoneMask.setSelection(mask.length)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun TextInputEditText.addDateMask() {
        this.addTextChangedListener(object : TextWatcher {
            var isUpdating = false
            var old = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val str = s.toString().replace(Regex("[^0-9]"), "")
                var mask = ""
                
                if (isUpdating) {
                    old = str
                    isUpdating = false
                    return
                }

                var i = 0
                for (m in DATE_MASK.toCharArray()) {
                    if (m != '#' && str.length > old.length) {
                        mask += m
                        continue
                    }
                    try {
                        mask += str[i]
                    } catch (e: Exception) {
                        break
                    }
                    i++
                }

                isUpdating = true
                this@addDateMask.setText(mask)
                this@addDateMask.setSelection(mask.length)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // Funções para remover as máscaras
    fun removeMask(text: String): String {
        return text.replace(Regex("[^0-9]"), "")
    }
} 