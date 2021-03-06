package encrywm.lang.frontend.semantics.scope

import encrywm.lib.Types._
import encrywm.lib.predef.decode.{Base58decode, SchemaDecode}
import encrywm.lib.predef.hash._
import encrywm.lib.predef.signature._
import encrywm.lib.predef.time._

object ESPredefScope {

  val predefNames: Seq[Symbol] = Seq(
    Symbol(ESContext.ident.toLowerCase, ESContext),
    Symbol(CheckSig.name, ESFunc(CheckSig.args.toList, ESBoolean)),
    Symbol(Blake2b256Hash.name, ESFunc(Blake2b256Hash.args.toList, ESByteVector)),
    Symbol(Blake2b512Hash.name, ESFunc(Blake2b512Hash.args.toList, ESByteVector)),
    Symbol(Keccak256Hash.name, ESFunc(Keccak256Hash.args.toList, ESByteVector)),
    Symbol(Keccak512Hash.name, ESFunc(Keccak512Hash.args.toList, ESByteVector)),
    Symbol(Sha256Hash.name, ESFunc(Sha256Hash.args.toList, ESByteVector)),
    Symbol(Str2Time.name, ESFunc(Str2Time.args.toList, ESLong)),
    Symbol(Base58decode.name, ESFunc(Base58decode.args.toList, ESOption(ESByteVector))),
    Symbol(SchemaDecode.name, ESFunc(SchemaDecode.args.toList, ESOption(SDObject)))
  )
}
