package com.ee.midas.dsl.expressions

import com.ee.midas.utils.Loggable

object Expressions extends Loggable {
  def build(jsonExpression: String) = {
    log.info(s"Building Expression $jsonExpression")
  }

  def validate(jsonExpression: String) = {
    log.info(s"Validating Expression $jsonExpression")
  }
}
