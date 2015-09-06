package com.bobwilsonsgarage.frontend.util

import common.util.Logging
import spray.routing.RequestContext

/**
 * Spray http request utilities
 *
 * @author dbolene
 */
object RequestContextUtil extends Logging {
  def hostAndPortFromRequestContext(ctx: RequestContext): String = {
    ctx.request.uri.authority.host.address + ":"  + ctx.request.uri.authority.port
    }
}