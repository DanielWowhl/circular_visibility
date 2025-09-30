package circular_visibility_region;

import java.util.ArrayList;
import java.util.List;

import drawtool.Edge;

public class fullLinearResult {
	public List<DoorSegment> doors = null;
	public List<Edge> linearVis = new ArrayList<>();

	public fullLinearResult(List<Edge> linearVis, List<DoorSegment> doors) {
		this.doors = doors;
		this.linearVis = linearVis;
	}
}
