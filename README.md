# 🧭 Polygon Visibility Visualizer

A stable Java application that lets you draw a polygon and explore circular visibility from a user-defined point.

## 📝 Instructions

1. **Draw the polygon**  
   Click to add vertices in **counter-clockwise (CCW)** order. 

2. **Place the observer point**  
   Click anywhere inside the polygon to place the point of view.

3. **Drag and edit**  
   - Move vertices to edit the polygon shape.  
   - Drag the observer point to update visibility dynamically.

## 🔍 Features

- Interactive polygon drawing (CCW)
- Drag-and-drop editing of vertices
- Observer point placement and movement
- Real-time circular visibility region visualization

## 💡 Notes

- Ensure the polygon is **simple** (no self-intersections).
- The visibility region updates automatically when you move the observer or edit the shape.

## 🛠️ Technologies

- Java (Swing / AWT)

  **NOTE** the project is not yet finished.
  TODOs:
  > Add find Concave Cap method for CW and CCW arcs
  > Add a merge for concave and convex cap to have the correct circular visibility
