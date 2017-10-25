package org.scalatest

trait NotReally extends Assertions {
  import org.scalatest._
 /** 
   * NOT Expect that the value passed as <code>expected</code> equals the value passed as <code>actual</code>.
   * If the <code>actual</code> value equals the <code>expected</code> value
   * (as determined by <code>==</code>), <code>expect</code> returns
   * normally. Else, <code>expect</code> throws an
   * <code>TestFailedException</code> whose detail message includes the expected and actual values.
   *
   * @param expected the expected value
   * @param actual the actual value, which should equal the passed <code>expected</code> value
   * @throws TestFailedException if the passed <code>actual</code> value does not equal the passed <code>expected</code> value.
   */
  def expectnot(expected: Any)(actual: Any) {
     
    if (actual == expected) {
      val (act, exp) = Suite.getObjectsForFailureMessage(actual, expected)
      val s = FailureMessages("expectedButGot", exp, act)
      throw newAssertionFailedException(Some(s), None, 4)
      // throw new TestFailedException(s, 2)
    }
  }
}
