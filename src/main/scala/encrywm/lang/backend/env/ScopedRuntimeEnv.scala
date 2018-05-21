package encrywm.lang.backend.env

import encrywm.lib.predef.PredefFunctions

class ScopedRuntimeEnv(val name: String,
                       val level: Int,
                       override val members: Map[String, ESEnvComponent],
                       val parentOpt: Option[ScopedRuntimeEnv] = None,
                       val isFunc: Boolean = false) extends RuntimeEnv {

  def updated(s: ESEnvComponent): ScopedRuntimeEnv =
    new ScopedRuntimeEnv(name, level, members.updated(s.id, s), parentOpt)

  def emptyChild(n: String): ScopedRuntimeEnv = ScopedRuntimeEnv.empty(n, level + 1, Some(this))

  def child(n: String,
            ms: Map[String, ESEnvComponent],
            isFunc: Boolean = false): ScopedRuntimeEnv = new ScopedRuntimeEnv(n, level + 1, ms, Some(this), isFunc)

  override def get(id: String): Option[ESEnvComponent] = members.get(id).orElse(parentOpt.flatMap(_.get(id)))

  override def toString: String = s"<ScopedContext name=$name lvl=$level size=${members.size}>"
}

object ScopedRuntimeEnv {

  def apply(name: String,
            level: Int,
            members: Map[String, ESEnvComponent] = Map.empty,
            parentOpt: Option[ScopedRuntimeEnv] = None,
            isFunc: Boolean = false): ScopedRuntimeEnv =
    new ScopedRuntimeEnv(name, level, members, parentOpt, isFunc)

  def empty(n: String, l: Int, parent: Option[ScopedRuntimeEnv] = None): ScopedRuntimeEnv =
    new ScopedRuntimeEnv(n, l, Map.empty, parent)

  def initialized(n: String, l: Int, env: Map[String, ESEnvComponent]): ScopedRuntimeEnv =
    new ScopedRuntimeEnv(n, l, env ++ PredefFunctions.all, None)
}
