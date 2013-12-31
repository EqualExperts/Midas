package com.ee.midas

import com.ee.midas.transform.TransformType

case class Config (midasHost:String = "localhost", midasPort: Int = 27020 , mongoHost: String = "localhost", mongoPort: Int = 27017, mode:TransformType = TransformType.EXPANSION, deltasDir: String = "deltas/")
