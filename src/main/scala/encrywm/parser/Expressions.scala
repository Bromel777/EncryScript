package encrywm.parser

import encrywm.parser.Ast.EXPR
import encrywm.parser.Scanner.kwd
import encrywm.parser.WsApi._
import fastparse.core
import fastparse.noApi._

/**
  * Expression grammar. This is stuff that can be used within a larger
  * expression. Everything here ignores whitespace and does not care about
  * indentation
  */
object Expressions {

  def tuplize(xs: Seq[Ast.EXPR]): Ast.EXPR = xs match{
    case Seq(x) => x
    case xs => Ast.EXPR.Tuple(xs, Ast.EXPR_CTX.Load)
  }

  val NAME: P[Ast.Identifier] = Scanner.identifier
  val NUMBER: P[Ast.EXPR.Num] = P( Scanner.floatnumber | Scanner.longinteger | Scanner.integer | Scanner.imagnumber ).map(Ast.EXPR.Num)
  val STRING: P[Ast.string] = Scanner.stringliteral

  val test: P[Ast.EXPR] = {
    val ternary = P( orTest ~ (kwd("if") ~ orTest ~ kwd("else") ~ test).? ).map{
      case (x, None) => x
      case (x, Some((test, neg))) => Ast.EXPR.IfExp(test, x, neg)
    }
    P( ternary | lambdef )
  }
  val orTest: core.Parser[Ast.EXPR, Char, String] = P( andTest.rep(1, kwd("or")) ).map{
    case Seq(x) => x
    case xs => Ast.EXPR.BoolOp(Ast.BOOL_OP.Or, xs)
  }
  val andTest: core.Parser[Ast.EXPR, Char, String] = P( notTest.rep(1, kwd("and")) ).map{
    case Seq(x) => x
    case xs => Ast.EXPR.BoolOp(Ast.BOOL_OP.And, xs)
  }
  val notTest: P[Ast.EXPR] = P( ("not" ~ notTest).map(Ast.EXPR.UnaryOp(Ast.UNARY_OP.Not, _)) | comparison )

  val comparison: P[Ast.EXPR] = P( expr ~ (comp_op ~ expr).rep ).map{
    case (lhs, Nil) => lhs
    case (lhs, chunks) =>
      val (ops, vals) = chunks.unzip
      Ast.EXPR.Compare(lhs, ops, vals)
  }

  // Common operators, mapped from their
  // strings to their type-safe representations
  def op[T](s: P0, rhs: T): core.Parser[T, Char, String] = s.!.map(_ => rhs)
  val LShift = op("<<", Ast.OPERATOR.LShift)
  val RShift = op(">>", Ast.OPERATOR.RShift)
  val Lt = op("<", Ast.COMP_OP.Lt)
  val Gt = op(">", Ast.COMP_OP.Gt)
  val Eq = op("==", Ast.COMP_OP.Eq)
  val GtE = op(">=", Ast.COMP_OP.GtE)
  val LtE = op("<=", Ast.COMP_OP.LtE)
  val NotEq = op("<>" | "!=", Ast.COMP_OP.NotEq)
  val In = op("in", Ast.COMP_OP.In)
  val NotIn = op("not" ~ "in", Ast.COMP_OP.NotIn)
  val Is = op("is", Ast.COMP_OP.Is)
  val IsNot = op("is" ~ "not", Ast.COMP_OP.IsNot)
  val comp_op = P( LtE|GtE|Eq|Gt|Lt|NotEq|In|NotIn|IsNot|Is )
  val Add = op("+", Ast.OPERATOR.Add)
  val Sub = op("-", Ast.OPERATOR.Sub)
  val Pow = op("**", Ast.OPERATOR.Pow)
  val Mult= op("*", Ast.OPERATOR.Mult)
  val Div = op("/", Ast.OPERATOR.Div)
  val Mod = op("%", Ast.OPERATOR.Mod)
  val FloorDiv = op("//", Ast.OPERATOR.FloorDiv)
  val BitOr = op("|", Ast.OPERATOR.BitOr)
  val BitAnd = op("&", Ast.OPERATOR.BitAnd)
  val BitXor = op("^", Ast.OPERATOR.BitXor)
  val UAdd = op("+", Ast.UNARY_OP.UAdd)
  val USub = op("-", Ast.UNARY_OP.USub)
  val Invert = op("~", Ast.UNARY_OP.Invert)
  val unary_op = P ( UAdd | USub | Invert )


