package com.os.actor.util

import com.os.Settings
import akka.actor.Actor

/**
 * @author Vadim Bobrov
 */
trait SettingsUse {
	this: Actor =>
	val settings = Settings(context.system.settings.config)
}
