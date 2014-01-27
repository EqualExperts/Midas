package com.ee.midas.run

import org.specs2._
import runner.SpecificationsFinder._

class Index extends Specification { def is =

  examplesLinks("Example specifications")

  // see the SpecificationsFinder trait for the parameters of the 'specifications' method
  def examplesLinks(t: String) = t.title ^ specifications(pattern = ".*Specs", verbose = true).map(see)
}