  def Unary(p: P[Ast.EXPR]): core.Parser[EXPR.UnaryOp, Char, String] =
    (unary_op ~ p).map{ case (op, operand) => Ast.EXPR.UnaryOp(op, operand) }

  def Chain(p: P[Ast.EXPR], op: P[Ast.OPERATOR]): core.Parser[EXPR, Char, String] =
    P( p ~ (op ~ p).rep ).map {
      case (lhs, chunks) =>
        chunks.foldLeft(lhs){case (lhs, (op, rhs)) =>
          Ast.EXPR.BinOp(lhs, op, rhs)
        }
    }
  val expr: P[Ast.EXPR] = P( Chain(xor_expr, BitOr) )
  val xor_expr: P[Ast.EXPR] = P( Chain(and_expr, BitXor) )
  val and_expr: P[Ast.EXPR] = P( Chain(shift_expr, BitAnd) )
  val shift_expr: P[Ast.EXPR] = P( Chain(arith_expr, LShift | RShift) )

  val arith_expr: P[Ast.EXPR] = P( Chain(term, Add | Sub) )
  val term: P[Ast.EXPR] = P( Chain(factor, Mult | Div | Mod | FloorDiv) )
  // NUMBER appears here and below in `atom` to give it precedence.
  // This ensures that "-2" will parse as `Num(-2)` rather than
  // as `UnaryOp(USub, Num(2))`.
  val factor: P[Ast.EXPR] = P( NUMBER | Unary(factor) | power )
  val power: P[Ast.EXPR] = P( atom ~ trailer.rep ~ (Pow ~ factor).? ).map{
    case (lhs, trailers, rhs) =>
      val left = trailers.foldLeft(lhs)((l, t) => t(l))
      rhs match{
        case None => left
        case Some((op, right)) => Ast.EXPR.BinOp(left, op, right)
      }
  }
  val atom: P[Ast.EXPR] = {
    val empty_tuple = ("(" ~ ")").map(_ => Ast.EXPR.Tuple(Nil, Ast.EXPR_CTX.Load))
    val empty_list = ("[" ~ "]").map(_ => Ast.EXPR.List(Nil, Ast.EXPR_CTX.Load))
    val empty_dict = ("{" ~ "}").map(_ => Ast.EXPR.Dict(Nil, Nil))
    P(
      empty_tuple  |
        empty_list |
        empty_dict |
        "(" ~ (yield_expr | generator | tuple | test) ~ ")" |
        "[" ~ (list_comp | list) ~ "]" |
        "{" ~ dictorsetmaker ~ "}" |
        "`" ~ testlist1.map(x => Ast.EXPR.Repr(Ast.EXPR.Tuple(x, Ast.EXPR_CTX.Load))) ~ "`" |
        STRING.rep(1).map(_.mkString).map(Ast.EXPR.Str) |
        NAME.map(Ast.EXPR.Name(_, Ast.EXPR_CTX.Load)) |
        NUMBER
    )
  }
  val list_contents = P( test.rep(1, ",") ~ ",".? )
  val list = P( list_contents ).map(Ast.EXPR.List(_, Ast.EXPR_CTX.Load))
  val tuple_contents = P( test ~ "," ~ list_contents.?).map { case (head, rest)  => head +: rest.getOrElse(Seq.empty) }
  val tuple = P( tuple_contents).map(Ast.EXPR.Tuple(_, Ast.EXPR_CTX.Load))
  val list_comp_contents = P( test ~ comp_for.rep(1) )
  val list_comp = P( list_comp_contents ).map(Ast.EXPR.ListComp.tupled)
  val generator = P( list_comp_contents ).map(Ast.EXPR.GeneratorExp.tupled)

