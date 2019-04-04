# render

status: porting to github  
version: 0.5  
progress: utilities libraries and assignments 1 and 2 posted.  
TODO: see [TODO list](./TODO.md)

## Overview
This is the Java implementation of a ray-tracing renderer framework that supported teaching *3D Rendering Techniques and
Algorithms* (CSE581) at Oregon Graduate Institute (OGI) in 2002, 2003, and 2004. The basic class outline is:
* **Build a Basic Ray Tracer** - Weeks 1-4 are building a simple ray tracer (single file using 3d geometry libraries). The focus
  is on understanding the basics of 3D vector math, geometry, materials, and lighting; without worrying about rendering
  system design.
* **Create (Work in) an Extensible Framework** - cast what was done in the first assignments into an extensible ray-tracing
  framework. Worry about system design.
* **Extend the Framework** - Add your own extensions to the framework:
  * **Geometry** - Add geometric objects you want to use.
  * **Lights** -
  * **Textures** -
  * **Materials** -
* **Better Rendering**
  * **Oversampling**
  * **Distributed Ray Tracing**
  * **Optimization**
* **Do an Independent Project**

## Project Organization
* **render/javadocs** - The javadocs for the project
* **render/src/java/cip** - The source code
  * **render/src/java/cip/CSE581** - The reference solutions for the class assignments
  * **render/src/java/cip/render** - Core interfaces, utility libraries, geometries, lights, materials, etc. for building
    ray tracers.
* **website** - the updated class website.
 
