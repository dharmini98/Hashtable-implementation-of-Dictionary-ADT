//Dharmini Saravanan
package edu.uwm.cs351.ps;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;


import edu.uwm.cs.junit.LockedTestCase;
import edu.uwm.cs.util.PowersOfTwo;
import edu.uwm.cs351.util.AbstractEntry;

/**
 * A dictionary for a postscript interpreter.
 */
public class Dictionary extends AbstractMap<Name,Object> {

	private static class Entry extends AbstractEntry<Name,Object> { //ensure that all abstract methods are implemented
		final Name key;
		Object data;

		Entry(Name k, Object v) { 
			key = k;
			data = v;
		}

		@Override
		public Name getKey() {
			return key;
		}
		@Override
		public Object getValue() {
			return data;
		}
		@Override
		public Object setValue(Object v) { //sets the value to v and return old value
			Object old = data;
			data = v;
			return old;
		}

	}

	private static final int INITIAL_CAPACITY = 8; // must be a power of two
	private static final float FILL_FRACTION = 2/3.0f;
	private Entry[] table; // must not be null, must have power of two length >= INITIAL_CAPACITY
	private Entry dummy;   // must not be null, must have a null key
	private int count;     // must equal the number in "created chain"
	private int used;      // must equal the number of slots filled in table
	private int version;
	// Additional invariant
	// - the entries in the table must be the same as those in the "created" chain plus number of slots with dummy
	// - none of the entries in the table can have null keys unless they are the dummy node
	// - every entry is a dummy or is in correct place in the table
	// - no two entries can have the same key
	// - number of used spots is no more than FILL_FRACTION times the table length;


	// DO NOT CHANGE: Used for reporting invariant problems
	private static Consumer<String> logger = (s) -> System.out.println("Invariant error: " + s);

	private static boolean report(String s) {
		logger.accept(s);
		return false;
	}

	
	private boolean wellFormed() {
		// TODO check that everything is OK
		if(dummy.getKey()!=null||dummy==null)return report("dummy is not proper");
		if(table==null)return report("Table is null");
		if(table.length < INITIAL_CAPACITY) return report("capacity is less then expected");
		if(PowersOfTwo.contains(table.length)==false)return report("Length of table is not proper");
		int n = 0;
		int c=0;
		for(int i=0;i<table.length;i++)
		{
			if(table[i]!=null)
			{
				if(table[i].key!=null)
			    c++;
				else if(table[i]!=dummy)
						return report("The entry with null is not dummy");  //Checks that if the entry key is null but entry
				                                                          //itself not null, it should be dummy or else issue
						n++;
				
			}
		}
		for(int i=0;i<table.length;i++)
		{
			if(table[i]!=null && table[i]!=dummy)    //if its a valid entry value check if its in the right position
			{
				int temp=findIndex(table[i].getKey());
				if(temp!=i)
					return report("The entry is not in the right place");
			}
		}
		if(used!=n)
			return report("The number of used entries is not correct");
		if(c!=count)
			return report("The count is not correct");
		if(used>=FILL_FRACTION*table.length)
			return report("The fraction condition has failed");
		return true;
		
	}
	

	// The next two methods are used by the test driver
	// to make sure your hash table is properly structured.  Don't change them.

	/**
	 * Return the size of the internal table.
	 * For debugging purposes only.
	 * @return size of internal table
	 */
	public int getCapacity() {
		return table.length;
	}

	/**
	 * Return the i'th element of the 
	 * internal table.  For debugging purposes only.
	 * @param i must be between 0 (inclusive) and capacity (exclusive)
	 */
	public Map.Entry<Name, Object> getInternal(int i) {
		Entry s = table[i];
		return s;
	}


	private Dictionary(boolean ignored) {} // used for debugging. Do not change
	
	/**
	 * Create an empty dictionary.
	 */
	public Dictionary() {
		// TODO: Initialize data structure
		table=new Entry[INITIAL_CAPACITY];
		dummy=new Entry(null, null);
		count=0;
		used=0;
		version=1;
		assert wellFormed() : "invariant broken in constructor";
		
	}
	/**
	 * This method is used to find the index of the key given as parameter
	 * @param k
	 * @return index 
	 */
    private int findIndex(Name k)
    {
    	if(k == null) return -1;
		int temp=k.hashCode();
		int len=table.length;
	    for(int i=0;i<len;i++)
		{
			int check=(temp+((i*i)+i)/2)%len;      //Quadratic Probing to find in which index of table the Name k will be found
			check = Math.floorMod(check, len);
			if(table[check]==null)                 //If that spot is empty, return -1
				return -1;
			else if(table[check] != dummy && table[check].key.equals(k))   // If there is an entry which is not dummy
			return check;                                                 // and its key matches with the Name k, return index
		}
    	return -1;
    }

