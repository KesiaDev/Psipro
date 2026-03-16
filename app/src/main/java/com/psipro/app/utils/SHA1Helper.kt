package com.psipro.app.utils

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateEncodingException

object SHA1Helper {
    private const val TAG = "SHA1Helper"
    
    /**
     * Obtém o SHA-1 do certificado usado para assinar o app
     */
    fun getSHA1(context: Context): String? {
        return try {
            val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }
            
            val md = MessageDigest.getInstance("SHA-1")
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val signingInfo = packageInfo.signingInfo
                if (signingInfo.hasMultipleSigners()) {
                    // App assinado com múltiplos certificados
                    val signatures = signingInfo.apkContentsSigners
                    if (signatures.isNotEmpty()) {
                        val digest = md.digest(signatures[0].toByteArray())
                        return bytesToHex(digest)
                    }
                } else {
                    // App assinado com um único certificado
                    val signatures = signingInfo.apkContentsSigners
                    if (signatures.isNotEmpty()) {
                        val digest = md.digest(signatures[0].toByteArray())
                        return bytesToHex(digest)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val signatures = packageInfo.signatures
                if (signatures.isNotEmpty()) {
                    val digest = md.digest(signatures[0].toByteArray())
                    return bytesToHex(digest)
                }
            }
            
            null
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Package não encontrado", e)
            null
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "SHA-1 não disponível", e)
            null
        } catch (e: CertificateEncodingException) {
            Log.e(TAG, "Erro ao codificar certificado", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter SHA-1", e)
            null
        }
    }
    
    /**
     * Obtém o SHA-1 formatado com dois pontos (formato Firebase)
     */
    fun getSHA1Formatted(context: Context): String? {
        val sha1 = getSHA1(context) ?: return null
        return formatSHA1(sha1)
    }
    
    /**
     * Converte bytes para hexadecimal
     */
    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = HEX_ARRAY[v ushr 4]
            hexChars[i * 2 + 1] = HEX_ARRAY[v and 0x0F]
        }
        return String(hexChars)
    }
    
    /**
     * Formata SHA-1 com dois pontos (ex: 8A:21:33:E7:...)
     */
    private fun formatSHA1(sha1: String): String {
        return sha1.chunked(2).joinToString(":").uppercase()
    }
    
    private val HEX_ARRAY = "0123456789abcdef".toCharArray()
}