  val lambdef: P[Ast.EXPR.Lambda] = P( kwd("lambda") ~ varargslist ~ ":" ~ test ).map(Ast.EXPR.Lambda.tupled)
  val trailer: P[Ast.EXPR => Ast.EXPR] = {
    val call = P("(" ~ arglist ~ ")").map{ case (args, (keywords, starargs, kwargs)) => (lhs: Ast.EXPR) => Ast.EXPR.Call(lhs, args, keywords, starargs, kwargs)}
    val slice = P("[" ~ subscriptlist ~ "]").map(args => (lhs: Ast.EXPR) => Ast.EXPR.Subscript(lhs, args, Ast.EXPR_CTX.Load))
    val attr = P("." ~ NAME).map(id => (lhs: Ast.EXPR) => Ast.EXPR.Attribute(lhs, id, Ast.EXPR_CTX.Load))
    P( call | slice | attr )
  }
  val subscriptlist = P( subscript.rep(1, ",") ~ ",".? ).map{
    case Seq(x) => x
    case xs => Ast.SLICE.ExtSlice(xs)
  }
  val subscript: P[Ast.SLICE] = {
    val ellipses = P( ("." ~ "." ~ ".").map(_ => Ast.SLICE.Ellipsis) )
    val single = P( test.map(Ast.SLICE.Index) )
    val multi = P(test.? ~ ":" ~ test.? ~ sliceop.?).map { case (lower, upper, step) =>
      Ast.SLICE.Slice(
        lower,
        upper,
        step.map(_.getOrElse(Ast.EXPR.Name(Ast.Identifier("None"), Ast.EXPR_CTX.Load)))
      )
    }
    P( ellipses | multi | single )
  }

  val sliceop = P( ":" ~ test.? )
  val exprlist: P[Seq[Ast.EXPR]] = P( expr.rep(1, sep = ",") ~ ",".? )
  val testlist: P[Seq[Ast.EXPR]] = P( test.rep(1, sep = ",") ~ ",".? )
  val dictorsetmaker: P[Ast.EXPR] = {
    val dict_item = P( test ~ ":" ~ test )
    val dict: P[Ast.EXPR.Dict] = P(
      (dict_item.rep(1, ",") ~ ",".?).map{x =>
        val (keys, values) = x.unzip
        Ast.EXPR.Dict(keys, values)
      }
    )
    val dict_comp = P(
      (dict_item ~ comp_for.rep(1)).map(Ast.EXPR.DictComp.tupled)
    )
    val set: P[Ast.EXPR.Set] = P( test.rep(1, ",") ~ ",".? ).map(Ast.EXPR.Set)
    val set_comp = P( test ~ comp_for.rep(1) ).map(Ast.EXPR.SetComp.tupled)
    P( dict_comp | dict | set_comp | set)
  }

  val arglist = {
    val inits = P( (plain_argument ~ !"=").rep(0, ",") )
    val later = P( named_argument.rep(0, ",") ~ ",".? ~ ("*" ~ test).? ~ ",".? ~ ("**" ~ test).? )
    P( inits ~ ",".? ~ later )
  }

  val plain_argument = P( test ~ comp_for.rep ).map{
    case (x, Nil) => x
    case (x, gens) => Ast.EXPR.GeneratorExp(x, gens)
  }

  val named_argument = P( NAME ~ "=" ~ test  ).map(Ast.keyword.tupled)

  val comp_for: P[Ast.comprehension] = P( "for" ~ exprlist ~ "in" ~ orTest ~ comp_if.rep ).map{
    case (targets, test, ifs) => Ast.comprehension(tuplize(targets), test, ifs)
  }
  val comp_if: P[Ast.EXPR] = P( "if" ~ test )

  val testlist1: P[Seq[Ast.EXPR]] = P( test.rep(1, sep = ",") )

  // not used in grammar, but may appear in "node" passed from Parser to Compiler
  //  val encoding_decl: P0 = P( NAME )

  val yield_expr: P[Ast.EXPR.Yield] = P( kwd("yield") ~ testlist.map(tuplize).? ).map(Ast.EXPR.Yield)

  val varargslist: P[Ast.arguments] = {
    val named_arg = P( fpdef ~ ("=" ~ test).? )
    val x = P( named_arg.rep(sep = ",") ~ ",".? ~ ("*" ~ NAME).? ~ ",".? ~ ("**" ~ NAME).? ).map{
      case (normal_args, starargs, kwargs) =>
        val (args, defaults) = normal_args.unzip
        Ast.arguments(args, starargs, kwargs, defaults.flatten)
    }
    P( x )
  }

  val fpdef: P[Ast.EXPR] = P( NAME.map(Ast.EXPR.Name(_, Ast.EXPR_CTX.Param)) | "(" ~ fplist ~ ")" )
  val fplist: P[Ast.EXPR] = P( fpdef.rep(sep = ",") ~ ",".? ).map(Ast.EXPR.Tuple(_, Ast.EXPR_CTX.Param))
}