package io.github.camshaft54.aes.algorithms

import java.nio.charset.Charset
import java.util.*

/**
 * Encrypts a string using AES-128 with the given 128-bit key using the following steps:
 * 1. Converts the key and data to binary 2D arrays using UTF-8. The data arrays are padded with 0 to make them 4x4.
 * 2. Use cipher block chaining (CBC) to encrypt the states
 * 3. Convert back to text using base64
 */
fun cbcEncrypt(key: String, data: String, IV: Array<Array<Int>> = emptyList<Int>().toState(0, 4)): String {
    if (key.toByteArray().size != 16) throw IllegalArgumentException("Key must be 16 bytes long but was ${key.toByteArray().size} bytes!")
    if (data.isEmpty()) throw IllegalArgumentException("Data must not be empty!")

    val binKey = key.toByteArray().toIntList().chunked(4).deepToTypedArray()
    val binData = data.toByteArray().toIntList().chunked(16).map { it.toState(0, 4) }.toMutableList()
    // Add initialization vector as first "state"
    binData.add(0, IV)

    // Loop through each state and xor with the previous encrypted state (or IV to begin with) then encrypt in AES-128
    val res = mutableListOf<Int>()
    var prevState = IV
    for (i in 1..binData.lastIndex) {
        val currentState = binData[i] xor prevState
        val encryptedState = aesEncrypt(binKey, currentState)
        prevState = encryptedState
        res.addAll(encryptedState.flatten())
    }
    // Encode list of encrypted ints to base64
    return Base64.getEncoder().encodeToString(res.map { it.toByte() }.toByteArray())
}

/**
 * Decrypts a string using AES-128 with the given 128-bit key using the following steps:
 * 1. Converts the key to binary 2D array with UTF-8 and encrypted date to binary 2D arrays with base64
 * 2. Use cipher block chaining (CBC) to decrypt the states
 * 3. Convert back to text using UTF-8
 */
fun cbcDecrypt(key: String, data: String, IV: Array<Array<Int>> = emptyList<Int>().toState(0, 4)): String {
    if (key.toByteArray().size != 16) throw IllegalArgumentException("Key must be 16 bytes long but was ${key.toByteArray().size} bytes!")
    if (data.isEmpty()) throw IllegalArgumentException("Data must not be empty!")

    val binKey = key.toByteArray().toIntList().chunked(4).deepToTypedArray()
    val binData = Base64.getDecoder().decode(data).toIntList().chunked(16)
        .map { it.toState(0, 4) }.toMutableList()
    // Add initialization vector as first "state"
    binData.add(0, IV)

    val res = mutableListOf<Int>()
    // Loop through each encrypted state and encrypt in AES-128 then xor with the previous encrypted state (or IV to begin with)
    for (i in 1..binData.lastIndex) {
        val decryptedState = aesDecrypt(binKey, binData[i])
        res.addAll((decryptedState xor binData[i - 1]).flatten())
    }
    // Encode list of decrypted ints to UTF-8
    return String(res.filterNot { it == 0 }.map { it.toByte() }.toByteArray(), Charset.forName("UTF-8"))
}