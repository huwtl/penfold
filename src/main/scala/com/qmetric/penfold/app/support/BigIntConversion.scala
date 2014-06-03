package com.qmetric.penfold.app.support

import com.mongodb.casbah.commons.conversions.MongoConversionHelper
import org.bson.{BSON, Transformer}

object RegisterBigIntConversionHelpers extends MongoDBBigIntSerializer {
  def apply() {
    super.register()
  }
}

trait MongoDBBigIntSerializer extends MongoConversionHelper {
  private val encodeTypeBigInt = classOf[BigInt]

  private val transformer = new Transformer {
    def transform(o: AnyRef): AnyRef = o match {
      case bigInt: BigInt => bigInt.toLong.asInstanceOf[Object]
      case _ => o
    }
  }

  override def register() {
    BSON.addEncodingHook(encodeTypeBigInt, transformer)
    super.register()
  }

  override def unregister() {
    BSON.removeEncodingHooks(encodeTypeBigInt)
    super.unregister()
  }
}