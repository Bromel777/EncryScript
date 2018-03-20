package encrywm.frontend.ast

import encrywm.builtins.Types
import encrywm.builtins.Types.TYPE
import encrywm.frontend.ast.Ast._
import scodec.Codec
import scodec.codecs.{Discriminated, uint2, uint4, uint8}

object AstCodec {

  implicit def dRoot = Discriminated[TREE_ROOT, Int](uint2)
  implicit def dCon = dRoot.bind[TREE_ROOT.Contract](0)
  implicit def dRExp = dRoot.bind[TREE_ROOT.Expression](1)

  implicit def dT = Discriminated[TYPE, Int](uint4)
  implicit def dUnit = dT.bind[Types.UNIT.type](0)
  implicit def dBool = dT.bind[Types.BOOLEAN.type](1)
  implicit def dInt = dT.bind[Types.INT.type](2)
  implicit def dLong = dT.bind[Types.LONG.type](3)
  implicit def dFloat = dT.bind[Types.FLOAT.type](4)
  implicit def dDouble = dT.bind[Types.DOUBLE.type](5)
  implicit def dStr = dT.bind[Types.STRING.type](6)
  implicit def dBytes = dT.bind[Types.BYTE_VECTOR.type](7)
  implicit def dList = dT.bind[Types.LIST](8)
  implicit def dDict = dT.bind[Types.DICT](9)
  implicit def dOpt = dT.bind[Types.OPTION](10)
  implicit def dTr = dT.bind[Types.TYPE_REF](11)

  implicit def dSt = Discriminated[STMT, Int](uint4)
  implicit def dFnDef = dSt.bind[STMT.FunctionDef](0)
  implicit def dRet = dSt.bind[STMT.Return](1)
  implicit def dAsg = dSt.bind[STMT.Assign](2)
  implicit def dAugAsg = dSt.bind[STMT.AugAssign](3)
  implicit def dFor = dSt.bind[STMT.For](4)
  implicit def dIf = dSt.bind[STMT.If](5)
  implicit def dAsrt = dSt.bind[STMT.Assert](6)
  implicit def dExpSt = dSt.bind[STMT.Expr](7)
  implicit def dUnl = dSt.bind[STMT.Unlock.type](8)
  implicit def dHalt = dSt.bind[STMT.Halt.type](9)

  implicit def dEx = Discriminated[EXPR, Int](uint8)
  implicit def dBoolOp = dEx.bind[EXPR.BoolOp](0)
  implicit def dBinOp = dEx.bind[EXPR.BinOp](1)
  implicit def dUnaryOp = dEx.bind[EXPR.UnaryOp](2)
  implicit def dLambda = dEx.bind[EXPR.Lambda](3)
  implicit def dIfExpr = dEx.bind[EXPR.IfExp](4)
  implicit def dCompare = dEx.bind[EXPR.Compare](5)
  implicit def dCall = dEx.bind[EXPR.Call](6)
  implicit def dIntConst = dEx.bind[EXPR.IntConst](7)
  implicit def dLongConst = dEx.bind[EXPR.LongConst](8)
  implicit def dFloatConst = dEx.bind[EXPR.FloatConst](9)
  implicit def dDoubleConst = dEx.bind[EXPR.DoubleConst](10)
  implicit def dStrEx = dEx.bind[EXPR.Str](11)
  implicit def dAttribute = dEx.bind[EXPR.Attribute](12)
  implicit def dSubscript = dEx.bind[EXPR.Subscript](13)
  implicit def dName = dEx.bind[EXPR.Name](14)
  implicit def dDictEx = dEx.bind[EXPR.Dict](15)
  implicit def dSet = dEx.bind[EXPR.ESet](16)
  implicit def dListEx = dEx.bind[EXPR.EList](17)
  implicit def dTuple = dEx.bind[EXPR.Tuple](18)
  implicit def dDecl = dEx.bind[EXPR.Decl](19)

  implicit def dECTX = Discriminated[EXPR_CTX, Int](uint4)
  implicit def dLoad = dECTX.bind[EXPR_CTX.Load.type](0)
  implicit def dStore = dECTX.bind[EXPR_CTX.Store.type](1)
  implicit def dParam = dECTX.bind[EXPR_CTX.Param.type](2)
  implicit def dAugLoad = dECTX.bind[EXPR_CTX.AugLoad.type](3)
  implicit def dAugStore = dECTX.bind[EXPR_CTX.AugStore.type](4)

  implicit def dSl = Discriminated[SLICE, Int](uint2)
  implicit def dEllipsis = dSl.bind[SLICE.Ellipsis.type](0)
  implicit def dSlice = dSl.bind[SLICE.Slice](1)
  implicit def dExtSlice = dSl.bind[SLICE.ExtSlice](2)
  implicit def dIndex = dSl.bind[SLICE.Index](3)

  implicit def dBOp = Discriminated[BOOL_OP, Int](uint2)
  implicit def dAnd = dBOp.bind[BOOL_OP.And.type](0)
  implicit def dOr = dBOp.bind[BOOL_OP.Or.type](1)

  implicit def dOp = Discriminated[OPERATOR, Int](uint4)
  implicit def dAdd = dOp.bind[OPERATOR.Add.type](0)
  implicit def dSub = dOp.bind[OPERATOR.Sub.type](1)
  implicit def dMult = dOp.bind[OPERATOR.Mult.type](2)
  implicit def dDiv = dOp.bind[OPERATOR.Div.type](3)
  implicit def dMod = dOp.bind[OPERATOR.Mod.type](4)
  implicit def dPow = dOp.bind[OPERATOR.Pow.type](5)
  implicit def dFloorDiv = dOp.bind[OPERATOR.FloorDiv.type](6)

  implicit def dUnOp = Discriminated[UNARY_OP, Int](uint4)
  implicit def dInvert = dUnOp.bind[UNARY_OP.Invert.type](0)
  implicit def dNot = dUnOp.bind[UNARY_OP.Not.type](1)
  implicit def dUAdd = dUnOp.bind[UNARY_OP.UAdd.type](2)
  implicit def dUSub = dUnOp.bind[UNARY_OP.USub.type](3)

  implicit def dCoOp = Discriminated[COMP_OP, Int](uint4)
  implicit def dEq = dCoOp.bind[COMP_OP.Eq.type](0)
  implicit def dNotEq = dCoOp.bind[COMP_OP.NotEq.type](1)
  implicit def dLt = dCoOp.bind[COMP_OP.Lt.type](2)
  implicit def dLtE = dCoOp.bind[COMP_OP.LtE.type](3)
  implicit def dGt = dCoOp.bind[COMP_OP.Gt.type](4)
  implicit def dGtE = dCoOp.bind[COMP_OP.GtE.type](5)
  implicit def dIs = dCoOp.bind[COMP_OP.Is.type](6)
  implicit def dIsNot = dCoOp.bind[COMP_OP.IsNot.type](7)
  implicit def dIn = dCoOp.bind[COMP_OP.In.type](8)
  implicit def dNotIn = dCoOp.bind[COMP_OP.NotIn.type](9)

  implicit def dEXCPH = Discriminated[EXCP_HANDLER, Int](uint2)
  implicit def dExceptHandler = dEXCPH.bind[EXCP_HANDLER.ExceptHandler](0)

  implicit def dAN = Discriminated[AST_NODE, Int](uint4)
  implicit def dArguments = dAN.bind[Arguments](0)
  implicit def dKeyword = dAN.bind[Keyword](1)
  implicit def dAlias = dAN.bind[Alias](2)

  //val codec: Codec[TREE_ROOT] = Codec[TREE_ROOT]
}
