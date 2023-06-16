import edu.uwm.cs351.ps.Dictionary;
import edu.uwm.cs351.ps.Name;


public class TestInternals extends Dictionary.TestInternals {
	// most tests inherited
	
	public void test() {
		// don't debug this until TestDictionary passes:
		Dictionary d = new Dictionary();
		// debugString gives a picture of the keys in the table, 
		// where "." is used for null and "-" for the dummy
		assertEquals("........", d.toDebugString()); // at first: eight nulls
		// NB: A, a and 1 all hash to 1
		d.put(new Name("a"), null); // "a" will take the place of the null at index 1.
		assertEquals(Ts(2053904404), d.toDebugString()); // what now?
		d.put(new Name("A"), null); //collision
		assertEquals(Ts(2104286430), d.toDebugString());
		d.put(new Name("1"), null); // collision again.  QUADRATIC!
		assertEquals(Ts(1645842007), d.toDebugString());
		// NB: B, b and 2 all hash to 2
		d.put(new Name("B"), null);
		assertEquals(Ts(1334123737), d.toDebugString());
		// NB: C, c and 3 all hash to 3
		d.put(new Name("C"), null);
		assertEquals(Ts(1987158313), d.toDebugString());
		testRemove(d);
	}
	
	private void testRemove(Dictionary d) {
		assertEquals(".aAB1.C.", d.toDebugString());
		d.remove(new Name("A")); // read about place holders!
		assertEquals(Ts(2096028663), d.toDebugString());
		d.remove(new Name("1"));
		assertEquals(Ts(1305108968), d.toDebugString());
		// NB: B b and 2 all hash to 2
		d.put(new Name("b"), null);
		assertEquals(Ts(2112634189), d.toDebugString());
		testRehash(d);
	}
	
	private void testRehash(Dictionary d) {
		assertEquals(Ts(2112634189), d.toDebugString());
		// I maps to 9, which currently hashes to 1
		d.put(new Name("I"), null);
		assertEquals(Ts(1979846564), d.toDebugString());
		// H maps to 8, which currently hashes to 0
		d.put(new Name("H"), null);
		// 6 is too many entries.  Read Homework PDF for what capacity we get
		assertEquals(Ti(1448791728), d.toDebugString().length());
		// Entries are placed in new array one by one,
		// by the order they were in the old array,
		// but they don't always end up in the same place!
		// What are the first 10 slots (slots 0 through 9) ?
		// ("H" and "I" will be in their natural slots.)
		assertEquals(Ts(221734579), d.toDebugString().substring(0,10));
	}
}
