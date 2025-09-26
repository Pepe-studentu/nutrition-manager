package org.example.project.service

import java.io.File

class SignatureManager {
    
    private val signatureDir = File(System.getProperty("user.home"), "NutritionApp")
    private val signatureFile = File(signatureDir, "signature_template.html")
    
    init {
        ensureDirectoryExists()
        ensureDefaultSignatureExists()
    }
    
    private fun ensureDirectoryExists() {
        if (!signatureDir.exists()) {
            signatureDir.mkdirs()
        }
    }
    
    private fun ensureDefaultSignatureExists() {
        if (!signatureFile.exists()) {
            createDefaultSignature()
        }
    }
    
    private fun createDefaultSignature() {
        val defaultSignature = """
            <div class="signature-section">
                <div class="signature-line">
                    <span>Nutritionist Signature: ___________________________</span>
                </div>
                <div class="signature-date">
                    <span>Date: ___________________________</span>
                </div>
            </div>
            <style>
                .signature-section {
                    margin-top: 30px;
                    padding: 20px 0;
                    border-top: 1px solid #333;
                }
                .signature-line, .signature-date {
                    margin: 10px 0;
                    font-family: Arial, sans-serif;
                    font-size: 10pt;
                }
            </style>
        """.trimIndent()
        
        signatureFile.writeText(defaultSignature)
    }
    
    fun getSignatureTemplate(): String? {
        return try {
            if (signatureFile.exists()) {
                val content = signatureFile.readText()
                // Fix image paths by converting relative paths to absolute file URLs
                fixImagePaths(content)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun fixImagePaths(content: String): String {
        val signaturePngFile = File(signatureDir, "signature.png")

        return if (signaturePngFile.exists()) {
            // Convert the image reference to a file URL that OpenHTMLToPDF can resolve
            val imageUrl = signaturePngFile.toURI().toString()
            content.replace("signature.png", imageUrl)
        } else {
            // If signature.png doesn't exist, remove the img tag to avoid broken images
            content.replace(Regex("<img[^>]*src=\"signature\\.png\"[^>]*>"), "<!-- signature.png not found -->")
        }
    }
    
    fun updateSignatureTemplate(content: String): Boolean {
        return try {
            signatureFile.writeText(content)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun getSignatureFilePath(): String {
        return signatureFile.absolutePath
    }
    
    fun signatureExists(): Boolean {
        return signatureFile.exists()
    }
    
    fun deleteSignature(): Boolean {
        return try {
            if (signatureFile.exists()) {
                signatureFile.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}