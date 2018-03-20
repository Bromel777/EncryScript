package encrywm.frontend.semantics.scope

import encrywm.builtins.{Attribute, ESObject}

// TODO: Implement Scope initialization process.
object InitialScope {

  import encrywm.builtins.Types._

  val attrs = Seq(
    Attribute("timestamp", LONG, null),
    Attribute("sender", STRING, null)
  )
  val tx = ESObject("transaction", attrs)

  private val builtinSymbs = staticTypes.map(_._2.symbol).toSeq :+
    BuiltInTypeSymbol(tx.name, tx.attrs.map(attr => VariableSymbol(attr.name, Some(BuiltInTypeSymbol(attr.tpe.identifier)))))

  def global: ScopedSymbolTable = {
    val symtab = new ScopedSymbolTable("GLOBAL", 1)
    builtinSymbs.foreach(symtab.insert)
    symtab
  }
}
