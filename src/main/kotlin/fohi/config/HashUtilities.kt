package fohi.config

import java.nio.ByteBuffer
import java.security.MessageDigest

/**
 * Utility method to generate cryptographic hash of specified data by specified algorithm
 *
 * @param algorithm cryptographic hash algorithm
 * @param data
 */
private fun hashWithAlgorithm(algorithm: String, data: ByteArray): ByteArray {
    val digest = MessageDigest.getInstance(algorithm)
    return digest.digest(data)
}

/**
 * Creates Hex String from ByteArray
 *
 * @receiver [ByteArray]
 */
private fun generateHexString(data: ByteArray): String = data.fold(StringBuilder()) { str, it -> str.append("%02x".format(it)) }.toString()

fun ByteArray.hexString(): String = generateHexString(this)

fun ByteBuffer.hexString(): String = generateHexString(this.array())

fun String.sha(): ByteArray = hashWithAlgorithm("SHA-1", this.toByteArray())

fun String.sha256(): ByteArray = hashWithAlgorithm("SHA-256", this.toByteArray())

fun String.sha512(): ByteArray = hashWithAlgorithm("SHA-256", this.toByteArray())

fun String.md5(): ByteArray = hashWithAlgorithm("MD5", this.toByteArray())

fun ByteBuffer.md5(): ByteArray = hashWithAlgorithm("MD5", this.array())

fun ByteArray.md5(): ByteArray = hashWithAlgorithm("MD5", this)
