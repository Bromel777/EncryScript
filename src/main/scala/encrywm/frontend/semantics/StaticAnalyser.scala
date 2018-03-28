package encrywm.frontend.semantics

import encrywm.core.ESMath
import encrywm.core.Types._
import encrywm.ast.Ast._
import encrywm.ast.AstNodeScanner
import encrywm.frontend.semantics.error._
import encrywm.frontend.semantics.scope._
import encrywm.utils.Stack
import scorex.crypto.encode.Base58

import scala.annotation.tailrec
import scala.util.Random

object StaticAnalyser extends AstNodeScanner {

  private lazy val scopes: Stack[ScopedSymbolTable] = new Stack

  private def currentScopeOpt: Option[ScopedSymbolTable] = scopes.currentOpt

  override def scan(node: AST_NODE): Unit = node match {
    case root: TREE_ROOT => scanRoot(root)
    case stmt: STMT => scanStmt(stmt)
    case expr: EXPR => scanExpr(expr)
    case _ => // Do nothing.
  }

  private def scanRoot(node: TREE_ROOT): Unit = node match {
    case c: TREE_ROOT.Contract =>
      scopes.push(ScopedSymbolTable.initialized)
      c.body.foreach(scan)
    case _ => // Do nothing.
  }

  private def scanStmt(node: STMT): Unit = node match {

    case asg: STMT.Assign =>
      scan(asg.value)
      asg.target match {
        case EXPR.Declaration(name: EXPR.Name, typeOpt) =>
          val valueType = inferType(asg.value)
          val typeDeclOpt = typeOpt.map(t =>
            typeByIdent(t.name).getOrElse(throw NameError(t.name))
          )
          typeDeclOpt.foreach(tpe => assertEquals(tpe, valueType))
          addNameToScope(name, valueType)
        case _ => // ???
      }

    case fd: STMT.FunctionDef =>
      val declaredRetType = typeByIdent(fd.returnType.name)
        .getOrElse(throw NameError(fd.returnType.name))
      val paramSymbols = fd.args.args.map { arg =>
        arg.target match {
          case n: EXPR.Name =>
            val valT = arg.typeOpt.flatMap(t => typeByIdent(t.name))
              .getOrElse(throw IllegalExprError)
            ValSymbol(n.id.name, valT)
          case _ => throw IllegalExprError
        }
      }
      currentScopeOpt.foreach(_.insert(FuncSymbol(fd.name.name, declaredRetType, paramSymbols)))
      val fnScope = ScopedSymbolTable(fd.name.name, currentScopeOpt.get)
      scopes.push(fnScope)
      paramSymbols.foreach(s => currentScopeOpt.foreach(_.insert(s)))
      fd.body.foreach(scan)

      val retType = findReturns(fd.body).map(_.value.map { exp =>
        scanExpr(exp)
        exp.tpeOpt.get
      }).foldLeft(Seq[ESType]()) { case (acc, tOpt) =>
        val tpe = tOpt.getOrElse(ESUnit)
        if (acc.nonEmpty) assertEquals(acc.head, tpe)
        acc :+ tpe
      }.headOption.getOrElse(ESUnit)
      assertEquals(declaredRetType, retType)

      scopes.popHead()

    case ret: STMT.Return => ret.value.foreach(scan)

    case expr: STMT.Expr =>
      scan(expr.value)
      inferType(expr.value)

    case ifStmt: STMT.If =>
      scan(ifStmt.test)
      val bodyScope = ScopedSymbolTable(s"if_body_${Random.nextInt()}", currentScopeOpt.get)
      scopes.push(bodyScope)
      ifStmt.body.foreach(scan)
      scopes.popHead()
      val elseScope = ScopedSymbolTable(s"if_else_${Random.nextInt()}", currentScopeOpt.get)
      scopes.push(elseScope)
      ifStmt.orelse.foreach(scan)
      scopes.popHead()

    case STMT.UnlockIf(test) =>
      scanExpr(test)

    case _ => // Do nothing.
  }

  private def scanExpr(node: EXPR): Unit = {
    node match {
      case n: EXPR.Name =>
        assertDefined(n.id.name)

      case bo: EXPR.BoolOp =>
        bo.values.foreach(scanExpr)

      case bin: EXPR.BinOp =>
        Seq(bin.left, bin.right).foreach(scanExpr)

      case EXPR.Call(EXPR.Name(id, _, _), args, keywords, _) =>
        currentScopeOpt.flatMap(_.lookup(id.name)).map { case FuncSymbol(_, _, params) =>
          if (params.size != args.size + keywords.size) throw WrongNumberOfArgumentsError(id.name)
          id.name
        }.getOrElse(throw NameError(id.name))
        args.foreach(scanExpr)
        keywords.map(_.value).foreach(scanExpr)

      case EXPR.Attribute(value, attr, _, _) =>
        if (getAttributeBase(value).getAttrType(attr.name).isEmpty)
          throw NameError(attr.name)

      case cmp: EXPR.Compare =>
        cmp.comparators.foreach(scanExpr)
        scanExpr(cmp.left)

      case uop: EXPR.UnaryOp => scanExpr(uop.operand)

      case ifExp: EXPR.IfExp =>
        Seq(ifExp.test, ifExp.body, ifExp.orelse).foreach(scanExpr)

      case dct: EXPR.ESDictNode =>
        dct.keys.foreach(scan)
        if (!dct.keys.forall(k => k.tpeOpt.get.isPrimitive)) throw IllegalExprError
        dct.values.foreach(scan)

      case lst: EXPR.ESList => lst.elts.foreach(scanExpr)

      case sub: EXPR.Subscript =>
        scanExpr(sub.value)
        sub.slice match {
          case SLICE.Index(idx) =>
            scanExpr(idx)
            val idxT = idx.tpeOpt.get
            sub.value.tpeOpt match {
              case Some(ESList(_)) => assertEquals(idxT, ESInt)
              case Some(ESDict(keyT, _)) => assertEquals(idxT, keyT)
              case _ => throw IllegalExprError
            }
          // TODO: Complete for other SLICE_OPs.
        }

      case EXPR.Base58Str(s) =>
        if (Base58.decode(s).isFailure) throw Base58DecodeError

      case _ => // Do nothing.
    }
    inferType(node)
  }

