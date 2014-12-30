package pl.edu.mimuw.changeanalyzer.extraction;

import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;

import ch.uzh.ifi.seal.changedistiller.model.entities.ClassHistory;
import ch.uzh.ifi.seal.changedistiller.model.entities.MethodHistory;

/**
 * Class for wrapping a collection of class histories and iterating over
 * histories of all their methods. It allows also for iterating over histories
 * of methods of inner classes.
 * 
 * @author Wiezzel
 */
public class ClassHistoryWrapper implements Iterable<MethodHistory> {
	
	private Collection<ClassHistory> classHistories;
	
	/**
	 * Construct a new ClassHistoryWrapper wrapping given class histories.
	 * 
	 * @param classHistories Class histories to be wrapped
	 */
	public ClassHistoryWrapper(Collection<ClassHistory> classHistories) {
		this.classHistories = classHistories;
	}

	@Override
	public Iterator<MethodHistory> iterator() {
		return new MethodHistoryIterator();
	}
	
	private class MethodHistoryIterator implements Iterator<MethodHistory> {
		
		private Stack<ClassHistory> classHistories;
		private Iterator<MethodHistory> methodHistories;
		private MethodHistory next;
		
		public MethodHistoryIterator() {
			this.classHistories = new Stack<ClassHistory>();
			this.classHistories.addAll(ClassHistoryWrapper.this.classHistories);
			this.loadNext();
		}
		
		private synchronized void loadNext() {
			if (this.methodHistories != null && this.methodHistories.hasNext()) {
				this.next = this.methodHistories.next();
			}
			else if (!this.classHistories.empty()) {
				ClassHistory history = this.classHistories.pop();
				if (history != null) {
					if (history.getInnerClassHistories() != null) {
						this.classHistories.addAll(history.getInnerClassHistories().values());
					}
					if (history.getMethodHistories() != null) {
						this.methodHistories = history.getMethodHistories().values().iterator();
					}
				}
				this.loadNext();
			}
			else {
				this.next = null;
			}
		}

		@Override
		public boolean hasNext() {
			return this.next != null;
		}

		@Override
		public MethodHistory next() {
			MethodHistory next = this.next;
			this.loadNext();
			return next;
		}
		
	}

}
