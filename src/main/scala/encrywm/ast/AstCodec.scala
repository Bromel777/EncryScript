package encrywm.ast

import encrywm.ast.Ast._
import encrywm.lib.Types
import encrywm.lib.Types.ESType
import scodec.Codec
import scodec.codecs.{Discriminated, Discriminator, uint2, uint4, uint8}

object AstCodec {

  import scodec.codecs.implicits._

  implicit def dRoot = Discriminated[TREE_ROOT, Int](uint2)
  implicit def dCon: Discriminator[TREE_ROOT, TREE_ROOT.Contract, Int] = dRoot.bind[TREE_ROOT.Contract](0)
  implicit def dRExp = dRoot.bind[TREE_ROOT.Expression](1)

  implicit def dT = Discriminated[ESType, Int](uint8)
  implicit def dUnit = dT.bind[Types.ESUnit.type](0)
  implicit def dBool = dT.bind[Types.ESBoolean.type](1)
  implicit def dInt = dT.bind[Types.ESInt.type](2)
  implicit def dLong = dT.bind[Types.ESLong.type](3)
  implicit def dStr = dT.bind[Types.ESString.type](6)
  implicit def dBytes = dT.bind[Types.ESByteVector.type](7)
  implicit def dList = dT.bind[Types.ESList](8)
  implicit def dDict = dT.bind[Types.ESDict](9)
  implicit def dOpt = dT.bind[Types.ESOption](10)
  implicit def dTr = dT.bind[Types.ESTransaction.type](11)
  implicit def dSte = dT.bind[Types.ESState.type](12)
  implicit def dCtx = dT.bind[Types.ESContext.type](13)
  implicit def dProof = dT.bind[Types.ESProof.type](14)
  implicit def dSig = dT.bind[Types.Signature25519.type](15)
  implicit def dProp = dT.bind[Types.ESProposition.type](16)
  implicit def dAp = dT.bind[Types.AccountProposition.type](17)
  implicit def dBx = dT.bind[Types.ESBox.type](18)
  implicit def dAbx = dT.bind[Types.AssetBox.type](19)
  implicit def dUnlc = dT.bind[Types.ESUnlocker.type](20)
  implicit def dFn = dT.bind[Types.ESFunc](21)
  implicit def dNi = dT.bind[Types.Nit.type](22)
  implicit def dAny = dT.bind[Types.ESAny.type](23)
  implicit def dScr = dT.bind[Types.ESScript.type](24)
  implicit def dMuls = dT.bind[Types.MultiSig.type](25)
  implicit def dTObj = dT.bind[Types.ESTypedObject](26)
  implicit def dOpp = dT.bind[Types.OpenProposition.type](27)
  implicit def dCp = dT.bind[Types.ContractProposition.type](28)
  implicit def dHp = dT.bind[Types.HeightProposition.type](29)
  implicit def dTsT = dT.bind[Types.SDObject.type](30)
  implicit def dAib = dT.bind[Types.AssetIssuingBox.type](31)
  implicit def dDbx = dT.bind[Types.DataBox.type](32)

  implicit def dSt: Discriminated[STMT, Int] = Discriminated[STMT, Int](uint4)
  implicit def dFnDef = dSt.bind[STMT.FunctionDef](0)
  implicit def dRet = dSt.bind[STMT.Return](1)
  implicit def dAsg = dSt.bind[STMT.Let](2)
  implicit def dIf = dSt.bind[STMT.If](3)
  implicit def dAsrt = dSt.bind[STMT.Assert](4)
  implicit def dExpSt = dSt.bind[STMT.Expr](5)
  implicit def dUnl = dSt.bind[STMT.UnlockIf](6)
  implicit def dMatch = dSt.bind[STMT.Match](7)
  implicit def dCase = dSt.bind[STMT.Case](8)
  implicit def dHalt = dSt.bind[STMT.Halt.type](9)
  implicit def dPass = dSt.bind[STMT.Pass.type](10)

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
  implicit def dTrue = dEx.bind[EXPR.True.type](11)
  implicit def dFalse = dEx.bind[EXPR.False.type](12)
  implicit def dStrEx = dEx.bind[EXPR.Str](13)
  implicit def dB58Str = dEx.bind[EXPR.Base58Str](14)
  implicit def dAttribute = dEx.bind[EXPR.Attribute](15)
  implicit def dSubscript = dEx.bind[EXPR.Subscript](16)
  implicit def dName = dEx.bind[EXPR.Name](17)
  implicit def dDictEx = dEx.bind[EXPR.ESDictNode](18)
  implicit def dSet = dEx.bind[EXPR.ESSet](19)
  implicit def dListEx = dEx.bind[EXPR.ESList](20)
  implicit def dTuple = dEx.bind[EXPR.ESTuple](21)
  implicit def dSizeOf = dEx.bind[EXPR.SizeOf](22)
  implicit def dIsD = dEx.bind[EXPR.IsDefined](23)
  implicit def dExi = dEx.bind[EXPR.Exists](24)
  implicit def dGet = dEx.bind[EXPR.Get](25)
  implicit def dSum = dEx.bind[EXPR.Sum](26)
  implicit def dMap = dEx.bind[EXPR.Map](27)
  implicit def dDecl = dEx.bind[EXPR.Declaration](28)
  implicit def dBpd = dEx.bind[EXPR.TypeMatching](29)
  implicit def dScM = dEx.bind[EXPR.SchemaMatching](30)
  implicit def dGc = dEx.bind[EXPR.GenericCond.type](31)

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

  implicit def dAN = Discriminated[AST_NODE, Int](uint4)
  implicit def dArguments = dAN.bind[Arguments](0)
  implicit def dKeyword = dAN.bind[Keyword](1)
  implicit def dAlias = dAN.bind[Alias](2)

  val codec: Codec[TREE_ROOT] = Codec[TREE_ROOT]
}