  private def addNameToScope(name: EXPR.Name, tpe: ESType): Unit = {
    currentScopeOpt.foreach(_.insert(ValSymbol(name.id.name, tpe)))
  }

  @tailrec
  def getAttributeBase(node: AST_NODE): ESProduct = node match {
    case name: EXPR.Name =>
      val sym = currentScopeOpt.flatMap(_.lookup(name.id.name))
        .getOrElse(throw NameError(name.id.name))
      sym match {
        case ValSymbol(_, t: ESProduct) => t
        case _ => throw NotAnObjectError(sym.name)
      }
    case at: EXPR.Attribute => getAttributeBase(at.value)
    case _ => throw IllegalExprError
  }

  private def findReturns(stmts: Seq[STMT]): Seq[STMT.Return] = {

    def findReturnsIn(stmt: STMT): Seq[STMT.Return] = stmt match {
      case ret: STMT.Return => Seq(ret)
      case ifStmt: STMT.If => findReturns(ifStmt.body) ++ findReturns(ifStmt.orelse)
      case fn: STMT.FunctionDef => findReturns(fn.body)
      case _ => Seq.empty
    }

    stmts.flatMap(findReturnsIn)
  }

  private def inferType(exp: EXPR): ESType = {

    val scope = currentScopeOpt.getOrElse(throw MissedContextError)

    def inferTypeIn(e: EXPR): ESType = e.tpeOpt.getOrElse {
      exp match {
        case n: EXPR.Name => scope.lookup(n.id.name)
          .map(_.tpe).getOrElse(throw NameError(n.id.name))

        case a: EXPR.Attribute =>
          getAttributeBase(a).fields.getOrElse(a.attr.name, throw NameError(a.attr.name))

        // TODO: Move type checking to `scan()`.
        case fc: EXPR.Call =>
          fc.func match {
            case n: EXPR.Name =>
              scope.lookup(n.id.name).map { case sym: FuncSymbol =>
                val argTypes = sym.params.map(_.tpe)
                fc.args.map(inferType).zip(argTypes).foreach { case (t1, t2) =>
                  if (t1 != t2) throw TypeMismatchError(t2.identifier, t1.identifier)
                }
                sym.tpe
              }.getOrElse(throw IllegalExprError)

            case _ => throw IllegalExprError
          }

        case bop: EXPR.BinOp =>
          ESMath.ensureZeroDivision(bop.op, bop.right)
          ESMath.BinaryOperationResults.find {
            case (op, (o1, o2), _) =>
              bop.op == op && o1 == inferType(bop.left) && o2 == inferType(bop.right)
          }.map(_._3).getOrElse(throw IllegalOperandError)

        case ifExp: EXPR.IfExp =>
          val bodyType = inferType(ifExp.body)
          val elseType = inferType(ifExp.orelse)
          if (bodyType != elseType) throw IllegalExprError
          bodyType

        case uop: EXPR.UnaryOp => inferType(uop.operand)

        case EXPR.ESList(elts, _, _) =>
          val listT = elts.headOption.map(inferType).getOrElse(ESUnit)  // TODO: Allow creating empty colls?
          elts.tail.foreach(e => assertEquals(listT, inferType(e)))
          ensureNestedColl(elts)
          ESList(listT)

        case EXPR.ESDictNode(keys, vals, _) =>
          val keyT = keys.headOption.map(inferType).getOrElse(ESUnit)
          val valT = vals.headOption.map(inferType).getOrElse(ESUnit)
          keys.tail.foreach(k => assertEquals(keyT, inferType(k)))
          vals.tail.foreach(v => assertEquals(valT, inferType(v)))
          ensureNestedColl(vals)  // TODO: Ensure nested coll for keys?
          ESDict(keyT, valT)

        case EXPR.Subscript(value, SLICE.Index(_), _, _) =>
          inferType(value) match {
            case list: ESList => list.valT
            case dict: ESDict => dict.valT
          }

        case _ => throw IllegalExprError
      }
    }

    val tpe = inferTypeIn(exp)
    if (exp.tpeOpt.isEmpty) exp.tpeOpt = Some(tpe)
    tpe
  }

  private def ensureNestedColl(exps: Seq[EXPR]): Unit = exps.foreach { exp =>
    val expT = exp.tpeOpt.get
    if (expT.isInstanceOf[ESList] || expT.isInstanceOf[ESDict]) throw NestedCollectionError
  }

  private def assertEquals(t1: ESType, t2: ESType): Unit =
    if (t1 != t2) throw TypeMismatchError(t1.identifier, t2.identifier)

  private def assertDefined(n: String): Unit = if (currentScopeOpt.flatMap(_.lookup(n)).isEmpty) throw NameError(n)
}
