package pl.edu.mimuw.changeanalyzer.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * Utility class for storing sequences provided by a disposable
 * iterable object.
 * 
 * @author Adam Wierzbicki
 * @param <T> Type of the stored elements
 */
public class LazyList<T> implements Iterable<T> {

	private Iterator<T> source;
	private List<T> list;
	
	/**
	 * Construct a new LazyList.
	 * 
	 * @param source Iterator providing objects to be stored in the list
	 */
	public LazyList(Iterator<T> source) {
		this.source = source;
		this.list = new ArrayList<T>();
	}
	
	/**
	 * Construct a new LazyList.
	 * 
	 * @param source Iterable providing objects to be stored in the list
	 */
	public LazyList(Iterable<T> source) {
		this(source.iterator());
	}

	@Override
	public Iterator<T> iterator() {
		return new LazyIterator();
	}
	
	/**
	 * Get an element of the list with the given index.
	 * 
	 * @param index Index of an element
	 * @return Element with the given index
	 */
	private synchronized T get(int index) {
		this.loadElementsFromSource(index);
		return this.list.get(index);
	}
	
	/**
	 * Check, whether there is an element at the given index.
	 * 
	 * @param index Index to be checked
	 * @return True iff this list contains an element at the given index
	 */
	private synchronized boolean hasElementAt(int index) {
		this.loadElementsFromSource(index);
		return this.list.size() > index;
	}
	
	/**
	 * Load elements from the source up to the given index.
	 * 
	 * @param index Index of the last element to be loaded
	 */
	private void loadElementsFromSource(int index) {
		while (this.list.size() <= index && this.source.hasNext()) {
			this.list.add(this.source.next());
		}
	}
	
	private class LazyIterator implements Iterator<T> {

		private int position = 0;
		
		@Override
		public boolean hasNext() {
			return LazyList.this.hasElementAt(this.position);
		}

		@Override
		public T next() {
			try {
				return LazyList.this.get(this.position++);
			} catch (IndexOutOfBoundsException ex) {
				throw new NoSuchElementException();
			}
		}
		
	}

}
