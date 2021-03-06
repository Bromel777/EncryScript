package encrywm.lang.frontend.semantics.scope

import encrywm.ast.Ast.AST_NODE
import encrywm.lang.frontend.semantics.exceptions.AlreadyDefinedException

import scala.collection.mutable

trait SymbolTable {

  val symbols: mutable.TreeMap[String, Symbol] = mutable.TreeMap.empty[String, Symbol]

  def insert(sym: Symbol, node: AST_NODE): Unit = {
    symbols.get(sym.name).map(_ => throw AlreadyDefinedException(sym.name, node))
    symbols.update(sym.name, sym)
  }

  def lookup(name: String, currentScopeOnly: Boolean = false): Option[Symbol] = symbols.get(name)
}