	/**
	 * Return the definition of a name in this dictionary
	 * @param n name to lookup, may not be null
	 * @return definition for the name
	 * @throws ExecutionException if there is no definition, or if the name is null
	 */
    /**
     * This method returns the value at the passed key argument
     * @param n
     * @return
     * @throws ExecutionException
     */
	public Object get(Name n) throws ExecutionException { //Exception is thrown if there is no name, or if name is null
		assert wellFormed() : "invariant broken at start of get()";
		// TODO
		if(n == null) throw new ExecutionException("Name is null");
		int index=findIndex(n);
		if(index==-1)
			throw new ExecutionException("undefined");
		return table[index].getValue();
		//throw new ExecutionException("undefined");
	}
    /**
     * This method method returns the value of the key parameter too, but parameter of any datatype can be passed as argument
     * @param x
     * @return value
     */
	@Override // efficiency
	public Object get(Object x) {
		assert wellFormed() : "invariant broken at start of get()";
		if (!(x instanceof Name)) return null;
		// TODO
		int index=findIndex((Name)x);
		if(index==-1)
			return null;
		return table[index].getValue();
	}

	/**
	 * Return whether the parameter is a name in the dictionary
	 * @param n name to look up, may be null
	 * @return whether n is a name in the dictionary
	 */
	/**
	 * This method returns true or false, based on whether the given name is found in the table
	 * @param n
	 * @return
	 */
	public boolean known(Name n) {
		assert wellFormed() : "invariant broken at start of known()";
		// TODO
		int index=findIndex(n);
		if(index==-1)
			return false;
		return true;
	}

	@Override // efficiency
	public boolean containsKey(Object x) {
		assert wellFormed() : "invariant broken at start of containsKey()";
		if (x instanceof Name) return known((Name)x);
		return false;
	}

	/**
	 * Return the number of names defined in the dictionary.
	 * @return number of names in dictionary.
	 */
	@Override // required
	public int size() {
		assert wellFormed() : "invariant broken at start of size()";
		return count;
	}

	/**
	 * Define a name in the dictionary.
	 * If the name already has a definition, the old definition is replaced
	 * with the new definition.
	 * @return former definition of name (or null, if none)
	 * @param n name to defined (must not be null)
	 * @param x (new) definition of the name (may be null)
	 * @exception ExecutionException if the name is null
	 */
	/**
	 * This method adds in a key and its value into a new entry(if key is not present) or replaces the current value with the value
	 */
	public Object put(Name n, Object x) {
		assert wellFormed() : "invariant broken at start of put()";
		if (n == null || n.rep == null) throw new ExecutionException("null key not allowed");
		Object result = null; 
		// TODO
		int index=findIndex(n);
		if(index!=-1)
		{
			result=table[index].getValue();
			if(result != null && result.equals(x))
			version--;
			table[index].setValue(x);
		}
		else
		{
			version++;
			int len=table.length;
			int temp=n.hashCode();
			int check;
			for(int i=0;i<len;i++)
			{
			
			  check= (temp+((i*i)+i)/2)%len;
			  check = Math.floorMod(check, len);
			   if(table[check]==null)
			   {
				   table[check]=new Entry(n,x);
				   count++;
				   used++;
				   break;
			   }
			   else if(table[check].getKey() == null) 
			   {
				   table[check]=new Entry(n,x);
				   count++;
				   break;
			   }
			}
		}
			if(used>=FILL_FRACTION*table.length)
			{
				Entry[] temptable=new Entry[PowersOfTwo.next(3*table.length)];
				count=0;
				used=0;
				for(int i=0;i<table.length;i++)
				{
					if(table[i]!=null && table[i].getKey()!=null)
					{
						int indice=table[i].getKey().hashCode();
						int tempcheck;
						for(int j=0;j<temptable.length;j++)
						{
							tempcheck = Math.floorMod((indice+(j*j+j)/2), temptable.length);
							if(temptable[tempcheck]==null)
							{
								temptable[tempcheck]=table[i];
								count++;
								used++;
								break;
							}
						}
					}
				}
				table=temptable;
			}
		
		assert wellFormed() : "invariant broken at end of put()";
		return result;
	}

