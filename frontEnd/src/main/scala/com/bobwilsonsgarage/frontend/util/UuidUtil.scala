package com.bobwilsonsgarage.frontend.util

/**
 * UUID Utilities.
 *
 * @author dbolene
 */
object UuidUtil {

  def generateNewUuid(): String = java.util.UUID.randomUUID().toString

}