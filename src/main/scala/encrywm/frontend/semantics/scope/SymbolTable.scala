package encrywm.frontend.semantics.scope

import encrywm.frontend.semantics.error.AlreadyDefinedError

import scala.collection.mutable

trait SymbolTable {

  protected val symbols: mutable.TreeMap[String, Symbol] = mutable.TreeMap.empty[String, Symbol]

  def insert(sym: Symbol): Unit = {
    //TODO: replace 0
    symbols.get(sym.name).map(_ => throw AlreadyDefinedError(sym.name, "None"))
    symbols.update(sym.name, sym)
  }

  def lookup(name: String, currentScopeOnly: Boolean = false): Option[Symbol] = symbols.get(name)
}
