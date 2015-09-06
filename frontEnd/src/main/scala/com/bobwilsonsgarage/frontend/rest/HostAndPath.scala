package com.bobwilsonsgarage.frontend.rest

import com.bobwilsonsgarage.frontend.util.RequestContextUtil
import spray.routing.RequestContext

/**
 * Url prepending host, port, and api path.
 *
 * @author dbolene
 */
object HostAndPath {

  def hostAndPath(ctx: RequestContext): String = {
    RequestContextUtil.hostAndPortFromRequestContext(ctx) + "/"
  }

}