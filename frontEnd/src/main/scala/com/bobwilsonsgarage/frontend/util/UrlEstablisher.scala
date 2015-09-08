package com.bobwilsonsgarage.frontend.util

/**
 * Establishes Urls for different types.
 *
 * @author dbolene
 */
object UrlEstablisher {
  def encode(basePath: String, uniquePath: String) = {
    s"http://$basePath$uniquePath"
  }

  def orderUrl(orderId: String, hostAndPath: String) =
    encode(hostAndPath, s"BobWilsonsGarage/orders/$orderId")

}