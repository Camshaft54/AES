package io.github.camshaft54.aes.algorithms

/**
 * Converts a 4x4 2D array of integers from base 16 to base 10
 */
fun String.hexToDecArray(): Array<Array<Int>> {
    val bytes = this.split(" ").filterNot { it.isEmpty() }
    if (bytes.size == 4 && bytes.all { it.length == 8 }) {
        return bytes.map { byte ->
            byte.chunked(2)
                .map {
                    Integer.parseInt(it, 16)
                }.toTypedArray()
        }.toTypedArray()
    } else {
        throw IllegalArgumentException("Hex key must contain 4 separate hex bytes separated by spaces")
    }
}

/**
 * Prints the hexadecimal version of decimal integers in a 2D array
 */
fun Array<Array<Int>>.printHex(): String {
    return this.joinToString(" ") { byte ->
        byte.joinToString("") {
            Integer.toHexString(it).padStart(2, '0')
        }
    }
}

/**
 * Rotates the integers in an array a specified number of times to the left
 */
fun Array<Int>.rotateLeft(amount: Int = 1): Array<Int> {
    val new = this.toMutableList()
    repeat(amount) { new.add(new.removeAt(0)) }
    return new.toTypedArray()
}

/**
 * Rotates the integers in an array a specified number of times to the right
 */
fun Array<Int>.rotateRight(amount: Int = 1): Array<Int> {
    val new = this.toMutableList()
    repeat(amount) { new.add(0, new.removeAt(new.lastIndex)) }
    return new.toTypedArray()
}

/**
 * Converts a list of integers to a 2D array (state) and pads any extra characters with the specified padding
 */
fun List<Int>.toState(pad: Int, size: Int): Array<Array<Int>> {
    val new = Array(size) { Array(size) { pad } }
    this.forEachIndexed { i, byte ->
        new[i / size][i % size] = byte
    }
    return new
}

/**
 * XORs two 2D arrays with each other
 */
infix fun Array<Array<Int>>.xor(other: Array<Array<Int>>) = this.mapIndexed { i, row ->
    row.mapIndexed { j, byte ->
        byte xor other[i][j]
    }.toTypedArray()
}.toTypedArray()

/**
 * Converts a byte array to a non-negative Int array (all negative values are shifted up by 256)
 */
fun ByteArray.toIntList(): List<Int> {
    return this.map {
        val int = it.toInt()
        if (int >= 0) int else int + 256
    }
}

/**
 * Converts a 2D list to a 2D array
 */
fun List<List<Int>>.deepToTypedArray(): Array<Array<Int>> = map {it.toTypedArray()}.toTypedArray()