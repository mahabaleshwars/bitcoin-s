package org.scalacoin.crypto

import java.util

import org.bitcoinj.core.{ DumpedPrivateKey}
import org.bitcoinj.core.Transaction.SigHash
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.script.{ScriptOpCodes, ScriptChunk, ScriptBuilder}
import org.scalacoin.protocol.script.{UpdateScriptPubKeyAsm, UpdateScriptPubKeyBytes, ScriptPubKey, ScriptPubKeyFactory}
import org.scalacoin.protocol.transaction._
import org.scalacoin.script.ScriptOperationFactory
import org.scalacoin.script.bitwise.OP_EQUALVERIFY
import org.scalacoin.script.constant._
import org.scalacoin.script.crypto.{OP_CHECKSIG, OP_HASH160, SIGHASH_ALL, OP_CODESEPARATOR}
import org.scalacoin.script.stack.OP_DUP
import org.scalacoin.util.{BitcoinjConversions, ScalacoinUtil, TestUtil}
import org.scalatest.{FlatSpec, MustMatchers}
import scala.collection.JavaConversions._


/**
 * Created by chris on 2/19/16.
 */
class TransactionSignatureSerializerTest extends FlatSpec with MustMatchers {


  "TransactionSignatureSerializer" must "serialize a given script signature without OP_CODESEPARATORS" in {
    val txSerializer = new BaseTransactionSignatureSerializer(TestUtil.transaction)
    val scriptPubKey = TestUtil.scriptPubKey
    val expectedScript = txSerializer.removeOpCodeSeparators(scriptPubKey)
    txSerializer.serializeScriptCode(scriptPubKey) must be (expectedScript)
  }


  it must "hash a multisignature SIGHASH_ALL correctly" in {


    val params = TestNet3Params.get();
    val key1 = new DumpedPrivateKey(params, "cVLwRLTvz3BxDAWkvS3yzT9pUcTCup7kQnfT2smRjvmmm1wAP6QT").getKey();
    val key2 = new DumpedPrivateKey(params, "cTine92s8GLpVqvebi8rYce3FrUYq78ZGQffBYCS1HmDPJdSTxUo").getKey();
    val key3 = new DumpedPrivateKey(params, "cVHwXSPRZmL9adctwBwmn4oTZdZMbaCsR5XF6VznqMgcvt1FDDxg").getKey();
    val multiSigScript : org.bitcoinj.script.Script = ScriptBuilder.createMultiSigOutputScript(2, util.Arrays.asList(key1, key2, key3));
    val scriptPubKey = BitcoinjConversions.toScriptPubKey(multiSigScript)
    require(scriptPubKey.hex == ScalacoinUtil.encodeHex(multiSigScript.getProgram), "Script pub key hex not the same as multiSigScript hex")
    val spendingTx = Transaction.factory(bitcoinjMultiSigTransaction.bitcoinSerialize())

    spendingTx.hex must be (ScalacoinUtil.encodeHex(bitcoinjMultiSigTransaction.bitcoinSerialize()))

    val txSignatureSerializer = new BaseTransactionSignatureSerializer(spendingTx)
    val sigBytes : Seq[Byte] = txSignatureSerializer.serialize(0,scriptPubKey,SIGHASH_ALL)
    ScalacoinUtil.encodeHex(sigBytes) must be (createBitcoinjMultiSigScriptHashForSig)
  }


  /**
   * Mimics a test case inside of bitcoinj
   * https://github.com/bitcoinj/bitcoinj/blob/c9cce479624bfd4d6f94824f9da885e24d18ea7c/core/src/test/java/org/bitcoinj/script/ScriptTest.java#L127
   * hashes a bitcoinj tx for a signature
   * @return
   */
  private def createBitcoinjMultiSigScriptHashForSig : String = {
    val params = TestNet3Params.get()
    val key1 = new DumpedPrivateKey(params, "cVLwRLTvz3BxDAWkvS3yzT9pUcTCup7kQnfT2smRjvmmm1wAP6QT").getKey()
    val key2 = new DumpedPrivateKey(params, "cTine92s8GLpVqvebi8rYce3FrUYq78ZGQffBYCS1HmDPJdSTxUo").getKey()
    val key3 = new DumpedPrivateKey(params, "cVHwXSPRZmL9adctwBwmn4oTZdZMbaCsR5XF6VznqMgcvt1FDDxg").getKey()
    val spendTx = bitcoinjMultiSigTransaction
    val multisigScript = ScriptBuilder.createMultiSigOutputScript(2, util.Arrays.asList(key1, key2, key3))
    val sighash = spendTx.hashForSignature(0, multisigScript, SigHash.ALL, false)
    ScalacoinUtil.encodeHex(sighash.getBytes)
  }


  private def bitcoinjMultiSigTransaction : org.bitcoinj.core.Transaction = {
    //https://github.com/bitcoinj/bitcoinj/blob/master/core/src/test/java/org/bitcoinj/script/ScriptTest.java#L127
    val txHex = "01000000013df681ff83b43b6585fa32dd0e12b0b502e6481e04ee52ff0fdaf55a16a4ef61000000006b483045022100a84acca7906c13c5895a1314c165d33621cdcf8696145080895cbf301119b7cf0220730ff511106aa0e0a8570ff00ee57d7a6f24e30f592a10cae1deffac9e13b990012102b8d567bcd6328fd48a429f9cf4b315b859a58fd28c5088ef3cb1d98125fc4e8dffffffff02364f1c00000000001976a91439a02793b418de8ec748dd75382656453dc99bcb88ac40420f000000000017a9145780b80be32e117f675d6e0ada13ba799bf248e98700000000"
    val params = TestNet3Params.get()
    val creditingTx = new org.bitcoinj.core.Transaction(params,ScalacoinUtil.decodeHex(txHex).toArray)
    val output = creditingTx.getOutput(1)
    val spendTx = new org.bitcoinj.core.Transaction(params)
    val address = new org.bitcoinj.core.Address(params, "n3CFiCmBXVt5d3HXKQ15EFZyhPz4yj5F3H")
    val outputScript = org.bitcoinj.script.ScriptBuilder.createOutputScript(address)
    spendTx.addOutput(output.getValue(), outputScript)
    spendTx.addInput(output)
    spendTx

  }





}
