package eu.compassresearch.core.analysis.modelchecker.graphBuilder.event;

import eu.compassresearch.core.analysis.modelchecker.graphBuilder.type.Type;

public class CommEv implements Event {

	private String begin;
	private String middle;
	private Type end;

	public CommEv(String str1, String str2, Type str3) {
		this.begin = str1;
		this.middle = str2;
		this.end = str3;
	}
	public String getBegin() {
		return begin;
	}

	public void setBegin(String begin) {
		this.begin = begin;
	}

	public String getMiddle() {
		return middle;
	}

	public void setMiddle(String middle) {
		this.middle = middle;
	}

	public Type getEnd() {
		return end;
	}

	public void setEnd(Type end) {
		this.end = end;
	}



	@Override
	public String toString() {
		String result = begin;
		if(this.end.toString().length() > 0){
			result = result + this.end.toString();
		}
		return result;
	}

	@Override
	public int hashCode() {
		return ("ev" + getBegin() + getMiddle() + getEnd()).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj instanceof CommEv) {
			CommEv other = (CommEv) obj;
			result = this.getBegin().equals(other.getBegin())
					&& this.getMiddle().equals(other.getMiddle())
					&& this.getEnd().equals(other.getEnd());
		}
		return result;
	}
}
