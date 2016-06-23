package org.bitcoins.core.util

import scala.math.BigInt

/**
 * Created by chris on 2/26/16.
 */
trait BitcoinSUtil {

  def decodeHex(hex : String) : Seq[Byte] = {
    hex.replaceAll("[^0-9A-Fa-f]", "").sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte).toList
  }

  def encodeHex(bytes : Seq[Byte]) : String = bytes.map("%02x".format(_)).mkString

  def encodeHex(byte : Byte) : String = encodeHex(Seq(byte))

  /**
    * Encodes a long number to a hex string, pads it with an extra '0' char
    * if the hex string is an odd amount of characters
    * @param long
    * @return
    */
  def encodeHex(long : Long) : String = {
    val hex = long.toHexString.length % 2 match {
      case 1 => "0" + long.toHexString
      case _ : Int => long.toHexString
    }
    addPadding(16,hex)
  }

  def encodeHex(int : Int) : String = {
    val hex = int.toHexString.length % 2 match {
      case 1 => "0" + int.toHexString
      case _ : Int => int.toHexString
    }
    addPadding(8,hex)
  }

  def encodeHex(bigInt : BigInt) : String = BitcoinSUtil.encodeHex(bigInt.toByteArray)

  /**
   * Tests if a given string is a hexadecimal string
   * @param str
   * @return
   */
  def isHex(str : String) = {
    //check valid characters & hex strings have to have an even number of chars
    str.matches("^[0-9a-f]+$") && (str.size % 2 == 0)
  }

  /**
    * Converts a two character hex string to its byte representation
    * @param hex
    * @return
    */
  def hexToByte(hex : String): Byte = {
    require(hex.size == 2)
    BitcoinSUtil.decodeHex(hex).head
  }

  /**
   * Flips the endianess of the give hex string
   * @param hex
   * @return
   */
  def flipEndianess(hex : String) : String = flipEndianess(decodeHex(hex))

  /**
   * Flips the endianess of the given sequence of bytes
   * @param bytes
   * @return
   */
  def flipEndianess(bytes : Seq[Byte]) : String = encodeHex(bytes.reverse)


  /**
    * Adds the amount padding bytes needed to fix the size of the hex string
    * for instance, ints are required to be 4 bytes. If the number is just 1
    * it will only take 1 byte. We need to pad the byte with an extra 3 bytes so the result is
    * 00000001 instead of just 1
    * @param charactersNeeded
    * @param hex
    * @return
    */
  private def addPadding(charactersNeeded : Int, hex : String) : String = {
    val paddingNeeded = charactersNeeded - hex.size
    val padding = for { i <- 0 until paddingNeeded} yield "0"
    val paddedHex = padding.mkString + hex
    paddedHex
  }
}

object BitcoinSUtil extends BitcoinSUtil