	@Override // efficiency
	public Object remove(Object arg0) {
		assert wellFormed() : "invariant broken at start of remove()";
		if (!(arg0 instanceof Name)) return null;
		Object result = null;
		// TODO
		int index=findIndex((Name)arg0);
		if(index!=-1)
		{
			result=table[index].getValue();
			table[index]=dummy;
			count--;
			version++;
		}
		assert wellFormed() : "invariant broken at end of remove()";
		return result;
	}

	private final Set<Map.Entry<Name, Object>> entrySet = new EntrySet();

	@Override // required
	public Set<Map.Entry<Name, Object>> entrySet() {
		return entrySet;
	}

	/**
	 * Copy all the definitions from the argument into this dictionary
	 * replacing previous definitions (if any).
	 * @param dict1 dictionary whose definition we copy (must not be null)
	 * NB: Behavior if the argument is null is not defined.
	 */
	/**
	 * This method copies the current Dictionary into a new Dictionary
	 * @param dict1
	 */
	public void copy(Dictionary dict1) {
		assert wellFormed() : "invariant broken at start of copy()";
		if (dict1 == this) return;
		for (Entry e : dict1.table) {
			if (e == dict1.dummy || e == null) continue;
			put(e.key, e.data);
		}
		assert wellFormed() : "invariant broken at start of copy()";
	}

