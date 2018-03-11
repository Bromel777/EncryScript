package encrywm.parser

object Ast {

  case class Identifier(name: String)

  sealed trait MOD
  object MOD {
    case class Contract(body: Seq[STMT]) extends MOD
    case class Expression(body: Seq[STMT]) extends MOD
  }

  sealed trait STMT
  object STMT {

    case class FunctionDef(name: Identifier, args: Arguments, body: Seq[STMT], returnTypeOpr: Option[Identifier]) extends STMT
    case class Return(value: Option[EXPR]) extends STMT

    case class Assign(target: EXPR, value: EXPR) extends STMT
    case class AugAssign(target: EXPR, op: OPERATOR, value: EXPR) extends STMT

    // not sure if bool allowed: is, can always use int
    case class Print(dest: Option[EXPR], values: Seq[EXPR], nl: Boolean) extends STMT

    case class For(target: EXPR, iter: EXPR, body: Seq[STMT], orelse: Seq[STMT]) extends STMT
    case class If(test: EXPR, body: Seq[STMT], orelse: Seq[STMT]) extends STMT

    case class Assert(test: EXPR, msg: Option[EXPR]) extends STMT

    case class Expr(value: EXPR) extends STMT

    // Under discussion.
    case object Unlock extends STMT
    case object Abort extends STMT

    // col_offset is the byte offset in the utf8 string the parser uses
    case class attributes(lineno: Int, col_offset: Int)
  }

  sealed trait EXPR
  object EXPR {

    case class BoolOp(op: BOOL_OP, values: Seq[EXPR]) extends EXPR
    case class BinOp(left: EXPR, op: OPERATOR, right: EXPR) extends EXPR
    case class UnaryOp(op: UNARY_OP, operand: EXPR) extends EXPR
    case class Lambda(args: Arguments, body: EXPR) extends EXPR
    case class IfExp(test: EXPR, body: EXPR, orelse: EXPR) extends EXPR
    case class Dict(keys: Seq[EXPR], values: Seq[EXPR]) extends EXPR
    case class Set(elts: Seq[EXPR]) extends EXPR

    // Sequences are required for compare to distinguish between
    // x < 4 < 3 and (x < 4) < 3
    case class Compare(left: EXPR, ops: Seq[COMP_OP], comparators: Seq[EXPR]) extends EXPR
    case class Call(func: EXPR, args: Seq[EXPR], keywords: Seq[keyword]) extends EXPR
    case class IntConst(n: Int) extends EXPR
    case class LongConst(n: Int) extends EXPR
    case class Str(s: String) extends EXPR

    // the following expression can appear in assignment context
    case class Attribute(value: EXPR, attr: Identifier, ctx: EXPR_CTX) extends EXPR
    case class Subscript(value: EXPR, slice: SLICE, ctx: EXPR_CTX) extends EXPR
    case class Name(id: Identifier, ctx: EXPR_CTX) extends EXPR
    case class List(elts: Seq[EXPR], ctx: EXPR_CTX) extends EXPR
    case class Tuple(elts: Seq[EXPR], ctx: EXPR_CTX) extends EXPR

    case class Decl(target: EXPR, typeOpt: Option[Identifier]) extends EXPR
  }
  // col_offset is the byte offset in the utf8 string the parser uses
  case class Attributes(lineno: Int, col_offset: Int)

  sealed trait EXPR_CTX
  object EXPR_CTX {

    case object Load extends EXPR_CTX
    case object Store extends EXPR_CTX
    case object AugLoad extends EXPR_CTX
    case object AugStore extends EXPR_CTX
    case object Param extends EXPR_CTX
  }

  sealed trait SLICE
  object SLICE {

    case object Ellipsis extends SLICE
    case class Slice(lower: Option[EXPR], upper: Option[EXPR], step: Option[EXPR]) extends SLICE
    case class ExtSlice(dims: Seq[SLICE]) extends SLICE
    case class Index(value: EXPR) extends SLICE
  }

  sealed trait BOOL_OP
  object BOOL_OP {
    case object And extends BOOL_OP
    case object Or extends BOOL_OP
  }

  sealed trait OPERATOR
  case object OPERATOR {
    case object Add extends OPERATOR
    case object Sub  extends OPERATOR
    case object Mult  extends OPERATOR
    case object Div  extends OPERATOR
    case object Mod  extends OPERATOR
    case object Pow  extends OPERATOR

    case object FloorDiv extends OPERATOR
  }

  sealed trait UNARY_OP
  object UNARY_OP {

    case object Invert extends UNARY_OP
    case object Not extends UNARY_OP
    case object UAdd extends UNARY_OP
    case object USub extends UNARY_OP
  }

  sealed trait COMP_OP
  object COMP_OP {

    case object Eq extends COMP_OP
    case object NotEq extends COMP_OP
    case object Lt extends COMP_OP
    case object LtE extends COMP_OP
    case object Gt extends COMP_OP
    case object GtE extends COMP_OP
    case object Is extends COMP_OP
    case object IsNot extends COMP_OP
    case object In extends COMP_OP
    case object NotIn extends COMP_OP
  }

  // not sure what to call the first argument for raise and except
  sealed trait EXCP_HANDLER

  object EXCP_HANDLER {
    case class ExceptHandler(`type`: Option[EXPR], name: Option[EXPR], body: Seq[STMT]) extends EXCP_HANDLER
  }

  case class Arguments(args: Seq[EXPR])

  // keyword arguments supplied to call
  case class keyword(arg: Identifier, value: EXPR)

  // import name with optional 'as' alias.
  case class alias(name: Identifier, asname: Option[Identifier])
}
