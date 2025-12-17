# 🧭 Polygon Visibility Visualizer V1.0

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
- Save, remove or turn the Caps into full circles
#Pro Tip: Press Space to repeat the last action.

## 💡 Notes

- Ensure the polygon is **simple** (no self-intersections).
- The visibility region updates automatically when you move the observer or edit the shape.
- linear Caps will be straigt lines

## 🛠️ Technologies

- Java (Swing / AWT)

This implementation of circular visibility  was made following the Algorithm of the Master Thesis of Mikkel Abrahamsen. https://hjemmesider.diku.dk/~jyrki/PE-lab/Mikkel/thesis.pdf . The Algorithm runs in O(n²), n = vertices.

The implematntation of finding the tangent point on a segment, given two points, was made on the basis of "A Novel Method for Drawing a Circle Tangent to Three Circles Lying on a Plane by Straightedge, Compass, and Inversion Circles". https://arxiv.org/abs/1906.00068