	/**
	 * Return a string of the form << name1 value1 name2 value2 ... namek valuek >>
	 * where the names are in order and everything is separated by single spaces.
	 * @see java.lang.Object#toString()
	 */
	@Override // implementation
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<<");
		for (Entry e : table) {
			if (e == dummy || e == null) continue;
			sb.append(" ");
			sb.append(e.key);
			sb.append(" ");
			sb.append(e.data);
		}
		sb.append(" >>");
		return sb.toString();
	}
	
	/**
	 * Return a picture of the keys in the table with "-" for dummy and "." for null
	 * @return picture d=for debugging purposes.
	 */
	public String toDebugString() {
		StringBuilder sb = new StringBuilder();
		if (table == null) return "<null>";
		for (Entry e : table) {
			if (e == null) sb.append(".");
			else if (e == dummy) sb.append('-');
			else sb.append(e.key.rep);
		}
		return sb.toString();
	}

	private class EntrySet extends AbstractSet<Map.Entry<Name,Object>> {

		@Override // efficiency
		public boolean contains(Object arg0) {
			assert wellFormed() : "invariant fails in contains()";
			if (!(arg0 instanceof Map.Entry<?,?>)) return false;
			Map.Entry<?,?> e = (Map.Entry<?,?>) arg0;
			Object key = e.getKey();
			if (!(key instanceof Name)) return false;
			//return super.contains(arg0); // TODO
			int temp = findIndex((Name)key);
			if(temp != -1 && e.equals(table[temp])) return true;
			return false;
		}

		@Override // efficiency
		public boolean remove(Object arg0) {
			assert wellFormed() : "invariant fails in remove()";
			if (!contains(arg0)) return false;
			Map.Entry<?,?> e = (Map.Entry<?,?>) arg0;
			// TODO
			for(int i=0;i<table.length;i++)
			{
				if(e.equals(table[i]))
				{
				  table[i]=dummy;
				  version++;
				  count--;
				  break;
				}
			}
			assert wellFormed() : "invariant fails at end of remove()";
			return true;
		}

		@Override // efficiency
		public void clear() {
			// TODO: if not empty, start array over, but watch out for empty case!
			boolean flag=false;
			for(int i=0;i<table.length;i++)
			{
				if(table[i]!=null && table[i]!=dummy)
			    flag=true;
				table[i]=null;

			}
			if(flag==true)
			version++;
			count=0;
			used=0;
		}

		@Override // required
		public int size() {
			assert wellFormed() : "invariant fails in size()";
			return count;
		}

		@Override // required
		public Iterator<Map.Entry<Name, Object>> iterator() {
			assert wellFormed() : "invariant fails in iterator()";
			return new MyIterator();
		}

	}


	private class MyIterator implements Iterator<Map.Entry<Name, Object>> {
		private int myVersion = version;
		private Entry current;
		private int nextIndex;
        /**
         * This method checks conditions such as if the version matches, current and nextIndex is proper or whether current is 
         * dummy 
         * @return
         */
		private boolean wellFormed() {
			if (!Dictionary.this.wellFormed()) return false;
			if (version != myVersion) return true;
			if (nextIndex < 0 || nextIndex > table.length) return report("nextIndex impossible: " + nextIndex);
			if (nextIndex < table.length) {
				if (table[nextIndex] == null) return report("next entry doesn't exist");
				if (table[nextIndex] == dummy) return report("next entry is dummy");
			}
			if (current == dummy) return report("current is dummy");
			if (current != null) {
				int i = nextIndex-1;
				while (i >= 0) {
					if (table[i] != null && table[i] != dummy) break;
					--i;
				}
				if (i < 0) return report("current is wrong: " + current + " is not null");
				if (table[i] != current) return report("current is wrong: " + current + " is not same as " + table[i]);
			}
			return true;
		}
        /**
         * This method checks if the versions of the iterator and the table match or else throws Concurrent Modification 
         * Exception
         */
		private void checkVersion() {
			if (version != myVersion) throw new ConcurrentModificationException("stale");
		}

		/**
		 * Create an iterator at the start of the dictionary.
		 */
		public MyIterator() {
			// TODO
			nextIndex=table.length;
			current=null;
			for(int i=0;i<table.length;i++) {
				if(table[i] != null && table[i] != dummy) {
					nextIndex = i;
					break;
				}
			}
			assert wellFormed() : "invariant failed in iterator constructor";
		}

		@Override // required
		public boolean hasNext() {
			assert wellFormed() : "invariant failed in hasNext()";
			checkVersion();
			if(nextIndex!=table.length)
				return true;
			return false; // TODO
		}

		@Override // required
		public Map.Entry<Name, Object> next() {
			assert wellFormed() : "invariant fails in next()";
			checkVersion(); // redundant (but OK)
			if (!hasNext()) throw new NoSuchElementException("no more");
			// TODO
			boolean flag=false;
			current=table[nextIndex];
			for(int i=nextIndex+1;i<table.length;i++)
			{
				if(table[i]!=dummy && table[i]!=null)
				{
				nextIndex=i;
				flag=true;
				break;
				}
			}
			if(flag==false)
			nextIndex=table.length;
			assert wellFormed() : "invariant fails in next()";
			return current;
		}

		@Override // implementation
		public void remove() {
			assert wellFormed() : "invariant fails at start of remove()";
			checkVersion();
			if (current == null) throw new IllegalStateException("can't remove");
			// TODO
			int temp=findIndex(current.getKey());
			table[temp]=dummy;
			count--;
			version++;
			current = null;
			myVersion = version;
			assert wellFormed() : "invariant fails at end of remove()";
		}		
	}
    public static class TestInternals extends LockedTestCase {
		private static String lastMessage;
		private static Consumer<String> saveMessage = (s) -> lastMessage = s;
		private static Consumer<String> errorMessage = (s) -> System.err.println("Erroneously reported problem: " + (lastMessage = s));

		private Dictionary self;
        
        private Entry[] a;
        private Entry d;
        private Entry s0, s1, s2, s2a, s2b, s3, s3a, sn;
        
        protected Entry e(String s) {
        	if (s == null) return new Entry(null, null);
        	return new Entry(new Name(s), null);
        }
        
        @Override
        protected void setUp() {
                self = new Dictionary(false);
                a = new Entry[INITIAL_CAPACITY];
                d = e(null);
                s0 = e("@");
                s1 = e("a");
                s2 = e("b");
                s2a = e("B");
                s2b = e("BA".substring(0,1));
                s3 = e("c");
                //s4 = e("D");
                s3a = e("C");
                sn = e(null);
                logger = saveMessage;
        }

		protected void assertWellFormed(boolean expected) {
			logger = expected ? errorMessage : saveMessage;
			lastMessage = null;
			assertEquals(expected, self.wellFormed());
			logger = saveMessage;
			if (expected == false) {
				assertTrue("Didn't report problem", lastMessage != null && lastMessage.trim().length() > 0);
			}
		}

        public void testA() {
                self.table = a;
                self.dummy = d;
                assertWellFormed(true);
        }
        
        public void testB() {
                testA();
                self.table = null;
                assertWellFormed(false);
                self.table = new Entry[4];
                assertWellFormed(false);
                self.table = new Entry[12];
                assertWellFormed(false);
                self.table = new Entry[16];
                assertWellFormed(true);
                self.table = new Entry[256];
                assertWellFormed(true);
                self.table = new Entry[1024];
                assertWellFormed(true);
                self.table = new Entry[1000];
                assertWellFormed(false);
       }
        
        public void testC() {
                testA();
                self.dummy = null;
                assertWellFormed(false);
        }
        
        public void testD() {
                testA();
                self.dummy = s0;
                assertWellFormed(false);
        }
        
        public void testE() {
                testA();
                self.used = 1;
                assertWellFormed(false);
        }
        
        public void testF() {
                testA();
                a[3] = d;
                self.used = 1;
                assertWellFormed(true);
        }
        
        public void testG() {
                testA();
                a[3] = sn;
                self.used = 1;
                assertWellFormed(false);
        }
        
        public void testH() {
                testA();
                self.count = 1;
                self.used = 1;
                a[0] = d;
                assertWellFormed(false);
        }
        
        public void testI() {
                testA();
                self.count = 1;
                self.used = 1;
                a[0] = sn;
                assertWellFormed(false);
        }
        
        public void testJ() {
                testA();
                a[0] = s0;
                self.used = 1;
                assertWellFormed(false);
        }
                                        
        public void testK() {
                testA();
                a[0] = s0;
                self.count = 1;
                assertWellFormed(false);
        }

        public void testM(){
                self.table = a;
                self.dummy = d;
                a[2] = s2;
                self.count = 1;
                self.used = 1;
                assertWellFormed(true);
        }
        
        public void testN() {
                testM();
                self.count = 2;
                assertWellFormed(false);
        }
        
        public void testO() {
                testM();
                self.count = 0;
                assertWellFormed(false);
        }
        
        public void testP() {
                testM();
                a[0] = d;
                assertWellFormed(false);
        }
        
        public void testQ() {
                testM();
                a[5] = d;
                ++self.count;
                ++self.used;
                assertWellFormed(false);
        }
        
        public void testR() {
                testM();
                ++self.used;
                assertWellFormed(false);
        }
        
        public void testS(){
                self.table = a;
                self.dummy = d;
                a[2] = s2;
                a[3] = s3;
                a[5] = s2a;
                self.count = 3;
                self.used = 3;
                assertWellFormed(true);
        }
        
        public void testT() {
                testS();
                a[5] = d;
                assertWellFormed(false);
                --self.count;
                assertWellFormed(true);
                a[0] = d;
                assertWellFormed(false);
                ++self.used;
                assertWellFormed(true);
                ++self.count;
                assertWellFormed(false);
        }
        
        public void testU() {
                testS();
                a[2] = s2b;
                assertWellFormed(false);
                a[2] = s2;
                assertWellFormed(true);
        }
        
        public void testV() {
                testS();
                a[3] = null;
                a[4] = s3;
                assertWellFormed(false);
                a[3] = d;
                assertWellFormed(false);
                ++self.used;
                assertWellFormed(true);
        }
        
        public void testW() {
                self.table = a;
                self.dummy = d;
                a[2] = s2;
                a[3] = s2a;
                a[4] = s3;
                a[6] = s3a;
                self.count = 4;
                self.used = 4;
                assertWellFormed(true);
        }
        
        public void testX() {
                testW();
                a[0] = d;
                assertWellFormed(false);
                ++self.used;
                assertWellFormed(true);
                a[1] = d;
                ++self.used; // now 6!
                assertWellFormed(false);
        }
        
        public void testY() {
                testW();
                a[3] = null;
                a[5] = s2a;
                assertWellFormed(false);
                a[3] = s2a;
                assertWellFormed(false);
                a[3] = sn;
                assertWellFormed(false);
                a[3] = s2a;
                a[5] = null;
                self.count = 3;
                assertWellFormed(false);
        }
        
        public void testZ() {
                testW();
                a[0] = s0;
                assertWellFormed(false);
                self.count = 5;
                self.used = 5;
                assertWellFormed(true);
                a[1] = s1;
                self.count = 6;
                self.used = 6;
                assertWellFormed(false);
                self.table = a = new Entry[16];
                a[0] = s0;
                a[1] = s1;
                a[2] = s2;
                a[3] = s2a;
                a[4] = s3;
                a[6] = s3a;
                assertWellFormed(true);
                a[9] = a[10] = a[11] = a[5] = d;
                self.used += 4; // now 10
                assertWellFormed(true);
                a[12] = d;
                ++self.used; // now 11
                assertWellFormed(false);
        }
    }
}
