package pl.edu.mimuw.changeanalyzer.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class LazyList<T> implements Iterable<T> {

	private Iterator<T> source;
	private List<T> list;
	
	public LazyList(Iterable<T> source) {
		this.source = source.iterator();
		this.list = new ArrayList<T>();
	}

	@Override
	public Iterator<T> iterator() {
		return new LazyIterator();
	}
	
	private synchronized T get(int index) {
		this.loadElementsFromSource(index);
		return this.list.get(index);
	}
	
	private synchronized boolean hasElementAt(int index) {
		this.loadElementsFromSource(index);
		return this.list.size() > index;
	}
	
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
			return LazyList.this.get(this.position++);
		}
		
	}

}
