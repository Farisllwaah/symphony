package eu.compassresearch.core.typechecker;

import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class OperationCmlTypeCheckerTestCase extends
		AbstractTypeCheckerTestCase {

	static {
		// 0//
		add("class c = begin operations o:int ==> int o(n) == (return (1)) end");
		// 1//
		add("class c = begin state a:int := 10 operations o:int ==> int o(n) == (return (a + n)) end");
		// 2//
		add("class d = begin state a:int operations c: int ==> () c(v) == a := v end");
		// 3//
		add("class test = begin operations o:int ==> int o(a) == (return (\"ras\")) end",
				true, false);
		// 4//
		add("class test = begin operations o:int ==> int o(a) == (return (let a : int = 2 in \"42\")) end",
				true, false);
		// 5//
		add("process A = B ; C process D = begin @ Skip end", false, false);
		// 6//
		add("class test = begin operations o1:int ==> int o1(a) == let b : int = a+1 in return (c) end",
				true, false);
		// 7// This test checks the positive case for invoking a Cml Operation
		add("process K = begin state f : int := 0 operations op1: int ==> int op1(a) == return (a+1) @ f := op1(10) end");
		// 8//
		add("class test = begin operations o: int ==> int o(a) == return a pre a > 0 post a~ = a end ");
		// 9//
		add("class t = begin types A :: a : int operations o:A==>int o(b) == return b.a pre b.a > 0 post b.a = b~.a end");
		// 10// Fix me the parser cannot handle the b.a in the pre condition ?
		add("class t = begin types A :: a : int operations o:A==>int o(b) == return b.a pre b.a > 0 post b.a = b~.a and 'b.a' = b~.a end",
				true, false);
		// 11// op-call
		add("class test = begin operations op1: int ==> int op1(a) == return (a+1) values k : int = op1(10) end",
				true, false);
		// 12//

	}

	public OperationCmlTypeCheckerTestCase(String cmlSource, boolean parsesOk,
			boolean typesOk, String[] errorMessages) {
		super(cmlSource, parsesOk, typesOk, errorMessages);
	}

	@Parameters
	public static Collection<Object[]> parameter() {
		return testData.get(OperationCmlTypeCheckerTestCase.class);
	}

}