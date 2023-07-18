package io.github.camshaft54.aes.algorithms

/**
 * Encrypts 128-bit data using a 128-bit key with AES-128
 */
fun aesEncrypt(cipherKey: Array<Array<Int>>, data: Array<Array<Int>>): Array<Array<Int>> {
    val roundKeys = getRoundKeys(cipherKey)
    var res = data

    res = res.addRoundKey(roundKeys[0])

    for (round in 1..10) {
        res = if (round != 10) {
            mixColumns(shiftRows(subBytes(res))).addRoundKey(roundKeys[round])
        } else {
            shiftRows(subBytes(res)).addRoundKey(roundKeys[round])
        }
    }
    return res
}

/**
 * Decrypts 128-bit data using a 128-bit key with AES-128
 */
fun aesDecrypt(cipherKey: Array<Array<Int>>, encryptedData: Array<Array<Int>>): Array<Array<Int>> {
    val roundKeys = getRoundKeys(cipherKey)
    var res = encryptedData

    for (round in 10 downTo 1) {
        res = if (round != 10) {
            invSubBytes(invShiftRows(invMixColumns(res.removeRoundKey(roundKeys[round]))))
        } else {
            invSubBytes(invShiftRows(res.removeRoundKey(roundKeys[round])))
        }
    }

    res = res.removeRoundKey(roundKeys[0])

    return res
}

/**
 * Get the 10 round keys according to the first key based on the AES key schedule
 */
private fun getRoundKeys(k: Array<Array<Int>>): Array<Array<Array<Int>>> {
    val words = k.toMutableList()
    val n = 4 // Number of words in a key
    val r = 11 // Number of rounds
    for (i in 4..(n * r)) {
        if (i % n == 0) { // If this is the first word in one of the round keys
            words.add(
                words[i - 1]
                    .rotateLeft()
                    .mapIndexed { byteIdx, byte ->
                        val res = words[i - n][byteIdx] xor sBox[byte]
                        if (byteIdx == 0) res xor rCon[i / n] else res
                    }.toTypedArray()
            )
        } else {
            words.add(
                words[i - 1].mapIndexed { byteIdx, byte ->
                    words[i - n][byteIdx] xor byte
                }.toTypedArray()
            )
        }
    }
    return words.chunked(4).map { it.toTypedArray() }.toTypedArray()
}

/**
 * Takes a state and maps each byte using sBox
 */
private fun subBytes(data: Array<Array<Int>>): Array<Array<Int>> =
    data.map { row ->
        row.map {
            sBox[it]
        }
    }.deepToTypedArray()

/**
 * Takes a state and shifts each row to the left in the state by its index (row 0 shifts 0, row 1 shifts 1, etc.)
 */
private fun shiftRows(data: Array<Array<Int>>): Array<Array<Int>> {
    val res = data.clone()
    for (i in 0..3) {
        val column = arrayOf(data[0][i], data[1][i], data[2][i], data[3][i]).rotateLeft(i)
        res[0][i] = column[0]
        res[1][i] = column[1]
        res[2][i] = column[2]
        res[3][i] = column[3]
    }
    return res
}

/**
 * Passes a state through the Rjindael MixColumns algorithm, which is the primary source of diffusion for AES.
 * More info here: https://en.wikipedia.org/wiki/Rijndael_MixColumns
 */
private fun mixColumns(data: Array<Array<Int>>): Array<Array<Int>> {
    return Array(4) { c ->
        val b0 = data[c][0]
        val b1 = data[c][1]
        val b2 = data[c][2]
        val b3 = data[c][3]

        val d0 = ((2 gfMult b0) xor (3 gfMult b1) xor (1 gfMult b2) xor (1 gfMult b3)) % 256
        val d1 = ((1 gfMult b0) xor (2 gfMult b1) xor (3 gfMult b2) xor (1 gfMult b3)) % 256
        val d2 = ((1 gfMult b0) xor (1 gfMult b1) xor (2 gfMult b2) xor (3 gfMult b3)) % 256
        val d3 = ((3 gfMult b0) xor (1 gfMult b1) xor (1 gfMult b2) xor (2 gfMult b3)) % 256
        arrayOf(d0, d1, d2, d3)
    }
}

/**
 * Galois Field multiplication of two bytes for Rjindael MixColumns
 * This adaption for Kotlin is from https://en.wikipedia.org/wiki/Rijndael_MixColumns#Implementation_example
 */
private infix fun Int.gfMult(other: Int): Int {
    var a: Int = this
    var b: Int = other

    var res = 0x00

    for (counter in 0..7) {
        if ((b and 1) != 0x00) {
            res = res xor a
        }

        val hiBitSet: Boolean = (a and 0x80) != 0
        a = a shl 1

        if (hiBitSet) {
            a = a xor 0x1B // x^8 + x^4 + x^3 + x + 1
        }
        b = b shr 1
    }
    return res
}

/**
 * XORs each byte in a state with the corresponding round key byte
 */
private fun Array<Array<Int>>.addRoundKey(roundKey: Array<Array<Int>>): Array<Array<Int>> {
    return this xor roundKey
}

/**
 * Inverts the addRoundKey operation on a state (bitwise XOR is the inverse of itself)
 */
private fun Array<Array<Int>>.removeRoundKey(roundKey: Array<Array<Int>>): Array<Array<Int>> {
    return this xor roundKey
}

/**
 * Inverts the subBytes operation by passing each byte through the inverse sBox
 */
 private fun invSubBytes(data: Array<Array<Int>>): Array<Array<Int>> =
    data.map { row ->
        row.map {
            sBox.indexOf(it)
        }
    }.deepToTypedArray()

/**
 * Inverses shiftRows by shifting each row in the state back by the row's index
 */
private fun invShiftRows(data: Array<Array<Int>>): Array<Array<Int>> {
    val res = data.clone()
    for (i in 0..3) {
        val column = arrayOf(data[0][i], data[1][i], data[2][i], data[3][i]).rotateRight(i)
        res[0][i] = column[0]
        res[1][i] = column[1]
        res[2][i] = column[2]
        res[3][i] = column[3]
    }
    return res
}

/**
 * Inverse of Rjindael MixColumns
 * More info: https://en.wikipedia.org/wiki/Rijndael_MixColumns
 */
private fun invMixColumns(data: Array<Array<Int>>): Array<Array<Int>> {
    return Array(4) { c ->
        val d0 = data[c][0]
        val d1 = data[c][1]
        val d2 = data[c][2]
        val d3 = data[c][3]

        val b0 = ((14 gfMult d0) xor (11 gfMult d1) xor (13 gfMult d2) xor (9 gfMult d3)) % 256
        val b1 = ((9 gfMult d0) xor (14 gfMult d1) xor (11 gfMult d2) xor (13 gfMult d3)) % 256
        val b2 = ((13 gfMult d0) xor (9 gfMult d1) xor (14 gfMult d2) xor (11 gfMult d3)) % 256
        val b3 = ((11 gfMult d0) xor (13 gfMult d1) xor (9 gfMult d2) xor (14 gfMult d3)) % 256
        arrayOf(b0, b1, b2, b3)
    }
}